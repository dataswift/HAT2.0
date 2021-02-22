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
import org.hatdex.hat.api.service.applications.ApplicationsServiceContext
import io.dataswift.models.hat.json.ApplicationJsonProtocol
import io.dataswift.models.hat.applications.{ Application, HatApplication }
import io.dataswift.models.hat.{ AccessToken, ErrorMessage }
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import play.api.libs.json.{ JsObject, JsString }
import org.specs2.mock.Mockito
import org.specs2.specification.{ BeforeAll, BeforeEach }
import play.api.Logger
import play.api.test.{ FakeRequest, PlaySpecification }

import scala.concurrent.Await
import scala.concurrent.duration._

class ApplicationsSpec
    extends PlaySpecification
    with Mockito
    with ApplicationsServiceContext
    with BeforeEach
    with BeforeAll {

  val logger = Logger(this.getClass)

  sequential

  def beforeAll: Unit =
    Await.result(databaseReady, 60.seconds)

  override def before: Unit = {
    import org.hatdex.hat.dal.Tables
    import org.hatdex.libs.dal.HATPostgresProfile.api._

    val action = DBIO.seq(Tables.ApplicationStatus.delete)

    Await.result(hatDatabase.run(action), 60.seconds)
  }

  import ApplicationJsonProtocol._
  import io.dataswift.models.hat.json.HatJsonFormats.{ accessTokenFormat, errorMessage }

  "The `applications` method" should {
    "Return list of available applications" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Applications]
      val result     = controller.applications().apply(request)

      status(result) must equalTo(OK)
      contentAsJson(result).validate[Seq[HatApplication]].isSuccess must beTrue
      val apps = contentAsJson(result).as[Seq[HatApplication]]
      apps.length must be equalTo 8
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
      val result     = controller.applicationStatus(notablesApp.id).apply(request)

      status(result) must equalTo(OK)
      val app = contentAsJson(result).as[HatApplication]
      app.application.info.name must be equalTo notablesApp.info.name
    }

    "Return 404 for a non-existent application" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Applications]
      val result     = controller.applicationStatus("random-id").apply(request)

      status(result) must equalTo(NOT_FOUND)
      val error = contentAsJson(result).as[ErrorMessage]
      error.message must be equalTo "Application not Found"
    }
  }

  "The `hmi` method" should {
    "Return the information about the specified application" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")

      val controller = application.injector.instanceOf[Applications]
      val result     = controller.hmi(notablesApp.id).apply(request)

      status(result) must equalTo(OK)
      val app = contentAsJson(result).as[Application]
      app.id must beEqualTo(notablesApp.id)
    }

    "Return 404 for non-existend application" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")

      val controller = application.injector.instanceOf[Applications]
      val result     = controller.hmi("random-id").apply(request)

      status(result) must equalTo(NOT_FOUND)
      val error = contentAsJson(result).as[ErrorMessage]
      error.cause must startWith("Application configuration for ID random-id could not be found")
    }
  }

  "The `applicationSetup` method" should {
    "Return setup application status" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Applications]
      val result     = controller.applicationSetup(notablesApp.id).apply(request)

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
      val result     = controller.applicationSetup("random-id").apply(request)

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
      val result     = controller.applicationDisable(notablesApp.id).apply(request)

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
      val result     = controller.applicationDisable("random-id").apply(request)

      status(result) must equalTo(BAD_REQUEST)
      val error = contentAsJson(result).as[ErrorMessage]
      error.message must be equalTo "Application not Found"
    }
  }

  "The `applicationToken` method" should {
    "Return 401 Forbidden for application token with no explicit permission" in {
      val authenticator: HatApiAuthEnvironment#A =
        FakeAuthenticator[HatApiAuthEnvironment](owner.loginInfo)
          .copy(customClaims =
            Some(
              JsObject(
                Map(
                  "application" -> JsString("notables"),
                  "applicationVersion" -> JsString("1.0.0")
                )
              )
            )
          )

      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator[HatApiAuthEnvironment](authenticator)(environment)

      val controller = application.injector.instanceOf[Applications]
      val result     = controller.applicationToken(notablesApp.id).apply(request)

      status(result) must equalTo(FORBIDDEN)
      logger.info(s"Got back result ${contentAsString(result)}")
      val error = contentAsJson(result) \ "error"
      error.get.as[String] must be equalTo "Forbidden"
    }

    "Return 404 for application that does not exist" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Applications]
      val result     = controller.applicationToken("random-id").apply(request)

      status(result) must equalTo(NOT_FOUND)
      val error = contentAsJson(result).as[ErrorMessage]
      error.message must be equalTo "Application not Found"
    }

    "Return access token" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Applications]
      val result     = controller.applicationToken(notablesApp.id).apply(request)

      status(result) must equalTo(OK)
      val token = contentAsJson(result).as[AccessToken]
      token.accessToken must not beEmpty
    }
  }
}
