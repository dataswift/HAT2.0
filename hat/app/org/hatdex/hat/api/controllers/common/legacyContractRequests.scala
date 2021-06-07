package org.hatdex.hat.api.controllers.common

import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import io.dataswift.adjudicator.Types.{ ContractId, HatName, ShortLivedToken }
import io.dataswift.models.hat.json.HatJsonFormats
import io.dataswift.models.hat.json.RichDataJsonFormats._
import io.dataswift.models.hat.{ ApiHatFile, EndpointData }
import org.hatdex.hat.authentication.models.HatUser
import pdi.jwt.JwtClaim
import play.api.libs.json.{ JsValue, Json, Reads }

import java.util.UUID
import scala.util.Try

case class ContractDataInfoRefined(
    token: ShortLivedToken,
    hatName: HatName,
    contractId: ContractId)

trait MaybeWithContractInfo {
  def extractContractInfo: Option[ContractDataInfoRefined]
}

// Remove this later
object MachineData {
  case class SLTokenBody(
      iss: String,
      exp: Long,
      deviceId: String)
  implicit val sltokenBodyReads: Reads[SLTokenBody] = Json.reads[SLTokenBody]
}

case class DeviceDataInfoRefined(
    token: ShortLivedToken,
    hatName: HatName,
    contractId: ContractId)

sealed trait RequiredNamespacePermissions
case object Read extends RequiredNamespacePermissions
case object Write extends RequiredNamespacePermissions
case object ReadWrite extends RequiredNamespacePermissions

// -- Errors
sealed abstract class DeviceVerificationFailure
object DeviceVerificationFailure {
  final case class ServiceRespondedWithFailure(failureDescription: String) extends DeviceVerificationFailure
  final case class InvalidTokenFailure(failureDescription: String) extends DeviceVerificationFailure
  final case object ApplicationAndNamespaceNotValid extends DeviceVerificationFailure
  final case class InvalidDeviceDataRequestFailure(
      failureDescription: String)
      extends DeviceVerificationFailure
  final case object FailedDeviceRefinement extends DeviceVerificationFailure
  final case class DeviceRequestFailure(message: String) extends DeviceVerificationFailure
}

sealed abstract class DeviceVerificationSuccess
object DeviceVerificationSuccess {
  final case class JwtClaimVerified(jwtClaim: JwtClaim) extends DeviceVerificationSuccess
  final case class DeviceRequestSuccess(hatUser: HatUser) extends DeviceVerificationSuccess
  final case class DeviceRequestVerificationSuccess(message: String) extends DeviceVerificationSuccess
}

case class ContractDataReadRequest(
    token: String,
    hatName: String,
    contractId: String)
    extends MaybeWithContractInfo {
  def extractContractInfo: Option[ContractDataInfoRefined] =
    RequestValidator.validateContractInfo(token, hatName, contractId)
}

object ContractDataReadRequest {
  implicit val contractDataReadRequestReads: Reads[ContractDataReadRequest] =
    Json.reads[ContractDataReadRequest]
}

case class ContractDataCreateRequest(
    token: String,
    hatName: String,
    contractId: String,
    body: Option[JsValue])
    extends MaybeWithContractInfo {
  def extractContractInfo: Option[ContractDataInfoRefined] =
    RequestValidator.validateContractInfo(token, hatName, contractId)
}

object ContractDataCreateRequest {
  implicit val contractDataCreateRequestReads: Reads[ContractDataCreateRequest] =
    Json.reads[ContractDataCreateRequest]
}

case class ContractDataUpdateRequest(
    token: String,
    hatName: String,
    contractId: String,
    body: Seq[EndpointData])
    extends MaybeWithContractInfo {
  def extractContractInfo: Option[ContractDataInfoRefined] =
    RequestValidator.validateContractInfo(token, hatName, contractId)
}

object ContractDataUpdateRequest {
  implicit val contractDataUpdateRequestReads: Reads[ContractDataUpdateRequest] =
    Json.reads[ContractDataUpdateRequest]
}

case class ContractFile(
    token: String,
    hatName: String,
    contractId: String,
    file: ApiHatFile)
    extends MaybeWithContractInfo {
  def extractContractInfo: Option[ContractDataInfoRefined] =
    RequestValidator.validateContractInfo(token, hatName, contractId)
}

object ContractFile {
  import HatJsonFormats.apiHatFileFormat
  implicit val contractFileReads: Reads[ContractFile] = Json.reads[ContractFile]
}

private object RequestValidator {
  def validateContractInfo(
      token: String,
      hatName: String,
      contractId: String): Option[ContractDataInfoRefined] =
    for {
      tokenR <- refineV[NonEmpty](token).toOption
      hatNameR <- refineV[NonEmpty](hatName).toOption
      contractIdR <- refineV[NonEmpty](contractId).toOption
      uuid <- Try(UUID.fromString(contractIdR.value)).toOption
    } yield ContractDataInfoRefined(ShortLivedToken(tokenR), HatName(hatNameR), ContractId(uuid))
}

sealed trait RequestValidationFailure
case class HatNotFound(hatName: String) extends RequestValidationFailure
case class MissingHatName(hatName: String) extends RequestValidationFailure
case class InaccessibleNamespace(namespace: String) extends RequestValidationFailure
case class InvalidShortLivedToken(contractId: String) extends RequestValidationFailure
case object GeneralError extends RequestValidationFailure
case object XAuthTokenMissing extends RequestValidationFailure
case object ApplicationNotFound extends RequestValidationFailure

case class RequestVerified(namespace: String) extends AnyVal

sealed trait ContractVerificationFailure
case class ServiceRespondedWithFailure(failureDescription: String) extends ContractVerificationFailure
case class InvalidTokenFailure(failureDescription: String) extends ContractVerificationFailure
case class InvalidContractDataRequestFailure(failureDescription: String) extends ContractVerificationFailure

sealed trait ContractVerificationSuccess
case class JwtClaimVerified(jwtClaim: JwtClaim) extends ContractVerificationSuccess
