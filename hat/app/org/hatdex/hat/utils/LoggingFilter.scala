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

package org.hatdex.hat.utils

import javax.inject.{ Inject, Singleton }

import scala.concurrent.ExecutionContext
import scala.util.Try

import akka.stream.Materializer
import com.nimbusds.jose.JWSObject
import com.nimbusds.jwt.JWTClaimsSet
import io.dataswift.log.Component
import io.dataswift.log.play.Slf4jLoggingFilter
import play.api.Configuration
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.RequestHeader

@Singleton
class ActiveHatCounter() {
  // Careful! Mutable state
  private var count: Long = 0

  def get(): Long      = count
  def increase(): Unit = this.synchronized(count += 1)
  def decrease(): Unit = this.synchronized(count -= 1)
}

class LoggingFilter @Inject() (
    configuration: Configuration,
    implicit override val mat: Materializer,
    implicit override val ec: ExecutionContext)
    extends Slf4jLoggingFilter(configuration, Component.Hat) {

  private val authTokenFieldName: String =
    configuration.get[String]("silhouette.authenticator.fieldName")

  override val body: Some[RequestHeader => JsValue] = Some((requestHeader: RequestHeader) =>
    Json.parse(
      requestHeader.queryString
        .get(authTokenFieldName)
        .flatMap(_.headOption)
        .orElse(requestHeader.headers.get(authTokenFieldName))
        .flatMap(t =>
          if (t.isEmpty) None
          else Some(t)
        )
        .flatMap(t => Try(JWSObject.parse(t)).toOption)
        .map(o => JWTClaimsSet.parse(o.getPayload.toJSONObject))
        .map { claimSet =>
        s"""
        {
          "request":{
            "uri":"${requestHeader.uri}"
            "application":{
              "id":"${Option(claimSet.getStringClaim("application")).getOrElse("api")}"
              "version":"${Option(claimSet.getStringClaim("applicationVersion")).getOrElse("_")}"
            }
           }
        }
        """
        }
        .getOrElse("""{"request":"unauthorized"}""")
    )
  )
}

object LoggingFilter {}
