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

package org.hatdex.hat.authentication

import com.mohiva.play.silhouette.api.{ Authenticator, Authorization }
import com.mohiva.play.silhouette.impl.authenticators.{ CookieAuthenticator, JWTRS256Authenticator }
import org.hatdex.hat.authentication.models.HatUser
import play.api.mvc.Request

import scala.concurrent.Future

trait AccessScopeValidator {
  def isValid(user: HatUser, authenticator: JWTRS256Authenticator, roles: String*)(isAuthorized: (HatUser, Seq[String]) => Boolean): Boolean = {
    authenticator.customClaims.flatMap { claims =>
      (claims \ "accessScope").validate[String].asOpt.map { scope =>
        if (scope == user.role) {
          isAuthorized(user, roles)
        }
        else {
          false
        }
      }
    } getOrElse {
      false
    }
  }
}

/**
 * Only allows those users that have at least a service of the selected.
 * Master service is always allowed.
 * Ex: WithService("serviceA", "serviceB") => only users with services "serviceA" OR "serviceB" (or "master") are allowed.
 */
case class WithRole(anyOf: String*) extends Authorization[HatUser, JWTRS256Authenticator] with AccessScopeValidator {
  def isAuthorized[B](user: HatUser, authenticator: JWTRS256Authenticator)(implicit r: Request[B]): Future[Boolean] = {
    Future.successful {
      isValid(user, authenticator, anyOf: _*)(WithRole.isAuthorized)
    }
  }

}
object WithRole {
  def isAuthorized(user: HatUser, anyOf: String*): Boolean =
    anyOf.contains(user.role)
}

case class HasFrontendRole(anyOf: String*) extends Authorization[HatUser, CookieAuthenticator] {
  def isAuthorized[B](user: HatUser, authenticator: CookieAuthenticator)(implicit r: Request[B]): Future[Boolean] = {
    Future.successful {
      WithRole.isAuthorized(user, anyOf: _*)
    }
  }
}
object HasFrontendRole {
  def isAuthorized(user: HatUser, anyOf: String*): Boolean =
    anyOf.contains(user.role)
}

/**
 * Only allows those users that have every of the selected services.
 * Master service is always allowed.
 * Ex: Restrict("serviceA", "serviceB") => only users with services "serviceA" AND "serviceB" (or "master") are allowed.
 */
case class WithRoles(allOf: String*) extends Authorization[HatUser, JWTRS256Authenticator] with AccessScopeValidator {
  def isAuthorized[B](user: HatUser, authenticator: JWTRS256Authenticator)(implicit r: Request[B]): Future[Boolean] = {
    Future.successful {
      isValid(user, authenticator, allOf: _*)(WithRoles.isAuthorized)
    }
  }
}

object WithRoles {
  def isAuthorized(user: HatUser, allOf: String*): Boolean =
    allOf.forall(_ == user.role)
}
