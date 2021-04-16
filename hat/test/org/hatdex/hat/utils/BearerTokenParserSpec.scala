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
 * Written by Tyler Weir <tyler.weir@dataswift.io>
 * 1 / 2021
 */
package org.hatdex.hat.utils

import io.dataswift.test.common.BaseSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Logger
import org.hatdex.hat.BearerTokenParser._

class BearerTokenParserSpec extends BaseSpec with MockitoSugar {

  val logger: Logger = Logger(this.getClass)

  "The Bearer token parser" should "parse headers correctly" in {
    BearerTokenParser.parseToken("Bearer fff") must equal(Some("fff"))
    BearerTokenParser.parseToken("Bearer fff\" ") must equal(Some("fff"))
    BearerTokenParser.parseToken("Beare fff\" ") must equal(None)
    BearerTokenParser.parseToken("") must equal(None)
    BearerTokenParser.parseToken(" ") must equal(None)
  }
}
