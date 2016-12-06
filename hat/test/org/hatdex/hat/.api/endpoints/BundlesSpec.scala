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
import org.hatdex.hat.api.actors.DalExecutionContext
import org.hatdex.hat.api.{DatabaseInfo, TestDataCleanup, TestFixtures}
import org.hatdex.hat.api.endpoints.jsonExamples.BundleExamples
import org.hatdex.hat.api.json.JsonProtocol
import org.hatdex.hat.api.models._
import org.hatdex.hat.authentication.HatAuthTestHandler
import org.hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.hatdex.hat.dal.SlickPostgresDriver.api._
import org.hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.json._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class BundlesSpec extends Specification with Specs2RouteTest with BeforeAfterAll with Bundles with DalExecutionContext {
  def actorRefFactory = system
  val logger: LoggingAdapter = system.log

  val ownerAuthToken = HatAuthTestHandler.validUsers.find(_.role == "owner").map(_.userId).flatMap { ownerId =>
    HatAuthTestHandler.validAccessTokens.find(_.userId == ownerId).map(_.accessToken)
  } getOrElse ("")
  val ownerAuthHeader = RawHeader("X-Auth-Token", ownerAuthToken)

  import JsonProtocol._

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  def beforeAll() = {
    val f = TestDataCleanup.cleanupAll.flatMap { c =>
      TestFixtures.contextlessBundleContext
    }
    Await.result(f, Duration("20 seconds"))
  }

  // Clean up all data
  def afterAll() = {
    //    TestDataCleanup.cleanupAll
  }

  sequential

  "Contextless Bundle Service" should {
    "Create and combine required bundles" in {
      val bundleJson: String = BundleExamples.fullbundle
      val cBundle = HttpRequest(POST, "/bundles/contextless")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, bundleJson)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            val responseString = responseAs[String]
            logger.debug(s"Bundle create response: $responseString")
            response.status should be equalTo Created
          }
          responseAs[ApiBundleContextless]
        }

      logger.debug(s"Looking up bundle id ${cBundle.id}")

      HttpRequest(GET, s"/bundles/contextless/${cBundle.id.get}")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            val responseString = responseAs[String]
            //            logger.info(s"Bundle data response ${responseString}")
            response.status should be equalTo OK
          }
        }

      HttpRequest(GET, s"/bundles/contextless/${cBundle.id.get}/values")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            val responseString = responseAs[String]

            //            logger.info(s"Bundle data response ${responseString}")

            response.status should be equalTo OK

            responseString must contain("dataGroups")
            responseString must contain("event record 1")
            responseString must contain("event record 2")
            responseString must contain("event record 3")

            responseString must contain("kitchen record 1")
            responseString must contain("kitchen record 2")
            responseString must contain("kitchen record 3")

            responseString must contain("kitchen value 1")
            responseString must contain("event name 1")
            responseString must contain("event location 1")
          }
        }
    }

    "Correctly exclude fields not part of a bundle from results" in {
      val bundleJson: String = BundleExamples.fieldSelectionsBundle
      val cBundle = HttpRequest(POST, "/bundles/contextless")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, bundleJson)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            val responseString = responseAs[String]
            response.status should be equalTo Created
          }
          responseAs[ApiBundleContextless]
        }

      HttpRequest(GET, s"/bundles/contextless/${cBundle.id.get}/values")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            val responseString = responseAs[String]

            response.status should be equalTo OK

            responseString must contain("dataGroups")
            responseString must contain("event record 1")
            responseString must contain("event record 2")
            responseString must contain("event record 3")

            responseString must contain("kitchen record 1")
            responseString must contain("kitchen record 2")
            responseString must contain("kitchen record 3")

            responseString must not contain ("kitchen time 1")
            responseString must contain("kitchen value 1")
            responseString must contain("event name 1")
            responseString must not contain ("event location 1")
            responseString must contain("event name 2")
            responseString must not contain ("event location 2")
            responseString must not contain ("event startTime 2")
            responseString must not contain ("event endTime 2")
          }
        }
    }

    "Do not allow source datasets with no tables/fields" in {
      val bundleJson: String = BundleExamples.fieldlessDataset
      HttpRequest(POST, "/bundles/contextless")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, bundleJson)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo BadRequest
          }
        }
    }

    "Return correct error code for bundle that doesn't exist" in {
      HttpRequest(GET, "/bundles/contextless/0")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo NotFound
        }
    }

  }
}

