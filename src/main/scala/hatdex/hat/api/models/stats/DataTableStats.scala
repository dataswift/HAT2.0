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
