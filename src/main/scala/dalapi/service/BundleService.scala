package dalapi.service

import dal.SlickPostgresDriver.simple._
import dal.Tables._
import dalapi.models._
import dalapi.InboundService
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Try, Success}

// this trait defines our service behavior independently from the service actor
trait BundleService extends HttpService with InboundService {

  val routes = {
    pathPrefix("bundles") {
      createBundleTable ~ getBundleTable ~ createBundleContextless ~ getBundleContextless
    }
  }

  import ApiJsonProtocol._


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
   * Retrieves bundle table by ID
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
                (NotFound, s"Bundle Table ${bundleTableId} not found")
            }
          }
        }
      }
  }


  /*
   * Creates a new virtual table for storing arbitrary incoming data
   */
  def createBundleContextless = path("contextless") {
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

  def getBundleContextless = path("contextless" / IntNumber) {
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

  private def storeBundleTable(bundleTable: ApiBundleTable)(implicit session: Session): Try[ApiBundleTable] = {
    bundleTable.table.id match {
      case Some(tableId) =>
        val bundleTableRow = new BundleTableRow(0, LocalDateTime.now(), LocalDateTime.now(), bundleTable.name, tableId)

        Try((BundleTable returning BundleTable) += bundleTableRow) flatMap { insertedBundleTable =>
          val insertedApiBundleTable = ApiBundleTable.fromBundleTable(insertedBundleTable)(bundleTable.table)
          def storeBundleSlice = storeSlice (insertedApiBundleTable) _

          val apiSlices = bundleTable.slices map { tableSlices =>
            val slices = tableSlices.map(storeBundleSlice)
            flatten(slices)
          }

          apiSlices match {
            case Some(Success(slices)) =>
              Success(insertedApiBundleTable.copy(slices = Some(slices)))
            case Some(Failure(e)) =>
              Failure(e)
            case None =>
              Success(insertedApiBundleTable)
          }

        }
      case None =>
        Failure(new IllegalArgumentException("Table provided for bundling must contain ID"))
    }

  }

  private def getBundleTableById(bundleTableId: Int)(implicit session: Session): Option[ApiBundleTable] = {
    val tableQuery = for {
      t <- BundleTable.filter(_.id === bundleTableId)
      d <- t.dataTableFk
    } yield (t, d)


    val table = tableQuery.run.headOption

    table map {
      case (bundleTable: BundleTableRow, dataTable: DataTableRow) =>
        val apiDataTable = ApiDataTable.fromDataTable(dataTable)(None)(None)
        val apiBundleTable = ApiBundleTable.fromBundleTable(bundleTable)(apiDataTable)
        val slices = getBundleTableSlices(apiBundleTable)
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

  private def getBundleTableSlices(bundleTable: ApiBundleTable)
                                  (implicit session: Session): Option[Seq[ApiBundleTableSlice]] = {
    bundleTable.id map { bundleTableId =>
      val slicesQuery = for {
        s <- BundleTableslice.filter(_.bundleTableId === bundleTableId)
        t <- s.dataTableFk
      } yield (s, t)

      val slices = slicesQuery.run

      slices map { case (slice: BundleTablesliceRow, dataTable: DataTableRow) =>
        // Returning the Data Table information of the slice without subTables or fields
        val apiDataTable = ApiDataTable.fromDataTable(dataTable)(None)(None)
        val apiSlice = ApiBundleTableSlice.fromBundleTableSlice(slice)(apiDataTable)
        val conditions = getSliceConditions(apiSlice)
        apiSlice.copy(conditions = conditions)
      }
    }
  }

  private def storeCondition(bundleTableSlice: ApiBundleTableSlice)
                            (condition: ApiBundleTableCondition)
                            (implicit session: Session): Try[ApiBundleTableCondition] = {

    (bundleTableSlice.table.id, condition.field.id, bundleTableSlice.id) match {
      case (Some(condition.field.tableId), Some(fieldId), Some(sliceId)) =>
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

  private def getSliceConditions(bundleTableSlice: ApiBundleTableSlice)
                                (implicit session: Session): Seq[ApiBundleTableCondition] = {
    bundleTableSlice.id match {
      case Some(tableSliceId) =>
        val conditionsQuery = for {
          c <- BundleTableslicecondition.filter(_.tablesliceId === bundleTableSlice.id)
          f <- c.dataFieldFk
        } yield (c, f)

        val conditions = conditionsQuery.run
        conditions map { case (condition:BundleTablesliceconditionRow, field:DataFieldRow) =>
          val apiField = ApiDataField.fromDataField(field)
          ApiBundleTableCondition.fromBundleTableSliceCondition(condition)(apiField)
        }

      case None =>
        Seq()
    }
  }

  private def storeBundleContextless(bundle: ApiBundleContextless)
                                    (implicit session: Session): Try[ApiBundleContextless] = {
    val bundleContextlessRow = new BundleContextlessRow(0, LocalDateTime.now(), LocalDateTime.now(), bundle.name)

    Try((BundleContextless returning BundleContextless) += bundleContextlessRow) flatMap { insertedBundle =>
      val bundleApi = ApiBundleContextless.fromBundleContextlessTables(insertedBundle)(bundle.tables)
      val storedCombinations = bundleApi.tables map { combination =>
        storeBundleCombination(combination)(bundleApi)
      }

      flatten(storedCombinations) map { apiCombinations =>
        bundleApi.copy(tables = apiCombinations)
      }
    }
  }

  private def getBundleContextlessById(bundleId: Int)(implicit session: Session): Option[ApiBundleContextless] = {
    BundleContextless.filter(_.id === bundleId).run.headOption map { bundle =>
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

      ApiBundleContextless.fromBundleContextlessTables(bundle)(apiTables)
    }

  }

  private def storeBundleCombination(combination: ApiBundleCombination)
                                    (bundle: ApiBundleContextless)
                                    (implicit session: Session): Try[ApiBundleCombination] = {

    (combination.bundleTable.id, bundle.id) match {

      case (Some(bundleTableId), Some(bundleId)) =>

        (combination.bundleJoinField, combination.bundleTableField, combination.operator) match {
          case (Some(bundleJoinField), Some(bundleTableField), Some(comparisonOperator)) =>
            Success(combination)
            val combinationRow = new BundleJoinRow(0, LocalDateTime.now(), LocalDateTime.now(),
              combination.name, bundleTableId, bundleId,
              bundleJoinField.id, bundleTableField.id, Some(comparisonOperator.toString))

            Try((BundleJoin returning BundleJoin) += combinationRow) map { insertedCombination =>
              ApiBundleCombination.fromBundleJoin(insertedCombination)(Some(bundleJoinField), Some(bundleTableField), combination.bundleTable)
            }

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

  def flatten[T](xs: Seq[Try[T]]): Try[Seq[T]] = {
    val (ss: Seq[Success[T]]@unchecked, fs: Seq[Failure[T]]@unchecked) =
      xs.partition(_.isSuccess)

    if (fs.isEmpty) Success(ss map (_.get))
    else Failure[Seq[T]](fs(0).exception) // Only keep the first failure
  }
}