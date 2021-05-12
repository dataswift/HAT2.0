package org.hatdex.hat.api.controllers

import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import io.dataswift.adjudicator.Types.{ ContractId, HatName, ShortLivedToken }
import io.dataswift.models.hat.json.HatJsonFormats
import io.dataswift.models.hat.{ ApiHatFile, EndpointData }
import play.api.libs.json.{ JsValue, Json, Reads }
import io.dataswift.models.hat.json.RichDataJsonFormats._

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
