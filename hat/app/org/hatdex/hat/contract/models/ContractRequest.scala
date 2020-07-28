package org.hatdex.hat.contract.models

import io.dataswift.adjudicator.Types.{ ContractId, HatName, ShortLivedToken }
import play.api.libs.json.JsValue

// final case class ContractDataRequest(token: ShortLivedToken, hatName: HatName, contractId: ContractId)
case class ContractRequestBodyRefined(token: ShortLivedToken, hatName: HatName, contractId: ContractId, body: Option[JsValue])
//  implicit val contractRequestBodyRefinedReads = Json.reads[ContractRequestBodyRefined]
case class ContractRequestBody(token: String, hatName: String, contractId: String, body: Option[JsValue])
