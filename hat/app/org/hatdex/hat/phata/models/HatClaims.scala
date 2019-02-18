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

case class ApiClaimHatRequest(applicationId: String, email: String)

object ApiClaimHatRequest {
  implicit val claimHatRequestApiReads: Reads[ApiClaimHatRequest] = (
    (JsPath \ "applicationId").read[String] and (JsPath \ "email").read[String](Reads.email))(ApiClaimHatRequest.apply _)

  implicit val claimHatRequestApiWrites: Writes[ApiClaimHatRequest] = Json.format[ApiClaimHatRequest]

}

/**
 * hatters data object
 */
case class HatClaimMembership(plan: String, membershipType: String)
object HatClaimMembership {
  implicit val hatClaimMembershipReads: Reads[HatClaimMembership] = (
    (JsPath \ "plan").read[String] and
    (JsPath \ "membershipType").read[String])(HatClaimMembership.apply _)

  implicit val hatClaimMembershipWrites: Writes[HatClaimMembership] = Json.format[HatClaimMembership]
}

case class HatClaimRequest(
    firstName: String,
    lastName: String,
    email: String,
    termsAgreed: Boolean,
    optins: Array[String],
    hatName: String,
    hatCluster: String,
    hatCountry: String,
    password: String,
    membership: HatClaimMembership,
    applicationId: String)

object HatClaimRequest {
  implicit val hatClaimRequestReads: Reads[HatClaimRequest] = (
    (JsPath \ "firstName").read[String] and
    (JsPath \ "lastName").read[String] and
    (JsPath \ "email").read[String](Reads.email) and
    (JsPath \ "termsAgreed").read[Boolean] and
    (JsPath \ "optins").read[Array[String]] and
    (JsPath \ "hatName").read[String] and
    (JsPath \ "hatCluster").read[String] and
    (JsPath \ "hatCountry").read[String] and
    (JsPath \ "password").read[String] and
    (JsPath \ "membership").read[HatClaimMembership] and
    (JsPath \ "applicationId").read[String])(HatClaimRequest.apply _)

  implicit val HatClaimRequestWrites: Writes[HatClaimRequest] = Json.format[HatClaimRequest]
}