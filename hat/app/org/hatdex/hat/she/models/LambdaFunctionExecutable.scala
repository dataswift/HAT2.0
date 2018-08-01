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
 * 8 / 2018
 */

package org.hatdex.hat.she.models

import org.hatdex.dex.apiV2.services.Errors.{ ApiException, DataFormatException }
import org.hatdex.hat.api.models.EndpointDataBundle
import org.hatdex.hat.api.models.applications.Version
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{ ExecutionContext, Future }

class LambdaFunctionExecutable(
    id: String,
    version: Version,
    baseUrl: String,
    val configuration: FunctionConfiguration)(
    wsClient: WSClient)(
    implicit
    val ec: ExecutionContext) extends FunctionExecutable {
  import FunctionConfigurationJsonProtocol._
  import play.api.http.Status._

  protected val logger = Logger(this.getClass)

  logger.info(s"Initialised SHE lambda function $id v$version")

  def execute(configuration: FunctionConfiguration, request: Request): Future[Seq[Response]] = {
    wsClient.url(s"$baseUrl/$version/$id")
      .post(Json.toJson(Map(
        "functionConfiguration" → Json.toJson(configuration),
        "request" → Json.toJson(request))))
      .map(response ⇒
        response.status match {
          case OK ⇒
            val jsResponse = response.json.validate[Seq[Response]] recover {
              case e =>
                val message = s"Error parsing SHE function results: $e"
                logger.error(message)
                throw DataFormatException(message)
            }
            // Convert to OfferClaimsInfo - if validation has failed, it will have thrown an error already
            jsResponse.get
          case _ ⇒
            val message = s"Retrieving SHE function results failed: $response, ${response.body}"
            logger.error(message)
            throw new ApiException(message)
        })
  }

  override def bundleFilterByDate(fromDate: Option[DateTime], untilDate: Option[DateTime]): Future[EndpointDataBundle] = {
    wsClient.url(s"$baseUrl/$id/$version/data-bundle")
      .withQueryStringParameters(Seq(fromDate.map(f ⇒ "from" → f.toString), untilDate.map(f ⇒ "until" → f.toString)).flatten: _*)
      .get()
      .map(response ⇒
        response.status match {
          case OK ⇒
            val jsResponse = response.json.validate[EndpointDataBundle] recover {
              case e =>
                val message = s"Error parsing SHE function data bundle structures: $e"
                logger.error(message)
                throw DataFormatException(message)
            }
            // Convert to OfferClaimsInfo - if validation has failed, it will have thrown an error already
            jsResponse.get
          case _ ⇒
            val message = s"Retrieving SHE function bundle failed: $response, ${response.body}"
            logger.error(message)
            throw new ApiException(message)
        })
  }
}

object LambdaFunctionExecutable {
  import play.api.http.Status._
  protected val logger = Logger(this.getClass)
  import FunctionConfigurationJsonProtocol.functionConfigurationFormat

  def apply(wsClient: WSClient)(id: String, version: Version, baseUrl: String)(implicit ec: ExecutionContext): Future[LambdaFunctionExecutable] = {
    wsClient.url(s"$baseUrl/$id/$version/configuration")
      .get()
      .map(response ⇒
        response.status match {
          case OK ⇒
            val jsResponse = response.json.validate[FunctionConfiguration] recover {
              case e =>
                val message = s"Error parsing SHE function data bundle structures: $e"
                logger.error(message)
                throw DataFormatException(message)
            }
            // Convert to OfferClaimsInfo - if validation has failed, it will have thrown an error already
            new LambdaFunctionExecutable(id, version, baseUrl, jsResponse.get)(wsClient)
          case _ ⇒
            val message = s"Retrieving SHE function bundle failed: $response, ${response.body}"
            logger.error(message)
            throw new ApiException(message)
        })
  }
}