package dalapi.models

import dal.Tables.{DataRecordRow, DataTableRow, DataValueRow, DataFieldRow}
import org.joda.time.LocalDateTime

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

case class ApiDataField(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    tableId: Int,
    name: String,
    values: Option[Seq[ApiDataValue]])

object ApiDataField {
  def fromDataField(field: DataFieldRow) = {
    new ApiDataField(
      Some(field.id), Some(field.dateCreated), Some(field.lastUpdated),
      field.tableIdFk, field.name, None
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
    fieldId: Int,
    recordId: Int)

object ApiDataValue {
  def fromDataValue(value: DataValueRow) = {
    new ApiDataValue(
      Some(value.id), Some(value.dateCreated), Some(value.lastUpdated),
      value.value, value.fieldId, value.recordId
    )
  }
}
