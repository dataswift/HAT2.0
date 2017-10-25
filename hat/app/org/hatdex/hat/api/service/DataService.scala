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
 * 2 / 2017
 */
package org.hatdex.hat.api.service

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import org.hatdex.hat.api.models.{ ApiDataField, ApiDataRecord, ApiDataTable, ApiDataValue, _ }
import org.hatdex.hat.api.service.richData.RichDataService
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.Tables.{ DataTabletotablecrossref, _ }
import org.hatdex.hat.utils.FutureTransformations
import org.hatdex.libs.dal.HATPostgresProfile.api._
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success

// this trait defines our service behavior independently from the service actor
class DataService @Inject() (migrationService: MigrationService) extends DalExecutionContext {
  val logger = Logger(this.getClass)

  def getTableValues(
    tableId: Int,
    maybeLimit: Option[Int] = None,
    maybeStartTime: Option[LocalDateTime] = None,
    maybeEndTime: Option[LocalDateTime] = None)(implicit db: Database): Future[Seq[ApiDataRecord]] = {

    val startTime = maybeStartTime.getOrElse(LocalDateTime.now().minusDays(7))
    val endTime = maybeEndTime.getOrElse(LocalDateTime.now())
    val limit = maybeLimit.getOrElse(1000)

    // Get All Data Table trees matching source and name
    val dataTableTreesQuery = for {
      rootTable <- DataTableTree.filter(_.id === tableId)
      tree <- DataTableTree.filter(t => t.rootTable === rootTable.id && t.deleted === false)
    } yield tree

    val eventualTables = buildDataTreeStructures(dataTableTreesQuery, Set(tableId))

    val fieldsetQuery = dataTableTreesQuery.join(DataField).on(_.id === _.tableIdFk)
      .map(_._2) // Only fields
      .filter(_.deleted === false) // That have not been deleted
      .distinct // And are distinct
    val eventualValues = fieldsetValues(fieldsetQuery, startTime, endTime, limit)

    for {
      tables <- eventualTables
      values <- eventualValues
    } yield {
      if (tables.isEmpty) {
        throw new RuntimeException("No such table exists")
      }
      else {
        restructureTableValuesToRecords(values, tables).take(limit)
      }
    }
  }

  def createValue(value: ApiDataValue, maybeFieldId: Option[Int], maybeRecordId: Option[Int])(implicit db: Database): Future[ApiDataValue] = {
    // find field ID either from the function parameter or from within the value structure
    val someFieldId = maybeFieldId.orElse(value.field.flatMap(_.id))

    // find record ID either from the function parameter or from within the value structure
    val someRecordId = maybeRecordId.orElse(value.record.flatMap(_.id))

    val eventuallyInsertedValue = (someFieldId, someRecordId) match {
      case (Some(fieldId), Some(recordId)) =>
        val query = (DataValue returning DataValue) +=
          DataValueRow(0, LocalDateTime.now(), value.lastUpdated.getOrElse(LocalDateTime.now()),
            value.value, fieldId, recordId)
        db.run(query)
      case (None, _) => Future.failed(new IllegalArgumentException("Correct field must be set for value to be inserted into"))
      case (_, None) => Future.failed(new IllegalArgumentException("Correct record must be set for value to be inserted as part of"))
    }

    eventuallyInsertedValue map { inserted =>
      ModelTranslation.fromDbModel(inserted, value.field, value.record)
    }
  }

  protected def createFieldsAction(fields: Seq[ApiDataField], tableId: Int): DBIOAction[Seq[DataFieldRow], NoStream, Effect.Write] = {
    val t = LocalDateTime.now()
    (DataField returning DataField) ++= fields.map(f => DataFieldRow(0, t, t, f.name, tableId))
  }

  protected def linkTablesAction(subTables: Seq[DataTableRow], tableId: Int): DBIOAction[Seq[Int], NoStream, Effect.Write] = {
    val t = LocalDateTime.now()
    DataTabletotablecrossref returning DataTabletotablecrossref.map(_.id) ++=
      subTables.map(s => DataTabletotablecrossrefRow(1, t, t, "parent child", tableId, s.id))
  }

  protected def createTableAction(table: ApiDataTable): DBIOAction[DataTableRow, NoStream, Effect.Write] = {
    val t = LocalDateTime.now()
    val query = for {
      dbTable <- (DataTable returning DataTable) += DataTableRow(0, t, t, table.name, table.source)
      _ <- createFieldsAction(table.fields.getOrElse(Seq()), dbTable.id)
      subTables <- DBIO.sequence(table.subTables.getOrElse(Seq()).map(createTableAction))
      _ <- linkTablesAction(subTables, dbTable.id)
    } yield {
      dbTable
    }
    query
  }

  def createTable(table: ApiDataTable)(implicit db: Database): Future[ApiDataTable] = {
    for {
      created <- db.run(createTableAction(table).transactionally)
      table <- getTableStructure(created.id).map(_.get)
    } yield {
      table
    }
  }

  def linkTables(parentId: Int, childId: Int, relationship: ApiRelationship)(implicit db: Database): Future[Int] = {
    val dataTableToTableCrossRefRow = DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
      relationship.relationshipType, parentId, childId)
    db.run((DataTabletotablecrossref returning DataTabletotablecrossref.map(_.id)) += dataTableToTableCrossRefRow)
  }

  def createField(field: ApiDataField, maybeTableId: Option[Int] = None)(implicit db: Database): Future[ApiDataField] = {
    val eventuallyNewField = maybeTableId.orElse(field.tableId) map { tableId =>
      val query = (DataField returning DataField) += DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), field.name, tableId)
      db.run(query)
    } getOrElse {
      Future.failed(new IllegalArgumentException("Table ID for field being inserted must be set"))
    }

    eventuallyNewField map { field =>
      ModelTranslation.fromDbModel(field)
    }
  }

  def getFieldValues(fieldId: Int)(implicit db: Database): Future[Option[ApiDataField]] = {
    for {
      maybeField <- retrieveDataFieldId(fieldId)
      maybeValues <- maybeField.map { _ =>
        db.run {
          DataValue.filter(v => v.fieldId === fieldId && v.deleted === false)
            .sortBy(_.lastUpdated.desc)
            .result
        } map (v => Some(v))
      } getOrElse { Future.successful(None) }
    } yield {
      val apiDataValues = maybeValues.map { values =>
        values.map(ModelTranslation.fromDbModel)
      }
      maybeField.map(_.copy(values = apiDataValues))
    }
  }

  /*
   * Private function finding data field by ID
   */
  def retrieveDataFieldId(fieldId: Int)(implicit db: Database): Future[Option[ApiDataField]] = {
    val eventualField = db.run(DataField.filter(f => f.id === fieldId && f.deleted === false).take(1).result).map(_.headOption)
    eventualField.map(_.map(ModelTranslation.fromDbModel))
  }

  def createRecord(record: ApiDataRecord)(implicit db: Database): Future[ApiDataRecord] = {
    val newRecord = DataRecordRow(0, LocalDateTime.now(), record.lastUpdated.getOrElse(LocalDateTime.now()), record.name)
    db.run {
      (DataRecord returning DataRecord) += newRecord
    } map { record =>
      ModelTranslation.fromDbModel(record, None)
    }
  }

  def findTable(name: String, source: String)(implicit db: Database): Future[Seq[ApiDataTable]] = {
    db.run {
      DataTable.filter(t => t.sourceName === source && t.name === name && t.deleted === false).result
    } map { tables =>
      tables.map(ModelTranslation.fromDbModel(_, None, None))
    }
  }

  def findTablesLike(name: String, source: String)(implicit db: Database): Future[Seq[ApiDataTable]] = {
    db.run {
      DataTable.filter(t => t.sourceName === source && t.deleted === false).filter(_.name like "%" + name + "%").result
    } map { tables =>
      tables.map(ModelTranslation.fromDbModel(_, None, None))
    }
  }

  def findTablesNotLike(name: String, source: String)(implicit db: Database): Future[Seq[ApiDataTable]] = {
    db.run {
      DataTable.filter(t => t.sourceName === source && t.deleted === false).filterNot(_.name like "%" + name + "%").result
    } map { tables =>
      tables.map(ModelTranslation.fromDbModel(_, None, None))
    }
  }

  /*
   * Get all "Sources" of data - root tables of each data tree
   */
  def dataSources()(implicit db: Database): Future[Seq[ApiDataTable]] = {
    val rootTablesQuery = for {
      trees <- DataTableTree.filter(_.table1.isEmpty)
    } yield trees

    val eventualTrees = db.run(rootTablesQuery.result)

    eventualTrees.map { trees =>
      trees map { tree =>
        ModelTranslation.fromDbModel(tree, None, None)
      }
    }
  }

  /*
   *  Construct nested DataTable records with associated fields and sub-tables
   */
  def getTableStructure(tableId: Int)(implicit db: Database): Future[Option[ApiDataTable]] = {
    val dataTableTrees = for {
      tree <- DataTableTree.filter(_.rootTable === tableId)
    } yield tree

    buildDataTreeStructures(dataTableTrees, Set(tableId)).map(_.headOption)
  }

  /*
   * Stores a set of values for a data record as a single batch operation
   */
  def storeRecordValues(recordValues: Seq[ApiRecordValues], userId: UUID)(implicit db: Database, request: SecuredRequest[HatApiAuthEnvironment, _]): Future[Seq[ApiRecordValues]] = {
    val records = recordValues map { apiRecordValues =>
      val newRecord = DataRecordRow(0, LocalDateTime.now(), apiRecordValues.record.lastUpdated.getOrElse(LocalDateTime.now()), apiRecordValues.record.name)
      val maybeRecord = db.run((DataRecord returning DataRecord) += newRecord)

      maybeRecord flatMap { insertedRecord =>
        val record = ModelTranslation.fromDbModel(insertedRecord, None)
        val valueRows = apiRecordValues.values map { value =>
          DataValueRow(0, LocalDateTime.now(), value.lastUpdated.getOrElse(LocalDateTime.now()), value.value, value.field.get.id.get, record.id.get)
        }
        val insertedValues = db.run {
          ((DataValue returning DataValue) ++= valueRows).transactionally
        }

        insertedValues flatMap { values =>
          val valueSet = values.map(_.id).toSet
          val valueDetailQuery = for {
            value <- DataValue.filter(_.id inSet valueSet)
            field <- value.dataFieldFk
            record <- value.dataRecordFk
          } yield (value, field, record)
          val eventualDataValues = db.run(valueDetailQuery.result).map { values =>
            values map {
              case (value, field, valueRecord) =>
                ModelTranslation.fromDbModel(value, field, valueRecord)
            }
          }

          eventualDataValues map { dataValues =>
            ApiRecordValues(record, dataValues)
          }
        } andThen {
          case Success(_) =>
            // TEMPORARY
            getRecordValues(insertedRecord.id)
              .flatMap { r =>
                FutureTransformations.transform(
                  r.map(record => migrationService.migrateOldDataRecord(userId, record)))
              } onFailure {
                case e => logger.error(s"Error migrating record to new APIs: ${e.getMessage}")
              }
        }
      }
    }

    Future.sequence(records)
  }

  /*
   * Fetches values from database matching a set of fields and falling within a time range
   */
  protected[hat] def fieldsetValues(
    fieldset: Set[Int],
    startTime: LocalDateTime, endTime: LocalDateTime,
    maybeLimit: Option[Int] = None)(implicit db: Database): Future[Seq[(DataRecordRow, DataFieldRow, DataValueRow)]] = {
    val fieldValues = DataValue.filter(_.fieldId inSet fieldset)
    val valuesQuery = fieldValues.filter(v => v.lastUpdated <= endTime && v.lastUpdated >= startTime && v.deleted === false)
      .sortBy(_.recordId.desc)
    val valueQuery = for {
      (record, value) <- DataRecord.filter(_.id in valuesQuery.map(_.recordId))
        .filter(r => r.lastUpdated <= endTime && r.lastUpdated >= startTime && r.deleted === false)
        .sortBy(_.lastUpdated.desc)
        .take(maybeLimit.getOrElse(1000))
        .join(valuesQuery)
        .on(_.id === _.recordId)
      field <- value.dataFieldFk if field.deleted === false
    } yield (record, field, value)

    db.run(valueQuery.result)
  }

  protected[hat] def fieldsetValues(
    fieldset: Query[DataField, DataField#TableElementType, Seq],
    startTime: LocalDateTime, endTime: LocalDateTime,
    limit: Int)(implicit db: Database): Future[Seq[(DataRecordRow, DataFieldRow, DataValueRow)]] = {

    val valuesQuery = fieldset.join(DataValue).on(_.id === _.fieldId).map(_._2)
      .filter { v => v.lastUpdated <= endTime && v.lastUpdated >= startTime && v.deleted === false }

    val recordsQuery = DataRecord.filter(_.id in valuesQuery.sortBy(_.recordId.desc).map(_.recordId).distinct)
      .filter(r => r.lastUpdated <= endTime && r.lastUpdated >= startTime && r.deleted === false)
      .sortBy(_.lastUpdated.desc)
      .take(limit)

    val valueQuery = for {
      record <- recordsQuery
      value <- valuesQuery.filter(_.recordId === record.id)
      field <- value.dataFieldFk if field.deleted === false
    } yield (record, field, value)

    db.run(valueQuery.result)
  }

  def recordValues(recordId: Int)(implicit db: Database): Future[Seq[(DataRecordRow, DataFieldRow, DataValueRow)]] = {
    val valueQuery = for {
      value <- DataValue.filter(_.deleted === false)
      record <- value.dataRecordFk.filter(_.id === recordId) if record.deleted === false
      field <- value.dataFieldFk if field.deleted === false
    } yield (record, field, value)

    db.run(valueQuery.sortBy(_._1.id.desc).result)
  }

  def getValue(id: Int)(implicit db: Database): Future[Option[ApiDataValue]] = {
    val valueQuery = for {
      value <- DataValue.filter(v => v.id === id && v.deleted === false)
      record <- value.dataRecordFk if record.deleted === false
      field <- value.dataFieldFk if field.deleted === false
    } yield (record, field, value)

    val eventualValues = db.run(valueQuery.sortBy(_._1.id.desc).result)

    eventualValues map { values =>
      values.headOption map {
        case (record: DataRecordRow, field: DataFieldRow, value: DataValueRow) =>
          ModelTranslation.fromDbModel(value, field, record)
      }
    }
  }

  def getRecordValues(recordId: Int)(implicit db: Database): Future[Option[ApiDataRecord]] = {
    val eventualValues = recordValues(recordId)
    val eventualFieldset = eventualValues.map { recordValues =>
      // logger.debug(s"Getting record values $recordValues")
      recordValues.unzip3._2.map(_.id).toSet
    }

    val eventualTables = eventualFieldset flatMap { fieldset: Set[Int] =>
      // logger.debug(s"Getting tables for fieldset $fieldset")
      val dataTableTreesQuery = for {
        field <- DataField.filter(_.id inSet fieldset) if field.deleted === false
        rootTable <- DataTableTree.filter(field.tableIdFk === _.path.any)
        tree <- DataTableTree.filter(_.rootTable === rootTable.path.any)
      } yield tree

      buildDataTreeStructures(dataTableTreesQuery)
    }

    eventualTables flatMap { tables =>
      eventualValues.map(values => restructureTableValuesToRecords(values, tables))
    } map (_.headOption)
  }

  ///////////////////
  // Data deletion
  // Important: Data never gets deleted from the HAT, it only gets hidden from all APIs

  def deleteRecord(recordId: Int)(implicit db: Database): Future[Unit] = {
    db.run {
      DBIO.seq(
        // 1. Delete all values in the record
        DataValue.filter(_.recordId === recordId)
          .map(v => (v.lastUpdated, v.deleted))
          .update((LocalDateTime.now(), true)),
        // 2. Delete the record
        DataRecord.filter(_.id === recordId)
          .map(r => (r.lastUpdated, r.deleted))
          .update((LocalDateTime.now(), true)))
    }
  }

  def deleteValue(value: Int)(implicit db: Database): Future[Unit] = {
    deleteValues(List(value))
  }

  def deleteValues(values: List[Int])(implicit db: Database): Future[Unit] = {
    val valueSet = values.toSet
    // Delete the matching values
    db.run {
      DBIO.seq(
        DataValue.filter(_.id inSet valueSet)
          .map(v => (v.lastUpdated, v.deleted))
          .update((LocalDateTime.now(), true)))
    }
  }

  def deleteField(fieldId: Int)(implicit db: Database): Future[Unit] = {
    db.run {
      DBIO.seq(
        // 1. Delete all values in the field
        DataValue.filter(_.fieldId === fieldId)
          .map(v => (v.lastUpdated, v.deleted))
          .update((LocalDateTime.now(), true)),
        // 2. Delete the field
        DataField.filter(_.id === fieldId)
          .map(f => (f.lastUpdated, f.deleted))
          .update((LocalDateTime.now(), true)))
    }
  }

  def deleteTable(tableId: Int)(implicit db: Database): Future[Unit] = {
    // 1. Find the full tree of subtables
    val tableQuery = DataTable.filter(_.id in DataTableTree.filter(_.rootTable === tableId).map(_.id))
    // 2. Find all table relationships
    val tableRelationshipInQuery = DataTabletotablecrossref.filter(_.table1 in tableQuery.map(_.id))
    val tableRelatonshipOutQuery = DataTabletotablecrossref.filter(_.table2 in tableQuery.map(_.id))
    // 3. Find the full tree of fields
    val fieldQuery = DataField.filter(_.tableIdFk in tableQuery.map(_.id))
    // 4. Find all values in the fieldset
    val valueQuery = DataValue.filter(_.fieldId in fieldQuery.map(_.id))

    db.run {
      DBIO.seq(
        // 5. Delete the values
        valueQuery
          .map(v => (v.lastUpdated, v.deleted))
          .update((LocalDateTime.now(), true)),
        // 6. Delete the fields
        fieldQuery
          .map(f => (f.lastUpdated, f.deleted))
          .update((LocalDateTime.now(), true)),
        // 7. Delete table relationships
        tableRelationshipInQuery
          .map(tr => (tr.lastUpdated, tr.deleted))
          .update((LocalDateTime.now(), true)),
        tableRelatonshipOutQuery
          .map(tr => (tr.lastUpdated, tr.deleted))
          .update((LocalDateTime.now(), true)),
        // 8. Delete the tables
        tableQuery
          .map(t => (t.lastUpdated, t.deleted))
          .update((LocalDateTime.now(), true)))
    }
  }

  protected[hat] def restructureTableValuesToRecords(
    dbValues: Seq[(DataRecordRow, DataFieldRow, DataValueRow)],
    tables: Seq[ApiDataTable]): Seq[ApiDataRecord] =
    DataService.restructureTableValuesToRecords(dbValues, tables)

  protected[service] def buildTableStructure(
    table: ApiDataTable,
    fields: Seq[ApiDataField],
    dataTables: Seq[(ApiDataTable, Option[Int])]): ApiDataTable =
    DataService.buildTableStructure(table, fields, dataTables)

  protected[hat] def getStructureFields(structure: ApiDataTable): Set[Int] =
    DataService.getStructureFields(structure)

  protected[api] def buildDataTreeStructures(
    dataTableTrees: Query[DataTableTree, DataTableTreeRow, Seq],
    roots: Set[Int] = Set())(implicit db: Database): Future[Seq[ApiDataTable]] =
    DataService.buildDataTreeStructures(dataTableTrees, roots)
}

object DataService {
  protected[service] def buildTableStructure(
    table: ApiDataTable,
    fields: Seq[ApiDataField],
    dataTables: Seq[(ApiDataTable, Option[Int])]): ApiDataTable = {
    val tableFields = {
      val tFields = fields.filter(_.tableId == table.id)
      if (tFields.isEmpty) {
        None
      }
      else {
        Some(tFields)
      }
    }
    val subtables = dataTables.filter(tablePair => table.id == tablePair._2)
    val apiTables = subtables.map { apiTable =>
      // Every subtable with a linked ID exists, no need to handle Option
      buildTableStructure(apiTable._1, fields, dataTables)
    }
    val someApiTables = if (apiTables.isEmpty) {
      None
    }
    else {
      Some(apiTables)
    }
    table.copy(fields = tableFields, subTables = someApiTables)
  }

  protected[hat] def getStructureFields(structure: ApiDataTable): Set[Int] = {
    val fieldSet = structure.fields.getOrElse(Seq()).flatMap(_.id).toSet

    structure.subTables
      .getOrElse(Seq())
      .map(getStructureFields)
      .fold(fieldSet)((fieldset, subtableFieldset) => fieldset ++ subtableFieldset)
  }

  protected[hat] def restructureTableValuesToRecords(
    dbValues: Seq[(DataRecordRow, DataFieldRow, DataValueRow)],
    tables: Seq[ApiDataTable]): Seq[ApiDataRecord] = {

    // Group values by record
    val byRecord = dbValues.groupBy(_._1)

    val records = byRecord flatMap {
      case (record, recordValues: Seq[(DataRecordRow, DataFieldRow, DataValueRow)]) =>
        val fieldValues = recordValues.map { case (_, f, v) => (f, v) }
          .groupBy(_._1.id) // Group values by field
          .map { case (k, v) => (k, v.unzip._2.map(ModelTranslation.fromDbModel)) }

        val filledRecords = tables.flatMap { table =>
          val filledValues = fillStructure(table)(fieldValues)
          if (filledValues.fields.isDefined && filledValues.fields.get.nonEmpty ||
            filledValues.subTables.isDefined && filledValues.subTables.get.nonEmpty) {
            // Keep records separate for each root table
            Some(ModelTranslation.fromDbModel(record, Some(Seq(filledValues))))
          }
          else {
            None
          }
        }
        filledRecords
    }

    records.toSeq.sortBy(-_.id.getOrElse(0))
  }

  /*
   * Fills ApiDataTable with values grouped by field ID
   */
  protected def fillStructure(table: ApiDataTable)(values: Map[Int, Seq[ApiDataValue]]): ApiDataTable = {
    val filledFields = table.fields map { fields =>
      // For each field, insert values
      fields flatMap {
        case ApiDataField(Some(fieldId), dateCreated, lastUpdated, tableId, fieldName, _) =>
          val fieldValues = values.get(fieldId)
          fieldValues.map { fValues =>
            // Create a new field with only the values updated
            ApiDataField(Some(fieldId), dateCreated, lastUpdated, tableId, fieldName, Some(fValues))
          }
      }
    }

    val filledSubtables = table.subTables map { subtables =>
      val filled = subtables map { subtable: ApiDataTable =>
        fillStructure(subtable)(values)
      }
      val nonEmpty = filled.filter { t =>
        (t.fields.nonEmpty && t.fields.get.nonEmpty) || (t.subTables.nonEmpty && t.subTables.get.nonEmpty)
      }
      nonEmpty
    }

    table.copy(fields = filledFields, subTables = filledSubtables)
  }

  protected[api] def buildDataTreeStructures(
    dataTableTrees: Query[DataTableTree, DataTableTreeRow, Seq],
    roots: Set[Int] = Set())(implicit db: Database, ec: ExecutionContext): Future[Seq[ApiDataTable]] = {

    // Another round of field filtering to only get those within the returned trees
    val treeFieldQuery = for {
      (tree, maybeField) <- dataTableTrees joinLeft DataField.filter(_.deleted === false)
    } yield (tree, maybeField)

    val eventualTreeFields = db.run(treeFieldQuery.result)

    eventualTreeFields.map { treeFields =>
      val tablesWithParents = treeFields.map(_._1) // Get the trees
        .map(tree => (ModelTranslation.fromDbModel(tree, None, None), tree.table1)) // Use the API data model for each tree
        .distinct // Take only distinct trees (duplicates are returned with each field
      //logger.debug(s"Got tables with parents: $tablesWithParents")

      val fields = treeFields.flatMap(_._2)
        .map(ModelTranslation.fromDbModel)
        .distinct

      val rootTables = if (roots.nonEmpty) {
        tablesWithParents.filter(t => roots.contains(t._1.id.get)).map(_._1)
      }
      else {
        tablesWithParents.filter(_._2.isEmpty).map(_._1)
      }

      rootTables map { table =>
        buildTableStructure(table, fields, tablesWithParents)
      }
    }
  }

}