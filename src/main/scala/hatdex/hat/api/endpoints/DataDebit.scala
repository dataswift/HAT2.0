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
package hatdex.hat.api.endpoints

import java.util.UUID

import akka.actor.{ ActorRefFactory, ActorContext }
import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.{ DataDebitOperations, StatsService, DataDebitService }
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.{ UserAuthorization, DataDebitAuthorization }
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.Tables
import org.joda.time.{ LocalDateTime, DateTime }
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Try, Failure, Success }

// this trait defines our service behavior independently from the service actor
trait DataDebit extends HttpService with DataDebitService with HatServiceAuthHandler with StatsService {

  val db = DatabaseInfo.db

  val logger: LoggingAdapter
  def actorRefFactory: ActorRefFactory

  val routes = {
    pathPrefix("dataDebit") {
      proposeDataDebitApi ~
        getDataDebit ~
        retrieveDataDebitValuesApi ~
        listDataDebitsApi ~
        enableDataDebitApi ~
        disableDataDebitApi
    }
  }

  import JsonProtocol._

  def proposeDataDebitApi = path("propose") {
    post {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataDebit")) {
          entity(as[ApiDataDebit]) { debit =>
            (debit.kind, debit.bundleContextless, debit.bundleContextual) match {
              case ("contextless", Some(bundle), None) => processContextlessDDProposal(debit, bundle)
              case ("contextual", None, Some(bundle))  => processContextualDDProposal(debit, bundle)
              case _ =>
                complete {
                  (BadRequest, ErrorMessage("Request to create a data debit is malformed", "Data debit must be for contextual or contextless data and have associated bundle defined"))
                }
            }
          }
        }
      }
    }
  }

  private def processContextlessDDProposal(debit: ApiDataDebit, bundle: ApiBundleContextless)(implicit user: User) = {
    val eventualDataDebit = storeContextlessDataDebit(debit, bundle)

    onComplete(eventualDataDebit) {
      case Success(createdDebit) =>
        val recordResponse = recordDataDebitOperation(createdDebit, user, DataDebitOperations.Create(), "Contextless Data Debit created")
          .recover { case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}") }
        complete((Created, createdDebit))
      case Failure(e) =>
        complete((BadRequest, ErrorMessage("Request to create a contextless data debit is malformed", e.getMessage)))
    }
  }

  private def processContextualDDProposal(debit: ApiDataDebit, bundle: ApiBundleContext)(implicit user: User) = {
    val eventualDataDebit = storeContextDataDebit(debit, bundle)
    eventualDataDebit map { createdDebit =>
      recordDataDebitOperation(createdDebit, user, DataDebitOperations.Create(), "Contextual Data Debit created")
        .recover { case e =>
          logger.error(s"Error while recording data debit operation: ${e.getMessage}")
            throw e
        }
    }
    onComplete(eventualDataDebit) {
      case Success(createdDebit: ApiDataDebit) => complete((Created, createdDebit))
      case Failure(e)                          => complete((BadRequest, ErrorMessage("Request to create a contextual data debit is malformed", e.getMessage)))
    }
  }

  def enableDataDebitApi = path(JavaUUID / "enable") { dataDebitKey: UUID =>
    put {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          val resp = findDataDebitByKey(dataDebitKey) flatMap { dataDebit =>
            if (DataDebitAuthorization.hasPermissionModifyDataDebit(dataDebit)) {
              val result = dataDebit.map(enableDataDebit)
                .map(eventualDD => eventualDD.map(dd => Some(dd)))
                .getOrElse(Future.successful(None))

              dataDebit.foreach { dd =>
                recordDataDebitOperation(ApiDataDebit.fromDbModel(dd), user, DataDebitOperations.Enable(), "Data Debit enabled") recover {
                  case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}")
                }
              }
              result
            }
            else {
              Future.failed(new SecurityException("You do not have the necessary rights to enable this data debit"))
            }
          }

          onComplete(resp) {
            case Success(None)                 => complete((NotFound, ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found")))
            case Success(Some(debit))          => complete((OK, SuccessResponse("Data Debit enabled")))
            case Failure(e: SecurityException) => complete((Forbidden, ErrorMessage("Forbidden", e.getMessage)))
            case Failure(e)                    => complete((BadRequest, ErrorMessage("Error enabling DataDebit", e.getMessage)))
          }
        }
      }
    }
  }

  def disableDataDebitApi = path(JavaUUID / "disable") { dataDebitKey: UUID =>
    put {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform")) {
          val resp = findDataDebitByKey(dataDebitKey) flatMap { dataDebit =>
            if (DataDebitAuthorization.hasPermissionModifyDataDebit(dataDebit)) {
              val result = dataDebit.map(disableDataDebit)
                .map(eventualDD => eventualDD.map(dd => Some(dd)))
                .getOrElse(Future.successful(None))

              dataDebit.foreach { dd =>
                recordDataDebitOperation(ApiDataDebit.fromDbModel(dd), user, DataDebitOperations.Disable(), "Data Debit disabled") recover {
                  case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}")
                }
              }
              result
            }
            else {
              Future.failed(new SecurityException("You do not have the necessary rights to disable this data debit"))
            }
          }

          onComplete(resp) {
            case Success(None)                 => complete((NotFound, ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found")))
            case Success(Some(debit))          => complete((OK, SuccessResponse("Data Debit enabled")))
            case Failure(e: SecurityException) => complete((Forbidden, ErrorMessage("Forbidden", e.getMessage)))
            case Failure(e)                    => complete((BadRequest, ErrorMessage("Error enabling DataDebit", e.getMessage)))
          }
        }
      }
    }
  }

  def retrieveDataDebitValuesApi = path(JavaUUID / "values") { dataDebitKey: UUID =>
    get {
      accessTokenHandler { implicit user: User =>
        parameters('limit.as[Option[Int]], 'starttime.as[Option[Int]], 'endtime.as[Option[Int]]) {
          (maybeLimit: Option[Int], maybeStartTimestamp: Option[Int], maybeEndTimestamp: Option[Int]) =>
            val maybeStartTime = maybeStartTimestamp.map(t => new DateTime(t * 1000L).toLocalDateTime)
            val maybeEndTime = maybeEndTimestamp.map(t => new DateTime(t * 1000L).toLocalDateTime)

            val eventualMaybeDataDebit = findDataDebitByKey(dataDebitKey)

            val response = eventualMaybeDataDebit flatMap { maybeDataDebit =>
              if (DataDebitAuthorization.hasPermissionAccessDataDebit(maybeDataDebit)) {
                maybeDataDebit map { dataDebit =>
                  val apiDataDebit = ApiDataDebit.fromDbModel(dataDebit)
                  (dataDebit.kind, dataDebit.bundleContextlessId, dataDebit.bundleContextId) match {
                    case ("contextless", Some(bundleId), None) => getContextlessDataDebitValues(dataDebit, bundleId, maybeLimit, maybeStartTime, maybeEndTime)
                    case ("contextual", None, Some(bundleId))  => getContextualDataDebitValues(dataDebit, bundleId, maybeLimit, maybeStartTime, maybeEndTime)
                    case _                                     => Future.failed(new RuntimeException(s"Data Debit ${dataDebit.dataDebitKey} is malformed"))
                  }
                } getOrElse {
                  Future.failed(new RuntimeException("No such Data Debit exists"))
                }
              }
              else {
                Future.failed(new SecurityException("You do not have rights to access values for this data debit"))
              }
            }

            onComplete(response) {
              case Success(dd)                   => complete((OK, dd))
              case Failure(e: SecurityException) => complete((Forbidden, ErrorMessage("Forbidden", e.getMessage)))
              case Failure(e)                    => complete((BadRequest, ErrorMessage("Bad Request", e.getMessage)))
            }
        }
      }
    }
  }

  def getContextlessDataDebitValues(dataDebit: Tables.DataDebitRow, bundleId: Int,
                                    maybeLimit: Option[Int], maybeStartTime: Option[LocalDateTime], maybeEndTime: Option[LocalDateTime])(implicit user: User): Future[ApiDataDebitOut] = {
    val eventualValues = retrieveDataDebiValues(dataDebit, bundleId, maybeLimit, maybeStartTime, maybeEndTime)
    eventualValues map { values =>
      val apiDataDebit = ApiDataDebit.fromDbModel(dataDebit)
      val statsRecorded = recordDataDebitRetrieval(apiDataDebit, values, user, "Contextless Data Debit Retrieved") recover {
        case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}")
      }
      values
    }
  }

  def getContextualDataDebitValues(dataDebit: Tables.DataDebitRow, bundleId: Int,
                                   maybeLimit: Option[Int], maybeStartTime: Option[LocalDateTime], maybeEndTime: Option[LocalDateTime])(implicit user: User): Future[ApiDataDebitOut] = {
    val eventualValues = retrieveDataDebitContextualValues(dataDebit, bundleId)
    eventualValues map {
      case values =>
        val apiDataDebit = ApiDataDebit.fromDbModel(dataDebit)
        recordDataDebitRetrieval(apiDataDebit, values.bundleContextual.getOrElse(Seq()), user, "Contextual Data Debit Retrieved") recover {
          case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}")
        }
        values
    }
  }

  def getDataDebit = path(JavaUUID) { dataDebitKey: UUID =>
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform")) {
          val eventualDebit = for {
            maybeDataDebit <- findDataDebitByKey(dataDebitKey)
          } yield {
            maybeDataDebit.map(ApiDataDebit.fromDbModel)
          }

          onComplete(eventualDebit) {
            case Success(None)                 => complete((NotFound, ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found")))
            case Success(Some(debit))          => complete((OK, debit))
            case Failure(e: SecurityException) => complete((Forbidden, ErrorMessage("Forbidden", e.getMessage)))
            case Failure(e)                    => complete((BadRequest, ErrorMessage("Error enabling DataDebit", e.getMessage)))
          }

        }
      }
    }
  }

  def listDataDebitsApi = pathEnd {
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          val apiDebits = listDataDebits
          onComplete(apiDebits) {
            case Success(debits) => complete(debits)
            case Failure(e) =>
              logger.debug(s"Error while listing Data Debits: ${e.getMessage}")
              complete((InternalServerError, ErrorMessage("Error while listing Data Debits", "Unknown error")))
          }
        }
      }
    }
  }
}