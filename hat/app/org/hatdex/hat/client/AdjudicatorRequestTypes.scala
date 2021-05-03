// package org.hatdex.hat.client

// import io.dataswift.adjudicator.Types.ContractId

// object AdjudicatorRequestTypes {

//   // Public Key Request
//   sealed trait PublicKeyRequestFailure
//   case class PublicKeyServiceFailure(failureDescription: String) extends PublicKeyRequestFailure
//   case class InvalidPublicKeyFailure(failureDescription: String) extends PublicKeyRequestFailure
//   case class PublicKeyReceived(publicKeyAsByteArray: Array[Byte]) extends AnyVal

//   // Join Contract Request
//   case class JoinContractServiceFailure(failureDescription: String)
//   case class ContractJoined(contractId: ContractId)

//   // Leave Contract Request
//   sealed trait LeaveContractRequestFailure
//   case class LeaveContractServiceFailure(failureDescription: String) extends LeaveContractRequestFailure
//   case class LeaveContractFailure(failureDescription: String) extends LeaveContractRequestFailure
//   case class ContractLeft(contractId: ContractId)

// }
