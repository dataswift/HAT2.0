package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.authentication.HatAuthTestHandler
import hatdex.hat.api.endpoints.jsonExamples.{DataExamples, EntityExamples}
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

class PersonSpec extends Specification with Specs2RouteTest with Person with BeforeAfterAll {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  val locationEndpoint = new Location {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  val organisationEndpoint = new Organisation {
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
  }

  val ownerAuthParams = "?username=bob@gmail.com&password=pa55w0rd"

  "Person Endpoint" should {
    "Accept new people created" in {
      //test createEntity
      val newPerson = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.personValid)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("HATperson")
        responseAs[ApiPerson]
      }

      newPerson.id must beSome
    }

    "Accept relationships with people created" in {
      val newPerson = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.personValid)) ~>
        sealRoute(createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("HATperson")
          responseAs[ApiPerson]
        }

      newPerson.id must beSome

      val personRelative = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.personRelative)
      ) ~>
        sealRoute(createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("HATRelative")
          responseAs[ApiPerson]
        }

      personRelative.id must beSome

      val personRelationship = HttpRequest(
        POST, "/relationshipType" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipPersonRelative)
      ) ~>
        sealRoute(createPersonRelationshipType) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("Family Member")
          responseAs[ApiPersonRelationshipType].id must beSome
          responseAs[String]
        }



      HttpRequest(
        POST, s"/${newPerson.id.get}/person/${personRelative.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, personRelationship)) ~>
        sealRoute(linkToPerson) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      HttpRequest(
        GET, s"/${newPerson.id.get}" + ownerAuthParams) ~> sealRoute(getApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain(s"${personRelative.id.get}")
            responseAs[String] must contain(s"${personRelative.name}")
          }
        }

    }

    "Retrieve created people" in {
      val newPerson = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.personValid)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("HATperson")
        responseAs[ApiPerson]
      }

      HttpRequest(GET, s"/${newPerson.id.get}" + ownerAuthParams) ~> sealRoute(getApi) ~> check {
        eventually {
          response.status should be equalTo OK
          responseAs[String] must contain("HATperson")
          responseAs[ApiPerson]
          responseAs[String] must contain(s"${newPerson.id.get}")
        }
      }
    }

    "Accept retrieval of people and other entities linked" in {
      val newPerson = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.personValid)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("HATperson")
        responseAs[ApiPerson]
      }

      newPerson.id must beSome

      val personLocation = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.locationValid)) ~>
        sealRoute(locationEndpoint.createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("home")
        responseAs[ApiLocation]
      }

      personLocation.id must beSome
      //test linkToPerson
      HttpRequest(
        POST, s"/${newPerson.id.get}/location/${personLocation.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(linkToLocation) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      val personEmployer = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.orgValid)) ~>
        sealRoute(locationEndpoint.createEntity) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("HATorg")
        responseAs[ApiOrganisation]
      }

      personEmployer.id must beSome
      //test linkToPerson
      HttpRequest(
        POST, s"/${newPerson.id.get}/location/${personEmployer.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipWorksAt)) ~>
        sealRoute(linkToLocation) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      HttpRequest(
        GET, s"/${newPerson.id.get}" + ownerAuthParams) ~> sealRoute(getApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain(s"${personLocation.id.get}")
            responseAs[String] must contain(s"${personLocation.name}")
            responseAs[String] must contain(s"${personEmployer.id.get}")
            responseAs[String] must contain(s"${personEmployer.name}")
          }
        }
    }

    "Reject bad people and relationships" in {
      val tmpPerson = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.personBadName)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo BadRequest
      }

      HttpRequest(POST, s"/0/location/1}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToPerson) ~> check {
        response.status should be equalTo NotFound
      }

      HttpRequest(POST, s"/0/organisation/0}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToThing) ~> check {
        response.status should be equalTo NotFound
      }
    }
  }
}
