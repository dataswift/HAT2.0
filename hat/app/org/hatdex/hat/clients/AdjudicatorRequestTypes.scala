package org.hatdex.hat.clients

import io.dataswift.adjudicator.Types.ContractId

object AdjudicatorRequestTypes {
  // Public Key Request
  sealed trait PublicKeyRequestFailure
  object PublicKeyRequestFailure {
    final case class ServiceRespondedWithFailure(failureDescription: String) extends PublicKeyRequestFailure
    final case class InvalidPublicKeyFailure(failureDescription: String) extends PublicKeyRequestFailure
  }
  final case class PublicKeyReceived(publicKeyAsByteArray: Array[Byte]) extends AnyVal

  // Join Contract Request
  sealed trait JoinContractRequestFailure
  object JoinContractRequestFailure {
    final case class ServiceRespondedWithFailure(failureDescription: String) extends JoinContractRequestFailure
    final case class JoinContractFailure(failureDescription: String) extends JoinContractRequestFailure
  }
  // TODO: if we attempt to extend AnyVal here, the compiler complains
  final case class ContractJoined(contractId: ContractId)

  // Leave Contract Request
  sealed trait LeaveContractRequestFailure
  object LeaveContractRequestFailure {
    final case class ServiceRespondedWithFailure(failureDescription: String) extends LeaveContractRequestFailure
    final case class LeaveContractFailure(failureDescription: String) extends LeaveContractRequestFailure
  }
  final case class ContractLeft(contractId: ContractId)
}
