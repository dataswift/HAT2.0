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
 * 2 / 2018
 */

package org.hatdex.hat.api.controllers

import com.mohiva.play.silhouette.test._
import org.hatdex.hat.api.json.ApplicationJsonProtocol
import org.hatdex.hat.api.models.ErrorMessage
import org.hatdex.hat.api.models.applications.HatApplication
import org.hatdex.hat.api.service.applications.ApplicationsServiceContext
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.{ BeforeAll, BeforeEach }
import play.api.Logger
import play.api.test.{ FakeRequest, PlaySpecification }

import scala.concurrent.Await
import scala.concurrent.duration._

class ApplicationsSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with ApplicationsServiceContext with BeforeEach with BeforeAll {

  val logger = Logger(this.getClass)

  sequential

  def beforeAll: Unit = {
    Await.result(databaseReady, 60.seconds)
  }

  override def before: Unit = {
    import org.hatdex.hat.dal.Tables
    import org.hatdex.libs.dal.HATPostgresProfile.api._

    val action = DBIO.seq(
      Tables.ApplicationStatus.delete)

    Await.result(hatDatabase.run(action), 60.seconds)
  }

  import ApplicationJsonProtocol.applicationStatusFormat
  import org.hatdex.hat.api.json.HatJsonFormats.errorMessage

  "The `applications` method" should {
    "Return list of available applications" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Applications]
      val result = controller.applications().apply(request)

      status(result) must equalTo(OK)
      val apps = contentAsJson(result).as[Seq[HatApplication]]
      apps.length must be equalTo 5
      apps.find(_.application.id == notablesApp.id) must beSome
      apps.find(_.application.id == notablesAppDebitless.id) must beSome
      apps.find(_.application.id == notablesAppIncompatible.id) must beSome
    }
  }

  "The `applicationStatus` method" should {
    "Return status of a single application" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Applications]
      val result = controller.applicationStatus(notablesApp.id).apply(request)

      status(result) must equalTo(OK)
      val app = contentAsJson(result).as[HatApplication]
      app.application.info.name must be equalTo notablesApp.info.name
    }

    "Return 404 for a non-existent application" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Applications]
      val result = controller.applicationStatus("random-id").apply(request)

      status(result) must equalTo(NOT_FOUND)
      val error = contentAsJson(result).as[ErrorMessage]
      error.message must be equalTo "Application not Found"
    }
  }

  "The `applicationSetup` method" should {
    "Return setup application status" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Applications]
      val result = controller.applicationSetup(notablesApp.id).apply(request)

      status(result) must equalTo(OK)
      val app = contentAsJson(result).as[HatApplication]
      app.application.info.name must be equalTo notablesApp.info.name
      app.setup must be equalTo true
      app.active must be equalTo true
    }

    "Return 404 for a non-existent application" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Applications]
      val result = controller.applicationSetup("random-id").apply(request)

      status(result) must equalTo(BAD_REQUEST)
      val error = contentAsJson(result).as[ErrorMessage]
      error.message must be equalTo "Application not Found"
    }
  }

  "The `applicationSetup` method" should {
    "Return disabled application status" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Applications]
      val result = controller.applicationDisable(notablesApp.id).apply(request)

      status(result) must equalTo(OK)
      val app = contentAsJson(result).as[HatApplication]
      app.application.info.name must be equalTo notablesApp.info.name
      app.setup must be equalTo true
      app.active must be equalTo false
    }

    "Return 404 for a non-existent application" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Applications]
      val result = controller.applicationDisable("random-id").apply(request)

      status(result) must equalTo(BAD_REQUEST)
      val error = contentAsJson(result).as[ErrorMessage]
      error.message must be equalTo "Application not Found"
    }
  }

}
