/*
 * Copyright (C) 2017 HAT Data Exchange Ltd
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
 *
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>
 * 2 / 2017
 */
package org.hatdex.hat.api.service

import java.util.UUID
import javax.inject.Inject

import org.hatdex.hat.api.models.{ User, _ }
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.SlickPostgresDriver.api._
import org.hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.concurrent.Future

class DataDebitService @Inject() (bundlesService: BundleService) extends DalExecutionContext {

  def storeContextlessDataDebit(debit: ApiDataDebit, bundle: ApiBundleContextless)(implicit db: Database, user: User): Future[ApiDataDebit] = {
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

      val maybeCreatedDebit = db.run((DataDebit returning DataDebit) += newDebit)

      maybeCreatedDebit.map(ModelTranslation.fromDbModel)
        .map(_.copy(bundleContextless = Some(bundle)))
    }
  }

  def storeContextDataDebit(debit: ApiDataDebit, bundle: ApiBundleContext)(implicit db: Database, user: User): Future[ApiDataDebit] = {
    throw new NotImplementedError("Contextual APIs not implemented")
  }

  def enableDataDebit(debit: DataDebitRow)(implicit db: Database): Future[Int] = {
    val query = DataDebit.filter(_.dataDebitKey === debit.dataDebitKey)
      .map(dd => (dd.enabled, dd.lastUpdated))
      .update((true, LocalDateTime.now()))
    db.run(query)
  }

  def disableDataDebit(debit: DataDebitRow)(implicit db: Database): Future[Int] = {
    val query = DataDebit.filter(_.dataDebitKey === debit.dataDebitKey)
      .map(dd => (dd.enabled, dd.lastUpdated))
      .update((false, LocalDateTime.now()))
    db.run(query)
  }

  def rollDataDebit(debit: DataDebitRow)(implicit db: Database): Future[Int] = {
    val query = DataDebit.filter(_.dataDebitKey === debit.dataDebitKey)
      .map(dd => (dd.rolling, dd.lastUpdated))
      .update((true, LocalDateTime.now()))
    db.run(query)
  }

  def findDataDebitByKey(dataDebitKey: UUID)(implicit db: Database): Future[Option[DataDebitRow]] = {
    db.run(DataDebit.filter(_.dataDebitKey === dataDebitKey).result).map(_.headOption)
  }

  def retrieveDataDebiValues(debit: DataDebitRow, bundleId: Int,
    maybeLimit: Option[Int] = None,
    maybeStartTime: Option[LocalDateTime] = None,
    maybeEndTime: Option[LocalDateTime] = None)(implicit db: Database): Future[ApiDataDebitOut] = {
    // No records created in the future
    val filterEndTime = maybeEndTime.getOrElse(LocalDateTime.now())
    // And by default no records older than a month
    val filterStartTime = maybeStartTime.getOrElse(LocalDateTime.now().minusMonths(1))

    val bundleValues = bundlesService.getBundleContextlessValues(bundleId, maybeLimit, filterStartTime, filterEndTime)
    bundleValues.map { data =>
      ModelTranslation.fromDbModel(debit, Some(data), None)
    }
  }

  def retrieveDataDebitContextualValues(debit: DataDebitRow, bundleId: Int)(implicit db: Database): Future[ApiDataDebitOut] = {
    throw new NotImplementedError("Contextual APIs not implemented")
  }

  def listDataDebits()(implicit db: Database): Future[Seq[ApiDataDebit]] = {
    // FIXME: local import while the rest of the service uses the old slick model
    import org.hatdex.hat.dal.SlickPostgresDriver.api._

    val ddbundleContextlessQuery = for {
      dd <- DataDebit
      bundleContextless <- dd.bundleContextlessFk
    } yield (dd, bundleContextless)

    val ddBundleContextualQuery = for {
      dd <- DataDebit
      bundleContextual <- dd.bundleContextFk
    } yield (dd, bundleContextual)

    val eventualBunleContextless = db.run(ddbundleContextlessQuery.result)
    val eventualBundleContextual = db.run(ddBundleContextualQuery.result)

    val debits = for {
      contextless <- eventualBunleContextless
      contextual <- eventualBundleContextual
    } yield {
      val cless = contextless.map {
        case (dd, bundle) =>
          val apidd = ModelTranslation.fromDbModel(dd)
          val apiBundle = ModelTranslation.fromDbModel(bundle)
          apidd.copy(bundleContextless = Some(apiBundle))
      }
      //      val cfull = contextual.map {
      //        case (dd, bundle) =>
      //          val apidd = ApiDataDebit.fromDbModel(dd)
      //          val apiBundle = ApiBundleContext.fromDbModel(bundle)
      //          apidd.copy(bundleContextual = Some(apiBundle))
      //      }
      cless //++ cfull
    }

    debits
  }
}