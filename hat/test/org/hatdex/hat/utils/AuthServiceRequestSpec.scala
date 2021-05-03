/*
 * Copyright (C) 2020 HAT Data Exchange Ltd
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
 */

package org.hatdex.hat.utils

import eu.timepit.refined.auto._
import io.dataswift.adjudicator.Types.{ DeviceId, HatName }
import io.dataswift.test.common.BaseSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Logger

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import org.hatdex.hat.client.AuthServiceWsClient

class AuthServiceRequestSpec extends BaseSpec with AuthServiceContext {

  val logger: Logger                                 = Logger(this.getClass)
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val hatName: HatName   = HatName("hatName")
  val deviceId: DeviceId = DeviceId("fk-s-blimjfer")

  "The `AuthServiceRequest should`" should "Add a Hat to a Device" in {
    val joinDevice = mockAuthServiceClient.joinDevice("hatName", deviceId)
    val eventuallyJoinDevice = joinDevice.map { response =>
        response match {
          case Left(_)  => false
          case Right(_) => true
        }
      } recover {
          case e =>
            logger.info(s"Exception: ${e.getMessage()}")
            Future.successful(false)
        }

    val result = Await.result(eventuallyJoinDevice, 10.seconds)
    result must equal(true)
  }

  it should "Request a PublicKey" in {
    val getPublicKey =
      mockAuthServiceClient.getPublicKey(hatName, deviceId, "21a3eed7-5d32-46ba-a884-1fdaf7259739")
    val eventuallyPublicKey = getPublicKey.map { response =>
        response match {
          case Left(_)  => false
          case Right(_) => true
        }
      } recover {
          case e =>
            logger.info(s"Exception: ${e.getMessage()}")
            Future.successful(false)
        }

    val result = Await.result(eventuallyPublicKey, 10.seconds)
    result must equal(true)
  }

  it should "Remove a Hat to a Device" in {
    val leaveDevice = mockAuthServiceClient.leaveDevice("hatName", deviceId)
    val eventuallyLeaveDevice = leaveDevice.map { response =>
        response match {
          case Left(_)  => false
          case Right(_) => true
        }
      } recover {
          case e =>
            logger.info(s"Exception: ${e.getMessage()}")
            Future.successful(false)
        }

    val result = Await.result(eventuallyLeaveDevice, 10.seconds)
    result must equal(true)
  }
}

trait AuthServiceContext extends MockitoSugar {
  import org.mockito.ArgumentMatchers.{ any }
  import org.hatdex.hat.client.AuthServiceRequestTypes._
  import org.mockito.Mockito._
  import eu.timepit.refined.auto._

  val fakeDeviceId: DeviceId     = DeviceId("iamafortress")
  val fakePublicKey: Array[Byte] = "publicKey".getBytes()

  val mockAuthServiceClient: AuthServiceWsClient = mock[AuthServiceWsClient]

  // Mocked JoinDevice
  when(
    mockAuthServiceClient
      .joinDevice(any[String], any[DeviceId])
  )
    .thenReturn(Future.successful(Right(DeviceJoined(fakeDeviceId))))

  // Mocked getPublicKey
  when(
    mockAuthServiceClient
      .getPublicKey(any[HatName], any[DeviceId], any[String])
  )
    .thenReturn(Future.successful(Right(PublicKeyReceived(fakePublicKey))))

  // Mocked LeaveDevice
  when(
    mockAuthServiceClient
      .leaveDevice(any[String], any[DeviceId])
  )
    .thenReturn(Future.successful(Right(DeviceLeft(fakeDeviceId))))
}
