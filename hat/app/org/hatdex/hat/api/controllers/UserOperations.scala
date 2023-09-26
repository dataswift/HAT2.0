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
 * Written by Tyler Weir <tyler.weir@dataswift.io>
 * 9 / 2022
 */

package org.hatdex.hat.api.controllers

import com.mohiva.play.silhouette.api.Silhouette
import io.dataswift.models.hat.json.HatJsonFormats
import io.dataswift.models.hat.ErrorMessage
import org.hatdex.hat.api.service.{ HatServicesService, UserService }
import org.hatdex.hat.api.service.applications.{ ApplicationsService, TrustedApplicationProvider }
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.utils.HatBodyParsers
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._
import org.hatdex.hat.utils.TrustProxyUtils
import org.hatdex.hat.client.TrustProxyClient
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class UserOperations @Inject() (
    components: ControllerComponents,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    hatBodyParsers: HatBodyParsers,
    contractAction: ContractAction,
    userService: UserService,
    applicationsService: ApplicationsService,
    hatServicesService: HatServicesService,
    wsClient: WSClient
  )(implicit ec: ExecutionContext)
    extends HatApiController(components, silhouette)
    with Logging {

  import HatJsonFormats._

  case class EmailOnlyForPasswordReset(
    email: String
  )

  implicit val emailOnlyForPasswordResetReads = Json.reads[EmailOnlyForPasswordReset]
  implicit val emailOnlyForPasswordResetWrites = Json.writes[EmailOnlyForPasswordReset]

  def resetPassword(): Action[EmailOnlyForPasswordReset] =
      (UserAwareAction).async(hatBodyParsers.json[EmailOnlyForPasswordReset]) { implicit request =>
        val emailOnlyForPasswordReset = request.body

        userService.getUser(emailOnlyForPasswordReset.email).flatMap { maybeExistingUser =>
          maybeExistingUser map { _ =>
            println("found the user")
            Future.successful(Ok)
          } getOrElse {
            println("did NOT find the user")
            Future.successful(Ok)
          }
        }
    }
  }