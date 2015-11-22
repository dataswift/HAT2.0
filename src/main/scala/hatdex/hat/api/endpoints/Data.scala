package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.Utils
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.{ApiDataField, ApiDataRecord, ApiDataTable, ApiDataValue, _}
import hatdex.hat.api.service.DataService
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Success, Try}

// this trait defines our service behavior independently from the service actor
trait Data extends HttpService with DataService with HatServiceAuthHandler {
  //  import hatdex.hat.authentication.HatServiceAuthHandler._

  val logger: LoggingAdapter
  val db = DatabaseInfo.db

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
        getDataSourcesApi ~
        createTableApi ~
        linkTableToTableApi ~
        createFieldApi ~
        createRecordApi ~
        createRecordValuesApi ~
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
        logger.debug("POST /table")
        entity(as[ApiDataTable]) { table =>
          db.withSession { implicit session =>
            val tableStructure = createTable(table)
            session.close()
            complete {
              tableStructure match {
                case Success(structure) =>
                  (Created, structure)
                case Failure(e) =>
                  (BadRequest, ErrorMessage("Error creating Table", e.getMessage))
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
    post {
      logger.debug(s"POST /table/$parentId/table/$childId")
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        entity(as[ApiRelationship]) { relationship =>
          db.withSession { implicit session =>
            val inserted = linkTables(parentId, childId, relationship)
            session.close()
            complete {
              inserted match {
                case Success(id) =>
                  ApiGenericId(id)
                case Failure(e) =>
                  (BadRequest, ErrorMessage(s"Error linking Tables ${parentId} and ${childId}", e.getMessage))
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
    get {
      logger.debug(s"GET /table/$tableId")
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        db.withSession { implicit session =>
          val structure = getTableStructure(tableId)
          session.close()
          complete {
            structure
          }
        }
      }
    }
  }

  def findTableApi = path("table" / "search") {
    get {
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        logger.debug("GET /table/search")
        parameters('name, 'source) { (name: String, source: String) =>
          db.withSession { implicit session =>
            val table = findTable(name, source)
            session.close()
            complete {
              table
            }
          }
        }
      }
    }
  }

  def getTableValuesApi = path("table" / IntNumber / "values") { (tableId: Int) =>
    get {
      logger.debug(s"GET /table/$tableId/values")
      userPassHandler { implicit user: User =>
        db.withSession { implicit session =>
          val someTableValues = getTableValues(tableId)
          session.close()
          complete {
            someTableValues match {
              case Some(tableValues) =>
                tableValues
              case None =>
                (NotFound, ErrorMessage("NotFound", s"Table $tableId not found"))
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
    post {
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        logger.debug("POST /field")
        entity(as[ApiDataField]) { field =>
          db.withSession { implicit session =>
            val insertedField = createField(field)
            session.close()
            complete {
              insertedField match {
                case Success(field) =>
                  (Created, field)
                case Failure(e) =>
                  (BadRequest, ErrorMessage("Error creating Field", e.getMessage))
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
    get {
      logger.debug(s"GET /field/$fieldId")
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        db.withSession { implicit session =>
          val dataField = retrieveDataFieldId(fieldId)
          session.close()
          complete {
             dataField match {
              case Some(field) =>
                field
              case None =>
                (NotFound, ErrorMessage("Field Not Found", s"Data field $fieldId not found"))
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
    get {
      logger.debug(s"GET /field/$fieldId/values")
      userPassHandler { implicit user: User =>
        db.withSession { implicit session =>
          val fieldValues = getFieldValues(fieldId)
          session.close()
          complete {
            fieldValues match {
              case Some(field) =>
                field
              case None =>
                (NotFound, ErrorMessage("Field Not Found", s"Data field $fieldId not found"))
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
    post {
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        entity(as[ApiDataRecord]) { record =>
          db.withSession { implicit session =>
            val newRecord = new DataRecordRow(0, LocalDateTime.now(), LocalDateTime.now(), record.name)
            val insertedRecord = Try((DataRecord returning DataRecord) += newRecord)
            session.close()
            complete {
              insertedRecord match {
                case Success(apiRecord) =>
                  (Created, ApiDataRecord.fromDataRecord(apiRecord)(None) )
                case Failure(e) =>
                  (BadRequest, ErrorMessage("Error creating Record", e.getMessage))
              }

            }
          }
        }
        }
    }
  }

  def createRecordValuesApi = path("record" / "values") {
    post {
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        // the more complicated case of putting in data and record for creation of both together
        entity(as[ApiRecordValues]) { recordValues =>
          db.withSession { implicit session =>
            val maybeRecordValues = storeRecordValues(recordValues)
            session.close()

            complete {
              maybeRecordValues match {
                case Success(recordValues) =>
                  (Created, recordValues)
                case Failure(e) =>
                  (BadRequest, ErrorMessage("Error creating Record with Values", e.getMessage))
              }
            }
          }
        } ~
        entity(as[Seq[ApiRecordValues]]) { recordValueList =>
          db.withSession { implicit session =>
            val listInsertedValues = recordValueList map { recordValues =>
              storeRecordValues(recordValues)
            }
            val maybeList = Utils.flatten(listInsertedValues)
            session.close()

            complete {
              maybeList match {
                case Success(list) =>
                  (Created, list)
                case Failure(e) =>
                  (BadRequest, ErrorMessage("Error creating a list of Records with Values", e.getMessage))
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
    get {
      logger.debug(s"GET /record/$recordId")
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        db.withSession { implicit session =>
          val record = DataRecord.filter(_.id === recordId).run.headOption
          session.close()
          complete {
            record match {
              case Some(dataRecord) =>
                ApiDataRecord.fromDataRecord(dataRecord)(None)
              case None =>
                (NotFound, ErrorMessage("Record Not Found", s"Data Record $recordId not found"))
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
      logger.debug(s"POST /record/$recordId/values")
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

          session.close()
          complete {
            apiRecord match {
              case Some(dataRecord) =>
                dataRecord
              case None =>
                (NotFound, ErrorMessage("Record Not Found", s"Data Record $recordId not found"))
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
      logger.debug(s"POST /record/$recordId/values")
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        logger.debug("Authenticated user submitting record values")
        entity(as[Seq[ApiDataValue]]) { values =>
          logger.debug("Authenticated user submitting PARSED record values")
          db.withSession { implicit session =>
            val maybeApiValues = values.map(x => createValue(x, None, Some(recordId)))
            val apiValues = Utils.flatten(maybeApiValues)
            session.close()
            complete {
              apiValues match {
                case Success(response) =>
                  (Created, response)
                case Failure(e) =>
                  (BadRequest, ErrorMessage("Error storing Record values", e.getMessage))
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
    post {
      (userPassHandler | accessTokenHandler) { implicit user: User =>
        logger.debug("POST /value")
        entity(as[ApiDataValue]) { value =>
          db.withSession { implicit session =>
            val inserted = createValue(value, None, None)

            complete {
              session.close()
              inserted match {
                case Success(insertedValue) =>
                  (Created, insertedValue)
                case Failure(e) =>
                  (BadRequest, ErrorMessage("Error storing value", e.getMessage))
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
    get {
      logger.debug(s"GET /value/$valueId")
      userPassHandler { implicit user: User =>
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
          session.close()
          complete {
            apiValue match {
              case Some(response) =>
                response
              case None =>
                (NotFound, ErrorMessage("Value Not Found", s"Data value $valueId not found"))
            }
          }
        }
      }
    }
  }

  def getDataSourcesApi = path("sources") {
    get {
      userPassHandler { implicit user =>
        logger.debug("GET /sources")
        db.withSession { implicit session =>
          val dataSources = getDataSources()
          session.close()
          complete {
            dataSources
          }
        }
      }
    }
  }
}