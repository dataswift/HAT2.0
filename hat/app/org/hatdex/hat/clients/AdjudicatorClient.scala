package org.hatdex.hat.clients

import io.dataswift.adjudicator.Types.{ ContractId, HatName }
import org.hatdex.hat.clients.AdjudicatorRequestTypes._

import scala.concurrent.Future

trait AdjudicatorClient {
  def getPublicKey(
      hatName: HatName,
      contractId: ContractId,
      keyId: String): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]]

  def joinContract(
      hatName: String,
      contractId: ContractId): Future[Either[
    JoinContractRequestFailure.ServiceRespondedWithFailure,
    ContractJoined
  ]]

  def leaveContract(
      hatName: String,
      contractId: ContractId): Future[Either[LeaveContractRequestFailure, ContractLeft]]
}
