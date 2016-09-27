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

class ThingSpec extends Specification with Specs2RouteTest with Thing with BeforeAfterAll with DalExecutionContext {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  val personEndpoint = new Person with DalExecutionContext {
    def actorRefFactory = system

    override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

    override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

    val logger: LoggingAdapter = system.log
  }

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

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

  logger.debug(s"Owner auth header: $ownerAuthHeader")

  def createNewThing = HttpRequest(POST, "/thing")
    .withHeaders(ownerAuthHeader)
    .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.thingValid)) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        // logger.debug("Create thing response:" + response.toString)
        response.status should be equalTo Created
        responseAs[String] must contain("tv")
      }
      responseAs[ApiThing]
    }

  def createOtherThing = HttpRequest(POST, "/thing")
    .withHeaders(ownerAuthHeader)
    .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.otherThingValid)) ~>
    sealRoute(routes) ~>
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
      HttpRequest(POST, s"/thing/${newThing.id.get}/thing/${otherThing.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }
    }

    "Accept relationships with things created" in {

      val newThing = createNewThing
      newThing.id must beSome

      val ownerPerson = HttpRequest(POST, "/person")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.personValid)) ~>
        sealRoute(personEndpoint.routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("HATperson")
          responseAs[ApiThing]
        }

      ownerPerson.id must beSome
      //test linkToPerson
      HttpRequest(POST, s"/thing/${newThing.id.get}/person/${ownerPerson.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipOwnedBy)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      HttpRequest(GET, s"/thing/${newThing.id.get}")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
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

      HttpRequest(GET, s"/thing/${newThing.id.get}")
        .withHeaders(ownerAuthHeader) ~>
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
      HttpRequest(POST, "/thing")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.thingBadName)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }

      HttpRequest(POST, s"/thing/0/thing/1")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }

      HttpRequest(POST, s"/thing/0/person/0")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }
    }

    "Reject unsuported relationships" in {
      val newThing = createNewThing
      newThing.id must beSome

      HttpRequest(POST, s"/thing/${newThing.id.get}/organisation/1")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }

      HttpRequest(POST, s"/thing/${newThing.id.get}/location/1")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }

      HttpRequest(POST, s"/thing/${newThing.id.get}/event/1")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
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

      HttpRequest(GET, "/thing")
        .withHeaders(ownerAuthHeader) ~>
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

      HttpRequest(POST, s"/thing/${newThing.id.get}/type/${postalAddressType.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
        }

      HttpRequest(POST, s"/thing/${newThing.id.get}/type/0")
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
      val newThing = createNewThing
      newThing.id must beSome

      val dataField = populatedData match {
        case (dataTable, dataField, record) =>
          dataField
      }
      val dynamicPropertyLink = ApiPropertyRelationshipDynamic(
        None, property, None, None, "test property", dataField)

      val propertyLinkId = HttpRequest(POST, s"/thing/${newThing.id.get}/property/dynamic/${property.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, dynamicPropertyLink.toJson.toString)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Created
          }
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/thing/${newThing.id.get}/property/dynamic")
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

      HttpRequest(GET, s"/thing/${newThing.id.get}/property/dynamic/${propertyLinkId.id}/values")
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

      HttpRequest(GET, s"/thing/${newThing.id.get}/values")
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
        POST, s"/thing/${newThing.id.get}/property/static/${property.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, staticPropertyLink.toJson.toString)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            logger.debug("Static property creation resp: "+response.toString)
            response.status should be equalTo Created
          }
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/thing/${newThing.id.get}/property/static")
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

      HttpRequest(GET, s"/thing/${newThing.id.get}/property/static/${propertyLinkId.id}/values")
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

      HttpRequest(GET, s"/thing/${newThing.id.get}/values")
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
