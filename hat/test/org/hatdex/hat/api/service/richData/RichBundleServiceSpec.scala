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

import java.io.StringReader
import java.util.UUID

import akka.stream.Materializer
import com.atlassian.jwt.core.keys.KeyUtils
import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.test.FakeEnvironment
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.FakeCache
import org.hatdex.hat.api.models.{ DataCredit, DataDebitOwner, Owner, _ }
import org.hatdex.hat.api.service.{ FileManagerS3Mock, UsersService }
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SchemaMigration
import org.hatdex.hat.resourceManagement.{ FakeHatConfiguration, FakeHatServerProvider, HatServer, HatServerProvider }
import org.hatdex.libs.dal.HATPostgresProfile.backend.Database
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.{ BeforeEach, Scope }
import play.api.cache.AsyncCacheApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{ JsObject, Json }
import play.api.test.PlaySpecification
import play.api.{ Application, Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class RichBundleServiceSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with RichBundleServiceContext with BeforeEach {

  val logger = Logger(this.getClass)

  def before: Unit = {
    await(databaseReady)(30.seconds)
  }

  sequential

  "The `saveCombinator` method" should {
    "Save a combinator" in {
      val service = application.injector.instanceOf[RichBundleService]
      val saved = service.saveCombinator("testCombinator", testEndpointQuery)
      saved map { _ =>
        true must beTrue
      } await (3, 10.seconds)
    }

    "Update a combinator if one already exists" in {
      val service = application.injector.instanceOf[RichBundleService]
      val saved = for {
        _ <- service.saveCombinator("testCombinator", testEndpointQueryUpdated)
        saved <- service.saveCombinator("testCombinator", testEndpointQueryUpdated)
      } yield saved

      saved map { _ =>
        true must beTrue
      } await (3, 10.seconds)
    }
  }

  "The `combinator` method" should {
    "Retrieve a combinator" in {
      val service = application.injector.instanceOf[RichBundleService]
      val saved = for {
        _ <- service.saveCombinator("testCombinator", testEndpointQuery)
        combinator <- service.combinator("testCombinator")
      } yield combinator

      saved map { r =>
        r must beSome
        r.get.length must equalTo(2)
      } await (3, 10.seconds)
    }

    "Return None if combinator doesn't exist" in {
      val service = application.injector.instanceOf[RichBundleService]
      val saved = for {
        combinator <- service.combinator("testCombinator")
      } yield combinator

      saved map { r =>
        r must beNone
      } await (3, 10.seconds)
    }
  }

  "The `combinators` method" should {
    "List all combinators" in {
      val service = application.injector.instanceOf[RichBundleService]
      val saved = for {
        _ <- service.saveCombinator("testCombinator", testEndpointQuery)
        _ <- service.saveCombinator("testCombinator2", testEndpointQueryUpdated)
        combinators <- service.combinators()
      } yield combinators

      saved map { r =>
        r.length must equalTo(2)
      } await (3, 10.seconds)
    }
  }

  "The `deleteCombinator` method" should {
    "Delete combinator by ID" in {
      val service = application.injector.instanceOf[RichBundleService]
      val saved = for {
        _ <- service.saveCombinator("testCombinator", testEndpointQuery)
        _ <- service.saveCombinator("testCombinator2", testEndpointQueryUpdated)
        _ <- service.deleteCombinator("testCombinator")
        combinators <- service.combinators()
      } yield combinators

      saved map { r =>
        r.length must equalTo(1)
        r.head._1 must equalTo("testCombinator2")
      } await (3, 10.seconds)
    }
  }

  "The `saveBundle` method" should {
    "Save a bundle" in {
      val service = application.injector.instanceOf[RichBundleService]
      val saved = service.saveBundle(testBundle)
      saved map { _ =>
        true must beTrue
      } await (3, 10.seconds)
    }

    "Update a bundle if one already exists" in {
      val service = application.injector.instanceOf[RichBundleService]
      val saved = for {
        _ <- service.saveBundle(testBundle)
        saved <- service.saveBundle(testBundle)
      } yield saved

      saved map { _ =>
        true must beTrue
      } await (3, 10.seconds)
    }
  }

  "The `bundle` method" should {
    "Retrieve a bundle by ID" in {
      val service = application.injector.instanceOf[RichBundleService]
      val saved = for {
        _ <- service.saveBundle(testBundle)
        combinator <- service.bundle(testBundle.name)
      } yield combinator

      saved map { r =>
        r must beSome
        r.get.name must equalTo(testBundle.name)
      } await (3, 10.seconds)
    }
  }

  "The `bundles` method" should {
    "Retrieve a list of bundles" in {
      val service = application.injector.instanceOf[RichBundleService]
      val saved = for {
        _ <- service.saveBundle(testBundle)
        _ <- service.saveBundle(testBundle2)
        combinator <- service.bundles()
      } yield combinator

      saved map { r =>
        r.length must equalTo(3)
      } await (3, 10.seconds)
    }
  }

  "The `deleteBundle` method" should {
    "Delete bundle by ID" in {
      val service = application.injector.instanceOf[RichBundleService]
      val saved = for {
        _ <- service.saveBundle(testBundle)
        _ <- service.saveBundle(testBundle2)
        _ <- service.deleteBundle(testBundle.name)
        combinators <- service.bundles()
      } yield combinators

      saved map { r =>
        r.length must equalTo(2)
        r.find(_.name == testBundle2.name) must beSome
      } await (3, 10.seconds)
    }
  }

}

trait RichBundleServiceContext extends Scope with Mockito {
  // Initialize configuration
  val hatAddress = "hat.hubofallthings.net"
  val hatUrl = s"http://$hatAddress"
  private val configuration = Configuration.from(FakeHatConfiguration.config)
  private val hatConfig = configuration.get[Configuration](s"hat.$hatAddress")

  // Build up the FakeEnvironment for authentication testing
  private val keyUtils = new KeyUtils()
  implicit protected def hatDatabase: Database = Database.forConfig("", hatConfig.get[Configuration]("database").underlying)
  implicit val hatServer: HatServer = HatServer(hatAddress, "hat", "user@hat.org",
    keyUtils.readRsaPrivateKeyFromPem(new StringReader(hatConfig.get[String]("privateKey"))),
    keyUtils.readRsaPublicKeyFromPem(new StringReader(hatConfig.get[String]("publicKey"))), hatDatabase)

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
    lazy val cacheAPI = mock[AsyncCacheApi]

    def configure(): Unit = {
      bind[Environment[HatApiAuthEnvironment]].toInstance(environment)
      bind[HatServerProvider].toInstance(new FakeHatServerProvider(hatServer))
      bind[AsyncCacheApi].toInstance(cacheAPI)
    }
  }

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(FakeHatConfiguration.config)
    .overrides(new FakeModule)
    .build()

  implicit lazy val materializer: Materializer = application.materializer

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
}
