/*
 * Copyright (C) 2017 HAT Data Exchange Ltd
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
 *
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>
 * 5 / 2017
 */

package org.hatdex.hat.api.controllers

import com.mohiva.play.silhouette.test._
import org.hatdex.hat.api.HATTestContext
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.richData.{ DataDebitContractService, RichDataService }
import org.joda.time.LocalDateTime
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.{ BeforeAll, BeforeEach }
import play.api.Logger
import play.api.libs.json.{ JsArray, JsObject, JsValue, Json }
import play.api.mvc.Result
import play.api.test.{ FakeRequest, Helpers, PlaySpecification }

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

class ContractDataSpec(implicit ee: ExecutionEnv)
    extends PlaySpecification
    with Mockito
    with ContractDataContext
    with BeforeEach
    with BeforeAll {

  val logger = Logger(this.getClass)

  import org.hatdex.hat.api.json.RichDataJsonFormats._

  sequential

  def beforeAll: Unit =
    Await.result(databaseReady, 60.seconds)

  override def before: Unit = {
    import org.hatdex.hat.dal.Tables._
    import org.hatdex.libs.dal.HATPostgresProfile.api._
    val action = DBIO.seq()
    Await.result(hatDatabase.run(action), 60.seconds)
  }

  "The Save Contract method" should {
    "Save a single record" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)
        .withJsonBody(saveContractJson)

      val controller = application.injector.instanceOf[ContractData]

      val response = for {
        _ <- Helpers.call(controller.createContractData("testnamespace", "testendpoint", None), request)
        r <- Helpers.call(controller.readContractData("testnamespace", "testendpoint", None, None, None, None), request)
      } yield r

      val responseData = contentAsJson(response).as[Seq[EndpointData]]
      responseData.length must beEqualTo(1)
      responseData.head.data must be equalTo saveContractJson
    }
  }

  "The Read Contract Data method" should {
    "Return an empty array for an unknown endpoint" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[RichData]

      val response     = Helpers.call(controller.getEndpointData("test", "endpoint", None, None, None, None), request)
      val responseData = contentAsJson(response).as[Seq[EndpointData]]
      responseData must beEmpty
    }
  }

  "The Update Contract Data method" should {
    "Return an empty array for an unknown endpoint" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[RichData]

      val response     = Helpers.call(controller.getEndpointData("test", "endpoint", None, None, None, None), request)
      val responseData = contentAsJson(response).as[Seq[EndpointData]]
      responseData must beEmpty
    }
  }
}

trait ContractDataContext extends HATTestContext {
  val saveContractJson: JsValue =
    Json.parse(
      """{"token":"acf871b6-6008-11eb-ae93-0242ac130002","hatName":"contracthat","contractId":"acf871b6-6008-11eb-ae93-0242ac130002","body":{"a":"b"}}"""
    )
}
