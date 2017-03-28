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
 * 3 / 2017
 */

package org.hatdex.hat.api.service

import java.util.UUID

import org.hatdex.hat.api.models._
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.Tables.{ DataField, DataTableTree }
import org.joda.time.LocalDateTime
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeEach
import play.api.Logger
import play.api.test.PlaySpecification

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class DataServiceSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with DataServiceContext with BeforeEach {

  val logger = Logger(this.getClass)

  def before: Unit = {
    await(databaseReady)(10.seconds)
  }

  sequential

  "The `createTable` method" should {
    "Insert a simple data table" in {
      val service = application.injector.instanceOf[DataService]

      val simpleTable = ApiDataTable(None, None, None, "testTable", "test", None, None)

      val table = service.createTable(simpleTable)

      table map { t =>
        t.id must beSome
        t.subTables must beNone
        t.fields must beNone
        t.name must equalTo("testTable")
        t.source must equalTo("test")
        t.dateCreated must beSome
        t.lastUpdated must beSome
      } await (3, 10.seconds)
    }

    "Insert a nested data table" in {
      val service = application.injector.instanceOf[DataService]

      val simpleTable = ApiDataTable(None, None, None, "testTable", "test", None,
        Some(Seq(ApiDataTable(None, None, None, "testSubTable", "test", None, None))))

      service.createTable(simpleTable) map { t =>
        t.id must beSome
        t.subTables must beSome
        t.fields must beNone
        t.name must equalTo("testTable")
        t.source must equalTo("test")
        t.dateCreated must beSome
        t.lastUpdated must beSome
      } await (3, 10.seconds)
    }

    "Insert a nested data table with fields" in {
      val service = application.injector.instanceOf[DataService]

      val simpleTable = ApiDataTable(None, None, None, "testTable", "test",
        Some(Seq(ApiDataField(None, None, None, None, "field", None))),
        Some(Seq(ApiDataTable(None, None, None, "testSubTable", "test", None, None))))

      service.createTable(simpleTable) map { t =>
        t.id must beSome
        t.subTables must beSome
        t.subTables.get.map(_.name) must contain("testSubTable")
        t.fields must beSome
        t.fields.get.map(_.name) must contain("field")
        t.name must equalTo("testTable")
        t.source must equalTo("test")
        t.dateCreated must beSome
        t.lastUpdated must beSome
      } await (3, 10.seconds)
    }

    "Raise error when inserting invalid table structure" in {
      val service = application.injector.instanceOf[DataService]

      val simpleTable = ApiDataTable(None, None, None, "testTable", "test", None,
        Some(Seq(ApiDataTable(None, None, None, "testTable", "test", None, None))))

      val eventualTable = service.createTable(simpleTable)
      eventualTable must throwA[Exception].await

      service.findTable("testTable", "test") should beEmpty[Seq[ApiDataTable]].await
    }
  }

  "The `fieldsetValues` method" should {
    "Return an inserted value for a fieldset" in {
      val service = application.injector.instanceOf[DataService]

      val simpleTable = ApiDataTable(None, None, None, "testTable", "test",
        Some(Seq(ApiDataField(None, None, None, None, "field", None))),
        Some(Seq(ApiDataTable(None, None, None, "testSubTable", "test", None, None))))

      val response = for {
        table <- service.createTable(simpleTable)
        field <- Future.successful(table.fields.get.find(_.name == "field").get)
        _ <- service.storeRecordValues(Seq(
          ApiRecordValues(
            ApiDataRecord(None, None, None, "testRecord1", None),
            Seq(ApiDataValue(None, None, None, "testValue1", Some(ApiDataField(field.id, None, None, None, "field", None)), None)))))
        values <- service.fieldsetValues(fieldsetQuery(table.id.get), LocalDateTime.now().minusDays(7), LocalDateTime.now(), 1000)
      } yield {
        values.length must equalTo(1)
      }

      response.await
    }

    "Return an inserted value for a fieldset with a limit" in {
      val service = application.injector.instanceOf[DataService]

      val simpleTable = ApiDataTable(None, None, None, "testTable", "test",
        Some(Seq(ApiDataField(None, None, None, None, "field", None))),
        Some(Seq(ApiDataTable(None, None, None, "testSubTable", "test", None, None))))

      val response = for {
        table <- service.createTable(simpleTable)
        field <- Future.successful(table.fields.get.find(_.name == "field").get)
        _ <- service.storeRecordValues(Seq(
          ApiRecordValues(
            ApiDataRecord(None, None, None, "testRecord1", None),
            Seq(ApiDataValue(None, None, None, "testValue1", Some(ApiDataField(field.id, None, None, None, "field", None)), None)))))
        values <- service.fieldsetValues(fieldsetQuery(table.id.get), LocalDateTime.now().minusDays(7), LocalDateTime.now(), 1)
      } yield {
        values.length must equalTo(1)
      }

      response.await
    }

    "Return multiple inserted values for a fieldset with a limit" in {
      val service = application.injector.instanceOf[DataService]

      val simpleTable = ApiDataTable(None, None, None, "testTable", "test",
        Some(Seq(ApiDataField(None, None, None, None, "field", None), ApiDataField(None, None, None, None, "anotherfield", None))),
        Some(Seq(ApiDataTable(None, None, None, "testSubTable", "test", None, None))))

      val response = for {
        table <- service.createTable(simpleTable)
        field <- Future.successful(table.fields.get.find(_.name == "field").get)
        _ <- service.storeRecordValues(Seq(
          ApiRecordValues(
            ApiDataRecord(None, None, None, "testRecord1", None),
            Seq(
              ApiDataValue(None, None, None, "testValue1", Some(ApiDataField(field.id, None, None, None, "field", None)), None),
              ApiDataValue(None, None, None, "testValue1-2", Some(ApiDataField(field.id, None, None, None, "anotherfield", None)), None))),
          ApiRecordValues(
            ApiDataRecord(None, None, None, "testRecord2", None),
            Seq(
              ApiDataValue(None, None, None, "testValue2", Some(ApiDataField(field.id, None, None, None, "field", None)), None),
              ApiDataValue(None, None, None, "testValue2-2", Some(ApiDataField(field.id, None, None, None, "anotherfield", None)), None)))))
        values <- service.fieldsetValues(fieldsetQuery(table.id.get), LocalDateTime.now().minusDays(7), LocalDateTime.now(), 2)
      } yield {
        val string = values.toString
        string must contain("testValue1")
        string must contain("testValue2")
        string must contain("testValue1-2")
        string must contain("testValue2-2")
      }

      response.await
    }

    "Leave out older results the correct number of results" in {
      val service = application.injector.instanceOf[DataService]

      val simpleTable = ApiDataTable(None, None, None, "testTable", "test",
        Some(Seq(ApiDataField(None, None, None, None, "field", None), ApiDataField(None, None, None, None, "anotherfield", None))),
        Some(Seq(ApiDataTable(None, None, None, "testSubTable", "test", None, None))))

      val response = for {
        table <- service.createTable(simpleTable)
        field <- Future.successful(table.fields.get.find(_.name == "field").get)
        _ <- service.storeRecordValues(Seq(
          ApiRecordValues(
            ApiDataRecord(None, None, None, "testRecord1", None),
            Seq(
              ApiDataValue(None, None, None, "testValue1", Some(ApiDataField(field.id, None, None, None, "field", None)), None),
              ApiDataValue(None, None, None, "testValue1-2", Some(ApiDataField(field.id, None, None, None, "anotherfield", None)), None)))))
        _ <- service.storeRecordValues(Seq(
          ApiRecordValues(
            ApiDataRecord(None, None, None, "testRecord2", None),
            Seq(
              ApiDataValue(None, None, None, "testValue2", Some(ApiDataField(field.id, None, None, None, "field", None)), None),
              ApiDataValue(None, None, None, "testValue2-2", Some(ApiDataField(field.id, None, None, None, "anotherfield", None)), None))),
          ApiRecordValues(
            ApiDataRecord(None, None, None, "testRecord3", None),
            Seq(
              ApiDataValue(None, None, None, "testValue3", Some(ApiDataField(field.id, None, None, None, "field", None)), None),
              ApiDataValue(None, None, None, "testValue3-2", Some(ApiDataField(field.id, None, None, None, "anotherfield", None)), None)))))
        values <- service.fieldsetValues(fieldsetQuery(table.id.get), LocalDateTime.now().minusDays(7), LocalDateTime.now(), 2)
      } yield {
        val string = values.toString
        string must not contain ("testValue1")
        string must not contain ("testValue1-2")
        string must contain("testValue2")
        string must contain("testValue2-2")
        string must contain("testValue3")
        string must contain("testValue3-2")
      }

      response.await
    }

    "Return the correct number of results" in {
      val service = application.injector.instanceOf[DataService]

      val simpleTable = ApiDataTable(None, None, None, "testTable", "test",
        Some(Seq(ApiDataField(None, None, None, None, "field", None), ApiDataField(None, None, None, None, "anotherfield", None))),
        Some(Seq(ApiDataTable(None, None, None, "testSubTable", "test", None, None))))

      val response = for {
        table <- service.createTable(simpleTable)
        field <- Future.successful(table.fields.get.find(_.name == "field").get)
        _ <- service.storeRecordValues(Seq(
          ApiRecordValues(
            ApiDataRecord(None, None, None, "testRecord1", None),
            Seq(
              ApiDataValue(None, None, None, "testValue1", Some(ApiDataField(field.id, None, None, None, "field", None)), None),
              ApiDataValue(None, None, None, "testValue1-2", Some(ApiDataField(field.id, None, None, None, "anotherfield", None)), None)))))
        _ <- service.storeRecordValues(Seq(
          ApiRecordValues(
            ApiDataRecord(None, None, None, "testRecord2", None),
            Seq(
              ApiDataValue(None, None, None, "testValue2", Some(ApiDataField(field.id, None, None, None, "field", None)), None),
              ApiDataValue(None, None, None, "testValue2-2", Some(ApiDataField(field.id, None, None, None, "anotherfield", None)), None))),
          ApiRecordValues(
            ApiDataRecord(None, None, None, "testRecord3", None),
            Seq(
              ApiDataValue(None, None, None, "testValue3", Some(ApiDataField(field.id, None, None, None, "field", None)), None),
              ApiDataValue(None, None, None, "testValue3-2", Some(ApiDataField(field.id, None, None, None, "anotherfield", None)), None)))))
        values <- service.fieldsetValues(fieldsetQuery(table.id.get), LocalDateTime.now().minusDays(7), LocalDateTime.now(), 3)
      } yield {
        val string = values.toString
        string must contain("testValue1")
        string must contain("testValue1-2")
        string must contain("testValue2")
        string must contain("testValue2-2")
        string must contain("testValue3")
        string must contain("testValue3-2")
      }

      response.await
    }
  }
}

