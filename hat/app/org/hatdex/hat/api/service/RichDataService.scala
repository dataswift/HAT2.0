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

import com.github.tminglei.slickpg.TsVector
import org.hatdex.hat.api.models._
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.SlickPostgresDriver.api.{ Database, _ }
import org.hatdex.hat.dal.Tables._
import org.joda.time.{ DateTime, LocalDateTime }
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.libs.json._

import scala.annotation.tailrec
import scala.collection.immutable.HashMap
import scala.concurrent.Future

trait FieldTransformable[In] {
  type Out
  def apply(in: In): Out
}

object FieldTransformable {
  import FieldTransformation._
  type Aux[I, O] = FieldTransformable[I] { type Out = O }

  implicit val generateIdentityTranslation: Aux[Identity, Rep[JsValue] => Rep[JsValue]] =
    new FieldTransformable[Identity] {
      type Out = Rep[JsValue] => Rep[JsValue]

      def apply(in: Identity): Rep[JsValue] => Rep[JsValue] = {
        value: Rep[JsValue] => value
      }
    }

  implicit val generateDateTimeExtractTranslation: Aux[DateTimeExtract, Rep[JsValue] => Rep[JsValue]] =
    new FieldTransformable[DateTimeExtract] {
      type Out = Rep[JsValue] => Rep[JsValue]

      def apply(in: DateTimeExtract): Rep[JsValue] => Rep[JsValue] = {
        value => toJson(datePart(in.part, value.asColumnOf[String].asColumnOf[DateTime]))
      }
    }

  implicit val generateTimestampExtractTranslation: Aux[TimestampExtract, Rep[JsValue] => Rep[JsValue]] =
    new FieldTransformable[TimestampExtract] {
      type Out = Rep[JsValue] => Rep[JsValue]

      def apply(in: TimestampExtract): Rep[JsValue] => Rep[JsValue] = {
        value => toJson(datePartTimestamp(in.part, toTimestamp(value.asColumnOf[Double])))
      }
    }

  implicit val generateSearchableTranslation: Aux[Searchable, Rep[JsValue] => Rep[TsVector]] =
    new FieldTransformable[Searchable] {
      type Out = Rep[JsValue] => Rep[TsVector]

      def apply(in: Searchable): Rep[JsValue] => Rep[TsVector] = {
        value => toTsVector(value.asColumnOf[String])
      }
    }

  def process[I](in: I)(implicit p: FieldTransformable[I]): p.Out = p(in)
}

class RichDataServiceException(message: String = "", cause: Throwable = None.orNull) extends Exception(message, cause)
case class RichDataDuplicateException(message: String = "", cause: Throwable = None.orNull) extends RichDataServiceException(message, cause)
case class RichDataMissingException(message: String = "", cause: Throwable = None.orNull) extends RichDataServiceException(message, cause)

class RichDataService extends DalExecutionContext {

  val logger = Logger(this.getClass)

  private def dbDataRow(endpoint: String, userId: UUID, data: JsValue, recordId: Option[UUID] = None): DataJsonRow = {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(data.toString.getBytes)
    DataJsonRow(recordId.getOrElse(UUID.randomUUID()), endpoint, userId, LocalDateTime.now(), data, digest)
  }

  def saveData(userId: UUID, endpointData: Seq[EndpointData])(implicit db: Database): Future[Seq[EndpointData]] = {
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
      } recover {
        case e: PSQLException if e.getMessage.startsWith("ERROR: duplicate key value violates unique constraint \"data_json_hash_key\"") =>
          throw RichDataDuplicateException("Duplicate data", e)
        case e: PSQLException =>
          throw RichDataDuplicateException("Unexpected issue with inserting data", e)
        case e =>
          logger.error(s"Error inserting data: ${e.getMessage}")
          throw e
      }
  }

  def deleteRecords(userId: UUID, recordIds: Seq[UUID])(implicit db: Database): Future[Unit] = {
    val query = for {
      deletedGroupRecords <- DataJsonGroupRecords.filter(_.recordId inSet recordIds).delete // delete links between records and groups
      deletedGroups <- DataJsonGroups.filterNot(g => (g.owner === userId) && (g.groupId in DataJsonGroupRecords.map(_.groupId))).delete // delete any groups that have become empty
      deletedRecords <- DataJson.filter(r => (r.owner === userId) && (r.recordId inSet recordIds)).delete if deletedRecords == recordIds.length // delete the records, but only if all requested records are found
    } yield (deletedGroupRecords, deletedGroups, deletedRecords)

    db.run(query.transactionally).map(_ => ()) recover {
      case e: NoSuchElementException => throw RichDataMissingException("Records missing for deleting")
    }
  }

  def updateRecords(userId: UUID, records: Seq[EndpointData])(implicit db: Database): Future[Seq[EndpointData]] = {
    val updateRows = records.map { record =>
      dbDataRow(record.endpoint, userId, record.data, record.recordId)
    }

    val updateQueries = updateRows map { record =>
      for {
        updated <- DataJson.filter(r => r.recordId === record.recordId && r.owner === userId)
          .map(r => (r.data, r.date, r.hash))
          .update((record.data, record.date, record.hash)) if updated == 1
      } yield updated
    }

    db.run(DBIO.sequence(updateQueries).transactionally).map { _ =>
      updateRows.map(ModelTranslation.fromDbModel)
    } recover {
      case e: NoSuchElementException => throw RichDataMissingException("Records missing for updating")
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

  protected[service] def generatedDataQuery(endpointQuery: EndpointQuery, query: Query[DataJson, DataJsonRow, Seq]): Query[DataJson, DataJsonRow, Seq] = {
    val q = query.filter(_.source === endpointQuery.endpoint)
    endpointQuery.filters map { filters =>
      generateDataQueryFiltered(filters, q)
    } getOrElse {
      q
    }
  }

  private def generateDataQueryFiltered(filters: Seq[EndpointQueryFilter], query: Query[DataJson, DataJsonRow, Seq]): Query[DataJson, DataJsonRow, Seq] = {
    if (filters.isEmpty) {
      query
    }
    else {
      val currentQuery = processQueryFilter(filters.head, query)
      generateDataQueryFiltered(filters.tail, currentQuery)
    }
  }

  def processQueryFilter(filter: EndpointQueryFilter, query: Query[DataJson, DataJsonRow, Seq]): Query[DataJson, DataJsonRow, Seq] = {
    import FieldTransformation._
    import FilterOperator._

    val currentTransformation = filter.transformation.getOrElse(FieldTransformation.Identity())
    filter.operator match {
      case Contains(value) =>
        currentTransformation match {
          case t: Identity =>
            def f: Rep[JsValue] => Rep[JsValue] = FieldTransformable.process(t)
            query.filter(d => f(d.data #> filter.originalField) @> value)
          case t: DateTimeExtract =>
            def f: Rep[JsValue] => Rep[JsValue] = FieldTransformable.process(t)
            query.filter(d => f(d.data #> filter.originalField) @> value)
          case t: TimestampExtract =>
            def f: Rep[JsValue] => Rep[JsValue] = FieldTransformable.process(t)
            query.filter(d => f(d.data #> filter.originalField) @> value)
          case _ => query
        }

      case In(value) =>
        currentTransformation match {
          case t: Identity =>
            def f: Rep[JsValue] => Rep[JsValue] = FieldTransformable.process(t)
            query.filter(d => value <@: f(d.data #> filter.originalField))
          case t: DateTimeExtract =>
            def f: Rep[JsValue] => Rep[JsValue] = FieldTransformable.process(t)
            query.filter(d => value <@: f(d.data #> filter.originalField))
          case t: TimestampExtract =>
            def f: Rep[JsValue] => Rep[JsValue] = FieldTransformable.process(t)
            query.filter(d => value <@: f(d.data #> filter.originalField))
          case _ => query
        }

      case Between(lower, upper) =>
        currentTransformation match {
          case t: Identity =>
            def f: Rep[JsValue] => Rep[JsValue] = FieldTransformable.process(t)
            query.filter(d => f(d.data #> filter.originalField) between (lower, upper))
          case t: DateTimeExtract =>
            def f: Rep[JsValue] => Rep[JsValue] = FieldTransformable.process(t)
            query.filter(d => f(d.data #> filter.originalField) between (lower, upper))
          case t: TimestampExtract =>
            def f: Rep[JsValue] => Rep[JsValue] = FieldTransformable.process(t)
            query.filter(d => f(d.data #> filter.originalField) between (lower, upper))
          case _ => query
        }

      case Find(searchTerm) =>
        currentTransformation match {
          case t: Searchable =>
            val f = FieldTransformable.process(t)
            query.filter(d => f(d.data #> filter.originalField) @@ plainToTsQuery(searchTerm.asColumnOf[String]))
          case _ => query
        }
    }
  }

  private def propertyDataQuery(endpointQueries: Seq[EndpointQuery], orderBy: Option[String], limit: Int): Query[((DataJson, ConstColumn[String]), Rep[Option[(DataJson, ConstColumn[String])]]), ((DataJsonRow, String), Option[(DataJsonRow, String)]), Seq] = {
    val queriesWithMappers = //: Seq[Query[(DataJson, Rep[Option[JsValue]], ConstColumn[String]), (DataJsonRow, Option[JsValue], String), Seq]] =
      endpointQueries.zipWithIndex map {
        case (endpointQuery, endpointQueryIndex) =>
          orderBy map { orderBy =>
            for {
              data <- generatedDataQuery(endpointQuery, DataJson)
            } yield (data, data.data #> endpointQuery.originalField(orderBy), endpointQueryIndex.toString)
          } getOrElse {
            for {
              data <- generatedDataQuery(endpointQuery, DataJson)
            } yield (data, toJson(data.date.asColumnOf[String]).asColumnOf[Option[JsValue]], endpointQueryIndex.toString)
          }
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
                linkedRecord <- generatedDataQuery(link, DataJson.filter(_.recordId === linkedRecordId))
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

  def propertyData(endpointQueries: List[EndpointQuery], orderBy: Option[String], limit: Int)(implicit db: Database): Future[Seq[EndpointData]] = {
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

  def bundleData(bundle: EndpointDataBundle)(implicit db: Database): Future[Map[String, Seq[EndpointData]]] = {
    val results = bundle.bundle map {
      case (property, propertyQuery) =>
        propertyData(propertyQuery.endpoints, propertyQuery.orderBy, propertyQuery.limit)
          .map(property -> _)
    }

    Future.fold(results)(Map[String, Seq[EndpointData]]()) { (propertyMap, response) =>
      propertyMap + response
    }
  }
}

case class EndpointDataBundle(
    name: String,
    bundle: Map[String, PropertyQuery]) {
  def flatEndpointQueries: Seq[EndpointQuery] = {
    bundle.flatMap {
      case (k, v) =>
        v.endpoints.flatMap(endpointQueries)
    } toSeq
  }

  def endpointQueries(endpointQuery: EndpointQuery): Seq[EndpointQuery] = {
    endpointQuery.links
      .map(_.flatMap(endpointQueries))
      .getOrElse(Seq()) :+ endpointQuery
  }
}
