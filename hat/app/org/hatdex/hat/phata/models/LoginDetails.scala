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

case class LoginDetails(
  username: String,
  password: String,
  remember: Option[Boolean],
  name: Option[String],
  redirect: Option[String]
)

object LoginDetails {
  private val loginDetailsMapping: Mapping[LoginDetails] = Forms.mapping(
    "username" -> Forms.nonEmptyText,
    "password" -> Forms.nonEmptyText,
    "remember" -> Forms.optional(Forms.boolean),
    "name" -> Forms.optional(Forms.text),
    "redirect" -> Forms.optional(Forms.text)
  )(LoginDetails.apply)(LoginDetails.unapply)

  val loginForm: Form[LoginDetails] = Form(loginDetailsMapping)
}

case class PasswordChange(
  newPassword: String,
  confirmPassword: String
)

object PasswordChange {
  private val passwordChangeMapping: Mapping[PasswordChange] = Forms.mapping(
    "newPassword" -> Forms.tuple(
      "password" -> Forms.nonEmptyText(minLength = 8),
      "confirm" -> Forms.nonEmptyText(minLength = 8)
    ).verifying(
        "constraints.passwords.match",
        passConfirm => passConfirm._1 == passConfirm._2
      )
  )({
      case ((password, confirm)) =>
        PasswordChange(password, confirm)
    })({
      case passwordChange: PasswordChange =>
        Some(((passwordChange.newPassword, passwordChange.confirmPassword)))
    })

  val passwordChangeForm = Form(passwordChangeMapping)
}

