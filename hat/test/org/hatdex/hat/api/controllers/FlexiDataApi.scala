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
 * 3 / 2017
 */

package org.hatdex.hat.api.controllers

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads._
import play.api.libs.json.{JsArray, JsPath, JsValue, Json, Reads, _}
import play.api.test.PlaySpecification

object FlexiDataApi {
  val logger = Logger(this.getClass)

  // How array elements could be accessed by index
  private val arrayaccessPattern = "(\\w+)(\\[([0-9]+)?\\])?".r

  def parseJsPath(from: String): JsPath = {
    val pathNodes = from.split('.').map { node =>
      // the (possibly) extracted array index is the 3rd item in the regex groups
      val arrayaccessPattern(nodeName, _, item) = node
      (nodeName, item) match {
        case (name, null)  => __ \ name
        case (name, index) => (__ \ name)(index.toInt)
      }
    }

    pathNodes.reduceLeft((path, node) => path.compose(node))
  }

  def nestedDataPicker(destination: String, source: JsValue): Reads[JsObject] = {
    source match {
      case simpleSource: JsString =>
        parseJsPath(destination).json
          .copyFrom(parseJsPath(simpleSource.value).json.pick)
          .orElse(Reads.pure(Json.obj())) // empty object (skipped) if nothing to copy from

      case source: JsObject =>
        val nestedMappingPrefix = (source \ "source").get.as[JsString]
        val sourceJson = parseJsPath(nestedMappingPrefix.value.stripSuffix("[]")).json

        val transformation = (source \ "mappings").get.as[JsObject].fields.map {
          case (subDestination, subSource) =>
            nestedDataPicker(subDestination, subSource)
        } reduceLeft { (reads, addedReads) => reads and addedReads reduce }

        val transformed = if (destination.endsWith("[]")) {
          sourceJson.pick[JsArray].map { arr =>
            JsArray(arr.value.flatMap(_.transform(transformation).map(Some(_)).getOrElse(None)))
          }
        }
        else {
          sourceJson.pick.map(_.transform(transformation).get)
        }

        parseJsPath(destination.stripSuffix("[]")).json
          .copyFrom(transformed)
          .orElse(Reads.pure(Json.obj()))

      case _ =>
        Reads[JsObject](_ => JsError("Invalid mapping template - mappings can only be simple strings or well-structured objects"))
    }
  }

  def mappingTransformer(mapping: JsObject): Reads[JsObject] = {
    mapping.fields
      .map(f => nestedDataPicker(f._1, f._2))
      .reduceLeft((reads, addedReads) => reads and addedReads reduce)
  }
}

class FlexiDataApiSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito {

  val logger = Logger(this.getClass)

  sequential

  "JSON mappers" should {
    "remap from simple fields to flat json" in new FlexiDataApiContext {
      val transformation: JsObject = Json.parse("""
                                        | {
                                        |   "data.newField": "field",
                                        |   "data.newField": "anotherField",
                                        |   "data.arrayField": "object.objectFieldArray",
                                        |   "data.onemore": "object.objectFieldArray[1]"
                                        | }
                                      """.stripMargin).as[JsObject]

      val resultJson: JsResult[JsObject] = simpleJson.transform(FlexiDataApi.mappingTransformer(transformation))

      (resultJson.get \ "data" \ "newField").as[String] must equalTo("anotherFieldValue")
      (resultJson.get \ "data" \ "arrayField").as[List[String]] must contain("objectFieldArray3")
      (resultJson.get \ "data" \ "onemore").as[String] must equalTo("objectFieldArray2")
    }

    "remap array objects" in new FlexiDataApiContext {
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

      val resultJson: JsResult[JsObject] = simpleJson.transform(FlexiDataApi.mappingTransformer(transformation))

      (resultJson.get \ "data" \ "newField").as[String] must equalTo("value")
      (resultJson.get \ "data" \ "simpleArray").as[List[String]] must contain("objectFieldArray3")
      ((resultJson.get \ "newArray")(0) \ "mapTo").as[String] must equalTo("subObject1")
      ((resultJson.get \ "newArray")(1) \ "anotherProperty").as[String] must equalTo("subObject2-2")
    }

    "silently ignore missing fields" in new FlexiDataApiContext {
      val transformation: JsObject = Json.parse("""
                                        | {
                                        |   "data.newField": "field",
                                        |   "data.otherField": "missingField",
                                        |   "data.onemore": "object.objectFieldArray[4]"
                                        | }
                                      """.stripMargin).as[JsObject]

      val resultJson: JsResult[JsObject] = simpleJson.transform(FlexiDataApi.mappingTransformer(transformation))

      (resultJson.get \ "data" \ "newField").as[String] must equalTo("value")
      (resultJson.get \ "data" \ "otherField").toOption must beNone
    }

    "silently ignore missing arrays" in new FlexiDataApiContext {
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

      val resultJson: JsResult[JsObject] = simpleJson.transform(FlexiDataApi.mappingTransformer(transformation))

      (resultJson.get \ "data" \ "newField").as[String] must equalTo("value")
      (resultJson.get \ "newArray").toOption must beNone
    }

    "return an error for an invalid mapping" in new FlexiDataApiContext {
      val transformation: JsObject = Json.parse("""
                                        | {
                                        |   "data.newField": true
                                        | }
                                      """.stripMargin).as[JsObject]

      val resultJson: JsResult[JsObject] = simpleJson.transform(FlexiDataApi.mappingTransformer(transformation))

      resultJson must beAnInstanceOf[JsError]
    }
  }

}

trait FlexiDataApiContext extends Scope {
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