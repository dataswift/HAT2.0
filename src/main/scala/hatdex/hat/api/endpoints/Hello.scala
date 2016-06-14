package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import com.typesafe.config.ConfigFactory
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models.HatService
import hatdex.hat.authentication.HatAuthHandler.UserPassHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.{ JwtTokenHandler, HatServiceAuthHandler }
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.api._
import org.mindrot.jbcrypt.BCrypt
import spray.http.{ StatusCodes, HttpCookie }
import spray.http.MediaTypes._
import spray.routing.HttpService
import spray.httpx.PlayTwirlSupport._
import org.joda.time.Duration._
import hatdex.hat.dal.Tables.UserUser

import scala.util.{ Failure, Success, Try }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Hello extends HttpService with HatServiceAuthHandler with JwtTokenHandler {
  val routes = home ~ authHat ~ logout ~ getPublicKey ~ assets ~ passwordChange
  val logger: LoggingAdapter

  def home = path("") {
    get {
      respondWithMediaType(`text/html`) {
        (accessTokenHandler) { implicit user: User =>
          myhat
        } ~ complete {
          logger.info("HAT accessed")
          val parameters: Map[String, String] = Map(
            "hatName" -> conf.getString("hat.name"),
            "hatDomain" -> conf.getString("hat.domain"),
            "hatAddress" -> s"${conf.getString("hat.name")}.${conf.getString("hat.domain")}")
          hatdex.hat.views.html.index(parameters)
        }
      }
    } ~ post {
      formField('username.as[String], 'password.as[String], 'remember.?) {
        case (username, password, remember) =>
          val params = Map[String, String]("username" -> username, "password" -> password)
          val validity = if (remember.contains("remember")) {
            standardDays(1)
          }
          else {
            standardMinutes(15)
          }
          val fUser = UserPassHandler.authenticator(params)
          val fToken = fUser.flatMap { maybeUser =>
            // Fetch Access token if user has authenticated
            val maybeFutureToken = maybeUser.map(user => fetchOrGenerateToken(user, issuer, accessScope = user.role, validity = validity))
            // Transform option of future to future of option
            maybeFutureToken.map { futureToken =>
              futureToken.map { token =>
                Some(token)
              }
            } getOrElse {
              Future.successful(None)
            }
          }

          val fCredentials = for {
            maybeUser <- fUser
            maybeToken <- fToken
          } yield (maybeUser, maybeToken)

          onComplete(fCredentials) {
            case Success((Some(user), Some(token))) =>
              setCookie(HttpCookie("X-Auth-Token", content = token.accessToken)) {
                redirect("/", StatusCodes.Found)
              }
            case Success(_) =>
              deleteCookie("X-Auth-Token") {
                complete("Invalid Credentials!")
              }
            case Failure(e) =>
              deleteCookie("X-Auth-Token") {
                complete {
                  logger.error(s"Error while authenticating: ${e.getMessage}")
                  "Error while authenticating"
                }
              }
          }
      }
    }
  }

  def authHat = path("hat") {
    accessTokenHandler { implicit user: User =>
      authorize(UserAuthorization.withRole("owner")) {
        myhat
      }
    }
  }

  def logout = path("logout") {
    accessTokenHandler { implicit user: User =>
      deleteCookie("X-Auth-Token") {
        redirect("/", StatusCodes.Found)
      }
    }
  }

  def myhat(implicit user: User) = {
    respondWithMediaType(`text/html`) {
      get {
        val services = Seq(
          HatService("MarketSquare", "", "/assets/images/marketsquare_logo.jpg", "https://marketsquare.hubofallthings.net", "/authenticate/hat", browser = false),
          HatService("Rumpel", "", "/assets/images/rumpel_logo.jpg", "https://rumpel.hubofallthings.com", "/login", browser = true))

        val serviceCredentials = services.map { service =>
          val token = if (service.browser == false) {
            fetchOrGenerateToken(user, resource = service.url, accessScope = "validate")
          }
          else {
            fetchOrGenerateToken(user, resource = service.url, accessScope = user.role)
          }
          token.map { accessToken =>
            (service, accessToken)
          }
        }
        val futureCredentials = Future.sequence(serviceCredentials)
        onComplete(futureCredentials) {
          case Success(credentials) =>
            complete {
              hatdex.hat.views.html.authenticated(user, credentials)
            }
          case Failure(e) =>
            logger.warning(s"Error resolving access tokens for auth page: ${e.getMessage}")
            complete {
              hatdex.hat.views.html.authenticated(user, Seq())
            }
        }
      }
    }
  }

  def passwordChange = path("password") {
    accessTokenHandler { implicit user: User =>
      authorize(UserAuthorization.withRole("owner")) {
        respondWithMediaType(`text/html`) {
          get {
            complete {
              hatdex.hat.views.html.passwordChange(user, Seq(), false)
            }
          } ~ post {
              formField('oldPassword.as[String], 'newPassword.as[String], 'confirmPassword.as[String]) {
                case (oldPassword, newPassword, confirmPassword) =>
                  if (newPassword != confirmPassword) {
                    complete {
                      hatdex.hat.views.html.passwordChange(user, Seq("Passwords do not match"))
                    }
                  }
                  else if (!BCrypt.checkpw(oldPassword, user.pass.getOrElse(""))) {
                    complete {
                      hatdex.hat.views.html.passwordChange(user, Seq("Old password incorrect"))
                    }
                  }
                  else {
                    val passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
                    val tryPasswordChange = DatabaseInfo.db.run {
                      UserUser.filter(_.userId === user.userId)
                        .map(user => user.pass)
                        .update(Some(passwordHash))
                        .asTry
                    }
                    onComplete(tryPasswordChange) {
                      case Success(change) => complete(hatdex.hat.views.html.passwordChange(user, Seq(), true))
                      case Failure(e) => complete(hatdex.hat.views.html.passwordChange(user, Seq("An error occurred while changing your password, please try again"), false))
                    }
                  }
              }

          }
        }
      }
    }
  }

  def getPublicKey = path("publickey") {
    get {
      respondWithMediaType(`text/plain`) {
        complete {
          conf.getString("auth.publicKey")
        }
      }
    }
  }

  def assets = pathPrefix("assets") {
    getFromResourceDirectory("assets")
  }
}

