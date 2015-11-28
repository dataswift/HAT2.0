package hatdex.hat.api

import akka.event.LoggingAdapter
import org.specs2.mutable.Specification
import spray.http.HttpHeaders._
import spray.http.StatusCodes._
import spray.http._
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest


class CorsSpec extends Specification with Specs2RouteTest with HttpService with Cors {
  def actorRefFactory = system // Connect the service API to the test ActorSystem

  val logger: LoggingAdapter = system.log

  val testRoute = path("test") {
    cors {
      get {
        complete((200, "'CORS it works!"))
      } ~
        post {
          complete((200, "'CORS I'll update that!"))
        }
    }
  }

  val testOrigin = Origin(Seq(HttpOrigin("http://demo.hat.org")))

  sequential

  "CORS" should {
    "work for simple routes" in {
      Get("/test") ~> sealRoute(testRoute) ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("'CORS it works!")
      }
      Post("/test") ~> sealRoute(testRoute) ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("'CORS I'll update that!")
      }
      Put("/test") ~> sealRoute(testRoute) ~> check {
        response.status should be equalTo MethodNotAllowed
      }
    }

    "respond to OPTIONS requests properly" in {
      Options("/test") ~>
        addHeader(testOrigin) ~>
        sealRoute(testRoute) ~>
        check {
          eventually {
            response.status should be equalTo OK

            val allowMethods = header("Access-Control-Allow-Methods").get.value.split(", ").toSeq
            Seq("OPTIONS", "POST", "GET") foreach { method =>
              allowMethods should contain(method)
            }
            Seq("PUT", "DELETE") foreach { method =>
              allowMethods should not contain(method)
            }
            header("Access-Control-Allow-Headers").isDefined should be equalTo true
            header("Access-Control-Max-Age").isDefined should be equalTo true
          }
        }
    }

    "respond to all requests with the Access-Control-Allow-Origin header" in {
      Get("/test") ~> addHeader(testOrigin) ~>
        sealRoute(testRoute) ~>
        check {
          header("Access-Control-Allow-Origin").isDefined should be equalTo true
        }
      Post("/test") ~> addHeader(testOrigin) ~>
        sealRoute(testRoute) ~>
        check {
          header("Access-Control-Allow-Origin").isDefined should be equalTo true
        }
    }
  }
}