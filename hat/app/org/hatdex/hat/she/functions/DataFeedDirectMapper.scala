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
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class DataFeedDirectMapper extends FunctionExecutable with DataFeedItemJsonProtocol with JodaWrites with JodaReads {
  val namespace = "she"
  val endpoint = "feed"

  val configuration: FunctionConfiguration = FunctionConfiguration("data-feed-direct-mapper", "",
    FunctionTrigger.TriggerIndividual(), available = true, enabled = false,
    dataBundle = bundleFilterByDate(None, None),
    None)

  private val dataMappers: Map[String, DataEndpointMapper] = {
    Map(
      "facebook/feed" → new FacebookFeedMapper(),
      "facebook/events" → new FacebookEventMapper(),
      "twitter" → new TwitterFeedMapper(),
      "fitbit/sleep" → new FitbitSleepMapper(),
      "fitbit/weight" → new FitbitWeightMapper(),
      "fitbit/activity" → new FitbitActivityMapper(),
      "fitbit/activity/day/summary" → new FitbitActivityDaySummaryMapper(),
      "calendar" → new GoogleCalendarMapper(),
      "notables/feed" → new NotablesFeedMapper())
  }

  override def bundleFilterByDate(fromDate: Option[DateTime], untilDate: Option[DateTime]): EndpointDataBundle = {
    val fmt = ISODateTimeFormat.dateTime()
    val dateTimeFilter = if (fromDate.isDefined) {
      Some(FilterOperator.Between(Json.toJson(fromDate.map(_.toString(fmt))), Json.toJson(untilDate.map(_.toString(fmt)))))
    }
    else {
      None
    }
    val dateFilter = if (fromDate.isDefined) {
      Some(FilterOperator.Between(Json.toJson(fromDate.map(_.toString("yyyy-MM-dd"))), Json.toJson(untilDate.map(_.toString("yyyy-MM-dd")))))
    }
    else {
      None
    }
    EndpointDataBundle("data-feed-direct-mapper", Map(
      "twitter" -> PropertyQuery(
        List(
          EndpointQuery("twitter/tweets", None, dateTimeFilter.map(f => Seq(EndpointQueryFilter("lastUpdated", None, f))), None)),
        Some("lastUpdated"), None, None),
      "facebook/feed" -> PropertyQuery(
        List(
          EndpointQuery("facebook/feed", None, dateTimeFilter.map(f => Seq(EndpointQueryFilter("created_time", None, f))), None)),
        Some("created_time"), None, None),
      "facebook/events" -> PropertyQuery(
        List(
          EndpointQuery("facebook/events", None, dateTimeFilter.map(f => Seq(EndpointQueryFilter("start_time", None, f))), None)),
        Some("start_time"), None, None),
      "calendar" -> PropertyQuery(
        List(
          EndpointQuery("calendar/google/events", None, Some(Seq(
            dateFilter.map(f => EndpointQueryFilter("start.date", None, f))).flatten), None),
          EndpointQuery("calendar/google/events", None, Some(Seq(
            dateTimeFilter.map(f => EndpointQueryFilter("start.dateTime", None, f))).flatten), None)),
        Some("created"), None, None),
      "fitbit/sleep" -> PropertyQuery(
        List(
          EndpointQuery("fitbit/sleep", None, dateTimeFilter.map(f => Seq(EndpointQueryFilter("endTime", None, f))), None)),
        Some("endTime"), None, None),
      "fitbit/weight" -> PropertyQuery(
        List(
          EndpointQuery("fitbit/weight", None, dateFilter.map(f => Seq(EndpointQueryFilter("date", None, f))), None)),
        Some("date"), None, None),
      "fitbit/activity" -> PropertyQuery(
        List(
          EndpointQuery("fitbit/activity", None, dateTimeFilter.map(f => Seq(EndpointQueryFilter("originalStartTime", None, f))), None)),
        Some("originalStartTime"), None, None),
      "fitbit/activity/day/summary" -> PropertyQuery(
        List(
          EndpointQuery("fitbit/activity/day/summary", None, dateFilter.map(f => Seq(EndpointQueryFilter("dateCreated", None, f))), None)),
        Some("dateCreated"), None, None)))
  }

  private val logger: Logger = Logger(this.getClass)

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
