package dalapi

import com.wordnik.swagger.annotations.{ApiOperation, Api}
import spray.http.MediaTypes._
import spray.routing.HttpService

trait HelloService extends HttpService {

  val routes = home ~ docs

  def home = get {
    path("") {
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
