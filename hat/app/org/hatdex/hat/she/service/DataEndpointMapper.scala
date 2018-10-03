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
 * 3 / 2018
 */

package org.hatdex.hat.she.service

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.Source
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.models.applications._
import org.hatdex.hat.api.service.richData.RichDataService
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.hat.utils.SourceMergeSorter
import org.joda.time.format.{ DateTimeFormat, DateTimeFormatter, ISODateTimeFormat }
import org.joda.time.{ DateTime, DateTimeZone }
import play.api.Logger
import play.api.libs.json._

import scala.util.{ Failure, Success, Try }

trait DataEndpointMapper extends JodaWrites with JodaReads {
  protected lazy val logger: Logger = Logger(this.getClass)
  protected val dateTimeFormat: DateTimeFormatter = ISODateTimeFormat.dateTime()
  protected val dataDeduplicationField: Option[String] = None

  protected def dateFilter(fromDate: Option[DateTime], untilDate: Option[DateTime]): Option[FilterOperator.Operator] = {
    if (fromDate.isDefined) {
      Some(FilterOperator.Between(Json.toJson(fromDate.map(_.toString(dateTimeFormat))), Json.toJson(untilDate.map(_.toString(dateTimeFormat)))))
    }
    else {
      None
    }
  }

  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery]
  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem]

  private implicit def dataFeedItemOrdering: Ordering[DataFeedItem] = Ordering.fromLessThan(_.date isAfter _.date)

  final def feed(fromDate: Option[DateTime], untilDate: Option[DateTime])(
    implicit
    hatServer: HatServer, richDataService: RichDataService): Source[DataFeedItem, NotUsed] = {

    val feeds = dataQueries(fromDate, untilDate).map({ query ⇒
      val dataSource: Source[EndpointData, NotUsed] = richDataService.propertyDataStreaming(query.endpoints, query.orderBy,
        orderingDescending = query.ordering.contains("descending"), skip = 0, limit = None, createdAfter = None)(hatServer.db)

      val deduplicated = dataDeduplicationField.map { field ⇒
        dataSource.sliding(2, 1)
          .collect({
            case Seq(a, b) if a.data \ field != b.data \ field ⇒ b
            case Seq(a)                                        ⇒ a // only a single element, e.g. last element in sliding window
          })
      } getOrElse {
        dataSource
      }

      deduplicated
        .sliding(2, 1)
        .map {
          case Seq(head, tail) ⇒ mapDataRecord(head.recordId.get, head.data, tail.recordId, Some(tail.data))
          case Seq(item)       ⇒ mapDataRecord(item.recordId.get, item.data)
        }
        .collect({
          case Success(x) ⇒ x
        })
    })
    new SourceMergeSorter().mergeWithSorter(feeds)
  }

  protected def eventTimeIntervalString(start: DateTime, end: Option[DateTime]): (String, Option[String]) = {
    val startString = if (start.getMillisOfDay == 0) {
      s"${start.toString("dd MMMM")}"
    }
    else {
      s"${start.withZone(start.getZone).toString("dd MMMM HH:mm")}"
    }

    val endString = end.map {
      case t if t.isAfter(start.withTimeAtStartOfDay().plusDays(1)) ⇒
        if (t.hourOfDay().get() == 0) {
          s"- ${t.minusMinutes(1).toString("dd MMMM")}"
        }
        else {
          s"- ${t.withZone(start.getZone).toString("dd MMMM HH:mm z")}"
        }
      case t if t.hourOfDay().get() == 0 ⇒ ""
      case t                             ⇒ s"- ${t.withZone(start.getZone).toString("HH:mm z")}"
    }

    (startString, endString)
  }

  protected def eventTimeIntervalString(start: Option[DateTime], end: Option[DateTime]): (Option[String], Option[String]) = {
    val startString = start.map {
      case t if t.getMillisOfDay == 0 ⇒ s"${t.toString("dd MMMM")}"
      case t                          ⇒ s"${t.withZone(t.getZone).toString("dd MMMM HH:mm")}"
    }

    val endString = (start, end) match {
      case (None, Some(t)) ⇒
        if (t.hourOfDay().get() == 0) {
          Some(s"before ${t.minusMinutes(1).toString("dd MMMM")}")
        }
        else {
          Some(s"before ${t.toString("dd MMMM HH:mm z")}")
        }
      case (Some(s), Some(t)) if t.isAfter(s.withTimeAtStartOfDay().plusDays(1)) ⇒
        if (t.hourOfDay().get() == 0) {
          Some(s"- ${t.minusMinutes(1).toString("dd MMMM")}")
        }
        else {
          Some(s"- ${t.withZone(s.getZone).toString("dd MMMM HH:mm z")}")
        }
      case (_, Some(t)) if t.hourOfDay().get() == 0 ⇒ None
      case (Some(s), Some(t))                       ⇒ Some(s"- ${t.withZone(s.getZone).toString("HH:mm z")}")
      case (_, None)                                ⇒ None
    }

    (startString, endString)
  }
}

trait FeedItemComparator {
  def compareInt(content: JsValue, tailContent: JsValue, dataKey: String, humanKey: String): (Boolean, String) = {
    val previousValue = (tailContent \ dataKey).asOpt[Int].getOrElse(0)
    val currentValue = (content \ dataKey).asOpt[Int].getOrElse(0)
    val contentValue = previousValue == currentValue
    val contentText = s"Your $humanKey has changed from $previousValue to $currentValue."
    (contentValue, contentText)
  }

  def compareString(content: JsValue, tailContent: JsValue, dataKey: String, humanKey: String): (Boolean, String) = {
    val previousValue = (tailContent \ dataKey).asOpt[String].getOrElse("")
    val currentValue = (content \ dataKey).asOpt[String].getOrElse("")
    val contentValue = previousValue == currentValue
    val contentText = s"Your $humanKey has changed from $previousValue to $currentValue."
    (contentValue, contentText)
  }

  def compareFloat(content: JsValue, tailContent: JsValue, dataKey: String, humanKey: String): (Boolean, String) = {
    val previousValue = (tailContent \ dataKey).asOpt[Float].getOrElse(0.0)
    val currentValue = (content \ dataKey).asOpt[Float].getOrElse(0.0)
    val contentValue = previousValue == currentValue
    val contentText = s"Your $humanKey has changed from $previousValue to $currentValue."
    (contentValue, contentText)
  }
}

class InsightSentimentMapper extends DataEndpointMapper {
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("she/insights/emotions", None, dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("timestamp", None, f))), None)),
      Some("timestamp"), Some("descending"), None))
  }

  private val textMappings = Map(
    "twitter/tweets" → "Twitter",
    "facebook/feed" → "Facebook",
    "notables/feed" → "Notables")

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    for {
      sentiment ← Try((content \ "sentiment").as[String])
      text ← Try((content \ "text").as[String])
      source ← Try((content \ "source").as[String])
      timestamp ← Try((content \ "timestamp").as[DateTime])
    } yield {
      val title = DataFeedItemTitle(
        s"$sentiment Message",
        Some(s"on ${textMappings.getOrElse(source, source)}"),
        Some("sentiment"))

      val itemContent = DataFeedItemContent(text = Some(text), html = None, media = None, nestedStructure = None)

      DataFeedItem("she", timestamp, Seq("insight", "sentiment", sentiment,
        textMappings.getOrElse(source, source)), Some(title), Some(itemContent), None)
    }
  }
}

class InsightsMapper extends DataEndpointMapper {
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("she/insights/activity-records", None, dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("timestamp", None, f))), None)),
      Some("timestamp"), Some("descending"), None))
  }

  private val textMappings = Map(
    "twitter/tweets" → "Tweets sent",
    "facebook/feed" → "Posts composed",
    "notables/feed" → "Notes taken",
    "spotify/feed" → "Songs listened to",
    "calendar/google/events" → "Calendar events recorded",
    "monzo/transactions" → "Transactions performed",
    "she/insights/emotions" -> "Posts analysed for Sentiments",
    "she/insights/emotions/positive" -> "Positive",
    "she/insights/emotions/negative" -> "Negative",
    "she/insights/emotions/neutral" -> "Neutral")

  private val sourceMappings = Map(
    "twitter/tweets" → "twitter",
    "facebook/feed" → "facebook",
    "notables/feed" → "notables",
    "spotify/feed" → "spotify",
    "calendar/google/events" → "google",
    "monzo/transactions" → "monzo",
    "she/insights/emotions" -> "sentiment",
    "she/insights/emotions/positive" -> "sentiment-positive",
    "she/insights/emotions/negative" -> "sentiment-negative",
    "she/insights/emotions/neutral" -> "sentiment-neutral")

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID], tailContent: Option[JsValue]): Try[DataFeedItem] = {
    for {
      startDate ← Try((content \ "since").asOpt[DateTime])
      endDate ← Try((content \ "timestamp").asOpt[DateTime])
      timeIntervalString ← Try(eventTimeIntervalString(startDate, endDate))
      counters ← Try((content \ "counters").as[Map[String, Int]]) if counters.exists(_._2 != 0)
    } yield {
      val title = DataFeedItemTitle(
        "Your recent activity summary",
        Some(s"${timeIntervalString._1.getOrElse("")} ${timeIntervalString._2.getOrElse("")}"),
        Some("insight"))

      val nested: Map[String, Seq[DataFeedNestedStructureItem]] = counters.foldLeft(Seq[(String, DataFeedNestedStructureItem)]())({
        (structured, newitem) ⇒
          val item = DataFeedNestedStructureItem(textMappings.getOrElse(newitem._1, "unrecognised"), Some(newitem._2.toString), None)
          val source = sourceMappings.getOrElse(newitem._1, "unrecognised")
          structured :+ (source → item)
      })
        .filterNot(_._2.content == "unrecognised")
        .filterNot(_._2.badge.contains("0"))
        .groupBy(_._1).map({
          case (k, v) ⇒ k → v.unzip._2
        })

      val simplified: String = nested.map({
        case (source, info) ⇒
          s"""${source.capitalize}:
           |  ${info.map(i ⇒ s"${i.content}: ${i.badge.getOrElse("")}").mkString("\n  ")}
           |""".stripMargin
      }).mkString("\n")

      val itemContent = DataFeedItemContent(text = Some(simplified), html = None, media = None, nestedStructure = Some(nested))

      DataFeedItem("she", endDate.getOrElse(DateTime.now()), Seq("insight", "activity"), Some(title), Some(itemContent), None)
    }
  }
}

class GoogleCalendarMapper extends DataEndpointMapper {
  override protected val dataDeduplicationField: Option[String] = Some("id")

  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    val eventDateTimePropertyQuery = PropertyQuery(
      List(
        EndpointQuery("calendar/google/events", None, Some(Seq(
          dateFilter(fromDate, untilDate).map(f ⇒ EndpointQueryFilter("start.dateTime", None, f))).flatten), None)),
      Some("start.dateTime"), Some("descending"), None)

    val dateOnlyFilter = if (fromDate.isDefined) {
      Some(FilterOperator.Between(Json.toJson(fromDate.map(_.toString("yyyy-MM-dd"))), Json.toJson(untilDate.map(_.toString("yyyy-MM-dd")))))
    }
    else {
      None
    }

    val eventDatePropertyQuery = PropertyQuery(
      List(
        EndpointQuery("calendar/google/events", None, Some(Seq(
          dateOnlyFilter.map(f ⇒ EndpointQueryFilter("start.date", None, f))).flatten), None)),
      Some("start.date"), Some("descending"), None)

    Seq(eventDateTimePropertyQuery, eventDatePropertyQuery)
  }

  def cleanHtmlTags(input: String): String = {
    input.replaceAll("<br/?>", "\n")
      .replaceAll("&nbsp;", " ")
      .replaceAll("<a [^>]*>([^<]*)</a>", "$1")
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    for {
      startDate ← Try((content \ "start" \ "dateTime").asOpt[DateTime]
        .getOrElse((content \ "start" \ "date").as[DateTime])
        .withZone((content \ "start" \ "timeZone").asOpt[String].flatMap(z ⇒ Try(DateTimeZone.forID(z)).toOption).getOrElse(DateTimeZone.getDefault)))
      endDate ← Try((content \ "end" \ "dateTime").asOpt[DateTime]
        .getOrElse((content \ "end" \ "date").as[DateTime])
        .withZone((content \ "end" \ "timeZone").asOpt[String].flatMap(z ⇒ Try(DateTimeZone.forID(z)).toOption).getOrElse(DateTimeZone.getDefault)))
      timeIntervalString ← Try(eventTimeIntervalString(startDate, Some(endDate)))
      itemContent ← Try(DataFeedItemContent(
        (content \ "description").asOpt[String].map(cleanHtmlTags), None, None, None))
      location ← Try(DataFeedItemLocation(
        geo = None,
        address = (content \ "location").asOpt[String].map(l ⇒ LocationAddress(None, None, Some(l), None, None)), // TODO integrate with geocoding API for full location information?
        tags = None))
    } yield {
      val title = DataFeedItemTitle(s"${(content \ "summary").as[String]}", Some(s"${timeIntervalString._1} ${timeIntervalString._2.getOrElse("")}"), Some("event"))
      val loc = Some(location).filter(l ⇒ l.address.isDefined || l.geo.isDefined || l.tags.isDefined)
      DataFeedItem("google", startDate, Seq("event"),
        Some(title), Some(itemContent), loc)
    }
  }
}

class FitbitWeightMapper extends DataEndpointMapper {
  override val dateTimeFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("fitbit/weight", None, dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("date", None, f))), None)),
      Some("date"), Some("descending"), None))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    val title = DataFeedItemTitle("You added a new weight measurement", None, Some("weight"))

    val itemContent = DataFeedItemContent(
      Some(Seq(
        (content \ "weight").asOpt[Double].map(w ⇒ s"- Weight: $w"),
        (content \ "fat").asOpt[Double].map(w ⇒ s"- Body Fat: $w"),
        (content \ "bmi").asOpt[Double].map(w ⇒ s"- BMI: $w")).flatten.mkString("\n")),
      None,
      None,
      None)

    for {
      date ← Try(JsString(s"${(content \ "date").as[String]}T${(content \ "time").as[String]}").as[DateTime])
    } yield DataFeedItem("fitbit", date, Seq("fitness", "weight"), Some(title), Some(itemContent), None)
  }
}

// calendar events
// facebook events

class FitbitActivityMapper extends DataEndpointMapper {
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("fitbit/activity", None, dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("originalStartTime", None, f))), None)),
      Some("originalStartTime"), Some("descending"), None))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    for {
      date ← Try((content \ "originalStartTime").as[DateTime])
    } yield {
      val title = DataFeedItemTitle("You logged Fitbit activity", None, Some("fitness"))

      val message = Seq(
        (content \ "activityName").asOpt[String].map(c ⇒ s"- Activity: $c"),
        (content \ "duration").asOpt[Long].map(c ⇒ s"- Duration: ${c / 1000 / 60} minutes"),
        (content \ "averageHeartRate").asOpt[Long].map(c ⇒ s"- Average heart rate: $c"),
        (content \ "calories").asOpt[Long].map(c ⇒ s"- Calories burned: $c")).flatten.mkString("\n")

      DataFeedItem(
        "fitbit", date, Seq("fitness", "activity"),
        Some(title), Some(DataFeedItemContent(Some(message), None, None, None)), None)
    }
  }
}

class FitbitActivityDaySummaryMapper extends DataEndpointMapper {
  override val dateTimeFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("fitbit/activity/day/summary", None, dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("dateCreated", None, f))), None)),
      Some("dateCreated"), Some("descending"), None))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    val fitbitSummary = for {
      count ← Try((content \ "steps").as[Int]) if count > 0
      date ← Try((content \ "dateCreated").as[DateTime])
    } yield {
      val adjustedDate = if (date.getSecondOfDay == 0) {
        date.secondOfDay().withMaximumValue()
      }
      else {
        date
      }
      val title = DataFeedItemTitle(s"You walked $count steps", None, Some("fitness"))
      DataFeedItem("fitbit", adjustedDate, Seq("fitness", "activity"),
        Some(title), None, None)
    }

    fitbitSummary recoverWith {
      case _: NoSuchElementException ⇒ Failure(new RuntimeException("Fitbit empty day summary"))
    }
  }
}

class FitbitSleepMapper extends DataEndpointMapper {
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("fitbit/sleep", None, dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("endTime", None, f))), None)),
      Some("endTime"), Some("descending"), None))
  }

  def fitbitDateCorrector(date: String): String = {
    val zonedDatePattern = """.*(z|Z|\+\d{2})(:?\d{2})?$""".r // does the string end with ISO8601 timezone indicator
    if (zonedDatePattern.findFirstIn(date).isDefined) {
      date
    }
    else {
      date + "+0000"
    }
  }

  override implicit val DefaultJodaDateTimeReads: Reads[DateTime] = jodaDateReads("", fitbitDateCorrector)

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    val title = DataFeedItemTitle("You woke up!", None, Some("sleep"))

    val timeInBed = (content \ "timeInBed").asOpt[Int]
      .map(t ⇒ s"You spent ${t / 60} hours and ${t % 60} minutes in bed.")
    val minutesAsleep = (content \ "minutesAsleep").asOpt[Int]
      .map(asleep ⇒ s"You slept for ${asleep / 60} hours and ${asleep % 60} minutes" +
        (content \ "minutesAwake").asOpt[Int].map(t ⇒ s" and were awake for $t minutes").getOrElse("") +
        ".")
    val efficiency = (content \ "efficiency").asOpt[Int]
      .map(e ⇒ s"Your sleep efficiency score tonight was $e.")

    val itemContent = DataFeedItemContent(
      Some(Seq(timeInBed, minutesAsleep, efficiency).flatten.mkString(" ")),
      None,
      None,
      None)

    for {
      date ← Try((content \ "endTime").as[DateTime])
    } yield {
      DataFeedItem("fitbit", date, Seq("fitness", "sleep"), Some(title), Some(itemContent), None)
    }
  }

}

class FitbitProfileMapper extends DataEndpointMapper with FeedItemComparator {
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("fitbit/profile", None, None, None)),
      Some("updated_time"), None, None))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    val comparison = compare(content, tailContent).filter(_._1 == false) // remove all fields that have the same values pre/current
    if (comparison.length == 0) {
      Failure(new RuntimeException("Comparision Failure. Data the same"))
    }
    else {
      for {
        title <- Try(DataFeedItemTitle("Your Fitbit Data has changed.", None, None))
        itemContent ← {
          val contentText = comparison.map(item => s"${item._2}\n").mkString
          Try(DataFeedItemContent(
            Some(contentText), None, None, None))
        }
      } yield {
        DataFeedItem("fitbit", (content \ "dateCreated").as[DateTime], Seq("profile"),
          Some(title), Some(itemContent), None)
      }
    }
  }

  /**
   *
   * @param content
   * @param tailContent
   * @return (true if data is the same and both content is the not None, )
   */
  def compare(content: JsValue, tailContent: Option[JsValue]): Seq[(Boolean, String)] = {
    if (tailContent.isEmpty)
      Seq()
    else {
      Seq(
        compareString(content, tailContent.get, "fullName", "Name"),
        compareString(content, tailContent.get, "displayName", "Display Name"),
        compareString(content, tailContent.get, "gender", "Gender"),
        compareInt(content, tailContent.get, "age", "Age"),
        compareInt(content, tailContent.get, "height", "Height"),
        compareFloat(content, tailContent.get, "weight", "Weight"),
        compareString(content, tailContent.get, "country", "Country"))
    }
  }
}

class TwitterFeedMapper extends DataEndpointMapper {
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("twitter/tweets", None, dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("lastUpdated", None, f))), None)),
      Some("lastUpdated"), Some("descending"), None))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    for {
      title ← Try(if ((content \ "retweeted").as[Boolean]) {
        DataFeedItemTitle("You retweeted", None, Some("repeat"))
      }
      else if ((content \ "in_reply_to_user_id").isDefined && !((content \ "in_reply_to_user_id").get == JsNull)) {
        DataFeedItemTitle(s"You replied to @${(content \ "in_reply_to_screen_name").as[String]}", None, None)
      }
      else {
        DataFeedItemTitle("You tweeted", None, None)
      })
      itemContent ← Try(DataFeedItemContent((content \ "text").asOpt[String], None, None, None))
      date ← Try((content \ "lastUpdated").as[DateTime])
    } yield {
      val location = Try(DataFeedItemLocation(
        geo = (content \ "coordinates").asOpt[JsObject]
          .map(coordinates ⇒
            LocationGeo(
              (coordinates \ "coordinates" \ 0).as[Double],
              (coordinates \ "coordinates" \ 1).as[Double])),
        address = (content \ "place").asOpt[JsObject]
          .map(address ⇒
            LocationAddress(
              (address \ "country").asOpt[String],
              (address \ "name").asOpt[String],
              None, None, None)),
        tags = None))
        .toOption
        .filter(l ⇒ l.address.isDefined || l.geo.isDefined || l.tags.isDefined)

      DataFeedItem("twitter", date, Seq("post"), Some(title), Some(itemContent), location)
    }
  }
}

class FacebookProfileMapper extends DataEndpointMapper with FeedItemComparator {
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("facebook/profile", None, None, None)),
      Some("updated_time"), None, None))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    val comparison = compare(content, tailContent).filter(_._1 == false) // remove all fields that have the same values pre/current
    if (comparison.length == 0) {
      Failure(new RuntimeException("Comparision Failure. Data the same"))
    }
    else {
      for {
        title <- Try(DataFeedItemTitle("Your Facebook Profile has changed.", None, None))
        itemContent ← {
          val contentText = comparison.map(item => s"${item._2}\n").mkString
          Try(DataFeedItemContent(
            Some(contentText), None, None, None))
        }
      } yield {
        DataFeedItem("facebook", (content \ "updated_time").as[DateTime], Seq("profile"),
          Some(title), Some(itemContent), None)
      }
    }
  }

  def compare(content: JsValue, tailContent: Option[JsValue]): Seq[(Boolean, String)] = {
    if (tailContent.isEmpty)
      Seq()
    else {
      Seq(
        compareString(content, tailContent.get, "name", "Name"),
        compareString(content, tailContent.get, "gender", "Gender"),
        compareString(content, tailContent.get, "age_range", "Age Range"),
        compareInt(content, tailContent.get, "friend_count", "Number of Friends"))
    }
  }
}

class FacebookEventMapper extends DataEndpointMapper {
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("facebook/events", None, dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("start_time", None, f))), None)),
      Some("start_time"), None, None))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    for {
      timeIntervalString ← Try(eventTimeIntervalString(
        (content \ "start_time").as[DateTime],
        Some((content \ "end_time").as[DateTime])))

      itemContent ← Try(DataFeedItemContent(
        Some((content \ "description").as[String]), None, None, None))
      title ← Try(if ((content \ "rsvp_status").as[String] == "attending") {
        DataFeedItemTitle("You are attending an event", Some(s"${timeIntervalString._1} ${timeIntervalString._2.getOrElse("")}"), Some("event"))
      }
      else {
        DataFeedItemTitle("You have an event", Some(s"${timeIntervalString._1} ${timeIntervalString._2.getOrElse("")}"), Some("event"))
      })
    } yield {
      val location = Try(DataFeedItemLocation(
        geo = (content \ "place").asOpt[JsObject]
          .map(location ⇒
            LocationGeo(
              (location \ "location" \ "longitude").as[String].toDouble,
              (location \ "location" \ "latitude").as[String].toDouble)),
        address = (content \ "place").asOpt[JsObject]
          .map(location ⇒
            LocationAddress(
              (location \ "location" \ "country").asOpt[String],
              (location \ "location" \ "city").asOpt[String],
              (location \ "name").asOpt[String],
              (location \ "location" \ "street").asOpt[String],
              (location \ "location" \ "zip").asOpt[String])),
        tags = None))
        .toOption
        .filter(l ⇒ l.address.isDefined || l.geo.isDefined || l.tags.isDefined)

      DataFeedItem("facebook", (content \ "start_time").as[DateTime], Seq("event"),
        Some(title), Some(itemContent), location)
    }
  }
}

class FacebookFeedMapper extends DataEndpointMapper {
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("facebook/feed", None, dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("created_time", None, f))), None)),
      Some("created_time"), Some("descending"), None))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    for {
      title ← Try(if ((content \ "type").as[String] == "photo") {
        DataFeedItemTitle("You posted a photo", None, Some("photo"))
      }
      else if ((content \ "type").as[String] == "link") {
        DataFeedItemTitle("You shared a story", None, None)
      }
      else {
        DataFeedItemTitle("You posted", None, None)
      })
      itemContent ← Try(DataFeedItemContent(
        Some(
          s"""${(content \ "message").asOpt[String].getOrElse((content \ "story").as[String])}
             |
             |${(content \ "link").asOpt[String].getOrElse("")}""".stripMargin.trim), None,
        (content \ "picture").asOpt[String].map(url ⇒ List(DataFeedItemMedia(Some(url), (content \ "full_picture").asOpt[String]))), None))
      date ← Try((content \ "created_time").as[DateTime])
      tags ← Try(Seq("post", (content \ "type").as[String]))
    } yield {

      val locationGeo = Try(LocationGeo(
        (content \ "place" \ "location" \ "longitude").as[Double],
        (content \ "place" \ "location" \ "latitude").as[Double])).toOption

      val locationAddress = Try(LocationAddress(
        (content \ "place" \ "location" \ "country").asOpt[String],
        (content \ "place" \ "location" \ "city").asOpt[String],
        (content \ "place" \ "name").asOpt[String],
        (content \ "place" \ "location" \ "street").asOpt[String],
        (content \ "place" \ "location" \ "zip").asOpt[String])).toOption

      val maybeLocation = if (locationAddress.contains(LocationAddress(None, None, None, None, None))) {
        None
      }
      else {
        locationAddress
      }

      val location = locationGeo.orElse(maybeLocation).map(_ ⇒ DataFeedItemLocation(locationGeo, maybeLocation, None))

      DataFeedItem("facebook", date, tags, Some(title), Some(itemContent), location)
    }
  }
}

class NotablesFeedMapper extends DataEndpointMapper {
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(EndpointQuery("rumpel/notablesv1", None,
        dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("created_time", None, f))), None)), Some("created_time"), Some("descending"), None))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    for {
      title ← Try(if ((content \ "currently_shared").as[Boolean]) {
        DataFeedItemTitle("You posted", None, Some("public"))
      }
      else {
        DataFeedItemTitle("You posted", None, Some("private"))
      })
      itemContent ← Try(if ((content \ "photov1").isDefined && (content \ "photov1" \ "link").as[String].nonEmpty) {
        DataFeedItemContent(Some((content \ "message").as[String]), None, Some(Seq(
          DataFeedItemMedia(Some((content \ "photov1" \ "link").as[String]), Some((content \ "photov1" \ "link").as[String])))), None)
      }
      else {
        DataFeedItemContent(Some((content \ "message").as[String]), None, None, None)
      })
      location ← Try(if ((content \ "locationv1").isDefined) {
        Some(DataFeedItemLocation(Some(LocationGeo(
          (content \ "locationv1" \ "longitude").as[Double],
          (content \ "locationv1" \ "latitude").as[Double])), None, None))
      }
      else {
        None
      })
      date ← Try((content \ "created_time").as[DateTime])
      tags ← Try((content \ "shared_on").as[Seq[String]])
    } yield DataFeedItem("notables", date, tags, Some(title), Some(itemContent), location)
  }
}

class SpotifyFeedMapper extends DataEndpointMapper {
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(EndpointQuery("spotify/feed", None,
        dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("played_at", None, f))), None)), Some("played_at"), Some("descending"), None))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    for {
      durationSeconds ← Try((content \ "track" \ "duration_ms").as[Int] / 1000)
      title ← Try(
        DataFeedItemTitle("You listened", None, Some(s"${"%02d".format(durationSeconds / 60)}:${"%02d".format(durationSeconds % 60)}")))
      itemContent ← Try(DataFeedItemContent(
        Some(
          s"""${(content \ "track" \ "name").as[String]},
          |${(content \ "track" \ "artists").as[Seq[JsObject]].map(a ⇒ (a \ "name").as[String]).mkString(", ")},
          |${(content \ "track" \ "album" \ "name").as[String]}""".stripMargin),
        None,
        Some(
          Seq(DataFeedItemMedia((content \ "track" \ "album" \ "images" \ 0 \ "url").asOpt[String], (content \ "track" \ "album" \ "images" \ 0 \ "url").asOpt[String]))),
        None))
      date ← Try((content \ "played_at").as[DateTime])
    } yield DataFeedItem("spotify", date, Seq(), Some(title), Some(itemContent), None)
  }
}

class MonzoTransactionMapper extends DataEndpointMapper {
  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(EndpointQuery("monzo/transactions", None,
        dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("created", None, f))), None)), Some("created"), Some("descending"), None))
  }

  private val currencyMap = Map(
    "EUR" → "\u20AC",
    "GBP" → "\u00A3",
    "USD" → "\u0024",
    "JPY" → "\u00A5",
    "THB" → "\u0E3F")

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    for {
      paymentAmount ← Try((content \ "local_amount").as[Int] / 100.0)
      paymentCurrency ← Try({
        val currencyCode = (content \ "local_currency").as[String]
        currencyMap.getOrElse(currencyCode, currencyCode)
      })
      title ← Try(if ((content \ "is_load").as[Boolean] && (content \ "metadata" \ "p2p_transfer_id").asOpt[String].isEmpty) {
        DataFeedItemTitle("You topped up", Some(s"$paymentCurrency$paymentAmount"), Some("money"))
      }
      else if ((content \ "metadata" \ "p2p_transfer_id").asOpt[String].nonEmpty) {
        if ((content \ "originator").as[Boolean]) {
          DataFeedItemTitle("You paid a friend", Some(s"$paymentCurrency${-paymentAmount}"), Some("money"))
        }
        else {
          DataFeedItemTitle("You received money from a friend", Some(s"$paymentCurrency$paymentAmount"), Some("money"))
        }
      }
      else {
        if ((content \ "metadata" \ "is_reversal").asOpt[String].contains("true")) {
          DataFeedItemTitle("Your payment was refunded", Some(s"$paymentCurrency$paymentAmount"), Some("money"))
        }
        else if (paymentAmount > 0) {
          DataFeedItemTitle("You received", Some(s"$paymentCurrency$paymentAmount"), Some("money"))
        }
        else {
          DataFeedItemTitle("You spent", Some(s"$paymentCurrency${-paymentAmount}"), Some("money"))
        }
      })
      itemContent ← Try(DataFeedItemContent(
        Option(
          s"""|${(content \ "counterparty" \ "name").asOpt[String].orElse((content \ "merchant" \ "name").asOpt[String]).getOrElse("")}
             |
             |${(content \ "notes").asOpt[String].getOrElse("")}
             |
             |${(content \ "description").asOpt[String].getOrElse("")}
             |
             |${(content \ "merchant" \ "address" \ "short_formatted").asOpt[String].getOrElse("")}"""
            .stripMargin.replaceAll("\n\n\n", "").trim)
          .filter(_.nonEmpty),
        None,
        None,
        None))
      date ← Try((content \ "created").as[DateTime])
      tags ← Try(Seq("transaction", (content \ "category").as[String]))
    } yield {

      val locationGeo = Try(LocationGeo(
        (content \ "merchant" \ "address" \ "longitude").as[Double],
        (content \ "merchant" \ "address" \ "latitude").as[Double])).toOption

      val locationAddress = Try(LocationAddress(
        (content \ "merchant" \ "address" \ "country").asOpt[String],
        (content \ "merchant" \ "address" \ "city").asOpt[String],
        (content \ "merchant" \ "name").asOpt[String],
        (content \ "merchant" \ "address" \ "address").asOpt[String],
        (content \ "merchant" \ "address" \ "postcode").asOpt[String])).toOption

      val maybeLocation = if (locationAddress.contains(LocationAddress(None, None, None, None, None))) {
        None
      }
      else {
        locationAddress
      }

      val location = locationGeo.orElse(maybeLocation).map(_ ⇒ DataFeedItemLocation(locationGeo, maybeLocation, None))
      val feedItemContent = if (itemContent == DataFeedItemContent(None, None, None, None)) {
        None
      }
      else {
        Some(itemContent)
      }

      DataFeedItem("monzo", date, tags, Some(title), feedItemContent, location)
    }
  }
}

class InstagramMediaMapper extends DataEndpointMapper {
  override protected val dataDeduplicationField: Option[String] = Some("id")

  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    val unixDateFilter = fromDate.flatMap { _ =>
      Some(FilterOperator.Between(Json.toJson(fromDate.map(t => (t.getMillis / 1000).toString)), Json.toJson(untilDate.map(t => (t.getMillis / 1000).toString))))
    }

    Seq(PropertyQuery(
      List(EndpointQuery("instagram/feed", None,
        unixDateFilter.map(f ⇒ Seq(EndpointQueryFilter("created_time", None, f))), None)), Some("created_time"), Some("descending"), None))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    for {
      createdTime <- Try(new DateTime((content \ "created_time").as[String].toLong * 1000))
      tags <- Try((content \ "tags").as[List[String]])
      kind <- Try((content \ "type").as[String])
      title <- Try(Some(DataFeedItemTitle("You posted", None, Some(kind))))
      feedItemContent <- Try(Some(DataFeedItemContent(
        (content \ "caption" \ "text").asOpt[String],
        None,
        kind match {
          case "image" =>
            Some(Seq(DataFeedItemMedia(
              (content \ "images" \ "thumbnail" \ "url").asOpt[String],
              (content \ "images" \ "standard_resolution" \ "url").asOpt[String])))
          case "carousel" =>
            Some((content \ "carousel_media").as[Seq[JsObject]].map { imageInfo =>
              DataFeedItemMedia(
                (imageInfo \ "images" \ "thumbnail" \ "url").asOpt[String],
                (imageInfo \ "images" \ "standard_resolution" \ "url").asOpt[String])
            })
          case _ => None
        },
        None)))
    } yield {
      val location = Try(DataFeedItemLocation(
        geo = (content \ "location").asOpt[JsObject]
          .map(location =>
            LocationGeo(
              (location \ "longitude").as[Double],
              (location \ "latitude").as[Double])),
        address = (content \ "location" \ "street_address").asOpt[String]
          .map(fullAddress => LocationAddress(None, None, Some(fullAddress), None, None)),
        tags = None)).toOption

      DataFeedItem("instagram", createdTime, tags, title, feedItemContent, location)
    }
  }
}
