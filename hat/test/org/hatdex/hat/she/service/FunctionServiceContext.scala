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
 * 11 / 2017
 */

package org.hatdex.hat.she.service

import java.io.StringReader
import java.util.UUID

import akka.stream.Materializer
import com.atlassian.jwt.core.keys.KeyUtils
import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.test._
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.models.{ DataCredit, DataDebitOwner, EndpointDataBundle, Owner }
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatFrontendAuthEnvironment }
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SchemaMigration
import org.hatdex.hat.resourceManagement.{ FakeHatConfiguration, FakeHatServerProvider, HatServer, HatServerProvider }
import org.hatdex.hat.she.functions.DataFeedDirectMapper
import org.hatdex.hat.she.models._
import org.hatdex.libs.dal.HATPostgresProfile.backend.Database
import org.specs2.specification.Scope
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{ Application, Configuration }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success

trait FunctionServiceContext extends Scope {
  val hatAddress = "hat.hubofallthings.net"
  val hatUrl = s"http://$hatAddress"
  private val keyUtils = new KeyUtils()
  private val configuration = Configuration.from(FakeHatConfiguration.config)
  private val hatConfig = configuration.get[Configuration](s"hat.$hatAddress")

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
    def configure() = {
      bind[Environment[HatApiAuthEnvironment]].toInstance(environment)
      bind[HatServerProvider].toInstance(new FakeHatServerProvider(hatServer))
    }

    @Provides
    def provideFunctionExecutableRegistry(): FunctionExecutableRegistry = {
      new FunctionExecutableRegistry(Seq(registeredFunction, registeredDummyFunction))
    }
  }

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(FakeHatConfiguration.config)
    .overrides(new FakeModule)
    .build()

  implicit lazy val materializer: Materializer = application.materializer

  val dummyFunctionConfiguration = FunctionConfiguration("dummy-function", "Dummy Function",
    FunctionTrigger.TriggerIndividual(), available = false, enabled = false,
    dataBundle = EndpointDataBundle("data-feed-dummy-mapper", Map()),
    None)

  val dummyFunctionConfigurationUpdated = FunctionConfiguration("dummy-function", "Updated Function",
    FunctionTrigger.TriggerIndividual(), available = false, enabled = true,
    dataBundle = EndpointDataBundle("data-feed-dummy-mapper", Map()),
    None)

  val unavailableFunctionConfiguration = FunctionConfiguration("unavailable-function", "Unavailable Function",
    FunctionTrigger.TriggerIndividual(), available = false, enabled = false,
    dataBundle = EndpointDataBundle("unavailable-function-bundler", Map()),
    None)

  val registeredFunction = new DataFeedDirectMapper()
  val registeredDummyFunction: FunctionExecutable = new DummyFunctionExecutable(dummyFunctionConfiguration)
}

class DummyFunctionExecutable(conf: FunctionConfiguration) extends FunctionExecutable {
  val configuration = conf

  override def execute(configuration: FunctionConfiguration, request: Request)(implicit ec: ExecutionContext): Future[Seq[Response]] = {
    Future.failed(new RuntimeException("Dummy Function"))
  }
}
