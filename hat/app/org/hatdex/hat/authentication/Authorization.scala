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

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.JWTRS256Authenticator
import org.hatdex.hat.api.models.{ Owner, RetrieveApplicationToken, UserRole }
import org.hatdex.hat.api.service.applications.ApplicationsService
import org.hatdex.hat.authentication.models._
import org.hatdex.hat.resourceManagement.HatServer
import play.api.Logger
import play.api.mvc.Request

import scala.concurrent.{ ExecutionContext, Future }

object WithTokenParameters {
  def roleMatchesToken(user: HatUser, authenticator: JWTRS256Authenticator, roles: UserRole*): Boolean = {
    authenticator.customClaims.flatMap { claims =>
      (claims \ "accessScope").validate[String].asOpt.map(_.toLowerCase).map { scope =>
        user.roles.map(_.title.toLowerCase).contains(scope)
      }
    } getOrElse {
      true
    }
  }
}

/**
 * Only allows those users that have at least a service of the selected.
 * Master service is always allowed.
 * Ex: WithService("serviceA", "serviceB") => only users with services "serviceA" OR "serviceB" (or "master") are allowed.
 */
case class WithRole(anyOf: UserRole*) extends Authorization[HatUser, JWTRS256Authenticator, HatServer] {
  def isAuthorized[B](user: HatUser, authenticator: JWTRS256Authenticator, hat: HatServer)(implicit r: Request[B]): Future[Boolean] = {
    authenticator.customClaims.map(_ \ "application")
    Future.successful {
      WithRole.isAuthorized(user, authenticator, anyOf: _*)
    }
  }
}

object WithRole {
  def isAuthorized(user: HatUser, authenticator: JWTRS256Authenticator, anyOf: UserRole*): Boolean = {
    WithTokenParameters.roleMatchesToken(user, authenticator) &&
      authenticator.customClaims.forall(!_.keys.contains("application")) && // must NOT contain application claim
      (anyOf.intersect(user.roles).nonEmpty || user.roles.contains(Owner()))
  }
}

/**
 * Only allows those users that have every of the selected services.
 * Master service is always allowed.
 * Ex: Restrict("serviceA", "serviceB") => only users with services "serviceA" AND "serviceB" (or "master") are allowed.
 */
case class WithRoles(allOf: UserRole*) extends Authorization[HatUser, JWTRS256Authenticator, HatServer] {
  def isAuthorized[B](user: HatUser, authenticator: JWTRS256Authenticator, hat: HatServer)(implicit r: Request[B]): Future[Boolean] = {
    Future.successful {
      WithRoles.isAuthorized(user, authenticator, allOf: _*)
    }
  }
}

object WithRoles {
  def isAuthorized(user: HatUser, authenticator: JWTRS256Authenticator, allOf: UserRole*): Boolean = {
    WithTokenParameters.roleMatchesToken(user, authenticator) &&
      authenticator.customClaims.forall(!_.keys.contains("application")) && // must NOT contain application claim
      (allOf.intersect(user.roles).length == allOf.length || user.roles.contains(Owner()))
  }
}

case class ContainsApplicationRole(anyOf: UserRole*)(implicit val applicationsService: ApplicationsService, ec: ExecutionContext)
  extends Authorization[HatUser, JWTRS256Authenticator, HatServer] {

  private val rolesRequiringExplicitApproval: Set[String] = Set(RetrieveApplicationToken("").title)

  def isAuthorized[B](user: HatUser, authenticator: JWTRS256Authenticator, hat: HatServer)(implicit r: Request[B]): Future[Boolean] = {
    val containsApplicationClaim = authenticator.customClaims.exists(_.keys.contains("application")) // MUST contain application claim
    val status = authenticator.customClaims.flatMap { customClaims ⇒
      (customClaims \ "application").asOpt[String]
    } map { app ⇒
      applicationsService.applicationStatus(app)(hat, user, r)
    } getOrElse {
      Future.successful(None)
    }

    status map { maybeAppStatus ⇒
      val appStatusOk = maybeAppStatus map { appStatus ⇒
        (appStatus.active && appStatus.application.permissions.rolesGranted.intersect(anyOf).nonEmpty) || // App has been granted a specific role
          (appStatus.application.permissions.rolesGranted.contains(Owner()) && // Or is an owner app
            anyOf.exists(r ⇒ !rolesRequiringExplicitApproval.contains(r.title))) // is there a required role that does not require explicit approval even for owner scope
      } getOrElse false // Unauthorized if app status not found
      val userRoleAuthorized = anyOf.intersect(user.roles).nonEmpty || user.roles.contains(Owner())
      containsApplicationClaim && appStatusOk && userRoleAuthorized
    }
  }
}
