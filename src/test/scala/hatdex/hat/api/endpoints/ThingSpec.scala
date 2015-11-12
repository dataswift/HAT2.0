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

class ThingSpec extends Specification with Specs2RouteTest with Thing with BeforeAfterAll {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  val personEndpoint = new Person {
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

  "ThingsService" should {
    "Accept new things created" in {
      //test createEntity
      val newThing = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.thingValid)) ~>
        sealRoute(createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("tv")
          responseAs[ApiThing]
        }

      newThing.id must beSome

      val otherThing = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.otherThingValid)) ~>
        sealRoute(createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("smartphone")
          responseAs[ApiThing]
        }

      otherThing.id must beSome
      //test linkToThing
      HttpRequest(POST, s"/${newThing.id.get}/thing/${otherThing.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(linkToThing) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }
    }

    "Accept relationships with things created" in {

      val newThing = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.thingValid)) ~>
        sealRoute(createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("tv")
          responseAs[ApiThing]
        }

      newThing.id must beSome

      val ownerPerson = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.personValid)) ~>
        sealRoute(personEndpoint.createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("HATperson")
          responseAs[ApiThing]
        }

      ownerPerson.id must beSome
      //test linkToThing
      HttpRequest(
        POST, s"/${newThing.id.get}/person/${ownerPerson.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipOwnedBy)
      ) ~>
        sealRoute(linkToPerson) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      HttpRequest(
        GET, s"/${newThing.id.get}" + ownerAuthParams) ~> sealRoute(getApi) ~>
        check {
        eventually {
          response.status should be equalTo OK
          responseAs[String] must contain(s"${ownerPerson.id.get}")
          responseAs[String] must contain(s"${ownerPerson.name}")
        }
      }
    }

    "Retrieve created things" in {
      val newThing = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.thingValid)
      ) ~>
        sealRoute(createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("tv")
          responseAs[ApiThing]
        }

      newThing.id must beSome

      HttpRequest(
        GET, s"/${newThing.id.get}" + ownerAuthParams) ~>
        sealRoute(getApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("tv")
            responseAs[ApiThing]
            responseAs[String] must contain(s"${newThing.id.get}")
          }
        }
    }

    "Reject bad things and relationships" in {
      val tmpThing = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.thingBadName)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo BadRequest
      }

      HttpRequest(POST, s"/0/thing/1}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(linkToThing) ~> check {
        response.status should be equalTo NotFound
      }

      HttpRequest(POST, s"/0/person/0}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(linkToThing) ~> check {
        response.status should be equalTo NotFound
      }
    }
  }
}
