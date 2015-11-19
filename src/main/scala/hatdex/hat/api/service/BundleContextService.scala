package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.Utils
import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.util.{Failure, Success, Try}

// this trait defines our service behavior independently from the service actor
trait BundleContextService {

  val logger: LoggingAdapter

  /*
   * Stores bundle table provided from the incoming API call
   */
  private def storeBundleContext(bundleContext: ApiBundleContext)(implicit session: Session): Try[ApiBundleContext] = {
    // Require the bundle to be based on a data table that already exists
    bundleContext.entity.id match {
      case Some(bundlecontextId) =>
        val bundleContextRow = new BundleContextlessRow()(0, parentbundleId, LocalDateTime.now(), LocalDateTime.now(), bundleTable.name, entityselectionId)

        // Using Try to handle errors
        Try((BundleContext returning BundleContext) += bundleContextRow) flatMap { insertedBundleContext =>
          // Convert from database format to API format
          val insertedApiBundleContext = ApiBundleContext.fromBundleContext(insertedBundleContext)(bundleContext.table)
          // A partial function to store all table slices related to this bundle table
          def storeBundlePropertySlice = storePropertySlice(insertedApiBundleContext) _

          val apiPropertySlices = bundleContext.slices map { propertySlices =>
            val propertyslices = bundlePropertySlices.map(storeBundlePropertySlice)
            // Flattens to a simple Try with list if slices or returns the first error that occurred
            Utils.flatten(propertyslices)
          }

          apiPropertySlices match {
            case Some(Success(propertyslices)) =>
              Success(insertedApiBundleContext.copy(propertyslices = Some(propertyslices)))
            case Some(Failure(e)) =>
              Failure(e)
            case None =>
              // Bundle Table with no slicing
              Success(insertedApiBundleContext)
          }

        }
      case None =>
        Failure(new IllegalArgumentException("Table provided for bundling must contain ID"))
    }

  }

  private def getBundleContextById(bundleContextId: Int)(implicit session: Session): Option[ApiBundleContext] = {
    // Traversing entity graph the Slick way
    val tableQuery = for {
      bundleContext <- BundleContext.filter(_.id === bundleContextId)
      entitySelection <- bundleContext.entitySelectionFk
    } yield (bundleContext, entitySelection)

    val table = tableQuery.run.headOption

    // Map back from database types to API ones
    bundleContext map {
      case (bundleContext: BundleContextRow, entitySelection: EntitySelectionRow) =>
        val apiDataTable = ApiEntitySelection.fromentitySelection(entitySelection)(None)(None)
        val apibundleContext = ApibundleContext.frombundleContext(bundleContext)(apiEntitySelection)
        val slices = getBundleContextSlices(apibundleContext)
        apibundleContext
    }
  }
}