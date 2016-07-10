/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
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
 */
package hatdex.hat.authentication.authorization

import hatdex.hat.authentication.models.User
import hatdex.hat.dal.Tables.DataDebitRow

object UserAuthorization {
  /**
    * Only allows those users that have at least a service of the selected.
    * Master service is always allowed.
    * Ex: WithService("serviceA", "serviceB") => only users with services "serviceA" OR "serviceB" (or "master") are allowed.
    */
  def withRole(anyOf: String*)(implicit user: User): Boolean = {
    anyOf.intersect(Seq(user.role)).nonEmpty
  }

  def withRoles(allOf: String*)(implicit user: User): Boolean = {
    allOf.intersect(Seq(user.role)).size == allOf.size
  }

  def hasPermissionCreateUser(implicit user: User): Boolean = {
    user.role match {
      case "owner" =>
        true
      case "platform" =>
        true
      case _ =>
        false
    }
  }

  // FIXME: Need more sophisticated checking to disallow disabling platform and owner accounts
  def hasPermissionDisableUser(implicit user: User): Boolean = {
    user.role match {
      case "owner" =>
        true
      case "platform" =>
        true
      case _ =>
        false
    }
  }

  // Need more sophisitcated checking
  def hasPermissionEnableUser(implicit user: User): Boolean = {
    user.role match {
      case "owner" =>
        true
      case "platform" =>
        true
      case _ =>
        false
    }
  }
}
