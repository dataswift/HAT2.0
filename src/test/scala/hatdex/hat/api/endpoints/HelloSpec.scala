package hatdex.hat.api.endpoints

import hatdex.hat.api.authentication.HatAuthTestHandler
import hatdex.hat.authentication.authenticators.{UserPassHandler, AccessTokenHandler}
import org.specs2.mutable.Specification
import spray.http.StatusCodes._
import spray.testkit.Specs2RouteTest

class HelloSpec extends Specification with Specs2RouteTest with Hello {
  def actorRefFactory = system

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  sequential

  "InboundDataService" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> home ~>
        check {
          responseAs[String] must contain("Hello HAT 2.0")
        }
    }

    "disallow GET requests to hat path without credentials" in {
      Get("/hat") ~> sealRoute(authHat) ~>
        check {
          eventually {
            response.status should be equalTo Unauthorized
          }
        }
    }

    "disallow GET requests to hat path with incorrect credentials" in {
      Get("/hat?username=bob@gmail.com&password=asdasd") ~>
        sealRoute(authHat) ~>
        check {
          eventually {
            response.status should be equalTo Unauthorized
          }
        }
    }

    "accept access_token authenticated GET requests to hat path" in {
      Get("/hat?access_token=df4545665drgdfg") ~> sealRoute(authHat) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("Welcome to your Hub of All Things")
          }
        }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> home ~>
        check {
          handled must beFalse
        }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(home) ~>
        check {
          response.status should be equalTo MethodNotAllowed
          responseAs[String] === "HTTP method not allowed, supported methods: GET"
        }
    }
  }
}
