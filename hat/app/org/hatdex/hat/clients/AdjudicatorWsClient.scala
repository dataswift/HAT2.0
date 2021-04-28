package org.hatdex.hat.clients

import dev.profunktor.auth.jwt.JwtSecretKey
import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
import io.dataswift.adjudicator.Types.{ ContractId, HatName }
import io.dataswift.adjudicator.{ HatClaim, JwtClaimBuilder }
import org.hatdex.hat.clients._
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim, JwtHeader }
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{ WSClient, WSRequest }
import play.api.{ Configuration, Logging }

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class AdjudicatorWsClient @Inject() (
    configuration: Configuration,
    secret: JwtSecretKey,
    ws: WSClient
  )(implicit ec: ExecutionContext)
    extends AdjudicatorClient
    with Logging {
  import AdjudicatorRequestTypes._

  private val adjudicatorAddress =
    configuration.underlying.getString("adjudicator.address")
  private val adjudicatorScheme =
    configuration.underlying.getString("adjudicator.scheme")
  private val adjudicatorEndpoint =
    s"${adjudicatorScheme}${adjudicatorAddress}"
  private val adjudicatorSharedSecret =
    configuration.underlying.getString("adjudicator.sharedSecret")

  //val logger: Logger = Logger(this.getClass)

  private val hatClaimBuiler: JwtClaimBuilder[HatClaim] = HatClaim.builder

  private def createHatClaimFromString(
      hatNameAsStr: String,
      contractId: ContractId): Option[HatClaim] =
    for {
      hatName <- refineV[NonEmpty](hatNameAsStr).toOption
      hatClaim = HatClaim(HatName(hatName), contractId)
    } yield hatClaim

  private def createHatClaimFromHatName(
      maybeHatName: Option[HatName],
      contractId: ContractId): Option[HatClaim] =
    maybeHatName.map(HatClaim(_, contractId))

  // Internal calls
  override def getPublicKey(
      hatName: HatName,
      contractId: ContractId,
      keyId: String): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]] = {
    val claim = createHatClaimFromHatName(Some(hatName), contractId)

    val url = s"${adjudicatorEndpoint}/v1/contracts/hat/kid/${keyId}"

    makeRequest(url, claim, ws) match {
      case Some(req) =>
        runPublicKeyRequest(req)
      case None =>
        Future.successful(
          Left(
            PublicKeyRequestFailure.ServiceRespondedWithFailure(
              s"The Adjudicator Service responded with an error."
            )
          )
        )
    }
  }

  override def joinContract(
      hatName: String,
      contractId: ContractId): Future[Either[
    JoinContractRequestFailure.ServiceRespondedWithFailure,
    ContractJoined
  ]] = {
    val claim = createHatClaimFromString(hatName, contractId)

    val url = s"${adjudicatorEndpoint}/v1/contracts/hat"

    makeRequest(url, claim, ws) match {
      case Some(req) =>
        runJoinContractRequest(req, contractId)
      case None =>
        Future.successful(
          Left(
            JoinContractRequestFailure.ServiceRespondedWithFailure(
              s"The Adjudicator Service responded with an error."
            )
          )
        )
    }
  }

  override def leaveContract(
      hatName: String,
      contractId: ContractId): Future[Either[LeaveContractRequestFailure, ContractLeft]] = {
    val claim = createHatClaimFromString(hatName, contractId)

    val url = s"${adjudicatorEndpoint}/v1/contracts/hat"

    makeRequest(url, claim, ws) match {
      case Some(req) =>
        runLeaveContractRequest(req, contractId)
      case None =>
        Future.successful(
          Left(
            LeaveContractRequestFailure.ServiceRespondedWithFailure(
              s"The Adjudicator Service responded with an error."
            )
          )
        )
    }
  }

  // Specific Requests
  private def runJoinContractRequest(
      req: WSRequest,
      contractId: ContractId
    )(implicit ec: ExecutionContext): Future[Either[
    JoinContractRequestFailure.ServiceRespondedWithFailure,
    ContractJoined
  ]] = {
    logger.info(s"runJoinContractRequest: ${contractId}")
    req.put("").map { response =>
      response.status match {
        case OK =>
          logger.info("runJoinContractRequest - OK")
          Right(ContractJoined(contractId))
        case _ =>
          logger.error(s"runJoinContractRequest: KO response: ${response.statusText}")
          Left(
            JoinContractRequestFailure.ServiceRespondedWithFailure(
              s"The Adjudicator Service responded with an error: ${response.statusText}"
            )
          )
      }
    } recover {
      case e =>
        logger.error(s"runJoinContractRequest: exception: ${e.getMessage}")
        Left(
          JoinContractRequestFailure.ServiceRespondedWithFailure(
            s"The Adjudicator Service responded with an error: ${e.getMessage}"
          )
        )
    }
  }

  private def runLeaveContractRequest(
      req: WSRequest,
      contractId: ContractId
    )(implicit ec: ExecutionContext): Future[Either[LeaveContractRequestFailure, ContractLeft]] = {
    logger.info(s"runLeaveContractRequest: ${contractId}")
    req.delete().map { response =>
      response.status match {
        case OK =>
          logger.info("runLeaveContractRequest - OK")
          Right(ContractLeft(contractId))
        case _ =>
          logger.error(s"runLeaveContractRequest: KO response: ${response.statusText}")
          Left(
            LeaveContractRequestFailure.ServiceRespondedWithFailure(
              s"The Adjudicator Service responded with an error: ${response.statusText}"
            )
          )
      }
    } recover {
      case e =>
        logger.error(s"runLeaveContractRequest: KO response: ${e.getMessage()}")
        Left(
          LeaveContractRequestFailure.ServiceRespondedWithFailure(
            s"The Adjudicator Service responded with an error: ${e.getMessage}"
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
              s"The Adjudicator Service responded with an error: ${response.statusText}"
            )
          )
      }
    } recover {
      case e =>
        Left(
          PublicKeyRequestFailure.ServiceRespondedWithFailure(
            s"The Adjudicator Service responded with an error: ${e.getMessage}"
          )
        )
    }
  }

  // Base Request
  private def makeRequest(
      url: String,
      maybeHatClaim: Option[HatClaim],
      ws: WSClient
    )(implicit ec: ExecutionContext): Option[WSRequest] = {
    logger.info(s"makeRequest: ${url}")
    for {
      hatClaim <- maybeHatClaim
      token: JwtClaim = hatClaimBuiler.build(hatClaim)
      encoded         = Jwt.encode(JwtHeader(JwtAlgorithm.HS256), token, secret.value)
      request         = ws.url(url).withHttpHeaders("Authorization" -> s"Bearer ${encoded}")
    } yield request
  }
}
