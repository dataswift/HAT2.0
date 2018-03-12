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

import java.util.UUID

import org.hatdex.hat.api.json.DataFeedItemJsonProtocol
import org.hatdex.hat.api.models.{ applications, _ }
import org.hatdex.hat.api.models.applications._
import org.hatdex.hat.she.models._
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

class DataFeedDirectMapper extends FunctionExecutable with DataFeedItemJsonProtocol with JodaWrites with JodaReads {
  val namespace = "she"
  val endpoint = "feed"

  val configuration: FunctionConfiguration = FunctionConfiguration("data-feed-direct-mapper", "",
    FunctionTrigger.TriggerIndividual(), available = true, enabled = false,
    dataBundle = bundleFilterByDate(None, None),
    None)

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
    val responseData = request.data collect {
      case (n, data) if n == "twitter" =>
        val records = data collect {
          case EndpointData("twitter/tweets", Some(recordId), content, _) =>
            (recordId, mapTweet(recordId, content))
          case EndpointData(e, Some(recordId), _, _) =>
            (recordId, Failure(new RuntimeException(s"Unexpected endpoint data $e within $n")))
        }
        ("twitter", records)
      case (n, data) if n == "facebook/feed" =>
        val records = data collect {
          case EndpointData("facebook/feed", Some(recordId), content, _) =>
            (recordId, mapFacebookPost(recordId, content))
          case EndpointData(e, Some(recordId), _, _) =>
            (recordId, Failure(new RuntimeException(s"Unexpected endpoint data $e within $n")))
        }
        ("facebook/feed", records)
      case (n, data) if n == "facebook/events" =>
        val records = data collect {
          case EndpointData("facebook/events", Some(recordId), content, _) =>
            (recordId, mapFacebookEvent(recordId, content))
          case EndpointData(e, Some(recordId), _, _) =>
            (recordId, Failure(new RuntimeException(s"Unexpected endpoint data $e within $n")))
        }
        ("facebook/events", records)
      case (n, data) if n == "fitbit/sleep" =>
        val records = data collect {
          case EndpointData("fitbit/sleep", Some(recordId), content, _) =>
            (recordId, mapFitbitSleep(recordId, content))
          case EndpointData(e, Some(recordId), _, _) =>
            (recordId, Failure(new RuntimeException(s"Unexpected endpoint data $e within $n")))
        }
        ("fitbit/sleep", records)
      case (n, data) if n == "fitbit/weight" =>
        val records = data collect {
          case EndpointData("fitbit/weight", Some(recordId), content, _) =>
            (recordId, mapFitbitWeight(recordId, content))
          case EndpointData(e, Some(recordId), _, _) =>
            (recordId, Failure(new RuntimeException(s"Unexpected endpoint data $e within $n")))
        }
        ("fitbit/weight", records)
      case (n, data) if n == "fitbit/activity" =>
        val records = data collect {
          case EndpointData("fitbit/activity", Some(recordId), content, _) =>
            (recordId, mapFitbitActivity(recordId, content))
          case EndpointData(e, Some(recordId), _, _) =>
            (recordId, Failure(new RuntimeException(s"Unexpected endpoint data $e within $n")))
        }
        ("fitbit/activity", records)
      case (n, data) if n == "fitbit/activity/day/summary" =>
        val records = data collect {
          case EndpointData("fitbit/activity/day/summary", Some(recordId), content, _) =>
            (recordId, mapFitbitDaySummarySteps(recordId, content))
          case EndpointData(e, Some(recordId), _, _) =>
            (recordId, Failure(new RuntimeException(s"Unexpected endpoint data $e within $n")))
        }
        ("fitbit/activity/day/summary", records)
      case (n, data) if n == "calendar" =>
        val records = data collect {
          case EndpointData("calendar/google/events", Some(recordId), content, _) =>
            (recordId, mapGoogleCalendarEvent(recordId, content))
          case EndpointData(e, Some(recordId), _, _) =>
            (recordId, Failure(new RuntimeException(s"Unexpected endpoint data $e within $n")))
        }
        ("calendar/google/events", records)
    }

    val response = responseData
      .flatMap { d =>
        logger.info(s"[${d._1}] Computed ${d._2.count(r => r._2.isSuccess)} out of ${d._2.length} records")
        d._2.collect {
          case (_, Failure(e)) => logger.warn(s"[${d._1}] Error while transforming data: ${e.getMessage}")
        }
        d._2.collect {
          case (recordId, Success(data)) => Response(namespace, endpoint, Seq(Json.toJson(data)), Seq(recordId))
        }
      }

    Future.successful(response.toSeq)
  }

  protected def mapTweet(recordId: UUID, content: JsValue): Try[DataFeedItem] = {
    for {
      title <- Try(if ((content \ "retweeted").as[Boolean]) {
        DataFeedItemTitle("You retweeted", Some("repeat"))
      }
      else if ((content \ "in_reply_to_user_id").isDefined && !((content \ "in_reply_to_user_id").get == JsNull)) {
        DataFeedItemTitle(s"You replied to @${(content \ "in_reply_to_screen_name").as[String]}", None)
      }
      else {
        DataFeedItemTitle("You tweeted", None)
      })
      itemContent <- Try(DataFeedItemContent((content \ "text").asOpt[String], None))
      date <- Try((content \ "lastUpdated").as[DateTime])
    } yield {
      val location = Try(DataFeedItemLocation(
        geo = (content \ "coordinates").asOpt[JsObject]
          .map(coordinates =>
            LocationGeo(
              (coordinates \ "coordinates" \ 0).as[Double],
              (coordinates \ "coordinates" \ 1).as[Double])),
        address = (content \ "place").asOpt[JsObject]
          .map(address =>
            applications.LocationAddress(
              (address \ "country").asOpt[String],
              (address \ "name").asOpt[String],
              None, None, None)),
        tags = None))
        .toOption
        .filter(l => l.address.isDefined || l.geo.isDefined || l.tags.isDefined)

      DataFeedItem("twitter", date, Seq("post"), Some(title), Some(itemContent), location)
    }
  }

  protected def mapFacebookPost(recordId: UUID, content: JsValue): Try[DataFeedItem] = {
    for {
      title <- Try(if ((content \ "type").as[String] == "photo") {
        DataFeedItemTitle("You posted a photo", Some("photo"))
      }
      else if ((content \ "type").as[String] == "link") {
        DataFeedItemTitle("You shared a story", None)
      }
      else {
        DataFeedItemTitle("You posted", None)
      })
      itemContent <- Try(DataFeedItemContent(
        Some(
          s"""${(content \ "message").as[String]}
             |
             |${(content \ "link").asOpt[String].getOrElse("")}""".stripMargin),
        (content \ "picture").asOpt[String].map(url => List(DataFeedItemMedia(Some(url))))))
      date <- Try((content \ "created_time").as[DateTime])
      tags <- Try(Seq("post", (content \ "type").as[String]))
    } yield DataFeedItem("facebook", date, tags, Some(title), Some(itemContent), None)
  }

  protected def mapFacebookEvent(recordId: UUID, content: JsValue): Try[DataFeedItem] = {
    for {
      timeIntervalString <- Try(eventTimeIntervalString(
        (content \ "start_time").as[DateTime],
        Some((content \ "end_time").as[DateTime])))

      itemContent <- Try(DataFeedItemContent(
        Some(
          s"""## ${(content \ "name").as[String]}
         |
           |${timeIntervalString._1} ${timeIntervalString._2.getOrElse("")}
         |
           |${(content \ "description").as[String]}
         |""".stripMargin), None))
      title <- Try(if ((content \ "rsvp_status").as[String] == "attending") {
        DataFeedItemTitle("You are attending an event", Some("event"))
      }
      else {
        DataFeedItemTitle("You have an event", Some("event"))
      })
    } yield {
      val location = Try(DataFeedItemLocation(
        geo = (content \ "place").asOpt[JsObject]
          .map(location =>
            LocationGeo(
              (location \ "location" \ "longitude").as[String].toDouble,
              (location \ "location" \ "latitude").as[String].toDouble)),
        address = (content \ "place").asOpt[JsObject]
          .map(location =>
            LocationAddress(
              (location \ "location" \ "country").asOpt[String],
              (location \ "location" \ "city").asOpt[String],
              (location \ "name").asOpt[String],
              (location \ "location" \ "street").asOpt[String],
              (location \ "location" \ "zip").asOpt[String])),
        tags = None))
        .toOption
        .filter(l => l.address.isDefined || l.geo.isDefined || l.tags.isDefined)

      DataFeedItem("facebook", (content \ "start_time").as[DateTime], Seq("event"),
        Some(title), Some(itemContent), location)
    }
  }

  protected def mapFitbitWeight(recordId: UUID, content: JsValue): Try[DataFeedItem] = {
    val title = DataFeedItemTitle("You added a new weight measurement", Some("weight"))

    val itemContent = DataFeedItemContent(
      Some(Seq(
        (content \ "weight").asOpt[Double].map(w => s"- Weight: $w"),
        (content \ "fat").asOpt[Double].map(w => s"- Body Fat: $w"),
        (content \ "bmi").asOpt[Double].map(w => s"- BMI: $w")).flatten.mkString("\n")),
      None)

    for {
      date <- Try(JsString(s"${(content \ "date").as[String]}T${(content \ "time").as[String]}").as[DateTime])
    } yield DataFeedItem("fitbit", date, Seq("fitness", "weight"), Some(title), Some(itemContent), None)
  }

  protected def mapFitbitSleep(recordId: UUID, content: JsValue): Try[DataFeedItem] = {
    val title = DataFeedItemTitle("You woke up!", Some("sleep"))

    val timeInBed = (content \ "timeInBed").asOpt[Int]
      .map(t => s"You spent ${t / 60} hours and ${t % 60} minutes in bed.")
    val minutesAsleep = (content \ "minutesAsleep").asOpt[Int]
      .map(asleep => s"You slept for ${asleep / 60} hours and ${asleep % 60} minutes" +
        (content \ "minutesAwake").asOpt[Int].map(t => s" and were awake for $t minutes").getOrElse("") +
        ".")
    val efficiency = (content \ "efficiency").asOpt[Int]
      .map(e => s"Your sleep efficiency score tonight was $e.")

    val itemContent = DataFeedItemContent(
      Some(Seq(timeInBed, minutesAsleep, efficiency).flatten.mkString(" ")),
      None)

    for {
      date <- Try((content \ "endTime").as[DateTime])
    } yield DataFeedItem("fitbit", date, Seq("fitness", "sleep"), Some(title), Some(itemContent), None)
  }

  protected def mapFitbitActivity(recordId: UUID, content: JsValue): Try[DataFeedItem] = {
    for {
      date <- Try((content \ "originalStartTime").as[DateTime])
    } yield {
      val title = DataFeedItemTitle("You logged Fitbit activity", Some("fitness"))

      val message = Seq(
        (content \ "activityName").asOpt[String].map(c => s"- Activity: $c"),
        (content \ "duration").asOpt[Long].map(c => s"- Duration: ${c / 1000 / 60} minutes"),
        (content \ "averageHeartRate").asOpt[Long].map(c => s"- Average heart rate: $c"),
        (content \ "calories").asOpt[Long].map(c => s"- Calories burned: $c")).flatten.mkString("\n")

      DataFeedItem(
        "fitbit", date, Seq("fitness", "activity"),
        Some(title), Some(DataFeedItemContent(Some(message), None)), None)
    }
  }

  protected def mapFitbitDaySummarySteps(recordId: UUID, content: JsValue): Try[DataFeedItem] = {
    val fitbitSummary = for {
      count <- Try((content \ "steps").as[Int]) if count > 0
      date <- Try((content \ "dateCreated").as[DateTime])
    } yield {
      val adjustedDate = if (date.getSecondOfDay == 0) {
        date.secondOfDay().withMaximumValue()
      }
      else {
        date
      }
      val title = DataFeedItemTitle(s"You walked $count steps", Some("fitness"))
      DataFeedItem("fitbit", adjustedDate, Seq("fitness", "activity"),
        Some(title), None, None)
    }

    fitbitSummary recoverWith {
      case _: NoSuchElementException => Failure(new RuntimeException("Fitbit empty day summary"))
    }
  }

  protected def mapGoogleCalendarEvent(recordId: UUID, content: JsValue): Try[DataFeedItem] = {
    for {
      startDate <- Try((content \ "start" \ "dateTime").asOpt[DateTime]
        .getOrElse((content \ "start" \ "date").as[DateTime])
        .withZone((content \ "start" \ "timeZone").asOpt[String].flatMap(z => Try(DateTimeZone.forID(z)).toOption).getOrElse(DateTimeZone.getDefault)))
      endDate <- Try((content \ "end" \ "dateTime").asOpt[DateTime]
        .getOrElse((content \ "end" \ "date").as[DateTime])
        .withZone((content \ "end" \ "timeZone").asOpt[String].flatMap(z => Try(DateTimeZone.forID(z)).toOption).getOrElse(DateTimeZone.getDefault)))
      timeIntervalString <- Try(eventTimeIntervalString(startDate, Some(endDate)))
      itemContent <- Try(DataFeedItemContent(
        Some(Seq(
          Some(s"## ${(content \ "summary").as[String]}"),
          Some(s"${timeIntervalString._1} ${timeIntervalString._2.getOrElse("")}"),
          (content \ "location").asOpt[String].map(c => s"@$c"),
          (content \ "description").asOpt[String]).flatten.mkString("\n\n")),
        None))
      location <- Try(DataFeedItemLocation(
        geo = None,
        address = None, // TODO integrate with geocoding API for full location information?
        tags = None))
    } yield {
      val title = DataFeedItemTitle("You have an event", Some("event"))
      val loc = Some(location).filter(l => l.address.isDefined || l.geo.isDefined || l.tags.isDefined)
      DataFeedItem("google", startDate, Seq("event"),
        Some(title), Some(itemContent), loc)
    }
  }

  private def eventTimeIntervalString(start: DateTime, end: Option[DateTime]): (String, Option[String]) = {
    val startString = if (start.getMillisOfDay == 0) {
      s"${start.toString("dd MMMM")}"
    }
    else {
      s"${start.withZone(start.getZone).toString("dd MMMM HH:mm")}"
    }

    val endString = end.map {
      case t if t.isAfter(start.withTimeAtStartOfDay().plusDays(1)) =>
        if (t.hourOfDay().get() == 0) {
          s"- ${t.minusMinutes(1).toString("dd MMMM")}"
        }
        else {
          s"- ${t.withZone(start.getZone).toString("dd MMMM HH:mm z")}"
        }
      case t if t.hourOfDay().get() == 0 => ""
      case t                             => s"- ${t.withZone(start.getZone).toString("HH:mm z")}"
    }

    (startString, endString)
  }
}
