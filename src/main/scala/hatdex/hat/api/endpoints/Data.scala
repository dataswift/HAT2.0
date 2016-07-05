package hatdex.hat.api.endpoints

import akka.actor.ActorRefFactory
import akka.event.LoggingAdapter
import hatdex.hat.Utils
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.{ ApiDataField, ApiDataRecord, ApiDataTable, ApiDataValue, _ }
import hatdex.hat.api.service.{ StatsService, DataService }
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import org.joda.time.DateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.collection.mutable
import scala.collection.mutable.Set
import scala.util.{ Failure, Success, Try }

// this trait defines our service behavior independently from the service actor
trait Data extends HttpService with DataService with StatsService with HatServiceAuthHandler {
  //  import hatdex.hat.authentication.HatServiceAuthHandler._

  val logger: LoggingAdapter
  val db = DatabaseInfo.db
  def actorRefFactory: ActorRefFactory

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
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataCredit")) {
          logger.debug("POST /table")
          entity(as[ApiDataTable]) { table =>
            db.withSession { implicit session =>
              val tableStructure = createTable(table)
              session.close()
              complete {
                tableStructure match {
                  case Success(structure) => (Created, structure)
                  case Failure(e)         => (BadRequest, ErrorMessage("Error creating Table", e.getMessage))
                }
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
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataCredit")) {
          entity(as[ApiRelationship]) { relationship =>
            db.withSession { implicit session =>
              val inserted = linkTables(parentId, childId, relationship)
              session.close()
              complete {
                inserted match {
                  case Success(id) => ApiGenericId(id)
                  case Failure(e)  => (BadRequest, ErrorMessage(s"Error linking Tables ${parentId} and ${childId}", e.getMessage))
                }
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
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataCredit")) {
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
  }

  def findTableApi = path("table") {
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataCredit")) {
          logger.debug("GET /table")
          parameters('name.?, 'namelike.?, 'nameunlike.?, 'source.?) {
            (name: Option[String], nameLike: Option[String], nameUnlike: Option[String], source: Option[String]) =>
              db.withSession { implicit session =>
                val tables = (name, nameLike, nameUnlike, source) match {
                  case (Some(tableName), _, _, Some(tableSource)) => findTable(tableName, tableSource)
                  case (None, Some(tableName), _, Some(tableSource)) => findTablesLike(tableName, tableSource)
                  case (None, None, Some(tableName), Some(tableSource)) => findTablesNotLike(tableName, tableSource)
                  case _ => Seq[ApiDataTable]()
                }

                session.close()

                val maybeTables = Utils.seqOption(tables)

                complete {
                  maybeTables match {
                    case Some(tables) if tables.size > 1 => tables
                    case Some(tables)                    => tables.headOption
                    case None                            => (NotFound, ErrorMessage("Table not found", s"Table with name=$name (namelike=$nameLike, nameUnlike=$nameUnlike) and source=$source was not found"))
                  }
                }
              }
          }
        }
      }
    }
  }

  def getTableValuesApi = path("table" / IntNumber / "values") { (tableId: Int) =>
    get {
      logger.debug(s"GET /table/$tableId/values")
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          parameters('limit.as[Option[Int]], 'starttime.as[Option[Int]], 'endtime.as[Option[Int]]) {
            (maybeLimit: Option[Int], maybeStartTimestamp: Option[Int], maybeEndTimestamp: Option[Int]) =>
              db.withSession { implicit session =>
                val maybeStartTime = maybeStartTimestamp.map(t => new DateTime(t * 1000L).toLocalDateTime)
                val maybeEndTime = maybeEndTimestamp.map(t => new DateTime(t * 1000L).toLocalDateTime)
                val someTableValues = getTableValues(tableId, maybeLimit, maybeStartTime, maybeEndTime)
                session.close()
                complete {
                  someTableValues match {
                    case Some(tableValues) => tableValues
                    case None => (NotFound, ErrorMessage("NotFound", s"Table $tableId not found"))
                  }
                }
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
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataCredit")) {
          logger.debug("POST /field")
          entity(as[ApiDataField]) { field =>
            db.withSession { implicit session =>
              val insertedField = createField(field)
              session.close()
              complete {
                insertedField match {
                  case Success(field) => (Created, field)
                  case Failure(e)     => (BadRequest, ErrorMessage("Error creating Field", e.getMessage))
                }
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
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataCredit")) {
          db.withSession { implicit session =>
            val dataField = retrieveDataFieldId(fieldId)
            session.close()
            complete {
              dataField match {
                case Some(field) => field
                case None        => (NotFound, ErrorMessage("Field Not Found", s"Data field $fieldId not found"))
              }
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
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          db.withSession { implicit session =>
            val fieldValues = getFieldValues(fieldId)
            session.close()
            complete {
              fieldValues match {
                case Some(field) => field
                case None        => (NotFound, ErrorMessage("Field Not Found", s"Data field $fieldId not found"))
              }
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
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataCredit")) {
          entity(as[ApiDataRecord]) { record =>
            db.withSession { implicit session =>
              val newRecord = new DataRecordRow(0, LocalDateTime.now(), LocalDateTime.now(), record.name)
              val insertedRecord = Try((DataRecord returning DataRecord) += newRecord)
              session.close()
              complete {
                insertedRecord match {
                  case Success(apiRecord) => (Created, ApiDataRecord.fromDataRecord(apiRecord)(None))
                  case Failure(e)         => (BadRequest, ErrorMessage("Error creating Record", e.getMessage))
                }
              }
            }
          }
        }
      }
    }
  }

  def createRecordValuesApi = path("record" / "values") {
    post {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataCredit")) {
          // the more complicated case of putting in data and record for creation of both together
          entity(as[ApiRecordValues]) { recordValues =>
            db.withSession { implicit session =>
              val maybeRecordValues = storeRecordValues(recordValues)
              session.close()

              complete {
                maybeRecordValues match {
                  case Success(recordValues) =>
                    recordDataInbound(Seq(recordValues), user, "Single Data Record Values set posted")
                    (Created, recordValues)
                  case Failure(e) =>
                    (BadRequest, ErrorMessage("Error creating Record with Values", e.getMessage))
                }
              }
            }
          } ~
            entity(as[Seq[ApiRecordValues]]) { recordValueList =>
              db.withSession { implicit session =>
                val listInsertedValues = recordValueList map storeRecordValues
                val maybeList = Utils.flatten(listInsertedValues)
                session.close()

                complete {
                  maybeList match {
                    case Success(list) =>
                      recordDataInbound(list, user, "Single Data Record Values set posted")
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
  }

  /*
   * Get record
   */
  def getRecordApi = path("record" / IntNumber) { (recordId: Int) =>
    get {
      logger.debug(s"GET /record/$recordId")
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataCredit")) {
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
  }

  /*
   * Get values associated with a record.
   * Constructs a hierarchy of fields and data within each field for the record
   */
  def getRecordValuesApi = path("record" / IntNumber / "values") { (recordId: Int) =>
    get {
      logger.debug(s"POST /record/$recordId/values")
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          db.withSession { implicit session =>
            // Retrieve joined Data Values, Fields and Tables
            val fieldValuesTables = DataValue.filter(_.recordId === recordId) join
              DataField on (_.fieldId === _.id) join
              DataTable on (_._2.tableIdFk === _.id)

            val result = fieldValuesTables.run

            val structures = getStructures(result.map(_._2).map { table => ApiDataTable.fromDataTable(table)(None)(None) })

            val apiRecord: Option[ApiDataRecord] = structures match {
              case Seq() => None
              case _ =>
                val values = result.map(_._1._1)
                //              val valueMap = Map(values map { value => value.fieldId -> ApiDataValue.fromDataValue(value) }: _*)
                val valueMap = new mutable.HashMap[Int, Set[ApiDataValue]] with mutable.MultiMap[Int, ApiDataValue]
                values foreach { value =>
                  valueMap.addBinding(value.fieldId, ApiDataValue.fromDataValue(value))
                }
                val recordData = fillStructures(structures)(valueMap)

                // Retrieve and prepare the record itself
                val record = DataRecord.filter(_.id === recordId).run.head
                Some(ApiDataRecord.fromDataRecord(record)(Some(recordData)))
            }

            session.close()
            complete {
              apiRecord match {
                case Some(dataRecord) => dataRecord
                case None             => (NotFound, ErrorMessage("Record Not Found", s"Data Record $recordId not found"))
              }
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
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataCredit")) {
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
                    recordDataValuesInbound(response, user, s"Data Values posted for record $recordId")
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
  }

  /*
   * Create (insert) a new data value
   */
  def createValueApi = path("value") {
    post {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataCredit")) {
          logger.debug("POST /value")
          entity(as[ApiDataValue]) { value =>
            db.withSession { implicit session =>
              val inserted = createValue(value, None, None)

              complete {
                session.close()
                inserted match {
                  case Success(insertedValue) =>
                    recordDataValuesInbound(Seq(insertedValue), user, s"Single data value posted")
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
  }

  /*
   * Retrieve a data value by ID
   */
  def getValueApi = path("value" / IntNumber) { (valueId: Int) =>
    get {
      logger.debug(s"GET /value/$valueId")
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
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
                case Some(response) => response
                case None           => (NotFound, ErrorMessage("Value Not Found", s"Data value $valueId not found"))
              }
            }
          }
        }
      }
    }
  }

  def getDataSourcesApi = path("sources") {
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataCredit")) {
          logger.debug("GET /sources")
          db.withSession { implicit session =>
            val dataSources = getDataSources()
            session.close()
            complete { dataSources }
          }
        }
      }
    }
  }
}