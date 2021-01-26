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

  // "The Read Contract Data method" should {
  //   "Return an empty array for an unknown endpoint" in {
  //     val request = FakeRequest("GET", "http://hat.hubofallthings.net")
  //       .withAuthenticator(owner.loginInfo)

  //     val controller = application.injector.instanceOf[RichData]

  //     val response     = Helpers.call(controller.getEndpointData("test", "endpoint", None, None, None, None), request)
  //     val responseData = contentAsJson(response).as[Seq[EndpointData]]
  //     responseData must beEmpty
  //   }
  // }

  // "The Update Contract Data method" should {
  //   "Return an empty array for an unknown endpoint" in {
  //     val request = FakeRequest("GET", "http://hat.hubofallthings.net")
  //       .withAuthenticator(owner.loginInfo)

  //     val controller = application.injector.instanceOf[RichData]

  //     val response     = Helpers.call(controller.getEndpointData("test", "endpoint", None, None, None, None), request)
  //     val responseData = contentAsJson(response).as[Seq[EndpointData]]
  //     responseData must beEmpty
  //   }
  // }

}

trait ContractDataContext extends HATTestContext {
  val saveContractJson: JsValue =
    Json.parse(
      """{"token":"acf871b6-6008-11eb-ae93-0242ac130002","hatName":"contracthat","contractId":"acf871b6-6008-11eb-ae93-0242ac130002","body":{"a":"b"}}"""
    )

  val simpleJson: JsValue = Json.parse("""
      | {
      |   "field": "value",
      |   "date": 1492699047,
      |   "date_iso": "2017-04-20T14:37:27+00:00",
      |   "anotherField": "anotherFieldValue",
      |   "object": {
      |     "objectField": "objectFieldValue",
      |     "objectFieldArray": ["objectFieldArray1", "objectFieldArray2", "objectFieldArray3"],
      |     "objectFieldObjectArray": [
      |       {"subObjectName": "subObject1", "subObjectName2": "subObject1-2"},
      |       {"subObjectName": "subObject2", "subObjectName2": "subObject2-2"}
      |     ]
      |   }
      | }
    """.stripMargin)

  val simpleJson2: JsValue = Json.parse("""
      | {
      |   "field": "value2",
      |   "date": 1492799047,
      |   "date_iso": "2017-04-21T18:24:07+00:00",
      |   "anotherField": "anotherFieldDifferentValue",
      |   "object": {
      |     "objectField": "objectFieldValue",
      |     "objectFieldArray": ["objectFieldArray1", "objectFieldArray2", "objectFieldArray3"],
      |     "objectFieldObjectArray": [
      |       {"subObjectName": "subObject1", "subObjectName2": "subObject1-2"},
      |       {"subObjectName": "subObject2", "subObjectName2": "subObject2-2"}
      |     ]
      |   }
      | }
    """.stripMargin)

  val complexJson: JsValue = Json.parse("""
      | {
      |  "birthday": "01/01/1970",
      |  "age_range": {
      |    "min": 18
      |  },
      |  "education": [
      |    {
      |      "school": {
      |        "id": "123456789",
      |        "name": "school name"
      |      },
      |      "type": "High School",
      |      "year": {
      |        "id": "123456789",
      |        "name": "1972"
      |      },
      |      "id": "123456789"
      |    },
      |    {
      |      "concentration": [
      |        {
      |          "id": "123456789",
      |          "name": "Computer science"
      |        }
      |      ],
      |      "school": {
      |        "id": "12345678910",
      |        "name": "university name"
      |      },
      |      "type": "Graduate School",
      |      "year": {
      |        "id": "123456889",
      |        "name": "1973"
      |      },
      |      "id": "12345678910"
      |    }
      |  ],
      |  "email": "email@example.com",
      |  "hometown": {
      |    "id": "12345678910",
      |    "name": "london, uk"
      |  },
      |  "locale": "en_GB",
      |  "id": "12345678910"
      |}
    """.stripMargin)

  private val simpleTransformation: JsObject = Json
    .parse("""
      | {
      |   "data.newField": "anotherField",
      |   "data.arrayField": "object.objectFieldArray",
      |   "data.onemore": "object.education[1]"
      | }
    """.stripMargin)
    .as[JsObject]

  private val complexTransformation: JsObject = Json
    .parse("""
      | {
      |   "data.newField": "hometown.name",
      |   "data.arrayField": "education",
      |   "data.onemore": "education[0].type"
      | }
    """.stripMargin)
    .as[JsObject]

  val testEndpointQuery = Seq(EndpointQuery("test/test", Some(simpleTransformation), None, None),
                              EndpointQuery("test/complex", Some(complexTransformation), None, None)
  )

  val testEndpointQueryUpdated = Seq(EndpointQuery("test/test", Some(simpleTransformation), None, None),
                                     EndpointQuery("test/anothertest", None, None, None)
  )

  val testBundle = EndpointDataBundle(
    "testBundle",
    Map(
      "test" -> PropertyQuery(List(EndpointQuery("test/test", Some(simpleTransformation), None, None)),
                              Some("data.newField"),
                              None,
                              Some(3)
          ),
      "complex" -> PropertyQuery(List(EndpointQuery("test/complex", Some(complexTransformation), None, None)),
                                 Some("data.newField"),
                                 None,
                                 Some(1)
          )
    )
  )

  val failingCondition = EndpointDataBundle(
    "testfailCondition",
    Map(
      "test" -> PropertyQuery(
            List(
              EndpointQuery("test/test",
                            None,
                            Some(
                              Seq(
                                EndpointQueryFilter("field",
                                                    transformation = None,
                                                    operator = FilterOperator.Contains(Json.toJson("N/A"))
                                )
                              )
                            ),
                            None
              )
            ),
            Some("data.newField"),
            None,
            Some(3)
          )
    )
  )

  val matchingCondition = EndpointDataBundle(
    "testfailCondition",
    Map(
      "test" -> PropertyQuery(
            List(
              EndpointQuery("test/test",
                            None,
                            Some(
                              Seq(
                                EndpointQueryFilter("field",
                                                    transformation = None,
                                                    operator = FilterOperator.Contains(Json.toJson("value"))
                                )
                              )
                            ),
                            None
              )
            ),
            Some("data.newField"),
            None,
            Some(3)
          )
    )
  )

  val testBundle2 = EndpointDataBundle(
    "testBundle2",
    Map(
      "test" -> PropertyQuery(List(EndpointQuery("test/test", Some(simpleTransformation), None, None)),
                              Some("data.newField"),
                              None,
                              Some(3)
          ),
      "complex" -> PropertyQuery(List(EndpointQuery("test/anothertest", None, None, None)),
                                 Some("data.newField"),
                                 None,
                                 Some(1)
          )
    )
  )

  val testDataDebitRequest =
    DataDebitRequest(testBundle, None, LocalDateTime.now(), LocalDateTime.now().plusDays(3), rolling = false)

  val testDataDebitRequestUpdate =
    DataDebitRequest(testBundle2, None, LocalDateTime.now(), LocalDateTime.now().plusDays(3), rolling = false)

  val ddRequestionConditionsFailed = DataDebitRequest(testBundle,
                                                      Some(failingCondition),
                                                      LocalDateTime.now(),
                                                      LocalDateTime.now().plusDays(3),
                                                      rolling = false
  )

  val ddRequestionConditionsFulfilled = DataDebitRequest(testBundle,
                                                         Some(matchingCondition),
                                                         LocalDateTime.now(),
                                                         LocalDateTime.now().plusDays(3),
                                                         rolling = false
  )
}
