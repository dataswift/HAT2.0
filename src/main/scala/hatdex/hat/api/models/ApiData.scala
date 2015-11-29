package hatdex.hat.api.models

import hatdex.hat.dal.Tables.{DataFieldRow, DataRecordRow, DataTableRow, DataValueRow}
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
      Some(field.tableIdFk), field.name, None
    )
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
      record.name, tables
    )
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
      subTables
    )
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
      Some(value.id), Some(value.dateCreated), Some(value.lastUpdated),
      value.value, None, None
    )
  }
  // Or construct the value object with value, field and record info (e.g. inbound, when determining where to insert)
  def fromDataValue(value: DataValueRow, maybeField: Option[DataFieldRow], maybeRecord: Option[DataRecordRow]): ApiDataValue = {
    val maybeApiRecord = maybeRecord map { record =>
      ApiDataRecord.fromDataRecord(record)(None)
    }
    ApiDataValue(Some(value.id), Some(value.dateCreated), Some(value.lastUpdated),
      value.value, maybeField.map(ApiDataField.fromDataField), maybeApiRecord)
  }

  def fromDataValueApi(value: DataValueRow, maybeApiField: Option[ApiDataField], maybeApiRecord: Option[ApiDataRecord]): ApiDataValue = {
    ApiDataValue(Some(value.id), Some(value.dateCreated), Some(value.lastUpdated),
      value.value, maybeApiField, maybeApiRecord)
  }
}

case class ApiRecordValues(
    record: ApiDataRecord,
    values: Seq[ApiDataValue])