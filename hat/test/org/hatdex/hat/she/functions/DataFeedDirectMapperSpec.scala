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

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Failure

import io.dataswift.models.hat.FilterOperator.Between
import io.dataswift.test.common.BaseSpec
import org.hatdex.hat.she.mappers._
import org.joda.time.{ DateTime, DateTimeUtils }
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import play.api.Logger

class DataFeedDirectMapperSpec
    extends BaseSpec
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with DataFeedDirectMapperContext {

  val logger: Logger = Logger(this.getClass)

  override def beforeAll: Unit = {
    DateTimeUtils.setCurrentMillisFixed(1514764800000L)
    Await.result(databaseReady, 60.seconds)
  }

  override def afterAll: Unit =
    DateTimeUtils.setCurrentMillisSystem()

  override def before(): Unit = {
    import org.hatdex.hat.dal.Tables._
    import org.hatdex.libs.dal.HATPostgresProfile.api._

    val endpointRecordsQuery = DataJson
      .filter(d =>
        d.source.like("test%") ||
          d.source.like("rumpel%") ||
          d.source.like("twitter%") ||
          d.source.like("facebook%") ||
          d.source.like("fitbit%") ||
          d.source.like("calendar%")
      )
      .map(_.recordId)

    val action = DBIO.seq(
      DataDebitBundle.filter(_.bundleId.like("test%")).delete,
      DataDebitContract.filter(_.dataDebitKey.like("test%")).delete,
      DataCombinators.filter(_.combinatorId.like("test%")).delete,
      DataBundles.filter(_.bundleId.like("test%")).delete,
      DataJsonGroupRecords.filter(_.recordId in endpointRecordsQuery).delete,
      DataJsonGroups.filterNot(g => g.groupId in DataJsonGroupRecords.map(_.groupId)).delete,
      DataJson.filter(r => r.recordId in endpointRecordsQuery).delete
    )

    Await.result(db.run(action), 60.seconds)
  }

  "The `mapGoogleCalendarEvent` method" should "translate google calendar event with timezone information" in {
    val mapper      = new GoogleCalendarMapper()
    val transformed = mapper.mapDataRecord(googleCalendarEvent.recordId.get, googleCalendarEvent.data).get
    transformed.source must equal("google")
    transformed.types must contain("event")
    transformed.title.value.text must include("MadHATTERs Tea Party: The Boston Party")
    transformed.title.value.subtitle.value must include("12 December 18:30 - 22:30 America/New_York")
    transformed.content.value.text.value must include("personal data, user accounts, security and value")
  }

  it should "remove html tags from google calendar event description" in {
    val mapper      = new GoogleCalendarMapper()
    val transformed = mapper.mapDataRecord(googleCalendarEventHtml.recordId.get, googleCalendarEventHtml.data).get
    transformed.source must be("google")
    transformed.types must contain("event")
    transformed.title.value.text must include("MadHATTERs Tea Party: The Boston Party")
    transformed.title.value.subtitle.value must include("12 December 18:30 - 22:30 America/New_York")
    transformed.content.value.text.value must include("BD call")
    transformed.content.value.text.value must not(include("<br>"))
    transformed.content.value.text.value must not(include("&nbsp;"))
    transformed.content.value.text.value must not(include("</a>"))
  }

  it should "translate google calendar full-day event" in {
    val mapper = new GoogleCalendarMapper()
    val transformed =
      mapper.mapDataRecord(googleCalendarFullDayEvent.recordId.get, googleCalendarFullDayEvent.data).get
    transformed.source must equal("google")
    transformed.types must contain("event")
    transformed.content.value.text must be(None)
  }

  // TODO: updated tweet with retweet structure
  "The `mapTweet` method" should "translate twitter retweets" in {
    val mapper      = new TwitterFeedMapper()
    val transformed = mapper.mapDataRecord(exampleTweetRetweet.recordId.get, exampleTweetRetweet.data).get
    transformed.source must equal("twitter")
    transformed.types must contain("post")
    transformed.title.value.text must include("You retweeted")
    transformed.content.value.text.value must include("RT @jupenur: Oh shit Adobe https://t.co/7rDL3LWVVz")

    transformed.location.value.geo.get.longitude must equal(-75.14310264)
    transformed.location.value.address.get.country.get must equal("United States")
    transformed.location.value.address.get.city.get must equal("Washington")
  }

  // TODO: update tweet with reply structure
  it should "translate twitter replies" in {
    val mapper      = new TwitterFeedMapper()
    val transformed = mapper.mapDataRecord(exampleTweetMentions.recordId.get, exampleTweetMentions.data).get
    transformed.source must equal("twitter")
    transformed.title.value.text must include("You replied to @drgeep")
  }

  it should "translate minimal tweet structure correctly" in {
    val mapper      = new TwitterFeedMapper()
    val transformed = mapper.mapDataRecord(exampleTweetMinimalFields.recordId.get, exampleTweetMinimalFields.data).get
    transformed.source must equal("twitter")
    transformed.content.value.text.value must be("Tweet from Portugal.")
  }

  "The `InstagramMediaMapper` class" should "translate single image posts using v1 API" in {
    val mapper      = new InstagramMediaMapper()
    val transformed = mapper.mapDataRecord(exampleInstagramImagev1.recordId.get, exampleInstagramImagev1.data).get
    transformed.source must equal("instagram")
    transformed.title.value.text must include("You posted")
    transformed.title.value.action.value must equal("image")
    transformed.content.value.text.value must include("Saturday breakfast magic")
    transformed.content.value.media.value.length must equal(1)
    transformed.content.value.media.value.head.url.value must startWith("https://scontent.cdninstagram.com/vp")
  }

  it should "translate multiple image carousel posts using v1 API" in {
    val mapper = new InstagramMediaMapper()
    val transformed =
      mapper.mapDataRecord(exampleMultipleInstagramImages.recordId.get, exampleMultipleInstagramImages.data).get
    transformed.source must equal("instagram")
    transformed.title.value.text must include("You posted")
    transformed.title.value.action.value must equal("carousel")
    transformed.content.value.text.value must include("The beauty of Richmond park...")
    transformed.content.value.media.value.length must equal(3)
    transformed.content.value.media.value.head.url.value must startWith("https://scontent.cdninstagram.com/vp")
  }

  it should "translate single image posts using v2 API" in {
    val mapper      = new InstagramMediaMapper()
    val transformed = mapper.mapDataRecord(exampleInstagramImagev2.recordId.get, exampleInstagramImagev2.data).get
    transformed.source must equal("instagram")
    transformed.title.value.text must include("You posted")
    transformed.title.value.action.value must equal("image")
    transformed.content.value.text.value must include("Saturday breakfast magic")
    transformed.content.value.media.value.length must equal(1)
    transformed.content.value.media.value.head.url.value must startWith("https://scontent.xx.fbcdn.net/v/")
  }

  it should "create data queries using correct unix timestamp format" in {
    val mapper    = new InstagramMediaMapper()
    val fromDate  = new DateTime("2018-05-01T09:00:00Z")
    val untilDate = fromDate.plusDays(1)

    val propertyQuery = mapper.dataQueries(Some(fromDate), Some(untilDate))
    propertyQuery.head.orderBy.value must equal("ds_created_time")
    propertyQuery.head.endpoints.head.endpoint must equal("instagram/feed")
    propertyQuery.head.endpoints.head.filters.value.head.operator
      .asInstanceOf[Between]
      .lower
      .as[String] must equal("1525165200")
    propertyQuery.head.endpoints.head.filters.value.head.operator
      .asInstanceOf[Between]
      .upper
      .as[String] must equal("1525251600")
  }

  "The `mapFacebookPost` method" should "translate facebook photo posts" in {
    val mapper      = new FacebookFeedMapper()
    val transformed = mapper.mapDataRecord(exampleFacebookPhotoPost.recordId.value, exampleFacebookPhotoPost.data).get

    transformed.source must equal("facebook")
    transformed.title.value.text must equal("You posted a photo")
    //transformed.content.value.media.value.head.url.value must be startingWith "https://scontent.xx.fbcdn.net"
  }

  it should "translate facebook replies" in {
    val mapper      = new FacebookFeedMapper()
    val transformed = mapper.mapDataRecord(exampleFacebookPost.recordId.value, exampleFacebookPost.data).get
    transformed.source must equal("facebook")
    transformed.title.value.text must equal("You posted")
    //transformed.content.value.text.value must be startingWith "jetlag wouldn't be so bad if not for  Aileen signing (whistling?) out the window overnight..."
  }

  it should "translate facebook stories" in {
    val mapper      = new FacebookFeedMapper()
    val transformed = mapper.mapDataRecord(facebookStory.recordId.value, facebookStory.data).get
    transformed.source must equal("facebook")
    transformed.title.value.text must equal("You shared a story")
    //transformed.content.value.text.value must be startingWith "Guilty. Though works for startups too."
    transformed.content.value.text.value must include("http://phdcomics.com/comics.php?f=1969")
  }

  "The `mapFacebookEvent` method" should "translate facebook events with location" in {
    val mapper      = new FacebookEventMapper()
    val transformed = mapper.mapDataRecord(facebookEvent.recordId.value, facebookEvent.data).get

    transformed.source must equal("facebook")
    transformed.types must contain("event")
    transformed.title.value.text must equal("You are attending an event")
    transformed.content.value.text.value must include("We're going somewhere new")
    transformed.location.value.address.value.city.value must equal("Singapore")
    transformed.location.value.address.value.name.value must equal("Carlton Hotel Singapore")
  }

  it should "translate facebook events without location" in {
    val mapper      = new FacebookEventMapper()
    val transformed = mapper.mapDataRecord(facebookEvenNoLocation.recordId.value, facebookEvenNoLocation.data).get

    transformed.source must equal("facebook")
    transformed.types must contain("event")
    transformed.title.value.text must equal("You are attending an event")
    transformed.content.value.text.value must include("privacy, security, access rights, regulation")
    transformed.location must equal(None)
  }

  it should "translate facebook events with incomplete location" in {
    val mapper = new FacebookEventMapper()
    val transformed =
      mapper.mapDataRecord(facebookEvenPartialLocation.recordId.value, facebookEvenPartialLocation.data).get

    transformed.source must equal("facebook")
    transformed.types must contain("event")
    transformed.title.value.text must equal("You are attending an event")
    transformed.content.value.text.value must include("privacy, security, access rights, regulation")
    transformed.location must equal(None)
  }

  "The `mapFitbitWeight` method" should "translate fitbit weight" in {
    val mapper      = new FitbitWeightMapper()
    val transformed = mapper.mapDataRecord(fitbitWeightMeasurement.recordId.value, fitbitWeightMeasurement.data).get

    transformed.source must equal("fitbit")
    transformed.types must contain("fitness")
    transformed.types must contain("weight")
    transformed.title.value.text must equal("You added a new weight measurement")
    transformed.content.value.text.value must include("94.8")
    transformed.content.value.text.value must include("25.46")
    transformed.content.value.text.value must include("21.5")
  }

  "The `mapFitbitSleep` method" should "translate fitbit sleep" in {
    val mapper      = new FitbitSleepMapper()
    val transformed = mapper.mapDataRecord(fitbitSleepMeasurement.recordId.value, fitbitSleepMeasurement.data).get

    transformed.source must equal("fitbit")
    transformed.types must contain("fitness")
    transformed.types must contain("sleep")
    transformed.title.value.text must include("You woke up")
    transformed.content.value.text.value must include("You spent 8 hours and 4 minutes in bed.")
    transformed.content.value.text.value must include("You slept for 7 hours and 20 minutes ")
    transformed.content.value.text.value must include("and were awake for 44 minutes")
  }

  "The `mapFitbitActivity` method" should "translate fitbit activity" in {
    val mapper      = new FitbitActivityMapper()
    val transformed = mapper.mapDataRecord(fitbitActivity.recordId.value, fitbitActivity.data).get

    transformed.source must equal("fitbit")
    transformed.types must contain("fitness")
    transformed.title.value.text must include("You logged Fitbit activity")
    transformed.content.value.text.value must include("Activity: Walk")
    transformed.content.value.text.value must include("Duration: 17 minutes")
    transformed.content.value.text.value must include("Average heart rate: 94")
    transformed.content.value.text.value must include("Calories burned: 126")
  }

  "The `mapFitbitDaySummarySteps` method" should "not generate a feed item for days with 0 steps recorded" in {
    val mapper      = new FitbitActivityDaySummaryMapper()
    val transformed = mapper.mapDataRecord(fitbitDayEmptySummary.recordId.value, fitbitDayEmptySummary.data)
    transformed match {
      case Failure(_) => true
      case _          => fail()
    }
  }

  it should "translate fitbit day summary to steps" in {
    val mapper      = new FitbitActivityDaySummaryMapper()
    val transformed = mapper.mapDataRecord(fitbitDaySummary.recordId.value, fitbitDaySummary.data).get
    transformed.source must equal("fitbit")
    transformed.types must contain("fitness")
    transformed.title.value.text  must include("You walked 12135 steps")
    transformed.content must equal(None)
  }
}
