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
 * 5 / 2017
 */

package org.hatdex.hat.api.service.richData

import io.dataswift.models.hat._
import org.joda.time.LocalDateTime

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class DataDebitContractServiceSpec extends DataDebitContractServiceContext {

  "The `createDataDebit` method" should "Save a data debit" in {
    val service = application.injector.instanceOf[DataDebitContractService]
    val saved   = service.createDataDebit("testddnew", testDataDebitRequest(), owner.userId)
    saved map { debit =>
      debit.client.email must equal(owner.email)
      debit.dataDebitKey must equal("testddnew")
      debit.bundles.length must equal(1)
      debit.bundles.head.rolling must equal(false)
      debit.bundles.head.enabled must equal(false)
    }
    Await.result(saved, 10.seconds)
  }

  it should "Throw an error when a duplicate data debit is getting saved" in {
    val service = application.injector.instanceOf[DataDebitContractService]
    try for {
      _ <- service.createDataDebit("testddup", testDataDebitRequest(), owner.userId)
      saved <- service.createDataDebit("testddup", testDataDebitRequest(), owner.userId)
    } yield saved
    catch {
      case (e: Exception) =>
        true
      case _: Throwable =>
        fail()
    }
  }

  // "The `dataDebit` method" should "Return a data debit by ID" in {
  //   val service = application.injector.instanceOf[DataDebitContractService]
  //   val saved = for {
  //     _ <- service.createDataDebit("testdd", testDataDebitRequest(), owner.userId)
  //     saved <- service.dataDebit("testdd")
  //   } yield saved

  //   saved map { maybeDebit =>
  //     maybeDebit must not be empty
  //     val debit = maybeDebit.get
  //     debit.client.email must equal(owner.email)
  //     debit.dataDebitKey must equal("testdd")
  //     debit.bundles.length must equal(1)
  //     debit.bundles.head.enabled must equal(false)
  //   }
  //   Await.result(saved, 10.seconds)
  // }

  "The `dataDebit` method" should "Return None when data debit doesn't exist" in {
    val service = application.injector.instanceOf[DataDebitContractService]
    val saved = for {
      saved <- service.dataDebit("testddnotfound")
    } yield saved

    saved map { maybeDebit =>
      maybeDebit must equal(empty)
    }
    Await.result(saved, 10.seconds)
  }

  "The `dataDebitEnable` method" should "Enable an existing data debit" in {
    val service = application.injector.instanceOf[DataDebitContractService]
    val saved = for {
      _ <- service.createDataDebit("testdd1", testDataDebitRequest(), owner.userId)
      _ <- service.dataDebitEnableBundle("testdd1", None)
      saved <- service.dataDebit("testdd1")
    } yield saved

    saved map { maybeDebit =>
      maybeDebit must not be empty
      val debit = maybeDebit.get
      debit.client.email must equal(owner.email)
      debit.dataDebitKey must equal("testdd1")
      debit.bundles.length must equal(1)
      debit.bundles.head.enabled must equal(true)
      debit.activeBundle must equal(empty)
    }
    Await.result(saved, 10.seconds)
  }

  it should "Enable a data debit after a few iterations of bundle adjustments" in {
    val service = application.injector.instanceOf[DataDebitContractService]
    val saved = for {
      _ <- service.createDataDebit("testdd2", testDataDebitRequest(), owner.userId)
      _ <- service.updateDataDebitBundle("testdd2", testDataDebitRequestUpdate(), owner.userId)
      _ <- service.dataDebitEnableBundle("testdd2", Some(testDataDebitRequestUpdate.bundle.name))
      saved <- service.dataDebit("testdd2")
    } yield saved

    saved map { maybeDebit =>
      maybeDebit must not be empty
      val debit = maybeDebit.get
      debit.client.email must equal(owner.email)
      debit.dataDebitKey must equal("testdd2")
      debit.bundles.length must equal(2)
      debit.activeBundle must not be empty
      debit.activeBundle.get.bundle.name must equal(testDataDebitRequestUpdate.bundle.name)
      debit.bundles.exists(_.enabled == false) must equal(true)
    }
    Await.result(saved, 10.seconds)
  }

  "The `dataDebitDisable` method" should "Disable all bundles linked to a data debit" in {
    val service = application.injector.instanceOf[DataDebitContractService]

    val saved = for {
      _ <- service.createDataDebit("testdd3", testDataDebitRequest(), owner.userId)
      _ <- service.dataDebitEnableBundle("testdd3", Some(testDataDebitRequest.bundle.name))
      _ <- service.updateDataDebitBundle("testdd3", testDataDebitRequestUpdate(), owner.userId)
      _ <- service.dataDebitEnableBundle("testdd3", Some(testDataDebitRequestUpdate.bundle.name))
      _ <- service.dataDebitDisable("testdd3")
      saved <- service.dataDebit("testdd3")
    } yield saved

    saved map { maybeDebit =>
      maybeDebit must not be empty
      val debit = maybeDebit.get
      debit.bundles.length must equal(2)
      debit.bundles.exists(_.enabled == true) must equal(false)
    }
    Await.result(saved, 10.seconds)
  }

  "The `updateDataDebitBundle` method" should "Update a data debit by inserting an additional bundle" in {
    val service = application.injector.instanceOf[DataDebitContractService]
    val saved = for {
      saved <- service.createDataDebit("testdd4", testDataDebitRequest(), owner.userId)
      updated <- service.updateDataDebitBundle("testdd4", testDataDebitRequestUpdate(), owner.userId)
    } yield updated

    saved map { debit =>
      debit.client.email must equal(owner.email)
      debit.dataDebitKey must equal("testdd4")
      debit.bundles.length must equal(2)
      debit.bundles.head.enabled must equal(false)
      debit.currentBundle must not be empty
      debit.currentBundle.get.bundle.name must equal(testBundle2.name)
      debit.activeBundle must equal(empty)
    }
    Await.result(saved, 10.seconds)
  }

  it should "Throw an error when updating with an existing bundle" in {
    val service  = application.injector.instanceOf[DataDebitContractService]
    val request1 = testDataDebitRequest()
    try for {
      _ <- service.createDataDebit("testdd5", request1, owner.userId)
      updated <- service.updateDataDebitBundle("testdd5", request1, owner.userId)
    } yield updated
    catch {
      case _: RichDataDuplicateBundleException =>
      // OK
      case _: Throwable =>
        fail()
    }
  }

  "The `all` method" should "List all setup data debits" in {
    val service = application.injector.instanceOf[DataDebitContractService]

    val saved = for {
      _ <- service.createDataDebit("testdd6", testDataDebitRequest(), owner.userId)
      _ <- service.createDataDebit("testdd7", testDataDebitRequestUpdate(), owner.userId)
      saved <- service.all()
    } yield saved

    saved map { debits =>
      debits.length must equal(2)
    }
    Await.result(saved, 10.seconds)
  }
}

class DataDebitContractServiceContext extends RichBundleServiceContext {

  private val uniqueSuffix = new AtomicInteger

  def testDataDebitRequest(): DataDebitRequest = {
    val bundle = testBundle.copy(name = s"${testBundle.name}-${uniqueSuffix.incrementAndGet()}")
    DataDebitRequest(bundle, None, LocalDateTime.now(), LocalDateTime.now().plusDays(3), rolling = false)
  }

  def testDataDebitRequestUpdate(): DataDebitRequest = {
    val bundle = testBundle2.copy(name = s"${testBundle2.name}-${uniqueSuffix.incrementAndGet()}")
    DataDebitRequest(bundle, None, LocalDateTime.now(), LocalDateTime.now().plusDays(3), rolling = false)
  }
}
