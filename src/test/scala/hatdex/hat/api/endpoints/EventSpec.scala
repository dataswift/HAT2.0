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
import hatdex.hat.api.endpoints.jsonExamples.EntityExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.authentication.authenticators.{ AccessTokenHandler, UserPassHandler }
import org.specs2.mutable.Specification
import org.specs2.specification.{ BeforeAfterAll, Scope }
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{ HttpEntity, HttpRequest, MediaTypes }
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.testkit.Specs2RouteTest

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class EventSpec extends Specification with Specs2RouteTest with Event with BeforeAfterAll {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  val personEndpoint = new Person {
    def actorRefFactory = system
    override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
    val logger: LoggingAdapter = system.log
  }

  val thingEndpoint = new Thing {
    def actorRefFactory = system
    override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
    val logger: LoggingAdapter = system.log
  }

  val organisationEndpoint = new Organisation {
    def actorRefFactory = system
    override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
    val logger: LoggingAdapter = system.log
  }

  val locationEndpoint = new Location {
    def actorRefFactory = system
    val logger: LoggingAdapter = system.log
    override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
  }

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  import JsonProtocol._

  def beforeAll() = {
    Await.result(TestDataCleanup.cleanupAll, Duration("20 seconds"))
  }

  def afterAll() = {
  }

  val ownerAuthToken = HatAuthTestHandler.validUsers.find(_.role == "owner").map(_.userId).flatMap { ownerId =>
    HatAuthTestHandler.validAccessTokens.find(_.userId == ownerId).map(_.accessToken)
  } getOrElse ("")
  val ownerAuthHeader = RawHeader("X-Auth-Token", ownerAuthToken)

  def createNewEvent = HttpRequest(POST, "/event")
    .withHeaders(ownerAuthHeader)
    .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.eventValid)) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("sunrise")
      }
      responseAs[ApiEvent]
    }

  def createOtherEvent = HttpRequest(POST, "/event")
    .withHeaders(ownerAuthHeader)
    .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.otherEventValid)) ~>
    sealRoute(routes) ~>
    check {
      response.status should be equalTo Created
      responseAs[String] must contain("breakfast")
      responseAs[ApiEvent]
    }

  sequential

  "EventsService" should {
    "Accept new events created" in {
      //test createEntity
      val newEvent = createNewEvent
      newEvent.id must beSome

      val otherEvent = createOtherEvent
      otherEvent.id must beSome

      //test linkToEvent
      HttpRequest(POST, s"/event/${newEvent.id.get}/event/${otherEvent.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipDuring)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          logger.info(s"Event created: ${responseAs[String]}")
          responseAs[String] must contain("id")
        }
    }

    "Accept relationships with other entities created" in {

      val newEvent = createNewEvent
      newEvent.id must beSome

      // Linking event to Person
      val ownerPerson = HttpRequest(POST, "/person")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.personValid)) ~>
        sealRoute(personEndpoint.routes) ~>
        check {
          logger.info(s"Person create response: ${responseAs[String]}")
          response.status should be equalTo Created
          responseAs[String] must contain("HATperson")
          responseAs[ApiEvent]
        }

      ownerPerson.id must beSome

      HttpRequest(POST, s"/event/${newEvent.id.get}/person/${ownerPerson.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipOwnedBy)) ~>
        sealRoute(routes) ~>
        check {
          logger.info(s"Relationship response: ${responseAs[String]}")
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      // Linking event to Thing

      val activeThing = HttpRequest(POST, "/thing")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.thingValid)) ~>
        sealRoute(thingEndpoint.routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("tv")
          responseAs[ApiThing]
        }

      activeThing.id must beSome

      HttpRequest(POST, s"/event/${newEvent.id.get}/thing/${activeThing.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipActiveAt)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      // Linking event to Location

      logger.debug("Creating location to link to event")
      val atLocation = HttpRequest(POST, "/location")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.locationValid)) ~>
        sealRoute(locationEndpoint.routes) ~>
        check {
          logger.debug("Location created: "+response)
          response.status should be equalTo Created
          responseAs[String] must contain("home")
          responseAs[ApiLocation]
        }

      atLocation.id must beSome

      HttpRequest(POST, s"/event/${newEvent.id.get}/location/${atLocation.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipHappensAt)) ~>
        sealRoute(routes) ~>
        check {
          logger.debug("event location link response: "+response)
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      // Linking event to Organisation

      val sponsoredByOrganisation = HttpRequest(POST, "/organisation")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.orgValid)) ~>
        sealRoute(organisationEndpoint.routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("HATorg")
          responseAs[ApiOrganisation]
        }

      sponsoredByOrganisation.id must beSome

      HttpRequest(POST, s"/event/${newEvent.id.get}/organisation/${sponsoredByOrganisation.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipHappensAt)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      HttpRequest(GET, s"/event/${newEvent.id.get}")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
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

      HttpRequest(GET, s"/event/${newEvent.id.get}")
        .withHeaders(ownerAuthHeader) ~>
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
      HttpRequest(POST, "/event")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.eventBadName)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo BadRequest
          }
        }

      HttpRequest(POST, s"/event/0/person/0")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo BadRequest
          }
        }

      HttpRequest(POST, s"/event/0/event/1")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo BadRequest
          }
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

      HttpRequest(GET, "/event")
        .withHeaders(ownerAuthHeader) ~>
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

      HttpRequest(POST, s"/event/${newEvent.id.get}/type/${postalAddressType.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
        }

      HttpRequest(POST, s"/event/${newEvent.id.get}/type/0")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }

    }

    val testLogger = logger
    object Context extends DataSpecContextMixin {
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
      val newEvent = createNewEvent
      newEvent.id must beSome

      val dataField = populatedData match {
        case (dataTable, dataField, record) =>
          dataField
      }
      val dynamicPropertyLink = ApiPropertyRelationshipDynamic(
        None, property, None, None, "test property", dataField)

      val propertyLinkId = HttpRequest(POST, s"/event/${newEvent.id.get}/property/dynamic/${property.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, dynamicPropertyLink.toJson.toString)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Created
          }
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/event/${newEvent.id.get}/property/dynamic")
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

      HttpRequest(GET, s"/event/${newEvent.id.get}/property/dynamic/${propertyLinkId.id}/values")
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

      HttpRequest(GET, s"/event/${newEvent.id.get}/values")
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

      val propertyLinkId = HttpRequest(POST, s"/event/${newEvent.id.get}/property/static/${property.id.get}")
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

      HttpRequest(GET, s"/event/${newEvent.id.get}/property/static")
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

      HttpRequest(GET, s"/event/${newEvent.id.get}/property/static/${propertyLinkId.id}/values")
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

      HttpRequest(GET, s"/event/${newEvent.id.get}/values")
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
