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

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import org.hatdex.hat.api.models.applications.{ Application, ApplicationStatus, HatApplication, Version }
import org.hatdex.hat.api.models.{ AccessToken, EndpointQuery }
import org.hatdex.hat.api.service.richData.{ DataDebitContractService, RichDataDuplicateDebitException, RichDataService }
import org.hatdex.hat.api.service.{ DalExecutionContext, RemoteExecutionContext }
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.Tables
import org.hatdex.hat.dal.Tables.ApplicationStatusRow
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.libs.dal.HATPostgresProfile.api._
import org.joda.time.DateTime
import play.api.cache.AsyncCacheApi
import play.api.libs.json.{ JsObject, JsString }
import play.api.libs.ws.WSClient
import play.api.mvc.RequestHeader
import play.api.{ Configuration, Logger }

import scala.concurrent.Future
import scala.concurrent.duration._

class ApplicationStatusCheckService @Inject() (wsClient: WSClient)(implicit val rec: RemoteExecutionContext) {

  def status(statusCheck: ApplicationStatus.Status, token: String): Future[Boolean] = {
    statusCheck match {
      case s: ApplicationStatus.Internal => status(s, token)
      case s: ApplicationStatus.External => status(s, token)
    }
  }

  protected def status(statusCheck: ApplicationStatus.Internal, token: String): Future[Boolean] =
    Future.successful(true)

  protected def status(statusCheck: ApplicationStatus.External, token: String): Future[Boolean] =
    wsClient.url(statusCheck.statusUrl)
      .withHttpHeaders("x-auth-token" -> token)
      .get()
      .map(_.status == statusCheck.expectedStatus)
}

class ApplicationsService @Inject() (
    configuration: Configuration,
    cache: AsyncCacheApi,
    richDataService: RichDataService,
    dataDebitContractService: DataDebitContractService,
    statusCheckService: ApplicationStatusCheckService,
    trustedApplicationProvider: TrustedApplicationProvider,
    silhouette: Silhouette[HatApiAuthEnvironment])(implicit val ec: DalExecutionContext) {

  private val logger = Logger(this.getClass)
  private val applicationsCacheDuration: FiniteDuration = 30.minutes

  def applicationStatus(id: String)(implicit hat: HatServer, user: HatUser, requestHeader: RequestHeader): Future[Option[HatApplication]] = {
    cache.getOrElseUpdate(appCacheKey(id), applicationsCacheDuration) {
      for {
        apps <- trustedApplicationProvider.application(id)
        setup <- applicationSetupStatus(id)(hat.db)
        status <- apps.map(collectStatus(_, setup).map(Some(_))).getOrElse(Future.successful(None))
      } yield status
    }
  }

  def applicationStatus()(implicit hat: HatServer, user: HatUser, requestHeader: RequestHeader): Future[Seq[HatApplication]] = {
    cache.getOrElseUpdate(s"apps:${hat.domain}", applicationsCacheDuration) {
      for {
        apps <- trustedApplicationProvider.applications
        setup <- applicationSetupStatus()(hat.db)
        statuses <- Future.sequence(apps
          .map(a => (a, setup.find(_.id == a.id)))
          .map(as => collectStatus(as._1, as._2)))
      } yield statuses
    }
  }

  def setup(application: HatApplication)(implicit hat: HatServer, user: HatUser, requestHeader: RequestHeader): Future[HatApplication] = {
    // Create and enable the data debit
    val maybeDataDebitSetup = for {
      ddRequest <- application.application.permissions.dataRequired
      ddId <- application.application.dataDebitId
    } yield {
      logger.debug(s"Enable data debit $ddId for ${application.application.id}")
      for {
        dd <- dataDebitContractService.createDataDebit(ddId, ddRequest, user.userId)(hat.db)
          .recover({
            case _: RichDataDuplicateDebitException => dataDebitContractService.updateDataDebitBundle(ddId, ddRequest, user.userId)(hat.db)
          })
        _ <- dataDebitContractService.dataDebitEnableBundle(ddId, Some(ddRequest.bundle.name))(hat.db)
      } yield dd
    }

    // Set up the app
    val appSetup = for {
      _ ← maybeDataDebitSetup.getOrElse(Future.successful(())) // If data debit was there, must have been setup successfully
      _ ← applicationSetupStatusUpdate(application, enabled = true)(hat.db) // Update status
      _ ← cache.remove(appCacheKey(application))
      _ ← cache.remove(s"apps:${hat.domain}")
      app ← applicationStatus(application.application.id) // Refetch all information
        .map(_.getOrElse(throw new RuntimeException("Application information not found during setup")))
    } yield {
      logger.debug(s"Application status for ${app.application.id}: active = ${app.active}, setup = ${app.setup}, needs updating = ${app.needsUpdating}")
      app
    }

    appSetup.recoverWith {
      case e: RuntimeException ⇒
        application.application.dataDebitId.map(dataDebitContractService.dataDebitDisable(_)(hat.db))
          .getOrElse(Future.successful(()))
          .map(_ ⇒ throw e)
    }
  }

  def disable(application: HatApplication)(implicit hat: HatServer, user: HatUser, requestHeader: RequestHeader): Future[HatApplication] = {
    for {
      _ <- application.application.dataDebitId.map(dataDebitContractService.dataDebitDisable(_)(hat.db))
        .getOrElse(Future.successful(())) // If data debit was there, disable
      _ <- applicationSetupStatusUpdate(application, enabled = false)(hat.db) // Update status
      _ <- cache.remove(appCacheKey(application))
      _ <- cache.remove(s"apps:${hat.domain}")
      app <- applicationStatus(application.application.id) // Refetch all information
        .map(_.getOrElse(throw new RuntimeException("Application information not found during setup")))
    } yield {
      logger.debug(s"Application status for ${app.application.id}: active = ${app.active}, setup = ${app.setup}, needs updating = ${app.needsUpdating}")
      app
    }
  }

  def applicationToken(user: HatUser, application: Application)(implicit hatServer: HatServer, requestHeader: RequestHeader): Future[AccessToken] = {
    val customClaims = JsObject(Map(
      "application" -> JsString(application.id),
      "applicationVersion" -> JsString(application.info.version.toString())))

    silhouette.env.authenticatorService.create(user.loginInfo)
      .map(_.copy(customClaims = Some(customClaims)))
      .flatMap(silhouette.env.authenticatorService.init)
      .map(AccessToken(_, user.userId))
  }

  protected def checkDataDebit(app: Application)(implicit hatServer: HatServer): Future[Boolean] = {
    app.permissions.dataRequired
      .map { dataDebitRequest =>
        dataDebitContractService.dataDebit(app.dataDebitId.get)(hatServer.db) // dataDebitId will return true if data debit request exists
          .map(_.flatMap(_.activeBundle.map(_.bundle.name == dataDebitRequest.bundle.name))) // Check that the active bundle (if any) is the same as requested
          .map(_.getOrElse(false)) // If no data debit or active bundle - not active
      }
      .getOrElse(Future.successful(true)) // If no data debit is required - OK
  }

  /*
   * Check application status - assumes application has been set up, so only checks if remote status still ok
   */
  protected def checkStatus(app: Application)(implicit hatServer: HatServer, user: HatUser, requestHeader: RequestHeader): Future[(Boolean, String)] = {
    for {
      token <- applicationToken(user, app)
      dd <- checkDataDebit(app)
      status <- statusCheckService.status(app.status, token.accessToken)
    } yield (dd && status, token.accessToken)
  }

  private def mostRecentDataTime(app: Application)(implicit hat: HatServer): Future[Option[DateTime]] = {
    app.status.recentDataCheckEndpoint map { endpoint ⇒
      richDataService.propertyDataMostRecentDate(Seq(EndpointQuery(endpoint, None, None, None)))(hat.db)
    } getOrElse {
      Future.successful(None)
    }
  }

  private def collectStatus(app: Application, setup: Option[ApplicationStatusRow])(
    implicit
    hat: HatServer, user: HatUser, requestHeader: RequestHeader): Future[HatApplication] = {

    setup match {
      case Some(ApplicationStatusRow(_, version, true)) =>
        for {
          (status, token) <- checkStatus(app)
          mostRecentData <- mostRecentDataTime(app)
        } yield {
          logger.debug(s"Check compatibility between $version and new ${app.status}: ${Version(version).greaterThan(app.status.compatibility)}")
          HatApplication(app, setup = true, active = status, Some(token),
            Some(app.status.compatibility.greaterThan(Version(version))), // Needs updating if setup version beyond compatible
            mostRecentData)
        }
      case Some(ApplicationStatusRow(_, _, false)) =>
        // If application has been disabled, reflect in status
        Future.successful(HatApplication(app, setup = true, active = false, applicationToken = None, needsUpdating = None, mostRecentData = None))
      case None =>
        Future.successful(HatApplication(app, setup = false, active = false, applicationToken = None, needsUpdating = None, mostRecentData = None))
    }
  }

  protected def applicationSetupStatus()(implicit db: Database): Future[Seq[ApplicationStatusRow]] = {
    val query = Tables.ApplicationStatus
    db.run(query.result)
  }

  protected def applicationSetupStatus(id: String)(implicit db: Database): Future[Option[ApplicationStatusRow]] = {
    val query = Tables.ApplicationStatus.filter(_.id === id)
    db.run(query.result).map(_.headOption)
  }

  protected def applicationSetupStatusUpdate(application: HatApplication, enabled: Boolean)(implicit db: Database): Future[ApplicationStatusRow] = {
    val status = ApplicationStatusRow(application.application.id, application.application.info.version.toString, enabled)
    val query = Tables.ApplicationStatus.insertOrUpdate(status)
    db.run(query).map(_ => status)
  }

  private def appCacheKey(application: HatApplication)(implicit hat: HatServer): String = s"apps:${hat.domain}:${application.application.id}"
  private def appCacheKey(id: String)(implicit hat: HatServer): String = s"apps:${hat.domain}:$id"
}
