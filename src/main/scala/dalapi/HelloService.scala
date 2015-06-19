package dalapi

import com.wordnik.swagger.annotations.{ApiOperation, Api}
import spray.http.MediaTypes._
import spray.routing.HttpService

// this trait defines our service behavior independently from the service actor
@Api(value = "/", description = "Say hello to the HAT!", position = 0)
trait HelloService extends HttpService {

  val routes = home ~ data

  @ApiOperation(value = "Get the system greeting",
    notes = "Returns a hello message only",
    httpMethod = "GET",
    response = classOf[String],
    produces = "text/html")
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

  def data = get {
    path("data") {
      respondWithMediaType(`text/html`) {
        complete {
          <html>
            <body>
              <h1>Here be dragons</h1>
            </body>
          </html>
        }
      }

    }
  }
}
