package hatdex.hat.api.service

import java.util.UUID

import hatdex.hat.api.DatabaseInfo
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.models.{User, AccessToken}
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import hatdex.hat.authentication.authorization.UserAuthorization
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport._

import scala.util.{Failure, Success, Try}

trait UserService extends HttpService with HatServiceAuthHandler {

  val db = DatabaseInfo.db

  val routes = {
    pathPrefix("users") {
      createApiUserAccount ~ getAccessToken ~ enableUserAccount ~ suspendUserAccount
    }
  }

  import hatdex.hat.api.json.JsonProtocol._

  def createApiUserAccount = path("user") {
    accessTokenHandler { implicit systemUser: User =>
      authorize(UserAuthorization.hasPermissionCreateUser) {
        post {
          entity(as[User]) { implicit newUser =>
            db.withSession { implicit session =>
              // Only two types of users can be created via the api
              val maybeUserRole = newUser.role match {
                case "dataDebit" =>
                  Some("dataDebit")
                case "dataCredit" =>
                  Some("dataCredit")
                case _ =>
                  None
              }

              maybeUserRole match {
                case Some(userRole) =>
                  val newUserDb = UserUserRow(newUser.userId, LocalDateTime.now(), LocalDateTime.now(),
                    newUser.email, newUser.pass, // The password is assumed to come in hashed, hence stored as is!
                    newUser.name, userRole, enabled = true)
                  complete {
                    val createdUser = Try((UserUser returning UserUser) += newUserDb)
                    createdUser match {
                      case Success(userDb) =>
                        User(userDb.userId, userDb.email, None, userDb.name, userDb.role)
                      case Failure(e) =>
                        (BadRequest, e.getMessage)
                    }
                  }
                case None =>
                  complete {
                    (Unauthorized, s"Wrong user role ${newUser.role}")
                  }
              }
            }
          }
        }
      }
    }
  }

  def suspendUserAccount = path("user" / JavaUUID / "disable") { userId: UUID =>
    accessTokenHandler { implicit systemUser: User =>
      authorize(UserAuthorization.hasPermissionDisableUser) {
        put {
          db.withSession { implicit session =>
            complete {
              Try(
                UserUser.filter(_.userId === userId)
                  .map(dd => (dd.enabled, dd.lastUpdated))
                  .update((false, LocalDateTime.now()))
              ) match {
                case Success(_) =>
                  OK
                case Failure(e) =>
                  (BadRequest, e.getMessage)
              }
            }
          }
        }
      }
    }
  }

  def enableUserAccount = path("user" / JavaUUID / "enable") { userId: UUID =>
    accessTokenHandler { implicit systemUser: User =>
      authorize(UserAuthorization.hasPermissionEnableUser) {
        put {
          db.withSession { implicit session =>
            complete {
              Try(
                UserUser.filter(_.userId === userId)
                  .map(dd => (dd.enabled, dd.lastUpdated))
                  .update((true, LocalDateTime.now()))
              ) match {
                case Success(_) =>
                  OK
                case Failure(e) =>
                  (BadRequest, e.getMessage)
              }
            }
          }
        }
      }
    }
  }

  def getAccessToken = path("access_token") {
    // Any password-authenticated user (not only owner)
    userPassApiHandler { implicit user: User =>
      get {
        db.withSession { implicit session =>
          val maybeToken = UserAccessToken.filter(_.userId === user.userId).run.headOption
          maybeToken match {
            case Some(token) =>
              complete {
                (OK, AccessToken(token.accessToken, token.userId))
              }
            case None =>
              val newAccessToken = UserAccessTokenRow(UUID.randomUUID().toString, user.userId)
              Try(UserAccessToken += newAccessToken) match {
                case Success(_) =>
                  complete {
                    (OK, AccessToken(newAccessToken.accessToken, user.userId))
                  }
                case Failure(e) =>
                  complete {
                    (InternalServerError, "Error while creating access_token, please try again later")
                  }
              }
          }
        }
      }
    }
  }
}
