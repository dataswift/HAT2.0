package hatdex.hat.api.service

import hatdex.hat.api.models.ApiLocation
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.authentication.HatAuthTestHandler
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.service.jsonExamples.DataExamples
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.{MediaTypes, HttpEntity, HttpRequest}
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.testkit.Specs2RouteTest

class LocationsServiceSpec extends Specification with Specs2RouteTest with LocationsService with BeforeAfterAll {
  def actorRefFactory = system
  val dataService = new DataService {
    def actorRefFactory = system
  }

  val apiDataService = dataService
  val propertyService = new PropertyService {
    def actorRefFactory = system
    val dataService = apiDataService
  }

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  import JsonProtocol._

  def beforeAll() = {

  }

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      TestDataCleanup.cleanupAll
    }
    db.close
  }

  val ownerAuthParams = "?username=bob@gmail.com&password=pa55w0rd"

  "LocationsService" should {
    "Accept new locations created" in {
      val newLocation = HttpRequest(POST, "/location" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.locationValid)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("home")
        responseAs[ApiLocation]
      }

      newLocation.id must beSome

      val subLocation = HttpRequest(POST, "/location" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.locationHomeStairs)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("stairs")
        responseAs[ApiLocation]
      }

      subLocation.id must beSome

      val tmpLocation = HttpRequest(POST, "/location" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.locationBadName)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo BadRequest
      }

      HttpRequest(POST, s"/location/${newLocation.id.get}/location/${subLocation.id.get}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToLocation) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("id")
      }
    }
  }
}
