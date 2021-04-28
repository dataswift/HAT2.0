package org.hatdex.hat.clients

import dev.profunktor.auth.jwt.JwtSecretKey
import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
import io.dataswift.adjudicator.Types.{ DeviceId, HatName }
import io.dataswift.adjudicator.{ HatDeviceClaim, JwtClaimBuilder }
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim, JwtHeader }
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{ WSClient, WSRequest }
import play.api.{ Configuration, Logging }

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class AuthServiceWsClient @Inject() (
    configuration: Configuration,
    secret: JwtSecretKey,
    ws: WSClient
  )(implicit ec: ExecutionContext)
    extends AuthServiceClient
    with Logging {
  import AuthServiceRequestTypes._

  private val authServiceAddress =
    configuration.underlying.getString("authservice.address")
  private val authServiceScheme =
    configuration.underlying.getString("authservice.scheme")
  private val authServiceEndpoint =
    s"${authServiceScheme}${authServiceAddress}"
  private val authServiceSharedSecret =
    configuration.underlying.getString("authservice.sharedSecret")

  private val hatDeviceClaimBuilder: JwtClaimBuilder[HatDeviceClaim] = HatDeviceClaim.builder

  private def createHatClaimFromString(
      hatNameAsStr: String,
      deviceId: DeviceId): Option[HatDeviceClaim] =
    for {
      hatName <- refineV[NonEmpty](hatNameAsStr).toOption
      hatClaim = HatDeviceClaim(HatName(hatName), deviceId)
    } yield hatClaim

  private def createHatClaimFromHatName(
      maybeHatName: Option[HatName],
      deviceId: DeviceId): Option[HatDeviceClaim] =
    maybeHatName.map(HatDeviceClaim(_, deviceId))

  // Internal calls
  def getPublicKey(
      hatName: HatName,
      deviceId: DeviceId,
      keyId: String): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]] = {
    val claim = createHatClaimFromHatName(Some(hatName), deviceId)

    val url = s"${authServiceEndpoint}/v1/devices/hat/kid/${keyId}"

    makeRequest(url, claim, ws) match {
      case Some(req) =>
        runPublicKeyRequest(req)
      case None =>
        Future.successful(
          Left(
            PublicKeyRequestFailure.ServiceRespondedWithFailure(
              s"The AuthService Service responded with an error."
            )
          )
        )
    }
  }

  def joinDevice(
      hatName: String,
      deviceId: DeviceId): Future[Either[
    JoinDeviceRequestFailure.ServiceRespondedWithFailure,
    DeviceJoined
  ]] = {
    val claim = createHatClaimFromString(hatName, deviceId)

    val url = s"${authServiceEndpoint}/v1/devices/hat"

    makeRequest(url, claim, ws) match {
      case Some(req) =>
        runJoinDeviceRequest(req, deviceId)
      case None =>
        Future.successful(
          Left(
            JoinDeviceRequestFailure.ServiceRespondedWithFailure(
              s"The AuthService Service responded with an error."
            )
          )
        )
    }
  }

  def leaveDevice(
      hatName: String,
      deviceId: DeviceId): Future[Either[LeaveDeviceRequestFailure, DeviceLeft]] = {
    val claim = createHatClaimFromString(hatName, deviceId)

    val url = s"${authServiceEndpoint}/v1/devices/hat"

    makeRequest(url, claim, ws) match {
      case Some(req) =>
        runLeaveDeviceRequest(req, deviceId)
      case None =>
        Future.successful(
          Left(
            LeaveDeviceRequestFailure.ServiceRespondedWithFailure(
              s"The AuthService Service responded with an error."
            )
          )
        )
    }
  }

  // Specific Requests
  private def runJoinDeviceRequest(
      req: WSRequest,
      deviceId: DeviceId
    )(implicit ec: ExecutionContext): Future[Either[
    JoinDeviceRequestFailure.ServiceRespondedWithFailure,
    DeviceJoined
  ]] = {
    logger.info(s"runJoinDeviceRequest: ${deviceId}")
    req.put("").map { response =>
      response.status match {
        case OK =>
          logger.info("runJoinDeviceRequest - OK")
          Right(DeviceJoined(deviceId))
        case _ =>
          logger.error(s"runJoinDeviceRequest: KO response: ${response.statusText}")
          Left(
            JoinDeviceRequestFailure.ServiceRespondedWithFailure(
              s"The AuthService Service responded with an error: ${response.statusText}"
            )
          )
      }
    } recover {
      case e =>
        logger.error(s"runJoinDeviceRequest: exception: ${e.getMessage}")
        Left(
          JoinDeviceRequestFailure.ServiceRespondedWithFailure(
            s"The AuthService Service responded with an error: ${e.getMessage}"
          )
        )
    }
  }

  private def runLeaveDeviceRequest(
      req: WSRequest,
      deviceId: DeviceId
    )(implicit ec: ExecutionContext): Future[Either[LeaveDeviceRequestFailure, DeviceLeft]] = {
    logger.info(s"runLeaveDeviceRequest: ${deviceId}")
    req.delete().map { response =>
      response.status match {
        case OK =>
          logger.info("runLeaveDeviceRequest - OK")
          Right(DeviceLeft(deviceId))
        case _ =>
          logger.error(s"runLeaveDeviceRequest: KO response: ${response.statusText}")
          Left(
            LeaveDeviceRequestFailure.ServiceRespondedWithFailure(
              s"The AuthService Service responded with an error: ${response.statusText}"
            )
          )
      }
    } recover {
      case e =>
        logger.error(s"runLeaveDeviceRequest: KO response: ${e.getMessage()}")
        Left(
          LeaveDeviceRequestFailure.ServiceRespondedWithFailure(
            s"The AuthService Service responded with an error: ${e.getMessage}"
          )
        )
    }

  }

  private def runPublicKeyRequest(
      req: WSRequest
    )(implicit ec: ExecutionContext): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]] = {
    logger.info(s"runPublicKeyRequest")
    req.get().map { response =>
      response.status match {
        case OK =>
          val maybeArrayByte = Json.parse(response.body).asOpt[Array[Byte]]
          maybeArrayByte match {
            case None =>
              Left(
                PublicKeyRequestFailure.InvalidPublicKeyFailure(
                  "The response was not able to be decoded into an Array[Byte]."
                )
              )
            case Some(arrayByte) => Right(PublicKeyReceived(arrayByte))
          }
        case _ =>
          Left(
            PublicKeyRequestFailure.ServiceRespondedWithFailure(
              s"The AuthService Service responded with an error: ${response.statusText}"
            )
          )
      }
    } recover {
      case e =>
        Left(
          PublicKeyRequestFailure.ServiceRespondedWithFailure(
            s"The AuthService Service responded with an error: ${e.getMessage}"
          )
        )
    }
  }

  // Base Request
  private def makeRequest(
      url: String,
      maybeHatDeviceClaim: Option[HatDeviceClaim],
      ws: WSClient
    )(implicit ec: ExecutionContext): Option[WSRequest] = {
    logger.info(s"makeRequest: ${url}")
    for {
      hatDeviceClaim <- maybeHatDeviceClaim
      token: JwtClaim = hatDeviceClaimBuilder.build(hatDeviceClaim)
      encoded         = Jwt.encode(JwtHeader(JwtAlgorithm.HS256), token, secret.value)
      request         = ws.url(url).withHttpHeaders("Authorization" -> s"Bearer ${encoded}")
    } yield request
  }
}
