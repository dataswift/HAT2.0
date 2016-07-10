/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
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
 */
package hatdex.hat.api.models

import java.util.UUID

import hatdex.hat.dal.Tables.DataDebitRow
import org.joda.time.LocalDateTime

case class ApiDataDebit(
  key: Option[UUID],
  dateCreated: Option[LocalDateTime],
  lastUpdated: Option[LocalDateTime],
  name: String,
  startDate: LocalDateTime,
  endDate: LocalDateTime,
  rolling: Boolean,
  sell: Boolean,
  price: Float,
  kind: String,
  bundleContextless: Option[ApiBundleContextless],
  bundleContextual: Option[ApiBundleContext])

object ApiDataDebit {
  def fromDbModel(dataDebitRow: DataDebitRow): ApiDataDebit = {
    new ApiDataDebit(Some(dataDebitRow.dataDebitKey), Some(dataDebitRow.dateCreated), Some(dataDebitRow.lastUpdated),
      dataDebitRow.name, dataDebitRow.startDate, dataDebitRow.endDate, dataDebitRow.rolling, dataDebitRow.sellRent,
      dataDebitRow.price, dataDebitRow.kind, None, None)

  }
}

case class ApiDataDebitOut(
  key: Option[UUID],
  dateCreated: Option[LocalDateTime],
  lastUpdated: Option[LocalDateTime],
  name: String,
  startDate: LocalDateTime,
  endDate: LocalDateTime,
  rolling: Boolean,
  sell: Boolean,
  price: Float,
  kind: String,
  bundleContextless: Option[ApiBundleContextlessData],
  bundleContextual: Option[Seq[ApiEntity]])

object ApiDataDebitOut {
  def fromDbModel(
    dataDebitRow: DataDebitRow,
    apiBundleContextlessData: Option[ApiBundleContextlessData],
    apiBundleContextualData: Option[Seq[ApiEntity]]): ApiDataDebitOut = {
    new ApiDataDebitOut(Some(dataDebitRow.dataDebitKey), Some(dataDebitRow.dateCreated), Some(dataDebitRow.lastUpdated),
      dataDebitRow.name, dataDebitRow.startDate, dataDebitRow.endDate, dataDebitRow.rolling, dataDebitRow.sellRent,
      dataDebitRow.price, dataDebitRow.kind, apiBundleContextlessData, apiBundleContextualData)
  }
}