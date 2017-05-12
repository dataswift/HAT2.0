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
 * 4 / 2017
 */

package org.hatdex.hat.api.controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.Clock
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.monitoring.HatDataEventBus
import org.hatdex.hat.api.service.richData._
import org.hatdex.hat.authentication.models._
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.resourceManagement._
import org.hatdex.hat.utils.HatBodyParsers
import play.api.cache.CacheApi
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{ JsArray, JsValue, Json }
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.Future
import scala.util.control.NonFatal

class RichData @Inject() (
  val messagesApi: MessagesApi,
  configuration: Configuration,
  parsers: HatBodyParsers,
  silhouette: Silhouette[HatApiAuthEnvironment],
  clock: Clock,
  hatServerProvider: HatServerProvider,
  cache: CacheApi,
  dataEventBus: HatDataEventBus,
  dataService: RichDataService,
  bundleService: RichBundleService,
  dataDebitService: DataDebitContractService)
    extends HatApiController(silhouette, clock, hatServerProvider, configuration) with RichDataJsonFormats {

  val logger = Logger(this.getClass)

  def getEndpointData(namespace: String, endpoint: String, recordId: Option[UUID], orderBy: Option[String], take: Option[Int]): Action[AnyContent] =
    SecuredAction(WithRole(Owner())).async { implicit request =>
      val query = cache.get[List[EndpointQuery]](endpoint)
        .getOrElse(List(EndpointQuery(endpoint, None, None, None)))
      val data = dataService.propertyData(query, orderBy, take.getOrElse(1000))
      data.map(d => Ok(Json.toJson(d)))
    }

  def saveEndpointData(namespace: String, endpoint: String): Action[JsValue] =
    SecuredAction(WithRole(DataCredit(namespace))).async(parsers.json[JsValue]) { implicit request =>
      val response = request.body match {
        case array: JsArray =>
          val values = array.value.map(EndpointData(endpoint, None, _, None))
          dataService.saveData(request.identity.userId, values) map { saved =>
            dataEventBus.publish(HatDataEventBus.DataCreatedEvent(
              request.dynamicEnvironment.hatName,
              ModelTranslation.fromInternalModel(request.identity), s"saved batch for $namespace/$endpoint", saved))
            Created(Json.toJson(saved))
          }
        case value: JsValue =>
          val values = Seq(EndpointData(endpoint, None, value, None))
          dataService.saveData(request.identity.userId, values) map { saved =>
            dataEventBus.publish(HatDataEventBus.DataCreatedEvent(
              request.dynamicEnvironment.hatName,
              ModelTranslation.fromInternalModel(request.identity), s"saved data for $namespace/$endpoint", saved))
            Created(Json.toJson(saved.head))
          }
      }

      response recover {
        case e: RichDataDuplicateException =>
          BadRequest(Json.toJson(ErrorMessage("Duplicate Data", s"Could not insert data - ${e.getMessage}")))
        case e: RichDataServiceException =>
          BadRequest(Json.toJson(ErrorMessage("Bad Request", s"Could not insert data - ${e.getMessage}")))
      }
    }

  private def endpointDataNamespaces(data: EndpointData): Option[Set[String]] = {
    data.endpoint.split('/').headOption map { namespace =>
      val namespaces = data.links map { linkedData =>
        linkedData.flatMap(endpointDataNamespaces)
          .reduce((set, namespaces) => set ++ namespaces)

      } getOrElse Set()
      namespaces + namespace
    }
  }
  private def authorizeEndpointDataWrite(data: Seq[EndpointData])(implicit user: HatUser, authenticator: HatApiAuthEnvironment#A) = {
    data.flatMap(endpointDataNamespaces)
      .reduce((set, namespaces) => set ++ namespaces)
      .forall(namespace => WithRole.isAuthorized(user, authenticator, DataCredit(namespace)))
  }

  def saveBatchData: Action[Seq[EndpointData]] =
    SecuredAction(WithRole(DataCredit(""), Owner())).async(parsers.json[Seq[EndpointData]]) { implicit request =>
      val response = if (authorizeEndpointDataWrite(request.body)) {
        dataService.saveData(request.identity.userId, request.body) map { saved =>
          Created(Json.toJson(saved))
        }
      }
      else {
        Future.failed(RichDataPermissionsException("No rights to insert some or all of the data in the batch"))
      }

      response recover {
        case e: RichDataDuplicateException =>
          BadRequest(Json.toJson(ErrorMessage("Duplicate Data", s"Could not insert data - ${e.getMessage}")))
        case e: RichDataPermissionsException =>
          Forbidden(Json.toJson(ErrorMessage("Forbidden", s"Access Denied - ${e.getMessage}")))
        case e: RichDataServiceException =>
          BadRequest(Json.toJson(ErrorMessage("Bad Request", s"Could not insert data - ${e.getMessage}")))
      }
    }

  def registerCombinator(combinator: String): Action[Seq[EndpointQuery]] =
    SecuredAction(WithRole(Owner())).async(parsers.json[Seq[EndpointQuery]]) { implicit request =>
      bundleService.saveCombinator(combinator, request.body) map { _ =>
        Created(Json.toJson(SuccessResponse(s"Endpoint $combinator registered")))
      }
    }

  def getCombinatorData(combinator: String, recordId: Option[UUID], orderBy: Option[String], take: Option[Int]): Action[AnyContent] =
    SecuredAction(WithRole(Owner())).async { implicit request =>
      val result = for {
        query <- bundleService.combinator(combinator).map(_.get)
        data <- dataService.propertyData(query, orderBy, take.getOrElse(1000))
      } yield data

      result map { d =>
        Ok(Json.toJson(d))
      } recover {
        case NonFatal(_) =>
          NotFound(Json.toJson(ErrorMessage("Combinator Not Found", s"Combinator $combinator not found")))
      }
    }

  def linkDataRecords(records: Seq[UUID]): Action[AnyContent] =
    SecuredAction(WithRole(DataCredit(""), Owner())).async { implicit request =>
      dataService.saveRecordGroup(request.identity.userId, records) map { _ =>
        Created(Json.toJson(SuccessResponse(s"Grouping registered")))
      } recover {
        case RichDataMissingException(message, _) =>
          BadRequest(Json.toJson(ErrorMessage("Data Missing", s"Could not link records: $message")))
      }
    }

  def deleteDataRecords(records: Seq[UUID]): Action[AnyContent] =
    SecuredAction(WithRole(DataCredit(""), Owner())).async { implicit request =>
      dataService.deleteRecords(request.identity.userId, records) map { _ =>
        Ok(Json.toJson(SuccessResponse(s"All records deleted")))
      } recover {
        case RichDataMissingException(message, _) =>
          BadRequest(Json.toJson(ErrorMessage("Data Missing", s"Could not delete records: $message")))
      }
    }

  def updateRecords(): Action[Seq[EndpointData]] =
    SecuredAction(WithRole(DataCredit(""), Owner())).async(parsers.json[Seq[EndpointData]]) { implicit request =>
      dataService.updateRecords(request.identity.userId, request.body) map { saved =>
        Created(Json.toJson(saved))
      } recover {
        case RichDataMissingException(message, _) =>
          BadRequest(Json.toJson(ErrorMessage("Data Missing", s"Could not update records: $message")))
      }
    }

  def registerBundle(bundleId: String): Action[Map[String, PropertyQuery]] =
    SecuredAction(WithRole(Owner())).async(parsers.json[Map[String, PropertyQuery]]) { implicit request =>
      bundleService.saveBundle(EndpointDataBundle(bundleId, request.body))
        .map { _ =>
          Created(Json.toJson(SuccessResponse(s"Bundle $bundleId registered")))
        }
    }

  def fetchBundle(bundleId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner())).async { implicit request =>
      val result = for {
        bundle <- bundleService.bundle(bundleId).map(_.get)
        data <- dataService.bundleData(bundle)
      } yield data

      result map { d =>
        Ok(Json.toJson(d))
      } recover {
        case NonFatal(_) =>
          NotFound(Json.toJson(ErrorMessage("Bundle Not Found", s"Bundle $bundleId not found")))
      }
    }

  def registerDataDebit(dataDebitId: String): Action[DataDebitRequest] =
    SecuredAction(WithRole(Owner(), DataDebitOwner(""), Platform())).async(parsers.json[DataDebitRequest]) { implicit request =>
      dataDebitService.createDataDebit(dataDebitId, request.body, request.identity.userId)
        .map { debit =>
          Created(Json.toJson(debit))
        }
        .recover {
          case RichDataDuplicateBundleException(message, _) =>
            BadRequest(Json.toJson(ErrorMessage("Bad Request", s"Data Debit request malformed: $message")))
          case RichDataDuplicateDebitException(message, _) =>
            BadRequest(Json.toJson(ErrorMessage("Bad Request", s"Data Debit request malformed: $message")))
        }
    }

  def updateDataDebit(dataDebitId: String): Action[DataDebitRequest] =
    SecuredAction(WithRole(Owner(), DataDebitOwner(dataDebitId))).async(parsers.json[DataDebitRequest]) { implicit request =>
      dataDebitService.updateDataDebitBundle(dataDebitId, request.body, request.identity.userId)
        .map { debit =>
          Ok(Json.toJson(debit))
        }
        .recover {
          case err: RichDataServiceException =>
            BadRequest(Json.toJson(ErrorMessage("Bad Request", s"Data Debit request malformed: ${err.getMessage}")))
        }
    }

  def getDataDebit(dataDebitId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner(), DataDebitOwner(dataDebitId))).async { implicit request =>
      dataDebitService.dataDebit(dataDebitId) map {
        case Some(debit) => Ok(Json.toJson(debit))
        case None        => NotFound(Json.toJson(ErrorMessage("Not Found", "Data Debit with this ID does not exist")))
      }
    }

  def getDataDebitValues(dataDebitId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner(), DataDebitOwner(dataDebitId))).async { implicit request =>
      dataDebitService.dataDebit(dataDebitId) flatMap {
        case Some(debit) if debit.activeBundle.isDefined =>
          dataService.bundleData(debit.activeBundle.get.bundle) map { values =>
            Ok(Json.toJson(values))
          }
        case Some(_) => Future.successful(BadRequest(Json.toJson(ErrorMessage("Bad Request", s"Data Debit $dataDebitId not enabled"))))
        case None    => Future.successful(NotFound(Json.toJson(ErrorMessage("Not Found", s"Data Debit $dataDebitId not found"))))
      }
    }

  def listDataDebits(): Action[AnyContent] =
    SecuredAction(WithRole(Owner())).async { implicit request =>
      dataDebitService.all map { debits =>
        Ok(Json.toJson(debits))
      }
    }

  def enableDataDebit(dataDebitId: String, bundleId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner())).async { implicit request =>
      val enabled = for {
        _ <- dataDebitService.dataDebitEnableBundle(dataDebitId, bundleId)
        debit <- dataDebitService.dataDebit(dataDebitId)
      } yield debit
      enabled map {
        case Some(debit) => Ok(Json.toJson(debit))
        case None        => BadRequest(Json.toJson(ErrorMessage("Not Found", "Data Debit with this ID does not exist")))
      }
    }

  def disableDataDebit(dataDebitId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner())).async { implicit request =>
      val disabled = for {
        _ <- dataDebitService.dataDebitDisable(dataDebitId)
        debit <- dataDebitService.dataDebit(dataDebitId)
      } yield debit
      disabled map {
        case Some(debit) => Ok(Json.toJson(debit))
        case None        => BadRequest(Json.toJson(ErrorMessage("Not Found", "Data Debit with this ID does not exist")))
      }
    }

}

