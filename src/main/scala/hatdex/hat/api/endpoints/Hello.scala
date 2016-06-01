package hatdex.hat.api.endpoints

import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.models.User
import spray.http.MediaTypes._
import spray.routing.HttpService
import spray.httpx.PlayTwirlSupport._

trait Hello extends HttpService with HatServiceAuthHandler {
  val routes = home ~ authHat ~ assets

  def home = path("") {
    get {
      respondWithMediaType(`text/html`) {
        complete {
          hatdex.hat.views.html.indexPrivate()
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
