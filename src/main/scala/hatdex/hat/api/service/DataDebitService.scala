package hatdex.hat.api.service

import java.util.UUID

import akka.event.LoggingAdapter
import hatdex.hat.api.models._
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.util.Try

trait DataDebitService extends BundleService {
  val logger: LoggingAdapter

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
    val bundleValues = getBundleContextlessValues(bundleId)
    ApiDataDebitOut.fromDbModel(debit, bundleValues, None)
  }

  def listDataDebits(implicit session: Session): Seq[ApiDataDebit] = {
    val dataDebits = DataDebit.run

    dataDebits.map { dataDebit =>
      ApiDataDebit.fromDbModel(dataDebit)
    }
  }
}
