package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.endpoints.jsonExamples.{LocationExamples, DataExamples}
import hatdex.hat.api.endpoints.{Location, Property, Data}
import hatdex.hat.api.models._
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.authentication.HatAuthTestHandler
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.service.{PropertyService, DataService, LocationsService}
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.{MediaTypes, HttpEntity, HttpRequest}
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.testkit.Specs2RouteTest

class LocationSpec extends Specification with Specs2RouteTest with Location with BeforeAfterAll {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  val thingEndpoint = new Thing {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  val personEndpoint = new Person {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  val organisationEndpoint = new Organisation {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  val eventEndpoint = new Event {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
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
    //    db.close
  }

  val ownerAuthParams = "?username=bob@gmail.com&password=pa55w0rd"

  "LocationsService" should {
    "Accept new locations created" in {
      //test createEntity
      val newLocation = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, LocationExamples.locationValid)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("home")
        responseAs[ApiLocation]
      }

      newLocation.id must beSome

      val subLocation = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, LocationExamples.locationHomeStairs)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("stairs")
        responseAs[ApiLocation]
      }

      subLocation.id must beSome
      //test linkToLocation
      HttpRequest(POST, s"/${newLocation.id.get}/location/${subLocation.id.get}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToLocation) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("id")
      }
    }

    "Accept relationships with locations created" in {

      val newLocation = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, LocationExamples.locationValid)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("home")
        responseAs[ApiLocation]
      }

      newLocation.id must beSome

      val subLocation = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, LocationExamples.locationHomeStairs)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("stairs")
        responseAs[ApiLocation]
      }

      subLocation.id must beSome
      //test linkToLocation
      HttpRequest(POST, s"/${newLocation.id.get}/location/${subLocation.id.get}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToLocation) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("id")
      }
      // TODO: no linking from location to person currently

      //      val somePerson = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, LocationExamples.validPerson)) ~>
      //        sealRoute(personEndpoint.createEntity) ~> check {
      //        response.status should be equalTo Created
      //        responseAs[String] must contain("HATperson")
      //        responseAs[ApiPerson] //cannot convert the response to ApiPerson
      //      }
      //
      //      somePerson.id must beSome
      //      //test linkToPerson
      //      HttpRequest(POST, s"/${newLocation.id.get}/person/${somePerson.id.get}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
      //        sealRoute(linkToPerson) ~> check {
      //        logger.debug("Link Location to Person: " + response.toString)
      //        response.status should be equalTo Created
      //        responseAs[String] must contain("id")
      //      }

//      val someOrg = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, LocationExamples.validOrg)) ~>
      //        sealRoute(organisationEndpoint.createEntity) ~> check {
      //        response.status should be equalTo Created
      //        responseAs[String] must contain("HATorg")
      //        responseAs[ApiOrganisation]
      //      }
      //
      //      someOrg.id must beSome
      //      //test linkToOrganisation
      //      HttpRequest(POST, s"/${newLocation.id.get}/organisation/${someOrg.id.get}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
      //        sealRoute(linkToOrganisation) ~> check {
      //        logger.debug("Link Location to Organisation: " + response.toString)
      //        response.status should be equalTo Created //retuns BadRequest, should be Created
      //        responseAs[String] must contain("id")
      //      }
      //
      //      val someEvent = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, LocationExamples.validEvent)) ~>
      //        sealRoute(eventEndpoint.createEntity) ~> check {
      //        response.status should be equalTo Created
      //        responseAs[String] must contain("sunset")
      //        responseAs[ApiEvent]
      //      }
      //
      //      someEvent.id must beSome
      //      //test linkToEvent
      //      HttpRequest(POST, s"/${newLocation.id.get}/event/${someEvent.id.get}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
      //        sealRoute(linkToEvent) ~> check {
      //        logger.debug("Link Location to Event: " + response.toString)
      //        response.status should be equalTo Created //retuns BadRequest, should be Created
      //        responseAs[String] must contain("id")
      //      }
    }

    "Retrieve created locations" in {
      val newLocation = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, LocationExamples.locationValid)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("home")
        responseAs[ApiLocation]
      }

      HttpRequest(GET, s"/${newLocation.id.get}" + ownerAuthParams) ~> sealRoute(getApi) ~> check {
        eventually {
          response.status should be equalTo OK
          responseAs[String] must contain("home")
          responseAs[ApiLocation]
          responseAs[String] must contain(s"${newLocation.id.get}")
        }
      }
    }

    "Accept retrieval of things and locations linked" in {
      val newLocation = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, LocationExamples.locationValid)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("home")
        responseAs[ApiLocation]
      }

      val someThing = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, LocationExamples.validThing)) ~>
        sealRoute(thingEndpoint.createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("tv")
        responseAs[ApiThing]
      }

      someThing.id must beSome
      //test link to thing
      HttpRequest(POST, s"/${newLocation.id.get}/thing/${someThing.id.get}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToThing) ~> check {
        response.status should be equalTo Created //retuns BadRequest, should be Created
        responseAs[String] must contain("id")
      }

      HttpRequest(GET, s"/${newLocation.id.get}" + ownerAuthParams) ~> sealRoute(getApi) ~> check {
        eventually {
          response.status should be equalTo OK
          responseAs[String] must contain(s"${someThing.id.get}")
          responseAs[String] must contain(s"${someThing.name}")
        }
      }
    }

    "Reject bad locations and relationships" in {
      val tmpLocation = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, LocationExamples.locationBadName)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo BadRequest
      }

      HttpRequest(POST, s"/0/location/1}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToLocation) ~> check {
        response.status should be equalTo NotFound
      }

      HttpRequest(POST, s"/0/thing/0}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToThing) ~> check {
        response.status should be equalTo NotFound
      }
    }
  }
}
