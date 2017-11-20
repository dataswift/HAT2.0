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

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.{ Clock, PasswordHasherRegistry }
import org.hatdex.hat.api.json.DataDebitFormats
import org.hatdex.hat.api.models.{ DataDebitOwner, Owner, Platform, User, _ }
import org.hatdex.hat.api.service.{ DataDebitService, StatsService }
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.dal.{ ModelTranslation, Tables }
import org.hatdex.hat.resourceManagement.{ HatServer, HatServerProvider }
import org.hatdex.hat.utils.HatBodyParsers
import org.joda.time.{ DateTime, LocalDateTime }
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// this trait defines our service behavior independently from the service actor
class DataDebitController @Inject() (
    components: ControllerComponents,
    passwordHasherRegistry: PasswordHasherRegistry,
    configuration: Configuration,
    silhouette: Silhouette[HatApiAuthEnvironment],
    hatServerProvider: HatServerProvider,
    dataDebitService: DataDebitService,
    statsService: StatsService,
    hatBodyParsers: HatBodyParsers,
    clock: Clock) extends HatApiController(components, silhouette, clock, hatServerProvider, configuration) with DataDebitFormats {

  val logger = Logger(this.getClass)

  def proposeDataDebit =
    SecuredAction(WithRole(Owner(), Platform(), DataDebitOwner(""))).async(hatBodyParsers.json[ApiDataDebit]) { implicit request =>
      val debit = request.body
      (debit.kind, debit.bundleContextless, debit.bundleContextual) match {
        case ("contextless", Some(bundle), None) => processContextlessDDProposal(debit, bundle)
        case ("contextual", None, Some(bundle))  => processContextualDDProposal(debit, bundle)
        case _                                   => Future.successful(BadRequest(Json.toJson(ErrorMessage("Request to create a data debit is malformed", "Data debit must be for contextual or contextless data and have associated bundle defined"))))
      }
    }

  private def processContextlessDDProposal(debit: ApiDataDebit, bundle: ApiBundleContextless)(implicit hatServer: HatServer, user: User) = {
    val eventualDataDebit = dataDebitService.storeContextlessDataDebit(debit, bundle)

    eventualDataDebit map {
      case createdDebit =>
        statsService.recordDataDebitOperation(createdDebit, user, DataDebitOperations.Create(), "Contextless Data Debit created")
          .recover { case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}") }
        Created(Json.toJson(createdDebit))

    } recover {
      case e =>
        BadRequest(Json.toJson(ErrorMessage("Request to create a contextless data debit is malformed", e.getMessage)))
    }
  }

  private def processContextualDDProposal(debit: ApiDataDebit, bundle: ApiBundleContext)(implicit hatServer: HatServer, user: User) = {
    val eventualDataDebit = dataDebitService.storeContextDataDebit(debit, bundle)
    eventualDataDebit map { createdDebit =>
      statsService.recordDataDebitOperation(createdDebit, user, DataDebitOperations.Create(), "Contextual Data Debit created")
        .recover {
          case e =>
            logger.error(s"Error while recording data debit operation: ${e.getMessage}")
            throw e
        }
    }

    eventualDataDebit map {
      case dataDebit => Created(Json.toJson(dataDebit))
    } recover {
      case e => BadRequest(Json.toJson(ErrorMessage("Request to create a contextual data debit is malformed", e.getMessage)))
    }
  }

  def enableDataDebit(dataDebitKey: UUID) = SecuredAction(WithRole(Owner())).async { implicit request =>
    dataDebitService.findDataDebitByKey(dataDebitKey) flatMap { dataDebit =>
      // TODO check permissions
      val result = dataDebit.map(dataDebitService.enableDataDebit)
        .map(eventualDD => eventualDD.map(dd => Some(dd)))
        .getOrElse(Future.successful(None))

      dataDebit.foreach { dd =>
        statsService.recordDataDebitOperation(
          ModelTranslation.fromDbModel(dd),
          ModelTranslation.fromInternalModel(request.identity),
          DataDebitOperations.Enable(), "Data Debit enabled") recover {
            case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}")
          }
      }
      result
    } map {
      case None        => NotFound(Json.toJson(ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found")))
      case Some(debit) => Ok(Json.toJson(SuccessResponse("Data Debit enabled")))
    } recover {
      case e: SecurityException => Forbidden(Json.toJson(ErrorMessage("Forbidden", e.getMessage)))
      case e                    => BadRequest(Json.toJson(ErrorMessage("Error enabling DataDebit", e.getMessage)))
    }
  }

  def disableDataDebit(dataDebitKey: UUID) = SecuredAction(WithRole(Owner())).async { implicit request =>
    dataDebitService.findDataDebitByKey(dataDebitKey) flatMap { dataDebit =>
      // TODO check permissions
      val result = dataDebit.map(dataDebitService.disableDataDebit)
        .map(eventualDD => eventualDD.map(dd => Some(dd)))
        .getOrElse(Future.successful(None))

      dataDebit.foreach { dd =>
        statsService.recordDataDebitOperation(
          ModelTranslation.fromDbModel(dd),
          ModelTranslation.fromInternalModel(request.identity),
          DataDebitOperations.Disable(), "Data Debit disabled") recover {
            case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}")
          }
      }
      result
    } map {
      case None        => NotFound(Json.toJson(ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found")))
      case Some(debit) => Ok(Json.toJson(SuccessResponse("Data Debit disabled")))
    } recover {
      case e: SecurityException => Forbidden(Json.toJson(ErrorMessage("Forbidden", e.getMessage)))
      case e                    => BadRequest(Json.toJson(ErrorMessage("Error disabling DataDebit", e.getMessage)))
    }
  }

  def retrieveDataDebitValues(dataDebitKey: UUID, limit: Option[Int], startTime: Option[Long], endTime: Option[Long],
    pretty: Option[Boolean]) = SecuredAction(WithRole(Owner(), DataDebitOwner(""))).async { implicit request =>
    val maybeStartTime = startTime.map(t => new DateTime(t * 1000L).toLocalDateTime)
    val maybeEndTime = endTime.map(t => new DateTime(t * 1000L).toLocalDateTime)

    dataDebitService.findDataDebitByKey(dataDebitKey) flatMap {
      case Some(dataDebit) if dataDebit.recipientId == request.identity.userId.toString || WithRole.isAuthorized(request.identity, request.authenticator, Owner()) =>
        Future.successful(dataDebit)
      case Some(dataDebit) => Future.failed(new SecurityException("No permissions to access the DataDebit"))
      case None            => Future.failed(new RuntimeException("No such Data Debit exists"))
    } flatMap { dataDebit =>
      (dataDebit.kind, dataDebit.bundleContextlessId, dataDebit.bundleContextId) match {
        case ("contextless", Some(bundleId), None) => getContextlessDataDebitValues(dataDebit, bundleId, limit, maybeStartTime, maybeEndTime)
        case ("contextual", None, Some(bundleId))  => getContextualDataDebitValues(dataDebit, bundleId, limit, maybeStartTime, maybeEndTime)
        case _                                     => Future.failed(new RuntimeException(s"Data Debit ${dataDebit.dataDebitKey} is malformed"))
      }
    } map {
      case data => Ok(Json.toJson(data))
    } recover {
      case e: SecurityException => Forbidden(Json.toJson(ErrorMessage("Forbidden", e.getMessage)))
      case e                    => BadRequest(Json.toJson(ErrorMessage("Bad Request", e.getMessage)))
    }
  }

  private def getContextlessDataDebitValues(dataDebit: Tables.DataDebitRow, bundleId: Int,
    maybeLimit: Option[Int], maybeStartTime: Option[LocalDateTime],
    maybeEndTime: Option[LocalDateTime])(implicit hatServer: HatServer, user: User): Future[ApiDataDebitOut] = {
    val eventualValues = dataDebitService.retrieveDataDebiValues(dataDebit, bundleId, maybeLimit, maybeStartTime, maybeEndTime)
    eventualValues map { values =>
      val apiDataDebit = ModelTranslation.fromDbModel(dataDebit)
      statsService.recordDataDebitRetrieval(apiDataDebit, values, user, "Contextless Data Debit Retrieved") recover {
        case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}")
      }
      values
    }
  }

  private def getContextualDataDebitValues(dataDebit: Tables.DataDebitRow, bundleId: Int,
    maybeLimit: Option[Int],
    maybeStartTime: Option[LocalDateTime],
    maybeEndTime: Option[LocalDateTime])(implicit hatServer: HatServer, user: User): Future[ApiDataDebitOut] = {
    val eventualValues = dataDebitService.retrieveDataDebitContextualValues(dataDebit, bundleId)
    eventualValues map { values =>
      val apiDataDebit = ModelTranslation.fromDbModel(dataDebit)
      statsService.recordDataDebitRetrieval(apiDataDebit, values.bundleContextual.getOrElse(Seq()), user, "Contextual Data Debit Retrieved") recover {
        case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}")
      }
      values
    }
  }

  def getDataDebit(dataDebitKey: UUID) = SecuredAction(WithRole(Owner())).async { implicit request =>
    dataDebitService.findDataDebitByKey(dataDebitKey)
      .map(dd => dd.map(ModelTranslation.fromDbModel))
      .map {
        case Some(debit) => Ok(Json.toJson(debit))
        case None        => NotFound(Json.toJson(ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found")))
      }
      .recover {
        case e: SecurityException => Forbidden(Json.toJson(ErrorMessage("Forbidden", e.getMessage)))
        case e                    => BadRequest(Json.toJson(ErrorMessage("Error getting DataDebit", e.getMessage)))
      }
  }

  def listDataDebits = SecuredAction(WithRole(Owner())).async { implicit request =>
    dataDebitService.listDataDebits map {
      apiDataDebits => Ok(Json.toJson(apiDataDebits))
    } recover {
      case e => InternalServerError(Json.toJson(ErrorMessage("Error while listing Data Debits", "Unknown error")))
    }
  }

  def rollDataDebitApi(dataDebitKey: UUID) = SecuredAction(WithRole(Owner(), Platform())).async { implicit request =>

    val resp = dataDebitService.findDataDebitByKey(dataDebitKey) flatMap { dataDebit =>
      val result = dataDebit.map(dataDebitService.rollDataDebit)
        .map(eventualDD => eventualDD.map(dd => Some(dd)))
        .getOrElse(Future.successful(None))

      dataDebit.foreach { dd =>
        statsService.recordDataDebitOperation(ModelTranslation.fromDbModel(dd), ModelTranslation.fromInternalModel(request.identity),
          DataDebitOperations.Roll(), "Data Debit rolled") recover {
            case e => logger.error(s"Error while recording data debit operation: ${e.getMessage}")
          }
      }
      result
    }

    resp map {
      case None    => NotFound(Json.toJson(ErrorMessage("DataDebit not Found", s"Data Debit $dataDebitKey not found")))
      case Some(_) => Ok(Json.toJson(SuccessResponse("Data Debit rolled")))
    } recover {
      case e: SecurityException => Forbidden(Json.toJson(ErrorMessage("Forbidden", e.getMessage)))
      case e                    => BadRequest(Json.toJson(ErrorMessage("Error enabling DataDebit", e.getMessage)))
    }
  }

}