package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.endpoints.jsonExamples.EntityExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.{BeforeAfterAll, Scope}
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{HttpEntity, HttpRequest, MediaTypes}
import spray.httpx.SprayJsonSupport._
import spray.json._
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

    override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

    override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()
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

  def createNewEvent = HttpRequest(
    POST, "" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.eventValid)) ~>
    sealRoute(createEntity) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("sunrise")
      }
      responseAs[ApiEvent]
    }

  def createOtherEvent = HttpRequest(
    POST, "" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.otherEventValid)) ~>
    sealRoute(createEntity) ~>
    check {
      response.status should be equalTo Created
      responseAs[String] must contain("breakfast")
      responseAs[ApiEvent]
    }

  "EventsService" should {
    "Accept new events created" in {
      //test createEntity
      val newEvent = createNewEvent
      newEvent.id must beSome

      val otherEvent = createOtherEvent
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

      val newEvent = createNewEvent
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
        POST, s"/event/${newEvent.id.get}/person/${ownerPerson.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipOwnedBy)
      ) ~>
        sealRoute(routes) ~>
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
        POST, s"/event/${newEvent.id.get}/thing/${activeThing.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipActiveAt)
      ) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      // Linking event to Location

      logger.debug("Creating location to link to event")
      val atLocation = HttpRequest(
        POST, "/location" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.locationValid)) ~>
        sealRoute(locationEndpoint.routes) ~>
        check {
          logger.debug("Location created: " + response)
          response.status should be equalTo Created
          responseAs[String] must contain("home")
          responseAs[ApiLocation]
        }

      atLocation.id must beSome

      HttpRequest(
        POST, s"/event/${newEvent.id.get}/location/${atLocation.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipHappensAt)
      ) ~>
        sealRoute(routes) ~>
        check {
          logger.debug("event location link response: " + response)
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
        POST, s"/event/${newEvent.id.get}/organisation/${sponsoredByOrganisation.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipHappensAt)
      ) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }


      HttpRequest(
        GET, s"/event/${newEvent.id.get}" + ownerAuthParams) ~> sealRoute(routes) ~>
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
      val newEvent = createNewEvent
      newEvent.id must beSome

      HttpRequest(
        GET, s"/event/${newEvent.id.get}" + ownerAuthParams) ~>
        sealRoute(routes) ~>
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
      HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.eventBadName)) ~>
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

    "Reject unsuported relationships" in {
      true should be equalTo true
    }

    "List All Entities correctly" in {
      val newEvent = createNewEvent
      newEvent.id must beSome

      val otherEvent = createOtherEvent
      otherEvent.id must beSome

      HttpRequest(GET, "/event" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[List[ApiEvent]]
            val asString = responseAs[String]
            asString must contain(s"${newEvent.id.get}")
            asString must contain(s"${newEvent.name}")
            asString must contain(s"${otherEvent.id.get}")
            asString must contain(s"${otherEvent.name}")
          }
        }
    }

    "Accept Type annotations" in {
      //      addTypeApi
      val newEvent = createNewEvent
      newEvent.id must beSome


      val typeSpec = new TypeSpec
      val postalAddressType = typeSpec.createPostalAddressType

      HttpRequest(
        POST,
        s"/event/${newEvent.id.get}/type/${postalAddressType.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)
      ) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
        }

      HttpRequest(
        POST, s"/event/${newEvent.id.get}/type/0" + ownerAuthParams,
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
      val newEvent = createNewEvent
      newEvent.id must beSome

      val dataField = populatedData match {
        case (dataTable, dataField, record) =>
          dataField
      }
      val dynamicPropertyLink = ApiPropertyRelationshipDynamic(
        None, property, None, None, "test property", dataField)

      val propertyLinkId = HttpRequest(
        POST, s"/event/${newEvent.id.get}/property/dynamic/${property.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, dynamicPropertyLink.toJson.toString)
      ) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Created
          }
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/event/${newEvent.id.get}/property/dynamic" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("BodyWeight")
            responseAs[String] must contain("field")
            responseAs[String] must not contain ("record")
          }
        }

      HttpRequest(GET, s"/event/${newEvent.id.get}/property/dynamic/${propertyLinkId.id}/values" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must contain("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }

      HttpRequest(GET, s"/event/${newEvent.id.get}/values" + ownerAuthParams) ~>
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
      val newEvent = createNewEvent
      newEvent.id must beSome

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
        POST, s"/event/${newEvent.id.get}/property/static/${property.id.get}" + ownerAuthParams,
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

      HttpRequest(GET, s"/event/${newEvent.id.get}/property/static" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("BodyWeight")
            responseAs[String] must contain("field")
            responseAs[String] must contain("record")
          }
        }

      HttpRequest(GET, s"/event/${newEvent.id.get}/property/static/${propertyLinkId.id}/values" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must not contain ("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }

      HttpRequest(GET, s"/event/${newEvent.id.get}/values" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must not contain ("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }
    }
  }
}
