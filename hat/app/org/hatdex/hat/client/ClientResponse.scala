package org.hatdex.hat.client

import io.dataswift.adjudicator.Types.{ ContractId, DeviceId }
import pdi.jwt.JwtClaim

object ClientResponse {
  sealed trait RequestValidationFailure
  case class HatNotFound(hatName: String) extends RequestValidationFailure
  case class MissingHatName(hatName: String) extends RequestValidationFailure
  case class InaccessibleNamespace(namespace: String) extends RequestValidationFailure
  case class InvalidShortLivedToken(contractId: String) extends RequestValidationFailure
  case object GeneralError extends RequestValidationFailure

  case class RequestVerified(namespace: String) extends AnyVal

  sealed trait ContractVerificationFailure
  case class ServiceRespondedWithFailure(failureDescription: String) extends ContractVerificationFailure
  case class InvalidTokenFailure(failureDescription: String) extends ContractVerificationFailure
  case class InvalidContractDataRequestFailure(failureDescription: String) extends ContractVerificationFailure

  sealed trait ContractVerificationSuccess
  case class JwtClaimVerified(jwtClaim: JwtClaim) extends ContractVerificationSuccess
}

object AdjudicatorRequestTypes {

  // Public Key Request
  sealed trait PublicKeyRequestFailure
  case class PublicKeyServiceFailure(failureDescription: String) extends PublicKeyRequestFailure
  case class InvalidPublicKeyFailure(failureDescription: String) extends PublicKeyRequestFailure
  case class PublicKeyReceived(publicKeyAsByteArray: Array[Byte]) extends AnyVal

  // Join Contract Request
  case class JoinContractServiceFailure(failureDescription: String)
  case class ContractJoined(contractId: ContractId)

  // Leave Contract Request
  sealed trait LeaveContractRequestFailure
  case class LeaveContractServiceFailure(failureDescription: String) extends LeaveContractRequestFailure
  case class LeaveContractFailure(failureDescription: String) extends LeaveContractRequestFailure
  case class ContractLeft(contractId: ContractId)

}

object AuthServiceRequestTypes {

  // -- Public Key Request

  // Failure
  sealed trait PublicKeyRequestFailure
  case class PublicKeyServiceFailure(failureDescription: String) extends PublicKeyRequestFailure
  case class InvalidPublicKeyFailure(failureDescription: String) extends PublicKeyRequestFailure

  // Success
  case class PublicKeyReceived(publicKeyAsByteArray: Array[Byte]) extends AnyVal

  // -- Join Device Request

  // Failure
  sealed trait JoinDeviceRequestFailure
  case class JoinDeviceServiceFailure(failureDescription: String) extends JoinDeviceRequestFailure
  case class JoinDeviceFailure(failureDescription: String) extends JoinDeviceRequestFailure

  // TODO: if we attempt to extend AnyVal here, the compiler complains
  // Success
  case class DeviceJoined(deviceId: DeviceId)

  // Leave Device Request

  // Failure
  sealed trait LeaveDeviceRequestFailure
  case class LeaveDeviceServiceFailure(failureDescription: String) extends LeaveDeviceRequestFailure
  case class LeaveDeviceFailure(failureDescription: String) extends LeaveDeviceRequestFailure
  // Success
  case class DeviceLeft(deviceId: DeviceId)
}
