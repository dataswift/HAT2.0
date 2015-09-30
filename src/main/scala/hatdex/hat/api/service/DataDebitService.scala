package hatdex.hat.api.service

import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import hatdex.hat.authentication.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._
import java.util.UUID

import scala.util.{Failure, Success, Try}

// this trait defines our service behavior independently from the service actor
trait DataDebitService extends HttpService with DatabaseInfo {

  import hatdex.hat.authentication.HatServiceAuthHandler._

  val bundleService: BundleService

  val routes = {
    pathPrefix("dataDebit") {
      accessTokenHandler { implicit user: User =>
        proposeDataDebitApi ~
          retrieveDataDebitValuesApi
      } ~
        userPassHandler { implicit user: User =>
          enableDataDebitApi ~
            disableDataDebitApi ~
            proposeDataDebitApi ~
            retrieveDataDebitValuesApi
        }
    }
  }

  import JsonProtocol._


  def proposeDataDebitApi(implicit user: User) = path("propose") {
    post {
      db.withSession { implicit session =>
        entity(as[ApiDataDebit]) { debit =>
          (debit.kind, debit.bundleContextless, debit.bundleContextual) match {

            case ("contextless", Some(bundle), None) =>
              val dataDebitKey = UUID.randomUUID()
              val newDebit = DataDebitRow(dataDebitKey, LocalDateTime.now(), LocalDateTime.now(), debit.name,
                debit.startDate, debit.endDate, debit.rolling, debit.sell, debit.price,
                enabled = false, "owner", user.userId,
                debit.bundleContextless.flatMap(bundle => bundle.id),
                None,
                "contextless"
              )

              val maybeCreatedDebit = Try((DataDebit returning DataDebit) += newDebit)

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

  def enableDataDebitApi(implicit user: User) = path(JavaUUID / "enable") { dataDebitKey: UUID =>
    put {
      db.withSession { implicit session =>
        val dataDebit = DataDebit.filter(_.dataDebitKey === dataDebitKey).run.headOption

        authorize(hasPermissionModifyDataDebit(dataDebit)) {
          val result = dataDebit map { debit =>
            Try(
              DataDebit.filter(_.dataDebitKey === debit.dataDebitKey)
                .map(dd => (dd.enabled, dd.lastUpdated))
                .update((true, LocalDateTime.now()))
            )
          }

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

        authorize(hasPermissionModifyDataDebit(dataDebit)) {
          val result = dataDebit map { debit =>
            Try(
              DataDebit.filter(_.dataDebitKey === debit.dataDebitKey)
                .map(dd => (dd.enabled, dd.lastUpdated))
                .update((false, LocalDateTime.now()))
            )
          }

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

  def retrieveDataDebitValuesApi(implicit user: User) = path(JavaUUID / "values") { dataDebitKey: UUID =>
    get {
      db.withSession { implicit session =>
        val dataDebit = DataDebit.filter(_.dataDebitKey === dataDebitKey).run.headOption

        authorize(hasPermissionAccessDataDebit(dataDebit)) {
          dataDebit match {
            case Some(debit) =>
              (debit.kind, debit.bundleContextlessId, debit.bundleContextId) match {
                case ("contextless", Some(bundleId), None) =>
                  complete {
                    ApiDataDebitOut.fromDbModel(debit, bundleService.getBundleContextlessValues(bundleId), None)
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

  private def hasPermissionAccessDataDebit(dataDebit: Option[DataDebitRow])(implicit user: User): Boolean = {
    dataDebit match {
      case Some(debit) =>
        (debit.recipientId equals user.userId) && debit.enabled
      case None =>
        false
    }
  }

  private def hasPermissionModifyDataDebit(dataDebit: Option[DataDebitRow])(implicit user: User): Boolean = {
    user.role match {
      case "owner" =>
        true
      case _ =>
        false
    }
  }


}