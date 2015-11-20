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
  protected def storeBundleContext(bundleContext: ApiBundleContext)(implicit session: Session): Try[ApiBundleContext] = {
    Failure(new IllegalArgumentException("Method not yet implemented"))
  }

  protected def getBundleContextById(bundleContextId: Int)(implicit session: Session): Option[ApiBundleContext] = {
    None
  }

  protected def getBundleContextData(bundleContextId: Int)(implicit session: Session): Option[ApiBundleContextData] = {
    None
  }

  def retrieveDataDebitContextualValues(debit: DataDebitRow, bundleId: Int)(implicit session: Session): Option[ApiBundleContextData] = {
    val bundleValues = getBundleContextData(bundleId)
    bundleValues
  }

  protected def storeBundleContextEntitySelection(bundleId: Int, entitySelection: ApiBundleContextEntitySelection)
      (implicit session: Session): Try[ApiBundleContextEntitySelection] = {
    Failure(new IllegalArgumentException("Method not yet implemented"))
  }

  protected def storeBundlePropertySelection(bundleId: Int, entitySelectionId: Int, propertySelection: ApiBundleContextPropertySelection)
      (implicit session: Session): Try[ApiBundleContextPropertySelection] = {
    Failure(new IllegalArgumentException("Method not yet implemented"))
  }
}