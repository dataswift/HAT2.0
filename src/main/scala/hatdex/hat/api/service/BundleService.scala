package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.Utils
import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.util.{Failure, Success, Try}

// this trait defines our service behavior independently from the service actor
trait BundleService extends DataService {

  val logger: LoggingAdapter

  protected[api] def getBundleTableValues(bundleTableId: Int)(implicit session: Session): Option[ApiBundleTable] = {
    val bundleDataTableQuery = for {
      bundleTable <- BundleContextlessTable.filter(_.id === bundleTableId)
      dataTable <- bundleTable.dataTableFk
    } yield (bundleTable, dataTable)

    val maybeBundleDataTable = Try(bundleDataTableQuery.run.headOption)

    maybeBundleDataTable match {
      case Success(bundleDataTable) =>

        bundleDataTable map { case (bundleTable: BundleContextlessTableRow, dataTable: DataTableRow) =>
          val tableId = dataTable.id
          val slices = getBundleTableSlices(bundleTableId)
          val apiDataTables = slices match {
            // Without any slices, the case degrades to plain data table access
            case None =>
              getTableValues(tableId)
            case Some(tableSlices) =>
              // For each table slice, get a query that filters only the records that match the slice conditions
              val recordLists = tableSlices map { slice =>
                slice.conditions.foldLeft(DataValue.flatMap(_.dataRecordFk).map(_.id)) { (sliceRecordSet, condition) =>
                  // Filter only the records that have been included so far
                  val fieldMatcher = DataValue.filter(_.recordId in sliceRecordSet)
                    .filter(_.fieldId === condition.field.id) // Then for matching field

                  // And maching condition value
                  val valueMatcher = condition.operator match {
                    case ComparisonOperators.equal =>
                      fieldMatcher.filter(_.value === condition.value)
                    case ComparisonOperators.notEqual =>
                      fieldMatcher.filterNot(_.value === condition.value)
                    case ComparisonOperators.greaterThan =>
                      fieldMatcher.filter(_.value > condition.value)
                    case ComparisonOperators.lessThan =>
                      fieldMatcher.filter(_.value < condition.value)
                    case ComparisonOperators.like =>
                      fieldMatcher.filter(_.value like condition.value)
                    // FIXME: handle date-related operators
                    case _ =>
                      fieldMatcher.filter(_.value === condition.value)
                  }

                  valueMatcher.flatMap(_.dataRecordFk) // Extract data record IDs
                     .map(_.id)
                }
              }

              // Union all records of the different slices
              val records = recordLists.tail.foldLeft(recordLists.head) { (recordUnion, recordSet) =>
                recordUnion ++ recordSet
              }

              // Query that takes only the records of interest
              val values = DataValue.filter(_.recordId in records)

              getTableValues(tableId, values)
          }

          val emptyBundleTable = ApiBundleTable.fromBundleTable(bundleTable)(ApiDataTable.fromDataTable(dataTable)(None)(None))
          emptyBundleTable.copy(data = apiDataTables)
        }
      case Failure(e) =>
        print("Failure getting bundle data: " + e.getMessage)
        None
    }

  }


  protected[api] def getBundleContextlessValues(bundleId: Int)(implicit session: Session): Option[ApiBundleContextlessData] = {
    // TODO: include join fields
    val bundleQuery = for {
      bundle <- BundleContextless.filter(_.id === bundleId) // 1 or 0
      combination <- BundleContextlessJoin.filter(_.bundleContextlessId === bundleId) // N for each bundle
      table <- combination.bundleContextlessTableFk // 1 for each combination
    } yield (bundle, combination, table)

    val maybeBundle = Try(bundleQuery.run)
    maybeBundle match {
      case Success(bundle) =>
        // Only one or no contextless bundles
        val contextlessBundle = bundle.headOption.map(_._1)

        contextlessBundle map { cBundle =>
          val bundleData = bundle.groupBy(_._2.name).map { case (combinationName, tableGroup) =>
            val bundleData = getBundleTableValues(tableGroup.map(_._3.id).head).get
            (combinationName, bundleData)
          }

          // TODO: rearrange data as per join fields
          val dataGroups = Iterable(bundleData)

          ApiBundleContextlessData.fromDbModel(cBundle, dataGroups)
        }
      case Failure(e) =>
        print(s"Error getting bundle: ${e.getMessage}\n")
        e.printStackTrace()
        None
    }
  }

  /*
   * Stores bundle table provided from the incoming API call
   */
  protected[api] def storeBundleTable(bundleTable: ApiBundleTable)(implicit session: Session): Try[ApiBundleTable] = {
    // Require the bundle to be based on a data table that already exists
    bundleTable.table.id match {
      case Some(tableId) =>
        val bundleTableRow = new BundleContextlessTableRow(0, LocalDateTime.now(), LocalDateTime.now(), bundleTable.name, tableId)

        // Using Try to handle errors
        Try((BundleContextlessTable returning BundleContextlessTable) += bundleTableRow) flatMap { insertedBundleTable =>
          // Convert from database format to API format
          val insertedApiBundleTable = ApiBundleTable.fromBundleTable(insertedBundleTable)(bundleTable.table)
          // A partial function to store all table slices related to this bundle table
          def storeBundleSlice = storeSlice(insertedApiBundleTable) _

          val apiSlices = bundleTable.slices map { tableSlices =>
            val slices = tableSlices.map(storeBundleSlice)
            // Flattens to a simple Try with list if slices or returns the first error that occurred
            Utils.flatten(slices)
          }

          apiSlices match {
            case Some(Success(slices)) =>
              Success(insertedApiBundleTable.copy(slices = Some(slices)))
            case Some(Failure(e)) =>
              Failure(e)
            case None =>
              // Bundle Table with no slicing
              Success(insertedApiBundleTable)
          }

        }
      case None =>
        Failure(new IllegalArgumentException("Table provided for bundling must contain ID"))
    }

  }

  protected[api] def getBundleTableById(bundleTableId: Int)(implicit session: Session): Option[ApiBundleTable] = {
    // Traversing entity graph the Slick way
    val tableQuery = for {
      bundleTable <- BundleContextlessTable.filter(_.id === bundleTableId)
      dataTable <- bundleTable.dataTableFk
    } yield (bundleTable, dataTable)

    val table = tableQuery.run.headOption

    // Map back from database types to API ones
    table map {
      case (bundleTable: BundleContextlessTableRow, dataTable: DataTableRow) =>
        val apiDataTable = ApiDataTable.fromDataTable(dataTable)(None)(None)
        val apiBundleTable = ApiBundleTable.fromBundleTable(bundleTable)(apiDataTable)
        val slices = apiBundleTable.id.flatMap(getBundleTableSlices)
        apiBundleTable.copy(slices = slices)
    }
  }

  protected[api] def storeSlice(bundleTable: ApiBundleTable)
                               (slice: ApiBundleTableSlice)
                               (implicit session: Session): Try[ApiBundleTableSlice] = {

    (bundleTable.table.id, bundleTable.id, slice.table.id) match {
      case (None, _, _) =>
        Failure(new IllegalArgumentException("Table provided for bundling must contain ID"))

      case (slice.table.id, Some(bundleTableId), Some(sliceTableId)) =>

        val sliceRow = new BundleContextlessTableSliceRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          bundleTableId, sliceTableId)

        Try((BundleContextlessTableSlice returning BundleContextlessTableSlice) += sliceRow) flatMap { insertedSlice =>
          val insertedApiBundleTableSlice = ApiBundleTableSlice.fromBundleTableSlice(insertedSlice)(slice.table)

          def storeSliceCondition = storeCondition(insertedApiBundleTableSlice) _
          val conditions = slice.conditions.map(storeSliceCondition)

          // If all conditions have been inserted successfully,
          // Return the complete ApiBundleTableSlice object;
          // Otherwise, the first error that occurred
          Utils.flatten(conditions) map { apiConditions =>
            insertedApiBundleTableSlice.copy(conditions = apiConditions)
          }
        }

      case _ =>
        Failure(new IllegalArgumentException("Bundle table must refer to the same table as each slice of the table"))
    }

  }

  protected[api] def getBundleTableSlices(bundleTableId: Int)
                                         (implicit session: Session): Option[Seq[ApiBundleTableSlice]] = {

    // Traversing entity graph the Slick way
    val slicesQuery = for {
      slice <- BundleContextlessTableSlice.filter(_.bundleContextlessTableId === bundleTableId)
      dataTable <- slice.dataTableFk
    } yield (slice, dataTable)

    val slices = slicesQuery.run

    val result = slices flatMap { case (slice: BundleContextlessTableSliceRow, dataTable: DataTableRow) =>
      // Returning the Data Table information of the slice without subTables or fields
      val apiDataTable = ApiDataTable.fromDataTable(dataTable)(None)(None)
      val apiSlice = ApiBundleTableSlice.fromBundleTableSlice(slice)(apiDataTable)
      apiSlice.id.map(getSliceConditions) map { conditions =>
        apiSlice.copy(conditions = conditions)
      }
    }
    seqOption(result)
  }

  protected[api] def storeCondition(bundleTableSlice: ApiBundleTableSlice)
                                   (condition: ApiBundleTableCondition)
                                   (implicit session: Session): Try[ApiBundleTableCondition] = {

    (bundleTableSlice.table.id, condition.field.id, bundleTableSlice.id) match {
      case (condition.field.tableId, Some(fieldId), Some(sliceId)) =>
        val conditionRow = new BundleContextlessTableSliceConditionRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          fieldId, sliceId, condition.operator.toString, condition.value)

        Try((BundleContextlessTableSliceCondition returning BundleContextlessTableSliceCondition) += conditionRow) map { insertedCondition =>
          ApiBundleTableCondition.fromBundleTableSliceCondition(insertedCondition)(condition.field)
        }

      case _ =>
        Failure(new IllegalArgumentException("Invalid table slice or table field provided"))

    }
  }

  protected[api] def getSliceConditions(tableSliceId: Int)
                                       (implicit session: Session): Seq[ApiBundleTableCondition] = {
    // Traversing entity graph the Slick way
    val conditionsQuery = for {
      c <- BundleContextlessTableSliceCondition.filter(_.tableSliceId === tableSliceId)
      f <- c.dataFieldFk
    } yield (c, f)

    val conditions = conditionsQuery.run
    conditions map { case (condition: BundleContextlessTableSliceConditionRow, field: DataFieldRow) =>
      val apiField = ApiDataField.fromDataField(field)
      ApiBundleTableCondition.fromBundleTableSliceCondition(condition)(apiField)
    }
  }

  protected[api] def storeBundleContextless(bundle: ApiBundleContextless)
                                           (implicit session: Session): Try[ApiBundleContextless] = {
    val bundleContextlessRow = new BundleContextlessRow(0, bundle.name, LocalDateTime.now(), LocalDateTime.now())

    Try((BundleContextless returning BundleContextless) += bundleContextlessRow) flatMap { insertedBundle =>
      val bundleApi = ApiBundleContextless.fromBundleContextlessTables(insertedBundle)(bundle.tables)
      bundleApi.tables match {
        case Some(tables) =>
          val storedCombinations = tables map { combination =>
            storeBundleCombination(combination)(bundleApi)
          }
          Utils.flatten(storedCombinations) map { apiCombinations =>
            bundleApi.copy(tables = Some(apiCombinations))
          }
        case None =>
          Success(bundleApi)
      }
    }
  }

  protected[api] def getBundleContextlessById(bundleId: Int)(implicit session: Session): Option[ApiBundleContextless] = {
    BundleContextless.filter(_.id === bundleId).run.headOption map { bundle =>
      // Traversing entity graph the Slick way
      // A fairly complex case with a lot of related data that needs to be retrieved for each bundle join
      val tableQuery = for {
        join <- BundleContextlessJoin.filter(_.bundleContextlessId === bundleId)
        bundleTable <- join.bundleContextlessTableFk
        dataTable <- bundleTable.dataTableFk
        joinField <- join.dataFieldFk3
        tableField <- join.dataFieldFk4
      } yield (join, bundleTable, dataTable, joinField, tableField)

      val tables = tableQuery.run

      val apiTables = tables.map {
        case (join: BundleContextlessJoinRow, bundleTable: BundleContextlessTableRow, dataTable: DataTableRow, joinField: DataFieldRow, tableField: DataFieldRow) =>
          val apiDataTable = ApiDataTable.fromDataTable(dataTable)(None)(None)
          val apiTable = ApiBundleTable.fromBundleTable(bundleTable)(apiDataTable)
          val apiJoinField = ApiDataField.fromDataField(joinField)
          val apiTableField = ApiDataField.fromDataField(tableField)
          ApiBundleCombination.fromBundleJoin(join)(Some(apiJoinField), Some(apiTableField), apiTable)
      }

      ApiBundleContextless.fromBundleContextlessTables(bundle)(Some(apiTables))
    }

  }

  protected[api] def storeBundleCombination(combination: ApiBundleCombination)
                                           (bundle: ApiBundleContextless)
                                           (implicit session: Session): Try[ApiBundleCombination] = {

    (combination.bundleTable.id, bundle.id) match {
      // Both bundle table and bundle id have to be "well defined", already with IDs
      case (Some(bundleTableId), Some(bundleId)) =>
        (combination.bundleJoinField, combination.bundleTableField, combination.operator) match {
          // Either both bundleJoinField, bundleTableField and the comparison operator have to be defined
          case (Some(bundleJoinField), Some(bundleTableField), Some(comparisonOperator)) =>
            val combinationRow = new BundleContextlessJoinRow(0, LocalDateTime.now(), LocalDateTime.now(),
              combination.name, bundleTableId, bundleId,
              bundleJoinField.id, bundleTableField.id, Some(comparisonOperator.toString))

            Try((BundleContextlessJoin returning BundleContextlessJoin) += combinationRow) map { insertedCombination =>
              ApiBundleCombination.fromBundleJoin(insertedCombination)(Some(bundleJoinField), Some(bundleTableField), combination.bundleTable)
            }

          // Or none of them
          case (None, None, None) =>
            val combinationRow = new BundleContextlessJoinRow(0, LocalDateTime.now(), LocalDateTime.now(),
              combination.name, bundleTableId, bundleId,
              None, None, None)

            Try((BundleContextlessJoin returning BundleContextlessJoin) += combinationRow) map { insertedCombination =>
              ApiBundleCombination.fromBundleJoin(insertedCombination)(None, None, combination.bundleTable)
            }
          case _ =>
            Failure(new IllegalArgumentException("Both columns must be provided to join data on as well as the operator, or none"))
        }

      case _ =>
        Failure(new IllegalArgumentException(s"Bundle Table ${combination.bundleTable.id} to create a bundle combination on (bundle ${bundle.id}) not found"))
    }
  }
}