/*
 * Copyright (C) 2017 HAT Data Exchange Ltd
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>
 * 2 / 2018
 */
package org.hatdex.hat.api.service.applications

import akka.Done
import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import org.hatdex.hat.api.models.applications.{
  Application,
  ApplicationKind,
  ApplicationStatus,
  HatApplication,
  Version
}
import org.hatdex.hat.api.models.{ AccessToken, DataDebit, EndpointQuery }
import org.hatdex.hat.api.service.richData.{
  DataDebitService,
  RichDataDuplicateDebitException,
  RichDataService
}
import org.hatdex.hat.api.service.{
  DalExecutionContext,
  RemoteExecutionContext,
  StatsReporter
}
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.Tables
import org.hatdex.hat.dal.Tables.ApplicationStatusRow
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.hat.utils.{ FutureTransformations, Utils }
import org.hatdex.libs.dal.HATPostgresProfile.api._
import org.joda.time.DateTime
import play.api.{ Configuration, Logger }
import play.api.cache.AsyncCacheApi
import play.api.libs.json.{ JsObject, JsString, Json }
import play.api.libs.ws.WSClient
import play.api.mvc.RequestHeader

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

class ApplicationStatusCheckService @Inject() (wsClient: WSClient)(
    implicit
    val rec: RemoteExecutionContext) {

  def status(
    statusCheck: ApplicationStatus.Status,
    token: String): Future[Boolean] = {
    statusCheck match {
      case _: ApplicationStatus.Internal ⇒ Future.successful(true)
      case s: ApplicationStatus.External ⇒ status(s, token)
    }
  }

  protected def status(
    statusCheck: ApplicationStatus.External,
    token: String): Future[Boolean] = {
    wsClient
      .url(statusCheck.statusUrl)
      .withHttpHeaders("x-auth-token" → token)
      .withRequestTimeout(5000.millis)
      .get()
      .map(_.status == statusCheck.expectedStatus)
  }
}

class ApplicationsService @Inject() (
    cache: AsyncCacheApi,
    richDataService: RichDataService,
    dataDebitService: DataDebitService,
    statusCheckService: ApplicationStatusCheckService,
    trustedApplicationProvider: TrustedApplicationProvider,
    silhouette: Silhouette[HatApiAuthEnvironment],
    statsReporter: StatsReporter,
    system: ActorSystem,
    configuration: Configuration)(wsClient: WSClient)(implicit val ec: DalExecutionContext) {

  private val logger = Logger(this.getClass)
  private val applicationsCacheDuration: FiniteDuration = 30.minutes

  val adjudicatorAddress =
    configuration.underlying.getString("adjudicator.address")
  val adjudicatorScheme =
    configuration.underlying.getString("adjudicator.scheme")
  val adjudicatorEndpoint = s"${adjudicatorScheme}${adjudicatorAddress}"

  def applicationStatus(id: String, bustCache: Boolean = false)(
    implicit
    hat: HatServer,
    user: HatUser,
    requestHeader: RequestHeader): Future[Option[HatApplication]] = {
    val eventuallyCleanedCache = if (bustCache) {
      cache.remove(s"apps:${hat.domain}")
      cache.remove(appCacheKey(id))
    }
    else { Future.successful(Done) }
    for {
      _ <- eventuallyCleanedCache
      application <- cache
        .get[HatApplication](appCacheKey(id))
        .flatMap {
          case Some(application) => Future.successful(Some(application))
          case None =>
            cache.remove(s"apps:${hat.domain}") // if any item has expired, the aggregated statuses must be refreshed
            for {
              maybeApp <- trustedApplicationProvider.application(id)
              setup <- applicationSetupStatus(id)(hat.db)
              status <- FutureTransformations.transform(
                maybeApp.map(refetchApplicationsStatus(_, Seq(setup).flatten)))
              _ <- status
                .map(
                  s =>
                    cache.set(appCacheKey(id), s._1, applicationsCacheDuration))
                .getOrElse(Future.successful(Done))
            } yield status.map(_._1)
        }
    } yield application
  }

  def applicationStatus()(
    implicit
    hat: HatServer,
    user: HatUser,
    requestHeader: RequestHeader): Future[Seq[HatApplication]] = {
    cache
      .get[Seq[HatApplication]](s"apps:${hat.domain}")
      .flatMap({
        case Some(applications) ⇒ Future.successful(applications)
        case None ⇒
          for {
            apps ← trustedApplicationProvider.applications // potentially caching
            setup ← applicationSetupStatus()(hat.db) // database
            statuses ← Future
              .sequence(apps.map(refetchApplicationsStatus(_, setup)))
            _ ← if (statuses.forall(_._2)) {
              cache.set(
                s"apps:${hat.domain}",
                statuses.unzip._1,
                applicationsCacheDuration)
            }
            else { Future.successful(Done) }
          } yield statuses.unzip._1
      })
  }

  private def refetchApplicationsStatus(
    app: Application,
    setup: Seq[Tables.ApplicationStatusRow])(implicit hat: HatServer, user: HatUser, requestHeader: RequestHeader) = {
    val aSetup = setup.find(_.id == app.id)
    Utils
      .timeFuture(s"${hat.domain} ${app.id} status", logger)(
        collectStatus(app, aSetup))
      .andThen {
        case Success((a, true)) ⇒
          cache.set(appCacheKey(a.application.id), a, applicationsCacheDuration)
      }
  }

  def setup(application: HatApplication)(
    implicit
    hat: HatServer,
    user: HatUser,
    requestHeader: RequestHeader): Future[HatApplication] = {
    // Create and enable the data debit
    val maybeDataDebitSetup: Option[Future[DataDebit]] = for {
      ddId <- application.application.dataDebitId
      ddsRequest <- application.application.dataDebitSetupRequest
    } yield {
      logger.debug(s"Enable data debit $ddId for ${application.application.id}")
      for {
        dds <- dataDebitService
          .createDataDebit(ddId, ddsRequest, user.userId)(hat)
          .recoverWith({
            case _: RichDataDuplicateDebitException =>
              dataDebitService
                .updateDataDebitInfo(ddId, ddsRequest)(hat)
                .flatMap(
                  _ =>
                    dataDebitService.updateDataDebitPermissions(
                      ddId,
                      ddsRequest,
                      user.userId)(hat))
          })
        _ <- dataDebitService.dataDebitEnableNewestPermissions(ddId)(hat)
      } yield dds
    }

    // Set up the app
    val appSetup = for {
      _ <- application.application.kind match {
        case ApplicationKind.Contract(x) =>
          org.hatdex.hat.utils.NetworkRequest
            .joinContract(
              adjudicatorEndpoint,
              hat.hatName,
              io.dataswift.adjudicator.Types
                .ContractId(
                  java.util.UUID.fromString(application.application.id)),
              wsClient)
        case _ => Future.successful(Unit)
      }
      _ <- maybeDataDebitSetup.getOrElse(Future.successful(())) // If data debit was there, must have been setup successfully
      _ <- applicationSetupStatusUpdate(application, enabled = true)(hat.db) // Update status
      _ <- statsReporter.registerOwnerConsent(application.application.id)
      app <- applicationStatus(application.application.id, bustCache = true) // Refetch all information
        .map(
          _.getOrElse(
            throw new RuntimeException(
              "Application information not found during setup")))
    } yield {
      logger.debug(
        s"Application status for ${app.application.id}: active = ${app.active}, setup = ${app.setup}, needs updating = ${app.needsUpdating}")
      app
    }

    appSetup.recoverWith {
      case e: RuntimeException ⇒
        logger.warn(s"Application setup failed: ${e.getMessage}")
        application.application.dataDebitId
          .map(
            dataDebitService.dataDebitDisable(_, cancelAtPeriodEnd = false)(hat))
          .getOrElse(Future.successful(()))
          .map(_ => throw e)
    }
  }

  def disable(application: HatApplication)(
    implicit
    hat: HatServer,
    user: HatUser,
    requestHeader: RequestHeader): Future[HatApplication] = {
    for {
      _ ← application.application.dataDebitId
        .map(
          dataDebitService.dataDebitDisable(_, cancelAtPeriodEnd = false)(hat))
        .getOrElse(Future.successful(())) // If data debit was there, disable
      _ <- applicationSetupStatusUpdate(application, enabled = false)(hat.db) // Update status
      app <- applicationStatus(application.application.id, bustCache = true) // Refetch all information
        .map(
          _.getOrElse(
            throw new RuntimeException(
              "Application information not found during setup")))
    } yield {
      logger.debug(
        s"Application status for ${app.application.id}: active = ${app.active}, setup = ${app.setup}, needs updating = ${app.needsUpdating}")
      app
    }
  }

  def applicationToken(user: HatUser, application: Application)(
    implicit
    hatServer: HatServer,
    requestHeader: RequestHeader): Future[AccessToken] = {
    val customClaims = JsObject(
      Map(
        "application" -> JsString(application.id),
        "applicationVersion" -> JsString(application.info.version.toString())))

    silhouette.env.authenticatorService
      .create(user.loginInfo)
      .map(_.copy(customClaims = Some(customClaims)))
      .flatMap(silhouette.env.authenticatorService.init)
      .map(AccessToken(_, user.userId))
  }

  protected def checkDataDebit(
    app: Application)(implicit hatServer: HatServer): Future[Boolean] = {
    app.permissions.dataRetrieved
      .map { dataBundle =>
        dataDebitService
          .dataDebit(app.dataDebitId.get)(hatServer.db) // dataDebitId will return true if data debit request exists
          .map(
            _.flatMap(_.activePermissions.map(_.bundle.name == dataBundle.name))) // Check that the active bundle (if any) is the same as requested
          .map(_.getOrElse(false)) // If no data debit or active bundle - not active
      }
      .getOrElse(Future.successful(true)) // If no data debit is required - OK
  }

  /*
   * Check application status - assumes application has been set up, so only checks if remote status still ok
   */
  protected def checkStatus(app: Application)(
    implicit
    hatServer: HatServer,
    user: HatUser,
    requestHeader: RequestHeader): Future[(Boolean, String)] = {
    for {
      token <- applicationToken(user, app)
      dd <- checkDataDebit(app)
      status <- statusCheckService.status(app.status, token.accessToken)
    } yield (dd && status, token.accessToken)
  }

  private def mostRecentDataTime(
    app: Application)(implicit hat: HatServer): Future[Option[DateTime]] = {
    app.status.recentDataCheckEndpoint map { endpoint ⇒
      richDataService.propertyDataMostRecentDate(
        Seq(EndpointQuery(endpoint, None, None, None)))(hat.db)
    } getOrElse {
      Future.successful(None)
    }
  }

  private def fastOrDefault[T](timeout: FiniteDuration, default: T)(
    block: => Future[T]): Future[(T, Boolean)] = {
    val fallback = akka.pattern.after(timeout, using = system.scheduler)(
      Future.successful((default, false)))
    Future.firstCompletedOf(Seq(block.map((_, true)), fallback))
  }

  private def collectStatus(
    app: Application,
    setup: Option[ApplicationStatusRow])(
    implicit
    hat: HatServer,
    user: HatUser,
    requestHeader: RequestHeader): Future[(HatApplication, Boolean)] = { // return status as well as flag indicating if it is successfully generated

    setup match {
      case Some(ApplicationStatusRow(_, version, true)) =>
        val eventualStatus =
          fastOrDefault(5.seconds, (false, ""))(checkStatus(app))
        val eventualMostRecentData =
          fastOrDefault(5.seconds, Option[DateTime](null))(
            mostRecentDataTime(app))
        for {
          ((status, _), canCacheStatus) <- eventualStatus
          (mostRecentData, canCacheData) <- eventualMostRecentData
        } yield {
          logger.debug(
            s"Check compatibility between $version and new ${app.status}: ${
              Version(version)
                .greaterThan(app.status.compatibility)
            }")
          (
            HatApplication(
              app,
              setup = true,
              enabled = true,
              active = status,
              Some(app.status.compatibility.greaterThan(Version(version))), // Needs updating if setup version beyond compatible
              dependenciesEnabled = None,
              mostRecentData),
              canCacheStatus && canCacheData)
        }
      case Some(ApplicationStatusRow(_, _, false)) =>
        // If application has been disabled, reflect in status
        Future.successful(
          (
            HatApplication(
              app,
              setup = true,
              enabled = false,
              active = false,
              needsUpdating = None,
              dependenciesEnabled = None,
              mostRecentData = None),
            true))
      case None =>
        Future.successful(
          (
            HatApplication(
              app,
              setup = false,
              enabled = false,
              active = false,
              needsUpdating = None,
              dependenciesEnabled = None,
              mostRecentData = None),
            true))
    }
  }

  protected def applicationSetupStatus()(
    implicit
    db: Database): Future[Seq[ApplicationStatusRow]] = {
    val query = Tables.ApplicationStatus
    db.run(query.result)
  }

  protected def applicationSetupStatus(
    id: String)(implicit db: Database): Future[Option[ApplicationStatusRow]] = {
    val query = Tables.ApplicationStatus.filter(_.id === id)
    db.run(query.result).map(_.headOption)
  }

  protected def applicationSetupStatusUpdate(
    application: HatApplication,
    enabled: Boolean)(implicit db: Database): Future[ApplicationStatusRow] = {
    val status = ApplicationStatusRow(
      application.application.id,
      application.application.info.version.toString,
      enabled)
    val query = Tables.ApplicationStatus.insertOrUpdate(status)
    db.run(query).map(_ => status)
  }

  def appCacheKey(id: String)(implicit hat: HatServer): String =
    s"apps:${hat.domain}:$id"
}
