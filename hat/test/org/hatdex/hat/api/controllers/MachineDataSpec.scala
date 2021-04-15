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
 * 5 / 2017
 */

package org.hatdex.hat.api.controllers

import org.hatdex.hat.api.HATTestContext
import play.api.Logger
import play.api.libs.json.{ JsValue, Json }
import play.api.test.Helpers._
import play.api.test.{ FakeRequest, Helpers }

import scala.concurrent.Await
import scala.concurrent.duration._

class MachineDataSpec extends MachineDataContext {
  import scala.concurrent.ExecutionContext.Implicits.global

  val logger: Logger = Logger(this.getClass)
  val postRequest = FakeRequest("POST", "http://hat.hubofallthings.net")
    .withJsonBody(emptyRequestBody)

  val getRequest = FakeRequest("POST", "http://hat.hubofallthings.net")

  "The Save Machine method" should "Return 400 on an empty request" in {

    val controller = application.injector.instanceOf[MachineData]

    val response = for {
      _ <- Helpers.call(controller.createData("samplemachine", "testendpoint", None), postRequest)
      r <- Helpers.call(controller.getData("samplemachine", "testendpoint", None, None, None, None), getRequest)
    } yield r

    val res = Await.result(response, 5.seconds)
    res.header.status must equal(400)
  }

  "The Read Machine Data method" should "Return 400 on an empty request" in {
    val controller = application.injector.instanceOf[MachineData]

    val response =
      Helpers.call(controller.getData("samplemachine", "testendpoint", None, None, None, None), getRequest)

    val res = Await.result(response, 5.seconds)
    res.header.status must equal(400)
  }

  "The Update Machine Data method" should "Return 400 on an empty request" in {
    val controller = application.injector.instanceOf[MachineData]

    val response =
      Helpers.call(controller.getData("samplemachine", "testendpoint", None, None, None, None), getRequest)

    val res = Await.result(response, 5.seconds)
    res.header.status must equal(400)
  }
}

class MachineDataContext extends HATTestContext {
  val emptyRequestBody: JsValue = Json.parse("""{"body":""}""")
}
