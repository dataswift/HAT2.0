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

import com.atlassian.jwt.core.keys.KeyUtils
import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.test._
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.models.Owner
import org.hatdex.hat.authentication.HatFrontendAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SchemaMigration
import org.hatdex.hat.resourceManagement.{ FakeHatConfiguration, FakeHatServerProvider, HatServer, HatServerProvider }
import org.hatdex.libs.dal.HATPostgresProfile.backend.Database
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.PlaySpecification
import play.api.{ Application, Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PhataSpec extends PlaySpecification with Mockito {

  val logger = Logger(this.getClass)

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
  // Initialize configuration
  val hatAddress = "hat.hubofallthings.net"
  val hatUrl = s"http://$hatAddress"
  private val configuration = Configuration.from(FakeHatConfiguration.config)
  private val hatConfig = configuration.get[Configuration](s"hat.$hatAddress")

  // Build up the FakeEnvironment for authentication testing
  private val keyUtils = new KeyUtils()
  private def hatDatabase: Database = Database.forConfig("", hatConfig.get[Configuration]("database").underlying)
  implicit val hatServer: HatServer = HatServer(hatAddress, "hat", "user@hat.org",
    keyUtils.readRsaPrivateKeyFromPem(new StringReader(hatConfig.get[String]("privateKey"))),
    keyUtils.readRsaPublicKeyFromPem(new StringReader(hatConfig.get[String]("publicKey"))), hatDatabase)

  // Setup default users for testing
  val owner = HatUser(UUID.randomUUID(), "hatuser", Some("pa55w0rd"), "hatuser", Seq(Owner()), enabled = true)
  implicit val env: Environment[HatFrontendAuthEnvironment] = FakeEnvironment[HatFrontendAuthEnvironment](
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
  }

  /**
   * A fake Guice module.
   */
  class FakeModule extends AbstractModule with ScalaModule {
    def configure() = {
      bind[Environment[HatFrontendAuthEnvironment]].toInstance(env)
      bind[HatServerProvider].toInstance(new FakeHatServerProvider(hatServer))
    }
  }

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(FakeHatConfiguration.config)
    .overrides(new FakeModule)
    .build()
}