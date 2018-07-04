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

import java.security.MessageDigest

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.crypto.Base64
import controllers.{ AssetsFinder, AssetsFinderProvider }
import javax.inject.Inject
import org.hatdex.hat.api.json.{ HatJsonFormats, RichDataJsonFormats }
import org.hatdex.hat.api.models.EndpointDataBundle
import org.hatdex.hat.api.service.richData.{ RichBundleService, RichDataService }
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.phata.{ views ⇒ phataViews }
import play.api.cache.{ Cached, CachedBuilder }
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{ Configuration, Logger }
import play.filters.headers.SecurityHeadersFilter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Phata @Inject() (
    components: ControllerComponents,
    assetsFinder: AssetsFinderProvider,
    cached: Cached,
    configuration: Configuration,
    silhouette: Silhouette[HatApiAuthEnvironment],
    bundleService: RichBundleService,
    dataService: RichDataService) extends HatApiController(components, silhouette) with HatJsonFormats with RichDataJsonFormats {

  implicit val assets: AssetsFinder = assetsFinder.get

  private val logger = Logger(this.getClass)

  val indefiniteSuccessCaching: CachedBuilder = cached
    .status(req => s"${req.host}${req.path}", 200)
    .includeStatus(404, 600)

  val csp: Map[String, String] = configuration.get[String]("play.filters.headers.contentSecurityPolicy")
    .split(';')
    .map(_.trim)
    .map({ p ⇒
      val splits = p.split(' ')
      splits.head → splits.tail.mkString(" ")
    })
    .toMap

  def rumpelIndex(): EssentialAction = indefiniteSuccessCaching {
    UserAwareAction.async { implicit request =>
      val rumpelConfigScript = s"""var httpProtocol = "${if (request.secure) { "https" } else { "http" }}:";"""

      val sha256Encoded = Base64.encode(MessageDigest.getInstance("SHA-256").digest(rumpelConfigScript.getBytes("UTF-8")))
      val cspMap: Map[String, String] = csp + ("script-src" → (csp.getOrElse("script-src", "") + s" 'sha256-$sha256Encoded'"))
      val cspCustom = cspMap
        .map({
          case (k: String, v: String) ⇒ s"$k $v"
        })
        .mkString("; ")

      Future.successful(Ok(phataViews.html.rumpelIndex(rumpelConfigScript, assets))
        .withHeaders(SecurityHeadersFilter.CONTENT_SECURITY_POLICY_HEADER → cspCustom))
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
      val scheme = if (request.secure) { "https://" } else { "http://" }
      val newRedirectUrl = s"$scheme${request.domain}/#/hatlogin?name=$name&redirect=${redirectUrl}"
      logger.debug(s"Redirect url from ${request.uri}: $newRedirectUrl")
      Redirect(newRedirectUrl)
    }
  }

  //  def remoteAsset(file: String): String = {
  //    val versionedUrl = assets.path(file)
  //    val maybeAssetsUrl = Some("https://rumpel.hubat.net/assets") //configuration.getOptional[String]("assets.url")
  //    maybeAssetsUrl.fold(versionedUrl)(_ + file)
  //  }

}
