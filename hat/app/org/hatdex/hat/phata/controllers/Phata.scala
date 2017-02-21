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
package org.hatdex.hat.phata.controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.Clock
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.authentication.{ HasFrontendRole, HatFrontendAuthEnvironment, HatFrontendController }
import org.hatdex.hat.phata.service.{ HatServicesService, NotablesService, UserProfileService }
import org.hatdex.hat.phata.models.{ Notable, PublicProfileResponse }
import org.hatdex.hat.phata.{ views => phataViews }
import org.hatdex.hat.resourceManagement.{ HatServerProvider, _ }
import play.api.i18n.MessagesApi
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Phata @Inject() (
    val messagesApi: MessagesApi,
    configuration: Configuration,
    silhouette: Silhouette[HatFrontendAuthEnvironment],
    hatServerProvider: HatServerProvider,
    clock: Clock,
    hatServicesService: HatServicesService,
    userProfileService: UserProfileService,
    notablesService: NotablesService
) extends HatFrontendController(silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  import org.hatdex.hat.phata.models.HatPublicInfo.hatServer2PublicInfo

  private val logger = Logger(this.getClass)

  def rumpelIndex(): Action[AnyContent] = Action { implicit request =>
    Ok(phataViews.html.rumpelIndex())
  }

  private def getProfile(maybeUser: Option[HatUser])(implicit server: HatServer, request: RequestHeader): Future[Result] = {
    val eventualProfileData = for {
      (profilePublic, profileInfo) <- userProfileService.getPublicProfile()
      notables <- notablesService.getPublicNotes()
    } yield {
      (profilePublic, profileInfo, notables)
    }

    eventualProfileData map {
      case (true, publicProfile, notables) => Ok(Json.toJson(PublicProfileResponse(true, Some(publicProfile), Some(notables))))
      case (false, publicProfile, _)       => Ok(Json.toJson(PublicProfileResponse(false, None, None)))
    } recover {
      case e => Ok(Json.toJson(PublicProfileResponse(false, None, None)))
    }
  }

  def profile: Action[AnyContent] = UserAwareAction.async { implicit request =>
    getProfile(request.identity)
  }

  def notables(id: Option[Int]): Action[AnyContent] = UserAwareAction.async { implicit request =>
    notablesService.getPublicNotes() map { notables =>
      Ok(Json.toJson(notables))
    } recover {
      case e =>
        Ok("Failed to retrieve notables")
    }
  }
}
