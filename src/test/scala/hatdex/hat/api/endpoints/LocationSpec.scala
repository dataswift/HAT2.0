/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.actors.DalExecutionContext
import hatdex.hat.api.endpoints.jsonExamples.{DataExamples, EntityExamples}
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.{BeforeAfterAll, Scope}
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{HttpEntity, HttpRequest, MediaTypes}
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.testkit.Specs2RouteTest

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class LocationSpec extends Specification with Specs2RouteTest with Location with BeforeAfterAll with DalExecutionContext {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  val thingEndpoint = new Thing with DalExecutionContext {
    def actorRefFactory = system
    override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
    val logger: LoggingAdapter = system.log
  }

  val typeEndpoint = new Type with DalExecutionContext {
    def actorRefFactory = system
    override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
    val logger: LoggingAdapter = system.log
  }

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  import JsonProtocol._

  def beforeAll() = {
    Await.result(TestDataCleanup.cleanupAll, Duration("20 seconds"))
  }

  // Clean up all data
  def afterAll() = {
//    TestDataCleanup.cleanupAll
  }

  val ownerAuthToken = HatAuthTestHandler.validUsers.find(_.role == "owner").map(_.userId).flatMap { ownerId =>
    HatAuthTestHandler.validAccessTokens.find(_.userId == ownerId).map(_.accessToken)
  } getOrElse ("")
  val ownerAuthHeader = RawHeader("X-Auth-Token", ownerAuthToken)

  def createNewValidLocation = HttpRequest(POST, "/location")
    .withHeaders(ownerAuthHeader)
    .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.locationValid)) ~>
    sealRoute(routes) ~> check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("home")
      }
      responseAs[ApiLocation]
    }

  def createSubLocation = HttpRequest(POST, "/location")
    .withHeaders(ownerAuthHeader)
    .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.locationHomeStairs)) ~>
    sealRoute(routes) ~> check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("stairs")
      }
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
      HttpRequest(POST, s"/location/${newLocation.id.get}/location/${subLocation.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }
    }

    "Reject unsuported relationships" in {
      val newLocation = createNewValidLocation
      newLocation.id must beSome

      HttpRequest(POST, s"/location/${newLocation.id.get}/organisation/1")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }

      HttpRequest(POST, s"/location/${newLocation.id.get}/person/1")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }

      HttpRequest(POST, s"/location/${newLocation.id.get}/event/1")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }
    }

    "Retrieve created locations" in {
      val newLocation = createNewValidLocation
      newLocation.id must beSome

      HttpRequest(GET, s"/location/${newLocation.id.get}")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
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

      val someThing = HttpRequest(POST, "/thing")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.thingValid)) ~>
        sealRoute(thingEndpoint.routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("tv")
          responseAs[ApiThing]
        }

      someThing.id must beSome
      //test link to thing
      HttpRequest(POST, s"/location/${newLocation.id.get}/thing/${someThing.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created //retuns BadRequest, should be Created
          responseAs[String] must contain("id")
        }

      HttpRequest(GET, s"/location/${newLocation.id.get}")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain(s"${someThing.id.get}")
            responseAs[String] must contain(s"${someThing.name}")
          }
        }
    }

    "Reject bad locations and relationships" in {
      val tmpLocation = HttpRequest(POST, "/location")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.locationBadName)) ~>
        sealRoute(routes) ~> check {
          response.status should be equalTo BadRequest
        }

      HttpRequest(POST, s"/location/0/location/1}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~> check {
          response.status should be equalTo NotFound
        }

      HttpRequest(POST, s"/location/0/thing/0}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~> check {
          response.status should be equalTo NotFound
        }
    }

    "List All Entities correctly" in {
      val newLocation = createNewValidLocation
      newLocation.id must beSome

      val subLocation = createSubLocation
      subLocation.id must beSome

      HttpRequest(GET, "/location")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
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

      HttpRequest(POST, s"/location/${newLocation.id.get}/type/${postalAddressType.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
        }

      HttpRequest(POST, s"/location/${newLocation.id.get}/type/0")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }

    }

    val testLogger = logger
    object Context extends DataSpecContextMixin with DalExecutionContext {
      val logger: LoggingAdapter = testLogger
      def actorRefFactory = system
      val propertySpec = new PropertySpec()
      val property = propertySpec.createWeightProperty
      createBasicTables
      val populatedData = populateDataReusable
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

      val newLocation = createNewValidLocation
      newLocation.id must beSome

      val dataField = populatedData match {
        case (dataTable, dataField, record) =>
          dataField
      }
      val dynamicPropertyLink = ApiPropertyRelationshipDynamic(
        None, property, None, None, "test property", dataField)

      val propertyLinkId = HttpRequest(POST, s"/location/${newLocation.id.get}/property/dynamic/${property.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, dynamicPropertyLink.toJson.toString)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Created
          }
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/location/${newLocation.id.get}/property/dynamic")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("BodyWeight")
            responseAs[String] must contain("field")
            responseAs[String] must not contain ("record")
          }
        }

      HttpRequest(GET, s"/location/${newLocation.id.get}/property/dynamic/${propertyLinkId.id}/values")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must contain("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }

      HttpRequest(GET, s"/location/${newLocation.id.get}/values")
        .withHeaders(ownerAuthHeader) ~>
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

      val propertyLinkId = HttpRequest(POST, s"/location/${newLocation.id.get}/property/static/${property.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, staticPropertyLink.toJson.toString)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Created
          }
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/location/${newLocation.id.get}/property/static")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("BodyWeight")
            responseAs[String] must contain("field")
            responseAs[String] must contain("record")
          }
        }

      HttpRequest(GET, s"/location/${newLocation.id.get}/property/static/${propertyLinkId.id}/values")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must not contain ("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }

      HttpRequest(GET, s"/location/${newLocation.id.get}/values")
        .withHeaders(ownerAuthHeader) ~>
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
