package hatdex.hat.api.service

import java.util.UUID

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.{LocalDateTime, DateTime}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

trait DataDebitService {
  val logger: LoggingAdapter

  val bundlesService: BundleService
  val bundleContextService: BundleContextService

  def storeContextlessDataDebit(debit: ApiDataDebit, bundle: ApiBundleContextless)
                               (implicit session: Session, user: User): Future[ApiDataDebit] = {
    val contextlessBundle = bundle.id match {
      case Some(bundleId) =>
        bundlesService.getBundleContextlessById(bundleId) map { maybeBundle: Option[ApiBundleContextless] =>
          maybeBundle.getOrElse {
            throw new IllegalArgumentException(s"Bundle with ID $bundleId does not exist")
          }
        }
      case None =>
        bundlesService.storeBundleContextless(bundle)
    }

    contextlessBundle map { bundle =>
      val dataDebitKey = UUID.randomUUID()
      val newDebit = DataDebitRow(dataDebitKey, LocalDateTime.now(), LocalDateTime.now(), debit.name,
        debit.startDate, debit.endDate, debit.rolling, debit.sell, debit.price,
        enabled = false, "owner", user.userId.toString,
        bundle.id,
        None,
        "contextless"
      )

      val createdDebit = (DataDebit returning DataDebit) += newDebit
      val responseDebit = ApiDataDebit.fromDbModel(createdDebit)
      responseDebit.copy(bundleContextless = Some(bundle))
    }
  }

  def storeContextDataDebit(debit: ApiDataDebit, bundle: ApiBundleContext)
                           (implicit session: Session, user: User): Try[ApiDataDebit] = {

    val contextBundle = bundle.id match {
      case Some(bundleId) =>
        bundleContextService.getBundleContextById(bundleId) match {
          case Some(bundle) =>
            Success(bundle)
          case None =>
            Failure(new IllegalArgumentException(s"Bundle with ID $bundleId does not exist"))
        }
      case None =>
        bundleContextService.storeBundleContext(bundle)
    }

    contextBundle flatMap { bundle =>
      val dataDebitKey = UUID.randomUUID()
      val newDebit = DataDebitRow(dataDebitKey, LocalDateTime.now(), LocalDateTime.now(), debit.name,
        debit.startDate, debit.endDate, debit.rolling, debit.sell, debit.price,
        enabled = false, "owner", user.userId.toString,
        None,
        bundle.id,
        "contextual"
      )

      val maybeCreatedDebit = Try((DataDebit returning DataDebit) += newDebit)

      maybeCreatedDebit map { createdDebit =>
        val responseDebit = ApiDataDebit.fromDbModel(createdDebit)
        responseDebit.copy(bundleContextual = Some(bundle))
      }
    }
  }


  def enableDataDebit(debit: DataDebitRow)(implicit session: Session): Try[Int] = {
//    import hatdex.hat.dal.SlickPostgresDriver.api._
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

  def retrieveDataDebiValues(debit: DataDebitRow, bundleId: Int, maybeLimit: Option[Int] = None, maybeStartTime: Option[LocalDateTime] = None, maybeEndTime: Option[LocalDateTime] = None): Future[ApiDataDebitOut] = {
    // No records created in the future
    val filterEndTime = maybeEndTime.getOrElse(LocalDateTime.now())
    // And by default no records older than a month
    val filterStartTime = maybeStartTime.getOrElse(LocalDateTime.now().minusMonths(1))

    val bundleValues = bundlesService.getBundleContextlessValues(bundleId, maybeLimit, filterStartTime, filterEndTime)
    bundleValues.map { data =>
      ApiDataDebitOut.fromDbModel(debit, Some(data), None)
    }
  }

  def listDataDebits: Future[Seq[ApiDataDebit]] = {
    // FIXME: local import while the rest of the service uses the old slick model
    import hatdex.hat.dal.SlickPostgresDriver.api._

    val ddbundleContextlessQuery = for {
      dd <- DataDebit
      bundleContextless <- dd.bundleContextlessFk
    } yield (dd, bundleContextless)

    val ddBundleContextualQuery = for {
      dd <- DataDebit
      bundleContextual <- dd.bundleContextFk
    } yield (dd, bundleContextual)

    val eventualBunleContextless = DatabaseInfo.db.run(ddbundleContextlessQuery.result)
    val eventualBundleContextual = DatabaseInfo.db.run(ddBundleContextualQuery.result)

    val debits = for {
      contextless <- eventualBunleContextless
      contextual <- eventualBundleContextual
    } yield {
      val cless = contextless.map { case (dd, bundle) =>
        val apidd = ApiDataDebit.fromDbModel(dd)
        val apiBundle = ApiBundleContextless.fromBundleContextless(bundle)
        apidd.copy(bundleContextless = Some(apiBundle))
      }
      val cfull = contextual.map { case (dd, bundle) =>
        val apidd = ApiDataDebit.fromDbModel(dd)
        val apiBundle = ApiBundleContext.fromDbModel(bundle)
        apidd.copy(bundleContextual = Some(apiBundle))
      }
      cless ++ cfull
    }

    debits
  }
}