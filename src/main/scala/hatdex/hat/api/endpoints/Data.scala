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
package hatdex.hat.api.endpoints

import akka.actor.ActorRefFactory
import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.{ ApiDataField, ApiDataRecord, ApiDataTable, ApiDataValue, _ }
import hatdex.hat.api.service.{ StatsService, DataService }
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import org.joda.time.DateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._
import spray.json._

import scala.util.{ Success, Failure }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future }

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
            onComplete(createTable(table)) {
              case Success(structure) => complete { (Created, structure) }
              case Failure(e)         => complete { (BadRequest, ErrorMessage("Error creating Table", e.getMessage)) }
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
            onComplete(linkTables(parentId, childId, relationship)) {
              case Success(id) => complete { (OK, ApiGenericId(id)) }
              case Failure(e)  => complete { (BadRequest, ErrorMessage(s"Error linking Tables $parentId and $childId", e.getMessage)) }
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
          onComplete(getTableStructure(tableId)) {
            case Success(Some(structure)) => complete((OK, structure))
            case Success(None)            => complete((NotFound, ErrorMessage(s"No such table", s"Table $tableId not found")))
            case Failure(e)               => complete((NotFound, ErrorMessage(s"No such valid table found", e.getMessage)))
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
              val tables = (name, nameLike, nameUnlike, source) match {
                case (Some(tableName), _, _, Some(tableSource)) => findTable(tableName, tableSource)
                case (None, Some(tableName), _, Some(tableSource)) => findTablesLike(tableName, tableSource)
                case (None, None, Some(tableName), Some(tableSource)) => findTablesNotLike(tableName, tableSource)
                case _ => Future.failed(new IllegalArgumentException("No such table found"))
              }

              onComplete(tables) {
                case Success(createdTables) if createdTables.size > 1  => complete { (OK, createdTables) }
                case Success(createdTables) if createdTables.size == 1 => complete { (OK, createdTables.head) }
                case Success(createdTables) if createdTables.isEmpty   => complete { (NotFound, ErrorMessage("Table not found", s"No tables matching filters found")) }
                case Failure(e)                                        => complete { (NotFound, ErrorMessage("Table not found", e.getMessage)) }
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
          parameters('limit.as[Option[Int]], 'starttime.as[Option[Int]], 'endtime.as[Option[Int]], 'pretty.as[Option[Boolean]] ? false) {
            (maybeLimit: Option[Int], maybeStartTimestamp: Option[Int], maybeEndTimestamp: Option[Int], pretty: Boolean) =>

              val maybeStartTime = maybeStartTimestamp.map(t => new DateTime(t * 1000L).toLocalDateTime)
              val maybeEndTime = maybeEndTimestamp.map(t => new DateTime(t * 1000L).toLocalDateTime)
              val eventualRecordValues = getTableValues(tableId, maybeLimit, maybeStartTime, maybeEndTime)

              val maybeFlatRecords = if (pretty) {
                eventualRecordValues.map { records =>
                  val temp = records.map(flattenRecordValues)
                  temp.toJson.toString
                }
              }
              else {
                eventualRecordValues.map(_.toJson.toString)
              }

              onComplete(maybeFlatRecords) {
                case Success(values) => complete { (OK, values) }
                case Failure(e)      => complete { (NotFound, ErrorMessage("No such table with values", e.getMessage)) }
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
            onComplete(createField(field)) {
              case Success(createdField) => complete { (Created, createdField) }
              case Failure(e)            => complete { (BadRequest, ErrorMessage("Error creating Field", e.getMessage)) }
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
          onComplete(retrieveDataFieldId(fieldId)) {
            case Success(Some(field)) => complete { (OK, field) }
            case Success(None)        => complete { (NotFound, ErrorMessage("Field Not Found", s"Data field $fieldId not found")) }
            case Failure(e)           => complete { (NotFound, ErrorMessage("Field Not Found", e.getMessage)) }
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
          onComplete(getFieldValues(fieldId)) {
            case Success(Some(field)) => complete { (OK, field) }
            case Success(None)        => complete { (NotFound, ErrorMessage("Field Not Found", s"Data field $fieldId not found")) }
            case Failure(e)           => complete { (NotFound, ErrorMessage("Field Not Found", e.getMessage)) }
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
            onComplete(createRecord(record)) {
              case Success(apiRecord) => complete { (Created, apiRecord) }
              case Failure(e)         => complete { (BadRequest, ErrorMessage("Error creating Record", e.getMessage)) }
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
            val insertedRecord = storeRecordValues(Seq(recordValues)).map(_.head)
            insertedRecord map { record =>
              recordDataInbound(Seq(record), user, "Single Data Record Values set posted")
            }

            onComplete(insertedRecord) {
              case Success(created) => complete { (Created, created) }
              case Failure(e)       => complete { (BadRequest, ErrorMessage("Error creating Record with Values", e.getMessage)) }
            }
          } ~
            entity(as[Seq[ApiRecordValues]]) { recordValueList =>
              val insertedRecords = storeRecordValues(recordValueList)
              insertedRecords map { records =>
                recordDataInbound(records, user, "Multiple Data Record Values set posted")
              }

              onComplete(insertedRecords) {
                case Success(recordValues) => complete { (Created, recordValues) }
                case Failure(e)            => complete { (BadRequest, ErrorMessage("Error creating Record with Values", e.getMessage)) }
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
          onComplete(getRecordValues(recordId)) {
            case Success(Some(dataRecord)) => complete { (OK, dataRecord) }
            case Success(None)             => complete { (NotFound, ErrorMessage("Record Not Found", s"Data Record $recordId not found")) }
            case Failure(e)                => complete { (NotFound, ErrorMessage("Record Not Found", e.getMessage)) }
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
          entity(as[Seq[ApiDataValue]]) { values =>
            val eventualValues = Future.sequence(values.map(x => createValue(x, None, Some(recordId))))
            eventualValues map {
              case values =>
                recordDataValuesInbound(values, user, s"Data Values posted for record $recordId")
            }
            onComplete(eventualValues) {
              case Success(response) => complete { (Created, response) }
              case Failure(e)        => complete { (BadRequest, ErrorMessage("Error storing Record values", e.getMessage)) }
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
            val eventualValues = createValue(value, None, None)
            eventualValues map {
              case insertedValue =>
                recordDataValuesInbound(Seq(insertedValue), user, s"Single data value posted")
            }
            onComplete(eventualValues) {
              case Success(insertedValue) => complete { (Created, insertedValue) }
              case Failure(e)             => complete { (BadRequest, ErrorMessage("Error storing value", e.getMessage)) }
            }
          }
        }
      }
    } ~ put {
      accessTokenHandler { implicit user: User =>
        logger.info("PUT /value")
        authorize(UserAuthorization.withRole("owner")) {
          logger.info("PUT /value")
          entity(as[ApiDataValue]) { value =>
            val eventualValues = updateValue(value)
            eventualValues map {
              case insertedValue =>
                recordDataValuesInbound(Seq(insertedValue), user, s"Single data value updated")
            }
            onComplete(eventualValues) {
              case Success(insertedValue) => complete { (Created, insertedValue) }
              case Failure(e)             => complete { (BadRequest, ErrorMessage("Error storing value", e.getMessage)) }
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
          onComplete(getValue(valueId)) {
            case Success(Some(response)) => complete { (OK, response) }
            case Success(None)           => complete { (NotFound, ErrorMessage("Value Not Found", s"Data value $valueId not found")) }
            case Failure(e)              => complete { (NotFound, ErrorMessage("Value Not Found", e.getMessage)) }
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
          onComplete(getDataSources()) {
            case Success(dataSources) => complete { (OK, dataSources) }
            case Failure(e)           => complete { (InternalServerError, ErrorMessage("Internal Server Error", e.getMessage)) }
          }
        }
      }
    }
  }
}