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
import org.specs2.specification.Scope
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{HttpEntity, HttpRequest, MediaTypes}
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.testkit.Specs2RouteTest

class LocationSpec extends Specification with Specs2RouteTest with Location with BeforeAfterAll {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  val thingEndpoint = new Thing {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  val typeEndpoint = new Type {
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
      session.close()
    }
  }

  val ownerAuthParams = "?username=bob@gmail.com&password=pa55w0rd"

  def createNewValidLocation = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.locationValid)) ~>
    sealRoute(createEntity) ~> check {
    response.status should be equalTo Created
    responseAs[String] must contain("home")
    responseAs[ApiLocation]
  }

  def createSubLocation = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.locationHomeStairs)) ~>
    sealRoute(createEntity) ~> check {
    response.status should be equalTo Created
    responseAs[String] must contain("stairs")
    responseAs[ApiLocation]
  }

  "LocationsService" should {
    "Accept new locations created" in {
      //test createEntity
      val newLocation = createNewValidLocation
      newLocation.id must beSome
    }

    "Accept relationships with locations created" in {
      val newLocation = createNewValidLocation
      newLocation.id must beSome

      val subLocation = createSubLocation
      subLocation.id must beSome

      //test linkToLocation
      HttpRequest(
        POST,
        s"/${newLocation.id.get}/location/${subLocation.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToLocation) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }
    }

    "Retrieve created locations" in {
      val newLocation = createNewValidLocation
      newLocation.id must beSome

      HttpRequest(
        GET,
        s"/${newLocation.id.get}" + ownerAuthParams) ~>
        sealRoute(getApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("home")
            responseAs[ApiLocation]
            responseAs[String] must contain(s"${newLocation.id.get}")
          }
        }
    }

    "Accept retrieval of things and locations linked" in {
      val newLocation = createNewValidLocation
      newLocation.id must beSome

      val someThing = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.thingValid)
      ) ~>
        sealRoute(thingEndpoint.createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("tv")
          responseAs[ApiThing]
        }

      someThing.id must beSome
      //test link to thing
      HttpRequest(
        POST, s"/${newLocation.id.get}/thing/${someThing.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)
      ) ~>
        sealRoute(linkToThing) ~>
        check {
          response.status should be equalTo Created //retuns BadRequest, should be Created
          responseAs[String] must contain("id")
        }

      HttpRequest(GET, s"/${newLocation.id.get}" + ownerAuthParams) ~>
        sealRoute(getApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain(s"${someThing.id.get}")
            responseAs[String] must contain(s"${someThing.name}")
          }
        }
    }

    "Reject bad locations and relationships" in {
      val tmpLocation = HttpRequest(POST, "" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.locationBadName)) ~>
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

    "List All Entities correctly" in {
      val newLocation = createNewValidLocation
      newLocation.id must beSome

      val subLocation = createSubLocation
      subLocation.id must beSome

      HttpRequest(GET, "" + ownerAuthParams) ~>
        sealRoute(getAllApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val allLocations = responseAs[List[ApiLocation]]
            val asString = responseAs[String]
            asString must contain(s"${newLocation.id.get}")
            asString must contain(s"${newLocation.name}")
            asString must contain(s"${subLocation.id.get}")
            asString must contain(s"${subLocation.name}")
          }
        }
    }

    "Accept Type annotations" in {
      //      addTypeApi
      val newLocation = createNewValidLocation
      newLocation.id must beSome

      val typeSpec = new TypeSpec
      val postalAddressType = typeSpec.createPostalAddressType

      HttpRequest(
        POST,
        s"/${newLocation.id.get}/type/${postalAddressType.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)
      ) ~>
        sealRoute(addTypeApi) ~>
        check {
          response.status should be equalTo Created
        }

      HttpRequest(
        POST, s"/${newLocation.id.get}/type/0" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)
      ) ~>
        sealRoute(addTypeApi) ~>
        check {
          response.status should be equalTo BadRequest
        }

    }

    class Context extends Scope {
      val property = Context.property
      val populatedData = Context.populatedData
    }

    object Context {
      val propertySpec = new PropertySpec()
      val property = propertySpec.createWeightProperty
      val dataSpec = new DataSpec()
      dataSpec.createBasicTables
      val populatedData = dataSpec.populateDataReusable
    }

    "Handle Dynamic Property Linking" in new Context {
      /*
      Dynamic Property link example:
      {
       "property": {
         "id": 222,
         "name": "property name",
         "description": "optional property description",
         "propertyType": {
           "id": 333,
           "name": "type name",
           "description": "optional type description"
         },
         "unitOfMeasurement": {
           "id": 444,
           "name": "uom name",
           "description": "optional uom description",
           "symbol": "optional uom symbol"
         }
       },
       "relationshipType": "dynamicProperty",
       "field": {
         "id": 123,
         "name": "field name"
       }
      }
      */

      logger.debug("TEST Handle Dynamic Property Linking")

      val newLocation = createNewValidLocation
      newLocation.id must beSome

      val dataField = populatedData match {
        case (dataTable, dataField, record) =>
          dataField
      }
      val dynamicPropertyLink = ApiPropertyRelationshipDynamic(
        None, property, None, None, "test property", dataField)

      val propertyLinkId = HttpRequest(
        POST, s"/${newLocation.id.get}/property/dynamic/${property.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, dynamicPropertyLink.toJson.toString)
      ) ~>
        sealRoute(linkToPropertyDynamic) ~>
        check {
          response.status should be equalTo Created
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/${newLocation.id.get}/property/dynamic/${propertyLinkId.id}/values" + ownerAuthParams) ~>
        sealRoute(getPropertyDynamicValueApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must contain("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }

      HttpRequest(GET, s"/${newLocation.id.get}/values" + ownerAuthParams) ~>
        sealRoute(getApiValues) ~>
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
      /*
      {
        "property": {
          "id": 222,
          "name": "property name",
          "description": "optional property description",
          "propertyType": {
            "id": 333,
            "name": "type name",
            "description": "optional type description"
          },
          "unitOfMeasurement": {
            "id": 444,
            "name": "uom name",
            "description": "optional uom description",
            "symbol": "optional uom symbol"
          }
        },
        "relationshipType": "dynamicProperty",
        "field": {
          "id": 123,
          "name": "field name"
        },
        "record": {
          "id": 111,
          "name": "record name"
        }
       }
       */
      logger.debug("TEST Handle Static Property Linking")

      val newLocation = createNewValidLocation
      newLocation.id must beSome

      val dataField = populatedData match {
        case (dataTable, dataField, record) =>
          dataField
      }

      val dataRecord = populatedData match {
        case (dataTable, dataField, record) =>
          record
      }
      val staticPropertyLink = ApiPropertyRelationshipStatic(
        None, property, None, None, "test property", dataField, dataRecord)

      val propertyLinkId = HttpRequest(
        POST, s"/${newLocation.id.get}/property/static/${property.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, staticPropertyLink.toJson.toString)
      ) ~>
        sealRoute(linkToPropertyStatic) ~>
        check {
          response.status should be equalTo Created
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/${newLocation.id.get}/property/static/${propertyLinkId.id}/values" + ownerAuthParams) ~>
        sealRoute(getPropertyStaticValueApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must not contain("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }

      HttpRequest(GET, s"/${newLocation.id.get}/values" + ownerAuthParams) ~>
        sealRoute(getApiValues) ~>
        check {
          eventually {
            response.status should be equalTo OK
            logger.debug("Get property linked statically response:" + response.toString)
            responseAs[String] must contain("testValue1")
            responseAs[String] must not contain("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }
    }
  }
}
