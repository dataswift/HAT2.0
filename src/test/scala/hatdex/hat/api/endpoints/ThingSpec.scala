package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.api.endpoints.jsonExamples.{DataExamples, EntityExamples}
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import org.specs2.specification.Scope
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{HttpEntity, HttpRequest, MediaTypes}
import spray.httpx.SprayJsonSupport._
import spray.json._
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
  }

  val ownerAuthParams = "?username=bob@gmail.com&password=pa55w0rd"

  def createNewThing = HttpRequest(
    POST, "" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.thingValid)) ~>
    sealRoute(createEntity) ~>
    check {
      eventually {
        // logger.debug("Create thing response:" + response.toString)
        response.status should be equalTo Created
        responseAs[String] must contain("tv")
      }
      responseAs[ApiThing]
    }

  def createOtherThing = HttpRequest(
    POST, "" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.otherThingValid)) ~>
    sealRoute(createEntity) ~>
    check {
      response.status should be equalTo Created
      responseAs[String] must contain("smartphone")
      responseAs[ApiThing]
    }

  sequential

  "ThingsService" should {
    "Accept new things created" in {
      //test createEntity
      val newThing = createNewThing
      newThing.id must beSome

      val otherThing = createOtherThing
      otherThing.id must beSome

      //test linkToThing
      HttpRequest(POST, s"/thing/${newThing.id.get}/thing/${otherThing.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }
    }

    "Accept relationships with things created" in {

      val newThing = createNewThing
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
      //test linkToPerson
      HttpRequest(
        POST, s"/thing/${newThing.id.get}/person/${ownerPerson.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipOwnedBy)
      ) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      HttpRequest(
        GET, s"/thing/${newThing.id.get}" + ownerAuthParams) ~> sealRoute(routes) ~>
        check {
        eventually {
          response.status should be equalTo OK
          responseAs[String] must contain(s"${ownerPerson.id.get}")
          responseAs[String] must contain(s"${ownerPerson.name}")
        }
      }
    }

    "Retrieve created things" in {
      val newThing = createNewThing
      newThing.id must beSome

      HttpRequest(
        GET, s"/thing/${newThing.id.get}" + ownerAuthParams) ~>
        sealRoute(routes) ~>
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
      HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.thingBadName)) ~>
        sealRoute(createEntity) ~> check {
        response.status should be equalTo BadRequest
      }

      HttpRequest(POST, s"/0/event/1}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(linkToThing) ~> check {
        response.status should be equalTo NotFound
      }

      HttpRequest(POST, s"/0/person/0}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(linkToThing) ~> check {
        response.status should be equalTo NotFound
      }
    }

    "Reject unsuported relationships" in {
      val newThing = createNewThing
      newThing.id must beSome

      HttpRequest(
        POST,
        s"/${newThing.id.get}/organisation/1" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToOrganisation) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }

      HttpRequest(
        POST,
        s"/${newThing.id.get}/location/1" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToLocation) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }

      HttpRequest(
        POST,
        s"/${newThing.id.get}/event/1" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToEvent) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }
    }

    "List All Entities correctly" in {
      val newThing = createNewThing
      newThing.id must beSome

      val otherThing = createOtherThing
      otherThing.id must beSome

      HttpRequest(GET, "/thing" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[List[ApiThing]]
            val asString = responseAs[String]
            asString must contain(s"${newThing.id.get}")
            asString must contain(s"${newThing.name}")
            asString must contain(s"${otherThing.id.get}")
            asString must contain(s"${otherThing.name}")
          }
        }
    }

    "Accept Type annotations" in {
      //      addTypeApi
      val newThing = createNewThing
      newThing.id must beSome

      val typeSpec = new TypeSpec
      val postalAddressType = typeSpec.createPostalAddressType

      HttpRequest(
        POST,
        s"/thing/${newThing.id.get}/type/${postalAddressType.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)
      ) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
        }

      HttpRequest(
        POST, s"/thing/${newThing.id.get}/type/0" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)
      ) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }

    }

    object Context {
      val propertySpec = new PropertySpec()
      val property = propertySpec.createWeightProperty
      val dataSpec = new DataSpec()
      dataSpec.createBasicTables
      val populatedData = dataSpec.populateDataReusable
    }

    class Context extends Scope {
      val property = Context.property
      val populatedData = Context.populatedData
    }

    "Handle Dynamic Property Linking" in new Context {
      val newThing = createNewThing
      newThing.id must beSome

      val dataField = populatedData match {
        case (dataTable, dataField, record) =>
          dataField
      }
      val dynamicPropertyLink = ApiPropertyRelationshipDynamic(
        None, property, None, None, "test property", dataField)

      val propertyLinkId = HttpRequest(
        POST, s"/thing/${newThing.id.get}/property/dynamic/${property.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, dynamicPropertyLink.toJson.toString)
      ) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Created
          }
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/thing/${newThing.id.get}/property/dynamic" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("BodyWeight")
            responseAs[String] must contain("field")
            responseAs[String] must not contain("record")
          }
        }

      HttpRequest(GET, s"/thing/${newThing.id.get}/property/dynamic/${propertyLinkId.id}/values" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must contain("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }

      HttpRequest(GET, s"/thing/${newThing.id.get}/values" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must contain("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }
    }

    "Handle Static Property Linking" in new Context {
      val newThing = createNewThing
      newThing.id must beSome

      val dataField = populatedData match {
        case (dataTable, field, record) =>
          field
      }

      val dataRecord = populatedData match {
        case (dataTable, field, record) =>
          record
      }
      val staticPropertyLink = ApiPropertyRelationshipStatic(
        None, property, None, None, "test property", dataField, dataRecord)

      val propertyLinkId = HttpRequest(
        POST, s"/thing/${newThing.id.get}/property/static/${property.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, staticPropertyLink.toJson.toString)
      ) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            logger.debug("Static property creation resp: " + response.toString)
            response.status should be equalTo Created
          }
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/thing/${newThing.id.get}/property/static" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("BodyWeight")
            responseAs[String] must contain("field")
            responseAs[String] must contain("record")
          }
        }

      HttpRequest(GET, s"/thing/${newThing.id.get}/property/static/${propertyLinkId.id}/values" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must not contain("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }

      HttpRequest(GET, s"/thing/${newThing.id.get}/values" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must not contain("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }
    }
  }
}
