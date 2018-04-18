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
 * 4 / 2018
 */

package org.hatdex.hat.api.controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import org.hatdex.hat.api.json.RichDataJsonFormats
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.applications.ApplicationsService
import org.hatdex.hat.api.service.monitoring.HatDataEventDispatcher
import org.hatdex.hat.api.service.richData._
import org.hatdex.hat.authentication.{ ContainsApplicationRole, HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.utils.{ HatBodyParsers, LoggingProvider }
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

class DataDebits @Inject() (
    components: ControllerComponents,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    dataEventDispatcher: HatDataEventDispatcher,
    dataService: RichDataService,
    dataDebitService: DataDebitService,
    loggingProvider: LoggingProvider,
    implicit val ec: ExecutionContext,
    implicit val applicationsService: ApplicationsService) extends HatApiController(components, silhouette) with RichDataJsonFormats {

  private val logger = loggingProvider.logger(this.getClass)

  def registerDataDebit(dataDebitId: String): Action[DataDebitSetupRequest] =
    SecuredAction(WithRole(Owner(), DataDebitOwner(""), Platform()) || ContainsApplicationRole(Owner(), DataDebitOwner(""), Platform())).async(parsers.json[DataDebitSetupRequest]) { implicit request =>
      dataDebitService.createDataDebit(dataDebitId, request.body, request.identity.userId)
        .andThen(dataEventDispatcher.dispatchEventDataDebit(DataDebitOperations.Create()))
        .map(debit => Created(Json.toJson(debit)))
        .recover {
          case err: RichDataDuplicateBundleException => BadRequest(Json.toJson(Errors.dataDebitMalformed(err)))
          case err: RichDataDuplicateDebitException  => BadRequest(Json.toJson(Errors.dataDebitMalformed(err)))
        }
    }

  def updateDataDebit(dataDebitId: String): Action[DataDebitSetupRequest] =
    SecuredAction(WithRole(Owner(), DataDebitOwner(dataDebitId)) || ContainsApplicationRole(Owner(), DataDebitOwner(dataDebitId))).async(parsers.json[DataDebitSetupRequest]) { implicit request =>
      dataDebitService.updateDataDebitPermissions(dataDebitId, request.body, request.identity.userId)
        .andThen(dataEventDispatcher.dispatchEventDataDebit(DataDebitOperations.Change()))
        .map(debit => Ok(Json.toJson(debit)))
        .recover {
          case err: RichDataServiceException => BadRequest(Json.toJson(Errors.dataDebitMalformed(err)))
        }
    }

  def getDataDebit(dataDebitId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner(), DataDebitOwner(dataDebitId)) || ContainsApplicationRole(Owner(), DataDebitOwner(dataDebitId))).async { implicit request =>
      logger.warn("Running new controller")
      dataDebitService.dataDebit(dataDebitId)
        .map {
          case Some(debit) => Ok(Json.toJson(debit))
          case None        => NotFound(Json.toJson(Errors.dataDebitNotFound(dataDebitId)))
        }
    }

  def getDataDebitValues(dataDebitId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner(), DataDebitOwner(dataDebitId)) || ContainsApplicationRole(Owner(), DataDebitOwner(dataDebitId))).async { implicit request =>
      dataDebitService.dataDebit(dataDebitId)
        .flatMap {
          case Some(debit) if debit.activePermissions.isDefined =>
            logger.debug("Got Data Debit, fetching data")
            val eventualData = debit.activePermissions.get.conditions map { bundleConditions =>
              logger.debug("Getting data for conditions")
              dataService.bundleData(bundleConditions).flatMap { conditionValues =>
                val conditionFulfillment: Map[String, Boolean] = conditionValues map {
                  case (condition, values) =>
                    (condition, values.nonEmpty)
                }

                if (conditionFulfillment.forall(_._2)) {
                  logger.debug(s"Data Debit $dataDebitId conditions satisfied")
                  dataService.bundleData(debit.activePermissions.get.bundle)
                    .map(DataDebitData(Some(conditionFulfillment), _))
                }
                else {
                  logger.debug(s"Data Debit $dataDebitId conditions not satisfied: $conditionFulfillment")
                  Future.successful(DataDebitData(Some(conditionFulfillment), Map()))
                }
              }

            } getOrElse {
              logger.debug(s"Data Debit $dataDebitId without conditions")
              dataService.bundleData(debit.activePermissions.get.bundle)
                .map(DataDebitData(None, _))
            }

            eventualData
              .andThen(dataEventDispatcher.dispatchEventDataDebitValues(debit))
              .map(d => Ok(Json.toJson(d)))

          case Some(_) => Future.successful(BadRequest(Json.toJson(Errors.dataDebitNotEnabled(dataDebitId))))
          case None    => Future.successful(NotFound(Json.toJson(Errors.dataDebitNotFound(dataDebitId))))
        }
        .recover {
          case err: RichDataBundleFormatException => BadRequest(Json.toJson(Errors.dataDebitBundleMalformed(dataDebitId, err)))
        }
    }

  def listDataDebits(): Action[AnyContent] =
    SecuredAction(WithRole(Owner()) || ContainsApplicationRole(Owner())).async { implicit request =>
      dataDebitService.all map { debits =>
        Ok(Json.toJson(debits))
      }
    }

  def enableDataDebitNewest(dataDebitId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner()) || ContainsApplicationRole(Owner())).async { implicit request =>
      enableDataDebit(dataDebitId)
    }

  protected def enableDataDebit(dataDebitId: String)(implicit request: SecuredRequest[HatApiAuthEnvironment, AnyContent]): Future[Result] = {
    val enabled = for {
      _ <- dataDebitService.dataDebitEnableNewestPermissions(dataDebitId)
      debit <- dataDebitService.dataDebit(dataDebitId)
    } yield debit

    enabled
      .andThen(dataEventDispatcher.dispatchEventMaybeDataDebit(DataDebitOperations.Enable()))
      .map {
        case Some(debit) => Ok(Json.toJson(debit))
        case None        => BadRequest(Json.toJson(Errors.dataDebitDoesNotExist))
      }
  }

  def disableDataDebit(dataDebitId: String, atPeriodEnd: Boolean): Action[AnyContent] =
    SecuredAction(WithRole(Owner()) || ContainsApplicationRole(Owner())).async { implicit request =>
      val disabled = for {
        _ <- dataDebitService.dataDebitDisable(dataDebitId, atPeriodEnd)
        debit <- dataDebitService.dataDebit(dataDebitId)
      } yield debit

      disabled
        .andThen(dataEventDispatcher.dispatchEventMaybeDataDebit(DataDebitOperations.Disable()))
        .map {
          case Some(debit) => Ok(Json.toJson(debit))
          case None        => BadRequest(Json.toJson(Errors.dataDebitDoesNotExist))
        }
    }

  private object Errors {
    def dataDebitDoesNotExist = ErrorMessage("Not Found", "Data Debit with this ID does not exist")
    def dataDebitNotFound(id: String) = ErrorMessage("Not Found", s"Data Debit $id not found")
    def dataDebitNotEnabled(id: String) = ErrorMessage("Bad Request", s"Data Debit $id not enabled")
    def dataDebitMalformed(err: Throwable) = ErrorMessage("Bad Request", s"Data Debit request malformed: ${err.getMessage}")
    def dataDebitBundleMalformed(id: String, err: Throwable) = ErrorMessage("Data Debit Bundle malformed", s"Data Debit $id active bundle malformed: ${err.getMessage}")

    def bundleNotFound(bundleId: String) = ErrorMessage("Bundle Not Found", s"Bundle $bundleId not found")

    def dataUpdateMissing(message: String) = ErrorMessage("Data Missing", s"Could not update records: $message")
    def dataDeleteMissing(message: String) = ErrorMessage("Data Missing", s"Could not delete records: $message")
    def dataLinkMissing(message: String) = ErrorMessage("Data Missing", s"Could not link records: $message")

    def dataCombinatorNotFound(combinator: String) = ErrorMessage("Combinator Not Found", s"Combinator $combinator not found")

    def richDataDuplicate(error: Throwable) = ErrorMessage("Bad Request", s"Duplicate data - ${error.getMessage}")
    def richDataError(error: Throwable) = ErrorMessage("Bad Request", s"Could not insert data - ${error.getMessage}")
    def forbidden(error: Throwable) = ErrorMessage("Forbidden", s"Access Denied - ${error.getMessage}")
  }
}

