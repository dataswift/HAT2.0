package hatdex.hat.api.endpoints

import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.models.User
import spray.http.MediaTypes._
import spray.routing.HttpService

trait Hello extends HttpService with HatServiceAuthHandler{
  val routes = home ~ authHat

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

  def myhat(implicit user: User) = {
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

  def authHat = path("hat") {
    (accessTokenHandler | userPassHandler) { implicit user: User =>
      myhat
    }
  }
}
