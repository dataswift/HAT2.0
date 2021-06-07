package org.hatdex.hat.api.controllers.v2

import com.mohiva.play.silhouette.api.Silhouette
import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
import io.dataswift.adjudicator.ShortLivedTokenOps
import io.dataswift.adjudicator.Types.{ HatName, ShortLivedToken, _ }
import io.dataswift.models.hat.applications.Application
import org.hatdex.hat.api.controllers.common._
import org.hatdex.hat.api.service.UserService
import org.hatdex.hat.api.service.applications.TrustedApplicationProvider
import org.hatdex.hat.authentication._
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.client.AdjudicatorClient
import org.hatdex.hat.client.AdjudicatorRequestTypes._
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.hat.utils.NamespaceUtils
import pdi.jwt._
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }
// -- LIB

class ContractActionImpl @Inject() (
    components: ControllerComponents,
    silhouette: Silhouette[HatApiAuthEnvironment],
    userService: UserService,
    trustedApplicationProvider: TrustedApplicationProvider,
    adjudicatorClient: AdjudicatorClient,
    contractDataOperations: ContractDataOperations
  )(implicit ec: ExecutionContext)
    extends HatApiController(components, silhouette)
    with Logging
    with ContractAction {

  override def doWithToken(
      maybeNamespace: Option[String],
      permissions: RequiredNamespacePermissions
    )(contractAction: (HatUser, HatServer, Option[HatApiAuthEnvironment]) => Future[Result]): Action[AnyContent] =
    UserAwareAction.async { implicit request =>
      val hatName   = request.dynamicEnvironment.hatName
      val namespace = maybeNamespace.getOrElse("")

      val maybeTokenUserAndApp: Future[(Option[MachineData.SLTokenBody], Option[HatUser], Option[Application])] = for {
        sltoken <- getTokenFromHeaders(request.headers)
        user <- userService.getUser(hatName)
        app <- trustedApplicationProvider.application(sltoken.map(_.deviceId).getOrElse(""))
      } yield (sltoken, user, app)

      maybeTokenUserAndApp flatMap {
        case (Some(sltoken), Some(user), Some(app)) =>
          verifyApplicationAndNamespace(sltoken.toString(), app, namespace, hatName, permissions).flatMap {
            appAndNamespaceOk =>
              appAndNamespaceOk match {
                case Left(_) =>
                  logger.info(s"doWithToken - App-namespace failure: App:${app.id} Namespace: ${namespace} ")
                  contractDataOperations.handleFailedRequestAssessment(GeneralError)
                case Right(_) =>
                  contractDataOperations.handleFailedRequestAssessment(GeneralError)
                //contractAction(request.body, user, request.dynamicEnvironment, request.authenticator)

              }
          }
        case (None, _, _) =>
          logger.info("doWithToken - Missing X-Auth-Token.")
          contractDataOperations.handleFailedRequestAssessment(XAuthTokenMissing)
        case (_, None, _) =>
          logger.info("doWithToken - Missing User.")
          contractDataOperations.handleFailedRequestAssessment(HatNotFound(request.dynamicEnvironment.hatName))
        case (_, _, None) =>
          logger.info("doWithToken - Missing Application.")
          contractDataOperations.handleFailedRequestAssessment(ApplicationNotFound)
        case (_, _, _) =>
          logger.info("doWithToken - Fallback Error")
          contractDataOperations.handleFailedRequestAssessment(GeneralError)
      }
    }

  override def doWithToken[A](
      parser: BodyParser[A],
      maybeNamespace: Option[String],
      permissions: RequiredNamespacePermissions
    )(contractAction: (A, HatUser, HatServer, Option[HatApiAuthEnvironment#A]) => Future[Result]): Action[A] =
    UserAwareAction.async(parser) { implicit request =>
      val hatName   = request.dynamicEnvironment.hatName
      val namespace = maybeNamespace.getOrElse("")

      val maybeTokenUserAndApp: Future[(Option[MachineData.SLTokenBody], Option[HatUser], Option[Application])] = for {
        sltoken <- getTokenFromHeaders(request.headers)
        user <- userService.getUser(hatName)
        app <- trustedApplicationProvider.application(sltoken.map(_.deviceId).getOrElse(""))
      } yield (sltoken, user, app)

      maybeTokenUserAndApp flatMap {
        case (Some(sltoken), Some(user), Some(app)) =>
          verifyApplicationAndNamespace(sltoken.toString(), app, namespace, hatName, permissions).flatMap {
            appAndNamespaceOk =>
              appAndNamespaceOk match {
                case Left(_) =>
                  logger.info(s"doWithToken - App-namespace failure: App:${app.id} Namespace: ${namespace} ")
                  contractDataOperations.handleFailedRequestAssessment(GeneralError)
                case Right(_) =>
                  contractAction(request.body, user, request.dynamicEnvironment, request.authenticator)

              }
          }
        case (None, _, _) =>
          logger.info("doWithToken - Missing X-Auth-Token.")
          contractDataOperations.handleFailedRequestAssessment(XAuthTokenMissing)
        case (_, None, _) =>
          logger.info("doWithToken - Missing User.")
          contractDataOperations.handleFailedRequestAssessment(HatNotFound(request.dynamicEnvironment.hatName))
        case (_, _, None) =>
          logger.info("doWithToken - Missing Application.")
          contractDataOperations.handleFailedRequestAssessment(ApplicationNotFound)
        case (_, _, _) =>
          logger.info("doWithToken - Fallback Error")
          contractDataOperations.handleFailedRequestAssessment(GeneralError)
      }
    }

  // -------------------
  // TO LIB
  // ------------------

  private val jwtRegex = """(.+?)\.(.+?)\.(.+?)""".r

  // JWT tokens come in three parts, split by a "."
  // see [[jwtRegex]]
  private def splitToken(jwt: String): Try[(String, String, String)] =
    jwt match {
      case jwtRegex(header, body, sig) => Success((header, body, sig))
      case _ =>
        Failure(new Exception("Token does not match the correct pattern"))
    }

  // Components of the JWT are also base64 encoded
  private def decodeElements(data: Try[(String, String, String)]): Try[(String, String, String)] =
    data map {
        case (header, body, sig) =>
          (JwtBase64.decodeString(header), JwtBase64.decodeString(body), sig)
      }

  def getBody(token: String): Try[String] =
    (splitToken _ andThen decodeElements)(token) flatMap {
        case (_, body, _) =>
          Success(body)
      }

  // -----------

  // -- Auth Token handler
  private def getTokenFromHeaders(headers: Headers): Future[Option[MachineData.SLTokenBody]] = {
    val slTokenBody = for {
      xAuthToken <- headers.get("X-Auth-Token")
      slTokenBody <- getBody(xAuthToken).toOption

      ret <- Json.parse(slTokenBody).validate[MachineData.SLTokenBody].asOpt
    } yield ret

    Future.successful(slTokenBody)
  }

  private def verifyApplicationAndNamespace(
      sltoken: String,
      app: Application,
      namespace: String,
      hatName: String,
      permissions: RequiredNamespacePermissions)
      : Future[Either[DeviceVerificationFailure, DeviceVerificationSuccess.DeviceRequestVerificationSuccess]] = {
    val maybeRefinedDevice = refineDeviceInfo(sltoken.trim, hatName, app.id)

    maybeRefinedDevice match {
      case Some(refinedDevice) =>
        verifyDevice(refinedDevice).flatMap {
          case Right(deviceIsVerified @ _) =>
            if (NamespaceUtils.verifyNamespaceWrite(app, namespace))
              Future.successful(
                Right(
                  DeviceVerificationSuccess.DeviceRequestVerificationSuccess(
                    s"Application ${app.id}-${namespace} verified."
                  )
                )
              )
            else
              Future.successful(Left(DeviceVerificationFailure.ApplicationAndNamespaceNotValid))
          case Left(verifyDeviceError) =>
            Future.successful(Left(verifyDeviceError))
        }
      case None =>
        logger.info(s"Failed to refine: ${sltoken} - ${app} - ${namespace} - ${hatName}")
        Future.successful(Left(DeviceVerificationFailure.FailedDeviceRefinement))
    }
  }

  private def contractValid(
      contract: ContractDataInfoRefined,
      maybeNamespace: Option[String],
      isWriteAction: Boolean
    )(implicit hatServer: HatServer): Future[(Option[HatUser], Either[RequestValidationFailure, RequestVerified])] =
    for {
      hatUser <- userService.getUser(contract.hatName.value)
      requestAssessment <- assessRequest(contract, maybeNamespace, isWriteAction)
    } yield (hatUser, requestAssessment)

  private def assessRequest(
      contractDataInfo: ContractDataInfoRefined,
      maybeNamespace: Option[String],
      isWriteAction: Boolean): Future[Either[RequestValidationFailure, RequestVerified]] = {
    val eventuallyMaybeDecision = verifyContract(contractDataInfo)
    val eventuallyMaybeApp =
      trustedApplicationProvider.application(contractDataInfo.contractId.value.toString)

    eventuallyMaybeDecision.flatMap { maybeDecision =>
      eventuallyMaybeApp.flatMap { maybeApp =>
        logger.info(
          s"AssessRequest: decision $maybeDecision - app id ${maybeApp.map(_.id)} - namespace $maybeNamespace"
        )
        if (decide(maybeDecision, maybeApp, maybeNamespace, isWriteAction))
          Future.successful(Right(RequestVerified(s"Token: ${contractDataInfo.contractId}")))
        else {
          logger.info(s"assessRequest: decide returned false - $contractDataInfo - $maybeNamespace")
          Future.successful(Left(InvalidShortLivedToken(s"Token: ${contractDataInfo.contractId}")))
        }
      }
    } recover {
      case e =>
        logger.error(s"ContractData.assessRequest:Failure ${e}")
        Left(InvalidShortLivedToken(s"Token: ${contractDataInfo.contractId}"))
    }
  }

  private def decide(
      eitherDecision: Either[
        ContractVerificationFailure,
        ContractVerificationSuccess
      ],
      maybeApp: Option[Application],
      maybeNamespace: Option[String],
      isWriteAction: Boolean): Boolean =
    (eitherDecision, maybeApp) match {
      case (Right(JwtClaimVerified(_)), Some(app)) =>
        logger.debug(s"JwtClaim verified for app ${app.id}")
        maybeNamespace forall (verifyNamespace(app, _, isWriteAction))
      case (Left(decision), _) =>
        logger.info(s"contract not verified: $decision")
        false
      case (_, _) =>
        logger.info("contract not verified, catch all")
        false
    }

  private def verifyNamespace(
      app: Application,
      namespace: String,
      isWriteAction: Boolean): Boolean =
    if (isWriteAction) NamespaceUtils.testWriteNamespacePermissions(app.permissions.rolesGranted, namespace)
    else NamespaceUtils.testReadNamespacePermissions(app.permissions.rolesGranted, namespace)

  private def verifyContract(contractDataInfo: ContractDataInfoRefined): Future[
    Either[ContractVerificationFailure, ContractVerificationSuccess]
  ] =
    ShortLivedTokenOps.getKeyId(contractDataInfo.token.value).toOption match {
      case Some(keyId) =>
        verifyTokenWithAdjudicator(contractDataInfo, keyId)
      case _ =>
        Future.successful(Left(InvalidContractDataRequestFailure("Contract Data Request or KeyId missing")))
    }

  private def verifyJwtClaim(
      contractRequestBodyRefined: ContractDataInfoRefined,
      publicKeyAsByteArray: Array[Byte]): Either[ContractVerificationFailure, ContractVerificationSuccess] = {
    logger.debug(s"verifyJwtClaim.token: ${contractRequestBodyRefined.token}")
    val tryJwtClaim = ShortLivedTokenOps.verifyToken(
      Some(contractRequestBodyRefined.token.toString),
      publicKeyAsByteArray
    )
    tryJwtClaim match {
      case Success(jwtClaim) => Right(JwtClaimVerified(jwtClaim))
      case Failure(th) =>
        logger.error(s"verifyJwtClaim failed: ${th.getMessage}")
        Left(InvalidTokenFailure(s"Token: ${contractRequestBodyRefined.token} was not verified."))
    }
  }

  private def verifyTokenWithAdjudicator(
      contractRequestBodyRefined: ContractDataInfoRefined,
      keyId: String): Future[
    Either[ContractVerificationFailure, ContractVerificationSuccess]
  ] =
    adjudicatorClient
      .getPublicKey(
        contractRequestBodyRefined.hatName,
        contractRequestBodyRefined.contractId,
        keyId
      )
      .map {
        case Left(PublicKeyServiceFailure(failureDescription)) =>
          Left(ServiceRespondedWithFailure(s"The Adjudicator Service responded with an error: ${failureDescription}"))
        case Left(InvalidPublicKeyFailure(failureDescription)) =>
          Left(ServiceRespondedWithFailure(s"The Adjudicator Service responded with an error: ${failureDescription}"))
        case Right(PublicKeyReceived(publicKey)) =>
          verifyJwtClaim(contractRequestBodyRefined, publicKey)
      }

  private def refineDeviceInfo(
      token: String,
      hatName: String,
      contractId: String): Option[DeviceDataInfoRefined] =
    for {
      tokenR <- refineV[NonEmpty](token).toOption
      hatNameR <- refineV[NonEmpty](hatName).toOption
      contractIdR <- refineV[NonEmpty](contractId).toOption
      uuid <- Try(UUID.fromString(contractIdR.value)).toOption
      deviceDataInfoRefined = DeviceDataInfoRefined(
                                ShortLivedToken(tokenR),
                                HatName(hatNameR),
                                ContractId(uuid)
                              )
    } yield deviceDataInfoRefined

  def verifyDevice(deviceDataInfo: DeviceDataInfoRefined): Future[
    Either[DeviceVerificationFailure, DeviceVerificationSuccess]
  ] = {
    import DeviceVerificationFailure._

    // This logic is tied up with special types
    val maybeDeviceDataRequestKeyId = for {
      keyId <- requestKeyId(deviceDataInfo)
    } yield keyId

    maybeDeviceDataRequestKeyId match {
      case Some(keyId) =>
        logger.info(s"DeviceData-keyId: ${keyId}")
        verifyTokenWithAuthService(deviceDataInfo, keyId)
      case _ =>
        Future.successful(
          Left(
            InvalidDeviceDataRequestFailure(
              "Device Data Request or KeyId missing"
            )
          )
        )
    }
  }

  private def requestKeyId(
      deviceDataInfo: DeviceDataInfoRefined): Option[String] =
    for {
      keyId <- ShortLivedTokenOps
                 .getKeyId(deviceDataInfo.token.value)
                 .toOption
    } yield keyId

  def verifyJwtClaim(
      deviceRequestBodyRefined: DeviceDataInfoRefined,
      publicKeyAsByteArray: Array[Byte]): Either[DeviceVerificationFailure, DeviceVerificationSuccess] = {
    import DeviceVerificationFailure._
    import DeviceVerificationSuccess._

    logger.error(
      s"DeviceData.verifyJwtClaim.token: ${deviceRequestBodyRefined.token.toString}"
    )
    logger.error(
      s"DeviceData.verifyJwtClaim.pubKey: ${publicKeyAsByteArray}"
    )

    val tryJwtClaim = ShortLivedTokenOps.verifyToken(
      Some(deviceRequestBodyRefined.token.toString),
      publicKeyAsByteArray
    )
    tryJwtClaim match {
      case Success(jwtClaim) => Right(JwtClaimVerified(jwtClaim))
      case Failure(errorMsg) =>
        logger.error(
          s"DeviceData.verifyJwtClaim.failureMessage: ${errorMsg.getMessage}"
        )
        Left(
          InvalidTokenFailure(
            s"Token: ${deviceRequestBodyRefined.token.toString} was not verified."
          )
        )
    }
  }

  def verifyTokenWithAuthService(
      deviceRequestBodyRefined: DeviceDataInfoRefined,
      keyId: String): Future[
    Either[DeviceVerificationFailure, DeviceVerificationSuccess]
  ] = {
    println(s"verifyTokenWithAdjudicator: ${deviceRequestBodyRefined}")
    adjudicatorClient
      .getPublicKey(
        deviceRequestBodyRefined.hatName,
        deviceRequestBodyRefined.contractId,
        keyId
      )
      .map { publicKeyResponse =>
        publicKeyResponse match {
          case Left(
                PublicKeyServiceFailure(
                  failureDescription
                )
              ) =>
            Left(
              DeviceVerificationFailure.ServiceRespondedWithFailure(
                s"The AuthService responded with an error: ${failureDescription}"
              )
            )
          case Left(
                InvalidPublicKeyFailure(
                  failureDescription
                )
              ) =>
            Left(
              DeviceVerificationFailure.ServiceRespondedWithFailure(
                s"The  AuthService responded with an error: ${failureDescription}"
              )
            )
          case Right(PublicKeyReceived(publicKey)) =>
            verifyJwtClaim(deviceRequestBodyRefined, publicKey)
        }
      }
  }
  def assessRequest(
      deviceDataInfo: DeviceDataInfoRefined,
      namespace: String,
      trustedApplicationProvider: TrustedApplicationProvider)
      : Future[Either[RequestValidationFailure, RequestVerified]] = {
    val eventuallyMaybeDecision = verifyDevice(deviceDataInfo)
    val eventuallyMaybeApp =
      trustedApplicationProvider.application(deviceDataInfo.contractId.value.toString())

    eventuallyMaybeDecision.flatMap { maybeDecision =>
      eventuallyMaybeApp.flatMap { maybeApp =>
        logger.info(s"AssessRequest: ${maybeDecision} - ${maybeApp} - ${namespace}")

        decide(maybeDecision, maybeApp, namespace) match {
          case Some(ns) =>
            logger.info(s"Found a namespace: ${ns}")
            Future.successful(
              Right(
                RequestVerified(s"Token: ${deviceDataInfo.contractId}")
              )
            )
          case None =>
            logger.error(s"assessRequest: decide returned None - ${deviceDataInfo} - ${namespace}")
            Future.successful(
              Left(
                InvalidShortLivedToken(
                  s"Token: ${deviceDataInfo.contractId}"
                )
              )
            )
        }
      }
    } recover {
      case e =>
        logger.error(s"DeviceData.assessRequest:Failure ${e}")
        Left(
          InvalidShortLivedToken(
            s"Token: ${deviceDataInfo.contractId}"
          )
        )
    }
  }

  def decide(
      eitherDecision: Either[
        DeviceVerificationFailure,
        DeviceVerificationSuccess
      ],
      maybeApp: Option[Application],
      namespace: String): Option[String] = {
    import DeviceVerificationSuccess._

    logger.info(s"def decide: decision: ${eitherDecision} - app: ${maybeApp} - ns: ${namespace}")

    (eitherDecision, maybeApp) match {
      case (Right(JwtClaimVerified(_jwtClaim @ _)), Some(app)) =>
        logger.info(s"def decide: JwtClaim verified for app ${app}")
        if (NamespaceUtils.verifyNamespaceReadWrite(app, namespace))
          Some(namespace)
        else
          None
      case (Left(decision), _) =>
        logger.error(s"def decide: decision: ${decision}")
        None
      case (_, _) =>
        logger.error(s"def decide: decision is no.")
        None
    }
  }

}
