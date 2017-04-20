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

import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.SlickPostgresDriver.api._
import org.hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import play.api.Logger
import play.api.libs.json._

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

class RichDataService extends DalExecutionContext {

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

  def saveRecordGroup(userId: UUID, recordIds: Seq[UUID])(implicit db: Database): Future[UUID] = {
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

  private def queryMappers(endpointQueries: Seq[EndpointQuery]): HashMap[String, Reads[JsObject]] = {
    val mappers = endpointQueries.zipWithIndex flatMap {
      case (endpointQuery, index) =>
        val id = index.toString
        val transformer = endpointQuery.mapping collect {
          case m: JsObject => id -> JsonDataTransformer.mappingTransformer(m)
        }
        val subTransformers = endpointQuery.links.getOrElse(Seq()).zipWithIndex.map {
          case (link, subIndex) =>
            link.mapping collect {
              case m: JsObject => s"$id-$subIndex" -> JsonDataTransformer.mappingTransformer(m)
            }
        }
        (subTransformers :+ transformer).flatten
    }
    HashMap(mappers: _*)
  }

  private def propertyDataQuery(endpointQueries: Seq[EndpointQuery], orderBy: String, limit: Int): Query[((DataJson, ConstColumn[String]), Rep[Option[(DataJson, ConstColumn[String])]]), ((DataJsonRow, String), Option[(DataJsonRow, String)]), Seq] = {
    val queriesWithMappers = endpointQueries.zipWithIndex map {
      case (endpointQuery, endpointQueryIndex) =>
        for {
          data <- DataJson.filter(_.source === endpointQuery.endpoint)
        } yield (data, data.data #> endpointQuery.originalField(orderBy), endpointQueryIndex.toString)
    }

    val endpointDataQuery = queriesWithMappers
      .reduce((aggregate, query) => aggregate.unionAll(query)) // merge all the queries together
      .sortBy(_._2) // order all the results by the chosen column
      .take(limit) // take the desired number of records

    val linkedRecordQueries = endpointQueries.zipWithIndex map {
      case (endpointQuery, endpointQueryIndex) =>
        endpointQuery.links map { links =>
          links.zipWithIndex map {
            case (link, linkIndex) =>
              for {
                endpointQueryRecordGroup <- DataJsonGroupRecords
                (linkedGroupId, linkedRecordId) <- DataJsonGroupRecords.map(v => (v.groupId, v.recordId)) if endpointQueryRecordGroup.groupId === linkedGroupId && endpointQueryRecordGroup.recordId =!= linkedRecordId
                linkedRecord <- DataJson.filter(_.source === link.endpoint).filter(_.recordId === linkedRecordId)
              } yield (endpointQueryIndex.toString, s"$endpointQueryIndex-$linkIndex", endpointQueryRecordGroup.recordId, linkedRecord)
          }
        } getOrElse {
          Seq(for {
            noGroup <- DataJsonGroupRecords.take(0)
            noLinkedRecord <- DataJson.take(0)
          } yield (endpointQueryIndex.toString, s"$endpointQueryIndex-", noGroup.recordId, noLinkedRecord))
        }
    }

    val groupRecords = linkedRecordQueries.flatten
      .reduce((aggregate, query) => aggregate.unionAll(query))

    val resultQuery = endpointDataQuery
      .joinLeft(groupRecords)
      .on((l, r) => l._1.recordId === r._3 && l._3.asColumnOf[String] === r._1.asColumnOf[String])
      .sortBy(_._1._2)
      .map(v => ((v._1._1, v._1._3), v._2.map(lr => (lr._4, lr._2))))

    resultQuery
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
    val query = propertyDataQuery(endpointQueries, orderBy, limit)
    val mappers = queryMappers(endpointQueries)
    db.run(query.result).map { results =>
      groupRecords[(DataJsonRow, String), (DataJsonRow, String)](results).map {
        case ((record, queryId), linkedResults) =>
          val linked = linkedResults.map {
            case (linkedRecord, linkedQueryId) =>
              endpointDataWithMappers(linkedRecord, linkedQueryId, mappers)
          }
          val endpointData = endpointDataWithMappers(record, queryId, mappers)
          if (linked.nonEmpty) {
            endpointData.copy(links = Some(linked))
          }
          else {
            endpointData
          }
      }
    }
  }

  private def endpointDataWithMappers(record: DataJsonRow, queryId: String, mappers: HashMap[String, Reads[JsObject]]): EndpointData = {
    EndpointData(
      record.source,
      Some(record.recordId),
      mappers.get(queryId)
        .map(record.data.transform) // apply JSON transformation if it is present
        .map(_.getOrElse(Json.obj())) // quietly empty object of transformation fails
        .getOrElse(record.data), // if no mapper, return data as-is
      None)
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
