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
 * 5 / 2018
 */

package org.hatdex.hat.she.functions

import org.hatdex.hat.api.json.DataFeedItemJsonProtocol
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.models.applications.{ DataFeedItem, DataFeedItemContent, DataFeedItemTitle, DataFeedNestedStructureItem }
import org.hatdex.hat.she.models._
import org.hatdex.hat.she.service._
import org.joda.time.{ DateTime, Period }
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.{ ExecutionContext, Future }

class DataFeedCounter extends FunctionExecutable with DataFeedItemJsonProtocol with JodaWrites with JodaReads {
  val namespace = "she"
  val endpoint = "insights/activity-records"
  private val logger: Logger = Logger(this.getClass)

  private val dataMappers: Map[String, DataEndpointMapper] = Map(
    "facebook/feed" → new FacebookFeedMapper(),
    "twitter/tweets" → new TwitterFeedMapper(),
    "fitbit/sleep" → new FitbitSleepMapper(),
    "fitbit/activity" → new FitbitActivityMapper(),
    "fitbit/weight" → new FitbitWeightMapper(),
    "calendar/google/events" → new GoogleCalendarMapper(),
    "notables/feed" → new NotablesFeedMapper(),
    "spotify/feed" → new SpotifyFeedMapper(),
    "monzo/transactions" → new MonzoTransactionMapper())

  override def bundleFilterByDate(fromDate: Option[DateTime], untilDate: Option[DateTime]): EndpointDataBundle = {
    EndpointDataBundle("data-feed-counter", dataMappers.map {
      case (name, mapper) ⇒
        name → mapper.dataQueries(fromDate, untilDate)
          .reduce({ (query, secondary) ⇒ query.copy(endpoints = query.endpoints ++ secondary.endpoints) })
    })
  }

  val configuration: FunctionConfiguration = FunctionConfiguration(
    "data-feed-counter",
    "Weekly summary",
    """Weekly Summary show your weekly online activities.
      |It allows you to to have an overview of your data accumulated in a week. The first weekly summary establish the start date of the tool and is a summary of your history of activities""".stripMargin,
    "A summary of your week’s digital activities",
    Some(FunctionGraphics(Drawable(None, "https://github.com/Hub-of-all-Things/exchange-assets/blob/master/insights-activity-summary/logo.png?raw=true", None, None), Seq(Drawable(None, "https://github.com/Hub-of-all-Things/exchange-assets/blob/master/insights-activity-summary/screenshot1.jpg?raw=true", None, None), Drawable(None, "https://github.com/Hub-of-all-Things/exchange-assets/blob/master/insights-activity-summary/screenshot2.jpg?raw=true", None, None)))),
    FunctionTrigger.TriggerPeriodic(Period.parse("P1W")),
    available = true,
    enabled = false,
    dataBundle = bundleFilterByDate(None, None),
    None,
    Some(Seq(
      DataFeedItem("she", DateTime.now(), Seq("note"),
        Some(DataFeedItemTitle("HAT Private Micro-server created", Some("21 June 23:00 - 29 June 06:42 GMT"), Some("insight"))),
        Some(DataFeedItemContent(Some("Twitter:\n  Tweets sent: 1\n\nFacebook:\n  Posts composed: 13\n"), None, None, Some(Map("twitter" -> Seq(DataFeedNestedStructureItem("Tweets sent", Some("1"), None), DataFeedNestedStructureItem("Posts composed", Some("13"), None)))))),
        None),
      DataFeedItem("she", DateTime.now(), Seq("note"),
        Some(DataFeedItemTitle("HAT Private Micro-server created", Some("21 June 23:00 - 29 June 06:42 GMT"), Some("insight"))),
        Some(DataFeedItemContent(Some("Twitter:\n  Tweets sent: 1\n\nFacebook:\n  Posts composed: 13\n"), None, None, Some(Map("twitter" -> Seq(DataFeedNestedStructureItem("Tweets sent", Some("4"), None), DataFeedNestedStructureItem("Posts composed", Some("2"), None), DataFeedNestedStructureItem("Notes taken", Some("4"), None)))))),
        None))),
    Some(s"$namespace/$endpoint"))

  def execute(configuration: FunctionConfiguration, request: Request)(implicit ec: ExecutionContext): Future[Seq[Response]] = {
    val counters = request.data
      .collect {
        case (mappingEndpoint, records) ⇒ (mappingEndpoint, records.length)
      }

    logger.info(s"Recorded records since ${configuration.lastExecution}: ${counters}")

    val data = Json.obj(
      "timestamp" → Json.toJson(DateTime.now()),
      "since" → Json.toJson(configuration.lastExecution),
      "counters" → Json.toJson(counters))

    val response = Response(namespace, endpoint, Seq(data), Seq())

    Future.successful(Seq(response))
  }

}
