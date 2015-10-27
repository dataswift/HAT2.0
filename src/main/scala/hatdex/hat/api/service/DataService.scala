package hatdex.hat.api.service

import hatdex.hat.Utils
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models.{ApiDataField, ApiDataRecord, ApiDataTable, ApiDataValue, _}
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._
import hatdex.hat.authentication.{HatServiceAuthHandler, HatAuthHandler}

import scala.util.{Failure, Success, Try}

// this trait defines our service behavior independently from the service actor
trait DataService extends HttpService with DatabaseInfo with HatServiceAuthHandler {

  //  import hatdex.hat.authentication.HatServiceAuthHandler._

  val routes = {
    pathPrefix("data") {
      getFieldApi ~
        getFieldValuesApi ~
        getTableApi ~
        findTableApi ~
        getTableValuesApi ~
        getRecordApi ~
        getRecordValuesApi ~
        getValueApi ~
        createTableApi ~
        linkTableToTableApi ~
        createFieldApi ~
        createRecordApi ~
        createValueApi ~
        storeValueListApi
    }
  }

  import JsonProtocol._

  /*
   * Creates a new virtual table for storing arbitrary incoming data
   */
  def createTableApi = path("table") {
    post {
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        entity(as[ApiDataTable]) { table =>
          db.withSession { implicit session =>
            val tableStructure = createTable(table)

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
  }

  /*
   * Marks provided table as a "child" of another, e.g. to created nested data structured
   */
  def linkTableToTableApi = path("table" / IntNumber / "table" / IntNumber) { (parentId: Int, childId: Int) =>
    (accessTokenHandler | userPassHandler) { implicit user: User =>
      post {
        entity(as[ApiRelationship]) { relationship =>
          db.withSession { implicit session =>
            val inserted = linkTables(parentId, childId, relationship)

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
  }

  /*
   * Get specific table information. Includes all fields and sub-tables
   */
  def getTableApi = path("table" / IntNumber) { (tableId: Int) =>
    (userPassHandler | accessTokenHandler) { implicit user: User =>
      get {
        db.withSession { implicit session =>
          val structure = getTableStructure(tableId)
          complete {
            session.close()
            structure
          }
        }
      }
    }
  }

  def findTableApi = path("table" / "search") {
    (userPassHandler | accessTokenHandler) { implicit user: User =>
      get {
        parameters('name, 'source) { (name: String, source: String) =>
          db.withSession { implicit session =>
            val table = findTable(name, source)
            complete {
              session.close()
              table
            }
          }
        }
      }
    }
  }

  def getTableValuesApi = path("table" / IntNumber / "values") { (tableId: Int) =>
    userPassHandler { implicit user: User =>
      get {
        db.withSession { implicit session =>
          val someTableValues = getTableValues(tableId)
          session.close()
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
  }

  /*
   * Create a new field in a virtual table
   */
  def createFieldApi = path("field") {
    (accessTokenHandler | userPassHandler) { implicit user: User =>
    post {
      entity(as[ApiDataField]) { field =>
        db.withSession { implicit session =>
          val insertedField = createField(field)
          complete {
            insertedField match {
              case Success(field) =>
                (Created, field)
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }
        }
      }
    }
    }
  }

  /*
   * Get field (information only) by ID
   */
  def getFieldApi = path("field" / IntNumber) { (fieldId: Int) =>
    (userPassHandler | accessTokenHandler) { implicit user: User =>
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
  }

  /*
   * Get data stored in a specific field.
   * Returns all Data Values stored in the field
   */
  def getFieldValuesApi = path("field" / IntNumber / "values") { (fieldId: Int) =>
    userPassHandler { implicit user: User =>
      get {
        db.withSession { implicit session =>
          complete {
            getFieldValues(fieldId) match {
              case Some(field) =>
                field
              case None =>
                (NotFound, s"Data field $fieldId not found")
            }
          }
        }
      }
    }
  }

  /*
   * Insert a new, potentially named, data record
   */
  def createRecordApi = path("record") {
    (accessTokenHandler | userPassHandler) { implicit user: User =>
      post {
        entity(as[ApiDataRecord]) { record =>
          db.withSession { implicit session =>
            val newRecord = new DataRecordRow(0, LocalDateTime.now(), LocalDateTime.now(), record.name)
            val recordId = Try((DataRecord returning DataRecord.map(_.id)) += newRecord)
            complete {
              recordId match {
                case Success(id) =>
                  (Created, record.copy(id = Some(id)))
                case Failure(e) =>
                  (BadRequest, e.getMessage)
              }

            }
          }
        }
      }
    }
  }

  /*
   * Get record
   */
  def getRecordApi = path("record" / IntNumber) { (recordId: Int) =>
    (userPassHandler | accessTokenHandler) { implicit user: User =>
      get {
        db.withSession { implicit session =>
          val record = DataRecord.filter(_.id === recordId).run.headOption

          complete {
            record match {
              case Some(dataRecord) =>
                ApiDataRecord.fromDataRecord(dataRecord)(None)
              case None =>
                (NotFound, s"Data Record $recordId not found")
            }
          }
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
      userPassHandler { implicit user: User =>
        db.withSession { implicit session =>
          // Retrieve joined Data Values, Fields and Tables
          val fieldValuesTables = DataValue.filter(_.recordId === recordId) join
            DataField on (_.fieldId === _.id) join
            DataTable on (_._2.tableIdFk === _.id)

          val result = fieldValuesTables.run

          val structures = getStructures(result.map(_._2).map { table =>
            ApiDataTable.fromDataTable(table)(None)(None)
          })

          val apiRecord: Option[ApiDataRecord] = structures match {
            case Seq() =>
              None
            case _ =>
              val values = result.map(_._1._1)
              val valueMap = Map(values map { value => value.fieldId -> ApiDataValue.fromDataValue(value) }: _*)
              val recordData = fillStructures(structures)(valueMap)

              // Retrieve and prepare the record itself
              val record = DataRecord.filter(_.id === recordId).run.head
              Some(ApiDataRecord.fromDataRecord(record)(Some(recordData)))
          }

          complete {
            apiRecord match {
              case Some(dataRecord) =>
                dataRecord
              case None =>
                (NotFound, s"Data Record $recordId not found")
            }
          }
        }
      }
    }
  }

  /*
   * Batch-insert data values as a list
   */
  def storeValueListApi = path("record" / IntNumber / "values") { (recordId: Int) =>
    post {
      (accessTokenHandler | userPassHandler) { implicit user: User =>
        println("Authenticated user submitting record values")
        entity(as[Seq[ApiDataValue]]) { values =>
          println("Authenticated user submitting PARSED record values")
          db.withSession { implicit session =>
            val maybeApiValues = values.map(x => createValue(x, None, Some(recordId)))
            val apiValues = Utils.flatten(maybeApiValues)

            complete {
              apiValues match {
                case Success(response) =>
                  (Created, response)
                case Failure(e) =>
                  (BadRequest, e.getMessage)
              }
            }
          }
        }
      }
    }
  }

  /*
   * Create (insert) a new data value
   */
  def createValueApi = path("value") {
    (accessTokenHandler | userPassHandler) { implicit user: User =>
      post {
        entity(as[ApiDataValue]) { value =>
          db.withSession { implicit session =>
            val inserted = createValue(value, None, None)

            complete {
              session.close()
              inserted match {
                case Success(insertedValue) =>
                  (Created, insertedValue)
                case Failure(e) =>
                  (BadRequest, e.getMessage)
              }

            }
          }
        }
      }
    }
  }

  /*
   * Retrieve a data value by ID
   */
  def getValueApi = path("value" / IntNumber) { (valueId: Int) =>
    userPassHandler { implicit user: User =>
      get {
        db.withSession { implicit session =>
          val valueQuery = for {
            value <- DataValue.filter(_.id === valueId)
            field <- value.dataFieldFk
            record <- value.dataRecordFk
          } yield (value, field, record)
          val someValue = valueQuery.run.headOption
          val apiValue = someValue map {
            case (value: DataValueRow, field: DataFieldRow, record: DataRecordRow) => {
              ApiDataValue.fromDataValue(value, Some(field), Some(record))
            }
          }

          complete {
            session.close()
            apiValue match {
              case Some(response) =>
                response
              case None =>
                (NotFound, s"Data value $valueId not found")
            }
          }
        }
      }
    }
  }

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
        .sortBy(_.recordId.asc)
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

      dataRecords
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

  private def findTable(name: String, source: String)(implicit session: Session): Option[ApiDataTable] = {
    val dbDataTable = DataTable.filter(_.sourceName === source).filter(_.name === name).run.headOption
    dbDataTable map { table =>
      ApiDataTable.fromDataTable(table)(None)(None)
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
  private def retrieveDataFieldId(fieldId: Int)(implicit session: Session): Option[ApiDataField] = {
    val field = DataField.filter(_.id === fieldId).run.headOption
    field map { dataField =>
      ApiDataField.fromDataField(dataField)
    }
  }
}