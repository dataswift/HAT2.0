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

import java.util.UUID

import org.joda.time.DateTime

trait MailToken {
  def id: String
  def email: String
  def expirationTime: DateTime
  def isExpired: Boolean = expirationTime.isBeforeNow
}

case class MailTokenUser(
    id: String,
    email: String,
    expirationTime: DateTime,
    isSignUp: Boolean)
    extends MailToken

object MailTokenUser {
  private val mailTokenValidityHours = 24

  def apply(
      email: String,
      isSignUp: Boolean
    ): MailTokenUser =
    MailTokenUser(
      UUID.randomUUID().toString,
      email,
      new DateTime().plusHours(mailTokenValidityHours),
      isSignUp
    )
}

// isSignUp is true here since the user has not claimed their HAT.
object MailClaimTokenUser {
  private val mailClaimTokenValidityDays = 7

  def apply(email: String): MailTokenUser =
    MailTokenUser(
      UUID.randomUUID().toString,
      email,
      new DateTime().plusDays(mailClaimTokenValidityDays),
      isSignUp = true
    )
}
