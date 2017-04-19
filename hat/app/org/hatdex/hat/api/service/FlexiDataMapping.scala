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

import scala.annotation.tailrec
import scala.collection.immutable.HashMap
import scala.concurrent.Future

case class EndpointData(
  endpoint: String,
  recordId: Option[UUID],
  data: JsValue,
  links: Option[Seq[EndpointData]])

case class EndpointQuery(
    endpoint: String,
    mapping: Option[JsValue],
    links: Option[Seq[EndpointQuery]]) {
  def originalField(field: String): Option[List[String]] = {
    mapping.flatMap { m =>
      (m \ field)
        .toOption
        .map(_.as[String].split('.').toList)
    }
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

  def saveRecordGroup(userId: UUID, recordIds: List[UUID])(implicit db: Database): Future[UUID] = {
    val groupRow = DataJsonGroupsRow(UUID.randomUUID(), userId, LocalDateTime.now())
    val groupRecordRows = recordIds.map(DataJsonGroupRecordsRow(groupRow.groupId, _))

    val query = for {
      group <- DataJsonGroups += groupRow
      groupRecords <- DataJsonGroupRecords ++= groupRecordRows
    } yield (group, groupRecords)

    db.run(query.transactionally)
      .map { _ =>
        groupRow.groupId
      }
  }

  private def propertyDataQuery(endpointQueries: Seq[EndpointQuery], orderBy: String, limit: Int): (Query[((DataJson, ConstColumn[String]), Rep[Option[DataJson]]), ((DataJsonRow, String), Option[DataJsonRow]), Seq], HashMap[String, Reads[JsObject]]) = {
    val mappers = endpointQueries.zipWithIndex flatMap {
      case (endpointQuery, index) =>
        val id = index.toString
        val transformer = endpointQuery.mapping collect {
          case m: JsObject => FlexiDataMapping.mappingTransformer(m)
        }
        val subTransformers = endpointQuery.links.getOrElse(Seq()).zipWithIndex.map {
          case (link, subIndex) =>
            val transformer = link.mapping collect {
              case m: JsObject => FlexiDataMapping.mappingTransformer(m)
            }
            (s"$id-$subIndex", transformer)
        }

        id -> transformer :: subTransformers.toList
    } collect {
      case (id, Some(transformer)) => id -> transformer
    }

    val queriesWithMappers = endpointQueries.zipWithIndex map {
      case (endpointQuery, index) =>
        for {
          data <- DataJson.filter(_.source === endpointQuery.endpoint)
        } yield (data, data.data #> endpointQuery.originalField(orderBy), index.toString)
    }

    val queryUnion = queriesWithMappers.reduce((aggregate, query) => aggregate.unionAll(query))

    val endpointDataQuery = queryUnion.sortBy(_._2)
      .take(limit)
      .map(r => (r._1, r._3))

    val linkedRecordQueries = endpointQueries.zipWithIndex flatMap {
      case (endpointQuery, index) =>
        endpointQuery.links map { links =>
          links.zipWithIndex map {
            case (link, linkIndex) =>
              for {
                group <- DataJsonGroupRecords
                groupLinks <- DataJsonGroupRecords.map(v => (v.groupId, v.recordId)) if group.groupId === groupLinks._1 && group.recordId =!= groupLinks._2
                linkedRecords <- DataJson.filter(_.source === link.endpoint).filter(_.recordId === groupLinks._2)
              } yield (index.toString, s"$index-$linkIndex", group.recordId, linkedRecords)
          }
        }
    }

    val maybeGroupRecords = linkedRecordQueries.flatten
      .reduceLeftOption((aggregate, query) => aggregate.unionAll(query))

    val resultQuery = maybeGroupRecords map { groupRecords =>
      for {
        ((endpointData, group), linkedRecord) <- endpointDataQuery.joinLeft(groupRecords)
          .on((l, r) => l._1.recordId === r._3)
      } yield ((endpointData, group), linkedRecord.map(_._4))
    } getOrElse {
      for {
        ((endpointData, group), linkedRecord) <- endpointDataQuery.joinLeft(DataJson.take(0))
      } yield ((endpointData, group), linkedRecord)
    }

    (resultQuery, HashMap(mappers: _*))
  }

  implicit def equalDataJsonRowIdentity(a: (DataJsonRow, String), b: (DataJsonRow, String)): Boolean = {
    a._1.recordId == b._1.recordId
  }

  @tailrec
  private def groupRecords[T, U](list: Seq[(T, Option[U])], groups: Seq[(T, Seq[U])] = Seq())(implicit equalIdentity: ((T, T) => Boolean)): Seq[(T, Seq[U])] = {
    if (list.isEmpty) {
      groups
    }
    else {
      groupRecords(
        list.dropWhile(v => equalIdentity(v._1, list.head._1)),
        groups :+ ((list.head._1, list.takeWhile(v => equalIdentity(v._1, list.head._1)).unzip._2.flatten)))
    }
  }

  def propertyData(endpointQueries: List[EndpointQuery], orderBy: String, limit: Int)(implicit db: Database): Future[Seq[EndpointData]] = {
    val queryWithMappers = propertyDataQuery(endpointQueries, orderBy, limit)
    val mappers = queryWithMappers._2
    db.run(queryWithMappers._1.result).map { results =>
      groupRecords[(DataJsonRow, String), DataJsonRow](results).map {
        case ((record, queryId), linkedResults) =>
          EndpointData(
            record.source,
            Some(record.recordId),
            mappers.get(queryId)
              .map(record.data.transform) // apply JSON transformation if it is present
              .map(_.getOrElse(Json.obj())) // quietly empty object of transformation fails
              .getOrElse(record.data), // if no mapper, return data as-is
            Some(linkedResults.map(ModelTranslation.fromDbModel)))
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
