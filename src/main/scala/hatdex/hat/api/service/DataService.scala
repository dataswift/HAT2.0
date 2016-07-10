/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
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
 */
package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.Utils
import hatdex.hat.api.models.{ApiDataField, ApiDataRecord, ApiDataTable, ApiDataValue, _}
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import hatdex.hat.api.models._
import org.joda.time.LocalDateTime

import collection.mutable.{ HashMap, MultiMap }
import scala.collection.mutable
import scala.collection.immutable
import scala.util.{Failure, Success, Try}
import slick.jdbc.GetResult
import slick.jdbc.StaticQuery.interpolation

// this trait defines our service behavior independently from the service actor
trait DataService {

  val logger: LoggingAdapter

  def getTableValues(tableId: Int, maybeLimit: Option[Int] = None, maybeStartTime: Option[LocalDateTime] = None, maybeEndTime: Option[LocalDateTime] = None)(implicit session: Session): Option[Seq[ApiDataRecord]] = {
    val valuesQuery = DataValue
    val filteredByStart = maybeStartTime.map { startTime =>
      valuesQuery.filter(_.dateCreated >= startTime)
    } getOrElse {
      valuesQuery
    }
    val filteredByEnd = maybeEndTime.map { endTime =>
      filteredByStart.filter(_.dateCreated <= endTime)
    } getOrElse {
      filteredByStart
    }
    getTableValues(tableId, filteredByEnd, maybeLimit)
  }

  def getTableValues(tableId: Int, valuesQuery: Query[DataValue, DataValueRow, Seq], maybeLimit: Option[Int])
                    (implicit session: Session): Option[Seq[ApiDataRecord]] = {
    val someStructure = getTableStructure(tableId)

    someStructure map { structure =>
      // Partially applied function to fill the data into table structure
      def filler = fillStructure(structure) _

      // Get all data fields of interest according to the data structure
      val fieldsToGet = getStructureFields(structure)

      val assumedMaxValuesPerRecord = 30
      // Retrieve values of those fields from the database, grouping by Record ID
      val values = valuesQuery.filter(_.fieldId inSet fieldsToGet)
        .sortBy(_.recordId.desc)
        .take(maybeLimit.getOrElse(10000)*30)
        .run
        .groupBy(_.recordId)

      // Retrieve matching data records and transform to ApiDataRecord format
      val records: Map[Int, ApiDataRecord] = DataRecord.filter(_.id inSet values.keySet)
        .sortBy(_.id.desc)
        .take(maybeLimit.getOrElse(10000))
        .run
        .groupBy(_.id)
        .map { case (recordId, groupedRecord) =>
          (recordId, ApiDataRecord.fromDataRecord(groupedRecord.head)(None))
        }

      // Convert the retrieved structure into a sequence of maps from field ID to values
      val dataRecords = values map { case (recordId, recordValues) =>
        // Each field can have a list of values associated with it
        val fieldValueMap = new mutable.HashMap[Int, mutable.Set[ApiDataValue]] with mutable.MultiMap[Int, ApiDataValue]
        // Values are grouped by record id
        recordValues foreach { value =>
          fieldValueMap.addBinding(value.fieldId, ApiDataValue.fromDataValue(value))
        }
        // Fills reconstructed data structure with grouped field values
        val mappedValues = filler(fieldValueMap)

        if (records.contains(recordId)) {
          Some(records(recordId).copy(tables = Some(Seq(mappedValues))))
        } else {
          None
        }
      }

      dataRecords.flatten.toSeq.sortWith((a, b) =>
        a.lastUpdated
          .getOrElse(LocalDateTime.now())
          .isAfter(
            b.lastUpdated.getOrElse(LocalDateTime.now())
          )
      )
    }
  }


  def createValue(value: ApiDataValue, maybeFieldId: Option[Int], maybeRecordId: Option[Int])(implicit session: Session): Try[ApiDataValue] = {
    // find field ID either from the function parameter or from within the value structure
    val someFieldId = (maybeFieldId, value.field) match {
      case (Some(id), _) =>
        Some(id)
      case (None, Some(ApiDataField(Some(id), _, _, _, _, _))) =>
        Some(id)
      case _ =>
        None
    }

    // find record ID either from the function parameter or from within the value structure
    val someRecordId = (maybeRecordId, value.record) match {
      case (Some(id), _) =>
        Some(id)
      case (None, Some(ApiDataRecord(Some(id), _, _, _, _))) =>
        Some(id)
      case _ =>
        None
    }


    val maybeNewValue = (someFieldId, someRecordId) match {
      case (Some(fieldId), Some(recordId)) =>
        Success(DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), value.value, fieldId, recordId))
      case (None, _) =>
        Failure(new IllegalArgumentException("Correct field must be set for value to be inserted into"))
      case (_, None) =>
        Failure(new IllegalArgumentException("Correct record must be set for value to be inserted as part of"))
    }
    val insertedValue = maybeNewValue flatMap { newValue =>
      Try((DataValue returning DataValue) += newValue)
    }

    insertedValue map { inserted =>
      ApiDataValue.fromDataValueApi(inserted, value.field, value.record)
    }
  }

  def createTable(table: ApiDataTable)(implicit session: Session): Try[ApiDataTable] = {
    logger.debug("Creating new table for " + table)
    val newTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), table.name, table.source)
    val maybeTable = Try((DataTable returning DataTable) += newTable)

    logger.debug("Table created? " + maybeTable)

    val result = maybeTable flatMap { insertedTable =>
      val apiDataFields = table.fields map { fields =>
        val insertedFields = fields.map(createField(_, Some(insertedTable.id)))
        // Flattens to a simple Try with list if fields
        // or returns the first error that occurred
        Utils.flatten(insertedFields)
      }

      val apiSubtables = table.subTables map { subtables =>
        val insertedSubtables = subtables.map(createTable)
        val maybeSubtables = Utils.flatten(insertedSubtables)
        val links = maybeSubtables map { tables =>
          // Link the table to all its subtables
          tables.map(x => Try(linkTables(insertedTable.id, x.id.get, ApiRelationship("parent child"))))
        }
        maybeSubtables
      }

      (apiDataFields, apiSubtables) match {
        case (None, None) =>
          Success(ApiDataTable.fromDataTable(insertedTable)(None)(None))
        // If field insertion failed at any point, return that error
        case (Some(Failure(e)), _) =>
          Failure(e)
        // If subtable insertion failed at any point, return that error
        case (_, Some(Failure(e))) =>
          Failure(e)
        case (Some(Success(insertedFields)), Some(Success(insertedSubtables))) =>
          Success(ApiDataTable.fromDataTable(insertedTable)(Some(insertedFields))(Some(insertedSubtables)))
        case (Some(Success(insertedFields)), None) =>
          Success(ApiDataTable.fromDataTable(insertedTable)(Some(insertedFields))(None))
        case (None, Some(Success(insertedSubtables))) =>
          Success(ApiDataTable.fromDataTable(insertedTable)(None)(Some(insertedSubtables)))
      }
    }

    logger.debug("create table result" + result)
    result
  }

  def linkTables(parentId: Int, childId: Int, relationship: ApiRelationship)(implicit session: Session): Try[Int] = {
    val dataTableToTableCrossRefRow = new DataTabletotablecrossrefRow(
      1, LocalDateTime.now(), LocalDateTime.now(),
      relationship.relationshipType, parentId, childId
    )
    Try((DataTabletotablecrossref returning DataTabletotablecrossref.map(_.id)) += dataTableToTableCrossRefRow)
  }


  def createField(field: ApiDataField, maybeTableId: Option[Int] = None)(implicit session: Session): Try[ApiDataField] = {
    val maybeNewField = (maybeTableId, field.tableId) match {
      case (Some(tableId), _) =>
        Success(DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), field.name, tableId))
      case (_, Some(tableId)) =>
        Success(DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), field.name, tableId))
      case (None, None) =>
        Failure(new IllegalArgumentException("Table ID for field being inserted must be set"))
    }

    val maybeField = maybeNewField flatMap { newField =>
      Try((DataField returning DataField) += newField)
    }
    val temp = maybeField map { field =>
      ApiDataField.fromDataField(field)
    }

    temp
  }


  def getFieldValues(fieldId: Int)(implicit session: Session): Option[ApiDataField] = {
    val apiFieldOption = retrieveDataFieldId(fieldId)
    apiFieldOption map { apiField =>
      val values = DataValue.filter(_.fieldId === fieldId).sortBy(_.lastUpdated.desc).run
      val apiDataValues = values.map(ApiDataValue.fromDataValue)
      apiField.copy(values = Some(apiDataValues))
    }
  }

  def getFieldRecordValue(fieldId: Int, recordId: Int)(implicit session: Session): Option[ApiDataField] = {
    val apiFieldOption = retrieveDataFieldId(fieldId)
    apiFieldOption map { apiField =>
      val values = DataValue.filter(_.fieldId === fieldId).filter(_.recordId === recordId).run
      val apiDataValues = values.map(ApiDataValue.fromDataValue)
      apiField.copy(values = Some(apiDataValues))
    }
  }

  def fillStructures(tables: Seq[ApiDataTable])(values: mutable.HashMap[Int, mutable.Set[ApiDataValue]] with mutable.MultiMap[Int, ApiDataValue]): Seq[ApiDataTable] = {
    tables map { table =>
      fillStructure(table)(values)
    }
  }

  def fillStructure(table: ApiDataTable)(values: mutable.HashMap[Int, mutable.Set[ApiDataValue]] with mutable.MultiMap[Int, ApiDataValue]): ApiDataTable = {
    logger.debug("Filling structure " + table.id + " with values " + values)
    val filledFields = table.fields map { fields =>
      // For each field, insert values
      fields map { field: ApiDataField =>
        field.id match {
          // If a given field has an ID (all fields should)
          case Some(fieldId) =>
            // Get the value from the map to be added to the field
            val fieldValue = values get fieldId map { matchingFieldValue =>
              matchingFieldValue.toSeq
            }
            // Create a new field with only the values updated
            field.copy(values = fieldValue)
          case None =>
            field
        }
      }
    }

    val filledSubtables = table.subTables map { subtables =>
      subtables map { subtable: ApiDataTable =>
        fillStructure(subtable)(values)
      }
    }
    table.copy(fields = filledFields, subTables = filledSubtables)
  }

  def getStructures(tables: Seq[ApiDataTable])(implicit session: Session): Seq[ApiDataTable] = {
    // Get all root tables from the given ApiDataTables
    val roots = tables.map { table =>
      table.id match {
        case Some(id) =>
          getRootTables(id)
        case None =>
          Set[Int]()
      }
    }

    // Get the set of "root" tables to build data trees from
    val rootSet = roots.foldLeft(Set[Int]())((roots, parents) => roots ++ parents)

    // Construct table structures from the root
    rootSet.toSeq.flatMap { root =>
      getTableStructure(root)
    }
  }

  /*
   * Get IDs of all (Database) DataTables, using the DataTabletotablecrossref
   */
  private def getRootTables(tableId: Int)(implicit session: Session): Set[Int] = {
    val parents = DataTabletotablecrossref.filter(_.table2 === tableId).map(_.table1).run
    // If a table doesn't have any parents, it is a root
    if (parents.isEmpty) {
      Set(tableId)
    }
    else {
      // Get all roots recursively
      val parentSets = parents map { parentId =>
        getRootTables(parentId)
      }
      // Fold them into a single set with no repetitions
      parentSets.foldLeft(Set[Int]())((roots, parents) => roots ++ parents)
    }
  }

  def findTable(name: String, source: String)(implicit session: Session): Seq[ApiDataTable] = {
    val dbDataTable = DataTable.filter(_.sourceName === source).filter(_.name === name).run
    dbDataTable map { table =>
      ApiDataTable.fromDataTable(table)(None)(None)
    }
  }

  def findTablesLike(name: String, source: String)(implicit session: Session): Seq[ApiDataTable] = {
    val dbDataTable = DataTable.filter(_.sourceName === source).filter(_.name like "%" + name + "%").run
    dbDataTable map { table =>
      ApiDataTable.fromDataTable(table)(None)(None)
    }
  }

  def findTablesNotLike(name: String, source: String)(implicit session: Session): Seq[ApiDataTable] = {
    val dbDataTable = DataTable.filter(_.sourceName === source).filterNot(_.name like "%" + name + "%").run
    dbDataTable map { table =>
      ApiDataTable.fromDataTable(table)(None)(None)
    }
  }

  def getDataSources()(implicit session: Session): Seq[ApiDataTable] = {
    val childTablesQuery = DataTabletotablecrossref.map(_.table2)
    val rootTablesQuery = DataTable.filterNot(_.id in childTablesQuery)

    val rootTables = rootTablesQuery.run

    rootTables.map { dataTable =>
      ApiDataTable.fromDataTable(dataTable)(None)(None)
    }
  }

  /*
   * Recursively construct nested DataTable records with associated fields and sub-tables
   */
  def getTableStructure(tableId: Int)(implicit session: Session): Option[ApiDataTable] = {
    logger.debug(s"Get sturcture for $tableId")
    val tablesWithParents = getTablesRecursively(tableId)

    logger.debug(s"Got tables: $tablesWithParents")
    val tableIds = tablesWithParents.flatMap(_._1.id).toSet
    val fields = DataField.filter(_.tableIdFk inSet tableIds)
      .run
      .map(ApiDataField.fromDataField)

    val rootTable = tablesWithParents.headOption.map(_._1)

    logger.debug(s"Starting with root table $rootTable")
    rootTable map { table =>
      buildTableStructure(table, fields, tablesWithParents)
    }
  }

  protected[service] def buildTableStructure(table: ApiDataTable, fields: Seq[ApiDataField], dataTables: Seq[(ApiDataTable, Option[Int])]): ApiDataTable = {
    val tableFields = {
      val tFields = fields.filter(_.tableId == table.id)
      if (tFields.isEmpty) {
        None
      } else {
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
    } else {
      Some(apiTables)
    }
    table.copy(fields = tableFields, subTables = someApiTables)
  }

  private def getTablesRecursively(tableId: Int)(implicit session: Session): Seq[(ApiDataTable, Option[Int])] = {
    val tables = DataTableTree.filter(_.rootTable === tableId).run
    tables map { table =>
      (ApiDataTable.fromNestedTable(table)(None)(None), table.table1)
    }
  }

  protected[api] def getStructureFields(structure: ApiDataTable): Set[Int] = {
    val fieldSet = structure.fields match {
      case Some(fields) =>
        fields.flatMap(_.id).toSet
      case None =>
        Set[Int]()
    }

    val subtableFieldSets: Seq[Set[Int]] = structure.subTables match {
      case Some(subTables) =>
        subTables map getStructureFields
      case None =>
        Seq[Set[Int]]()
    }

    subtableFieldSets.foldLeft(fieldSet)((fields, subtableFields) => fields ++ subtableFields)
  }


  /*
   * Private function finding data field by ID
   */
  def retrieveDataFieldId(fieldId: Int)(implicit session: Session): Option[ApiDataField] = {
    val field = DataField.filter(_.id === fieldId).run.headOption
    field map { dataField =>
      ApiDataField.fromDataField(dataField)
    }
  }

  def storeRecordValues(recordValues: ApiRecordValues)(implicit session: Session): Try[ApiRecordValues] = {
    val newRecord = new DataRecordRow(0, LocalDateTime.now(), LocalDateTime.now(), recordValues.record.name)
    val maybeRecord = Try((DataRecord returning DataRecord) += newRecord)

    maybeRecord flatMap { insertedRecord =>
      val record = ApiDataRecord.fromDataRecord(insertedRecord)(None)
      val insertedValues = recordValues.values map { value =>
        createValue(value, None, record.id)
      }

      val maybeValues = Utils.flatten(insertedValues)
      maybeValues.map { values =>
        ApiRecordValues(record, values)
      }
    }
  }
}