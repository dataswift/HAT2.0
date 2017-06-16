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
 * 5 / 2017
 */

package org.hatdex.hat.authentication.models

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import org.hatdex.hat.resourceManagement.HatServer

case class HatUser(userId: UUID, email: String, pass: Option[String], name: String, roles: Seq[UserRole], enabled: Boolean) extends Identity {
  def loginInfo(implicit hatServer: HatServer) = LoginInfo(hatServer.domain, email)

  def withRoles(roles: UserRole*): HatUser = {
    this.copy(roles = (this.roles.toSet ++ roles.toSet).toSeq)
  }

  def withoutRoles(roles: UserRole*): HatUser = {
    this.copy(roles = (this.roles.toSet -- roles.toSet).toSeq)
  }

  lazy val primaryRole: UserRole = {
    if (roles.contains(Owner())) {
      Owner()
    }
    else if (roles.contains(DataDebitOwner(""))) {
      DataDebitOwner("")
    }
    else if (roles.contains(DataCredit(""))) {
      DataCredit("")
    }
    else if (roles.contains(Platform())) {
      Platform()
    }
    else {
      Validate()
    }
  }
}

sealed abstract class UserRole(roleTitle: String) {
  def title: String = roleTitle.toLowerCase

  def name: String = this.toString.replaceAll("\\(.*\\)", "")

  def extra: Option[String] = None
}

object UserRole {
  //noinspection ScalaStyle
  def userRoleDeserialize(userRole: String, roleExtra: Option[String]): UserRole = {
    (userRole, roleExtra) match {
      case (role, None) =>
        role match {
          case "owner"      => Owner()
          case "platform"   => Platform()
          case "validate"   => Validate()
          case "datadebit"  => DataDebitOwner("")
          case "datacredit" => DataCredit("")
          case _            => UnknownRole()
        }
      case (role, Some(extra)) =>
        role match {
          case "datadebit"      => DataDebitOwner(extra)
          case "datacredit"     => DataCredit(extra)
          case "namespacewrite" => NamespaceWrite(extra)
          case "namespaceread"  => NamespaceRead(extra)
          case _                => UnknownRole()
        }
    }
  }
}

// Owner
case class Owner() extends UserRole("owner")

case class Validate() extends UserRole("validate")

case class UnknownRole() extends UserRole("unknown")

// Clients
case class DataDebitOwner(dataDebitId: String) extends UserRole(s"datadebit") {
  override def extra: Option[String] = Some(dataDebitId)
}

case class DataCredit(endpoint: String) extends UserRole(s"datacredit") {
  override def extra: Option[String] = Some(endpoint)
}

case class Platform() extends UserRole("platform")

case class NamespaceWrite(namespace: String) extends UserRole(s"namespacewrite") {
  override def extra: Option[String] = Some(namespace)
}

case class NamespaceRead(namespace: String) extends UserRole(s"namespaceread") {
  override def extra: Option[String] = Some(namespace)
}
