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

package org.hatdex.hat.api.controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.{ Clock, PasswordHasherRegistry }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.DataService
import org.hatdex.hat.authentication.{ HatApiController, WithRole, _ }
import org.hatdex.hat.resourceManagement._
import org.joda.time.DateTime
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.Future

// this trait defines our service behavior independently from the service actor
class Data @Inject() (
    val messagesApi: MessagesApi,
    passwordHasherRegistry: PasswordHasherRegistry,
    configuration: Configuration,
    credentialsProvider: CredentialsProvider[HatServer],
    silhouette: Silhouette[HatApiAuthEnvironment],
    hatServerProvider: HatServerProvider,
    clock: Clock,
    dataService: DataService) extends HatApiController(silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  val logger = Logger(this.getClass)

  /**
   * Creates a new virtual table for storing arbitrary incoming data
   */
  def createTable(): Action[ApiDataTable] =
    SecuredAction(WithRole("owner", "platform", "dataCredit")).async(BodyParsers.parse.json[ApiDataTable]) { implicit request =>
      dataService.createTable(request.body) map {
        case structure => Created(Json.toJson(structure))
      } recover {
        case e => BadRequest(Json.toJson(ErrorMessage("Error creating Table", e.getMessage)))
      }
    }

  /**
   * Marks provided table as a "child" of another, e.g. to created nested data structured
   */
  def linkTables(parentId: Int, childId: Int): Action[ApiRelationship] =
    SecuredAction(WithRole("owner", "platform", "dataCredit")).async(BodyParsers.parse.json[ApiRelationship]) { implicit request =>
      dataService.linkTables(parentId, childId, request.body) map { id =>
        Created(Json.toJson(ApiGenericId(id)))
      } recover {
        case e => BadRequest(Json.toJson(ErrorMessage(s"Error linking Tables $parentId and $childId", e.getMessage)))
      }
    }

  /**
   * Get specific table information. Includes all fields and sub-tables
   */
  def getTable(tableId: Int) =
    SecuredAction(WithRole("owner", "platform", "dataCredit")).async { implicit request =>
      dataService.getTableStructure(tableId) map {
        case Some(structure) => Ok(Json.toJson(structure))
        case None            => NotFound(Json.toJson(ErrorMessage(s"No such table", s"Table $tableId not found")))
      } recover {
        case e => BadRequest(Json.toJson(ErrorMessage(s"No such valid table found", e.getMessage)))
      }
    }

  /**
   * Looks up a table for a provided source and name, also supporting name search
   * with LIKE and UNLIKE operators)
   *
   */
  def findTable(name: Option[String], nameLike: Option[String], nameUnlike: Option[String], tableSource: String) =
    SecuredAction(WithRole("owner", "platform", "dataCredit")).async { implicit request =>
      logger.debug("GET /table")
      val tables = (name, nameLike, nameUnlike) match {
        case (Some(tableName), _, _)       => dataService.findTable(tableName, tableSource)
        case (None, Some(tableName), _)    => dataService.findTablesLike(tableName, tableSource)
        case (None, None, Some(tableName)) => dataService.findTablesNotLike(tableName, tableSource)
        case _                             => Future.failed(new IllegalArgumentException("No such table found"))
      }

      val eventualTableStructures = tables flatMap { tables =>
        val eventualStructures = tables.flatMap(t => t.id.map(dataService.getTableStructure))
        Future.sequence(eventualStructures).map(_.flatten)
      }

      eventualTableStructures map {
        case createdTables if createdTables.size > 1  => Ok(Json.toJson(createdTables))
        case createdTables if createdTables.size == 1 => Ok(Json.toJson(createdTables.head))
        case createdTables if createdTables.isEmpty   => NotFound(Json.toJson(ErrorMessage("Table not found", s"No tables matching filters found")))
      } recover {
        case e => NotFound(Json.toJson(ErrorMessage("Table not found", e.getMessage)))
      }
    }

  /**
   * TBD
   */
  def getTableValues(tableId: Int, limit: Option[Int], startTime: Option[Long], endTime: Option[Long], pretty: Option[Boolean]) =
    SecuredAction(WithRole("owner")).async { implicit request =>
      val maybeStartTime = startTime.map(t => new DateTime(t * 1000L).toLocalDateTime)
      val maybeEndTime = endTime.map(t => new DateTime(t * 1000L).toLocalDateTime)
      val eventualRecordValues = dataService.getTableValues(tableId, limit, maybeStartTime, maybeEndTime)

      eventualRecordValues map {
        case records if pretty.contains(true) => Ok(Json.toJson(records.map(HatJsonFormats.flattenRecordValues)))
        case records                          => Ok(Json.toJson(records))
      } recover {
        case e => NotFound(Json.toJson(ErrorMessage("No such table with values", e.getMessage)))
      }
    }

  /**
   * Create a new field in a virtual table
   */
  def createField(): Action[ApiDataField] =
    SecuredAction(WithRole("owner", "platform", "dataCredit")).async(BodyParsers.parse.json[ApiDataField]) { implicit request =>
      logger.debug("POST /table")
      dataService.createField(request.body) map {
        case field => Created(Json.toJson(field))
      } recover {
        case e => BadRequest(Json.toJson(ErrorMessage("Error creating Field", e.getMessage)))
      }
    }

  /**
   * Get field (information only) by ID
   */
  def getField(fieldId: Int) =
    SecuredAction(WithRole("owner", "platform", "dataCredit")).async { implicit request =>
      dataService.retrieveDataFieldId(fieldId) map {
        case Some(field) => Ok(Json.toJson(field))
        case None        => NotFound(Json.toJson(ErrorMessage("Field Not Found", s"Data field $fieldId not found")))
      } recover {
        case e => NotFound(Json.toJson(ErrorMessage("Field Not Found", e.getMessage)))
      }
    }

  /**
   * Get data stored in a specific field.
   * Returns all Data Values stored in the field
   */
  def getFieldValues(fieldId: Int) =
    SecuredAction(WithRole("owner")).async { implicit request =>
      dataService.getFieldValues(fieldId) map {
        case Some(field) => Ok(Json.toJson(field))
        case None        => NotFound(Json.toJson(ErrorMessage("Field Not Found", s"Data field $fieldId not found")))
      } recover {
        case e => NotFound(Json.toJson(ErrorMessage("Field Not Found", e.getMessage)))
      }
    }

  /*
   * Insert a new, potentially named, data record
   */
  def createRecord(): Action[ApiDataRecord] =
    SecuredAction(WithRole("owner", "platform", "dataCredit")).async(BodyParsers.parse.json[ApiDataRecord]) { implicit request =>
      logger.debug("POST /record")
      dataService.createRecord(request.body) map {
        case record => Created(Json.toJson(record))
      } recover {
        case e => BadRequest(Json.toJson(ErrorMessage("Error creating Record", e.getMessage)))
      }
    }

  def createRecordValues =
    SecuredAction(WithRole("owner", "platform", "dataCredit")).async(BodyParsers.parse.json) { implicit request =>
      val recordValues = request.body
      val insertedRecord = recordValues.validate[ApiRecordValues] match {
        case recordValues: JsSuccess[ApiRecordValues] => dataService.storeRecordValues(Seq(recordValues.value)).map(v => Json.toJson(v.head))
        case e: JsError                               => Future.failed(new RuntimeException(s"Record value parsing failed: ${e.toString}"))
      }

      val insertedRecords = insertedRecord.recoverWith {
        case e: RuntimeException =>
          recordValues.validate[Seq[ApiRecordValues]] match {
            case recordValues: JsSuccess[Seq[ApiRecordValues]] => dataService.storeRecordValues(recordValues.value).map(v => Json.toJson(v))
            case e: JsError                                    => Future.failed(new RuntimeException(s"Record value parsing failed: ${e.toString}"))
          }
      }

      //      insertedRecord map { record =>
      //        recordDataInbound(Seq(record), user, "Single Data Record Values set posted")
      //      }
      insertedRecords map {
        case json => Created(json)
      } recover {
        case e => BadRequest(Json.toJson(ErrorMessage("Error creating Record with Values", e.getMessage)))
      }
    }

  /*
   * Get values associated with a record.
   * Constructs a hierarchy of fields and data within each field for the record
   */
  def getRecordValues(recordId: Int) = SecuredAction(WithRole("owner")).async { implicit request =>
    dataService.getRecordValues(recordId) map {
      case Some(dataRecord) => Ok(Json.toJson(dataRecord))
      case None             => NotFound(Json.toJson(ErrorMessage("Record Not Found", s"Data Record $recordId not found")))
    } recover {
      case e => NotFound(Json.toJson(ErrorMessage("Record Not Found", e.getMessage)))
    }
  }

  /*
   * Batch-insert data values as a list
   */
  def storeValueList(recordId: Int): Action[Seq[ApiDataValue]] =
    SecuredAction(WithRole("owner", "platform", "dataCredit")).async(BodyParsers.parse.json[Seq[ApiDataValue]]) { implicit request =>
      val eventualValues = Future.sequence(request.body.map(x => dataService.createValue(x, None, Some(recordId))))

      //      eventualValues map {
      //        case values =>
      //          recordDataValuesInbound(values, user, s"Data Values posted for record $recordId")
      //      }
      eventualValues map {
        case values => Created(Json.toJson(values))
      } recover {
        case e => BadRequest(Json.toJson(ErrorMessage("Error storing Record values", e.getMessage)))
      }
    }

  /*
   * Create (insert) a new data value
   */
  def createValue: Action[ApiDataValue] =
    SecuredAction(WithRole("owner", "platform", "dataCredit")).async(BodyParsers.parse.json[ApiDataValue]) { implicit request =>
      val eventualValues = dataService.createValue(request.body, None, None)
      //      eventualValues map {
      //        case inserted => recordDataValuesInbound(Seq(insertedValue), user, s"Single data value posted")
      //      }
      eventualValues map {
        case inserted => Created(Json.toJson(inserted))
      } recover {
        case e => BadRequest(Json.toJson(ErrorMessage("Error storing value", e.getMessage)))
      }
    }

  def getDataSources = SecuredAction(WithRole("owner", "platform", "dataCredit")).async { implicit request =>
    dataService.dataSources() map {
      case dataSources => Ok(Json.toJson(dataSources))
    } recover {
      case e => InternalServerError(Json.toJson(ErrorMessage("Internal Server Error", e.getMessage)))
    }
  }

  def deleteDataValue(valueId: Int) = SecuredAction(WithRole("owner", "dataCredit")).async { implicit request =>
    dataService.deleteValue(valueId) map {
      case _ => Ok(Json.toJson(SuccessResponse(s"Value $valueId deleted")))
    } recover {
      case e => InternalServerError(Json.toJson(ErrorMessage("Internal Server Error", e.getMessage)))
    }
  }

  def deleteDataField(fieldId: Int) = SecuredAction(WithRole("owner", "dataCredit")).async { implicit request =>
    dataService.deleteField(fieldId) map {
      case _ => Ok(Json.toJson(SuccessResponse(s"Field $fieldId deleted")))
    } recover {
      case e => InternalServerError(Json.toJson(ErrorMessage("Internal Server Error", e.getMessage)))
    }
  }

  def deleteDataTable(tableId: Int) = SecuredAction(WithRole("owner", "dataCredit")).async { implicit request =>
    dataService.deleteTable(tableId) map {
      case _ => Ok(Json.toJson(SuccessResponse(s"Table $tableId deleted")))
    } recover {
      case e => InternalServerError(Json.toJson(ErrorMessage("Internal Server Error", e.getMessage)))
    }
  }

  def deleteDataRecord(recordId: Int) = SecuredAction(WithRole("owner", "dataCredit")).async { implicit request =>
    dataService.deleteRecord(recordId) map {
      case _ => Ok(Json.toJson(SuccessResponse(s"Record $recordId deleted")))
    } recover {
      case e => InternalServerError(Json.toJson(ErrorMessage("Internal Server Error", e.getMessage)))
    }
  }
}