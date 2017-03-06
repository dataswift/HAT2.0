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
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeEach
import play.api.Logger
import play.api.test.PlaySpecification

import scala.concurrent.ExecutionContext.Implicits.global
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
}

