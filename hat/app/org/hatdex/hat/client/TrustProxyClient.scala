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

package org.hatdex.hat.client

import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.{ Json, Reads }
import play.api.libs.json.Json.reads
import play.api.libs.ws.{ WSClient, WSRequest }

import scala.concurrent.{ ExecutionContext, Future }

object TrustProxyRequestTypes {
  // Public Key Request
  sealed trait PublicKeyRequestFailure
  case class PublicKeyServiceFailure(failureDescription: String) extends PublicKeyRequestFailure
  case class InvalidPublicKeyFailure(failureDescription: String) extends PublicKeyRequestFailure
  case class PublicKeyReceived(publicKey: String) extends AnyVal
}

object TrustProxyTypes {
  case class TrustProxyContent(
      iss: String,
      email: String,
      pdaUrl: String)
  implicit val of: Reads[TrustProxyContent] = reads[TrustProxyContent]

}

trait TrustProxyClient {
  import TrustProxyRequestTypes._
  def getPublicKey(
      ws: WSClient
    )(implicit ec: ExecutionContext): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]]
}

class TrustProxyWsClient(
    ws: WSClient)
    extends TrustProxyClient
    with Logging {
  import TrustProxyRequestTypes._

  override def getPublicKey(
      ws: WSClient
    )(implicit ec: ExecutionContext): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]] = {

    // FIXME: Temp until this is deployed
    val url = s"https://pdaproxy.playconcepts.co.uk/publickey"

    runPublicKeyRequest(makeRequest(url, ws))
  }

  // Base Request
  private def makeRequest(
      url: String,
      ws: WSClient
    )(implicit ec: ExecutionContext): WSRequest = {
    logger.info(s"makeRequest: ${url}")
    ws.url(url)
  }

  private def runPublicKeyRequest(
      req: WSRequest
    )(implicit ec: ExecutionContext): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]] = {
    logger.info(s"runPublicKeyRequest")
    req.get().map { response =>
      response.status match {
        case OK =>
          val body = response.body
          Right(PublicKeyReceived(body))
        case _ =>
          Left(
            PublicKeyServiceFailure(
              s"The Trusted Proxy Service responded with an error: ${response.statusText}"
            )
          )
      }
    } recover {
      case e =>
        Left(
          PublicKeyServiceFailure(
            s"The Trusted Proxy Service responded with an error: ${e.getMessage}"
          )
        )
    }
  }

}
