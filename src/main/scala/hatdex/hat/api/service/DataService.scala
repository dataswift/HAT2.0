package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.Utils
import hatdex.hat.api.models.{ApiDataField, ApiDataRecord, ApiDataTable, ApiDataValue, _}
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.util.{Failure, Success, Try}

// this trait defines our service behavior independently from the service actor
trait DataService {

  val logger: LoggingAdapter



  def getTableValues(tableId: Int)(implicit session: Session): Option[Iterable[ApiDataRecord]] = {
    val valuesQuery = DataValue
    getTableValues(tableId, valuesQuery)
  }

  def getTableValues(tableId: Int, valuesQuery: Query[DataValue, DataValueRow, Seq])
                    (implicit session: Session): Option[Iterable[ApiDataRecord]] = {
    val someStructure = getTableStructure(tableId)

    someStructure map { structure =>
      // Partially applied function to fill the data into table structure
      def filler = fillStructure(structure) _

      // Get all data fields of interest according to the data structure
      val fieldsToGet = getStructureFields(structure)

      // Retrieve values of those fields from the database, grouping by Record ID
      val values = valuesQuery.filter(_.fieldId inSet fieldsToGet)
         .take(10000) // FIXME: magic number for getting X records
         .sortBy(_.recordId.desc)
        .run
        .groupBy(_.recordId)

      // Retrieve matching data records and transform to ApiDataRecord format
      val records: Map[Int, ApiDataRecord] = DataRecord.filter(_.id inSet values.keySet)
        .run
        .groupBy(_.id)
        .map { case (recordId, records) =>
          (recordId, ApiDataRecord.fromDataRecord(records.head)(None))
        }

      // Convert the retrieved structure into a sequence of maps from field ID to values
      val dataRecords = values map { case (recordId, recordValues) =>
        val fieldValueMap = Map(recordValues map { value => value.fieldId -> ApiDataValue.fromDataValue(value) }: _*)
        val mappedValues = filler(fieldValueMap)
        records(recordId).copy(tables = Some(Seq(mappedValues)))
      }

      dataRecords.toList.sortWith((a, b) =>
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
    val newTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), table.name, table.source)
    val maybeTable = Try((DataTable returning DataTable) += newTable)

    maybeTable flatMap { insertedTable =>
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

  def fillStructures(tables: Seq[ApiDataTable])(values: Map[Int, ApiDataValue]): Seq[ApiDataTable] = {
    tables map { table =>
      fillStructure(table)(values)
    }
  }

  def fillStructure(table: ApiDataTable)(values: Map[Int, ApiDataValue]): ApiDataTable = {
    val filledFields = table.fields map { fields =>
      // For each field, insert values
      fields map { field: ApiDataField =>
        field.id match {
          // If a given field has an ID (all fields should)
          case Some(fieldId) =>
            // Get the value from the map to be added to the field
            val fieldValue = values get fieldId map { value: ApiDataValue =>
              Seq(value)
            }
            // Create a new field with only the values updated
            field.copy(values = fieldValue)
          case None =>
            field
        }
      }
    }

    var filledSubtables = table.subTables map { subtables =>
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

  def findTable(name: String, source: String)(implicit session: Session): Option[ApiDataTable] = {
    val dbDataTable = DataTable.filter(_.sourceName === source).filter(_.name === name).run.headOption
    dbDataTable map { table =>
      ApiDataTable.fromDataTable(table)(None)(None)
    }
  }

  // Noggin
  def findTablesLike(name: String, source: String)(implicit session: Session): Seq[ApiDataTable] = {
    val dbDataTable = DataTable.filter(_.sourceName === source).filter(_.name like "%"+name+"%").run
    dbDataTable map { table =>
      ApiDataTable.fromDataTable(table)(None)(None)
    }
  }

  // Noggin
  def findTablesNotLike(name: String, source: String)(implicit session: Session): Seq[ApiDataTable] = {
    val dbDataTable = DataTable.filter(_.sourceName === source).filterNot(_.name like "%"+name+"%").run
    dbDataTable map { table =>
      ApiDataTable.fromDataTable(table)(None)(None)
    }
  }

  // Noggin
  def findValue(recordId: Int, tableName: String, tableSource: String, fieldName: String, fieldValue: String)(implicit session: Session): Option[ApiDataValue] = {
    val dataValueQuery = DataValue.filter(dataValue => dataValue.recordId === recordId).filter(dataValue => dataValue.value === fieldValue).flatMap(dataValue =>
        DataField.filter(dataField => dataField.id === dataValue.fieldId).filter(dataField => dataField.name === fieldName).flatMap(dataField =>
          DataTable.filter(dataTable => dataTable.name === tableName).filter(dataTable => dataTable.sourceName === tableSource).filter(dataTable => dataTable.id === dataField.tableIdFk).map(dataTable => (dataValue.id, dataValue.dateCreated, dataValue.lastUpdated, dataValue.value))
        )
    ).run.headOption

    dataValueQuery map { result =>
      new ApiDataValue(Some(result._1), Some(result._2), Some(result._3), result._4, None, None)
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

    val someTable = DataTable.filter(_.id === tableId).run.headOption

    someTable map { table =>
      val fields = DataField.filter(_.tableIdFk === tableId).run
      val apiFields = fields.map(ApiDataField.fromDataField)

      val subtables = DataTabletotablecrossref.filter(_.table1 === tableId).map(_.table2).run
      val apiTables = subtables.map { subtableId =>
        // Every subtable with a linked ID exists, no need to handle Option
        getTableStructure(subtableId).get
      }

      ApiDataTable.fromDataTable(table)(Some(apiFields))(Some(apiTables))
    }

  }

  private def getStructureFields(structure: ApiDataTable): Set[Int] = {
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
}
