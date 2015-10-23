package hatdex.hat.api.service

import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.{ApiLocation, ApiDataTable}
import hatdex.hat.api.service.jsonExamples.DataExamples
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.{MediaTypes, HttpEntity, HttpRequest}
import spray.http.StatusCodes._
import spray.testkit.Specs2RouteTest

class LocationsServiceSpec extends Specification with Specs2RouteTest with LocationsService with BeforeAfterAll {
  def actorRefFactory = system

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
    }

    "Reject incorrect location json" in {
      val tmpLocation = HttpRequest(POST, "/location" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.locationBadName)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo BadRequest
      }
    }
  }
}
