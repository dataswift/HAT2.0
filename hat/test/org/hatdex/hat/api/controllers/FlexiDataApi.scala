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
import play.api.data.validation.ValidationError
import play.api.libs.json.{ JsValue, Json, _ }
import play.api.test.PlaySpecification
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

class FlexiDataApi(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito {

  val logger = Logger(this.getClass)

  sequential

  def parseJsPath(from: String): JsPath = {
    val fromPathNodes: List[PathNode] = from.split('.').map { node =>
      KeyPathNode(node)
    }.toList
    JsPath(fromPathNodes)
  }

  def nestedDataPicker(destination: String, source: JsValue): Reads[JsObject] = {
    source match {
      case simpleSource: JsString =>
        parseJsPath(destination).json.copyFrom(parseJsPath(simpleSource.value).json.pick)

      case source: JsObject =>
        val nestedMappingPrefix = (source \ "source").get.as[JsString]
        val sourceJson = parseJsPath(nestedMappingPrefix.value.stripSuffix("[]")).json

        val transformation = (source \ "mappings").get.as[JsObject].fields.map {
          case (subDestination, subSource) =>
            nestedDataPicker(subDestination, subSource)
        } reduceLeft { (reads, addedReads) => reads and addedReads reduce }

        val transformed = if (destination.endsWith("[]")) {
          sourceJson.pick[JsArray].map(arr => JsArray(arr.value.map(_.transform(transformation).get)))
        }
        else {
          sourceJson.pick.map(_.transform(transformation).get)
        }

        parseJsPath(destination.stripSuffix("[]")).json.copyFrom(transformed)
      case _ =>
        Reads[JsObject](json => JsError("Invalid mapping template - mappings can only be simple strings or well-structured objects"))
    }
  }

  def mappingTransformer(mapping: JsObject): Reads[JsObject] = {
    mapping.fields
      .map(f => nestedDataPicker(f._1, f._2))
      .reduceLeft((reads, addedReads) => reads and addedReads reduce)
  }

  "JSON mappers" should {
    "remap from simple fields to flat json" in new FlexiDataApiContext {
      val transformation = Json.parse("""
                                        | {
                                        |   "data.newField": "field",
                                        |   "data.newField": "anotherField",
                                        |   "data.arrayField": "object.objectFieldArray"
                                        | }
                                      """.stripMargin).as[JsObject]

      val transform = mappingTransformer(transformation)
      val resultJson = simpleJson.transform(transform)

      logger.info(s"JSON transformation: $resultJson")
      logger.info(s"Transformed JSON: ${Json.prettyPrint(resultJson.get)}")

      resultJson.get.toString must equalTo("what")

      true must equalTo(false)
    }

    "remap array objects" in new FlexiDataApiContext {
      val transformation = Json.parse("""
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

      val transform = mappingTransformer(transformation)
      val resultJson = simpleJson.transform(transform)

      logger.info(s"JSON transformation: $resultJson")
      logger.info(s"Transformed JSON: ${Json.prettyPrint(resultJson.get)}")

      resultJson.get.toString must equalTo("what")
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