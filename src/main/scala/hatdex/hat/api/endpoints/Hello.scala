package hatdex.hat.api.endpoints

import hatdex.hat.authentication.HatAuthHandler.UserPassHandler
import hatdex.hat.authentication.{ JwtTokenHandler, HatServiceAuthHandler }
import hatdex.hat.authentication.models.User
import spray.http.{StatusCodes, HttpCookie}
import spray.http.MediaTypes._
import spray.routing.HttpService
import spray.httpx.PlayTwirlSupport._

import scala.util.{ Failure, Success, Try }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Hello extends HttpService with HatServiceAuthHandler with JwtTokenHandler {
  val routes = home ~ authHat ~ assets

  def home = path("") {
    get {
      respondWithMediaType(`text/html`) {
        (accessTokenHandler) { implicit user: User =>
          myhat
        } ~ complete {
          hatdex.hat.views.html.index()
        }
      }
    } ~ post {
      formField('username.as[String], 'password.as[String]) {
        case (username, password) =>
          val params = Map[String, String]("username" -> username, "password" -> password)
          val fUser = UserPassHandler.authenticator(params)
          val fToken = fUser.flatMap { maybeUser =>
            val maybeFutureToken = maybeUser.map(user => fetchOrGenerateToken(user)) // Fetch Access token if user has authenticated
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
                redirect("/hat", StatusCodes.Found)
//                complete(s"User ${user.name} logged in")
              }
            case Success(_) =>
              complete("Invalid Credentials!")
            case Failure(e) =>
              complete {
                "Error while authenticating"
              }
          }
      }
    }
  }

  def authHat = path("hat") {
    (accessTokenHandler | userPassHandler) { implicit user: User =>
      myhat
    }
  }

  def myhat(implicit user: User) = {
    respondWithMediaType(`text/html`) {
      get {
        complete {
          hatdex.hat.views.html.authenticated(user)
        }
      }
    }
  }

  def assets = pathPrefix("assets") {
    getFromResourceDirectory("assets")
  }
}
