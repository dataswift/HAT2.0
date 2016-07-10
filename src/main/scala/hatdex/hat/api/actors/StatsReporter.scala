/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
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
 */
package hatdex.hat.api.actors

import akka.actor.{ Props, ActorLogging, ActorRefFactory, Actor }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.HttpsConnectionContext

import akka.http.scaladsl.model._
import akka.stream.{ ActorMaterializer, Materializer }
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import hatdex.hat.api.actors.StatsReporter.{ PostStats, StatsMessageQueued }
import hatdex.hat.authentication.JwtTokenHandler
import akka.http.scaladsl.model.headers.RawHeader
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.Tables._
import hatdex.hat.dal.SlickPostgresDriver.api._
import org.joda.time.{ LocalDateTime, LocalDate }
import spray.json._
import hatdex.hat.api.{ DatabaseInfo, Api }
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.stats._
import hatdex.hat.api.service.StatsService
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.pipe

object StatsReporter {
  def props: Props = Props[StatsReporter]

  case class StatsMessageQueued(timestamp: LocalDateTime, retries: Int, message: DataStats)
  case class PostStats()
  case class ComputeStorageStats(tableId: Option[Int])
}

class StatsReporter extends Actor with ActorLogging with JwtTokenHandler {
  import StatsReporter._
  val logger = Logging.getLogger(context.system, "Stats")
  logger.info("Stats Reporter Actor starting")

  val exchangeUri = conf.getString("exchange.uri")
  val retryLimit = conf.getInt("exchange.retryLimit")
  val retryTime = conf.getDuration("exchange.retryTime")
  val statsBatchSize = conf.getInt("exchange.batchSize")

  implicit val system = context.system
  val defaultSsslConfig = AkkaSSLConfig()

  lazy val platformUser: Future[User] = {
    val userQuery = UserUser.filter(_.role === "platform").filter(_.enabled === true).take(1)
    val matchingUsers = DatabaseInfo.db.run(userQuery.result)
    matchingUsers.map { users =>
      users.headOption.map(User.fromDbModel).get
    }
  }

  var statsQueue = mutable.Queue[StatsMessageQueued]()

  val reportingScheudle = context.system.scheduler
    .schedule(0 millis, FiniteDuration(retryTime.toMillis, "millis"), self, PostStats())

  def receive: Receive = {
    case stats: DataDebitStats =>
      statsQueue += StatsMessageQueued(LocalDateTime.now(), 0, stats)
    case stats: DataCreditStats =>
      statsQueue += StatsMessageQueued(LocalDateTime.now(), 0, stats)
    case stats: DataStorageStats =>
      logger.info(s"Data storage stats computed: $stats")
      statsQueue += StatsMessageQueued(LocalDateTime.now(), 0, stats)
    case PostStats() =>
      logger.info(s"Trying to post stats")
      postStats()
  }

  import JsonProtocol._
  import spray.json.CollectionFormats
  implicit val materializer: Materializer = ActorMaterializer()
  def postStats() = {
    val fToken = for {
      user <- platformUser
      token <- fetchOrGenerateToken(user, exchangeUri)
    } yield token

    fToken map { authToken =>
      val statsExpired = statsQueue.dequeueAll(_.retries >= retryLimit)
      if (statsExpired.nonEmpty) {
        logger.warning(s"Stats reporting expired for ${statsExpired.length} stats: ${statsExpired.map(_.message).mkString("\n")}")
      }

      val stats = statsQueue.dequeueAll(_.retries < retryLimit)

      for (i <- 0 to (stats.length / statsBatchSize)) {
        val statsBatch = stats.slice(i * statsBatchSize, Math.min((i + 1) * statsBatchSize, stats.length)).toList

        if (statsBatch.nonEmpty) {
          val request = HttpRequest(HttpMethods.POST, Uri(exchangeUri).withPath(Uri.Path(s"/stats/report")))
            .withHeaders(RawHeader("X-Auth-Token", authToken.accessToken))
            .withEntity(HttpEntity(MediaTypes.`application/json`, statsBatch.map(_.message).toJson.toString))

          val result = Http().singleRequest(request).map(_.status) map {
            case StatusCodes.OK =>
              logger.info(s"Stats successfully posted")
            case statusCode =>
              logger.error(s"Error while posting stats: $statusCode")
              statsQueue ++= statsBatch.map(message => message.copy(retries = message.retries + 1))
          } recover {
            case e =>
              logger.error(s"Stats could not be posted: ${e.getMessage}")
              statsQueue ++= statsBatch.map(message => message.copy(retries = message.retries + 1))
          }
        }
      }
    }
  }
}
