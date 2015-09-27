package dalapi.service

import dal.SlickPostgresDriver.simple._
import dal.Tables._
import dalapi.DatabaseInfo
import dalapi.models.{ApiDataField, ApiDataRecord, ApiDataTable, ApiDataValue, _}
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Success, Try}

// this trait defines our service behavior independently from the service actor
trait DataService extends HttpService with DatabaseInfo {

  val routes = { pathPrefix("data") {
      createTableApi ~
        linkTableToTableApi ~
        createFieldApi ~
        createRecordApi ~
        createValueApi ~
        storeValueListApi ~
        getFieldApi ~
        getFieldValuesApi ~
        getTableApi ~
        getTableValuesApi ~
        getRecordApi ~
        getRecordValuesApi ~
        getValueApi
    }
  }

  import JsonProtocol._

  /*
   * Creates a new virtual table for storing arbitrary incoming data
   */
  def createTableApi = path("table") {
    post {
      entity(as[ApiDataTable]) { table =>
        db.withSession { implicit session =>
          val newTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), table.name, table.source)
          val tableId = Try((DataTable returning DataTable.map(_.id)) += newTable)

          val tableStructure = tableId match {
            case Success(id) =>
              Success(getTableStructure(id).get)
            case Failure(e) =>
              Failure(e)
          }

          complete {
            tableStructure match {
              case Success(structure) =>
                (Created, structure)
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }
        }
      }

    }
  }

  /*
   * Marks provided table as a "child" of another, e.g. to created nested data structured
   */
  def linkTableToTableApi = path("table" / IntNumber / "table" / IntNumber) {
    (parentId: Int, childId: Int) =>
      post {
        entity(as[ApiRelationship]) { relationship =>
          db.withSession { implicit session =>
            val dataTableToTableCrossRefRow = new DataTabletotablecrossrefRow(
              1, LocalDateTime.now(), LocalDateTime.now(),
              relationship.relationshipType, parentId, childId
            )
            val inserted = Try((DataTabletotablecrossref returning DataTabletotablecrossref.map(_.id)) += dataTableToTableCrossRefRow)

            complete {
              inserted match {
                case Success(id) =>
                  ApiGenericId(id)
                case Failure(e) =>
                  (BadRequest, e.getMessage)
              }
            }
          }
        }
      }
  }

  /*
   * Get specific table information. Includes all fields and sub-tables
   */
  def getTableApi = path("table" / IntNumber) {
    (tableId: Int) =>
      get {
        db.withSession { implicit session =>
          complete {
            getTableStructure(tableId)
          }
        }
      }
  }

  def getTableValuesApi = path("table" / IntNumber / "values") {
    (tableId: Int) =>
      get {
        db.withSession { implicit session =>
          val someStructure = getTableStructure(tableId)

          val someTableValues = someStructure map { structure =>
            // Partially applied function to fill the data into table structure
            def filler = fillStructure(structure) _

            // Get all data fields of interest according to the data structure
            val fieldsToGet = getStructureFields(structure)

            // Retrieve values of those fields from the database, grouping by Record ID
            val values = db.withSession { implicit session =>
              DataValue.filter(_.fieldId inSet fieldsToGet)
                .take(fieldsToGet.size * 1000) // FIXME: magic number for getting X records
                .sortBy(_.recordId.asc)
                .run
                .groupBy(_.recordId)
            }

            // Convert the retrieved structure into a sequence of maps from field ID to values
            val data = values map { case (recordId, recordValues) =>
              Map(recordValues map { value => value.fieldId -> ApiDataValue.fromDataValue(value) }: _*)
            }

            // Fill the structure with the data
            data.map(filler)
          }

          complete {
            someTableValues match {
              case Some(tableValues) =>
                tableValues
              case None =>
                (NotFound, s"Table $tableId not found")
            }
          }
        }
      }
  }

  /*
   * Create a new field in a virtual table
   */
  def createFieldApi = path("field") {
    post {
      entity(as[ApiDataField]) { field =>
        db.withSession { implicit session =>
          val newField = new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), field.name, field.tableId)
          val insertedField = Try((DataField returning DataField) += newField)
          complete {
            insertedField match {
              case Success(field) =>
                (Created, ApiDataField.fromDataField(field))
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }
        }
      }
    }
  }

  /*
   * Get field (information only) by ID
   */
  def getFieldApi = path("field" / IntNumber){ (fieldId: Int) =>
    get {
      db.withSession { implicit session =>
        complete {
          retrieveDataFieldId(fieldId) match {
            case Some(field) =>
              field
            case None =>
              (NotFound, "Data field $fieldId not found")
          }
        }
      }
    }
  }

  /*
   * Get data stored in a specific field.
   * Returns all Data Values stored in the field
   */
  def getFieldValuesApi = path("field" / IntNumber / "values") { (fieldId: Int) =>
    get {
      db.withSession { implicit session =>
        complete {
          getFieldValues(fieldId) match {
            case Some(field) =>
              field
            case None =>
              (NotFound, "Data field $fieldId not found")
          }
        }
      }
    }
  }

  /*
   * Insert a new, potentially named, data record
   */
  def createRecordApi = path("record") {
    post {
      entity(as[ApiDataRecord]) { record =>
        db.withSession { implicit session =>
          val newRecord = new DataRecordRow(0, LocalDateTime.now(), LocalDateTime.now(), record.name)
          val recordId = (DataRecord returning DataRecord.map(_.id)) += newRecord
          complete {
            record.copy(id = Some(recordId))
          }
        }
      }
    }
  }

  /*
   * Get record
   */
  def getRecordApi = path("record" / IntNumber) { (recordId: Int) =>
    get {
      db.withSession { implicit session =>
        val record = DataRecord.filter(_.id === recordId).run.head

        complete {
          ApiDataRecord(Some(record.id), Some(record.dateCreated), Some(record.lastUpdated), record.name, None)
        }
      }
    }
  }

  /*
   * Get values associated with a record.
   * Constructs a hierarchy of fields and data within each field for the record
   */
  def getRecordValuesApi = path("record" / IntNumber / "values") { (recordId: Int) =>
    get {
      db.withSession { implicit session =>
        // Retrieve joined Data Values, Fields and Tables
        val fieldValuesTables =  DataValue.filter(_.recordId === recordId) join
            DataField on (_.fieldId === _.id) join
            DataTable on (_._2.tableIdFk === _.id)

        val result = fieldValuesTables.run

        val structures = getStructures(result.map(_._2).map{ table =>
          ApiDataTable.fromDataTable(table)(None)(None)
        })

        val values = result.map(_._1._1)
        val valueMap = Map(values map { value => value.fieldId -> ApiDataValue.fromDataValue(value) }: _*)
        val recordData = fillStructures(structures)(valueMap)

        // Retrieve and prepare the record itself
        val record = DataRecord.filter(_.id === recordId).run.head
        val apiRecord = ApiDataRecord.fromDataRecord(record)(Some(recordData))

        complete {
          apiRecord
        }
      }
    }
  }

  /*
   * Create (insert) a new data value
   */
  def createValueApi = path("value") {
    post {
      entity(as[ApiDataValue]) { value =>
        db.withSession { implicit session =>
          val newValue = new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), value.value, value.fieldId, value.recordId)
          val inserted = (DataValue returning DataValue) += newValue

          complete {
            ApiDataValue.fromDataValue(inserted)
          }
        }
      }
    }
  }

  /*
   * Retrieve a data value by ID
   */
  def getValueApi = path("value" / IntNumber) {  (valueId: Int) =>
    get {
      db.withSession { implicit session =>
        val value = DataValue.filter(_.id === valueId).run.head
        val apiValue = ApiDataValue.fromDataValue(value)

        complete {
          apiValue
        }
      }
    }

  }

  /*
   * Batch-insert data values as a list
   */
  def storeValueListApi = path("value" / "list") {
    post {
      entity(as[Seq[ApiDataValue]]) { values =>
        db.withSession { implicit session =>
          val dataValues = values.map { value =>
            DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), value.value, value.fieldId, value.recordId)
          }

          val insertedValues = Try((DataValue returning DataValue) ++= dataValues)

          val returning = insertedValues map { inserted =>
            inserted.map(ApiDataValue.fromDataValue)
          }

          complete {
            returning
          }
        }
      }
    }
  }

  def getFieldValues(fieldId: Int)(implicit session: Session): Option[ApiDataField] = {
    val apiFieldOption = retrieveDataFieldId(fieldId)
    apiFieldOption map { apiField =>
      val values = DataValue.filter(_.fieldId === fieldId).run
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
    table.copy(fields = filledFields)
  }

  def getStructures(tables: Seq[ApiDataTable])(implicit session: Session): Seq[ApiDataTable] = {
    // Get all root tables from the given ApiDataTables
    val roots = tables.map{ table =>
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

  private def getStructureFields(structure : ApiDataTable) : Set[Int] = {
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
  private def retrieveDataFieldId(fieldId: Int)(implicit session: Session): Option[ApiDataField] = {
    val field = DataField.filter(_.id === fieldId).run.headOption
    field map { dataField =>
      ApiDataField.fromDataField(dataField)
    }
  }
}