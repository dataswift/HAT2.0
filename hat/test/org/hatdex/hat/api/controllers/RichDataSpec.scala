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

package org.hatdex.hat.api.controllers

import java.io.StringReader
import java.util.UUID

import akka.stream.Materializer
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3Client }
import com.atlassian.jwt.core.keys.KeyUtils
import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.test._
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service._
import org.hatdex.hat.api.service.richData.{ DataDebitContractService, RichDataService }
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.api.models.{ DataCredit, DataDebitOwner, Owner }
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SchemaMigration
import org.hatdex.hat.dal.SlickPostgresDriver.backend.Database
import org.hatdex.hat.resourceManagement.{ FakeHatConfiguration, FakeHatServerProvider, HatServer, HatServerProvider }
import org.joda.time.LocalDateTime
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.{ BeforeEach, Scope }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{ JsObject, JsValue, Json }
import play.api.mvc.Result
import play.api.test.{ FakeRequest, Helpers, PlaySpecification }
import play.api.{ Application, Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class RichDataSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with RichDataContext with BeforeEach {

  val logger = Logger(this.getClass)

  import org.hatdex.hat.api.models.RichDataJsonFormats._

  def before: Unit = {
    await(databaseReady)(30.seconds)
  }

  sequential

  "The `registerBundle` method" should {
    "return accepted debit if data debit is registered" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)
        .withJsonBody(Json.toJson(testDataDebitRequest))

      val controller = application.injector.instanceOf[RichData]
      val result: Future[Result] = Helpers.call(controller.registerDataDebit("dd"), request)

      status(result) must equalTo(CREATED)
      val debit = contentAsJson(result).as[RichDataDebit]
      debit.dataDebitKey must equalTo("dd")
      debit.bundles.exists(_.enabled) must beFalse
      debit.bundles.length must equalTo(1)
    }

    "return status 400 if inserting duplicate data debit" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)
        .withJsonBody(Json.toJson(testDataDebitRequest))

      val controller = application.injector.instanceOf[RichData]
      val result: Future[Result] = for {
        _ <- Helpers.call(controller.registerDataDebit("dd"), request)
        debit <- Helpers.call(controller.registerDataDebit("dd"), request)
      } yield debit

      status(result) must equalTo(BAD_REQUEST)
    }
  }

  "The `updateDataDebit` method" should {
    "Update data debit" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)
        .withJsonBody(Json.toJson(testDataDebitRequestUpdate))

      val controller = application.injector.instanceOf[RichData]
      val service = application.injector.instanceOf[DataDebitContractService]
      val result: Future[Result] = for {
        _ <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        debit <- Helpers.call(controller.updateDataDebit("dd"), request)
      } yield debit

      status(result) must equalTo(OK)
    }

    "Respond with bad request if data debit does not exist" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)
        .withJsonBody(Json.toJson(testDataDebitRequestUpdate))

      val controller = application.injector.instanceOf[RichData]
      val result: Future[Result] = for {
        debit <- Helpers.call(controller.updateDataDebit("dd"), request)
      } yield debit

      status(result) must equalTo(BAD_REQUEST)
    }

    "Respond with bad request if data bundle is duplicated" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)
        .withJsonBody(Json.toJson(testDataDebitRequest))

      val controller = application.injector.instanceOf[RichData]
      val service = application.injector.instanceOf[DataDebitContractService]
      val result: Future[Result] = for {
        _ <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        debit <- Helpers.call(controller.updateDataDebit("dd"), request)
      } yield debit

      status(result) must equalTo(BAD_REQUEST)
    }
  }

  "The `getDataDebit` method" should {
    "Return 404 if data debit doesn't exist" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[RichData]
      val result: Future[Result] = for {
        debit <- Helpers.call(controller.getDataDebit("dd"), request)
      } yield debit

      status(result) must equalTo(NOT_FOUND)
    }

    "Return Data Debit information" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[RichData]
      val service = application.injector.instanceOf[DataDebitContractService]

      val result: Future[Result] = for {
        _ <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        debit <- Helpers.call(controller.getDataDebit("dd"), request)
      } yield debit

      status(result) must equalTo(OK)
      val debit = contentAsJson(result).as[RichDataDebit]
      debit.dataDebitKey must equalTo("dd")
      debit.bundles.exists(_.enabled) must beFalse
      debit.bundles.length must equalTo(1)
    }
  }

  "The `getDataDebitValues` method" should {
    "Return 404 if the data debit doesn't exist" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[RichData]
      val result: Future[Result] = for {
        debit <- Helpers.call(controller.getDataDebitValues("dd"), request)
      } yield debit

      status(result) must equalTo(NOT_FOUND)
    }

    "Return 403 if the data debit has not been enabled" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[RichData]
      val service = application.injector.instanceOf[DataDebitContractService]

      val result: Future[Result] = for {
        _ <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        debit <- Helpers.call(controller.getDataDebitValues("dd"), request)
      } yield debit

      status(result) must equalTo(BAD_REQUEST)
    }

    "Return data for matching bundle" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[RichData]
      val service = application.injector.instanceOf[DataDebitContractService]
      val dataService = application.injector.instanceOf[RichDataService]

      val result = for {
        _ <- dataService.saveData(owner.userId, List(EndpointData("test", None, simpleJson, None)))
        _ <- dataService.saveData(owner.userId, List(EndpointData("test", None, simpleJson2, None)))
        _ <- dataService.saveData(owner.userId, List(EndpointData("complex", None, complexJson, None)))
        _ <- service.createDataDebit("dd", testDataDebitRequest, owner.userId)
        _ <- service.dataDebitEnableBundle("dd", testDataDebitRequest.bundle.name)
        data <- Helpers.call(controller.getDataDebitValues("dd"), request)
      } yield data

      status(result) must equalTo(OK)
      val data = contentAsJson(result).as[Map[String, Seq[EndpointData]]]
      data("test").length must not equalTo 0
      data("complex").length must not equalTo 0
    }
  }

}

trait RichDataContext extends Scope {
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
  val owner = HatUser(UUID.randomUUID(), "hatuser", Some("pa55w0rd"), "hatuser", Seq(Owner()), enabled = true)
  val dataDebitUser = HatUser(UUID.randomUUID(), "dataDebitUser", Some("pa55w0rd"), "dataDebitUser", Seq(DataDebitOwner("")), enabled = true)
  val dataCreditUser = HatUser(UUID.randomUUID(), "dataCreditUser", Some("pa55w0rd"), "dataCreditUser", Seq(DataCredit("")), enabled = true)
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
      bind[AwsS3Configuration].toInstance(fileManagerS3Mock.s3Configuration)
      bind[AmazonS3].toInstance(fileManagerS3Mock.mockS3client)
      bind[FileManager].toInstance(new FileManagerS3(fileManagerS3Mock.s3Configuration, fileManagerS3Mock.mockS3client))
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
      |   "date": 1492699047,
      |   "date_iso": "2017-04-20T14:37:27+00:00",
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
      |   "date": 1492799047,
      |   "date_iso": "2017-04-21T18:24:07+00:00",
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
    "test" -> PropertyQuery(List(EndpointQuery("test", Some(simpleTransformation), None, None)), Some("data.newField"), None, 3),
    "complex" -> PropertyQuery(List(EndpointQuery("complex", Some(complexTransformation), None, None)), Some("data.newField"), None, 1)))

  val testBundle2 = EndpointDataBundle("testBundle2", Map(
    "test" -> PropertyQuery(List(EndpointQuery("test", Some(simpleTransformation), None, None)), Some("data.newField"), None, 3),
    "complex" -> PropertyQuery(List(EndpointQuery("anothertest", None, None, None)), Some("data.newField"), None, 1)))

  val testDataDebitRequest = DataDebitRequest(testBundle, LocalDateTime.now(), LocalDateTime.now().plusDays(3), rolling = false)

  val testDataDebitRequestUpdate = DataDebitRequest(testBundle2, LocalDateTime.now(), LocalDateTime.now().plusDays(3), rolling = false)
}