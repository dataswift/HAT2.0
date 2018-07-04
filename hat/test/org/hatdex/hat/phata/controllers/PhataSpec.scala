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
 * 2 / 2017
 */

package org.hatdex.hat.phata.controllers

import com.mohiva.play.silhouette.test._
import org.hatdex.hat.api.HATTestContext
import org.hatdex.hat.api.models.EndpointData
import org.hatdex.hat.api.service.richData.RichDataService
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.{ BeforeAll, BeforeEach }
import play.api.Logger
import play.api.libs.json.Json
import play.api.test.{ FakeRequest, Helpers, PlaySpecification }

import scala.concurrent.Await
import scala.concurrent.duration._

class PhataSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with Context with BeforeEach with BeforeAll {

  val logger = Logger(this.getClass)

  import org.hatdex.hat.api.json.RichDataJsonFormats._

  sequential

  def beforeAll: Unit = {
    Await.result(databaseReady, 60.seconds)
  }

  override def before: Unit = {
    import org.hatdex.hat.dal.Tables._
    import org.hatdex.libs.dal.HATPostgresProfile.api._

    val endpointRecrodsQuery = DataJson.filter(d => d.source.like("test%") || d.source.like("rumpel%")).map(_.recordId)

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

  "The `profile` method" should {
    "Return bundle data with profile information" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Phata]
      val dataService = application.injector.instanceOf[RichDataService]

      val data = List(
        EndpointData("rumpel/notablesv1", None, samplePublicNotable, None),
        EndpointData("rumpel/notablesv1", None, samplePrivateNotable, None),
        EndpointData("rumpel/notablesv1", None, sampleSocialNotable, None))

      val result = for {
        _ <- dataService.saveData(owner.userId, data)
        response <- Helpers.call(controller.profile, request)
      } yield response

      status(result) must equalTo(OK)
      val phataData = contentAsJson(result).as[Map[String, Seq[EndpointData]]]

      phataData.get("notables") must beSome
      phataData("notables").length must be equalTo (1)

    }
  }

  //  "The `launcher` method" should {
  //    "return status 401 if authenticator but no identity was found" in new Context {
  //      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
  //        .withAuthenticator(LoginInfo("xing", "comedian@watchmen.com"))
  //
  //      val controller = application.injector.instanceOf[Phata]
  //      val result: Future[Result] = databaseReady.flatMap(_ => controller.launcher().apply(request))
  //
  //      status(result) must equalTo(UNAUTHORIZED)
  //    }
  //
  //    "return OK if authenticator for matching identity" in new Context {
  //      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
  //        .withAuthenticator(owner.loginInfo)
  //
  //      val controller = application.injector.instanceOf[Phata]
  //      val result: Future[Result] = databaseReady.flatMap(_ => controller.launcher().apply(request))
  //
  //      status(result) must equalTo(OK)
  //      contentAsString(result) must contain("MarketSquare")
  //      contentAsString(result) must contain("Rumpel")
  //    }
  //  }

}

trait Context extends HATTestContext {

  val samplePublicNotable = Json.parse(
    """
      |{
      |    "kind": "note",
      |    "author":
      |    {
      |        "phata": "testing.hubat.net"
      |    },
      |    "shared": true,
      |    "message": "public message",
      |    "shared_on": "phata",
      |    "created_time": "2017-10-18T15:32:43+01:00",
      |    "public_until": "",
      |    "updated_time": "2017-10-23T18:29:59+01:00"
      |}
    """.stripMargin)

  val samplePrivateNotable = Json.parse(
    """
      |{
      |    "kind": "note",
      |    "author":
      |    {
      |        "phata": "testing.hubat.net"
      |    },
      |    "shared": false,
      |    "message": "private message",
      |    "shared_on": "marketsquare",
      |    "created_time": "2017-10-18T15:32:43+01:00",
      |    "public_until": "",
      |    "updated_time": "2017-10-23T18:29:59+01:00"
      |}
    """.stripMargin)

  val sampleSocialNotable = Json.parse(
    """
      |{
      |    "kind": "note",
      |    "author":
      |    {
      |        "phata": "testing.hubat.net"
      |    },
      |    "shared": true,
      |    "message": "social message",
      |    "shared_on": "facebook,twitter",
      |    "created_time": "2017-10-18T15:32:43+01:00",
      |    "public_until": "",
      |    "updated_time": "2017-10-23T18:29:59+01:00"
      |}
    """.stripMargin)
}