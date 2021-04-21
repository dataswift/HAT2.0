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
 * 4 / 2018
 */

package org.hatdex.hat.api.service.richData

import io.dataswift.models.hat._
import org.scalatest.LoneElement

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

class DataDebitServiceSpec extends DataDebitServiceSpecContext with LoneElement {

  before {
    import org.hatdex.hat.dal.Tables._
    import org.hatdex.libs.dal.HATPostgresProfile.api._
    val actions = DBIO.seq(
      DataDebitPermissions.delete,
      DataDebit.delete,
      DataCombinators.delete,
      DataBundles.filter(_.bundleId.like("test%")).delete,
      DataJsonGroupRecords.delete,
      DataJsonGroups.delete,
      DataJson.delete
    )
    await(db.run(actions.transactionally))
  }

  "The `createDataDebit` method" should "Save a data debit" in {
    val service = application.injector.instanceOf[DataDebitService]
    val saved   = await(service.createDataDebit("testdd", testDataDebitRequest, owner.userId))
    saved.dataDebitKey must equal("testdd")
    saved.permissions.loneElement.active mustBe false
  }

  it should "Throw an error when a duplicate data debit is getting saved" in {
    val service = application.injector.instanceOf[DataDebitService]
    val saved = for {
      _ <- service.createDataDebit("testdd2", testDataDebitRequest, owner.userId)
      saved <- service.createDataDebit("testdd2", testDataDebitRequestUpdate, owner.userId)
    } yield saved
    intercept[RichDataDuplicateDebitException](await(saved))
  }

  it should "Throw an error when a different data debit with same bundle ID is getting saved" in {
    val service = application.injector.instanceOf[DataDebitService]
    val saved = for {
      _ <- service.createDataDebit("testdd", testDataDebitRequest, owner.userId)
      saved <-
        service.createDataDebit("testdd2", testDataDebitDetailsUpdate.copy(dataDebitKey = "testdd2"), owner.userId)
    } yield saved
    intercept[RichDataDuplicateBundleException](await(saved))
  }

  "The `dataDebit` method" should "Return a data debit by ID" in {
    val service = application.injector.instanceOf[DataDebitService]
    val saved = for {
      _ <- service.createDataDebit("testdd", testDataDebitRequest, owner.userId)
      _ <- service.createDataDebit("testdd2", testDataDebitRequestUpdate, owner.userId)
      saved <- service.dataDebit("testdd")
    } yield saved
    val debit = await(saved).value

    debit.dataDebitKey mustBe "testdd"
    val permission = debit.permissions.loneElement
    permission.active mustBe false
    permission.bundle.name mustBe testDataDebitRequest.bundle.name
  }

  "The `dataDebit` method" should "Return None when data debit doesn't exist" in {
    val service = application.injector.instanceOf[DataDebitService]
    await(service.dataDebit("testdd")) mustBe None
  }

  "The `dataDebitEnable` method" should "Enable an existing data debit" in {
    val service = application.injector.instanceOf[DataDebitService]
    val saved = for {
      _ <- service.createDataDebit("testdd", testDataDebitRequest, owner.userId)
      _ <- service.dataDebitEnableNewestPermissions("testdd")
      saved <- service.dataDebit("testdd")
    } yield saved

    val debit = await(saved).value
    debit.dataDebitKey must equal("testdd")
    debit.permissions.loneElement.active mustBe true
    debit.activePermissions must not be empty
  }

  it should "Enable a data debit after a few iterations of bundle adjustments" in {
    val service = application.injector.instanceOf[DataDebitService]
    val saved = for {
      _ <- service.createDataDebit("testdd", testDataDebitRequest, owner.userId)
      _ <- service.updateDataDebitPermissions("testdd", testDataDebitRequestUpdate, owner.userId)
      _ <- service.dataDebitEnableNewestPermissions("testdd")
      saved <- service.dataDebit("testdd")
    } yield saved

    val debit = await(saved).value
    debit.dataDebitKey must equal("testdd")
    debit.permissions.length must equal(2)
    debit.activePermissions.value.bundle.name must equal(testDataDebitRequestUpdate.bundle.name)
    debit.permissions.exists(_.active == false) mustBe true
  }

  "The `dataDebitDisable` method" should "Disable all bundles linked to a data debit" in {
    val service = application.injector.instanceOf[DataDebitService]

    val saved = for {
      _ <- service.createDataDebit("testdd", testDataDebitRequest, owner.userId)
      _ <- service.dataDebitEnableNewestPermissions("testdd")
      _ <- service.updateDataDebitPermissions("testdd", testDataDebitRequestUpdate, owner.userId)
      _ <- service.dataDebitEnableNewestPermissions("testdd")
      _ <- service.dataDebitDisable("testdd", cancelAtPeriodEnd = false)
      saved <- service.dataDebit("testdd")
    } yield saved

    val debit = await(saved).value
    debit.permissions.length must equal(2)
    debit.permissions.exists(_.active == true) mustBe false
  }

  "The `updateDataDebitPermissions` method" should "Update a data debit by inserting an additional bundle" in {
    val service = application.injector.instanceOf[DataDebitService]
    val saved = for {
      _ <- service.createDataDebit("testdd", testDataDebitRequest, owner.userId)
      updated <- service.updateDataDebitPermissions("testdd", testDataDebitRequestUpdate, owner.userId)
    } yield updated

    val debit = await(saved)
    debit.dataDebitKey must equal("testdd")
    debit.permissions.length must equal(2)
    debit.permissions.head.active mustBe false
    debit.currentPermissions.value.bundle.name must equal(testBundle2.name)
    debit.activePermissions mustBe None
  }

  it should "Update a data debit by inserting an additional conditions bundle" in {
    val service = application.injector.instanceOf[DataDebitService]
    val saved = for {
      _ <- service.createDataDebit("testdd", testDataDebitRequest, owner.userId)
      updated <- service.updateDataDebitPermissions("testdd", testDataDebitRequestUpdateConditions, owner.userId)
    } yield updated

    val debit = await(saved)
    debit.dataDebitKey must equal("testdd")
    debit.permissions.length must equal(2)
    debit.permissions.head.active mustBe false
    debit.currentPermissions.value.bundle.name must equal(testBundle2.name)
    debit.activePermissions mustBe None
  }

  it should "Update data debit without changing bundle linked to this debit" in {
    val service = application.injector.instanceOf[DataDebitService]
    val saved = for {
      _ <- service.createDataDebit("testdd", testDataDebitRequest, owner.userId)
      updated <- service.updateDataDebitPermissions("testdd", testDataDebitDetailsUpdate, owner.userId)
    } yield updated

    val debit = await(saved)
    debit.dataDebitKey must equal("testdd")
    debit.permissions.length must equal(2)
    debit.permissions.head.active mustBe false
    debit.currentPermissions.value.bundle.name must equal(testBundle.name)
    debit.activePermissions mustBe None
  }

  it should "Throw an error when updating data debit that does not already exist" in {
    val service = application.injector.instanceOf[DataDebitService]
    intercept[RichDataDebitException](
      await(service.updateDataDebitPermissions("testdd2", testDataDebitDetailsUpdate, owner.userId))
    )
  }

  it should "Throw an error when updating data debit with bundle linked to another debit" in {
    val service = application.injector.instanceOf[DataDebitService]
    val updatedDataDebit = for {
      _ <- service.createDataDebit("testdd", testDataDebitRequest, owner.userId)
      _ <- service.createDataDebit("testdd2", testDataDebitRequestUpdate, owner.userId)
      updated <-
        service.updateDataDebitPermissions("testdd2",
                                           testDataDebitRequestUpdate.copy(bundle = testDataDebitRequest.bundle),
                                           owner.userId
        )
    } yield updated
    intercept[RichDataDuplicateBundleException](await(updatedDataDebit))
  }

  it should "Throw an error when updating data debit with conditions bundle linked to another debit" in {
    val service = application.injector.instanceOf[DataDebitService]
    val updatedDataDebit = for {
      _ <- service.createDataDebit("testdd", testDataDebitRequest, owner.userId)
      _ <- service.createDataDebit("testdd2", testDataDebitRequestUpdate, owner.userId)
      updated <- service.updateDataDebitPermissions(
                   "testdd2",
                   testDataDebitRequestUpdate.copy(conditions = testDataDebitRequest.conditions),
                   owner.userId
                 )
    } yield updated
    intercept[RichDataDuplicateBundleException](await(updatedDataDebit))
  }

  // Fails with duplicate bundle ID
  "The `all` method" should "List all setup data debits" in {
    val service = application.injector.instanceOf[DataDebitService]

    val saved = for {
      _ <- service.createDataDebit("testddA", testDataDebitRequestA, owner.userId)
      _ <- service.createDataDebit("testddB", testDataDebitRequestB, owner.userId)
      saved <- service.all()
    } yield saved
    await(saved).length mustBe 2
  }

  def await[T](block: => Future[T]): T = Await.result(block, 10.seconds)

}

class DataDebitServiceSpecContext extends RichBundleServiceContext {
  val testDataDebitRequest: DataDebitSetupRequest = DataDebitSetupRequest(
    "testdd",
    "purpose of the data use",
    org.joda.time.DateTime.now(),
    org.joda.time.Duration.standardDays(5),
    false,
    "clientName",
    "http://client.com",
    "http://client.com/logo.png",
    None,
    None,
    Some("Detailed description of the data debit"),
    "http://client.com/terms.html",
    Some(conditionsBundle),
    testBundle
  )

  val testDataDebitRequestA: DataDebitSetupRequest = DataDebitSetupRequest(
    "testddA",
    "purpose of the data use",
    org.joda.time.DateTime.now(),
    org.joda.time.Duration.standardDays(5),
    false,
    "clientName",
    "http://client.com",
    "http://client.com/logo.png",
    None,
    None,
    Some("Detailed description of the data debit"),
    "http://client.com/terms.html",
    Some(conditionsBundle),
    testBundleA
  )

  val testDataDebitRequestB: DataDebitSetupRequest = DataDebitSetupRequest(
    "testddB",
    "purpose of the data use",
    org.joda.time.DateTime.now(),
    org.joda.time.Duration.standardDays(5),
    false,
    "clientName",
    "http://client.com",
    "http://client.com/logo.png",
    None,
    None,
    Some("Detailed description of the data debit"),
    "http://client.com/terms.html",
    Some(conditionsBundle2),
    testBundleB
  )

  val testDataDebitDetailsUpdate: DataDebitSetupRequest = DataDebitSetupRequest(
    "testdd",
    "updated purpose of the data use",
    org.joda.time.DateTime.now(),
    org.joda.time.Duration.standardDays(15),
    false,
    "clientName",
    "http://client.com",
    "http://client.com/logo.png",
    None,
    None,
    Some("Detailed description of the data debit"),
    "http://client.com/terms.html",
    Some(conditionsBundle),
    testBundle
  )

  val testDataDebitRequestUpdate: DataDebitSetupRequest = DataDebitSetupRequest(
    "testdd",
    "updated purpose of the data use",
    org.joda.time.DateTime.now(),
    org.joda.time.Duration.standardDays(10),
    false,
    "clientName",
    "http://client.com",
    "http://client.com/logo.png",
    None,
    None,
    Some("Detailed description of the data debit"),
    "http://client.com/terms.html",
    None,
    testBundle2
  )

  val testDataDebitRequestUpdateConditions: DataDebitSetupRequest = DataDebitSetupRequest(
    "testdd",
    "updated purpose of the data use",
    org.joda.time.DateTime.now(),
    org.joda.time.Duration.standardDays(10),
    false,
    "clientName",
    "http://client.com",
    "http://client.com/logo.png",
    None,
    None,
    Some("Detailed description of the data debit"),
    "http://client.com/terms.html",
    Some(conditionsBundle2),
    testBundle2
  )
}
