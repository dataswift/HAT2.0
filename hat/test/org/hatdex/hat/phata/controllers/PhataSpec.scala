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

package org.hatdex.hat.phata.controllers

import java.io.StringReader
import java.util.UUID

import akka.stream.Materializer
import com.atlassian.jwt.core.keys.KeyUtils
import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.test._
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.models.{ EndpointData, Owner }
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.api.service.richData.RichDataService
import org.hatdex.hat.authentication.HatFrontendAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SchemaMigration
import org.hatdex.hat.resourceManagement.{ FakeHatConfiguration, FakeHatServerProvider, HatServer, HatServerProvider }
import org.hatdex.libs.dal.HATPostgresProfile.backend.Database
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.{ BeforeEach, Scope }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.{ FakeRequest, Helpers, PlaySpecification }
import play.api.{ Application, Configuration, Logger }

import scala.concurrent.Future
import scala.concurrent.duration._

class PhataSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with Context with BeforeEach {

  val logger = Logger(this.getClass)

  import org.hatdex.hat.api.models.RichDataJsonFormats._

  def before: Unit = {
    await(databaseReady)(30.seconds)
  }

  sequential

  "The `profile` method" should {
    "Return bundle data with profile information" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Phata]
      val dataService = application.injector.instanceOf[RichDataService]

      val data = List(
        EndpointData("rumpel/notablesv1", None, samplePublicNotable, None),
        EndpointData("rumpel/notablesv1", None, samplePrivateNotable, None),
        EndpointData("rumpel/notablesv1", None, sampleSocialNotable, None))

      val result = for {
        _ <- dataService.saveData(owner.userId, data)
        response <- Helpers.call(controller.profile, request)
      } yield response

      status(result) must equalTo(OK)
      val phataData = contentAsJson(result).as[Map[String, Seq[EndpointData]]]

      phataData.get("notables") must beSome
      phataData("notables").length must be equalTo (1)

    }
  }

  //  "The `launcher` method" should {
  //    "return status 401 if authenticator but no identity was found" in new Context {
  //      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
  //        .withAuthenticator(LoginInfo("xing", "comedian@watchmen.com"))
  //
  //      val controller = application.injector.instanceOf[Phata]
  //      val result: Future[Result] = databaseReady.flatMap(_ => controller.launcher().apply(request))
  //
  //      status(result) must equalTo(UNAUTHORIZED)
  //    }
  //
  //    "return OK if authenticator for matching identity" in new Context {
  //      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
  //        .withAuthenticator(owner.loginInfo)
  //
  //      val controller = application.injector.instanceOf[Phata]
  //      val result: Future[Result] = databaseReady.flatMap(_ => controller.launcher().apply(request))
  //
  //      status(result) must equalTo(OK)
  //      contentAsString(result) must contain("MarketSquare")
  //      contentAsString(result) must contain("Rumpel")
  //    }
  //  }

}

trait Context extends Scope {
  import scala.concurrent.ExecutionContext.Implicits.global
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
  implicit val environment: Environment[HatFrontendAuthEnvironment] = FakeEnvironment[HatFrontendAuthEnvironment](
    Seq(owner.loginInfo -> owner),
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
          _ <- usersService.saveUser(owner)
        } yield ()
      }
  }

  /**
   * A fake Guice module.
   */
  class FakeModule extends AbstractModule with ScalaModule {
    def configure() = {
      bind[Environment[HatFrontendAuthEnvironment]].toInstance(environment)
      bind[HatServerProvider].toInstance(new FakeHatServerProvider(hatServer))
    }
  }

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(FakeHatConfiguration.config)
    .overrides(new FakeModule)
    .build()

  implicit lazy val materializer: Materializer = application.materializer

  val samplePublicNotable = Json.parse(
    """
      |{
      |    "kind": "note",
      |    "author":
      |    {
      |        "phata": "testing.hubat.net"
      |    },
      |    "shared": "true",
      |    "message": "public message",
      |    "shared_on": "phata",
      |    "created_time": "2017-10-18T15:32:43+01:00",
      |    "public_until": "",
      |    "updated_time": "2017-10-23T18:29:59+01:00"
      |}
    """.stripMargin)

  val samplePrivateNotable = Json.parse(
    """
      |{
      |    "kind": "note",
      |    "author":
      |    {
      |        "phata": "testing.hubat.net"
      |    },
      |    "shared": "false",
      |    "message": "private message",
      |    "shared_on": "marketsquare",
      |    "created_time": "2017-10-18T15:32:43+01:00",
      |    "public_until": "",
      |    "updated_time": "2017-10-23T18:29:59+01:00"
      |}
    """.stripMargin)

  val sampleSocialNotable = Json.parse(
    """
      |{
      |    "kind": "note",
      |    "author":
      |    {
      |        "phata": "testing.hubat.net"
      |    },
      |    "shared": "true",
      |    "message": "social message",
      |    "shared_on": "facebook,twitter",
      |    "created_time": "2017-10-18T15:32:43+01:00",
      |    "public_until": "",
      |    "updated_time": "2017-10-23T18:29:59+01:00"
      |}
    """.stripMargin)
}