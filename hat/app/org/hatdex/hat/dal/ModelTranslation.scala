package org.hatdex.hat.dal

import org.hatdex.hat.dal.Tables._
import org.hatdex.hat.api.models._
import org.hatdex.hat.authentication.models.HatUser
import play.api.libs.json.Json

object ModelTranslation {
  def fromDbModel(user: UserUserRow): HatUser = {
    HatUser(user.userId, user.email, user.pass, user.name, user.role, user.enabled)
  }

  def fromInternalModel(user: HatUser): User = {
    User(user.userId, user.email, None, user.name, user.role)
  }

  def fromDbModel(field: DataFieldRow) = {
    ApiDataField(
      Some(field.id), Some(field.dateCreated), Some(field.lastUpdated),
      Some(field.tableIdFk), field.name, None)
  }

  def fromDbModel(record: DataRecordRow, tables: Option[Seq[ApiDataTable]]) = {
    new ApiDataRecord(
      Some(record.id), Some(record.dateCreated), Some(record.lastUpdated),
      record.name, tables)
  }

  def fromDbModel(table: DataTableRow, fields: Option[Seq[ApiDataField]], subTables: Option[Seq[ApiDataTable]]) = {
    new ApiDataTable(
      Some(table.id),
      Some(table.dateCreated),
      Some(table.lastUpdated),
      table.name,
      table.sourceName,
      fields,
      subTables)
  }

  def fromDbModel(table: DataTableTreeRow, fields: Option[Seq[ApiDataField]], subTables: Option[Seq[ApiDataTable]]) = {
    new ApiDataTable(
      table.id,
      table.dateCreated,
      table.lastUpdated,
      table.name.getOrElse(""),
      table.sourceName.getOrElse(""),
      fields,
      subTables)
  }

  def fromDbModel(value: DataValueRow): ApiDataValue = {
    ApiDataValue(
      Some(value.id), Some(value.dateCreated), Some(value.lastUpdated),
      value.value, None, None)
  }

  def fromDbModel(value: DataValueRow, field: DataFieldRow, record: DataRecordRow): ApiDataValue = {
    val apiRecord = fromDbModel(record, None)
    ApiDataValue(Some(value.id), Some(value.dateCreated), None,
      value.value, Some(fromDbModel(field)), Some(apiRecord))
  }

  def fromDbModel(value: DataValueRow, maybeApiField: Option[ApiDataField], maybeApiRecord: Option[ApiDataRecord]): ApiDataValue = {
    ApiDataValue(Some(value.id), Some(value.dateCreated), None,
      value.value, maybeApiField, maybeApiRecord)
  }

  def fromDbModel(value: DataStatsLogRow): DataStats = {
    import org.hatdex.hat.api.json.DataStatsFormat.dataStatsFormat
    value.stats.as[DataStats]
  }

  def fromDbModel(bundleContextless: BundleContextlessRow): ApiBundleContextless = {
    new ApiBundleContextless(
      Some(bundleContextless.id),
      Some(bundleContextless.dateCreated), Some(bundleContextless.lastUpdated),
      bundleContextless.name, None)
  }

  def fromDbModel(bundleContextless: BundleContextlessRow, sources: Option[Seq[ApiBundleDataSourceStructure]]): ApiBundleContextless = {
    ApiBundleContextless(
      Some(bundleContextless.id),
      Some(bundleContextless.dateCreated), Some(bundleContextless.lastUpdated),
      bundleContextless.name, sources)
  }

  def fromDbModel(dataDebitRow: DataDebitRow): ApiDataDebit = {
    ApiDataDebit(Some(dataDebitRow.dataDebitKey), Some(dataDebitRow.dateCreated), Some(dataDebitRow.lastUpdated),
      dataDebitRow.name, dataDebitRow.startDate, dataDebitRow.endDate, Some(dataDebitRow.enabled), dataDebitRow.rolling, dataDebitRow.sellRent,
      dataDebitRow.price, dataDebitRow.kind, None, None)
  }

  def fromDbModel(
    dataDebitRow: DataDebitRow,
    apiBundleContextlessData: Option[ApiBundleContextlessData],
    apiBundleContextualData: Option[Seq[ApiEntity]]): ApiDataDebitOut = {
    ApiDataDebitOut(Some(dataDebitRow.dataDebitKey), Some(dataDebitRow.dateCreated), Some(dataDebitRow.lastUpdated),
      dataDebitRow.name, dataDebitRow.startDate, dataDebitRow.endDate, Some(dataDebitRow.enabled), dataDebitRow.rolling, dataDebitRow.sellRent,
      dataDebitRow.price, dataDebitRow.kind, apiBundleContextlessData, apiBundleContextualData)
  }
}
