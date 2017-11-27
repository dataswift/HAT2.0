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

import org.hatdex.hat.api.HATTestContext
import org.hatdex.hat.api.models._
import org.joda.time.LocalDateTime
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeEach
import play.api.Logger
import play.api.libs.json.{ JsObject, Json }
import play.api.test.PlaySpecification

import scala.concurrent.duration._

class DataDebitContractServiceSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with DataDebitContractServiceContext with BeforeEach {

  val logger = Logger(this.getClass)

  sequential

  "The `createDataDebit` method" should {
    "Save a data debit" in {
      val service = application.injector.instanceOf[DataDebitContractService]
      val saved = service.createDataDebit("dd", testDataDebitRequest, owner.userId)
      saved map { debit =>
        debit.client.email must be equalTo (owner.email)
        debit.dataDebitKey must be equalTo ("dd")
        debit.bundles.length must be equalTo (1)
        debit.bundles.head.rolling must beFalse
        debit.bundles.head.enabled must beFalse
      } await (3, 10.seconds)
    }

    "Throw an error when a duplicate data debit is getting saved" in {
      val service = application.injector.instanceOf[DataDebitContractService]
      val saved = for {
        _ <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        saved <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
      } yield saved

      saved must throwA[Exception].await(3, 10.seconds)
    }
  }

  "The `dataDebit` method" should {
    "Return a data debit by ID" in {
      val service = application.injector.instanceOf[DataDebitContractService]
      val saved = for {
        _ <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        saved <- service.dataDebit("dd")
      } yield saved

      saved map { maybeDebit =>
        maybeDebit must beSome
        val debit = maybeDebit.get
        debit.client.email must be equalTo (owner.email)
        debit.dataDebitKey must be equalTo ("dd")
        debit.bundles.length must be equalTo (1)
        debit.bundles.head.enabled must beFalse
      } await (3, 10.seconds)
    }

    "Return None when data debit doesn't exist" in {
      val service = application.injector.instanceOf[DataDebitContractService]
      val saved = for {
        saved <- service.dataDebit("dd")
      } yield saved

      saved map { maybeDebit =>
        maybeDebit must beNone
      } await (3, 10.seconds)
    }
  }

  "The `dataDebitEnable` method" should {
    "Enable an existing data debit" in {
      val service = application.injector.instanceOf[DataDebitContractService]
      val saved = for {
        _ <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        _ <- service.dataDebitEnableBundle("dd", None)
        saved <- service.dataDebit("dd")
      } yield saved

      saved map { maybeDebit =>
        maybeDebit must beSome
        val debit = maybeDebit.get
        debit.client.email must be equalTo (owner.email)
        debit.dataDebitKey must be equalTo ("dd")
        debit.bundles.length must be equalTo (1)
        debit.bundles.head.enabled must beTrue
        debit.activeBundle must beSome
      } await (3, 10.seconds)
    }

    "Enable a data debit after a few iterations of bundle adjustments" in {
      val service = application.injector.instanceOf[DataDebitContractService]
      val saved = for {
        _ <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        _ <- service.updateDataDebitBundle("dd", testDataDebitRequestUpdate, owner.userId)
        _ <- service.dataDebitEnableBundle("dd", Some(testDataDebitRequestUpdate.bundle.name))
        saved <- service.dataDebit("dd")
      } yield saved

      saved map { maybeDebit =>
        maybeDebit must beSome
        val debit = maybeDebit.get
        debit.client.email must be equalTo (owner.email)
        debit.dataDebitKey must be equalTo ("dd")
        debit.bundles.length must be equalTo (2)
        debit.activeBundle must beSome
        debit.activeBundle.get.bundle.name must be equalTo (testDataDebitRequestUpdate.bundle.name)
        debit.bundles.exists(_.enabled == false) must beTrue
      } await (3, 10.seconds)
    }
  }

  "The `dataDebitDisable` method" should {
    "Disable all bundles linked to a data debit" in {
      val service = application.injector.instanceOf[DataDebitContractService]

      val saved = for {
        _ <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        _ <- service.dataDebitEnableBundle("dd", Some(testDataDebitRequest.bundle.name))
        _ <- service.updateDataDebitBundle("dd", testDataDebitRequestUpdate, owner.userId)
        _ <- service.dataDebitEnableBundle("dd", Some(testDataDebitRequestUpdate.bundle.name))
        _ <- service.dataDebitDisable("dd")
        saved <- service.dataDebit("dd")
      } yield saved

      saved map { maybeDebit =>
        maybeDebit must beSome
        val debit = maybeDebit.get
        debit.bundles.length must be equalTo (2)
        debit.bundles.exists(_.enabled == true) must beFalse
      } await (3, 10.seconds)
    }
  }

  "The `updateDataDebitBundle` method" should {
    "Update a data debit by inserting an additional bundle" in {
      val service = application.injector.instanceOf[DataDebitContractService]
      val saved = for {
        saved <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        updated <- service.updateDataDebitBundle("dd", testDataDebitRequestUpdate, owner.userId)
      } yield updated

      saved map { debit =>
        debit.client.email must be equalTo (owner.email)
        debit.dataDebitKey must be equalTo ("dd")
        debit.bundles.length must be equalTo (2)
        debit.bundles.head.enabled must beFalse
        debit.currentBundle must beSome
        debit.currentBundle.get.bundle.name must be equalTo (testBundle2.name)
        debit.activeBundle must beNone
      } await (3, 10.seconds)
    }

    "Throw an error when updating with an existing bundle" in {
      val service = application.injector.instanceOf[DataDebitContractService]
      val saved = for {
        saved <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        updated <- service.updateDataDebitBundle("dd", testDataDebitRequestUpdate.copy(bundle = testDataDebitRequest.bundle), owner.userId)
      } yield updated

      saved must throwA[RichDataDuplicateBundleException].await(3, 10.seconds)
    }
  }

  "The `all` method" should {
    "List all setup data debits" in {
      val service = application.injector.instanceOf[DataDebitContractService]

      val saved = for {
        _ <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        _ <- service.createDataDebit("dd2", testDataDebitRequestUpdate, owner.userId)
        saved <- service.all()
      } yield saved

      saved map { debits =>
        debits.length must be equalTo 2
      } await (3, 10.seconds)
    }
  }

}

trait DataDebitContractServiceContext extends HATTestContext {
  private val simpleTransformation: JsObject = Json.parse(
    """
      | {
      |   "data.newField": "anotherField",
      |   "data.arrayField": "object.objectFieldArray",
      |   "data.onemore": "object.education[1]"
      | }
    """.stripMargin).as[JsObject]

  private val complexTransformation: JsObject = Json.parse(
    """
      | {
      |   "data.newField": "hometown.name",
      |   "data.arrayField": "education",
      |   "data.onemore": "education[0].type"
      | }
    """.stripMargin).as[JsObject]

  val testEndpointQuery = Seq(
    EndpointQuery("test", Some(simpleTransformation), None, None),
    EndpointQuery("complex", Some(complexTransformation), None, None))

  val testEndpointQueryUpdated = Seq(
    EndpointQuery("test", Some(simpleTransformation), None, None),
    EndpointQuery("anothertest", None, None, None))

  val testBundle = EndpointDataBundle("testBundle", Map(
    "test" -> PropertyQuery(List(EndpointQuery("test", Some(simpleTransformation), None, None)), Some("data.newField"), None, Some(3)),
    "complex" -> PropertyQuery(List(EndpointQuery("complex", Some(complexTransformation), None, None)), Some("data.newField"), None, Some(1))))

  val testBundle2 = EndpointDataBundle("testBundle2", Map(
    "test" -> PropertyQuery(List(EndpointQuery("test", Some(simpleTransformation), None, None)), Some("data.newField"), None, Some(3)),
    "complex" -> PropertyQuery(List(EndpointQuery("anothertest", None, None, None)), Some("data.newField"), None, Some(1))))

  val testDataDebitRequest = DataDebitRequest(testBundle, None, LocalDateTime.now(), LocalDateTime.now().plusDays(3), rolling = false)

  val testDataDebitRequestUpdate = DataDebitRequest(testBundle2, None, LocalDateTime.now(), LocalDateTime.now().plusDays(3), rolling = false)
}
