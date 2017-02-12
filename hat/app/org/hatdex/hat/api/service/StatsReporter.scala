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
 * 2 / 2017
 */
package org.hatdex.hat.api.service

import javax.inject.{ Inject, Singleton }

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.impl.authenticators.JWTRS256Authenticator
import org.hatdex.hat.api.models.DataStats
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.SlickPostgresDriver.api._
import org.hatdex.hat.dal.Tables._
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.hat.utils.FutureRetries
import org.hatdex.marketsquare.api.services.MarketsquareClient
import play.api.{ Configuration, Logger }
import play.api.libs.json.{ JsObject, Json }
import play.api.libs.ws.WSClient
import play.api.mvc.RequestHeader

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class StatsReporter @Inject() (wsClient: WSClient, configuration: Configuration, system: ActorSystem,
    authenticatorService: AuthenticatorService[JWTRS256Authenticator, HatServer]) {
  val logger = Logger(this.getClass)

  implicit val scheduler = system.scheduler
  val retryLimit = configuration.underlying.getInt("exchange.retryLimit")
  val retryTime = FiniteDuration(configuration.underlying.getDuration("exchange.retryTime").toMillis, "millis")
  val statsBatchSize = 100

  val msClient = new MarketsquareClient(
    wsClient,
    configuration.underlying.getString("exchange.address"),
    configuration.underlying.getString("exchange.scheme"))
  //  val defaultSsslConfig = AkkaSSLConfig()

  def reportStatistics(stats: Seq[DataStats])(implicit server: HatServer, request: RequestHeader): Future[Unit] = {
    logger.debug(s"Reporting statistics: $stats")
    val logged = for {
      _ <- persistStats(stats)
      outstanding <- retrieveOutstandingStats()
      result <- reportPendingStatistics(outstanding)
    } yield result

    logged recover {
      case e =>
        logger.error(s"Error while reporting stats: ${e.getMessage}")
    }
  }

  protected def reportPendingStatistics(batch: Seq[DataStatsLogRow])(implicit server: HatServer, request: RequestHeader): Future[Unit] = {
    if (batch.isEmpty) {
      Future.successful(())
    }
    else {
      val statsBatch = batch.map(ModelTranslation.fromDbModel)
      for {
        _ <- FutureRetries.retry(uploadStats(statsBatch), FutureRetries.withDefault(List(), retryLimit, retryTime))
        _ <- clearUploadedStats(batch)
        nextBatch <- retrieveOutstandingStats()
        result <- reportPendingStatistics(nextBatch)
      } yield result
    }
  }

  private def clearUploadedStats(stats: Seq[DataStatsLogRow])(implicit server: HatServer): Future[Unit] = {
    server.db.run {
      DataStatsLog.filter(_.statsId inSet stats.map(_.statsId).toSet).delete
    } map { case _ => () }
  }

  private def persistStats(stats: Seq[DataStats])(implicit server: HatServer): Future[Seq[Long]] = {
    import org.hatdex.hat.api.json.DataStatsFormat.dataStatsFormat
    logger.debug(s"Persisting stats $stats")
    val dataStatsLogs = stats map { item =>
      DataStatsLogRow(0, Json.toJson(item))
    }
    server.db.run {
      (DataStatsLog returning DataStatsLog.map(_.statsId)) ++= dataStatsLogs
    }
  }

  private def retrieveOutstandingStats()(implicit server: HatServer): Future[Seq[DataStatsLogRow]] = {
    server.db.run {
      DataStatsLog.take(statsBatchSize).result
    }
  }

  private def uploadStats(stats: Seq[DataStats])(implicit server: HatServer, request: RequestHeader): Future[Unit] = {
    logger.debug(s"Uploading stats $stats")
    val uploaded = for {
      token <- applicationToken()
      result <- msClient.postStats(token, stats)
    } yield {
      result
    }
    uploaded recover {
      case e =>
        logger.error(s"Failed to upload stats: ${e.getMessage}")
        Future.failed(e)
    }
  }

  private def platformUser()(implicit server: HatServer): Future[HatUser] = {
    val userQuery = UserUser.filter(_.role === "platform").filter(_.enabled === true).take(1)
    val matchingUsers = server.db.run(userQuery.result)
    matchingUsers.map { users =>
      users.headOption.map(ModelTranslation.fromDbModel).get
    }
  }

  private def applicationToken()(implicit server: HatServer, request: RequestHeader): Future[String] = {
    val resource = configuration.underlying.getString("exchange.scheme") + configuration.underlying.getString("exchange.address")
    val customClaims = Map("resource" -> Json.toJson(resource), "accessScope" -> Json.toJson("validate"))
    for {
      user <- platformUser()
      authenticator <- authenticatorService.create(user.loginInfo)
      token <- authenticatorService.init(authenticator.copy(customClaims = Some(JsObject(customClaims))))
    } yield token
  }
}
