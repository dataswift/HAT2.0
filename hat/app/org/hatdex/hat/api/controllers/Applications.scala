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
 * 7 / 2017
 */

package org.hatdex.hat.api.controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.Clock
import org.hatdex.hat.api.json.ApplicationJsonProtocol
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.applications.ApplicationsService
import org.hatdex.hat.api.service.richData.RichDataDuplicateBundleException
import org.hatdex.hat.authentication.{ ContainsApplicationRole, HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.resourceManagement._
import play.api.libs.json._
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.{ ExecutionContext, Future }

class Applications @Inject() (
    components: ControllerComponents,
    configuration: Configuration,
    silhouette: Silhouette[HatApiAuthEnvironment],
    hatServerProvider: HatServerProvider,
    clock: Clock)(
    implicit
    val ec: ExecutionContext,
    applicationsService: ApplicationsService)
  extends HatApiController(components, silhouette, clock, hatServerProvider, configuration) with ApplicationJsonProtocol {

  import org.hatdex.hat.api.json.HatJsonFormats.errorMessage
  import org.hatdex.hat.api.json.HatJsonFormats.accessTokenFormat

  val logger = Logger(this.getClass)

  def applications(): Action[AnyContent] = SecuredAction(ContainsApplicationRole(Owner()) || WithRole(Owner())).async { implicit request =>
    applicationsService.applicationStatus()
      .map { apps ⇒
        Ok(Json.toJson(apps))
      }
  }

  def applicationStatus(id: String): Action[AnyContent] = SecuredAction(ContainsApplicationRole(Owner()) || WithRole(Owner())).async { implicit request =>
    applicationsService.applicationStatus(id).map { maybeStatus ⇒
      maybeStatus map { status ⇒
        Ok(Json.toJson(status))
      } getOrElse {
        NotFound(Json.toJson(ErrorMessage(
          "Application not Found",
          s"Application $id does not appear to be a valid application registered with the DEX")))
      }
    }

  }

  def applicationSetup(id: String): Action[AnyContent] = SecuredAction(ContainsApplicationRole(Owner()) || WithRole(Owner())).async { implicit request =>
    applicationsService.applicationStatus(id).flatMap { maybeStatus ⇒
      maybeStatus map { status ⇒
        applicationsService.setup(status)
          .map(s ⇒ Ok(Json.toJson(s)))
          .recover {
            case e: RichDataDuplicateBundleException ⇒
              logger.error(s"[${request.dynamicEnvironment.domain}] Error setting up applicatio - duplicate bundle: ${e.getMessage}")
              InternalServerError(Json.toJson(ErrorMessage(
                "Application malformed",
                s"Application $id bundle ${status.application.permissions.dataRequired.map(_.bundle.name)} clashes with one already setup")))
          }
      } getOrElse {
        Future.successful(
          BadRequest(Json.toJson(ErrorMessage(
            "Application not Found",
            s"Application $id does not appear to be a valid application registered with the DEX"))))
      }
    }
  }

  def applicationDisable(id: String): Action[AnyContent] = SecuredAction(ContainsApplicationRole(Owner()) || WithRole(Owner())).async { implicit request =>
    applicationsService.applicationStatus(id).flatMap { maybeStatus =>
      maybeStatus map { status ⇒
        applicationsService.disable(status)
          .map(s ⇒ Ok(Json.toJson(s)))
      } getOrElse {
        Future.successful(
          BadRequest(Json.toJson(ErrorMessage(
            "Application not Found",
            s"Application $id does not appear to be a valid application registered with the DEX"))))
      }
    }
  }

  def applicationToken(id: String): Action[AnyContent] = SecuredAction(ContainsApplicationRole(RetrieveApplicationToken(id)) || WithRole(Owner())).async { implicit request =>
    applicationsService.applicationStatus(id)
      .flatMap { maybeStatus =>
        maybeStatus map { status ⇒
          applicationsService.applicationToken(request.identity, status.application) map { token ⇒
            Ok(Json.toJson(token))
          }
        } getOrElse {
          Future.successful(NotFound(Json.toJson(ErrorMessage(
            "Application not Found",
            s"Application $id does not appear to be a valid application registered with the DEX"))))
        }
      }
  }

}

