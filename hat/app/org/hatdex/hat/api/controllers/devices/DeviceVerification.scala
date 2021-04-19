package org.hatdex.hat.api.controllers.devices

import dev.profunktor.auth.jwt
import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
import io.dataswift.adjudicator.ShortLivedTokenOps
import io.dataswift.adjudicator.Types.{ DeviceId, HatName, ShortLivedToken }
import io.dataswift.models.hat.applications.Application
import org.hatdex.hat.BearerTokenParser.BearerTokenParser
import org.hatdex.hat.NamespaceUtils.NamespaceUtils
import org.hatdex.hat.api.controllers.devices.Types.DeviceVerificationFailure._
import org.hatdex.hat.api.controllers.devices.Types.DeviceVerificationSuccess._
import org.hatdex.hat.api.controllers.devices.Types._
import org.hatdex.hat.api.controllers.{ MachineData, RequestValidationFailure, RequestVerified }
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.api.service.applications.TrustedApplicationProvider
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.hat.utils.{ AuthServiceRequest, AuthServiceRequestTypes }
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.Headers
import play.api.{ Configuration, Logger }

import scala.concurrent.Future
import scala.util.{ Failure, Success }

import AuthServiceRequestTypes._

class DeviceVerification(
    wsClient: WSClient,
    configuration: Configuration) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  private val logger                                 = Logger(this.getClass)

  // -- AuthService Client
  private val authServiceAddress =
    configuration.underlying.getString("authservice.address")
  private val authServiceScheme =
    configuration.underlying.getString("authservice.scheme")
  private val authServiceEndpoint =
    s"${authServiceScheme}${authServiceAddress}"
  private val authServiceSharedSecret =
    configuration.underlying.getString("authservice.sharedSecret")
  private val authServiceClient = new AuthServiceRequest(
    authServiceEndpoint,
    jwt.JwtSecretKey(authServiceSharedSecret),
    wsClient
  )

  // -- Auth Token handler
  def getTokenFromHeaders(headers: Headers): Future[Option[MachineData.SLTokenBody]] = {
    val slTokenBody = for {
      xAuthToken <- headers.get("X-Auth-Token")
      bearerToken <- BearerTokenParser.parseToken(xAuthToken)
      slTokenBody <- ShortLivedTokenOps.getBody(bearerToken).toOption
      ret <- Json.parse(slTokenBody).validate[MachineData.SLTokenBody].asOpt
    } yield ret

    Future.successful(slTokenBody)
  }

  def getRequestVerdict(
      reqHeaders: Headers,
      hatName: String,
      namespace: String,
      usersService: UsersService,
      trustedApplicationProvider: TrustedApplicationProvider
    )(implicit hatServer: HatServer): Future[Either[DeviceVerificationFailure, DeviceRequestSuccess]] = {
    val eventuallyMaybeUserAndApp = for {
      sltoken <- Future.successful(reqHeaders.get("X-Auth-Token"))
      sltokenBody <- getTokenFromHeaders(reqHeaders)
      user <- usersService.getUser(hatName)
      app <- trustedApplicationProvider.application(sltokenBody.map(_.deviceId).getOrElse(""))
    } yield (sltoken, user, app)

    eventuallyMaybeUserAndApp.flatMap { maybeUserAndApp =>
      maybeUserAndApp match {
        case (Some(sltoken), Some(user), Some(app)) =>
          verifyApplicationAndNamespace(sltoken, app, namespace, hatName).flatMap { appAndNamespaceOk =>
            appAndNamespaceOk match {
              case Left(_)  => Future.successful(Left(DeviceRequestFailure("appAndNamespaceOk failed")))
              case Right(_) => Future.successful(Right(DeviceRequestSuccess(user)))
            }
          }
        case (_, _, _) =>
          Future.successful(Left(DeviceRequestFailure("Missing element of maybeUserAndApp")))
      }
    }
  }

  def verifyApplicationAndNamespace(
      sltoken: String,
      app: Application,
      namespace: String,
      hatName: String): Future[Either[DeviceVerificationFailure, DeviceRequestVerificationSuccess]] = {
    val maybeRefinedDevice = refineDeviceInfo(sltoken.split(" ")(1).trim, hatName, app.id)

    maybeRefinedDevice match {
      case Some(refinedDevice) =>
        verifyDevice(refinedDevice).flatMap { isVerified =>
          isVerified match {
            case Right(deviceIsVerified @ _) =>
              if (NamespaceUtils.verifyNamespaceWrite(app, namespace))
                Future.successful(
                  Right(DeviceRequestVerificationSuccess("Application ${app.id}-${namespace} verified."))
                )
              else
                Future.successful(Left(DeviceVerificationFailure.ApplicationAndNamespaceNotValid))
            case Left(verifyDeviceError) =>
              Future.successful(Left(verifyDeviceError))
          }
        }
      case None =>
        logger.info("Failed to refine: ${sltoken} - ${app} - ${namespace} - ${hatName}")
        Future.successful(Left(DeviceVerificationFailure.FailedDeviceRefinement))
    }
  }

  def refineDeviceInfo(
      token: String,
      hatName: String,
      deviceId: String): Option[DeviceDataInfoRefined] =
    for {
      tokenR <- refineV[NonEmpty](token).toOption
      hatNameR <- refineV[NonEmpty](hatName).toOption
      deviceIdR <- refineV[NonEmpty](deviceId).toOption
      deviceDataInfoRefined = DeviceDataInfoRefined(
                                ShortLivedToken(tokenR),
                                HatName(hatNameR),
                                DeviceId(deviceIdR)
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
    authServiceClient
      .getPublicKey(
        deviceRequestBodyRefined.hatName,
        deviceRequestBodyRefined.deviceId,
        keyId
      )
      .map { publicKeyResponse =>
        publicKeyResponse match {
          case Left(
                AuthServiceRequestTypes.PublicKeyRequestFailure.ServiceRespondedWithFailure(
                  failureDescription
                )
              ) =>
            Left(
              DeviceVerificationFailure.ServiceRespondedWithFailure(
                s"The Adjudicator Service responded with an error: ${failureDescription}"
              )
            )
          case Left(
                AuthServiceRequestTypes.PublicKeyRequestFailure.InvalidPublicKeyFailure(
                  failureDescription
                )
              ) =>
            Left(
              DeviceVerificationFailure.ServiceRespondedWithFailure(
                s"The Adjudicator Service responded with an error: ${failureDescription}"
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
      trustedApplicationProvider.application(deviceDataInfo.deviceId.value.toString())

    eventuallyMaybeDecision.flatMap { maybeDecision =>
      eventuallyMaybeApp.flatMap { maybeApp =>
        logger.info(s"AssessRequest: ${maybeDecision} - ${maybeApp} - ${namespace}")
        decide(maybeDecision, maybeApp, namespace) match {
          case Some(ns) =>
            logger.info(s"Found a namespace: ${ns}")
            Future.successful(
              Right(
                RequestVerified(s"Token: ${deviceDataInfo.deviceId}")
              )
            )
          case None =>
            logger.error(s"def assessRequest: decide returned None - ${deviceDataInfo} - ${namespace}")
            Future.successful(
              Left(
                RequestValidationFailure.InvalidShortLivedToken(
                  s"Token: ${deviceDataInfo.deviceId}"
                )
              )
            )
        }
      }
    } recover {
      case e =>
        logger.error(s"DeviceData.assessRequest:Failure ${e}")
        Left(
          RequestValidationFailure.InvalidShortLivedToken(
            s"Token: ${deviceDataInfo.deviceId}"
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
        if (NamespaceUtils.verifyNamespace(app, namespace))
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
