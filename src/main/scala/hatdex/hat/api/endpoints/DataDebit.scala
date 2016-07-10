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
import org.joda.time.DateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{ Try, Failure, Success }

// this trait defines our service behavior independently from the service actor
trait DataDebit extends HttpService with DataDebitService with HatServiceAuthHandler with StatsService {

  val db = DatabaseInfo.db

  val logger: LoggingAdapter
  def actorRefFactory: ActorRefFactory

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
    val eventualDataDebit = db.withSession { implicit session =>
      val maybeCreatedDebit = storeContextlessDataDebit(debit, bundle)
      session.close()
      maybeCreatedDebit
    }

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
    val maybeCreatedDebit = db.withSession { implicit session =>
      val maybeCreatedDebit: Try[ApiDataDebit] = storeContextDataDebit(debit, bundle)
      session.close()
      maybeCreatedDebit
    }

    complete {
      maybeCreatedDebit match {
        case Success(createdDebit: ApiDataDebit) =>
          val recordResponse = recordDataDebitOperation(createdDebit, user, DataDebitOperations.Create(), "Contextual Data Debit created")
            .recover { case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}") }
          (Created, createdDebit)
        case Failure(e) =>
          (BadRequest, ErrorMessage("Request to create a contextual data debit is malformed", e.getMessage))
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
              dataDebit.foreach { dd =>
                recordDataDebitOperation(ApiDataDebit.fromDbModel(dd), user, DataDebitOperations.Enable(), "Data Debit enabled") recover {
                  case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}")
                }
              }

              complete {
                result match {
                  case None                 => (NotFound, ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found"))
                  case Some(Success(debit)) => (OK, SuccessResponse("Data Debit enabled"))
                  case Some(Failure(e))     => (BadRequest, ErrorMessage("Error enabling DataDebit", e.getMessage))
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

              dataDebit.foreach { dd =>
                recordDataDebitOperation(ApiDataDebit.fromDbModel(dd), user, DataDebitOperations.Disable(), "Data Debit disabled") recover {
                  case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}")
                }
              }

              complete {
                result match {
                  case None                 => (NotFound, ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found"))
                  case Some(Success(debit)) => (OK, SuccessResponse("Data Debit disabled"))
                  case Some(Failure(e))     => (BadRequest, ErrorMessage("Error disabling DataDebit", e.getMessage))
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
        val maybeDataDebit = db.withSession { implicit session =>
          val dd = findDataDebitByKey(dataDebitKey)
          session.close()
          dd
        }

        maybeDataDebit map { dataDebit =>
          val apiDataDebit = ApiDataDebit.fromDbModel(dataDebit)
          authorize(DataDebitAuthorization.hasPermissionAccessDataDebit(maybeDataDebit)) {
            parameters('limit.as[Option[Int]], 'starttime.as[Option[Int]], 'endtime.as[Option[Int]]) {
              (maybeLimit: Option[Int], maybeStartTimestamp: Option[Int], maybeEndTimestamp: Option[Int]) =>
                (dataDebit.kind, dataDebit.bundleContextlessId, dataDebit.bundleContextId) match {
                  case ("contextless", Some(bundleId), None) =>
                    val maybeStartTime = maybeStartTimestamp.map(t => new DateTime(t * 1000L).toLocalDateTime)
                    val maybeEndTime = maybeEndTimestamp.map(t => new DateTime(t * 1000L).toLocalDateTime)
                    val eventualValues = retrieveDataDebiValues(dataDebit, bundleId, maybeLimit, maybeStartTime, maybeEndTime)

                    onComplete(eventualValues) {
                      case Success(values) =>
                        val statsRecorded = recordDataDebitRetrieval(apiDataDebit, values, user, "Contextless Data Debit Retrieved") recover {
                          case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}")
                        }
                        complete(values)
                      case Failure(e) =>
                        complete((BadRequest, ErrorMessage("Error while fetching Data Debit values", s"Does Data Debit $dataDebitKey exist?")))
                    }

                  case ("contextual", None, Some(bundleId)) =>
                    complete {
                      db.withSession { implicit session =>
                        val values = bundleContextService.retrieveDataDebitContextualValues(dataDebit, bundleId)
                        val recordResponse = recordDataDebitRetrieval(apiDataDebit, values, user, "Contextual Data Debit Retrieved") recover {
                          case e =>
                            logger.error(s"Error while recording data debit operation: ${e.getMessage}")
                        }
                        session.close()
                        values
                      }
                    }
                  case _ =>
                    complete {
                      (BadRequest, ErrorMessage("Bad Request", s"Data Debit $dataDebitKey is malformed"))
                    }
                }
            }
          }
        } getOrElse {
          complete {
            (NotFound, ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found"))
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
            case Success(debits) =>
              complete(debits)
            case Failure(e) =>
              logger.debug(s"Error while listing Data Debits: ${e.getMessage}")
              complete((InternalServerError, ErrorMessage("Error while listing Data Debits", "Unknown error")))
          }
        }
      }
    }
  }
}