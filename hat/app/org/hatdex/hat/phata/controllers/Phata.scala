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
import controllers.{ AssetsFinder, AssetsFinderProvider }
import org.hatdex.hat.api.json.{ HatJsonFormats, RichDataJsonFormats }
import org.hatdex.hat.api.models.EndpointDataBundle
import org.hatdex.hat.api.service.richData.{ RichBundleService, RichDataService }
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.phata.{ views ⇒ phataViews }
import play.api.cache.{ Cached, CachedBuilder }
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Phata @Inject() (
    components: ControllerComponents,
    assetsFinder: AssetsFinderProvider,
    cached: Cached,
    configuration: Configuration,
    silhouette: Silhouette[HatApiAuthEnvironment],
    wsClient: WSClient,
    bundleService: RichBundleService,
    dataService: RichDataService) extends HatApiController(components, silhouette) with HatJsonFormats with RichDataJsonFormats {

  implicit val assets: AssetsFinder = assetsFinder.get

  private val logger = Logger(this.getClass)

  val indefiniteSuccessCaching: CachedBuilder = cached
    .status(req => s"${req.host}${req.path}", 200)
    .includeStatus(404, 600)

  def rumpelIndex(): EssentialAction = indefiniteSuccessCaching {
    Action.async { implicit request =>
      Future.successful(Ok(phataViews.html.rumpelIndex(assets)))
    }
  }

  def profile: Action[AnyContent] = UserAwareAction.async { implicit request =>
    val defaultBundleDefinition = Json.parse(configuration.get[String]("phata.defaultBundle")).as[EndpointDataBundle]
    for {
      bundle <- bundleService.bundle(defaultBundleDefinition.name).map(_.getOrElse(defaultBundleDefinition))
      data <- dataService.bundleData(bundle, None, None, None)
    } yield {
      Ok(Json.toJson(data))
    }
  }

  def hatLogin(name: String, redirectUrl: String) = indefiniteSuccessCaching {
    Action { implicit request =>
      val uri = wsClient.url(routes.Phata.hatLogin(name, redirectUrl).absoluteURL()).uri
      val newRedirectUrl = s"${uri.getScheme}://${uri.getAuthority}/#/hatlogin?${uri.getQuery}"
      logger.debug(s"Redirect url from ${request.uri}: $newRedirectUrl")
      Redirect(newRedirectUrl)
    }
  }
}
