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
 * 4 / 2017
 */

package org.hatdex.hat.api.service

import java.security.MessageDigest
import java.util.UUID

import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads._
import play.api.libs.json.{ JsArray, JsPath, JsValue, Json, Reads, _ }
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.SlickPostgresDriver.api._
import org.hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.concurrent.Future

case class EndpointData(
    endpoint: String,
    recordId: Option[UUID],
    data: JsValue,
    links: Option[Seq[EndpointData]])

case class EndpointQuery(
    endpoint: String,
    mapping: JsValue,
    links: Option[Seq[EndpointQuery]]) {
  def originalField(field: String): List[String] = {
    (mapping \ field)
      .toOption
      .map(_.as[String].split('.').toList)
      .getOrElse(List())
  }
}

case class PropertyQuery(
  endpoints: List[EndpointQuery],
  orderBy: String,
  limit: Int)

object FlexiDataMapping {
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
      .reduceLeft((reads, addedReads) => reads and addedReads reduce)
  }
}

class FlexiDataObject extends DalExecutionContext {

  val logger = Logger(this.getClass)

  private def dbDataRow(endpoint: String, userId: UUID, data: JsValue): DataJsonRow = {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(data.toString.getBytes)
    DataJsonRow(UUID.randomUUID(), endpoint, userId, LocalDateTime.now(), data, digest)
  }

  def saveData(userId: UUID, endpointData: List[EndpointData])(implicit db: Database): Future[Seq[EndpointData]] = {
    val queries = endpointData map { endpointDataGroup =>
      val endpointRow = dbDataRow(endpointDataGroup.endpoint, userId, endpointDataGroup.data)

      val linkedRows = endpointDataGroup.links
        .map(_.toList).getOrElse(List())
        .map(i => dbDataRow(i.endpoint, userId, i.data))

      val recordIds = endpointRow.recordId :: linkedRows.map(_.recordId)

      val (groupRow, groupRecordRows) = if (recordIds.length > 1) {
        val groupRow = DataJsonGroupsRow(UUID.randomUUID(), userId, LocalDateTime.now())
        val groupRecordRows = recordIds.map(DataJsonGroupRecordsRow(groupRow.groupId, _))
        (Seq(groupRow), groupRecordRows)
      }
      else {
        (Seq(), Seq())
      }

      for {
        endpointData <- (DataJson returning DataJson) += endpointRow
        linkedData <- (DataJson returning DataJson) ++= linkedRows
        group <- DataJsonGroups ++= groupRow
        groupRecords <- DataJsonGroupRecords ++= groupRecordRows
      } yield (endpointData, linkedData, group, groupRecords)
    }

    db.run(DBIO.sequence(queries).transactionally)
      .map {
        _.map { inserted =>
          ModelTranslation.fromDbModel(inserted._1, inserted._2)
        }
      }
  }

  def saveRecordGroup(userId: UUID, endpointData: List[EndpointData])(implicit db: Database): Future[UUID] = {
    // TODO: method to mark a group of records as belonging to the same "group"
    val group = endpointData.flatMap(_.recordId)
    val groupId = UUID.randomUUID()
    Future.failed(new RuntimeException("Not implemented"))
  }

  private def propertyDataQuery(endpointQueries: List[EndpointQuery], orderBy: String, limit: Int): (Query[(DataJson, ConstColumn[Int]), (DataJsonRow, Int), Seq], List[Reads[JsObject]]) = {
    val queriesWithMappers = endpointQueries.zipWithIndex map {
      case (endpointQuery, index) =>
        val transformer = FlexiDataMapping.mappingTransformer(endpointQuery.mapping.as[JsObject])
        val query = for {
          data <- DataJson.filter(_.source === endpointQuery.endpoint)
        } yield (data, data.data #> endpointQuery.originalField(orderBy), index)

        (query, transformer)
    }

    val queryUnion = queriesWithMappers.unzip._1.reduce { (aggregate, query) =>
      aggregate.unionAll(query)
    }

    val resultQuery = queryUnion.sortBy(_._2)
      .take(limit)
      .map(r => (r._1, r._3))

    (resultQuery, queriesWithMappers.unzip._2)
  }

  def propertyData(endpointQueries: List[EndpointQuery], orderBy: String, limit: Int)(implicit db: Database): Future[Seq[EndpointData]] = {
    val queryWithMappers = propertyDataQuery(endpointQueries, orderBy, limit)
    val mappers = queryWithMappers._2
    db.run(queryWithMappers._1.result).map { results =>
      results map {
        case (data, mapperIndex) =>
          EndpointData(
            data.source,
            Some(data.recordId),
            data.data.transform(mappers(mapperIndex)).getOrElse(Json.obj()),
            None)
      }
    }
  }

  def bundleData(bundle: Map[String, PropertyQuery])(implicit db: Database): Future[Map[String, Seq[EndpointData]]] = {
    val results = bundle map {
      case (property, propertyQuery) =>
        propertyData(propertyQuery.endpoints, propertyQuery.orderBy, propertyQuery.limit)
          .map(property -> _)
    }

    Future.fold(results)(Map[String, Seq[EndpointData]]()) { (propertyMap, response) =>
      propertyMap + response
    }
  }
}
