package hatdex.hat.api.endpoints

import org.specs2.mutable.Specification
import spray.http.StatusCodes._
import spray.testkit.Specs2RouteTest

class HelloSpec extends Specification with Specs2RouteTest with Hello {
  def actorRefFactory = system

  sequential

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
