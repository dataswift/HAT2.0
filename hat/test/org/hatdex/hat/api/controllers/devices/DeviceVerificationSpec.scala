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
 * Written by Tyler Weir <tyler.weir@dataswift.io>
 * 4 / 2021
 */

package org.hatdex.hat.api.controllers.devices

import org.hatdex.hat.api.controllers.devices.DeviceVerification
import org.hatdex.hat.fixture.DeviceVerificationFixture

import scala.concurrent.Await
import scala.concurrent.duration._

class DeviceVerificationSpec extends DeviceVerificationFixture {
  val deviceVerification = injector.instanceOf[DeviceVerification]

  "The getTokenFromHeaders" should "find an SLTokenBody" in {
    import org.hatdex.hat.api.controllers.MachineData.SLTokenBody

    val headers = play.api.mvc.Headers(
      ("X-Auth-Token",
       "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiIsImtpZCI6IjgzNDg0NTNiLWM0ZWYtNDczYS05ODhiLWY2NmFiZWFjYmZkMyJ9.eyJpc3MiOiJkYXRhc3dpZnRhZGp1ZGljYXRpb24iLCJleHAiOjE2MTg1ODMyNDQsCiAgImRldmljZUlkIiA6ICJzYW1wbGVkZXZpY2UiCn0.A0A1YR59Xatd8sudFFsRVZDSadPfC0Sm0c1OecMsyTudshXLb3Zg36eEIrZMRgh-M9Br30Ybr6X1ZrG9cdv5r-BkfPGaR9lGBFmP5GDkag5_LhpJvUyieucqQj2cyrNfGp1EiSeEwZtLHI_WvJhFH6LeGpMXk08DxOE_O6KA1RE"
      )
    )

    val ret = Await.result(deviceVerification.getTokenFromHeaders(headers), 2.seconds)

    ret must equal(
      Some(SLTokenBody("dataswiftadjudication", 1618583244, "sampledevice"))
    )
  }

  it should "gracefully fail on a bad bearer" in {
    val headers = play.api.mvc.Headers(
      ("X-Auth-Token", "garbage")
    )

    val ret = Await.result(deviceVerification.getTokenFromHeaders(headers), 2.seconds)
    ret must equal(None)
  }
}
