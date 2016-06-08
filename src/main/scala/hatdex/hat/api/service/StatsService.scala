package hatdex.hat.api.service

import akka.event.{ Logging, LoggingAdapter }
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import hatdex.hat.api.models.stats.{ DataDebitStats, DataTableStats, DataFieldStats }

import scala.collection.immutable.HashMap

//import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import hatdex.hat.authentication.models.User
import hatdex.hat.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait StatsService {

  def recordDataDebitOperation(
    dd: ApiDataDebit,
    user: User,
    operationType: DataDebitOperations.DataDebitOperation,
    logEntry: String): Future[Int] = {
    val ddOperation = StatsDataDebitOperationRow(0, LocalDateTime.now(), dd.key.get, user.userId, operationType.toString)
    DatabaseInfo.db.run {
      StatsDataDebitOperation += ddOperation
    } andThen {
      case _ =>
        DataDebitStats(dd, operationType.toString, ddOperation.dateCreated, None)
    }
  }

  def recordDataDebitRetrieval(
    dd: ApiDataDebit,
    values: Seq[ApiEntity],
    user: User,
    logEntry: String): Future[Unit] = {
    val ddOperation = StatsDataDebitOperationRow(0, LocalDateTime.now(), dd.key.get, user.userId, DataDebitOperations.GetValues().toString)
    val fOperation = DatabaseInfo.db.run {
      (StatsDataDebitOperation returning StatsDataDebitOperation) += ddOperation
    } andThen {
      case _ =>
        DataDebitStats(dd, ddOperation.operation, ddOperation.dateCreated, None)
    }
    fOperation.map { o =>
      ()
    }
  }

  def recordDataDebitRetrieval(
    dd: ApiDataDebit,
    values: ApiDataDebitOut,
    user: User,
    logEntry: String): Future[Unit] = {
    val ddOperation = StatsDataDebitOperationRow(0, LocalDateTime.now(), dd.key.get, user.userId, DataDebitOperations.GetValues().toString)
    values.bundleContextless.map { bundle =>
      val (totalBundleRecords, bundleTableStats, tableValueStats, fieldValueStats) = getBundleStats(bundle)
      storeBundleStats(ddOperation, totalBundleRecords, bundleTableStats, tableValueStats, fieldValueStats)
        .andThen {
          case _ =>
            val stats = convertBundleStats(tableValueStats, fieldValueStats)
            DataDebitStats(dd, ddOperation.operation, ddOperation.dateCreated, Some(stats))
        }
    } getOrElse {
      // Not contextless bundle info, do nothing
      Future {}
    }
  }

  def convertBundleStats(tableValueStats: HashMap[ApiDataTable, Int], fieldValueStats: HashMap[ApiDataField, Int]): Seq[DataTableStats] = {
    val stats = tableValueStats.map {
      case (table, values) =>
        val matchingFields = fieldValueStats.filter(_._1.tableId == table.id)
          .map {
            case (field, fieldValues) =>
              DataFieldStats(field.name, table.name, table.source, fieldValues)
          }

        DataTableStats(table.name, table.source, matchingFields.toSeq, None, values)
    }
    stats.toSeq
  }

  def getBundleStats(bundle: ApiBundleContextlessData): (Int, Map[ApiBundleTable, Int], HashMap[ApiDataTable, Int], HashMap[ApiDataField, Int]) = {
    val groupStats = bundle.dataGroups.flatMap { group => // Only one group until Joins come in
      group.map {
        case (groupName: String, bundleTable: ApiBundleTable) =>
          val bundleTableRecords = getBundleTableRecordCount(bundleTable)
          val aggregateTableValueCounts = getTableValueCounts(bundleTable)
          val aggregateFieldValueCounts = getFieldValueCounts(bundleTable)

          (bundleTableRecords, aggregateTableValueCounts, aggregateFieldValueCounts)
      }
    }
    val bundleTableRecordCounts = groupStats.map(_._1)
    val tableValueCounts = groupStats.map(_._2)
    val fieldValueCounts = groupStats.map(_._3)

    val bundleTableStats = bundleTableRecordCounts.groupBy(_._1).map {
      case (bundleTable, aggBundleTableCounts) =>
        bundleTable -> aggBundleTableCounts.map(_._2).sum
    }

    val tableValueStats = Utils.mergeMap(tableValueCounts)((v1, v2) => v1 + v2)
    val fieldValueStats = Utils.mergeMap(fieldValueCounts)((v1, v2) => v1 + v2)

    val totalBundleRecords = bundleTableStats.values.sum

    (totalBundleRecords, bundleTableStats, tableValueStats, fieldValueStats)
  }

  def storeBundleStats(
    ddOperation: StatsDataDebitOperationRow,
    totalBundleRecords: Int,
    bundleTableStats: Map[ApiBundleTable, Int],
    tableValueStats: HashMap[ApiDataTable, Int],
    fieldValueStats: HashMap[ApiDataField, Int]): Future[Unit] = {

    val fOperation = DatabaseInfo.db.run {
      (StatsDataDebitOperation returning StatsDataDebitOperation) += ddOperation
    }
    val fStatements = fOperation.map { o =>
      val dbTableStatsInsert = StatsDataDebitDataTableAccess ++=
        tableValueStats map {
          case (table, valueCount) =>
            StatsDataDebitDataTableAccessRow(0, o.dateCreated, table.id.get, o.recordId, valueCount)
        }

      val dbFieldStatsInsert = StatsDataDebitDataFieldAccess ++=
        fieldValueStats map {
          case (field, valueCount) =>
            StatsDataDebitDataFieldAccessRow(0, o.dateCreated, field.id.get, o.recordId, valueCount)
        }

      val ddRecordCountInsert = StatsDataDebitRecordCount +=
        StatsDataDebitRecordCountRow(0, o.dateCreated, o.recordId, totalBundleRecords)

      val dbBundleTableStatsInsert = StatsDataDebitClessBundleRecords ++=
        bundleTableStats map {
          case (bundleTable, recordCount) =>
            StatsDataDebitClessBundleRecordsRow(0, o.dateCreated, bundleTable.id.get, o.recordId, recordCount)
        }

      DBIO.seq(
        dbTableStatsInsert,
        dbFieldStatsInsert,
        dbBundleTableStatsInsert,
        ddRecordCountInsert)
    }

    fStatements.flatMap { statements =>
      DatabaseInfo.db.run(statements)
    }
  }

  def getBundleTableRecordCount(bundleTable: ApiBundleTable) = {
    (bundleTable, bundleTable.data.map(data => data.length).getOrElse(0))
  }

  def getTableValueCounts(bundleTable: ApiBundleTable): HashMap[ApiDataTable, Int] = {
    // Each group consists of a sequence of data records
    val tableValueCounts = bundleTable.data.getOrElse(Seq()).flatMap { record: ApiDataRecord =>
      record.tables.getOrElse(Seq()).map(countTableValues)
    }
    Utils.mergeMap(tableValueCounts)((v1, v2) => v1 + v2)
  }

  def getFieldValueCounts(bundleTable: ApiBundleTable): HashMap[ApiDataField, Int] = {
    // We also want to know what Data Attributes were retrieved, and how many items each of them contained
    val fieldValueCounts = bundleTable.data.getOrElse(Seq()).flatMap { record: ApiDataRecord =>
      record.tables.getOrElse(Seq()).map(countFieldValues)
    }
    Utils.mergeMap(fieldValueCounts)((v1, v2) => v1 + v2)
  }

  private def countTableValues(dataTable: ApiDataTable): HashMap[ApiDataTable, Int] = {
    val valueCount = countFieldValues(dataTable).values.sum
    val tableValueCount = HashMap(
      dataTable.copy(fields = None, subTables = None) -> valueCount)

    val counts = dataTable.subTables.getOrElse(Seq()).map(countTableValues)
    Utils.mergeMap(counts :+ tableValueCount)((v1, v2) => v1 + v2)
  }

  private def countFieldValues(dataTable: ApiDataTable): HashMap[ApiDataField, Int] = {
    val fieldCounts = HashMap(dataTable.fields.getOrElse(Seq()) map { field => // For all fields
      val fieldValues = field.values.map(_.length).getOrElse(0) // Count the number of values or 0 if no values
      val fieldUniform = field.copy(values = None)
      fieldUniform -> fieldValues
    }: _*)
    val counts = dataTable.subTables.getOrElse(Seq()).map(countFieldValues)
    Utils.mergeMap(counts :+ fieldCounts)((v1, v2) => v1 + v2)
  }
}

object DataDebitOperations {
  sealed trait DataDebitOperation
  case class Create() extends DataDebitOperation
  case class Enable() extends DataDebitOperation
  case class Disable() extends DataDebitOperation
  case class GetValues() extends DataDebitOperation
}

