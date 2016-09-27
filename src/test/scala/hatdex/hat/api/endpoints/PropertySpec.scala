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
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.actors.DalExecutionContext
import hatdex.hat.api.endpoints.jsonExamples.PropertyExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.{ApiProperty, ApiSystemType, ApiSystemUnitofmeasurement}
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{HttpEntity, HttpRequest, MediaTypes, Uri}
import spray.json._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class PropertySpec extends Specification with Specs2RouteTest with Property with BeforeAfterAll with DalExecutionContext {
  def actorRefFactory = system
  val logger: LoggingAdapter = system.log

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

  sequential

  val ownerAuthToken = HatAuthTestHandler.validUsers.find(_.role == "owner").map(_.userId).flatMap { ownerId =>
    HatAuthTestHandler.validAccessTokens.find(_.userId == ownerId).map(_.accessToken)
  } getOrElse ("")
  val ownerAuthHeader = RawHeader("X-Auth-Token", ownerAuthToken)

  def createWeightProperty = {
    val typeSpec = new TypeSpec
    val quantitativeType = typeSpec.createQuantitativeValueType
    val weightUom = typeSpec.createWeightUom

    val weightProperty = ApiProperty(None, None, None, "BodyWeight",
      Some("Person body weight"), quantitativeType, weightUom)

    HttpRequest(POST, "/property")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, weightProperty.toJson.toString)) ~>
      sealRoute(routes) ~>
      check {
        eventually {
          response.status should be equalTo Created
          responseAs[String] must contain("BodyWeight")
          responseAs[String] must contain("QuantitativeValue")
          responseAs[String] must contain("kilograms")
        }
        responseAs[ApiProperty]
      }
  }

  sequential

  "Property Service" should {
    "Accept new properties created" in {
      val weightProperty = createWeightProperty
      weightProperty.id must beSome

      HttpRequest(GET, s"/property/${weightProperty.id.get}")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val asString = responseAs[String]
            asString must contain(s"${weightProperty.id.get}")
            asString must contain(s"${weightProperty.name}")
          }
        }
    }

    "List properties" in {
      HttpRequest(GET, s"/property")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val asList = responseAs[List[ApiProperty]]
            asList must not be empty
            val asString = responseAs[String]
            asString must contain("BodyWeight")
          }
        }

      HttpRequest(GET, Uri("/property").withQuery(("name", "BodyWeight")))
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val asList = responseAs[List[ApiProperty]]
            asList must not be empty
            val asString = responseAs[String]
            asString must contain("BodyWeight")
          }
        }

      HttpRequest(GET, Uri("/property").withQuery(("name", "RandomProperty")))
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val asList = responseAs[List[ApiProperty]]
            asList must have size (0)
          }
        }
    }

    "Reject incomplete properties" in {
      HttpRequest(POST, "/property")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, PropertyExamples.bodyWeight)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }

      val typeEndpoint = new Type {
        def actorRefFactory = system
        val logger: LoggingAdapter = system.log
        override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
        override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()
      }

      val quantitativeType = HttpRequest(GET, Uri("/type/type").withQuery(("name", "QuantitativeValue")))
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(typeEndpoint.routes) ~>
        check {
          response.status should be equalTo OK
          val types = responseAs[List[ApiSystemType]]
          types must have size (1)
          types.head.id must beSome
          types.head
        }

      val weightUom = ApiSystemUnitofmeasurement(None, None, None, "kilograms", None, Some("kg"))

      val weightProperty = ApiProperty(None, None, None, "BodyWeight",
        Some("Person body weight"), quantitativeType, weightUom)

      HttpRequest(POST, "/property")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, weightProperty.toJson.toString)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }
    }
  }
}
