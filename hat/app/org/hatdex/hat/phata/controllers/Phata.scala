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
import org.hatdex.hat.phata.{ views => phataViews }
import org.hatdex.hat.resourceManagement.{ HatServerProvider, _ }
import play.api.i18n.MessagesApi
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

  def home: Action[AnyContent] = UserAwareAction.async { implicit request =>
    request.identity map { implicit identity =>
      Future.successful(Redirect(org.hatdex.hat.phata.controllers.routes.Phata.launcher()))
    } getOrElse {
      getProfile(None)
    }
  }

  def launcher: Action[AnyContent] = SecuredAction(HasFrontendRole("owner")).async { implicit request =>
    val user = request.identity
    val futureCredentials = for {
      services <- hatServicesService.hatServices(Set("app", "dataplug", "testapp"))
      serviceCredentials <- Future.sequence(services.map(hatServicesService.hatServiceLink(user, _)))
    } yield serviceCredentials

    futureCredentials map { credentials =>
      Ok(phataViews.html.authenticated(user, credentials))
    } recover {
      case e =>
        logger.warn(s"Error resolving access tokens for auth page: ${e.getMessage}")
        Ok(phataViews.html.authenticated(user, Seq()))
    }
  }

  private def getProfile(maybeUser: Option[HatUser])(implicit server: HatServer, request: RequestHeader): Future[Result] = {
    val eventualProfileData = for {
      (profilePublic, profileInfo) <- userProfileService.getPublicProfile()
      notables <- notablesService.getPublicNotes()
    } yield (profilePublic, profileInfo, notables)

    eventualProfileData map {
      case (true, publicProfile, notables) => Ok(phataViews.html.index(publicProfile, maybeUser, notables))
      case (false, publicProfile, _)       => Ok(phataViews.html.indexPrivate(maybeUser))
    } recover {
      case e => Ok(phataViews.html.indexPrivate(maybeUser))
    }
  }

  def profile: Action[AnyContent] = UserAwareAction.async { implicit request =>
    getProfile(request.identity)
  }

  def notables(id: Option[Int]): Action[AnyContent] = UserAwareAction.async { implicit request =>
    notablesService.getPublicNotes() map { notables =>
      val selectedNotable = notables.find(_.id == id).orElse(notables.headOption)
      Ok(phataViews.html.notablesPage(request.identity, selectedNotable, notables))
    } recover {
      case e =>
        Ok(phataViews.html.indexPrivate(request.identity))
    }
  }
}
