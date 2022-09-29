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
import io.dataswift.models.hat.{ EndpointData, EndpointQuery, ErrorMessage }
import org.hatdex.hat.api.service.{ HatServicesService, UserService }
import org.hatdex.hat.api.service.applications.{ ApplicationsService, TrustedApplicationProvider }
import org.hatdex.hat.api.service.richData.{ RichDataMissingException, RichDataService }
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.hat.utils.HatBodyParsers
import org.hatdex.libs.dal.HATPostgresProfile
import pdi.jwt.JwtClaim
import play.api.Logging
import play.api.libs.json.{ JsArray, JsValue, Json }
import play.api.mvc._
import org.hatdex.hat.utils.TrustProxyUtils
import org.hatdex.hat.client.TrustProxyClient
import play.api.libs.ws.WSClient

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class TrustedProxy @Inject() (
    components: ControllerComponents,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    dataService: RichDataService,
    trustProxyClient: TrustProxyClient,
    trustedApplicationProvider: TrustedApplicationProvider,
    contractAction: ContractAction,
    userService: UserService,
    applicationsService: ApplicationsService,
    hatServicesService: HatServicesService,
    wsClient: WSClient
  )(implicit ec: ExecutionContext)
    extends HatApiController(components, silhouette)
    with Logging {

  import HatJsonFormats._

  //import io.dataswift.models.hat.json.RichDataJsonFormats._
  case class AppStatus(
      active: Boolean,
      enabled: Boolean)

  private def trustProxyVerification(
      wsClient: WSClient,
      ownerEmail: String,
      domain: String,
      trustToken: Option[String]): Future[Boolean] = {
    val a = trustProxyClient
      .getPublicKey(wsClient)
      .flatMap {
        case Left(_) => Future.failed(new UnknownError("public key failed"))
        case Right(value) =>
          val rsaPublicKey = TrustProxyUtils.stringToPublicKey(value.publicKey)
          val verified = TrustProxyUtils.verifyToken(
            trustToken.getOrElse(""),
            rsaPublicKey,
            ownerEmail,
            domain,
            "pda-api-gateway"
          )
          Future.successful(verified)
      }
    a
  }

  def applicationStatus(applicationId: String): EssentialAction =
    UserAwareAction.async { implicit request =>
      val verified = trustProxyVerification(
        wsClient,
        request.dynamicEnvironment.ownerEmail,
        request.dynamicEnvironment.domain,
        request.headers.get("TRUST_TOKEN")
      )

      verified.flatMap { v =>
        if (v)
          userService.getUser(request.dynamicEnvironment.hatName).flatMap {
            case Some(user) =>
              println(user)
              val result =
                applicationsService.applicationSetupStatus(applicationId)(request.dynamicEnvironment.db)
              result.flatMap { row =>
                println(row)
                row match {
                  case Some(r) =>
                    println(r)
                    Future.successful(Ok("ok"))
                  case None =>
                    println("app not found")
                    Future.failed(new UnknownError("App not found"))
                }
              }
            case None =>
              println("user not found")
              Future.failed(new UnknownError("HAT not found"))
          }

        // Future.failed(new UnknownError("HAT claim failed"))
        else
          Future.failed(new UnknownError("HAT claim failed"))
      }
    }

  def applicationToken(id: String): Action[AnyContent] =
    UserAwareAction.async { implicit request =>
      val verified = trustProxyVerification(
        wsClient,
        request.dynamicEnvironment.ownerEmail,
        request.dynamicEnvironment.domain,
        request.headers.get("TRUST_TOKEN")
      )

      verified.flatMap { v =>
        if (v) {
          println("GET THE APPLICATION_TOKEN")
          // -------------
          userService.getUser(request.dynamicEnvironment.hatName).flatMap {
            case Some(hatUser) =>
              trustedApplicationProvider
                .application(id)
                .flatMap { maybeApp =>
                  maybeApp map { app =>
                    applicationsService.applicationToken(
                      hatUser,
                      app
                    ) map { token =>
                      Ok(Json.toJson(token))
                    }
                  } getOrElse {
                    Future.successful(
                      NotFound(
                        Json.toJson(
                          ErrorMessage(
                            "Application not Found",
                            s"Application $id does not appear to be a valid application registered with the DEX"
                          )
                        )
                      )
                    )
                  }
                }
          }
          // -------------
          // Future.successful(Ok("applicationToken"))
        } else {
          println("GET FUCT")
          Future.failed(new UnknownError("HAT claim failed"))
        }
      }
    }
}
