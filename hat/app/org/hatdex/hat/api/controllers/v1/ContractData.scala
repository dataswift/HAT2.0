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

package org.hatdex.hat.api.controllers.v1

import org.hatdex.hat.api.controllers.common._
import play.api.mvc._

trait ContractData {
  def readContractData(
      namespace: String,
      endpoint: String,
      orderBy: Option[String],
      ordering: Option[String],
      skip: Option[Int],
      take: Option[Int]): Action[ContractDataReadRequest]

  def createContractData(
      namespace: String,
      endpoint: String,
      skipErrors: Option[Boolean]): Action[ContractDataCreateRequest]

  def updateContractData(namespace: String): Action[ContractDataUpdateRequest]
}
