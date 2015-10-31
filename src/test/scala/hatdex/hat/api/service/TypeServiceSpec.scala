package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.authentication.HatAuthTestHandler
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.{ErrorMessage, ApiSystemType, ApiLocation}
import hatdex.hat.api.service.jsonExamples.DataExamples
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{HttpEntity, HttpRequest, MediaTypes}
import spray.httpx.SprayJsonSupport._
import spray.testkit.Specs2RouteTest

class TypeServiceSpec extends Specification with Specs2RouteTest with TypeService with BeforeAfterAll {
  def actorRefFactory = system
  val logger: LoggingAdapter = system.log

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  import JsonProtocol._

  def beforeAll() = {

  }

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
//      TestDataCleanup.cleanupAll
    }
    db.close
  }

  val ownerAuthParams = "?username=bob@gmail.com&password=pa55w0rd"

  "Types Service" should {
    "Accept new types created" in {
      HttpRequest(POST, "/type" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.typesPostalAddress)) ~>
        sealRoute(createType) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("PostalAddress")
        responseAs[ApiSystemType].id must beSome
      }

      HttpRequest(POST, "/type" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.typesPostalAddress)) ~>
        sealRoute(createType) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("PostalAddress")
        responseAs[ApiSystemType].id must beSome
      }
    }

    "Disallow duplicte types" in {
      HttpRequest(POST, "/type" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.typesPostalAddress)) ~>
        sealRoute(createType) ~> check {
        response.status should be equalTo BadRequest
        responseAs[String] must contain("PostalAddress")
        responseAs[ErrorMessage].cause must contain("Error")
      }
    }
  }
}
