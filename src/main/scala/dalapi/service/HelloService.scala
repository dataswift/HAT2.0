package dalapi.service

import hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import hat.authentication.{HatServiceAuthHandler, User, HatAuthHandler}
import spray.http.MediaTypes._
import spray.routing.HttpService
import spray.routing.authentication.{BasicAuth, UserPass}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait HelloService extends HttpService {
  import hat.authentication.HatServiceAuthHandler._
  val routes = home ~ docs ~ accessTokenHat ~ userPassHat

  def home =
    path("") {
      get {
      respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
        complete {
          <html>
            <body>
              <h1>Hello HAT 2.0!</h1>
            </body>
          </html>
        }
      }
    }
  }

  def myhat(implicit user: User) = path("hat") {
    respondWithMediaType(`text/html`) {
      get {

          complete {
            s"""<html>
            <body>
              <h1>Hello $user!</h1>
              <h2>Welcome to your Hub of All Things</h2>
            </body>
          </html>"""
          }
        }
      }
    }

  def accessTokenHat = accessTokenHandler { implicit user: User =>
    myhat
  }

  def userPassHat = userPassHandler { implicit user: User =>
    myhat
  }

  def docs = get {
    path("api" / "inbound") {
      respondWithMediaType(`application/json`) {
        getFromResource("api/inbound.json")
      }
    } ~ {
      getFromResourceDirectory("web")
    }
  }


}
