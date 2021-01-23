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
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import dev.profunktor.auth.jwt.JwtSecretKey
import io.dataswift.adjudicator.ShortLivedTokenOps
import io.dataswift.adjudicator.Types.{ ContractId, HatName, ShortLivedToken }
import org.hatdex.hat.api.json.RichDataJsonFormats
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.models.applications.{ Application, HatApplication }
import org.hatdex.hat.api.service.applications.{ ApplicationsService, TrustedApplicationProvider }
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.api.service.monitoring.HatDataEventDispatcher
import org.hatdex.hat.api.service.richData.{ RichDataServiceException, _ }
import org.hatdex.hat.authentication.models._
import org.hatdex.hat.authentication.{ ContainsApplicationRole, HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.utils.{ AdjudicatorRequest, HatBodyParsers, LoggingProvider }
import org.hatdex.hat.utils.AdjudicatorRequestTypes._
import play.api.libs.json.{ JsArray, JsValue, Json }
import play.api.libs.ws.WSClient
import play.api.mvc._
import org.hatdex.libs.dal.HATPostgresProfile
import play.api.Configuration
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined._
import pdi.jwt.JwtClaim
import play.api.libs.json.Reads._
import org.hatdex.hat.resourceManagement.HatServer

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import scala.util.control.NonFatal
import play.api.libs.json.Reads
import org.hatdex.hat.NamespaceUtils.NamespaceUtils
import org.joda.time.{ DateTime, Duration, LocalDateTime }

sealed trait RequestValidationFailure
object RequestValidationFailure {
  final case class InaccessibleNamespace(namespace: String) extends RequestValidationFailure
  final case class InvalidShortLivedToken(contractId: String) extends RequestValidationFailure
}
final case class RequestVerified(namespace: String) extends AnyVal

class RichData @Inject() (
    components: ControllerComponents,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    dataEventDispatcher: HatDataEventDispatcher,
    dataService: RichDataService,
    bundleService: RichBundleService,
    dataDebitService: DataDebitContractService,
    loggingProvider: LoggingProvider,
    configuration: Configuration,
    usersService: UsersService,
    trustedApplicationProvider: TrustedApplicationProvider,
    implicit val ec: ExecutionContext,
    implicit val applicationsService: ApplicationsService
  )(wsClient: WSClient)
    extends HatApiController(components, silhouette)
    with RichDataJsonFormats {

  private val logger             = loggingProvider.logger(this.getClass)
  private val defaultRecordLimit = 1000

  //** Adjudicator
  private val adjudicatorAddress =
    configuration.underlying.getString("adjudicator.address")
  private val adjudicatorScheme =
    configuration.underlying.getString("adjudicator.scheme")
  private val adjudicatorEndpoint =
    s"${adjudicatorScheme}${adjudicatorAddress}"
  private val adjudicatorSharedSecret =
    configuration.underlying.getString("adjudicator.sharedSecret")
  private val adjudicatorClient = new AdjudicatorRequest(
    adjudicatorEndpoint,
    JwtSecretKey(adjudicatorSharedSecret),
    wsClient
  )

  /**
    * Returns Data Records for a given endpoint
    *
    * @param namespace Namespace of the endpoint, typically restricted to a specific application
    * @param endpoint  Endpoint name within the namespace, any valid URL path
    * @param orderBy   Data Field within a data record by which data should be ordered
    * @param ordering  The ordering to use for data sorting - default is "ascending", set to "descending" for reverse order
    * @param skip      How many records to skip (used for paging)
    * @param take      How many data records to take - limits the number of results, could be used for paging responses
    * @return HTTP Response with JSON-serialized data records or an error message
    */
  def getEndpointData(
      namespace: String,
      endpoint: String,
      orderBy: Option[String],
      ordering: Option[String],
      skip: Option[Int],
      take: Option[Int]): Action[AnyContent] =
    SecuredAction(
      WithRole(Owner(), NamespaceRead(namespace)) || ContainsApplicationRole(
          Owner(),
          NamespaceRead(namespace)
        )
    ).async { implicit request =>
      makeData(namespace, endpoint, orderBy, ordering, skip, take)
    }

  def saveEndpointData(
      namespace: String,
      endpoint: String,
      skipErrors: Option[Boolean]): Action[JsValue] =
    SecuredAction(
      WithRole(NamespaceWrite(namespace)) || ContainsApplicationRole(
          NamespaceWrite(namespace)
        )
    )
      .async(parsers.json[JsValue]) { implicit request =>
        val dataEndpoint = s"$namespace/$endpoint"
        val response = request.body match {
          case array: JsArray =>
            // TODO: extract unique ID and timestamp
            val values = array.value.map(
              EndpointData(dataEndpoint, None, None, None, _, None)
            )
            dataService
              .saveData(
                request.identity.userId,
                values,
                skipErrors.getOrElse(false)
              )
              .andThen(
                dataEventDispatcher
                  .dispatchEventDataCreated(s"saved batch for $dataEndpoint")
              )
              .map(saved => Created(Json.toJson(saved)))

          case value: JsValue =>
            // TODO: extract unique ID and timestamp
            val values =
              Seq(EndpointData(dataEndpoint, None, None, None, value, None))
            dataService
              .saveData(request.identity.userId, values)
              .andThen(
                dataEventDispatcher
                  .dispatchEventDataCreated(s"saved data for $dataEndpoint")
              )
              .map(saved => Created(Json.toJson(saved.head)))
        }

        response recover {
          case e: RichDataDuplicateException =>
            BadRequest(Json.toJson(Errors.richDataDuplicate(e)))
          case e: RichDataServiceException =>
            BadRequest(Json.toJson(Errors.richDataError(e)))
        }
      }

  def deleteEndpointData(
      namespace: String,
      endpoint: String): Action[AnyContent] =
    SecuredAction(
      WithRole(NamespaceWrite(namespace)) || ContainsApplicationRole(
          NamespaceWrite(namespace)
        )
    ).async { implicit request =>
      val dataEndpoint = s"$namespace/$endpoint"
      dataService.deleteEndpoint(dataEndpoint) map { _ =>
        Ok(Json.toJson(SuccessResponse(s"All records deleted")))
      }
    }

  def saveBatchData: Action[Seq[EndpointData]] =
    SecuredAction(
      WithRole(DataCredit(""), Owner()) || ContainsApplicationRole(
          NamespaceWrite("*"),
          Owner()
        )
    )
      .async(parsers.json[Seq[EndpointData]]) { implicit request =>
        val response = request2ApplicationStatus(request).flatMap { maybeAppStatus =>
          if (authorizeEndpointDataWrite(request.body, maybeAppStatus))
            dataService
              .saveData(request.identity.userId, request.body)
              .andThen(
                dataEventDispatcher
                  .dispatchEventDataCreated(s"saved batch data")
              )
              .map(d => Created(Json.toJson(d)))
          else
            Future.failed(
              RichDataPermissionsException(
                "No rights to insert some or all of the data in the batch"
              )
            )
        }

        response recover {
          case e: RichDataDuplicateException =>
            BadRequest(Json.toJson(Errors.richDataError(e)))
          case e: RichDataPermissionsException =>
            Forbidden(Json.toJson(Errors.forbidden(e)))
          case e: RichDataServiceException =>
            BadRequest(Json.toJson(Errors.richDataError(e)))
        }
      }

  private def endpointDataNamespaces(data: EndpointData): Set[String] =
    data.endpoint.split('/').headOption map { namespace =>
      val namespaces = data.links map { linkedData =>
          linkedData
            .map(endpointDataNamespaces)
            .reduce((set, namespaces) => set ++ namespaces)
        } getOrElse Set()
      namespaces + namespace
    } getOrElse Set()

  // Extract to namespace library
  private def authorizeEndpointDataWrite(
      data: Seq[EndpointData],
      appStatus: Option[HatApplication]
    )(implicit
      user: HatUser,
      authenticator: HatApiAuthEnvironment#A): Boolean =
    data
      .map(endpointDataNamespaces)
      .reduce((set, namespaces) => set ++ namespaces)
      .map(namespace => NamespaceWrite(namespace))
      .forall(role =>
        WithRole.isAuthorized(user, authenticator, role) || appStatus.exists(
            ContainsApplicationRole.isAuthorized(user, _, authenticator, role)
          )
      )

  def registerCombinator(combinator: String): Action[Seq[EndpointQuery]] =
    SecuredAction(WithRole(Owner()) || ContainsApplicationRole(Owner()))
      .async(parsers.json[Seq[EndpointQuery]]) { implicit request =>
        bundleService.saveCombinator(combinator, request.body) map { _ =>
          Created(
            Json.toJson(SuccessResponse(s"Combinator $combinator registered"))
          )
        }
      }

  def getCombinatorData(
      combinator: String,
      orderBy: Option[String],
      ordering: Option[String],
      skip: Option[Int],
      take: Option[Int]): Action[AnyContent] =
    SecuredAction(WithRole(Owner()) || ContainsApplicationRole(Owner())).async { implicit request =>
      val result = for {
        query <- bundleService.combinator(combinator).map(_.get)
        data <- dataService.propertyData(
                  query,
                  orderBy,
                  ordering.contains("descending"),
                  skip.getOrElse(0),
                  take.orElse(Some(defaultRecordLimit))
                )
      } yield data

      result map { d =>
        Ok(Json.toJson(d))
      } recover {
        case NonFatal(_) =>
          NotFound(Json.toJson(Errors.dataCombinatorNotFound(combinator)))
      }
    }

  def linkDataRecords(records: Seq[UUID]): Action[AnyContent] =
    SecuredAction(
      WithRole(DataCredit(""), Owner()) || ContainsApplicationRole(
          NamespaceWrite("*"),
          Owner()
        )
    ).async { implicit request =>
      dataService.saveRecordGroup(request.identity.userId, records) map { _ =>
        Created(Json.toJson(SuccessResponse(s"Grouping registered")))
      } recover {
        case RichDataMissingException(message, _) =>
          BadRequest(Json.toJson(Errors.dataLinkMissing(message)))
      }
    }

  def deleteDataRecords(records: Seq[UUID]): Action[AnyContent] =
    SecuredAction(
      WithRole(DataCredit(""), Owner()) || ContainsApplicationRole(
          NamespaceWrite("*"),
          Owner()
        )
    ).async { implicit request =>
      val eventualPermissionContext = for {
        maybeAppStatus <- request2ApplicationStatus(request)
        recordNamespaces <- dataService.uniqueRecordNamespaces(records)
      } yield (maybeAppStatus, recordNamespaces)

      eventualPermissionContext flatMap {
        case (Some(appStatus), requiredNamespaces)
            if requiredNamespaces.forall(n =>
              ContainsApplicationRole
                .isAuthorized(
                  request.identity,
                  appStatus,
                  request.authenticator,
                  NamespaceWrite(n)
                )
            ) =>
          dataService.deleteRecords(request.identity.userId, records) map { _ =>
            Ok(Json.toJson(SuccessResponse(s"All records deleted")))
          } recover {
              case RichDataMissingException(message, _) =>
                BadRequest(Json.toJson(Errors.dataDeleteMissing(message)))
            }
        case (Some(_), _) =>
          Future.successful(
            Forbidden(
              Json.toJson(
                Errors.forbidden(
                  "Permissions to modify records in some of the namespaces missing"
                )
              )
            )
          )
        // TODO: remove after non-application tokens are phased out
        case (None, _) =>
          dataService.deleteRecords(request.identity.userId, records) map { _ =>
            Ok(Json.toJson(SuccessResponse(s"All records deleted")))
          } recover {
              case RichDataMissingException(message, _) =>
                BadRequest(Json.toJson(Errors.dataDeleteMissing(message)))
            }
      }
    }

  def listEndpoints: Action[AnyContent] =
    SecuredAction(
      WithRole(Owner(), Platform(), DataCredit("")) || ContainsApplicationRole(
          Owner(),
          Platform(),
          NamespaceWrite("*")
        )
    ).async { implicit request =>
      dataService.listEndpoints() map { endpoints =>
        Ok(Json.toJson(endpoints))
      }
    }

  def updateRecords(): Action[Seq[EndpointData]] =
    SecuredAction(
      WithRole(DataCredit(""), Owner()) || ContainsApplicationRole(
          NamespaceWrite("*"),
          Owner()
        )
    )
      .async(parsers.json[Seq[EndpointData]]) { implicit request =>
        request2ApplicationStatus(request).flatMap { maybeAppStatus =>
          if (authorizeEndpointDataWrite(request.body, maybeAppStatus))
            dataService.updateRecords(
              request.identity.userId,
              request.body
            ) map { saved =>
              Created(Json.toJson(saved))
            } recover {
              case RichDataMissingException(message, _) =>
                BadRequest(Json.toJson(Errors.dataUpdateMissing(message)))
            }
          else
            Future.failed(
              RichDataPermissionsException(
                "No rights to update some or all of the data requested"
              )
            )
        }
      }

  def registerBundle(bundleId: String): Action[Map[String, PropertyQuery]] =
    SecuredAction(WithRole(Owner()) || ContainsApplicationRole(Owner()))
      .async(parsers.json[Map[String, PropertyQuery]]) { implicit request =>
        bundleService
          .saveBundle(EndpointDataBundle(bundleId, request.body))
          .map { _ =>
            Created(
              Json.toJson(SuccessResponse(s"Bundle $bundleId registered"))
            )
          }
      }

  def fetchBundle(bundleId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner()) || ContainsApplicationRole(Owner())).async { implicit request =>
      val result = for {
        bundle <- bundleService.bundle(bundleId).map(_.get)
        data <- dataService.bundleData(bundle)
      } yield data

      result map { d =>
        Ok(Json.toJson(d))
      } recover {
        case NonFatal(_) =>
          NotFound(Json.toJson(Errors.bundleNotFound(bundleId)))
      }
    }

  def bundleStructure(bundleId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner()) || ContainsApplicationRole(Owner())).async { implicit request =>
      val result = for {
        bundle <- bundleService.bundle(bundleId).map(_.get)
      } yield bundle

      result map { d =>
        Ok(Json.toJson(d))
      } recover {
        case NonFatal(_) =>
          NotFound(Json.toJson(Errors.bundleNotFound(bundleId)))
      }
    }

  def registerDataDebit(dataDebitId: String): Action[DataDebitRequest] =
    SecuredAction(
      WithRole(
        Owner(),
        DataDebitOwner(""),
        Platform()
      ) || ContainsApplicationRole(Owner(), DataDebitOwner(""), Platform())
    ).async(parsers.json[DataDebitRequest]) { implicit request =>
      dataDebitService
        .createDataDebit(dataDebitId, request.body, request.identity.userId)
        .andThen(
          dataEventDispatcher
            .dispatchEventDataDebit(DataDebitOperations.Create())
        )
        .map(debit => Created(Json.toJson(debit)))
        .recover {
          case err: RichDataDuplicateBundleException =>
            BadRequest(Json.toJson(Errors.dataDebitMalformed(err)))
          case err: RichDataDuplicateDebitException =>
            BadRequest(Json.toJson(Errors.dataDebitMalformed(err)))
        }
    }

  def updateDataDebit(dataDebitId: String): Action[DataDebitRequest] =
    SecuredAction(
      WithRole(Owner(), DataDebitOwner(dataDebitId)) || ContainsApplicationRole(
          Owner(),
          DataDebitOwner(dataDebitId)
        )
    ).async(parsers.json[DataDebitRequest]) { implicit request =>
      dataDebitService
        .updateDataDebitBundle(
          dataDebitId,
          request.body,
          request.identity.userId
        )
        .andThen(
          dataEventDispatcher
            .dispatchEventDataDebit(DataDebitOperations.Change())
        )
        .map(debit => Ok(Json.toJson(debit)))
        .recover {
          case err: RichDataServiceException =>
            BadRequest(Json.toJson(Errors.dataDebitMalformed(err)))
        }
    }

  def getDataDebit(dataDebitId: String): Action[AnyContent] =
    SecuredAction(
      WithRole(Owner(), DataDebitOwner(dataDebitId)) || ContainsApplicationRole(
          Owner(),
          DataDebitOwner(dataDebitId)
        )
    ).async { implicit request =>
      dataDebitService
        .dataDebit(dataDebitId)
        .map {
          case Some(debit) => Ok(Json.toJson(debit))
          case None =>
            NotFound(Json.toJson(Errors.dataDebitNotFound(dataDebitId)))
        }
    }

  def getDataDebitValues(dataDebitId: String): Action[AnyContent] =
    SecuredAction(
      WithRole(Owner(), DataDebitOwner(dataDebitId)) || ContainsApplicationRole(
          Owner(),
          DataDebitOwner(dataDebitId)
        )
    ).async { implicit request =>
      dataDebitService
        .dataDebit(dataDebitId)
        .flatMap {
          case Some(debit) if debit.activeBundle.isDefined =>
            logger.debug("Got Data Debit, fetching data")
            val eventualData = debit.activeBundle.get.conditions map { bundleConditions =>
              logger.debug("Getting data for conditions")
              dataService.bundleData(bundleConditions).flatMap { conditionValues =>
                val conditionFulfillment: Map[String, Boolean] =
                  conditionValues map {
                      case (condition, values) =>
                        (condition, values.nonEmpty)
                    }

                if (conditionFulfillment.forall(_._2)) {
                  logger
                    .debug(s"Data Debit $dataDebitId conditions satisfied")
                  dataService
                    .bundleData(debit.activeBundle.get.bundle)
                    .map(RichDataDebitData(Some(conditionFulfillment), _))
                } else {
                  logger.debug(
                    s"Data Debit $dataDebitId conditions not satisfied: $conditionFulfillment"
                  )
                  Future.successful(
                    RichDataDebitData(Some(conditionFulfillment), Map())
                  )
                }
              }

            } getOrElse {
              logger.debug(s"Data Debit $dataDebitId without conditions")
              dataService
                .bundleData(debit.activeBundle.get.bundle)
                .map(RichDataDebitData(None, _))
            }

            eventualData
              .andThen(dataEventDispatcher.dispatchEventDataDebitValues(debit))
              .map(d => Ok(Json.toJson(d)))

          case Some(_) =>
            Future.successful(
              BadRequest(Json.toJson(Errors.dataDebitNotEnabled(dataDebitId)))
            )
          case None =>
            Future.successful(
              NotFound(Json.toJson(Errors.dataDebitNotFound(dataDebitId)))
            )
        }
        .recover {
          case err: RichDataBundleFormatException =>
            BadRequest(
              Json.toJson(Errors.dataDebitBundleMalformed(dataDebitId, err))
            )
        }
    }

  def listDataDebits(): Action[AnyContent] =
    SecuredAction(WithRole(Owner()) || ContainsApplicationRole(Owner())).async { implicit request =>
      dataDebitService.all map { debits =>
        Ok(Json.toJson(debits))
      }
    }

  def enableDataDebitBundle(
      dataDebitId: String,
      bundleId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner()) || ContainsApplicationRole(Owner())).async { implicit request =>
      enableDataDebit(dataDebitId, Some(bundleId))
    }

  def enableDataDebitNewest(dataDebitId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner()) || ContainsApplicationRole(Owner())).async { implicit request =>
      enableDataDebit(dataDebitId, None)
    }

  protected def enableDataDebit(
      dataDebitId: String,
      bundleId: Option[String]
    )(implicit request: SecuredRequest[HatApiAuthEnvironment, AnyContent]): Future[Result] = {
    val enabled = for {
      _ <- dataDebitService.dataDebitEnableBundle(dataDebitId, bundleId)
      debit <- dataDebitService.dataDebit(dataDebitId)
    } yield debit

    enabled
      .andThen(
        dataEventDispatcher
          .dispatchEventMaybeDataDebit(DataDebitOperations.Enable())
      )
      .map {
        case Some(debit) => Ok(Json.toJson(debit))
        case None        => BadRequest(Json.toJson(Errors.dataDebitDoesNotExist))
      }
  }

  def disableDataDebit(dataDebitId: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner()) || ContainsApplicationRole(Owner())).async { implicit request =>
      val disabled = for {
        _ <- dataDebitService.dataDebitDisable(dataDebitId)
        debit <- dataDebitService.dataDebit(dataDebitId)
      } yield debit

      disabled
        .andThen(
          dataEventDispatcher
            .dispatchEventMaybeDataDebit(DataDebitOperations.Disable())
        )
        .map {
          case Some(debit) => Ok(Json.toJson(debit))
          case None        => BadRequest(Json.toJson(Errors.dataDebitDoesNotExist))
        }
    }

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

  // ------------------------------------------------------------------------------------
  // Contracts

  // ** TODO this should be in a better place.
  sealed abstract class ContractVerificationFailure
  object ContractVerificationFailure {
    final case class ServiceRespondedWithFailure(failureDescription: String) extends ContractVerificationFailure
    final case class InvalidTokenFailure(failureDescription: String) extends ContractVerificationFailure
    final case class InvalidContractDataRequestFailure(
        failureDescription: String)
        extends ContractVerificationFailure
  }
  sealed abstract class ContractVerificationSuccess
  object ContractVerificationSuccess {
    final case class JwtClaimVerified(jwtClaim: JwtClaim) extends ContractVerificationSuccess
  }

  trait ContractRequest {
    def refineContract: ContractDataInfoRefined
  }

  // New Consolidated Contract Types

  // Extract this from each of the types below
  case class ContractDataInfoRefined(
      token: ShortLivedToken,
      hatName: HatName,
      contractId: ContractId)
      extends ContractRequest

  case class ContractDataReadRequest(
      token: String,
      hatName: String,
      contractId: String)

  case class ContractDataCreateRequest(
      token: String,
      hatName: String,
      contractId: String,
      body: Option[JsValue])

  case class ContractDataUpdateRequest(
      token: String,
      hatName: String,
      contractId: String,
      body: Option[JsValue])

  def refineContractInfo(
      token: String,
      hatName: String,
      contractId: String): Option[ContractDataInfoRefined] =
    for {
      tokenR <- refineV[NonEmpty](token).toOption
      hatNameR <- refineV[NonEmpty](hatName).toOption
      contractIdR <- refineV[NonEmpty](contractId).toOption
      contractDataInfoRefined = ContractDataInfoRefined(
                                  ShortLivedToken(tokenR),
                                  HatName(hatNameR),
                                  ContractId(UUID.fromString(contractIdR))
                                )
    } yield contractDataInfoRefined

  def refineContractDataReadRequest(req: ContractDataReadRequest): Option[ContractDataInfoRefined] =
    refineContractInfo(req.token, req.hatName, req.contractId)

  def refineContractDataCreateRequest(req: ContractDataCreateRequest): Option[ContractDataInfoRefined] =
    refineContractInfo(req.token, req.hatName, req.contractId)

  def refineContractDataUpdateRequest(req: ContractDataUpdateRequest): Option[ContractDataInfoRefined] =
    refineContractInfo(req.token, req.hatName, req.contractId)

  // ** TODO this should be in a better place.
  // final case class ContractDataRequest(token: ShortLivedToken, hatName: HatName, contractId: ContractId)
  case class ContractRequestBodyRefined(
      token: ShortLivedToken,
      hatName: HatName,
      contractId: ContractId,
      body: Option[JsValue])
  case class ContractRequestBody(
      token: String,
      hatName: String,
      contractId: String,
      body: Option[JsValue])
  implicit val contractRequestBodyReads: Reads[ContractRequestBody] = Json.reads[ContractRequestBody]

  // -- Contract Data: Write --
  def saveContractData(
      namespace: String,
      endpoint: String,
      skipErrors: Option[Boolean]): Action[ContractRequestBody] =
    UserAwareAction.async(parsers.json[ContractRequestBody]) { implicit request =>
      val contractRequestBody = request.body
      contractRequestBody.body match {
        // There was no body to store, so fail quickly
        case None =>
          logger.error {
            s"saveContractData included no json body - ns:${namespace} - endpoint:${endpoint}"
          }
          Future.successful(NotFound)

        // Body exists, so verify the request and process the body
        case Some(jsonBody) =>
          usersService.getUser(contractRequestBody.hatName).flatMap { hatUser =>
            hatUser match {
              case None =>
                logger.error {
                  s"handleJsArray - No user found for ${contractRequestBody.hatName}"
                }
                Future.successful(BadRequest("No user found."))
              case Some(hatUser) =>
                val dataEndpoint =
                  s"$namespace/$endpoint"
                val requestIsAllowed =
                  assessRequest(contractRequestBody, namespace)

                requestIsAllowed.flatMap {
                  case Right(RequestVerified(ns)) =>
                    logger.info(s"RequestVerified: ${ns}")
                    jsonBody match {
                      case array: JsArray =>
                        logger.info {
                          s"saveContractData.body is JsArray: ${jsonBody}"
                        }
                        handleJsArray(
                          hatUser.userId,
                          array,
                          dataEndpoint,
                          skipErrors
                        )
                      case value: JsValue =>
                        logger.info {
                          s"saveContractData.body is JsValue: ${jsonBody}"
                        }
                        handleJsValue(
                          hatUser.userId,
                          value,
                          dataEndpoint
                        )
                    }
                  case Left(x) =>
                    logger.error {
                      s"Contract Save Error: ${x} - ${namespace} - ${endpoint} - ${contractRequestBody}"
                    }
                    Future.successful(NotFound)
                } recover {
                  case e: RichDataDuplicateException =>
                    BadRequest(Json.toJson(Errors.richDataDuplicate(e)))
                  case e: RichDataServiceException =>
                    BadRequest(Json.toJson(Errors.richDataError(e)))
                  case e: Exception =>
                    logger.error(e.getMessage)
                    BadRequest("Contract Data request creation failure.")
                }
            }
          }
      }
    }

  def handleJsArray(
      userId: UUID,
      array: JsArray,
      dataEndpoint: String,
      skipErrors: Option[Boolean]
    )(implicit hatServer: HatServer): Future[Result] = {
    val values =
      array.value.map(EndpointData(dataEndpoint, None, None, None, _, None))
    dataService
      .saveData(userId, values, skipErrors.getOrElse(false))
      .map(saved => Created(Json.toJson(saved)))
  }

  def handleJsValue(
      userId: UUID,
      value: JsValue,
      dataEndpoint: String
    )(implicit hatServer: HatServer): Future[Result] = {
    val values = Seq(EndpointData(dataEndpoint, None, None, None, value, None))
    dataService
      .saveData(userId, values)
      .map(saved => Created(Json.toJson(saved.head)))
  }

  // -- Contract Data: Update --
  case class ContractUpdateBody(
      token: String,
      hatName: String,
      contractId: String,
      body: Seq[ContractUpdateData])
  implicit val contractUpdateBodyReads: Reads[ContractUpdateBody] = Json.reads[ContractUpdateBody]

  case class ContractUpdateData(
      endpoint: String,
      recordId: UUID,
      data: JsValue)
  implicit val contractUpdateDataReads: Reads[ContractUpdateData] = Json.reads[ContractUpdateData]

  // -- Contract Data: Update
  def updateContractData(
      namespace: String,
      endpoint: String,
      skipErrors: Option[Boolean]): Action[ContractUpdateBody] =
    UserAwareAction.async(parsers.json[ContractUpdateBody]) { implicit request =>
      val contractUpdateBody = request.body

      if (contractUpdateBody.body.isEmpty) {
        logger.error {
          s"updateContractData included no Endpoint Data body - ns:${namespace} - endpoint:${endpoint}"
        }
        Future.successful(NotFound)
      } else
        usersService.getUser(contractUpdateBody.hatName).flatMap { hatUser =>
          hatUser match {
            case None =>
              logger.error {
                s"handleJsArray - No user found for ${contractUpdateBody.hatName}"
              }
              Future.successful(BadRequest("No hat found"))
            case Some(hatUser) =>
              val dataEndpoint =
                s"$namespace/$endpoint"
              println(dataEndpoint)

              val requestIsAllowed =
                assessRequest(contractUpdateBody, namespace)

              Future.successful(Ok("No hat found"))
          }
        }
    }

  def verifyNamespace(
      app: Application,
      namespace: String): Boolean = {
    logger.info(s"verifyNamespace ${namespace} for app ${app}")

    val canReadNamespace  = verifyNamespaceRead(app, namespace)
    val canWriteNamespace = verifyNamespaceWrite(app, namespace)

    logger.info(
      s"def verifyNamespace read: ${canReadNamespace} - write: ${canWriteNamespace}"
    )

    (canReadNamespace || canWriteNamespace)
  }

  def verifyNamespaceRead(
      app: Application,
      namespace: String): Boolean = {
    val rolesOk = NamespaceUtils.testReadNamespacePermissions(app.permissions.rolesGranted, namespace)
    logger.info(
      s"NamespaceRead: AppPerms: ${app.permissions}, RolesOk: ${rolesOk}, namespace: ${namespace}, result: ${rolesOk}"
    )

    rolesOk
  }

  def verifyNamespaceWrite(
      app: Application,
      namespace: String): Boolean = {
    val rolesOk = NamespaceUtils.testWriteNamespacePermissions(app.permissions.rolesGranted, namespace)
    logger.info(
      s"NamespaceWrite: AppPerms: ${app.permissions}, RolesOk: ${rolesOk}, namespace: ${namespace}, result: ${rolesOk}"
    )

    rolesOk
  }

  // Convert the basic JSON representation of the ContactRequestBody to the Refined Version
  private def requestBodyToContractDataRequest(
      contractRequestBody: ContractRequestBody): Option[ContractRequestBodyRefined] =
    for {
      token <- refineV[NonEmpty]((contractRequestBody.token)).toOption
      hatName <- refineV[NonEmpty]((contractRequestBody.hatName)).toOption
      contractId <- refineV[NonEmpty]((contractRequestBody.contractId)).toOption
      contractRequestBodyRefined = ContractRequestBodyRefined(
                                     ShortLivedToken(token),
                                     HatName(hatName),
                                     ContractId(UUID.fromString(contractId)),
                                     None
                                   )
    } yield contractRequestBodyRefined

  // TODO: Use KeyId
  private def requestKeyId(
      contractRequestBody: ContractRequestBody): Option[String] =
    for {
      keyId <- ShortLivedTokenOps
                 .getKeyId(contractRequestBody.token)
                 .toOption
    } yield keyId

  def verifyContract(contractRequestBody: ContractRequestBody): Future[
    Either[ContractVerificationFailure, ContractVerificationSuccess]
  ] = {
    import ContractVerificationFailure._

    // This logic is tied up with special types
    val maybeContractDataRequestKeyId = for {
      contractDataRequest <- requestBodyToContractDataRequest(
                               contractRequestBody
                             )
      keyId <- requestKeyId(contractRequestBody)
    } yield (contractDataRequest, keyId)

    maybeContractDataRequestKeyId match {
      case Some((contractDataRequest, keyId)) =>
        logger.info(s"ContractData-keyId: ${keyId}")
        verifyTokenWithAdjudicator(contractDataRequest, keyId)
      case _ =>
        Future.successful(
          Left(
            InvalidContractDataRequestFailure(
              "Contract Data Request or KeyId missing"
            )
          )
        )
    }
  }

  def verifyJwtClaim(
      contractRequestBodyRefined: ContractRequestBodyRefined,
      publicKeyAsByteArray: Array[Byte]): Either[ContractVerificationFailure, ContractVerificationSuccess] = {
    import ContractVerificationFailure._
    import ContractVerificationSuccess._

    logger.error(
      s"ContractData.verifyJwtClaim.token: ${contractRequestBodyRefined.token.toString}"
    )
    logger.error(
      s"ContractData.verifyJwtClaim.pubKey: ${publicKeyAsByteArray}"
    )

    val tryJwtClaim = ShortLivedTokenOps.verifyToken(
      Some(contractRequestBodyRefined.token.toString),
      publicKeyAsByteArray
    )
    tryJwtClaim match {
      case Success(jwtClaim) => Right(JwtClaimVerified(jwtClaim))
      case Failure(errorMsg) =>
        logger.error(
          s"ContractData.verifyJwtClaim.failureMessage: ${errorMsg.getMessage}"
        )
        Left(
          InvalidTokenFailure(
            s"Token: ${contractRequestBodyRefined.token.toString} was not verified."
          )
        )
    }
  }

  def verifyTokenWithAdjudicator(
      contractRequestBodyRefined: ContractRequestBodyRefined,
      keyId: String): Future[
    Either[ContractVerificationFailure, ContractVerificationSuccess]
  ] =
    adjudicatorClient
      .getPublicKey(
        contractRequestBodyRefined.hatName,
        contractRequestBodyRefined.contractId,
        keyId
      )
      .map { publicKeyResponse =>
        publicKeyResponse match {
          case Left(
                PublicKeyRequestFailure.ServiceRespondedWithFailure(
                  failureDescription
                )
              ) =>
            Left(
              ContractVerificationFailure.ServiceRespondedWithFailure(
                s"The Adjudicator Service responded with an error: ${failureDescription}"
              )
            )
          case Left(
                PublicKeyRequestFailure.InvalidPublicKeyFailure(
                  failureDescription
                )
              ) =>
            Left(
              ContractVerificationFailure.ServiceRespondedWithFailure(
                s"The Adjudicator Service responded with an error: ${failureDescription}"
              )
            )
          case Right(PublicKeyReceived(publicKey)) =>
            verifyJwtClaim(contractRequestBodyRefined, publicKey)
        }
      }

  def getContractData(
      namespace: String,
      endpoint: String,
      orderBy: Option[String],
      ordering: Option[String],
      skip: Option[Int],
      take: Option[Int]): Action[ContractRequestBody] =
    UserAwareAction.async(parsers.json[ContractRequestBody]) { implicit request =>
      val contractRequestBody = request.body
      val requestIsAllowed    = assessRequest(contractRequestBody, namespace)

      requestIsAllowed.flatMap { testResult =>
        testResult match {
          case Right(RequestVerified(ns @ _)) =>
            makeData(namespace, endpoint, orderBy, ordering, skip, take)
          case Left(contractError) =>
            logger.error(
              s"Contract Get Error: ${contractError} - ${namespace} - ${endpoint} - ${contractRequestBody}"
            )
            Future.successful(NotFound)
        }
      } recover {
        case _ =>
          logger.error(
            s"Contract Get Error: Request Not Allowed - ${namespace} - ${endpoint} - ${contractRequestBody}"
          )
          NotFound
      }
    }

  // This is tied to ContractRequestBody
  // Let's generalize it
  // - we need the contractId
  // -
  def assessRequest(
      contractRequestBody: ContractRequestBody,
      namespace: String): Future[Either[RequestValidationFailure, RequestVerified]] = {
    val eventuallyMaybeDecision = verifyContract(contractRequestBody)
    val eventuallyMaybeApp =
      trustedApplicationProvider.application(contractRequestBody.contractId)

    eventuallyMaybeDecision.flatMap { maybeDecision =>
      eventuallyMaybeApp.flatMap { maybeApp =>
        logger.info(
          s"AssessRequest: ${maybeDecision} - ${maybeApp} - ${namespace}"
        )
        decide(maybeDecision, maybeApp, namespace) match {
          case Some(ns) =>
            logger.info(s"Found a namespace: ${ns}")
            Future.successful(
              Right(
                RequestVerified(s"Token: ${contractRequestBody.contractId}")
              )
            )
          case None =>
            logger.error(
              s"def assessRequest: decide returned None - ${contractRequestBody} - ${namespace}"
            )
            Future.successful(
              Left(
                RequestValidationFailure.InvalidShortLivedToken(
                  s"Token: ${contractRequestBody.contractId}"
                )
              )
            )
        }
      }
    } recover {
      case e =>
        logger.error(s"ContractData.assessRequest:Failure ${e}")
        Left(
          RequestValidationFailure.InvalidShortLivedToken(
            s"Token: ${contractRequestBody.contractId}"
          )
        )
    }
  }

  def decide(
      eitherDecision: Either[
        ContractVerificationFailure,
        ContractVerificationSuccess
      ],
      maybeApp: Option[Application],
      namespace: String): Option[String] = {
    import ContractVerificationSuccess._

    logger.info(
      s"def decide: decision: ${eitherDecision} - app: ${maybeApp} - ns: ${namespace}"
    )

    (eitherDecision, maybeApp) match {
      case (Right(JwtClaimVerified(_jwtClaim @ _)), Some(app)) =>
        logger.info(s"def decide: JwtClaim verified for app ${app}")
        if (verifyNamespace(app, namespace))
          Some(namespace)
        else
          None
      case (Left(decision), _) =>
        logger.error(s"def decide: decision: ${decision}")
        None
    }
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
