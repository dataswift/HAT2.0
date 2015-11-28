package hatdex.hat.api.service

import java.util.UUID

import akka.event.LoggingAdapter
import hatdex.hat.api.models._
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.util.Try

trait DataDebitService {
  val logger: LoggingAdapter

  val bundlesService: BundleService
  val bundleContextService: BundleContextService

  def storeContextlessDataDebit(debit: ApiDataDebit, bundle: ApiBundleContextless)
                               (implicit session: Session, user: User): Try[ApiDataDebit] = {
    val dataDebitKey = UUID.randomUUID()
    val newDebit = DataDebitRow(dataDebitKey, LocalDateTime.now(), LocalDateTime.now(), debit.name,
      debit.startDate, debit.endDate, debit.rolling, debit.sell, debit.price,
      enabled = false, "owner", user.userId.toString,
      debit.bundleContextless.flatMap(bundle => bundle.id),
      None,
      "contextless"
    )

    val maybeCreatedDebit = Try((DataDebit returning DataDebit) += newDebit)

    maybeCreatedDebit map { createdDebit =>
      val responseDebit = ApiDataDebit.fromDbModel(createdDebit)
      responseDebit.copy(bundleContextless = Some(bundle))
    }
  }

  def storeContextDataDebit(debit: ApiDataDebit, bundle: ApiBundleContext)
                           (implicit session: Session, user: User): Try[ApiDataDebit] = {
    val dataDebitKey = UUID.randomUUID()
    val newDebit = DataDebitRow(dataDebitKey, LocalDateTime.now(), LocalDateTime.now(), debit.name,
      debit.startDate, debit.endDate, debit.rolling, debit.sell, debit.price,
      enabled = false, "owner", user.userId.toString,
      None,
      bundle.id,
      "context"
    )

    val maybeCreatedDebit = Try((DataDebit returning DataDebit) += newDebit)

    maybeCreatedDebit map { createdDebit =>
      val responseDebit = ApiDataDebit.fromDbModel(createdDebit)
      responseDebit.copy(bundleContextual = Some(bundle))
    }
  }


  def enableDataDebit(debit: DataDebitRow)(implicit session: Session): Try[Int] = {
    Try(
      DataDebit.filter(_.dataDebitKey === debit.dataDebitKey)
        .map(dd => (dd.enabled, dd.lastUpdated))
        .update((true, LocalDateTime.now()))
    )
  }

  def disableDataDebit(debit: DataDebitRow)(implicit session: Session): Try[Int] = {
    Try(
      DataDebit.filter(_.dataDebitKey === debit.dataDebitKey)
        .map(dd => (dd.enabled, dd.lastUpdated))
        .update((false, LocalDateTime.now()))
    )
  }

  def findDataDebitByKey(dataDebitKey: UUID)(implicit session: Session): Option[DataDebitRow] = {
    DataDebit.filter(_.dataDebitKey === dataDebitKey).run.headOption
  }

  def retrieveDataDebiValues(debit: DataDebitRow, bundleId: Int)(implicit session: Session): ApiDataDebitOut = {
    val bundleValues = bundlesService.getBundleContextlessValues(bundleId)
    ApiDataDebitOut.fromDbModel(debit, bundleValues, None)
  }

  def listDataDebits(implicit session: Session): Seq[ApiDataDebit] = {

    // FIXME: quite sure this does not do the join correctly across both kinds of tables
    val dataDebitsQuery = for {
      (dd, bundleContextless) <- DataDebit leftJoin BundleContextless on (_.bundleContextlessId === _.id)
      (dd, bundleContextual) <- DataDebit leftJoin BundleContext on (_.bundleContextId === _.id)
    } yield (dd, bundleContextless.?, bundleContextual.?)

    val dataDebits = dataDebitsQuery.run

    logger.debug("Retrieved data debits: " + dataDebits.toString)
    dataDebits.map { case (dataDebit: DataDebitRow, bundleContextless: Option[BundleContextlessRow], bundleContextual: Option[BundleContextRow]) =>
      val dd = ApiDataDebit.fromDbModel(dataDebit)
      val apiBundleContextless = bundleContextless.map(ApiBundleContextless.fromBundleContextless)
      val apiBundleContextual = None
      dd.copy(bundleContextless = apiBundleContextless, bundleContextual = apiBundleContextual)
    }
  }
}