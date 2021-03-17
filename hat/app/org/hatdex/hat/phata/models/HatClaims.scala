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

import play.api.libs.json.{ Json, OFormat, Reads, Writes }

case class ApiVerificationRequest(
    applicationId: String,
    email: String,
    redirectUri: String)

object ApiVerificationRequest {
  implicit val claimHatRequestApiFormat: OFormat[ApiVerificationRequest] =
    Json.format[ApiVerificationRequest]
}

case class ApiVerificationCompletionRequest(
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

object ApiVerificationCompletionRequest {
  implicit val hatClaimRequestReads: Reads[ApiVerificationCompletionRequest] =
    Json.reads[ApiVerificationCompletionRequest]
}

object HattersClaimPayload {
  def apply(
      claim: ApiVerificationCompletionRequest,
      sandbox: Boolean): HattersClaimPayload =
    new HattersClaimPayload(
      claim.email,
      claim.termsAgreed,
      sandbox,
      "web",
      Some(claim.optins.nonEmpty),
      claim.hatName,
      claim.hatCluster
    )
  implicit val HatClaimRequestWrites: Writes[HattersClaimPayload] =
    Json.format[HattersClaimPayload]
}
