package hatdex.hat.api.endpoints

import java.util.UUID

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models.{ SuccessResponse, ApiError, ErrorMessage }
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.{ AccessToken, User }
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dalNew.Tables._

import org.joda.time.LocalDateTime
import spray.http.StatusCode
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._
import spray.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success, Try }

trait Users extends HttpService with HatServiceAuthHandler {

  val db = DatabaseInfo.db

  val logger: LoggingAdapter

  val routes = {
    pathPrefix("users") {
      createApiUserAccount ~ getAccessToken ~ enableUserAccount ~ suspendUserAccount
    }
  }

  import hatdex.hat.api.json.JsonProtocol._

  def createApiUserAccount = path("user") {
    (userPassHandler | accessTokenHandler) { implicit systemUser: User =>
      authorize(UserAuthorization.hasPermissionCreateUser) {
        post {
          entity(as[User]) { implicit newUser =>
            // Only two types of users can be created via the api
            val maybeUserRole = newUser.role match {
              case "dataDebit"  => Some("dataDebit")
              case "dataCredit" => Some("dataCredit")
              case _            => None
            }

            val fUser = maybeUserRole match {
              case Some(userRole) =>
                val newUserDb = UserUserRow(newUser.userId, LocalDateTime.now(), LocalDateTime.now(),
                  newUser.email, newUser.pass, // The password is assumed to come in hashed, hence stored as is!
                  newUser.name, userRole, enabled = true)
                val createdUser = db.run {
                  ((UserUser returning UserUser) += newUserDb).asTry
                }

                createdUser map {
                  case Success(userDb) => (Created, User(userDb.userId, userDb.email, None, userDb.name, userDb.role))
                  case Failure(e)      => throw ApiError(BadRequest, ErrorMessage("Error when creating user", e.getMessage))
                }
              case None => throw ApiError(Unauthorized, ErrorMessage("Invalid User Role", "Only HAT dataDebit and dataCredit roles can be created via this API"))
            }

            onComplete(fUser) {
              case Success((statusCode: StatusCode, value)) => complete((statusCode, value))
              case Failure(e: ApiError)                     => complete((e.statusCode, e.message))
              case Failure(e)                               => complete((InternalServerError, ErrorMessage("Error while creating user", "Unknown error occurred")))
            }

          }

        }
      }
    }
  }

  def suspendUserAccount = path("user" / JavaUUID / "disable") { userId: UUID =>
    (userPassHandler | accessTokenHandler) { implicit systemUser: User =>
      authorize(UserAuthorization.hasPermissionDisableUser) {
        put {
          val temp = UserUser.filter(_.userId === userId)
            .map(u => (u.enabled)) //            .map(u => (u.enabled, u.lastUpdated))
            .update((false)) //.update((false, LocalDateTime.now()))

          onComplete(db.run(temp.asTry)) {
            case Success(_) => complete((OK, SuccessResponse("Account suspended")))
            case Failure(e) => complete((BadRequest, ErrorMessage("Error suspending account", "Not Suspended")))
          }
        }
      }
    }
  }

  def enableUserAccount = path("user" / JavaUUID / "enable") { userId: UUID =>
    (userPassHandler | accessTokenHandler) { implicit systemUser: User =>
      logger.debug(s"User $systemUser trying to enable $userId")
      authorize(UserAuthorization.hasPermissionEnableUser) {
        logger.debug(s"User $systemUser authorized to enable $userId")
        put {
          val temp = UserUser.filter(_.userId === userId)
            .map(u => (u.enabled)) //            .map(u => (u.enabled, u.lastUpdated))
            .update((true)) //.update((false, LocalDateTime.now()))

          onComplete(db.run(temp.asTry)) {
            case Success(_) => complete((OK, SuccessResponse("Account enabled")))
            case Failure(e) => complete((BadRequest, ErrorMessage("Error enabling account", "Not Enabled")))
          }
        }
      }
    }
  }

  def getAccessToken = path("access_token") {
    // Any password-authenticated user (not only owner)
    userPassApiHandler { implicit user: User =>
      get {
        val maybeToken = db.run(UserAccessToken.filter(_.userId === user.userId).take(1).result).map(_.headOption)

        val response = maybeToken flatMap {
          case Some(token) =>
            Future((OK, AccessToken(token.accessToken, token.userId)))

          case None =>
            val newAccessToken = UserAccessTokenRow(UUID.randomUUID().toString, user.userId)
            db.run((UserAccessToken += newAccessToken).asTry) map {
              case Success(_) => (OK, AccessToken(newAccessToken.accessToken, user.userId))
              case Failure(e) => throw ApiError(InternalServerError, ErrorMessage("Error while creating access_token, please try again later", e.getMessage))
            }
        }

        onComplete(response) {
          case Success((statusCode: StatusCode, value)) => complete((statusCode, value))
          case Failure(e: ApiError)                     => complete((e.statusCode, e.message))
          case Failure(e)                               => complete((InternalServerError, ErrorMessage("Error while retrieving access token", "Unknown error occurred")))
        }
      }
    }
  }
}
