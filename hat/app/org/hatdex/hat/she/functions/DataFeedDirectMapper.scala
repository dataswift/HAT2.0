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
 * 11 / 2017
 */

package org.hatdex.hat.she.functions

import org.hatdex.hat.api.json.DataFeedItemJsonProtocol
import org.hatdex.hat.api.models._
import org.hatdex.hat.she.models._
import org.hatdex.hat.she.service._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class DataFeedDirectMapper extends FunctionExecutable with DataFeedItemJsonProtocol with JodaWrites with JodaReads {
  val namespace = "she"
  val endpoint = "feed"
  private val logger: Logger = Logger(this.getClass)

  private val dataMappers: Map[String, DataEndpointMapper] = Map(
    "twitter" → new TwitterFeedMapper(),
    "facebook/feed" → new FacebookFeedMapper(),
    "facebook/events" → new FacebookEventMapper(),
    "fitbit/sleep" → new FitbitSleepMapper(),
    "fitbit/weight" → new FitbitWeightMapper(),
    "fitbit/activity" → new FitbitActivityMapper(),
    "fitbit/activity/day/summary" → new FitbitActivityDaySummaryMapper(),
    "calendar" → new GoogleCalendarMapper(),
    "notables/feed" → new NotablesFeedMapper())

  override def bundleFilterByDate(fromDate: Option[DateTime], untilDate: Option[DateTime]): EndpointDataBundle = {
    EndpointDataBundle("data-feed-direct-mapper", dataMappers.map {
      case (name, mapper) ⇒
        name → mapper.dataQueries(fromDate, untilDate)
          .reduce({ (query, secondary) ⇒ query.copy(endpoints = query.endpoints ++ secondary.endpoints) })
    })
  }

  val configuration: FunctionConfiguration = FunctionConfiguration("data-feed-direct-mapper", "",
    FunctionTrigger.TriggerIndividual(), available = true, enabled = false,
    dataBundle = bundleFilterByDate(None, None),
    None)

  def execute(configuration: FunctionConfiguration, request: Request)(implicit ec: ExecutionContext): Future[Seq[Response]] = {
    val response = request.data
      .collect {
        case (mappingEndpoint, data) ⇒
          val records = dataMappers.get(mappingEndpoint).map { m ⇒
            data.collect {
              case EndpointData(_, Some(recordId), content, _) ⇒ (recordId, m.mapDataRecord(recordId, content))
            }
          }
          (mappingEndpoint, records.getOrElse(Seq.empty))
      }
      .flatMap {
        case (mappingEndpoint, endpointData) =>
          logger.info(s"[$mappingEndpoint] Computed ${endpointData.count(r => r._2.isSuccess)} out of ${endpointData.length} records")
          endpointData.collect {
            case (_, Failure(e)) => logger.warn(s"[$mappingEndpoint] Error while transforming data: ${e.getMessage}")
          }
          endpointData.collect {
            case (recordId, Success(data)) => Response(namespace, endpoint, Seq(Json.toJson(data)), Seq(recordId))
          }
      }

    Future.successful(response.toSeq)
  }

}
