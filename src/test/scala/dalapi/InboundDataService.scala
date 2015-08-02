package dalapi

import dalapi.service.DataService
import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import spray.http.MediaTypes._

class DataServiceSpec extends Specification with Specs2RouteTest with DataService {
  def actorRefFactory = system

  val home = get {
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

  "InboundDataService" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> home ~> check {
        responseAs[String] must contain("Hello HAT 2.0")
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> home ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(home) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}
