/*
 * Copyright (C) 2018 HAT Data Exchange Ltd
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
 * Written by Terry Lee <terry.lee@hatdex.org>
 * 12 / 2018
 */
package org.hatdex.hat.api.service.monitoring

import akka.Done
import javax.inject.{ Inject, Singleton }
import org.hatdex.hat.api.models.EndpointData
import org.hatdex.hat.api.service.richData.DataDebitService
import org.hatdex.hat.resourceManagement.HatServer
import org.joda.time.LocalDateTime
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class HatDataEventsNotifier @Inject() (wsClient: WSClient, dataDebitService: DataDebitService) {

  protected val logger = Logger(this.getClass)

  def sendNotification(endpoints: Seq[EndpointData])(implicit server: HatServer): Future[Done] = {
    logger.debug(s"Sending notifications for ${server.hatName}")

    val endpts = endpoints.collect {
      case ep: EndpointData => ep.endpoint
    }.distinct

    endpts.foreach { endpoint =>
      val callbackUrlsResult = for {
        results <- dataDebitService.dataDebitCallback(endpoint)
      } yield results

      callbackUrlsResult.foreach { callbackUrls =>
        callbackUrls.foreach { callbackUrl =>
          {
            val payload = Json.obj(
              "hat" -> server.domain,
              "dataDebitId" -> callbackUrl._1,
              "timestamp" -> LocalDateTime.now().toString)
            wsClient.url(callbackUrl._2)
              .withHttpHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
              .post(payload)
          }
        }
      }
    }

    Future.successful(Done)
  }
}
