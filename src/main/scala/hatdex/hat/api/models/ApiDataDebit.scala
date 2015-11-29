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
    price: Double,
    kind: String,
    bundleContextless: Option[ApiBundleContextlessData],
    bundleContextual: Option[Seq[ApiEntity]])

object ApiDataDebitOut {
  def fromDbModel(dataDebitRow: DataDebitRow, apiBundleContextlessData: Option[ApiBundleContextlessData],
                  apiBundleContextualData: Option[Seq[ApiEntity]]): ApiDataDebitOut = {
    new ApiDataDebitOut(Some(dataDebitRow.dataDebitKey), Some(dataDebitRow.dateCreated), Some(dataDebitRow.lastUpdated),
      dataDebitRow.name, dataDebitRow.startDate, dataDebitRow.endDate, dataDebitRow.rolling, dataDebitRow.sellRent,
      dataDebitRow.price, dataDebitRow.kind, apiBundleContextlessData, apiBundleContextualData)
  }
}