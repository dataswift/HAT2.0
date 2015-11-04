package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.DataDebitAuthorization
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._
import java.util.UUID

import scala.util.{Failure, Success, Try}

// this trait defines our service behavior independently from the service actor
trait DataDebitService extends HttpService with HatServiceAuthHandler {

  val bundleService: BundleService
  val db = DatabaseInfo.db

  val logger: LoggingAdapter

  val routes = {
    pathPrefix("dataDebit") {
      proposeDataDebitApi ~
        retrieveDataDebitValuesApi ~
        listDataDebits ~
      userPassHandler { implicit user: User =>
        enableDataDebitApi ~
          disableDataDebitApi
      }
    }
  }

  import JsonProtocol._


  def proposeDataDebitApi = path("propose") {
    post {
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        logger.debug(s"DataDebitService POST /dataDebit/propose authenticated")
        db.withSession { implicit session =>
          entity(as[ApiDataDebit]) { debit =>
            logger.debug(s"DataDebitService POST /dataDebit/propose parsed")
            (debit.kind, debit.bundleContextless, debit.bundleContextual) match {

              case ("contextless", Some(bundle), None) =>
                val dataDebitKey = UUID.randomUUID()
                val newDebit = DataDebitRow(dataDebitKey, LocalDateTime.now(), LocalDateTime.now(), debit.name,
                  debit.startDate, debit.endDate, debit.rolling, debit.sell, debit.price,
                  enabled = false, "owner", user.userId.toString,
                  debit.bundleContextless.flatMap(bundle => bundle.id),
                  None,
                  "contextless"
                )

                val maybeCreatedDebit = Try((DataDebit returning DataDebit) += newDebit)

                session.close()

                complete {
                  maybeCreatedDebit match {
                    case Success(createdDebit) =>
                      val responseDebit = ApiDataDebit.fromDbModel(createdDebit)
                      responseDebit.copy(bundleContextless = Some(bundle))
                    case Failure(e) =>
                      (BadRequest, "Request to create a data debit is malformed: " + e.getMessage)
                  }
                }

              case ("contextual", None, Some(bundle)) =>
                complete {
                  (NotImplemented, "Contextual bundles not yet implemented")
                }

              case _ =>
                complete {
                  (BadRequest, "Request to create a data debit is malformed")
                }
            }
          }
        }
      }
    }
  }

  def enableDataDebitApi(implicit user: User) = path(JavaUUID / "enable") { dataDebitKey: UUID =>
    put {
      db.withSession { implicit session =>
        val dataDebit = DataDebit.filter(_.dataDebitKey === dataDebitKey).run.headOption

        authorize(DataDebitAuthorization.hasPermissionModifyDataDebit(dataDebit)) {
          val result = dataDebit map { debit =>
            Try(
              DataDebit.filter(_.dataDebitKey === debit.dataDebitKey)
                .map(dd => (dd.enabled, dd.lastUpdated))
                .update((true, LocalDateTime.now()))
            )
          }
          session.close()
          complete {
            result match {
              case None =>
                (NotFound, s"Data Debit $dataDebitKey not found")
              case Some(Success(debit)) =>
                OK
              case Some(Failure(e)) =>
                (BadRequest, e.getMessage)
            }
          }
        }

      }
    }
  }

  def disableDataDebitApi(implicit user: User) = path(JavaUUID / "disable") { dataDebitKey: UUID =>
    put {
      db.withSession { implicit session =>
        val dataDebit = DataDebit.filter(_.dataDebitKey === dataDebitKey).run.headOption

        authorize(DataDebitAuthorization.hasPermissionModifyDataDebit(dataDebit)) {
          val result = dataDebit map { debit =>
            Try(
              DataDebit.filter(_.dataDebitKey === debit.dataDebitKey)
                .map(dd => (dd.enabled, dd.lastUpdated))
                .update((false, LocalDateTime.now()))
            )
          }
          session.close()
          complete {
            result match {
              case None =>
                (NotFound, s"Data Debit $dataDebitKey not found")
              case Some(Success(debit)) =>
                OK
              case Some(Failure(e)) =>
                (BadRequest, e.getMessage)
            }
          }
        }

      }
    }
  }

  def retrieveDataDebitValuesApi = path(JavaUUID / "values") { dataDebitKey: UUID =>
    get {
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        db.withSession { implicit session =>
          val dataDebit = DataDebit.filter(_.dataDebitKey === dataDebitKey).run.headOption
          session.close()
          authorize(DataDebitAuthorization.hasPermissionAccessDataDebit(dataDebit)) {
            dataDebit match {
              case Some(debit) =>
                (debit.kind, debit.bundleContextlessId, debit.bundleContextId) match {
                  case ("contextless", Some(bundleId), None) =>
                    complete {
                      // TODO: Find out why it is necessary to create a new session when working from within DataDebit
                      val bundleValues = db.withSession { implicit session =>
                        val result = bundleService.getBundleContextlessValues(bundleId)
                        session.close()
                        result
                      }
                      ApiDataDebitOut.fromDbModel(debit, bundleValues, None)
                    }
                  case ("contextual", None, Some(bundleId)) =>
                    complete {
                      (NotImplemented, "Not yet implemented")
                    }
                  case _ =>
                    complete {
                      (BadRequest, s"Data Debit $dataDebitKey is malformed")
                    }
                }

              case None =>
                complete {
                  (NotFound, s"Data Debit $dataDebitKey not found")
                }
            }
          }
        }
      }
    }
  }

  def listDataDebits = pathEnd {
    get {
      userPassHandler { implicit user =>
        logger.debug(s"DataDebitService GET /dataDebit authenticated")
        db.withSession { implicit session =>
          val dataDebits = DataDebit.run
          session.close()
          val apiDebits = dataDebits.map { dataDebit =>
            ApiDataDebit.fromDbModel(dataDebit)
          }

          logger.debug(s"DataDebitService GET /dataDebit returning " + apiDebits.toString)
          complete {
            apiDebits
          }

        }
      }
    }
  }
}