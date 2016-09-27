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
package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import hatdex.hat.authentication.JwtTokenHandler
import hatdex.hat.authentication.models.{AccessToken, User}
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.Duration._
import spray.http.Uri

import scala.concurrent.{ExecutionContext, Future}

trait HatServicesService extends JwtTokenHandler {
  val logger: LoggingAdapter
  implicit val dalExecutionContext: ExecutionContext

  def hatServices(categories: Set[String]): Future[Seq[HatService]] = {
    val applicationsQuery = for {
      application <- Applications.filter(_.category inSet categories)
    } yield application

    val eventualDbApps = DatabaseInfo.db.run(applicationsQuery.result)
    eventualDbApps map { dbApps =>
      dbApps.map(applicationFromDbModel)
    }
  }

  private def applicationFromDbModel(app: ApplicationsRow): HatService = {
    HatService(app.title, app.description, app.logoUrl, app.url, app.authUrl, app.browser, app.category, app.setup, app.loginAvailable)
  }

  def findOrCreateHatService(name: String, redirectUrl: String): Future[HatService] = {
    val redirectUri = Uri(redirectUrl)

    hatServices(Set("app", "dataplug", "testapp")) map { approvedHatServices =>
      approvedHatServices.find(s => s.title == name && redirectUrl.startsWith(s.url))
        .map(_.copy(url = s"${redirectUri.scheme}:${redirectUri.authority.toString}", authUrl = redirectUri.toRelative.toString()))
        .getOrElse(
          HatService(name, redirectUrl, "/assets/images/haticon.png",
            redirectUrl, redirectUri.path.toString(),
            browser = false, category = "app", setup = true,
            loginAvailable = true))
    }
  }

  protected def hatServiceToken(user: User, service: HatService): Future[AccessToken] = {
    val accessScope = if (service.browser) { user.role } else { "validate" }
    val resource = if (service.browser) { issuer } else { service.url }
    val validity = standardDays(1)

    fetchOrGenerateToken(user, resource = resource, accessScope, validity)
  }

  def hatServiceLink(user: User, service: HatService): Future[HatService] = {
    val eventualUri = if (service.loginAvailable) {
      hatServiceToken(user, service) map { token =>
        Uri(service.url).withPath(Uri.Path(service.authUrl)).withQuery(Uri.Query("token" -> token.accessToken))
      }
    } else {
      Future.successful(Uri(service.url).withPath(Uri.Path(service.authUrl)))
    }

    eventualUri map { serviceLink =>
      service.copy(url = serviceLink.toString, authUrl = "")
    }
  }

}
