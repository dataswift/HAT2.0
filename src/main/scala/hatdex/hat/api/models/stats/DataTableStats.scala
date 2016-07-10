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
package hatdex.hat.api.models.stats

import java.util.UUID

import hatdex.hat.api.models.ApiDataDebit
import hatdex.hat.authentication.models.User
import org.joda.time.LocalDateTime

case class DataTableStats(
  name: String,
  source: String,
  fields: Seq[DataFieldStats],
  subTables: Option[Seq[DataTableStats]],
  valueCount: Int)

case class DataFieldStats(
  name: String,
  tableName: String,
  tableSource: String,
  valueCount: Int)

sealed abstract class DataStats(
  statsType: String,
  time: LocalDateTime,
  dataTableStats: Option[Seq[DataTableStats]],
  logEntry: String)

case class DataDebitStats(
  statsType: String = "datadebit",
  dataDebit: ApiDataDebit,
  operation: String,
  time: LocalDateTime,
  user: User,
  dataTableStats: Option[Seq[DataTableStats]],
  logEntry: String) extends DataStats("datadebit", time, dataTableStats, logEntry)

case class DataCreditStats(
  statsType: String = "datacredit",
  operation: String,
  time: LocalDateTime,
  user: User,
  dataTableStats: Option[Seq[DataTableStats]],
  logEntry: String) extends DataStats("datacredit", time, dataTableStats, logEntry)

case class DataStorageStats(
  statsType: String = "storage",
  time: LocalDateTime,
  dataTableStats: Seq[DataTableStats],
  logEntry: String) extends DataStats("storage", time, Some(dataTableStats), logEntry)
