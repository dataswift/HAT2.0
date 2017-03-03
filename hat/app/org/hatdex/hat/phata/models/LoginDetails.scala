/*
 * Copyright (C) 2017 HAT Data Exchange Ltd
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
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>
 * 2 / 2017
 */

package org.hatdex.hat.phata.models

import play.api.data.{ Form, Forms, Mapping }
import me.gosimple.nbvcxz._
import play.api.Logger
import play.api.data.validation.{ Constraint, Invalid, Valid, ValidationError }
import play.api.libs.json.JsError

import collection.JavaConverters._

case class LoginDetails(
  username: String,
  password: String,
  remember: Option[Boolean],
  name: Option[String],
  redirect: Option[String])

object LoginDetails {
  private val loginDetailsMapping: Mapping[LoginDetails] = Forms.mapping(
    "username" -> Forms.nonEmptyText,
    "password" -> Forms.nonEmptyText,
    "remember" -> Forms.optional(Forms.boolean),
    "name" -> Forms.optional(Forms.text),
    "redirect" -> Forms.optional(Forms.text))(LoginDetails.apply)(LoginDetails.unapply)

  val loginForm: Form[LoginDetails] = Form(loginDetailsMapping)
}

case class PasswordChange(
  newPassword: String,
  confirmPassword: String)

object PasswordChange {
  val logger = Logger(this.getClass)
  private val nbvcxzDictionaryList = resources.ConfigurationBuilder.getDefaultDictionaries

  private val nbvcxzConfiguration = new resources.ConfigurationBuilder()
    .setMinimumEntropy(40d)
    .setDictionaries(nbvcxzDictionaryList)
    .createConfiguration()

  val nbvcxz = new Nbvcxz(nbvcxzConfiguration)

  val passwordCheckConstraint: Constraint[String] = Constraint("constraints.passwordcheck")({
    plainText =>
      val strengthEstimate = nbvcxz.estimate(plainText)
      val errors = if (!strengthEstimate.isMinimumEntropyMet) {
        val timeToCrackOff = scoring.TimeEstimate.getTimeToCrackFormatted(strengthEstimate, "OFFLINE_BCRYPT_14")
        val feedback = strengthEstimate.getFeedback.getSuggestion.asScala.toList.map { suggestion =>
          ValidationError(suggestion)
        }

        Seq(
          ValidationError("Password is too weak"),
          ValidationError(s"Estimated time to crack this password - $timeToCrackOff")) ++ feedback
      }
      else {
        Nil
      }

      if (errors.isEmpty) {
        Valid
      }
      else {
        Invalid(errors)
      }
  })

  private val passwordChangeMapping: Mapping[PasswordChange] = Forms.mapping(
    "newPassword" -> Forms.tuple(
      "password" -> Forms.nonEmptyText().verifying(passwordCheckConstraint),
      "confirm" -> Forms.nonEmptyText())
      .verifying(
        "constraints.passwords.match",
        passConfirm => passConfirm._1 == passConfirm._2))({
      case ((password, confirm)) => PasswordChange(password, confirm)
    })({
      passwordChange: PasswordChange => Some((passwordChange.newPassword, passwordChange.confirmPassword))
    })

  val passwordChangeForm = Form(passwordChangeMapping)
}

case class ApiPasswordChange(
  newPassword: String,
  password: Option[String])

import play.api.libs.json._
import play.api.libs.functional.syntax._

object ApiPasswordChange {
  def passwordStrength(implicit reads: Reads[String], p: String => scala.collection.TraversableLike[_, String]) =
    Reads[String] { js =>
      reads.reads(js)
        .flatMap { a =>
          val estimate = PasswordChange.nbvcxz.estimate(a)
          if (estimate.isMinimumEntropyMet) {
            JsSuccess(a)
          }
          else {
            JsError(ValidationError(
              "Minimum password requirement strength not met",
              estimate.getFeedback.getSuggestion.asScala.toList: _*))
          }
        }
    }

  Reads.filter[String](ValidationError("Minimum password requirement strength not met")) { p =>
    PasswordChange.nbvcxz.estimate(p).isMinimumEntropyMet
  }

  implicit val passwordChangeApiReads: Reads[ApiPasswordChange] = (
    (JsPath \ "newPassword").read[String](passwordStrength) and
    (JsPath \ "password").readNullable[String])(ApiPasswordChange.apply _)
  implicit val passwordChangeApiWrites: Writes[ApiPasswordChange] = Json.format[ApiPasswordChange]
}

case class ApiPasswordResetRequest(
  email: String)

object ApiPasswordResetRequest {
  implicit val passwordResetApiReads: Reads[ApiPasswordResetRequest] =
    (__ \ 'email).read[String](Reads.email).map { email => ApiPasswordResetRequest(email) }
  implicit val passwordResetApiWrites: Writes[ApiPasswordResetRequest] = Json.format[ApiPasswordResetRequest]
}
