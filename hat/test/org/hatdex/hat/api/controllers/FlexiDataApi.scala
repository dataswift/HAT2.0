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
import play.api.libs.json.{ Json, _ }
import play.api.test.PlaySpecification
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

class FlexiDataApi(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito {

  val logger = Logger(this.getClass)

  sequential

  def generateCopyFrom(from: String) = {
    val fromPathNodes: List[PathNode] = from.split('.').map { node =>
      logger.info(s"Path node: ${node}")
      KeyPathNode(node)
    }.toList
    val fromPath = JsPath(fromPathNodes)

    fromPath
  }

  def generateSourceReads(node: String, path: JsPath): Reads[JsValue] = {
    val nodesLeft = node.split('.')
    val remainingPath = nodesLeft.tail.mkString(".")
    nodesLeft.headOption map {
      case currentNode if currentNode.endsWith("[]") =>
        path.json.pick.map {
          case elementList: JsArray => Json.toJson {
            elementList.value
              .map {
                element => element.transform(generateSourceReads(remainingPath, path))
              }
              .collect {
                case JsSuccess(decoded, _) => decoded
              }
          }
        }
      case currentNode => generateSourceReads(remainingPath, path \ currentNode)
    } getOrElse {
      path.json.pick
    }


//    (__ \ "object" \ "objectFieldObjectArray").json.pick
//      .map {
//        case v: JsArray =>
//          val elements = v.value.map(_.transform(generateDataPicker("subObjectName", "")))
//            .collect {
//              case s: JsSuccess[JsValue] => s.get
//            }
//          Json.toJson(elements)
//      }
  }

  def generateDataPicker(from: String, to: String): Reads[JsObject] = {
    val fromPath = generateCopyFrom(from)

    val toPathNodes = to.split('.').map { node =>
      //      case node if node.endsWith("[]") =>
      //      case node => KeyPathNode(node)
      KeyPathNode(node)
    }.toList
    val toPath = JsPath(toPathNodes)

    logger.info(s"From Path: $fromPath")
    logger.info(s"To Path: $toPath")

    toPath.json.copyFrom(fromPath.json.pick)
  }

  "JSON mappers" should {
    "remap from simple fields to flat json" in new FlexiDataApiContext {
      val temp = generateCopyFrom("object.array").json.copyFrom{
        generateSourceReads("")
      }

      val transform = {
        generateDataPicker("field", "data.newField") and
          generateDataPicker("anotherField", "data.newField") and
          generateDataPicker("object.objectFieldArray", "data.arrayField") and
          temp
      }

      val resultJson = simpleJson.transform(transform reduce)

      logger.info(s"JSON transformation: $resultJson")
      logger.info(s"Transformed JSON: ${Json.prettyPrint(resultJson.get)}")

      resultJson.get.toString must equalTo("what")

      true must equalTo(false)
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
      |       {"subObjectName": "subObject1"},
      |       {"subObjectName": "subObject2"}
      |     ]
      |   }
      | }
    """.stripMargin

  val simpleJson: JsValue = Json.parse(simpleJsonString)
}