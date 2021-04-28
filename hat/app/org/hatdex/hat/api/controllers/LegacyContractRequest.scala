package org.hatdex.hat.api.controllers

import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import io.dataswift.adjudicator.Types.{ ContractId, HatName, ShortLivedToken }
import io.dataswift.models.hat.EndpointData
import play.api.libs.json.JsValue

import java.util.UUID
import scala.util.Try

case class ContractDataInfoRefined(
    token: ShortLivedToken,
    hatName: HatName,
    contractId: ContractId)

trait MaybeWithContractInfo {
  def extractContractInfo: Option[ContractDataInfoRefined]
}

case class ContractDataReadRequest(
    token: String,
    hatName: String,
    contractId: String)
    extends MaybeWithContractInfo {
  def extractContractInfo: Option[ContractDataInfoRefined] =
    RequestValidator.validateContractInfo(token, hatName, contractId)
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

case class ContractDataUpdateRequest(
    token: String,
    hatName: String,
    contractId: String,
    body: Seq[EndpointData])
    extends MaybeWithContractInfo {
  def extractContractInfo: Option[ContractDataInfoRefined] =
    RequestValidator.validateContractInfo(token, hatName, contractId)
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
