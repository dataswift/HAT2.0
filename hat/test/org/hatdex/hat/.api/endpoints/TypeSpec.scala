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

package org.hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import spray.httpx.RequestBuilding._
import spray.http.HttpHeaders._
import org.hatdex.hat.api.TestDataCleanup
import org.hatdex.hat.api.endpoints.jsonExamples.TypeExamples
import org.hatdex.hat.api.json.JsonProtocol
import org.hatdex.hat.api.models.{ ApiSystemType, ApiSystemUnitofmeasurement, ErrorMessage }
import org.hatdex.hat.authentication.HatAuthTestHandler
import org.hatdex.hat.authentication.authenticators.{ AccessTokenHandler, UserPassHandler }
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._
import scala.concurrent.Await
import scala.concurrent.duration._

class TypeSpec extends Specification with Specs2RouteTest with Type with BeforeAfterAll {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  def beforeAll() = {
    Await.result(TestDataCleanup.cleanupAll, 10 seconds)
  }

  import JsonProtocol._

  // Clean up all data
  def afterAll() = {
//    TestDataCleanup.cleanupAll
  }

  def createPostalAddressType = HttpRequest(POST, "/type/type")
    .withEntity(HttpEntity(MediaTypes.`application/json`, TypeExamples.postalAddress))
    .withHeaders(ownerAuthHeader) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("PostalAddress")
        responseAs[ApiSystemType].id must beSome
      }
      responseAs[ApiSystemType]
    }

  def createDateType = HttpRequest(POST, "/type/type")
    .withEntity(HttpEntity(MediaTypes.`application/json`, TypeExamples.date))
    .withHeaders(ownerAuthHeader) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("Date")
        responseAs[ApiSystemType].id must beSome
      }
      responseAs[ApiSystemType]
    }

  def createPlaceType = HttpRequest(POST, "/type/type")
    .withEntity(HttpEntity(MediaTypes.`application/json`, TypeExamples.place))
    .withHeaders(ownerAuthHeader) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("Place")
        responseAs[ApiSystemType].id must beSome
      }
      responseAs[ApiSystemType]
    }

  def createQuantitativeValueType = HttpRequest(POST, "/type/type")
    .withEntity(HttpEntity(MediaTypes.`application/json`, TypeExamples.quantitativeValue))
    .withHeaders(ownerAuthHeader) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("QuantitativeValue")
        responseAs[ApiSystemType].id must beSome
      }
      responseAs[ApiSystemType]
    }

  def createMetersUom = HttpRequest(POST, "/type/unitofmeasurement")
    .withEntity(HttpEntity(MediaTypes.`application/json`, TypeExamples.uomMeters))
    .withHeaders(ownerAuthHeader) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        logger.debug("UOM create response: "+response.toString)
        response.status should be equalTo Created
        responseAs[String] must contain("meters")
        responseAs[ApiSystemUnitofmeasurement].id must beSome
      }
      responseAs[ApiSystemUnitofmeasurement]
    }

  def createWeightUom = HttpRequest(POST, "/type/unitofmeasurement")
    .withEntity(HttpEntity(MediaTypes.`application/json`, TypeExamples.uomWeight))
    .withHeaders(ownerAuthHeader) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("kilograms")
        responseAs[ApiSystemUnitofmeasurement].id must beSome
      }
      responseAs[ApiSystemUnitofmeasurement]
    }

  sequential

  val ownerAuthToken = HatAuthTestHandler.validUsers.find(_.role == "owner").map(_.userId).flatMap { ownerId =>
    HatAuthTestHandler.validAccessTokens.find(_.userId == ownerId).map(_.accessToken)
  } getOrElse ("")
  val ownerAuthHeader = RawHeader("X-Auth-Token", ownerAuthToken)

  "Types Service" should {
    "Accept new types created" in {
      val postalAddressType = createPostalAddressType
      val dateType = createDateType
      val placeType = createPlaceType

      HttpRequest(POST, s"/type/${placeType.id.get}/type/${postalAddressType.id.get}")
        .withEntity(HttpEntity(MediaTypes.`application/json`, TypeExamples.addressOfPlace))
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Created
          }
        }
    }

    "Allow type lookup" in {
      HttpRequest(GET, Uri("/type/type").withQuery(("name", "PostalAddress")))
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val types = responseAs[List[ApiSystemType]]
            types must have size (1)
            responseAs[String] must contain("PostalAddress")
          }
        }

      HttpRequest(GET, "/type/type")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val types = responseAs[List[ApiSystemType]]
            types must not be empty
            responseAs[String] must contain("PostalAddress")
            responseAs[String] must contain("Date")
            responseAs[String] must contain("Place")
          }
        }
    }

    "Disallow duplicte types" in pending {
      HttpRequest(POST, "/type/type")
        .withEntity(HttpEntity(MediaTypes.`application/json`, TypeExamples.postalAddress))
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo BadRequest
            responseAs[String] must contain("PostalAddress")
            responseAs[ErrorMessage].message must contain("Error")
          }
        }
    }

    "Rejecet bad linking of types" in {
      HttpRequest(POST, s"/type/1/type/0")
        .withEntity(HttpEntity(MediaTypes.`application/json`, TypeExamples.addressOfPlace))
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo BadRequest
            responseAs[ErrorMessage].message must contain("Error linking Types")
          }
        }
    }

    "Accept new Units of Measurement" in {
      val uom = createMetersUom
      uom.id must beSome
      val kilograms = createWeightUom
      kilograms.id must beSome
    }

    "Reject duplicate Units of Measurement" in pending {
      HttpRequest(POST, "/type/unitofmeasurement")
        .withEntity(HttpEntity(MediaTypes.`application/json`, TypeExamples.uomMeters))
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo BadRequest
            responseAs[ErrorMessage].message must contain("Error")
          }
        }
    }

    "Allow Unit of Measurement lookup" in {
      HttpRequest(GET, Uri("/type/unitofmeasurement").withQuery(("name", "meters")) )
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val uoms = responseAs[List[ApiSystemUnitofmeasurement]]
            uoms must not be empty
            responseAs[String] must contain("meters")
          }
        }

      HttpRequest(GET, Uri("/type/unitofmeasurement"))
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val uoms = responseAs[List[ApiSystemUnitofmeasurement]]
            uoms must not be empty
            responseAs[String] must contain("meters")
            responseAs[String] must contain("kilograms")
          }
        }

      HttpRequest(GET, Uri("/type/unitofmeasurement").withQuery(("name", "notExistingName")) )
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val uoms = responseAs[List[ApiSystemUnitofmeasurement]]
            uoms must be empty
          }
        }
    }

  }
}
