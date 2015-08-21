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
      createBundleTable
    }
  }

  import ApiJsonProtocol._


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
//
//  /*
//   * Creates a new virtual table for storing arbitrary incoming data
//   */
//  def createBundleContextless = path("contextless") {
//    post {
//
//    }
//  }

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

  private def storeSlice(bundleTable: ApiBundleTable)
                        (slice: ApiBundleTableSlice)
                        (implicit session: Session): Try[ApiBundleTableSlice] = {

    (bundleTable.table.id, bundleTable.table.id, slice.table.id) match {
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

  def flatten[T](xs: Seq[Try[T]]): Try[Seq[T]] = {
    val (ss: Seq[Success[T]]@unchecked, fs: Seq[Failure[T]]@unchecked) =
      xs.partition(_.isSuccess)

    if (fs.isEmpty) Success(ss map (_.get))
    else Failure[Seq[T]](fs(0).exception) // Only keep the first failure
  }
}