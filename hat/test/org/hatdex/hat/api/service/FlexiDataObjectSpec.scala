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
 * 4 / 2017
 */

package org.hatdex.hat.api.service

import java.io.StringReader
import java.util.UUID

import akka.stream.Materializer
import com.atlassian.jwt.core.keys.KeyUtils
import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.test.FakeEnvironment
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SchemaMigration
import org.hatdex.hat.dal.SlickPostgresDriver.backend.Database
import org.hatdex.hat.resourceManagement.{ FakeHatConfiguration, FakeHatServerProvider, HatServer, HatServerProvider }
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.{ BeforeEach, Scope }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{ JsObject, JsValue, Json }
import play.api.test.PlaySpecification
import play.api.{ Application, Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class FlexiDataObjectSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with FlexiDataObjectContext with BeforeEach {

  val logger = Logger(this.getClass)

  def before: Unit = {
    await(databaseReady)(30.seconds)
  }

  sequential

  "The `saveData` method" should {
    "Save a single JSON datapoint and add ID" in {
      val service = application.injector.instanceOf[FlexiDataObject]

      val saved = service.saveData(owner.userId, List(EndpointData("test", None, simpleJson, None)))

      saved map { record =>
        record.length must equalTo(1)
        record.head.recordId must beSome
      } await (3, 10.seconds)
    }

    "Save multiple JSON datapoints" in {
      val service = application.injector.instanceOf[FlexiDataObject]

      val data = List(
        EndpointData("test", None, simpleJson, None),
        EndpointData("test", None, simpleJson2, None),
        EndpointData("complex", None, complexJson, None))
      val saved = service.saveData(owner.userId, data)

      saved map { record =>
        record.length must equalTo(3)
      } await (3, 10.seconds)
    }

    "Refuse to save data with duplicate records" in {
      val service = application.injector.instanceOf[FlexiDataObject]

      val data = List(
        EndpointData("test", None, simpleJson, None),
        EndpointData("test", None, simpleJson2, None),
        EndpointData("complex", None, complexJson, None))

      val saved = for {
        _ <- service.saveData(owner.userId, List(EndpointData("test", None, simpleJson, None)))
        saved <- service.saveData(owner.userId, data)
      } yield saved

      saved must throwA[Exception].await(3, 10.seconds)
    }

    "Save linked JSON datapoints" in {
      val service = application.injector.instanceOf[FlexiDataObject]

      val data = List(
        EndpointData("test", None, simpleJson,
          Some(List(EndpointData("test", None, simpleJson2, None)))))
      val saved = service.saveData(owner.userId, data)

      saved map { record =>
        record.length must equalTo(1)
        record.head.links must beSome
        record.head.links.get.length must equalTo(1)
        (record.head.links.get.head.data \ "field").as[String] must equalTo("value2")
      } await (3, 10.seconds)
    }
  }

  "The `propertyData` method" should {
    "Find test endpoint values and map them to expected json output" in {
      val service = application.injector.instanceOf[FlexiDataObject]

      val data = List(
        EndpointData("test", None, simpleJson, None),
        EndpointData("test", None, simpleJson2, None),
        EndpointData("complex", None, complexJson, None))

      val result = for {
        _ <- service.saveData(owner.userId, data)
        retrieved <- service.propertyData(List(EndpointQuery("test", simpleTransformation, None)), "data.newField", 1)
      } yield retrieved

      result map { result =>
        result.length must equalTo(1)
        (result.head.data \ "data" \ "newField").as[String] must equalTo("anotherFieldDifferentValue")
      } await (3, 10.seconds)
    }

    "Apply different mappers converting from different endpoints into a single format" in {
      val service = application.injector.instanceOf[FlexiDataObject]

      val data = List(
        EndpointData("test", None, simpleJson, None),
        EndpointData("test", None, simpleJson2, None),
        EndpointData("complex", None, complexJson, None))

      val result = for {
        _ <- service.saveData(owner.userId, data)
        retrieved <- service.propertyData(List(
          EndpointQuery("test", simpleTransformation, None),
          EndpointQuery("complex", complexTransformation, None)), "data.newField", 3)
      } yield retrieved

      result map { result =>
        result.length must equalTo(3)
        (result.head.data \ "data" \ "newField").as[String] must equalTo("anotherFieldDifferentValue")
        (result(2).data \ "data" \ "newField").as[String] must equalTo("london, uk")
      } await (3, 10.seconds)
    }
  }

  "The `bundleData` method" should {
    "retrieve values into corresponding properties" in {
      val service = application.injector.instanceOf[FlexiDataObject]

      val data = List(
        EndpointData("test", None, simpleJson, None),
        EndpointData("test", None, simpleJson2, None),
        EndpointData("complex", None, complexJson, None))

      val query = Map(
        "test" -> PropertyQuery(List(EndpointQuery("test", simpleTransformation, None)), "data.newField", 3),
        "complex" -> PropertyQuery(List(EndpointQuery("complex", complexTransformation, None)), "data.newField", 1))
      val result = for {
        _ <- service.saveData(owner.userId, data)
        retrieved <- service.bundleData(query)
      } yield retrieved

      result map { result =>
        result.size must equalTo(2)
        result.get("test") must beSome
        result.get("complex") must beSome
        result("test").length must equalTo(2)
        (result("test").head.data \ "data" \ "newField").as[String] must equalTo("anotherFieldDifferentValue")
        result("complex").length must equalTo(1)
        (result("complex").head.data \ "data" \ "newField").as[String] must equalTo("london, uk")
      } await (3, 10.seconds)
    }
  }

}

trait FlexiDataObjectContext extends Scope {
  // Initialize configuration
  val hatAddress = "hat.hubofallthings.net"
  val hatUrl = s"http://$hatAddress"
  private val configuration = Configuration.from(FakeHatConfiguration.config)
  private val hatConfig = configuration.getConfig(s"hat.$hatAddress").get

  // Build up the FakeEnvironment for authentication testing
  private val keyUtils = new KeyUtils()
  implicit protected def hatDatabase: Database = Database.forConfig("", hatConfig.getConfig("database").get.underlying)
  implicit val hatServer: HatServer = HatServer(hatAddress, "hat", "user@hat.org",
    keyUtils.readRsaPrivateKeyFromPem(new StringReader(hatConfig.getString("privateKey").get)),
    keyUtils.readRsaPublicKeyFromPem(new StringReader(hatConfig.getString("publicKey").get)), hatDatabase)

  // Setup default users for testing
  val owner = HatUser(UUID.randomUUID(), "hatuser", Some("pa55w0rd"), "hatuser", "owner", enabled = true)
  val dataDebitUser = HatUser(UUID.randomUUID(), "dataDebitUser", Some("pa55w0rd"), "dataDebitUser", "dataDebit", enabled = true)
  val dataCreditUser = HatUser(UUID.randomUUID(), "dataCreditUser", Some("pa55w0rd"), "dataCreditUser", "dataCredit", enabled = true)
  implicit val environment: Environment[HatApiAuthEnvironment] = FakeEnvironment[HatApiAuthEnvironment](
    Seq(owner.loginInfo -> owner, dataDebitUser.loginInfo -> dataDebitUser, dataCreditUser.loginInfo -> dataCreditUser),
    hatServer)

  // Helpers to (re-)initialize the test database and await for it to be ready
  val devHatMigrations = Seq(
    "evolutions/hat-database-schema/11_hat.sql",
    "evolutions/hat-database-schema/12_hatEvolutions.sql",
    "evolutions/hat-database-schema/13_liveEvolutions.sql",
    "evolutions/hat-database-schema/14_newHat.sql")

  def databaseReady: Future[Unit] = {
    val schemaMigration = application.injector.instanceOf[SchemaMigration]
    schemaMigration.resetDatabase()(hatDatabase)
      .flatMap(_ => schemaMigration.run(devHatMigrations)(hatDatabase))
      .flatMap { _ =>
        val usersService = application.injector.instanceOf[UsersService]
        for {
          _ <- usersService.saveUser(dataCreditUser)
          _ <- usersService.saveUser(dataDebitUser)
          _ <- usersService.saveUser(owner)
        } yield ()
      }
  }

  /**
   * A fake Guice module.
   */
  class FakeModule extends AbstractModule with ScalaModule {
    val fileManagerS3Mock = FileManagerS3Mock()

    def configure(): Unit = {
      bind[Environment[HatApiAuthEnvironment]].toInstance(environment)
      bind[HatServerProvider].toInstance(new FakeHatServerProvider(hatServer))
    }
  }

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(FakeHatConfiguration.config)
    .overrides(new FakeModule)
    .build()

  implicit lazy val materializer: Materializer = application.materializer

  val simpleJson: JsValue = Json.parse(
    """
      | {
      |   "field": "value",
      |   "anotherField": "anotherFieldValue",
      |   "object": {
      |     "objectField": "objectFieldValue",
      |     "objectFieldArray": ["objectFieldArray1", "objectFieldArray2", "objectFieldArray3"],
      |     "objectFieldObjectArray": [
      |       {"subObjectName": "subObject1", "subObjectName2": "subObject1-2"},
      |       {"subObjectName": "subObject2", "subObjectName2": "subObject2-2"}
      |     ]
      |   }
      | }
    """.stripMargin)

  val simpleJson2: JsValue = Json.parse(
    """
      | {
      |   "field": "value2",
      |   "anotherField": "anotherFieldDifferentValue",
      |   "object": {
      |     "objectField": "objectFieldValue",
      |     "objectFieldArray": ["objectFieldArray1", "objectFieldArray2", "objectFieldArray3"],
      |     "objectFieldObjectArray": [
      |       {"subObjectName": "subObject1", "subObjectName2": "subObject1-2"},
      |       {"subObjectName": "subObject2", "subObjectName2": "subObject2-2"}
      |     ]
      |   }
      | }
    """.stripMargin)

  val complexJson: JsValue = Json.parse(
    """
      | {
      |  "birthday": "01/01/1970",
      |  "age_range": {
      |    "min": 18
      |  },
      |  "education": [
      |    {
      |      "school": {
      |        "id": "123456789",
      |        "name": "school name"
      |      },
      |      "type": "High School",
      |      "year": {
      |        "id": "123456789",
      |        "name": "1972"
      |      },
      |      "id": "123456789"
      |    },
      |    {
      |      "concentration": [
      |        {
      |          "id": "123456789",
      |          "name": "Computer science"
      |        }
      |      ],
      |      "school": {
      |        "id": "12345678910",
      |        "name": "university name"
      |      },
      |      "type": "Graduate School",
      |      "year": {
      |        "id": "123456889",
      |        "name": "1973"
      |      },
      |      "id": "12345678910"
      |    }
      |  ],
      |  "email": "email@example.com",
      |  "hometown": {
      |    "id": "12345678910",
      |    "name": "london, uk"
      |  },
      |  "locale": "en_GB",
      |  "id": "12345678910"
      |}
    """.stripMargin)

  val simpleTransformation: JsObject = Json.parse(
    """
      | {
      |   "data.newField": "anotherField",
      |   "data.arrayField": "object.objectFieldArray",
      |   "data.onemore": "object.education[1]"
      | }
    """.stripMargin).as[JsObject]

  val complexTransformation: JsObject = Json.parse(
    """
      | {
      |   "data.newField": "hometown.name",
      |   "data.arrayField": "education",
      |   "data.onemore": "education[0].type"
      | }
    """.stripMargin).as[JsObject]
}