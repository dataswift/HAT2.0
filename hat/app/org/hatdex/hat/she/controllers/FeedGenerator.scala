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

package org.hatdex.hat.she.controllers

import javax.inject.Inject

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.Clock
import org.hatdex.hat.api.json.DataFeedItemJsonProtocol
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.models.applications.DataFeedItem
import org.hatdex.hat.api.service.richData.RichDataService
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.resourceManagement._
import org.hatdex.hat.she.models.FunctionConfigurationJsonProtocol
import org.hatdex.hat.she.service._
import org.hatdex.hat.utils.{ HatBodyParsers, SourceMergeSorter }
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext

class FeedGenerator @Inject() (
    components: ControllerComponents,
    configuration: Configuration,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    clock: Clock,
    hatServerProvider: HatServerProvider,
    functionService: FunctionService,
    richDataService: RichDataService,
    functionExecutionDispatcher: FunctionExecutionDispatcher)(
    implicit
    val actorSystem: ActorSystem,
    val ec: ExecutionContext)
  extends HatApiController(components, silhouette, clock, hatServerProvider, configuration)
  with RichDataJsonFormats
  with FunctionConfigurationJsonProtocol
  with DataFeedItemJsonProtocol {

  private val logger = Logger(this.getClass)
  protected implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val dataMappers: Map[String, DataEndpointMapper] = Map(
    "facebook/feed" → new FacebookFeedMapper(richDataService),
    "twitter/tweets" → new TwitterFeedMapper(richDataService),
    "fitbit/sleep" → new FitbitSleepMapper(richDataService),
    "fitbit/weight" → new FitbitWeightMapper(richDataService),
    "fitbit/activity" → new FitbitActivityMapper(richDataService),
    "fitbit/activity/day/summary" → new FitbitActivityDaySummaryMapper(richDataService),
    "calendar/google/events" → new GoogleCalendarMapper(richDataService),
    "notables/feed" → new NotablesFeedMapper(richDataService))

  def getFeed(endpoint: String, since: Option[Long], until: Option[Long]): Action[AnyContent] = SecuredAction(WithRole(Owner())).async { implicit request =>
    val data: Source[DataFeedItem, NotUsed] = dataMappers.get(endpoint)
      .map {
        _.feed(
          since.map(t ⇒ new DateTime(t * 1000L)).orElse(Some(DateTime.now().minusMonths(6))),
          until.map(t ⇒ new DateTime(t * 1000L)).orElse(Some(DateTime.now().plusMonths(3))))
      } getOrElse {
        logger.debug(s"No mapper for $endpoint")
        Source.empty[DataFeedItem]
      }

    data.runWith(Sink.seq)
      .map { items ⇒
        Ok(Json.toJson(items))
      }

  }

  def fullFeed(since: Option[Long], until: Option[Long]): Action[AnyContent] = SecuredAction(WithRole(Owner())).async { implicit request =>
    logger.debug(s"Mapping all known endpoints' data to feed")
    val sources = dataMappers.values.map {
      _.feed(
        since.map(t ⇒ new DateTime(t * 1000L)).orElse(Some(DateTime.now().minusMonths(6))),
        until.map(t ⇒ new DateTime(t * 1000L)).orElse(Some(DateTime.now().plusMonths(3))))
    }
    val sorter = new SourceMergeSorter()
    val data: Source[DataFeedItem, NotUsed] = sorter.mergeWithSorter(sources.toSeq)

    data.runWith(Sink.seq)
      .map { items ⇒
        Ok(Json.toJson(items))
      }

  }

  protected implicit def dataFeedItemOrdering: Ordering[DataFeedItem] = Ordering.fromLessThan(_.date isAfter _.date)

}
