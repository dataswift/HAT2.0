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
 * 4 / 2018
 */

package org.hatdex.hat.she.service

import javax.inject.Inject

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream._
import akka.stream.scaladsl.{ GraphDSL, Sink, Source }
import akka.stream.stage.{ GraphStage, GraphStageLogic }
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.models.applications.{ DataFeedItem, DataFeedItemLocation, LocationGeo }
import org.hatdex.hat.api.service.richData.RichDataService
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.hat.utils.SourceMergeSorter
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.JsNumber

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Success, Try }

class FeedGeneratorService @Inject() ()(
    implicit
    richDataService: RichDataService,
    val actorSystem: ActorSystem,
    val ec: ExecutionContext) {

  private val logger = Logger(this.getClass)
  private val streamLogger = Logging(actorSystem, this.getClass)
  protected implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val dataMappers: Seq[(String, DataEndpointMapper)] = Seq(
    "facebook/feed" → new FacebookFeedMapper(),
    "facebook/events" → new FacebookEventMapper(),
    "twitter/tweets" → new TwitterFeedMapper(),
    "fitbit/sleep" → new FitbitSleepMapper(),
    "fitbit/weight" → new FitbitWeightMapper(),
    "fitbit/activity" → new FitbitActivityMapper(),
    "fitbit/activity/day/summary" → new FitbitActivityDaySummaryMapper(),
    "calendar/google/events" → new GoogleCalendarMapper(),
    "notables/feed" → new NotablesFeedMapper(),
    "spotify/feed" → new SpotifyFeedMapper())

  def getFeed(endpoint: String, since: Option[Long], until: Option[Long])(implicit hatServer: HatServer): Future[Seq[DataFeedItem]] =
    feedForMappers(dataMappers.filter(_._1.startsWith(endpoint)), since, until)

  def fullFeed(since: Option[Long], until: Option[Long])(implicit hatServer: HatServer): Future[Seq[DataFeedItem]] =
    feedForMappers(dataMappers, since, until)

  private val defaultTimeBack = 360.days
  private val defaultTimeForward = 90.days
  protected def feedForMappers(mappers: Seq[(String, DataEndpointMapper)], since: Option[Long], until: Option[Long])(
    implicit
    hatServer: HatServer): Future[Seq[DataFeedItem]] = {
    logger.debug(s"Fetching feed data for ${mappers.unzip._1}")
    val now = DateTime.now()
    val sinceTime = since.map(t ⇒ new DateTime(t * 1000L)).getOrElse(now.minus(defaultTimeBack.toMillis))
    val untilTime = until.map(t ⇒ new DateTime(t * 1000L)).getOrElse(now.plus(defaultTimeForward.toMillis))
    val sources: Seq[Source[DataFeedItem, NotUsed]] = mappers
      .unzip._2
      .map(_.feed(Some(sinceTime), Some(untilTime)))

    val locations = locationStream(sinceTime.getMillis / 1000L, untilTime.getMillis / 1000L)
      .log("locations")(streamLogger)
      .map(l ⇒ { logger.debug(s"Got location $l"); l })

    val sortedSources = new SourceMergeSorter()
      .mergeWithSorter(sources)
      //      .log("org.hatdex.hat.she.service.FeedGeneratorService")(streamLogger)
      .map(l ⇒ { logger.debug(s"Got feed item $l"); l })

    combineFeedLocations(new MergeLocations, sortedSources, locations.sliding(2, 1)) { (_, _) => NotUsed }
      //    sortedSources
      .runWith(Sink.seq)
  }

  def locationStream(since: Long, until: Long)(implicit hatServer: HatServer): Source[(DateTime, LocationGeo), NotUsed] = {
    val endpoint = "rumpel/locations/ios"
    val endpointQuery = EndpointQuery(endpoint, None, Some(Seq(EndpointQueryFilter("dateCreated", None, FilterOperator.Between(JsNumber(since), JsNumber(until))))), None)
    val query = PropertyQuery(List(endpointQuery), Some("dateCreated"), Some("descending"), None)

    val eventualFeed: Future[Seq[EndpointData]] = richDataService.propertyData(query.endpoints, query.orderBy,
      orderingDescending = query.ordering.contains("descending"), skip = 0, limit = None, createdAfter = None)(hatServer.db)
    val dataSource = Source.fromFuture(eventualFeed)
      .mapConcat(f ⇒ f.toList)

    //    import play.api.libs.json.JodaReads._

    dataSource.map(d ⇒ Try(new DateTime((d.data \ "dateCreated").as[Long] * 1000L) → LocationGeo((d.data \ "longitude").as[Double], (d.data \ "latitude").as[Double])))
      .collect({
        case Success(x) ⇒ x
      })
  }

  protected implicit def dataFeedItemOrdering: Ordering[DataFeedItem] = Ordering.fromLessThan(_.date isAfter _.date)

  def combineFeedLocations[MatIn0, MatIn1, Mat](
    combinator: GraphStage[FanInShape2[DataFeedItem, Seq[(DateTime, LocationGeo)], DataFeedItem]],
    s0: Source[DataFeedItem, MatIn0],
    s1: Source[Seq[(DateTime, LocationGeo)], MatIn1])(combineMat: (MatIn0, MatIn1) => Mat): Source[DataFeedItem, Mat] =

    Source.fromGraph(GraphDSL.create(s0, s1)(combineMat) { implicit builder => (s0, s1) =>
      import GraphDSL.Implicits._
      val merge = builder.add(combinator)
      s0 ~> merge.in0
      s1 ~> merge.in1
      SourceShape(merge.out)
    })

  final class MergeLocations extends GraphStage[FanInShape2[DataFeedItem, Seq[(DateTime, LocationGeo)], DataFeedItem]] {
    private val left = Inlet[DataFeedItem]("left")
    private val right = Inlet[Seq[(DateTime, LocationGeo)]]("right")
    private val out = Outlet[DataFeedItem]("out")

    override val shape = new FanInShape2(left, right, out)

    override def createLogic(attr: Attributes) = new GraphStageLogic(shape) {
      setHandler(left, eagerTerminateInput)
      setHandler(right, ignoreTerminateInput)
      setHandler(out, eagerTerminateOutput)

      var feedItem: DataFeedItem = _
      var timedLocation: Seq[(DateTime, LocationGeo)] = _

      def dispatch(l: DataFeedItem, r: Seq[(DateTime, LocationGeo)]): Unit = {
        logger.debug(s"Dispatching event for $l with $r")
        if (l.location.isDefined) {
          logger.debug("Location defined, skip")
          timedLocation = r; emit(out, l, readL)
        }
        else if (l.date.isBefore(r(0)._1) && l.date.isAfter(r(1)._1)) {
          logger.debug(s"Location close, process")
          timedLocation = r; emit(out, l.copy(location = Some(DataFeedItemLocation(Some(r.head._2), None, None))), readL)
        }
        else if (l.date.isAfter(r(0)._1)) {
          logger.debug(s"Item newer than location, process")
          timedLocation = r; emit(out, l.copy(location = Some(DataFeedItemLocation(Some(r.head._2), None, None))), readL)
        }
        else {
          logger.debug(s"Item older than location, find new location")
          feedItem = l; readR()
        }
      }

      val dispatchR = { r: Seq[(DateTime, LocationGeo)] ⇒
        logger.debug(s"dispatchR ${r}")
        dispatch(feedItem, r: Seq[(DateTime, LocationGeo)])
      }
      val dispatchL = { l: DataFeedItem ⇒
        logger.debug(s"dispatchL ${l}")
        dispatch(l: DataFeedItem, timedLocation)
      }
      val passL = () ⇒ emit(out, feedItem, () ⇒ { logger.debug("Passing along L"); passAlong(left, out, doPull = true) })
      val readR = () ⇒ read(right)(dispatchR, passL)
      val readL = () ⇒ read(left)(dispatchL, readR)

      override def preStart(): Unit = {
        // all fan-in stages need to eagerly pull all inputs to get cycles started
        pull(right)
        read(left)(l ⇒ {
          logger.debug(s"Prestarting with $l, read R")
          feedItem = l
          readR()
        }, () ⇒ {
          logger.debug(s"Abort reading R")
          abortReading(right)
        })
      }
    }
  }
}
