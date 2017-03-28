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

  "JSON mappers" should {
    "remap from simple fields to flat json" in new FlexiDataApiContext {
      val transform = {
        val jsPath = __ \ 'data \ 'newField
        val jsPathFrom = __ \ 'field

        (__ \ 'data \ 'newField).json.copyFrom((__ \ 'field).json.pick) and // copy record id into the notable data
          (__ \ 'data \ 'newField).json.copyFrom((__ \ 'anotherField).json.pick) and
          (__ \ 'data \ 'arrayField).json.copyFrom((__ \ 'object \ 'objectFieldArray).json.pick) reduce //and
        //          (__ \ 'data \ 'subObjectArray).json.copyFrom((__ \ 'object \ 'objectFieldObjectArray(0) \ 'subObjectName).json.pick) reduce
      }

      val resultJson = simpleJson.transform(transform)

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