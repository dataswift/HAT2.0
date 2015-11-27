package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.api.endpoints.jsonExamples.{DataExamples, EntityExamples}
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.{BeforeAfterAll, Scope}
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{HttpEntity, HttpRequest, MediaTypes}
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.testkit.Specs2RouteTest

class OrganisationSpec extends Specification with Specs2RouteTest with Organisation with BeforeAfterAll {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  val locationEndpoint = new Location {
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

  def createNewValidOrg = HttpRequest(
    POST, "/organisation" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.orgValid)) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("HATorg")
      }
      responseAs[ApiOrganisation]
    }

  def createOtherOrg = HttpRequest(
    POST, "/organisation" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.otherOrgValid)) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("HATcontrol")
      }
      responseAs[ApiOrganisation]
    }

  "Organisation" should {
    "Accept new organisations created" in {
      //test createEntity
      val newOrg = createNewValidOrg
      newOrg.id must beSome

      val otherOrg = createOtherOrg
      otherOrg.id must beSome

      HttpRequest(POST, s"/organisation/${newOrg.id.get}/organisation/${otherOrg.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipControls)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }
    }

    "Reject unsuported relationships" in {
      val newOrg = createNewValidOrg
      newOrg.id must beSome

      HttpRequest(
        POST,
        s"/${newOrg.id.get}/person/1" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToPerson) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }

      HttpRequest(
        POST,
        s"/${newOrg.id.get}/event/1" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToEvent) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }

      HttpRequest(
        POST,
        s"/${newOrg.id.get}/thing/1" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkToThing) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }
    }

    "Retrieve created organisations" in {
      val newOrg = createNewValidOrg
      newOrg.id must beSome

      HttpRequest(
        GET,
        s"/organisation/${newOrg.id.get}" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("HATorg")
            responseAs[ApiOrganisation]
            responseAs[String] must contain(s"${newOrg.id.get}")
          }
        }
    }

    "Accept retrieval of things linked to organisations" in {
      val newOrg = createNewValidOrg
      newOrg.id must beSome

      val someLocation = HttpRequest(
        POST, "" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.locationValid)
      ) ~>
        sealRoute(locationEndpoint.createEntity) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("home")
          responseAs[ApiThing]
        }

      someLocation.id must beSome
      //test link to thing
      HttpRequest(
        POST, s"/organisation/${newOrg.id.get}/location/${someLocation.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)
      ) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      HttpRequest(GET, s"/organisation/${newOrg.id.get}" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain(s"${someLocation.id.get}")
            responseAs[String] must contain(s"${someLocation.name}")
          }
        }
    }

    "Reject bad locations and relationships" in {
      HttpRequest(POST, "/organisation" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.orgBadName)) ~>
        sealRoute(routes) ~> check {
        response.status should be equalTo BadRequest
      }

      HttpRequest(POST, s"/organisation/0/location/1}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~> check {
        response.status should be equalTo NotFound
      }

      HttpRequest(POST, s"/organisation/0/thing/0}" + ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~> check {
        response.status should be equalTo NotFound
      }
    }

    "List All Organisations correctly" in {
      val newOrg = createNewValidOrg
      newOrg.id must beSome

      val otherOrg = createOtherOrg
      otherOrg.id must beSome

      HttpRequest(GET, "/organisation" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[List[ApiOrganisation]]
            val asString = responseAs[String]
            asString must contain(s"${newOrg.id.get}")
            asString must contain(s"${newOrg.name}")
            asString must contain(s"${otherOrg.id.get}")
            asString must contain(s"${otherOrg.name}")
          }
        }
    }

    "Accept Type annotations" in {
      //      addTypeApi
      val newOrg = createNewValidOrg
      newOrg.id must beSome

      val typeSpec = new TypeSpec
      val postalAddressType = typeSpec.createPostalAddressType

      HttpRequest(
        POST,
        s"/organisation/${newOrg.id.get}/type/${postalAddressType.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)
      ) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
        }

      HttpRequest(
        POST, s"/organisation/${newOrg.id.get}/type/0" + ownerAuthParams,
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

      val newOrg = createNewValidOrg
      newOrg.id must beSome

      val dataField = populatedData match {
        case (dataTable, dataField, record) =>
          dataField
      }
      val dynamicPropertyLink = ApiPropertyRelationshipDynamic(
        None, property, None, None, "test property", dataField)

      val propertyLinkId = HttpRequest(
        POST, s"/organisation/${newOrg.id.get}/property/dynamic/${property.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, dynamicPropertyLink.toJson.toString)
      ) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Created
          }
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/organisation/${newOrg.id.get}/property/dynamic" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("BodyWeight")
            responseAs[String] must contain("field")
            responseAs[String] must not contain("record")
          }
        }

      HttpRequest(GET, s"/organisation/${newOrg.id.get}/property/dynamic/${propertyLinkId.id}/values" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must contain("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }

      HttpRequest(GET, s"/organisation/${newOrg.id.get}/values" + ownerAuthParams) ~>
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

      val newOrg = createNewValidOrg
      newOrg.id must beSome

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
        POST, s"/organisation/${newOrg.id.get}/property/static/${property.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, staticPropertyLink.toJson.toString)
      ) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Created
          }
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/organisation/${newOrg.id.get}/property/static" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("BodyWeight")
            responseAs[String] must contain("field")
            responseAs[String] must contain("record")
          }
        }

      HttpRequest(GET, s"/organisation/${newOrg.id.get}/property/static/${propertyLinkId.id}/values" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must not contain("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }

      HttpRequest(GET, s"/organisation/${newOrg.id.get}/values" + ownerAuthParams) ~>
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
