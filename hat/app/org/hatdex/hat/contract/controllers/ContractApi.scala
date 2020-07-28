package org.hatdex.hat.contract.controllers

import java.util.UUID

import dev.profunktor.auth.jwt.JwtSecretKey
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import org.hatdex.hat.api.controllers.{ RequestValidationFailure, RequestVerified }
import org.hatdex.hat.api.json.RichDataJsonFormats
import org.hatdex.hat.api.models.{ EndpointData, EndpointQuery, UserRole }
import org.hatdex.hat.api.models.applications.Application
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.api.service.applications.{ ApplicationsService, TrustedApplicationProvider }
import org.hatdex.hat.api.service.monitoring.HatDataEventDispatcher
import org.hatdex.hat.api.service.richData.{ DataDebitContractService, RichBundleService, RichDataDuplicateException, RichDataService, RichDataServiceException }
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.contract.models.{ ContractRequestBody, ContractRequestBodyRefined, ContractVerificationFailure, ContractVerificationSuccess, Errors }
import org.hatdex.hat.utils.AdjudicatorRequestTypes.{ PublicKeyReceived, PublicKeyRequestFailure }
import org.hatdex.hat.utils.{ AdjudicatorRequest, HatBodyParsers, LoggingProvider }
import play.api.Configuration
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined._
import io.dataswift.adjudicator.ShortLivedTokenOps
import io.dataswift.adjudicator.Types.{ ContractId, HatName, ShortLivedToken }
import play.api.libs.json.{ JsArray, JsValue, Json }
import play.api.libs.ws.WSClient
import play.api.mvc.{ Action, ControllerComponents, Result }
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.libs.dal.HATPostgresProfile

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class ContractApi @Inject() (
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
    implicit val applicationsService: ApplicationsService)(wsClient: WSClient)
  extends HatApiController(components, silhouette)
  with RichDataJsonFormats {

  private val logger = loggingProvider.logger(this.getClass)
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
  private val adjudicatorClient = new AdjudicatorRequest(adjudicatorEndpoint, JwtSecretKey(adjudicatorSharedSecret), wsClient)

  implicit val contractRequestBodyReads = Json.reads[ContractRequestBody]

  def saveContractData(
    namespace: String,
    endpoint: String,
    skipErrors: Option[Boolean]): Action[ContractRequestBody] =
    UserAwareAction.async(parsers.json[ContractRequestBody]) { implicit request =>
      val contractRequestBody = request.body
      val dataEndpoint = s"$namespace/$endpoint"
      val requestIsAllowed = assessRequest(contractRequestBody, namespace)

      requestIsAllowed.flatMap { testResult =>
        testResult match {
          case Right(RequestVerified(ns)) => {
            contractRequestBody.body match {
              case array: JsArray => handleJsArray(contractRequestBody.hatName, array, dataEndpoint, skipErrors)
              case value: JsValue => handleJsValue(contractRequestBody.hatName, value, dataEndpoint)
            }
          }
          case _ => Future.successful(NotFound)
        }
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

  def handleJsArray(hatName: String, array: JsArray, dataEndpoint: String, skipErrors: Option[Boolean])(implicit hatServer: HatServer) = {
    val values = array.value.map(EndpointData(dataEndpoint, None, None, None, _, None))
    usersService.getUser(hatName).flatMap { hatUser =>
      hatUser match {
        case Some(hatUser) => {
          dataService
            .saveData(hatUser.userId, values, skipErrors.getOrElse(false))
            .map(saved => Created(Json.toJson(saved)))
        }
        case None => {
          Future.successful(BadRequest("No user found."))
        }
      }
    }
  }

  def handleJsValue(hatName: String, value: JsValue, dataEndpoint: String)(implicit hatServer: HatServer) = {
    val values = Seq(EndpointData(dataEndpoint, None, None, None, value, None))
    usersService.getUser(hatName).flatMap { hatUser =>
      hatUser match {
        case Some(hatUser) => {
          dataService
            .saveData(hatUser.userId, values)
            .map(saved => Created(Json.toJson(saved.head)))
        }
        case None => {
          Future.successful(BadRequest("No user found."))
        }
      }
    }
  }

  def verifyNamespace(
    app: Application,
    namespace: String): Option[String] = {
    val roles = app.permissions.rolesGranted.map(r => UserRole.userRoleDeserialize(r.name, r.extra))
    if (roles.exists(_.name == namespace))
      Some(namespace)
    else
      None
  }

  // Convert the basic JSON representation of the ContactRequestBody to the Refined Version
  private def requestBodyToContractDataRequest(contractRequestBody: ContractRequestBody): Option[ContractRequestBodyRefined] =
    for {
      token <- refineV[NonEmpty]((contractRequestBody.token)).toOption
      hatName <- refineV[NonEmpty]((contractRequestBody.hatName)).toOption
      contractId <- refineV[NonEmpty]((contractRequestBody.contractId)).toOption
      contractRequestBodyRefined = ContractRequestBodyRefined(ShortLivedToken(token), HatName(hatName), ContractId(UUID.fromString(contractId)), None)
    } yield contractRequestBodyRefined

  // TODO: Use KeyId
  private def requestKeyId(contractRequestBody: ContractRequestBody): Option[String] =
    for {
      keyId <- ShortLivedTokenOps
        .getKeyId(contractRequestBody.token)
        .toOption
    } yield keyId

  def verifyContract(contractRequestBody: ContractRequestBody): Future[Either[ContractVerificationFailure, ContractVerificationSuccess]] = {
    import ContractVerificationFailure._

    val maybeContractDataRequestKeyId = for {
      contractDataRequest <- requestBodyToContractDataRequest(contractRequestBody)
      keyId <- requestKeyId(contractRequestBody)
    } yield (contractDataRequest, keyId)

    maybeContractDataRequestKeyId match {
      case Some((contractDataRequest, keyId)) =>
        verifyTokenWithAdjudicator(contractDataRequest, keyId)
      case _ => Future.successful(Left(InvalidContractDataRequestFailure("Contract Data Request or KeyId missing")))
    }
  }

  def verifyJwtClaim(contractRequestBodyRefined: ContractRequestBodyRefined, publicKeyAsByteArray: Array[Byte]): Either[ContractVerificationFailure, ContractVerificationSuccess] = {
    import ContractVerificationFailure._
    import ContractVerificationSuccess._

    val tryJwtClaim = ShortLivedTokenOps.verifyToken(
      Some(contractRequestBodyRefined.token.toString),
      publicKeyAsByteArray)
    tryJwtClaim match {
      case Success(jwtClaim) => Right(JwtClaimVerified(jwtClaim))
      case Failure(_) =>
        Left(InvalidTokenFailure(s"Token: ${contractRequestBodyRefined.token.toString} was not verified."))
    }
  }

  def verifyTokenWithAdjudicator(
    contractRequestBodyRefined: ContractRequestBodyRefined,
    keyId: String): Future[Either[ContractVerificationFailure, ContractVerificationSuccess]] = {

    adjudicatorClient.getPublicKey(contractRequestBodyRefined.hatName, contractRequestBodyRefined.contractId, keyId).map { publicKeyReqsponse =>
      {
        publicKeyReqsponse match {
          case Left(PublicKeyRequestFailure.ServiceRespondedWithFailure(failureDescription)) => {
            Left(ContractVerificationFailure.ServiceRespondedWithFailure(s"The Adjudicator Service responded with an error: ${failureDescription}"))
          }
          case Left(PublicKeyRequestFailure.InvalidPublicKeyFailure(failureDescription)) => {
            Left(ContractVerificationFailure.ServiceRespondedWithFailure(s"The Adjudicator Service responded with an error: ${failureDescription}"))
          }
          case Right(PublicKeyReceived(publicKey)) => verifyJwtClaim(contractRequestBodyRefined, publicKey)
        }
      }
    }
  }

  /* *** Contract Data *** */

  def getContractData(
    namespace: String,
    endpoint: String,
    orderBy: Option[String],
    ordering: Option[String],
    skip: Option[Int],
    take: Option[Int]): Action[ContractRequestBody] =
    UserAwareAction.async(parsers.json[ContractRequestBody]) { implicit request =>
      val contractRequestBody = request.body
      val requestIsAllowed = assessRequest(contractRequestBody, namespace)

      requestIsAllowed.flatMap { testResult =>
        testResult match {
          case Right(RequestVerified(ns)) => makeData(namespace, endpoint, orderBy, ordering, skip, take)
          case _                          => Future.successful(NotFound)
        }
      } recover {
        case _ => NotFound
      }
    }

  def assessRequest(
    contractRequestBody: ContractRequestBody,
    namespace: String): Future[Either[RequestValidationFailure, RequestVerified]] = {
    val eventuallyMaybeDecision = verifyContract(contractRequestBody)
    val eventuallyMaybeApp = trustedApplicationProvider.application(contractRequestBody.contractId)

    eventuallyMaybeDecision.flatMap { maybeDecision =>
      eventuallyMaybeApp.flatMap { maybeApp =>
        decide(maybeDecision, maybeApp, namespace) match {
          case Some(_ns) => Future.successful(Right(RequestVerified(s"Token: ${contractRequestBody.contractId}")))
          case None      => Future.successful(Left(RequestValidationFailure.InvalidShortLivedToken(s"Token: ${contractRequestBody.contractId}")))
        }
      }
    } recover {
      case _e => Left(RequestValidationFailure.InvalidShortLivedToken(s"Token: ${contractRequestBody.contractId}"))
    }
  }

  def decide(eitherDecision: Either[ContractVerificationFailure, ContractVerificationSuccess], maybeApp: Option[Application], namespace: String): Option[String] = {
    import ContractVerificationSuccess._

    (eitherDecision, maybeApp) match {
      case (Right(JwtClaimVerified(_jwtClaim)), Some(app)) => {
        verifyNamespace(app, namespace)
      }
      case (_, _) => {
        None
      }
    }
  }

  private def makeData(
    namespace: String,
    endpoint: String,
    orderBy: Option[String],
    ordering: Option[String],
    skip: Option[Int],
    take: Option[Int])(implicit db: HATPostgresProfile.api.Database): Future[Result] = {
    val dataEndpoint = s"$namespace/$endpoint"
    val query =
      Seq(EndpointQuery(dataEndpoint, None, None, None))
    val data = dataService.propertyData(
      query,
      orderBy,
      ordering.contains("descending"),
      skip.getOrElse(0),
      take.orElse(Some(defaultRecordLimit)))
    data.map(d => Ok(Json.toJson(d)))
  }
}
