package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.authentication.HatAuthTestHandler
import hatdex.hat.api.endpoints.jsonExamples.EntityExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{HttpEntity, HttpRequest, MediaTypes}
import spray.httpx.SprayJsonSupport._
import spray.testkit.Specs2RouteTest

class EventSpec extends Specification with Specs2RouteTest with Event with BeforeAfterAll {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  val personEndpoint = new Person {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  val thingEndpoint = new Thing {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  val organisationEndpoint = new Organisation {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  val locationEndpoint = new Location {
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

  "EventsService" should {
    "Accept new events created" in {
      //test createEntity
      val newEvent = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.eventValid)) ~>
        sealRoute(createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("sunrise")
          responseAs[ApiEvent]
        }

      newEvent.id must beSome

      val otherEvent = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.otherEventValid)) ~>
        sealRoute(createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("breakfast")
          responseAs[ApiEvent]
        }

      otherEvent.id must beSome
      //test linkToEvent
      HttpRequest(POST, s"/${newEvent.id.get}/event/${otherEvent.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipDuring)) ~>
        sealRoute(linkToEvent) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }
    }

    "Accept relationships with other entities created" in {

      val newEvent = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.eventValid)) ~>
        sealRoute(createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("sunrise")
          responseAs[ApiEvent]
        }

      newEvent.id must beSome

      // Linking event to Person
      val ownerPerson = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.personValid)) ~>
        sealRoute(personEndpoint.createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("HATperson")
          responseAs[ApiEvent]
        }

      ownerPerson.id must beSome

      HttpRequest(
        POST, s"/${newEvent.id.get}/person/${ownerPerson.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipOwnedBy)
      ) ~>
        sealRoute(linkToPerson) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      // Linking event to Thing

      val activeThing = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.thingValid)) ~>
        sealRoute(thingEndpoint.createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("tv")
          responseAs[ApiThing]
        }

      activeThing.id must beSome

      HttpRequest(
        POST, s"/${newEvent.id.get}/thing/${activeThing.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipActiveAt)
      ) ~>
        sealRoute(linkToThing) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      // Linking event to Location

      val atLocation = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.locationValid)) ~>
        sealRoute(locationEndpoint.createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("home")
          responseAs[ApiLocation]
        }

      atLocation.id must beSome

      HttpRequest(
        POST, s"/${newEvent.id.get}/location/${atLocation.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipHappensAt)
      ) ~>
        sealRoute(linkToLocation) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      // Linking event to Organisation

      val sponsoredByOrganisation = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.orgValid)) ~>
        sealRoute(organisationEndpoint.createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("HATorg")
          responseAs[ApiOrganisation]
        }

      sponsoredByOrganisation.id must beSome

      HttpRequest(
        POST, s"/${newEvent.id.get}/organisation/${sponsoredByOrganisation.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipHappensAt)
      ) ~>
        sealRoute(linkToOrganisation) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }


      HttpRequest(
        GET, s"/${newEvent.id.get}" + ownerAuthParams) ~> sealRoute(getApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain(s"${ownerPerson.id.get}")
            responseAs[String] must contain(s"${ownerPerson.name}")
            responseAs[String] must contain(s"${activeThing.id.get}")
            responseAs[String] must contain(s"${activeThing.name}")
            responseAs[String] must contain(s"${atLocation.id.get}")
            responseAs[String] must contain(s"${atLocation.name}")
            responseAs[String] must contain(s"${sponsoredByOrganisation.id.get}")
            responseAs[String] must contain(s"${sponsoredByOrganisation.name}")
          }
        }
    }

    "Retrieve created events" in {
      val newEvent = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.eventValid)
      ) ~>
        sealRoute(createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("sunrise")
          responseAs[ApiEvent]
        }

      newEvent.id must beSome

      HttpRequest(
        GET, s"/${newEvent.id.get}" + ownerAuthParams) ~>
        sealRoute(getApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("sunrise")
            responseAs[ApiEvent]
            responseAs[String] must contain(s"${newEvent.id.get}")
          }
        }
    }

    "Reject bad events and relationships" in {
      val tmpEvent = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.eventBadName)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo BadRequest
      }

      HttpRequest(POST, s"/0/event/1}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(linkToEvent) ~> check {
        response.status should be equalTo NotFound
      }

      HttpRequest(POST, s"/0/person/0}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(linkToEvent) ~> check {
        response.status should be equalTo NotFound
      }
    }
  }
}
