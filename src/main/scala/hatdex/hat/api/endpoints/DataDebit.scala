package hatdex.hat.api.endpoints

import java.util.UUID

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.{ DataDebitOperations, StatsService, DataDebitService }
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.{ UserAuthorization, DataDebitAuthorization }
import hatdex.hat.authentication.models.User
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{ Failure, Success }

// this trait defines our service behavior independently from the service actor
trait DataDebit extends HttpService with DataDebitService with HatServiceAuthHandler with StatsService {

  val db = DatabaseInfo.db

  val logger: LoggingAdapter

  val routes = {
    pathPrefix("dataDebit") {
      proposeDataDebitApi ~
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
          db.withSession { implicit session =>
            entity(as[ApiDataDebit]) { debit =>
              (debit.kind, debit.bundleContextless, debit.bundleContextual) match {

                case ("contextless", Some(bundle), None) =>
                  val maybeCreatedDebit = storeContextlessDataDebit(debit, bundle)
                  session.close()

                  complete {
                    maybeCreatedDebit match {
                      case Success(createdDebit) =>
                        recordDataDebitOperation(createdDebit, user, DataDebitOperations.Create(), "Contextless Data Debit created")
                        (Created, createdDebit)
                      case Failure(e) =>
                        (BadRequest, ErrorMessage("Request to create a contextless data debit is malformed", e.getMessage))
                    }
                  }

                case ("contextual", None, Some(bundle)) =>
                  val maybeCreatedDebit = storeContextDataDebit(debit, bundle)
                  session.close()

                  complete {
                    maybeCreatedDebit match {
                      case Success(createdDebit) =>
                        recordDataDebitOperation(createdDebit, user, DataDebitOperations.Create(), "Contextual Data Debit created")
                        (Created, createdDebit)
                      case Failure(e) =>
                        (BadRequest, ErrorMessage("Request to create a contextual data debit is malformed", e.getMessage))
                    }
                  }

                case _ =>
                  session.close()
                  complete {
                    (BadRequest, ErrorMessage("Request to create a data debit is malformed", "Data debit must be for contextual or contextless data and have associated bundle defined"))
                  }
              }
            }
          }
        }
      }
    }
  }

  def enableDataDebitApi = path(JavaUUID / "enable") { dataDebitKey: UUID =>
    put {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          db.withSession { implicit session =>
            val dataDebit = findDataDebitByKey(dataDebitKey)

            authorize(DataDebitAuthorization.hasPermissionModifyDataDebit(dataDebit)) {
              val result = dataDebit map enableDataDebit
              session.close()
              dataDebit.map { dd =>
                recordDataDebitOperation(ApiDataDebit.fromDbModel(dd), user, DataDebitOperations.Enable(), "Data Debit enabled")
              }

              complete {
                result match {
                  case None =>
                    (NotFound, ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found"))
                  case Some(Success(debit)) =>
                    (OK, SuccessResponse("Data Debit enabled"))
                  case Some(Failure(e)) =>
                    (BadRequest, ErrorMessage("Error enabling DataDebit", e.getMessage))
                }
              }
            }

          }
        }
      }
    }
  }

  def disableDataDebitApi = path(JavaUUID / "disable") { dataDebitKey: UUID =>
    put {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform")) {
          db.withSession { implicit session =>
            val dataDebit = findDataDebitByKey(dataDebitKey)

            authorize(DataDebitAuthorization.hasPermissionModifyDataDebit(dataDebit)) {
              val result = dataDebit map disableDataDebit
              session.close()

              dataDebit.map { dd =>
                recordDataDebitOperation(ApiDataDebit.fromDbModel(dd), user, DataDebitOperations.Disable(), "Data Debit disabled")
              }

              complete {
                result match {
                  case None =>
                    (NotFound, ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found"))
                  case Some(Success(debit)) =>
                    (OK, SuccessResponse("Data Debit disabled"))
                  case Some(Failure(e)) =>
                    (BadRequest, ErrorMessage("Error disabling DataDebit", e.getMessage))
                }
              }
            }

          }
        }
      }
    }
  }

  def retrieveDataDebitValuesApi = path(JavaUUID / "values") { dataDebitKey: UUID =>
    get {
      accessTokenHandler { implicit user: User =>
        val dataDebit = db.withSession { implicit session =>
          val dd = findDataDebitByKey(dataDebitKey)
          session.close()
          dd
        }

        val apiDataDebit = dataDebit.map(ApiDataDebit.fromDbModel)

        authorize(DataDebitAuthorization.hasPermissionAccessDataDebit(dataDebit)) {
          dataDebit match {
            case Some(debit) =>
              logger.debug("Retrieved data debit: "+debit)
              (debit.kind, debit.bundleContextlessId, debit.bundleContextId) match {
                case ("contextless", Some(bundleId), None) =>
                  complete {
                    // TODO: Find out why it is necessary to create a new session when working from within DataDebit
                    db.withSession { implicit session =>
                      val values = retrieveDataDebiValues(debit, bundleId)
                      apiDataDebit.map(dd => recordDataDebitRetrieval(dd, values, user, "Contextless Data Debit Retrieved"))
                      session.close()
                      values
                    }
                  }
                case ("contextual", None, Some(bundleId)) =>
                  complete {
                    db.withSession { implicit session =>
                      val values = bundleContextService.retrieveDataDebitContextualValues(debit, bundleId)
                      apiDataDebit.map(dd => recordDataDebitRetrieval(dd, values, user, "Contextual Data Debit Retrieved"))
                      session.close()
                      values
                    }
                  }
                case _ =>
                  complete {
                    (BadRequest, ErrorMessage("Bad Request", s"Data Debit $dataDebitKey is malformed"))
                  }
              }

            case None =>
              complete {
                (NotFound, ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found"))
              }
          }
        }
      }
    }
  }

  def listDataDebitsApi = pathEnd {
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          db.withSession { implicit session =>
            val apiDebits = listDataDebits
            session.close()

            complete {
              apiDebits
            }

          }
        }
      }
    }
  }
}