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
import io.dataswift.adjudicator.Types.{ ContractId, HatName }
import io.dataswift.test.common.BaseSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Logger

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }

class AdjudicatorRequestSpec extends BaseSpec with AdjudicatorContext {

  val logger: Logger                                 = Logger(this.getClass)
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val hatName: HatName       = HatName("hatName")
  val contractId: ContractId = ContractId(UUID.fromString("21a3eed7-5d32-46ba-a884-1fdaf7259739"))

  "The `AdjudicatorRequest should`" should "Add a Hat to a Contract" in {
    val joinContract = mockAdjudicatorClient.joinContract("hatName", contractId)
    val eventuallyJoinContract = joinContract.map { response =>
        response match {
          case Left(_)  => false
          case Right(_) => true
        }
      } recover {
          case e =>
            logger.info(s"Exception: ${e.getMessage()}")
            Future.successful(false)
        }

    val result = Await.result(eventuallyJoinContract, 10.seconds)
    result must equal(true)
  }

  it should "Request a PublicKey" in {
    val getPublicKey =
      mockAdjudicatorClient.getPublicKey(hatName, contractId, "21a3eed7-5d32-46ba-a884-1fdaf7259739")
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

  it should "Remove a Hat to a Contract" in {
    val leaveContract = mockAdjudicatorClient.leaveContract("hatName", contractId)
    val eventuallyLeaveContract = leaveContract.map { response =>
        response match {
          case Left(_)  => false
          case Right(_) => true
        }
      } recover {
          case e =>
            logger.info(s"Exception: ${e.getMessage()}")
            Future.successful(false)
        }

    val result = Await.result(eventuallyLeaveContract, 10.seconds)
    result must equal(true)
  }
}

trait AdjudicatorContext extends MockitoSugar {
  import org.mockito.ArgumentMatchers.{ any }
  import org.hatdex.hat.utils.AdjudicatorRequestTypes._
  import org.mockito.Mockito._

  val fakeContractUUID: UUID     = java.util.UUID.randomUUID()
  val fakePublicKey: Array[Byte] = "publicKey".getBytes()

  val mockAdjudicatorClient: AdjudicatorRequest = MockitoSugar.mock[AdjudicatorRequest]

  // Mocked JoinContract
  when(
    mockAdjudicatorClient
      .joinContract(any[String], any[ContractId])(any[ExecutionContext])
  )
    .thenReturn(Future.successful(Right(ContractJoined(ContractId(fakeContractUUID)))))

  // Mocked getPublicKey
  when(
    mockAdjudicatorClient
      .getPublicKey(any[HatName], any[ContractId], any[String])(any[ExecutionContext])
  )
    .thenReturn(Future.successful(Right(PublicKeyReceived(fakePublicKey))))

  // Mocked LeaveContract
  when(
    mockAdjudicatorClient
      .leaveContract(any[String], any[ContractId])(any[ExecutionContext])
  )
    .thenReturn(Future.successful(Right(ContractLeft(ContractId(fakeContractUUID)))))
}
