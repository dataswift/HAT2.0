package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Success, Try}

// this trait defines our service behavior independently from the service actor
trait BundleService extends HttpService with HatServiceAuthHandler {

  val dataService: DataService
  val db = DatabaseInfo.db

  val routes = {
    pathPrefix("bundles" / "contextless") {
      userPassHandler { implicit user: User =>
        createBundleTable ~
          getBundleTable ~
          createBundleContextless ~
          getBundleContextless ~
          getBundleTableValuesApi ~
          getBundleContextlessValuesApi
      }
    }
  }

  import JsonProtocol._


  /*
   * Creates a bundle table as per POST'ed data, including table information and its slicing conditions
   */
  def createBundleTable = path("table") {
    post {
      entity(as[ApiBundleTable]) { bundleTable =>
        db.withSession { implicit session =>
          val result = storeBundleTable(bundleTable)

          complete {
            result match {
              case Success(storedBundleTable) =>
                (Created, storedBundleTable)
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }
        }
      }
    }
  }

  /*
   * Retrieves bundle table structure by ID
   */
  def getBundleTable = path("table" / IntNumber) {
    (bundleTableId: Int) =>
      get {
        db.withSession { implicit session =>
          val bundleTable = getBundleTableById(bundleTableId)
          complete {
            bundleTable match {
              case Some(table) =>
                table
              case None =>
                (NotFound, s"Bundle Table $bundleTableId not found")
            }
          }
        }
      }
  }

  /*
   * Retrieves bundle table data
   */
  def getBundleTableValuesApi = path("table" / IntNumber / "values") {
    (bundleTableId: Int) =>
      get {
        db.withSession { implicit session =>
          val someResults = getBundleTableValues(bundleTableId)

          complete {
            someResults match {
              case Some(results) =>
                results
              case None =>
                (NotFound, s"Contextless Bundle Table $bundleTableId not found")
            }
          }
        }
      }
  }

  def getBundleTableValues(bundleTableId: Int)(implicit session: Session): Option[ApiBundleTable] = {
    val bundleDataTableQuery = for {
      bundleTable <- BundleTable.filter(_.id === bundleTableId)
      dataTable <- bundleTable.dataTableFk
    } yield (bundleTable, dataTable)

    val maybeBundleDataTable = Try(bundleDataTableQuery.run.headOption)

    maybeBundleDataTable match {
      case Success(bundleDataTable) =>

        bundleDataTable map { case (bundleTable: BundleTableRow, dataTable: DataTableRow) =>
          val tableId = dataTable.id
          val slices = getBundleTableSlices(bundleTableId)
          val apiDataTables = slices match {
            // Without any slices, the case degrades to plain data table access
            case None =>
              dataService.getTableValues(tableId)
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

              dataService.getTableValues(tableId, values)
          }

          val emptyBundleTable = ApiBundleTable.fromBundleTable(bundleTable)(ApiDataTable.fromDataTable(dataTable)(None)(None))
          emptyBundleTable.copy(data = apiDataTables)
        }
      case Failure(e) =>
        print("Failure getting bundle data: " + e.getMessage)
        None
    }

  }


  /*
   * Creates a new virtual table for storing arbitrary incoming data
   */
  def createBundleContextless = path("") {
    post {
      entity(as[ApiBundleContextless]) { bundle =>
        db.withSession { implicit session =>
          val result = storeBundleContextless(bundle)

          complete {
            result match {
              case Success(storedBundle) =>
                (Created, storedBundle)
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }
        }
      }
    }
  }

  /*
   * Retrieves contextless bundle structure by ID
   */
  def getBundleContextless = path(IntNumber) {
    (bundleId: Int) =>
      get {
        db.withSession { implicit session =>
          val bundle = getBundleContextlessById(bundleId)

          complete {
            bundle match {
              case Some(foundBundle) =>
                foundBundle
              case None =>
                (NotFound, s"Contextless Bundle $bundleId not found")
            }
          }
        }
      }
  }

  /*
   * Retrieves contextless bundle data
   */
  def getBundleContextlessValuesApi = path(IntNumber / "values") {
    (bundleId: Int) =>
      get {
        db.withSession { implicit session =>
          val someResults = getBundleContextlessValues(bundleId)
          complete {
            someResults match {
              case Some(results) =>
                results
              case None =>
                (NotFound, s"Contextless Bundle $bundleId not found")
            }
          }
        }
      }
  }

  def getBundleContextlessValues(bundleId: Int)(implicit session: Session): Option[ApiBundleContextlessData] = {
    // TODO: include join fields
    val bundleQuery = for {
      bundle <- BundleContextless.filter(_.id === bundleId) // 1 or 0
      combination <- BundleJoin.filter(_.bundleId === bundleId) // N for each bundle
      table <- combination.bundleTableFk // 1 for each combination
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
  private def storeBundleTable(bundleTable: ApiBundleTable)(implicit session: Session): Try[ApiBundleTable] = {
    // Require the bundle to be based on a data table that already exists
    bundleTable.table.id match {
      case Some(tableId) =>
        val bundleTableRow = new BundleTableRow(0, LocalDateTime.now(), LocalDateTime.now(), bundleTable.name, tableId)

        // Using Try to handle errors
        Try((BundleTable returning BundleTable) += bundleTableRow) flatMap { insertedBundleTable =>
          // Convert from database format to API format
          val insertedApiBundleTable = ApiBundleTable.fromBundleTable(insertedBundleTable)(bundleTable.table)
          // A partial function to store all table slices related to this bundle table
          def storeBundleSlice = storeSlice(insertedApiBundleTable) _

          val apiSlices = bundleTable.slices map { tableSlices =>
            val slices = tableSlices.map(storeBundleSlice)
            // Flattens to a simple Try with list if slices or returns the first error that occurred
            flatten(slices)
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

  private def getBundleTableById(bundleTableId: Int)(implicit session: Session): Option[ApiBundleTable] = {
    // Traversing entity graph the Slick way
    val tableQuery = for {
      bundleTable <- BundleTable.filter(_.id === bundleTableId)
      dataTable <- bundleTable.dataTableFk
    } yield (bundleTable, dataTable)

    val table = tableQuery.run.headOption

    // Map back from database types to API ones
    table map {
      case (bundleTable: BundleTableRow, dataTable: DataTableRow) =>
        val apiDataTable = ApiDataTable.fromDataTable(dataTable)(None)(None)
        val apiBundleTable = ApiBundleTable.fromBundleTable(bundleTable)(apiDataTable)
        val slices = apiBundleTable.id.flatMap(getBundleTableSlices)
        apiBundleTable.copy(slices = slices)
    }
  }

  private def storeSlice(bundleTable: ApiBundleTable)
                        (slice: ApiBundleTableSlice)
                        (implicit session: Session): Try[ApiBundleTableSlice] = {

    (bundleTable.table.id, bundleTable.id, slice.table.id) match {
      case (None, _, _) =>
        Failure(new IllegalArgumentException("Table provided for bundling must contain ID"))

      case (slice.table.id, Some(bundleTableId), Some(sliceTableId)) =>

        val sliceRow = new BundleTablesliceRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          bundleTableId, sliceTableId)

        Try((BundleTableslice returning BundleTableslice) += sliceRow) flatMap { insertedSlice =>
          val insertedApiBundleTableSlice = ApiBundleTableSlice.fromBundleTableSlice(insertedSlice)(slice.table)

          def storeSliceCondition = storeCondition(insertedApiBundleTableSlice) _
          val conditions = slice.conditions.map(storeSliceCondition)

          // If all conditions have been inserted successfully,
          // Return the complete ApiBundleTableSlice object;
          // Otherwise, the first error that occurred
          flatten(conditions) map { apiConditions =>
            insertedApiBundleTableSlice.copy(conditions = apiConditions)
          }
        }

      case _ =>
        Failure(new IllegalArgumentException("Bundle table must refer to the same table as each slice of the table"))
    }

  }

  private def getBundleTableSlices(bundleTableId: Int)
                                  (implicit session: Session): Option[Seq[ApiBundleTableSlice]] = {

    // Traversing entity graph the Slick way
    val slicesQuery = for {
      slice <- BundleTableslice.filter(_.bundleTableId === bundleTableId)
      dataTable <- slice.dataTableFk
    } yield (slice, dataTable)

    val slices = slicesQuery.run

    val result = slices flatMap { case (slice: BundleTablesliceRow, dataTable: DataTableRow) =>
      // Returning the Data Table information of the slice without subTables or fields
      val apiDataTable = ApiDataTable.fromDataTable(dataTable)(None)(None)
      val apiSlice = ApiBundleTableSlice.fromBundleTableSlice(slice)(apiDataTable)
      apiSlice.id.map(getSliceConditions) map { conditions =>
        apiSlice.copy(conditions = conditions)
      }
    }
    seqOption(result)
  }

  private def storeCondition(bundleTableSlice: ApiBundleTableSlice)
                            (condition: ApiBundleTableCondition)
                            (implicit session: Session): Try[ApiBundleTableCondition] = {

    (bundleTableSlice.table.id, condition.field.id, bundleTableSlice.id) match {
      case (condition.field.tableId, Some(fieldId), Some(sliceId)) =>
        val conditionRow = new BundleTablesliceconditionRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          fieldId, sliceId, condition.operator.toString, condition.value)

        Try((BundleTableslicecondition returning BundleTableslicecondition) += conditionRow) map { insertedCondition =>
          ApiBundleTableCondition.fromBundleTableSliceCondition(insertedCondition)(condition.field)
        }

      case _ =>
        Failure(new IllegalArgumentException("Invalid table slice or table field provided"))

    }
  }

  private def getSliceConditions(tableSliceId: Int)
                                (implicit session: Session): Seq[ApiBundleTableCondition] = {
    // Traversing entity graph the Slick way
    val conditionsQuery = for {
      c <- BundleTableslicecondition.filter(_.tablesliceId === tableSliceId)
      f <- c.dataFieldFk
    } yield (c, f)

    val conditions = conditionsQuery.run
    conditions map { case (condition: BundleTablesliceconditionRow, field: DataFieldRow) =>
      val apiField = ApiDataField.fromDataField(field)
      ApiBundleTableCondition.fromBundleTableSliceCondition(condition)(apiField)
    }
  }

  private def storeBundleContextless(bundle: ApiBundleContextless)
                                    (implicit session: Session): Try[ApiBundleContextless] = {
    val bundleContextlessRow = new BundleContextlessRow(0, bundle.name, LocalDateTime.now(), LocalDateTime.now())

    Try((BundleContextless returning BundleContextless) += bundleContextlessRow) flatMap { insertedBundle =>
      val bundleApi = ApiBundleContextless.fromBundleContextlessTables(insertedBundle)(bundle.tables)
      bundleApi.tables match {
        case Some(tables) =>
          val storedCombinations = tables map { combination =>
            storeBundleCombination(combination)(bundleApi)
          }
          flatten(storedCombinations) map { apiCombinations =>
            bundleApi.copy(tables = Some(apiCombinations))
          }
        case None =>
          Success(bundleApi)
      }
    }
  }

  private def getBundleContextlessById(bundleId: Int)(implicit session: Session): Option[ApiBundleContextless] = {
    BundleContextless.filter(_.id === bundleId).run.headOption map { bundle =>
      // Traversing entity graph the Slick way
      // A fairly complex case with a lot of related data that needs to be retrieved for each bundle join
      val tableQuery = for {
        join <- BundleJoin.filter(_.bundleId === bundleId)
        bundleTable <- join.bundleTableFk
        dataTable <- bundleTable.dataTableFk
        joinField <- join.dataFieldFk3
        tableField <- join.dataFieldFk4
      } yield (join, bundleTable, dataTable, joinField, tableField)

      val tables = tableQuery.run

      val apiTables = tables.map {
        case (join: BundleJoinRow, bundleTable: BundleTableRow, dataTable: DataTableRow, joinField: DataFieldRow, tableField: DataFieldRow) =>
          val apiDataTable = ApiDataTable.fromDataTable(dataTable)(None)(None)
          val apiTable = ApiBundleTable.fromBundleTable(bundleTable)(apiDataTable)
          val apiJoinField = ApiDataField.fromDataField(joinField)
          val apiTableField = ApiDataField.fromDataField(tableField)
          ApiBundleCombination.fromBundleJoin(join)(Some(apiJoinField), Some(apiTableField), apiTable)
      }

      ApiBundleContextless.fromBundleContextlessTables(bundle)(Some(apiTables))
    }

  }

  private def storeBundleCombination(combination: ApiBundleCombination)
                                    (bundle: ApiBundleContextless)
                                    (implicit session: Session): Try[ApiBundleCombination] = {

    (combination.bundleTable.id, bundle.id) match {
      // Both bundle table and bundle id have to be "well defined", already with IDs
      case (Some(bundleTableId), Some(bundleId)) =>
        (combination.bundleJoinField, combination.bundleTableField, combination.operator) match {
          // Either both bundleJoinField, bundleTableField and the comparison operator have to be defined
          case (Some(bundleJoinField), Some(bundleTableField), Some(comparisonOperator)) =>
            val combinationRow = new BundleJoinRow(0, LocalDateTime.now(), LocalDateTime.now(),
              combination.name, bundleTableId, bundleId,
              bundleJoinField.id, bundleTableField.id, Some(comparisonOperator.toString))

            Try((BundleJoin returning BundleJoin) += combinationRow) map { insertedCombination =>
              ApiBundleCombination.fromBundleJoin(insertedCombination)(Some(bundleJoinField), Some(bundleTableField), combination.bundleTable)
            }

          // Or none of them
          case (None, None, None) =>
            val combinationRow = new BundleJoinRow(0, LocalDateTime.now(), LocalDateTime.now(),
              combination.name, bundleTableId, bundleId,
              None, None, None)

            Try((BundleJoin returning BundleJoin) += combinationRow) map { insertedCombination =>
              ApiBundleCombination.fromBundleJoin(insertedCombination)(None, None, combination.bundleTable)
            }
          case _ =>
            Failure(new IllegalArgumentException("Both columns must be provided to join data on as well as the operator, or none"))
        }

      case _ =>
        Failure(new IllegalArgumentException(s"Bundle Table ${combination.bundleTable.id} to create a bundle combination on (bundle ${bundle.id}) not found"))
    }
  }

  protected def flatten[T](xs: Seq[Try[T]]): Try[Seq[T]] = {
    val (ss: Seq[Success[T]]@unchecked, fs: Seq[Failure[T]]@unchecked) =
      xs.partition(_.isSuccess)

    if (fs.isEmpty) Success(ss map (_.get))
    else Failure[Seq[T]](fs(0).exception) // Only keep the first failure
  }

  // Utility function to return None for empty sequences
  private def seqOption[T](seq: Seq[T]): Option[Seq[T]] = {
    if (seq.isEmpty)
      None
    else
      Some(seq)
  }
}