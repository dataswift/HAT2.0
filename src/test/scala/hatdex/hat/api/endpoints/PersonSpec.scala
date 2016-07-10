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
import hatdex.hat.api.endpoints.jsonExamples.{ DataExamples, EntityExamples }
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
import spray.httpx.SprayJsonSupport._

class PersonSpec extends Specification with Specs2RouteTest with Person with BeforeAfterAll {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  val locationEndpoint = new Location {
    def actorRefFactory = system
    override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
    val logger: LoggingAdapter = system.log
  }

  val organisationEndpoint = new Organisation {
    def actorRefFactory = system
    override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
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

  val ownerAuthToken = HatAuthTestHandler.validUsers.find(_.role == "owner").map(_.userId).flatMap { ownerId =>
    HatAuthTestHandler.validAccessTokens.find(_.userId == ownerId).map(_.accessToken)
  } getOrElse ("")
  val ownerAuthHeader = RawHeader("X-Auth-Token", ownerAuthToken)

  def createNewPerson = HttpRequest(POST, "/person")
    .withHeaders(ownerAuthHeader)
    .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.personValid)) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("HATperson")
      }
      responseAs[ApiPerson]
    }

  "Person Endpoint" should {
    "Accept new people created" in {
      //test createEntity
      val newPerson = createNewPerson
      newPerson.id must beSome
    }

    "Accept relationships with people created" in {
      val newPerson = createNewPerson
      newPerson.id must beSome

      val personRelative = HttpRequest(POST, "/person")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.personRelative)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("HATRelative")
          responseAs[ApiPerson]
        }

      personRelative.id must beSome

      val personRelationship = HttpRequest(POST, "/person/relationshipType")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipPersonRelative)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("Family Member")
          responseAs[ApiPersonRelationshipType].id must beSome
          responseAs[String]
        }

      HttpRequest(POST, s"/person/${newPerson.id.get}/person/${personRelative.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, personRelationship)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      HttpRequest(GET, s"/person/${newPerson.id.get}")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain(s"${personRelative.id.get}")
            responseAs[String] must contain(s"${personRelative.name}")
          }
        }

    }

    "Retrieve created people" in {
      val newPerson = createNewPerson
      newPerson.id must beSome

      HttpRequest(GET, s"/person/${newPerson.id.get}")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("HATperson")
            responseAs[ApiPerson]
            responseAs[String] must contain(s"${newPerson.id.get}")
          }
        }
    }

    "Allow retrieval of people and other entities linked" in {
      val newPerson = createNewPerson
      newPerson.id must beSome

      val personLocation = HttpRequest(POST, "/location")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.locationValid)) ~>
        sealRoute(locationEndpoint.routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("home")
          responseAs[ApiLocation]
        }

      personLocation.id must beSome
      //test linkToPerson
      HttpRequest(POST, s"/person/${newPerson.id.get}/location/${personLocation.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipNextTo)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      val personEmployer = HttpRequest(POST, "/location")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.orgValid)) ~>
        sealRoute(locationEndpoint.routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("HATorg")
          responseAs[ApiOrganisation]
        }

      personEmployer.id must beSome
      //test linkToPerson
      HttpRequest(POST, s"/person/${newPerson.id.get}/location/${personEmployer.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipWorksAt)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          responseAs[String] must contain("id")
        }

      HttpRequest(GET, s"/person/${newPerson.id.get}")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
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
      HttpRequest(POST, "/person")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.personBadName)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }

      HttpRequest(POST, s"/person/0/location/1")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }

      HttpRequest(POST, s"/person/0/organisation/0")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }
    }

    "Reject unsuported relationships" in {
      val newPerson = createNewPerson
      newPerson.id must beSome

      HttpRequest(POST, s"/person/${newPerson.id.get}/thing/1")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }

      HttpRequest(POST, s"/person/${newPerson.id.get}/event/1")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].cause must contain("Operation Not Supprted")
        }
    }

    "Accept Type annotations" in {
      //      addTypeApi
      val newPerson = createNewPerson
      newPerson.id must beSome

      val typeSpec = new TypeSpec
      val postalAddressType = typeSpec.createPostalAddressType

      HttpRequest(POST, s"/person/${newPerson.id.get}/type/${postalAddressType.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
        }

      HttpRequest(POST, s"/person/${newPerson.id.get}/type/0")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, EntityExamples.relationshipType)) ~>
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
      val newPerson = createNewPerson
      newPerson.id must beSome

      val dataField = populatedData match {
        case (dataTable, dataField, record) =>
          dataField
      }
      val dynamicPropertyLink = ApiPropertyRelationshipDynamic(
        None, property, None, None, "test property", dataField)

      val propertyLinkId = HttpRequest(POST, s"/person/${newPerson.id.get}/property/dynamic/${property.id.get}")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, dynamicPropertyLink.toJson.toString)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Created
          }
          responseAs[ApiGenericId]
        }

      HttpRequest(GET, s"/person/${newPerson.id.get}/property/dynamic")
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

      HttpRequest(GET, s"/person/${newPerson.id.get}/property/dynamic/${propertyLinkId.id}/values")
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

      HttpRequest(GET, s"/person/${newPerson.id.get}/values")
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
      val newPerson = createNewPerson
      newPerson.id must beSome

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

      val propertyLinkId = HttpRequest(POST, s"/person/${newPerson.id.get}/property/static/${property.id.get}")
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

      HttpRequest(GET, s"/person/${newPerson.id.get}/property/static")
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

      HttpRequest(GET, s"/person/${newPerson.id.get}/property/static/${propertyLinkId.id}/values")
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

      HttpRequest(GET, s"/person/${newPerson.id.get}/values")
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
