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

import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

case class ApiDataField(
  id: Option[Int],
  dateCreated: Option[LocalDateTime],
  lastUpdated: Option[LocalDateTime],
  tableId: Option[Int],
  name: String,
  values: Option[Seq[ApiDataValue]])

object ApiDataField {
  def fromDataField(field: DataFieldRow) = {
    ApiDataField(
      Some(field.id), Some(field.dateCreated), Some(field.lastUpdated),
      Some(field.tableIdFk), field.name, None)
  }
}

case class ApiDataRecord(
  id: Option[Int],
  dateCreated: Option[LocalDateTime],
  lastUpdated: Option[LocalDateTime],
  name: String,
  tables: Option[Seq[ApiDataTable]])

object ApiDataRecord {
  def fromDataRecord(record: DataRecordRow)(tables: Option[Seq[ApiDataTable]]) = {
    new ApiDataRecord(
      Some(record.id), Some(record.dateCreated), Some(record.lastUpdated),
      record.name, tables)
  }
}

case class ApiDataTable(
  id: Option[Int],
  dateCreated: Option[LocalDateTime],
  lastUpdated: Option[LocalDateTime],
  name: String,
  source: String,
  fields: Option[Seq[ApiDataField]],
  subTables: Option[Seq[ApiDataTable]])

object ApiDataTable {
  def fromDataTable(table: DataTableRow)(fields: Option[Seq[ApiDataField]])(subTables: Option[Seq[ApiDataTable]]) = {
    new ApiDataTable(
      Some(table.id),
      Some(table.dateCreated),
      Some(table.lastUpdated),
      table.name,
      table.sourceName,
      fields,
      subTables)
  }

  def fromNestedTable(table: DataTableTreeRow)(fields: Option[Seq[ApiDataField]])(subTables: Option[Seq[ApiDataTable]]) = {
    new ApiDataTable(
      table.id,
      table.dateCreated,
      table.lastUpdated,
      table.name.getOrElse(""),
      table.sourceName.getOrElse(""),
      fields,
      subTables)
  }
}

case class ApiDataValue(
  id: Option[Int],
  dateCreated: Option[LocalDateTime],
  lastUpdated: Option[LocalDateTime],
  value: String,
  field: Option[ApiDataField],
  record: Option[ApiDataRecord])

object ApiDataValue {
  // Can construct the value object with only value info (e.g. outbound, when part of record and field anyway)
  def fromDataValue(value: DataValueRow): ApiDataValue = {
    ApiDataValue(
      Some(value.id), Some(value.dateCreated), None,
      value.value, None, None)
  }
  // Or construct the value object with value, field and record info (e.g. inbound, when determining where to insert)
  def fromDataValue(value: DataValueRow, maybeField: Option[DataFieldRow], maybeRecord: Option[DataRecordRow]): ApiDataValue = {
    val maybeApiRecord = maybeRecord map { record =>
      ApiDataRecord.fromDataRecord(record)(None)
    }
    ApiDataValue(Some(value.id), Some(value.dateCreated), None,
      value.value, maybeField.map(ApiDataField.fromDataField), maybeApiRecord)
  }

  def fromDataValueApi(value: DataValueRow, maybeApiField: Option[ApiDataField], maybeApiRecord: Option[ApiDataRecord]): ApiDataValue = {
    ApiDataValue(Some(value.id), Some(value.dateCreated), None,
      value.value, maybeApiField, maybeApiRecord)
  }
}

case class ApiRecordValues(
  record: ApiDataRecord,
  values: Seq[ApiDataValue])