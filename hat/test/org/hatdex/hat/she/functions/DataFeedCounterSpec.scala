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

import org.hatdex.hat.api.models.EndpointData
import org.hatdex.hat.api.service.richData.RichDataService
import org.hatdex.hat.she.models.Request
import org.joda.time.DateTime
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeAll
import play.api.Logger
import play.api.test.PlaySpecification

import scala.concurrent.Await
import scala.concurrent.duration._

class DataFeedCounterSpec(implicit ee: ExecutionEnv) extends DataFeedCounter with PlaySpecification with Mockito with DataFeedDirectMapperContext with BeforeAll {
  val logger = Logger(this.getClass)

  def beforeAll: Unit = {
    Await.result(databaseReady, 60.seconds)
  }

  override def before(): Unit = {
    import org.hatdex.hat.dal.Tables._
    import org.hatdex.libs.dal.HATPostgresProfile.api._

    val endpointRecrodsQuery = DataJson.filter(d => d.source.like("test%") ||
      d.source.like("rumpel%") ||
      d.source.like("twitter%") ||
      d.source.like("facebook%") ||
      d.source.like("fitbit%") ||
      d.source.like("calendar%")).map(_.recordId)

    val action = DBIO.seq(
      DataDebitBundle.filter(_.bundleId.like("test%")).delete,
      DataDebitContract.filter(_.dataDebitKey.like("test%")).delete,
      DataCombinators.filter(_.combinatorId.like("test%")).delete,
      DataBundles.filter(_.bundleId.like("test%")).delete,
      DataJsonGroupRecords.filter(_.recordId in endpointRecrodsQuery).delete,
      DataJsonGroups.filterNot(g => g.groupId in DataJsonGroupRecords.map(_.groupId)).delete,
      DataJson.filter(r => r.recordId in endpointRecrodsQuery).delete)

    Await.result(hatDatabase.run(action), 60.seconds)
  }

  sequential

  "The `execute` method" should {
    "return correct counters of records" in {
      val request = Request(Map[String, Seq[EndpointData]](
        "twitter" -> Seq(exampleTweetRetweet, exampleTweetMentions),
        "facebook/feed" -> Seq(exampleFacebookPhotoPost, exampleFacebookPost, facebookStory),
        "facebook/events" -> Seq(facebookEvent, facebookEvenNoLocation, facebookEvenPartialLocation),
        "fitbit/sleep" -> Seq(fitbitSleepMeasurement),
        "fitbit/weight" -> Seq(fitbitWeightMeasurement),
        "fitbit/activity" -> Seq(fitbitActivity),
        "fitbit/activity/day/summary" -> Seq(fitbitDaySummary),
        "calendar" -> Seq(googleCalendarEvent, googleCalendarFullDayEvent)), linkRecords = true)

      val response = execute(configuration, request)

      response map { responseRecords =>
        responseRecords.headOption must beSome
        responseRecords.head.namespace must be equalTo "she"
        responseRecords.head.endpoint must be equalTo "insights/activity-records"
        responseRecords.head.data.length must be equalTo 1
        (responseRecords.head.data.head \ "counters" \ "facebook/feed").as[Int] must be equalTo 3
        (responseRecords.head.data.head \ "counters" \ "twitter").as[Int] must be equalTo 2
      } await (3, 10.seconds)
    }

    "include last execution date when available" in {
      val request = Request(Map[String, Seq[EndpointData]](
        "twitter" -> Seq(exampleTweetRetweet, exampleTweetMentions),
        "facebook/feed" -> Seq(exampleFacebookPhotoPost, exampleFacebookPost, facebookStory),
        "facebook/events" -> Seq(facebookEvent, facebookEvenNoLocation, facebookEvenPartialLocation),
        "fitbit/sleep" -> Seq(fitbitSleepMeasurement),
        "fitbit/weight" -> Seq(fitbitWeightMeasurement),
        "fitbit/activity" -> Seq(fitbitActivity),
        "fitbit/activity/day/summary" -> Seq(fitbitDaySummary),
        "calendar" -> Seq(googleCalendarEvent, googleCalendarFullDayEvent)), linkRecords = true)

      val response = execute(configuration.copy(lastExecution = Some(DateTime.now().minusDays(7))), request)

      response map { responseRecords =>
        responseRecords.headOption must beSome
        responseRecords.head.namespace must be equalTo "she"
        responseRecords.head.endpoint must be equalTo "insights/activity-records"
        responseRecords.head.data.length must be equalTo 1
        (responseRecords.head.data.head \ "since").as[DateTime].isBefore(DateTime.now().minusDays(6)) must beTrue
        (responseRecords.head.data.head \ "counters" \ "facebook/feed").as[Int] must be equalTo 3
        (responseRecords.head.data.head \ "counters" \ "twitter").as[Int] must be equalTo 2
      } await (3, 10.seconds)
    }
  }

  "The bundle specification" should {
    "Include all matching data" in {
      await(databaseReady)(10.seconds)

      val records = Seq(exampleTweetRetweet, exampleTweetMentions, exampleFacebookPhotoPost, exampleFacebookPost,
        facebookStory, facebookEvent, facebookEvenNoLocation, facebookEvenPartialLocation, fitbitSleepMeasurement,
        fitbitWeightMeasurement, fitbitActivity, fitbitDaySummary, googleCalendarEvent, googleCalendarFullDayEvent)

      val service = application.injector.instanceOf[RichDataService]
      val res = for {
        _ <- service.saveData(owner.userId, records)
        data <- service.bundleData(configuration.dataBundle)
      } yield {
        data("twitter/tweets").length must be equalTo 2
        data("facebook/feed").length must be equalTo 3
        data("calendar/google/events").length must be equalTo 2
        data("fitbit/sleep").length must be equalTo 1
        data("fitbit/weight").length must be equalTo 1
        data("fitbit/activity").length must be equalTo 1
        data.values.toSeq.flatten.length must be equalTo 10
      }

      res.await(1, 30.seconds)

    }

    "Correctly filter data by date" in {
      await(databaseReady)(10.seconds)
      val from: DateTime = DateTime.parse("2017-09-22T19:24:47+0000")
      val to: DateTime = DateTime.parse("2017-11-05T12:34:55+0000")

      val records = Seq(exampleTweetRetweet, exampleTweetMentions, exampleFacebookPhotoPost, exampleFacebookPost,
        facebookStory, facebookEvent, facebookEvenNoLocation, facebookEvenPartialLocation, fitbitSleepMeasurement,
        fitbitWeightMeasurement, fitbitActivity, fitbitDaySummary, googleCalendarEvent, googleCalendarFullDayEvent)

      val service = application.injector.instanceOf[RichDataService]
      val res = for {
        _ <- service.saveData(owner.userId, records)
        data <- service.bundleData(bundleFilterByDate(Some(from), Some(to)))
      } yield {
        data("twitter/tweets").length must be equalTo 1
        data("facebook/feed").length must be equalTo 1
        data("fitbit/sleep").length must be equalTo 0
        data("fitbit/weight").length must be equalTo 1
        data("fitbit/activity").length must be equalTo 0
        data("calendar/google/events").length must be equalTo 1
      }

      res.await(1, 30.seconds)
    }
  }
}

