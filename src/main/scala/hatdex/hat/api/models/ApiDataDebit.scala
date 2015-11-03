package hatdex.hat.api.models

import java.util.UUID

import hatdex.hat.dal.Tables.DataDebitRow
import org.joda.time.LocalDateTime

case class ApiDataDebit(
    dataDebitKey: Option[UUID],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    rolling: Boolean,
    sellRent: Boolean,
    price: Float,
    kind: String,
    bundleContextless: Option[ApiBundleContextless],
    bundleContextual: Option[String])

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
    start_date: LocalDateTime,
    end_date: LocalDateTime,
    rolling: Boolean,
    sell: Boolean,
    price: Double,
    kind: String,
    bundleContextless: Option[ApiBundleContextlessData],
    bundleContextual: Option[String])

object ApiDataDebitOut {
  def fromDbModel(dataDebitRow: DataDebitRow, apiBundleContextlessData: Option[ApiBundleContextlessData],
                  apiBundleContextualData: Option[String]): ApiDataDebitOut = {
    new ApiDataDebitOut(Some(dataDebitRow.dataDebitKey), Some(dataDebitRow.dateCreated), Some(dataDebitRow.lastUpdated),
      dataDebitRow.name, dataDebitRow.startDate, dataDebitRow.endDate, dataDebitRow.rolling, dataDebitRow.sellRent,
      dataDebitRow.price, dataDebitRow.kind, apiBundleContextlessData, apiBundleContextualData)
  }
}
