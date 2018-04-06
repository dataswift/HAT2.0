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

import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads._
import play.api.libs.json.{ JsArray, JsPath, JsValue, Json, Reads, _ }

object JsonDataTransformer {
  val logger = Logger(this.getClass)

  // How array elements could be accessed by index
  private val arrayAccessPattern = "(\\w+)(\\[([0-9]+)?\\])?".r

  def parseJsPath(from: String): JsPath = {
    val pathNodes = from.split('.').map { node =>
      // the (possibly) extracted array index is the 3rd item in the regex groups
      val arrayAccessPattern(nodeName, _, item) = node
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
        } reduceLeft { (reads, addedReads) => reads.and(addedReads).reduce }

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

  //  private def sumFieldsTogether(): Unit = {
  //    // TODO: example future code to potentially do more complex transformations based on operators
  //    (__ \ 'sum).json
  //      .copyFrom(
  //        ((__ \ 'key1).read[Int] and (__ \ 'key2).read[Int])
  //          .tupled
  //          .map { t => t.productIterator.reduce((acc: Int, b: Int) => acc + b) }
  //          .map(JsNumber(_)))
  //  }

  def mappingTransformer(mapping: JsObject): Reads[JsObject] = {
    mapping.fields
      .map(f => nestedDataPicker(f._1, f._2))
      .reduceLeft((reads, addedReads) => reads.and(addedReads).reduce)
  }
}
