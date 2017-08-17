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
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.HatServicesService
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.resourceManagement._
import play.api.i18n.MessagesApi
import play.api.libs.json._
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext

class Applications @Inject() (
    val messagesApi: MessagesApi,
    configuration: Configuration,
    silhouette: Silhouette[HatApiAuthEnvironment],
    hatServerProvider: HatServerProvider,
    hatServicesService: HatServicesService,
    clock: Clock)(implicit val ec: ExecutionContext) extends HatApiController(silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  val logger = Logger(this.getClass)

  def apps(): Action[AnyContent] = SecuredAction(WithRole(Owner(), Platform())).async { implicit request =>
    hatServicesService.hatServices(Set("app", "dataplug", "testapp")) map { apps =>
      Ok(Json.toJson(apps))
    }
  }

  def saveApp(): Action[HatService] = SecuredAction(WithRole(Platform())).async(parse.json[HatService]) { implicit request =>
    hatServicesService.save(request.body) map { app =>
      Ok(Json.toJson(app))
    }
  }

}

