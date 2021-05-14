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
 * Written by Tyler Weir <tyler.weir@dataswift.io>
 * 1 / 2021
 */

package org.hatdex.hat.api.controllers

import com.mohiva.play.silhouette.api.Silhouette
import eu.timepit.refined.auto._
import io.dataswift.models.hat._
import io.dataswift.models.hat.json.RichDataJsonFormats
import org.hatdex.hat.api.controllers.devices.DeviceVerification
import org.hatdex.hat.api.controllers.devices.Types.DeviceVerificationFailure._
import org.hatdex.hat.api.controllers.devices.Types._
import org.hatdex.hat.api.service.UserService
import org.hatdex.hat.api.service.applications.{ ApplicationsService, TrustedApplicationProvider }
import org.hatdex.hat.api.service.richData._
import org.hatdex.hat.authentication.models._
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.client.{ AuthServiceClient }
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.hat.utils.{ HatBodyParsers, LoggingProvider }
import org.hatdex.libs.dal.HATPostgresProfile
import play.api.Configuration
import play.api.libs.json.Reads._
import play.api.libs.json.{ JsArray, JsValue, Json, Reads }
import play.api.mvc._

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

object MachineData {
  case class SLTokenBody(
      iss: String,
      exp: Long,
      deviceId: String)
  implicit val sltokenBodyReads: Reads[SLTokenBody] = Json.reads[SLTokenBody]
}

class MachineData @Inject() (
    components: ControllerComponents,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    dataService: RichDataService,
    loggingProvider: LoggingProvider,
    configuration: Configuration,
    usersService: UserService,
    authServiceClient: AuthServiceClient,
    trustedApplicationProvider: TrustedApplicationProvider,
    deviceVerification: DeviceVerification,
    implicit val ec: ExecutionContext,
    implicit val applicationsService: ApplicationsService)
    extends HatApiController(components, silhouette) {
  import RichDataJsonFormats._

  private val logger             = loggingProvider.logger(this.getClass)
  private val defaultRecordLimit = 1000

  // -- Error Handler - Error to Response
  def handleFailedRequestAssessment(failure: DeviceVerificationFailure): Future[Result] =
    failure match {
      case DeviceRequestFailure(errorMessage) =>
        logger.info(s"DeviceRequestFailure: ${errorMessage}")
        Future.successful(BadRequest(s"DeviceRequest was invalid: ${errorMessage}"))

      case ServiceRespondedWithFailure(errorMessage) =>
        logger.info(s"ServiceRespondedWithFailure: ${errorMessage}")
        Future.successful(ServiceUnavailable)

      case ApplicationAndNamespaceNotValid =>
        logger.info(s"ApplicationAndNamespaceNotValid")
        Future.successful(BadRequest(s"DeviceRequest was invalid"))

      case FailedDeviceRefinement =>
        logger.info(s"FailedDeviceRefinement")
        Future.successful(BadRequest(s"DeviceRequest was invalid."))

      case InvalidTokenFailure(errorMessage) =>
        logger.info(s"InvalidTokenFailure: ${errorMessage}")
        Future.successful(BadRequest(s"DeviceRequest was invalid: ${errorMessage}"))

      case InvalidDeviceDataRequestFailure(errorMessage) =>
        logger.info(s"InvalidDeviceDataRequestFailure: ${errorMessage}")
        Future.successful(BadRequest(s"DeviceRequest Body was invalid"))
    }

  // --- API Handlers ---

  def getData(
      namespace: String,
      endpoint: String,
      orderBy: Option[String],
      ordering: Option[String],
      skip: Option[Int],
      take: Option[Int]): Action[AnyContent] =
    UserAwareAction.async { implicit request =>
      val eventuallyVerdict =
        deviceVerification.getRequestVerdict(request.headers,
                                             request.dynamicEnvironment.hatName,
                                             namespace,
                                             usersService,
                                             trustedApplicationProvider,
                                             permissions = Read
        )

      eventuallyVerdict.flatMap {
        case Left(verdictError) => handleFailedRequestAssessment(verdictError)
        case Right(DeviceVerificationSuccess.DeviceRequestSuccess(_)) =>
          makeData(namespace, endpoint, orderBy, ordering, skip, take)
      }
    }

  def createData(
      namespace: String,
      endpoint: String,
      skipErrors: Option[Boolean]): Action[JsValue] =
    UserAwareAction.async(parsers.json[JsValue]) { implicit request =>
      val deviceDataCreate = request.body
      val eventuallyVerdict =
        deviceVerification.getRequestVerdict(request.headers,
                                             request.dynamicEnvironment.hatName,
                                             namespace,
                                             usersService,
                                             trustedApplicationProvider,
                                             permissions = Write
        )

      eventuallyVerdict
        .flatMap {
          case Left(verdictError) => handleFailedRequestAssessment(verdictError)
          case Right(DeviceVerificationSuccess.DeviceRequestSuccess(hatUser)) =>
            handleCreateDeviceData(
              hatUser = hatUser,
              machineDataCreate = deviceDataCreate,
              namespace = namespace,
              endpoint = endpoint,
              skipErrors = skipErrors
            )
        }
        .recover {
          case e: RichDataDuplicateException =>
            BadRequest(Json.toJson(Errors.richDataError(e)))
          case e: RichDataPermissionsException =>
            Forbidden(Json.toJson(Errors.forbidden(e)))
          case e: RichDataServiceException =>
            BadRequest(Json.toJson(Errors.richDataError(e)))
        }

    }

  def updateData(namespace: String): Action[Seq[EndpointData]] =
    UserAwareAction.async(parsers.json[Seq[EndpointData]]) { implicit request =>
      val deviceDataUpdate = request.body
      val eventuallyVerdict =
        deviceVerification.getRequestVerdict(request.headers,
                                             request.dynamicEnvironment.hatName,
                                             namespace,
                                             usersService,
                                             trustedApplicationProvider,
                                             permissions = ReadWrite
        )

      eventuallyVerdict.flatMap {
        case Left(verdictError) => handleFailedRequestAssessment(verdictError)
        case Right(DeviceVerificationSuccess.DeviceRequestSuccess(hatUser)) =>
          handleUpdateDeviceData(hatUser, deviceDataUpdate, namespace)
      }
    }

  // -- Operations

  // -- Get Data
  private def makeData(
      namespace: String,
      endpoint: String,
      orderBy: Option[String],
      ordering: Option[String],
      skip: Option[Int],
      take: Option[Int]
    )(implicit db: HATPostgresProfile.api.Database): Future[Result] = {
    val dataEndpoint = s"$namespace/$endpoint"
    val query =
      Seq(EndpointQuery(dataEndpoint, None, None, None))
    val data = dataService.propertyData(
      query,
      orderBy,
      ordering.contains("descending"),
      skip.getOrElse(0),
      take.orElse(Some(defaultRecordLimit))
    )
    data.map(d => Ok(Json.toJson(d)))
  }

  // -- Create Data
  private def handleCreateDeviceData(
      hatUser: HatUser,
      machineDataCreate: JsValue,
      namespace: String,
      endpoint: String,
      skipErrors: Option[Boolean]
    )(implicit hatServer: HatServer): Future[Result] = {
    val dataEndpoint = s"$namespace/$endpoint"
    machineDataCreate match {
      case array: JsArray =>
        handleJsArray(
          hatUser.userId,
          array,
          dataEndpoint,
          skipErrors
        )
      case value: JsValue =>
        handleJsValue(
          hatUser.userId,
          value,
          dataEndpoint
        )
      case _ =>
        Future.successful(BadRequest("Incorrectly formatted body."))
    }
  }

  // -- Update Data
  private def handleUpdateDeviceData(
      hatUser: HatUser,
      machineDataUpdate: Seq[EndpointData],
      namespace: String
    )(implicit hatServer: HatServer): Future[Result] =
    machineDataUpdate.length match {
      // Missing Json Body, do nothing, return error
      case 0 =>
        logger.error(s"updateDeviceData included no json body - ns:${namespace}")
        Future.successful(BadRequest("Missing update body."))

      // There is a Json body, process either an JsArray or a JsValue
      case _ =>
        val dataSeq = machineDataUpdate //.map(item => item.copy(endpoint = s"${namespace}/${item.endpoint}"))
        dataService.updateRecords(
          hatUser.userId,
          dataSeq
        ) map { saved =>
          Created(Json.toJson(saved))
        } recover {
          case RichDataMissingException(message, _) =>
            println(message)
            BadRequest(Json.toJson(Errors.dataUpdateMissing(message)))
        }
    }

  trait DeviceRequest {
    def refineDevice: DeviceDataInfoRefined
  }

  private def handleJsArray(
      userId: UUID,
      array: JsArray,
      dataEndpoint: String,
      skipErrors: Option[Boolean]
    )(implicit hatServer: HatServer): Future[Result] = {
    val values =
      array.value.map(EndpointData(dataEndpoint, None, None, None, _, None))
    logger.info(s"handleJsArray: Values: ${values}")
    dataService
      .saveData(userId, values.toSeq, skipErrors.getOrElse(false))
      .map(saved => Created(Json.toJson(saved)))
  }

  private def handleJsValue(
      userId: UUID,
      value: JsValue,
      dataEndpoint: String
    )(implicit hatServer: HatServer): Future[Result] = {
    val values = Seq(EndpointData(dataEndpoint, None, None, None, value, None))
    logger.info(s"handleJsArray: Values: ${values}")
    dataService
      .saveData(userId, values)
      .map(saved => Created(Json.toJson(saved.head)))
  }

  private object Errors {
    def dataDebitDoesNotExist: ErrorMessage =
      ErrorMessage("Not Found", "Data Debit with this ID does not exist")
    def dataDebitNotFound(id: String): ErrorMessage =
      ErrorMessage("Not Found", s"Data Debit $id not found")
    def dataDebitNotEnabled(id: String): ErrorMessage =
      ErrorMessage("Bad Request", s"Data Debit $id not enabled")
    def dataDebitMalformed(err: Throwable): ErrorMessage =
      ErrorMessage(
        "Bad Request",
        s"Data Debit request malformed: ${err.getMessage}"
      )
    def dataDebitBundleMalformed(
        id: String,
        err: Throwable): ErrorMessage =
      ErrorMessage(
        "Data Debit Bundle malformed",
        s"Data Debit $id active bundle malformed: ${err.getMessage}"
      )

    def bundleNotFound(bundleId: String): ErrorMessage =
      ErrorMessage("Bundle Not Found", s"Bundle $bundleId not found")

    def dataMissing(message: String): ErrorMessage =
      ErrorMessage("Data Missing", s"Could not find unique record: $message")
    def dataUpdateMissing(message: String): ErrorMessage =
      ErrorMessage("Data Missing", s"Could not update records: $message")
    def dataDeleteMissing(message: String): ErrorMessage =
      ErrorMessage("Data Missing", s"Could not delete records: $message")
    def dataLinkMissing(message: String): ErrorMessage =
      ErrorMessage("Data Missing", s"Could not link records: $message")

    def dataCombinatorNotFound(combinator: String): ErrorMessage =
      ErrorMessage("Combinator Not Found", s"Combinator $combinator not found")

    def richDataDuplicate(error: Throwable): ErrorMessage =
      ErrorMessage("Bad Request", s"Duplicate data - ${error.getMessage}")
    def richDataError(error: Throwable): ErrorMessage =
      ErrorMessage(
        "Bad Request",
        s"Could not insert data - ${error.getMessage}"
      )
    def forbidden(error: Throwable): ErrorMessage =
      ErrorMessage("Forbidden", s"Access Denied - ${error.getMessage}")
    def forbidden(message: String): ErrorMessage =
      ErrorMessage("Forbidden", s"Access Denied - ${message}")
  }
}
