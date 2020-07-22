package org.hatdex.hat.contract.models

import pdi.jwt.JwtClaim

sealed abstract class ContractVerificationFailure
object ContractVerificationFailure {
  final case class ServiceRespondedWithFailure(failureDescription: String) extends ContractVerificationFailure
  final case class InvalidTokenFailure(failureDescription: String) extends ContractVerificationFailure
  final case class InvalidContractDataRequestFailure(failureDescription: String) extends ContractVerificationFailure
}
sealed abstract class ContractVerificationSuccess
object ContractVerificationSuccess {
  final case class JwtClaimVerified(jwtClaim: JwtClaim) extends ContractVerificationSuccess
}
