/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
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
 */
package org.hatdex.hat.phata.service

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.impl.authenticators.JWTRS256Authenticator
import org.hatdex.hat.api.actors.DalExecutionContext
import org.hatdex.hat.api.models._
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SlickPostgresDriver.api._
import org.hatdex.hat.dal.Tables._
import org.hatdex.hat.resourceManagement.HatServer
import play.api.Logger
import play.api.libs.json.{ JsObject, Json }
import play.api.mvc.RequestHeader
import spray.http.Uri

import scala.concurrent.Future

class HatServicesService @Inject() (silhouette: Silhouette[HatApiAuthEnvironment]) extends DalExecutionContext {
  private val logger = Logger(this.getClass)

  def hatServices(categories: Set[String])(implicit hatServer: HatServer): Future[Seq[HatService]] = {
    val applicationsQuery = for {
      application <- Applications.filter(_.category inSet categories)
    } yield application

    val eventualDbApps = hatServer.db.run(applicationsQuery.result)
    eventualDbApps map { dbApps =>
      dbApps.map(applicationFromDbModel)
    }
  }

  private def applicationFromDbModel(app: ApplicationsRow): HatService = {
    HatService(app.title, app.description, app.logoUrl, app.url, app.authUrl, app.browser, app.category, app.setup, app.loginAvailable)
  }

  def findOrCreateHatService(name: String, redirectUrl: String)(implicit hatServer: HatServer): Future[HatService] = {
    val redirectUri = Uri(redirectUrl)

    hatServices(Set("app", "dataplug", "testapp")) map { approvedHatServices =>
      approvedHatServices.find(s => s.title == name && redirectUrl.startsWith(s.url))
        .map(_.copy(url = s"${redirectUri.scheme}:${redirectUri.authority.toString}", authUrl = redirectUri.toRelative.toString()))
        .getOrElse(
          HatService(name, redirectUrl, "/assets/images/haticon.png",
            redirectUrl, redirectUri.path.toString(),
            browser = false, category = "app", setup = true,
            loginAvailable = true)
        )
    }
  }

  protected def hatServiceToken(user: HatUser, service: HatService)(implicit hatServer: HatServer, requestHeader: RequestHeader): Future[AccessToken] = {
    val accessScope = if (service.browser) { user.role } else { "validate" }
    val resource = if (service.browser) { hatServer.domain } else { service.url }

    val customClaims = JsObject(Map(
      "resource" -> Json.toJson(resource),
      "accessScope" -> Json.toJson(accessScope)
    ))

    silhouette.env.authenticatorService.create(user.loginInfo)
      .map(_.copy(customClaims = Some(customClaims)))
      .flatMap(silhouette.env.authenticatorService.init)
      .map(AccessToken(_, user.userId))
  }

  def hatServiceLink(user: HatUser, service: HatService)(implicit hatServer: HatServer, requestHeader: RequestHeader): Future[HatService] = {
    val eventualUri = if (service.loginAvailable) {
      hatServiceToken(user, service) map { token =>
        Uri(service.url).withPath(Uri.Path(service.authUrl)).withQuery(Uri.Query("token" -> token.accessToken))
      }
    }
    else {
      Future.successful(Uri(service.url).withPath(Uri.Path(service.authUrl)))
    }

    eventualUri map { serviceLink =>
      service.copy(url = serviceLink.toString, authUrl = "")
    }
  }

}