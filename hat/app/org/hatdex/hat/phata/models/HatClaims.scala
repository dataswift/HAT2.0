/*
 * Copyright (C) 2019 HAT Data Exchange Ltd
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Written by Terry Lee <terry.lee@hatdex.org>
 * 2 / 2019
 */

package org.hatdex.hat.phata.models

import play.api.libs.json.{ JsPath, Json, Reads, Writes }
import play.api.libs.functional.syntax._

case class ApiClaimHatRequest(
    applicationId: String,
    email: String)

object ApiClaimHatRequest {
  implicit val claimHatRequestApiReads: Reads[ApiClaimHatRequest] =
    ((JsPath \ "applicationId").read[String] and (JsPath \ "email")
          .read[String](Reads.email))(ApiClaimHatRequest.apply _)

  implicit val claimHatRequestApiWrites: Writes[ApiClaimHatRequest] =
    Json.format[ApiClaimHatRequest]

}

case class HatClaimCompleteRequest(
    email: String,
    termsAgreed: Boolean,
    optins: Array[String],
    hatName: String,
    hatCluster: String,
    password: String)

case class HattersClaimPayload(
    email: String,
    termsAgreed: Boolean,
    sandbox: Boolean,
    platform: String,
    newsletterOptin: Option[Boolean],
    hatName: String,
    hatCluster: String)

object HatClaimCompleteRequest {
  implicit val hatClaimRequestReads: Reads[HatClaimCompleteRequest] =
    Json.reads[HatClaimCompleteRequest]
}

object HattersClaimPayload {
  def apply(claim: HatClaimCompleteRequest): HattersClaimPayload =
    new HattersClaimPayload(
      claim.email,
      claim.termsAgreed,
      claim.hatCluster == "hubat.net",
      "web",
      Some(claim.optins.nonEmpty),
      claim.hatName,
      claim.hatCluster
    )
  implicit val HatClaimRequestWrites: Writes[HattersClaimPayload] =
    Json.format[HattersClaimPayload]
}
