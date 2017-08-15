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
import org.hatdex.hat.api.models.ErrorMessage
import org.hatdex.hat.api.service.HatServicesService
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.authentication.{ HatFrontendAuthEnvironment, HatFrontendController }
import org.hatdex.hat.phata.models.PublicProfileResponse
import org.hatdex.hat.phata.service.{ NotablesService, UserProfileService }
import org.hatdex.hat.phata.{ views => phataViews }
import org.hatdex.hat.resourceManagement.{ HatServerProvider, _ }
import play.api.cache.Cached
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Phata @Inject() (
    val messagesApi: MessagesApi,
    cached: Cached,
    configuration: Configuration,
    silhouette: Silhouette[HatFrontendAuthEnvironment],
    hatServerProvider: HatServerProvider,
    clock: Clock,
    wsClient: WSClient,
    hatServicesService: HatServicesService,
    userProfileService: UserProfileService,
    notablesService: NotablesService) extends HatFrontendController(silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  private val logger = Logger(this.getClass)

  val indefiniteSuccessCaching = cached
    .status(req => s"${req.host}${req.path}", 200)
    .includeStatus(404, 600)

  def rumpelIndex(): EssentialAction = indefiniteSuccessCaching {
    UserAwareAction.async { implicit request =>
      Future.successful(Ok(phataViews.html.rumpelIndex(configuration.getString("frontend.protocol").getOrElse("https:"))))
    }
  }

  private def getProfile(maybeUser: Option[HatUser])(implicit server: HatServer, request: RequestHeader): Future[Result] = {
    val eventualProfileData = for {
      (profilePublic, profileInfo) <- userProfileService.getPublicProfile()
      notables <- notablesService.getPublicNotes()
    } yield {
      (profilePublic, profileInfo, notables)
    }

    eventualProfileData map {
      case (true, publicProfile, notables) => Ok(Json.toJson(PublicProfileResponse(public = true, Some(publicProfile), Some(notables))))
      case (false, _, _)                   => Ok(Json.toJson(PublicProfileResponse(public = false, None, None)))
    } recover {
      case _ => Ok(Json.toJson(PublicProfileResponse(public = false, None, None)))
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
        InternalServerError(Json.toJson(ErrorMessage("Server Error", "Failed to retrieve notables")))
    }
  }

  def hatLogin(name: String, redirectUrl: String) = indefiniteSuccessCaching {
    UserAwareAction { implicit request =>
      val uri = wsClient.url(routes.Phata.hatLogin(name, redirectUrl).absoluteURL()).uri
      val newRedirectUrl = s"${uri.getScheme}://${uri.getAuthority}/#/hatlogin?${uri.getQuery}"
      logger.debug(s"Redirect url from ${request.uri}: ${newRedirectUrl}")
      Redirect(newRedirectUrl)
    }
  }
}
