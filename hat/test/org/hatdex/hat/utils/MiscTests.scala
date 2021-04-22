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

import io.dataswift.test.common.BaseSpec
import play.api.Logger
import org.hatdex.hat.phata.models.{ ApiVerificationCompletionRequest, ApiVerificationRequest, HattersClaimPayload }

class HatClaimRedactionTests extends BaseSpec {

  val logger: Logger                                 = Logger(this.getClass)
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  "HatClaim model objects" should "redact email and password when .toString is called" in {
    val apiVerificationRequest =
      new ApiVerificationRequest("AppId", "test@dataswift.io", "https://localhost:3000")

    val apiVerificationCompletionRequest =
      new ApiVerificationCompletionRequest("test@dataswift.io",
                                           termsAgreed = false,
                                           optins = Array.empty,
                                           "hatName",
                                           "hatCluster",
                                           "password"
      )

    val hattersClaimPayload =
      new HattersClaimPayload("test@dataswift.io",
                              termsAgreed = false,
                              sandbox = false,
                              "platform",
                              newsletterOptin = None,
                              "hatName",
                              "hatCluster"
      )

    // logger call invoke toString on case classes, etc.
    apiVerificationRequest.toString() must include("email:REDACTED")
    apiVerificationCompletionRequest.toString() must include("email:REDACTED")
    apiVerificationCompletionRequest.toString() must include("password:REDACTED")
    hattersClaimPayload.toString() must include("email:REDACTED")
  }
}
