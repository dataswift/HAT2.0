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

package org.hatdex.hat.utils

import org.hatdex.hat.client.TrustProxyTypes._
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim }
import play.api.libs.json.Json

import java.security.PublicKey
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import scala.util.{ Failure, Success, Try }
import play.api.Logger

object TrustProxyUtils {

  val logger: Logger = Logger(this.getClass)
  def stringToPublicKey(publicKeyAsString: String): PublicKey = {
    val pKey                      = publicKeyAsString
    val rsaKeyFactory: KeyFactory = KeyFactory.getInstance("RSA")

    val strippedKeyText = pKey.stripMargin
      .replace("\n", "")
      .replace("-----BEGIN PUBLIC KEY-----", "")
      .replace("-----END PUBLIC KEY-----", "")

    val bytes = Base64.getDecoder.decode(strippedKeyText)

    rsaKeyFactory.generatePublic(new X509EncodedKeySpec(bytes))
  }

  def decodeToken(
      token: String,
      key: PublicKey): Option[JwtClaim] = {
    val ret: Try[JwtClaim] = Jwt.decode(token, key, Seq(JwtAlgorithm.RS256))

    ret match {
      case Success(value) =>
        Some(value)
      case Failure(exception) =>
        logger.debug(s"Failed to decode token: ${exception.getMessage}")
        None
    }
  }

  def verifyToken(
      token: String,
      publicKey: PublicKey,
      email: String,
      pdaUrl: String,
      issuer: String): Boolean = {
    val jwtClaim = decodeToken(token, publicKey)
    jwtClaim match {
      case Some(value) =>
        val c = Json.parse(value.content).as[TrustProxyContent]

        if (
          c.iss.toLowerCase() == issuer.toLowerCase()
          && c.pdaUrl.toLowerCase() == pdaUrl.toLowerCase()
          && c.email.toLowerCase() == email.toLowerCase()
        ) {
          logger.debug(s"Issuer, email and pdaUrl match")
          println(s"Issuer, email and pdaUrl match")
          true
        } else {
          logger.debug(email)
          logger.debug(issuer)
          logger.debug(s"Issuer, email and pdaUrl do not match")
          false
        }
      case _ =>
        false
    }
  }

}
