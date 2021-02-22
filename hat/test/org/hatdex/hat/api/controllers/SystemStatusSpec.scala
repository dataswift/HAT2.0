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

package org.hatdex.hat.api.controllers

import com.mohiva.play.silhouette.test._
import org.hatdex.hat.api.HATTestContext
import io.dataswift.models.hat.json.HatJsonFormats
import io.dataswift.models.hat.{ HatStatus, StatusKind }
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeAll
import play.api.Logger
import play.api.test.{ FakeRequest, PlaySpecification }

import scala.concurrent.Await
import scala.concurrent.duration._

class SystemStatusSpec(implicit ee: ExecutionEnv)
    extends PlaySpecification
    with Mockito
    with HATTestContext
    with BeforeAll
    with HatJsonFormats {

  val logger = Logger(this.getClass)

  sequential

  def beforeAll: Unit =
    Await.result(databaseReady, 60.seconds)

  "The `update` method" should {
    "Return success response after updating HAT database" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")

      val controller = application.injector.instanceOf[SystemStatus]
      val result     = controller.update().apply(request)

      status(result) must equalTo(OK)
      (contentAsJson(result) \ "message").as[String] must be equalTo "Database updated"
    }
  }

  "The `status` method" should {
    "Return current utilisation" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[SystemStatus]
      val result     = controller.status().apply(request)

      status(result) must equalTo(OK)
      val stats = contentAsJson(result).as[List[HatStatus]]

      stats.length must be greaterThan 0
      stats.find(_.title == "Previous Login").get.kind must be equalTo StatusKind.Text("Never", None)
      stats.find(_.title == "Owner Email").get.kind must be equalTo StatusKind.Text("user@hat.org", None)
      stats.find(_.title == "Database Storage").get.kind must haveClass[StatusKind.Numeric]
      stats.find(_.title == "File Storage").get.kind must haveClass[StatusKind.Numeric]
      stats.find(_.title == "Database Storage Used").get.kind must haveClass[StatusKind.Numeric]
      stats.find(_.title == "File Storage Used").get.kind must haveClass[StatusKind.Numeric]
      stats.find(_.title == "Database Storage Used Share").get.kind must haveClass[StatusKind.Numeric]
      stats.find(_.title == "File Storage Used Share").get.kind must haveClass[StatusKind.Numeric]
    }

    "Return last login information when present" in {
      val authRequest = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withHeaders("username" -> "hatuser", "password" -> "pa55w0rd")

      val authController = application.injector.instanceOf[Authentication]

      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[SystemStatus]

      val result = for {
        _ <- authController.accessToken().apply(authRequest)
        // login twice - the second login is considered "current", not previous
        _ <- authController.accessToken().apply(authRequest)
        r <- controller.status().apply(request)
      } yield r

      status(result) must equalTo(OK)
      val stats = contentAsJson(result).as[List[HatStatus]]

      stats.length must be greaterThan 0
      stats.find(_.title == "Previous Login").get.kind must be equalTo StatusKind.Text("moments ago", None)
    }
  }
}
