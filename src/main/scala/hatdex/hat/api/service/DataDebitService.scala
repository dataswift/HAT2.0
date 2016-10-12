/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package hatdex.hat.api.service

import java.util.UUID

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.concurrent.{ExecutionContext, Future}

trait DataDebitService {
  val logger: LoggingAdapter
  implicit val dalExecutionContext: ExecutionContext
  val bundlesService: BundleService
  val bundleContextService: BundleContextService

  def storeContextlessDataDebit(debit: ApiDataDebit, bundle: ApiBundleContextless)(implicit user: User): Future[ApiDataDebit] = {
    val eventualBundleContextless = bundle.id match {
      case Some(bundleId) =>
        bundlesService.getBundleContextlessById(bundleId) map { maybeBundle: Option[ApiBundleContextless] =>
          maybeBundle.getOrElse {
            throw new IllegalArgumentException(s"Bundle with ID $bundleId does not exist")
          }
        }
      case None =>
        bundlesService.storeBundleContextless(bundle)
    }

    eventualBundleContextless flatMap { bundle =>
      val dataDebitKey = UUID.randomUUID()
      val newDebit = DataDebitRow(dataDebitKey, LocalDateTime.now(), LocalDateTime.now(), debit.name,
        debit.startDate, debit.endDate, debit.rolling, debit.sell, debit.price,
        enabled = false, "owner", user.userId.toString,
        bundle.id,
        None,
        "contextless")

      val maybeCreatedDebit = DatabaseInfo.db.run((DataDebit returning DataDebit) += newDebit)

      maybeCreatedDebit.map(ApiDataDebit.fromDbModel)
        .map(_.copy(bundleContextless = Some(bundle)))
    }
  }

  def storeContextDataDebit(debit: ApiDataDebit, bundle: ApiBundleContext)(implicit user: User): Future[ApiDataDebit] = {
    val contextBundle = bundle.id match {
      case Some(bundleId) => bundleContextService.getBundleContextById(bundleId).map(_.get)
      case None           => bundleContextService.storeBundleContext(bundle)
    }

    contextBundle flatMap { bundle =>
      val dataDebitKey = UUID.randomUUID()
      val newDebit = DataDebitRow(dataDebitKey, LocalDateTime.now(), LocalDateTime.now(), debit.name,
        debit.startDate, debit.endDate, debit.rolling, debit.sell, debit.price,
        enabled = false, "owner", user.userId.toString,
        None,
        bundle.id,
        "contextual")

      val maybeCreatedDebit = DatabaseInfo.db.run((DataDebit returning DataDebit) += newDebit)

      maybeCreatedDebit.map(ApiDataDebit.fromDbModel)
        .map(_.copy(bundleContextual = Some(bundle)))
    }
  }

  def enableDataDebit(debit: DataDebitRow): Future[Int] = {
    val query = DataDebit.filter(_.dataDebitKey === debit.dataDebitKey)
      .map(dd => (dd.enabled, dd.lastUpdated))
      .update((true, LocalDateTime.now()))
    DatabaseInfo.db.run(query)
  }

  def disableDataDebit(debit: DataDebitRow): Future[Int] = {
    val query = DataDebit.filter(_.dataDebitKey === debit.dataDebitKey)
      .map(dd => (dd.enabled, dd.lastUpdated))
      .update((false, LocalDateTime.now()))
    DatabaseInfo.db.run(query)
  }

  def findDataDebitByKey(dataDebitKey: UUID): Future[Option[DataDebitRow]] = {
    DatabaseInfo.db.run(DataDebit.filter(_.dataDebitKey === dataDebitKey).result).map(_.headOption)
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

  def retrieveDataDebitContextualValues(debit: DataDebitRow, bundleId: Int): Future[ApiDataDebitOut] = {
    val eventualApiEntities = bundleContextService.getBundleContextData(bundleId)
    eventualApiEntities map { data =>
      ApiDataDebitOut.fromDbModel(debit, None, Some(data))
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
      val cless = contextless.map {
        case (dd, bundle) =>
          val apidd = ApiDataDebit.fromDbModel(dd)
          val apiBundle = ApiBundleContextless.fromBundleContextless(bundle)
          apidd.copy(bundleContextless = Some(apiBundle))
      }
      val cfull = contextual.map {
        case (dd, bundle) =>
          val apidd = ApiDataDebit.fromDbModel(dd)
          val apiBundle = ApiBundleContext.fromDbModel(bundle)
          apidd.copy(bundleContextual = Some(apiBundle))
      }
      cless ++ cfull
    }

    debits
  }
}