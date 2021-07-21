package org.hatdex.hat.client

import io.dataswift.adjudicator.Types.{ ContractId, HatName }
import org.hatdex.hat.client.AdjudicatorRequestTypes._

import scala.concurrent.{ ExecutionContext, Future }

trait AdjudicatorClient {

  def getPublicKey(
      hatName: HatName,
      contractId: ContractId,
      keyId: String
    )(implicit ec: ExecutionContext): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]]

  def joinContract(
      hatName: String,
      contractId: ContractId
    )(implicit ec: ExecutionContext): Future[Either[JoinContractServiceFailure, ContractJoined]]

  def leaveContract(
      hatName: String,
      contractId: ContractId
    )(implicit ec: ExecutionContext): Future[Either[LeaveContractRequestFailure, ContractLeft]]
}
