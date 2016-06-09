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

case class DataDebitStats(
  dataDebit: ApiDataDebit,
  operation: String,
  time: LocalDateTime,
  user: User,
  dataTableStats: Option[Seq[DataTableStats]],
  logEntry: String
)