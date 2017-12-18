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

package org.hatdex.hat.api.service.richData

import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import play.api.Logger
import play.api.libs.json.Reads._
import play.api.libs.json.{JsValue, Json, _}
import play.api.test.PlaySpecification

class JsonDataTransformerSpec extends PlaySpecification with Mockito {

  val logger = Logger(this.getClass)

  sequential

  "JSON mappers" should {
    "remap from simple fields to flat json" in new JsonDataTransformerContext {
      val transformation: JsObject = Json.parse("""
                                        | {
                                        |   "data.newField": "field",
                                        |   "data.newField": "anotherField",
                                        |   "data.arrayField": "object.objectFieldArray",
                                        |   "data.onemore": "object.objectFieldArray[1]"
                                        | }
                                      """.stripMargin).as[JsObject]

      val resultJson: JsResult[JsObject] = simpleJson.transform(JsonDataTransformer.mappingTransformer(transformation))

      (resultJson.get \ "data" \ "newField").as[String] must equalTo("anotherFieldValue")
      (resultJson.get \ "data" \ "arrayField").as[List[String]] must contain("objectFieldArray3")
      (resultJson.get \ "data" \ "onemore").as[String] must equalTo("objectFieldArray2")
    }

    "remap array objects" in new JsonDataTransformerContext {
      val transformation: JsObject = Json.parse("""
                                        | {
                                        |   "data.newField": "field",
                                        |   "data.simpleArray": "object.objectFieldArray",
                                        |   "newArray[]": {
                                        |     "source": "object.objectFieldObjectArray[]",
                                        |     "mappings": {
                                        |       "mapTo": "subObjectName",
                                        |       "mapcopy": "subObjectName",
                                        |       "anotherProperty": "subObjectName2"
                                        |     }
                                        |   }
                                        | }
                                      """.stripMargin).as[JsObject]

      val resultJson: JsResult[JsObject] = simpleJson.transform(JsonDataTransformer.mappingTransformer(transformation))

      (resultJson.get \ "data" \ "newField").as[String] must equalTo("value")
      (resultJson.get \ "data" \ "simpleArray").as[List[String]] must contain("objectFieldArray3")
      ((resultJson.get \ "newArray")(0) \ "mapTo").as[String] must equalTo("subObject1")
      ((resultJson.get \ "newArray")(1) \ "anotherProperty").as[String] must equalTo("subObject2-2")
    }

    "silently ignore missing fields" in new JsonDataTransformerContext {
      val transformation: JsObject = Json.parse("""
                                        | {
                                        |   "data.newField": "field",
                                        |   "data.otherField": "missingField",
                                        |   "data.onemore": "object.objectFieldArray[4]"
                                        | }
                                      """.stripMargin).as[JsObject]

      val resultJson: JsResult[JsObject] = simpleJson.transform(JsonDataTransformer.mappingTransformer(transformation))

      (resultJson.get \ "data" \ "newField").as[String] must equalTo("value")
      (resultJson.get \ "data" \ "otherField").toOption must beNone
    }

    "silently ignore missing arrays" in new JsonDataTransformerContext {
      val transformation: JsObject = Json.parse("""
                                        | {
                                        |   "data.newField": "field",
                                        |   "newArray[]": {
                                        |     "source": "object.missingArray[]",
                                        |     "mappings": {
                                        |       "mapTo": "subObjectName"
                                        |     }
                                        |   }
                                        | }
                                      """.stripMargin).as[JsObject]

      val resultJson: JsResult[JsObject] = simpleJson.transform(JsonDataTransformer.mappingTransformer(transformation))

      (resultJson.get \ "data" \ "newField").as[String] must equalTo("value")
      (resultJson.get \ "newArray").toOption must beNone
    }

    "return an error for an invalid mapping" in new JsonDataTransformerContext {
      val transformation: JsObject = Json.parse("""
                                        | {
                                        |   "data.newField": true
                                        | }
                                      """.stripMargin).as[JsObject]

      val resultJson: JsResult[JsObject] = simpleJson.transform(JsonDataTransformer.mappingTransformer(transformation))

      resultJson must beAnInstanceOf[JsError]
    }
  }

}

trait JsonDataTransformerContext extends Scope {
  private val simpleJsonString =
    """
      | {
      |   "field": "value",
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
    """.stripMargin

  val simpleJson: JsValue = Json.parse(simpleJsonString)
}