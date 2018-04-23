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
 * 11 / 2017
 */

package org.hatdex.hat.she.controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import org.hatdex.hat.api.json.{ DataFeedItemJsonProtocol, RichDataJsonFormats }
import org.hatdex.hat.api.models._
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.she.models.FunctionConfigurationJsonProtocol
import org.hatdex.hat.she.service._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class FeedGenerator @Inject() (
    components: ControllerComponents,
    silhouette: Silhouette[HatApiAuthEnvironment],
    feedGeneratorService: FeedGeneratorService)(
    implicit
    val ec: ExecutionContext)
  extends HatApiController(components, silhouette)
  with RichDataJsonFormats
  with FunctionConfigurationJsonProtocol
  with DataFeedItemJsonProtocol {

  private val logger = Logger(this.getClass)

  def getFeed(endpoint: String, since: Option[Long], until: Option[Long]): Action[AnyContent] =
    SecuredAction(WithRole(Owner())).async { implicit request ⇒
      logger.debug(s"Get feed for $endpoint")
      feedGeneratorService.getFeed(endpoint, since, until)
        .map(items ⇒ Ok(Json.toJson(items)))
    }

  def fullFeed(since: Option[Long], until: Option[Long]): Action[AnyContent] =
    SecuredAction(WithRole(Owner())).async { implicit request ⇒
      feedGeneratorService.fullFeed(since, until)
        .map(items ⇒ Ok(Json.toJson(items)))
    }
}
