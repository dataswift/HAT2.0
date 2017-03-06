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

import java.io.StringReader
import java.util.UUID

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.atlassian.jwt.core.keys.KeyUtils
import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.test._
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.authentication.HatFrontendAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SchemaMigration
import org.hatdex.hat.dal.SlickPostgresDriver.backend.Database
import org.hatdex.hat.resourceManagement.{ FakeHatConfiguration, FakeHatServerProvider, HatServer, HatServerProvider }
import org.specs2.specification.Scope
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{ Application, Configuration }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

trait DataServiceContext extends Scope {
  val hatAddress = "hat.hubofallthings.net"
  val hatUrl = s"http://$hatAddress"
  private val keyUtils = new KeyUtils()
  private val configuration = Configuration.from(FakeHatConfiguration.config)
  private val hatConfig = configuration.getConfig(s"hat.$hatAddress").get

  implicit protected def hatDatabase: Database = Database.forConfig("", hatConfig.getConfig("database").get.underlying)

  implicit val hatServer: HatServer = HatServer(hatAddress, "hat", "user@hat.org",
    keyUtils.readRsaPrivateKeyFromPem(new StringReader(hatConfig.getString("privateKey").get)),
    keyUtils.readRsaPublicKeyFromPem(new StringReader(hatConfig.getString("publicKey").get)), hatDatabase)

  val owner = HatUser(UUID.randomUUID(), "hatuser", Some("pa55w0rd"), "hatuser", "owner", enabled = true)
  implicit val environment: Environment[HatFrontendAuthEnvironment] = FakeEnvironment[HatFrontendAuthEnvironment](
    Seq(owner.loginInfo -> owner),
    hatServer)

  // Helpers to (re-)initialize the test database and await for it to be ready
  val devHatMigrations = Seq(
    "evolutions/hat-database-schema/11_hat.sql",
    "evolutions/hat-database-schema/12_hatEvolutions.sql",
    "evolutions/hat-database-schema/13_liveEvolutions.sql")

  def databaseReady: Future[Unit] = {
    val schemaMigration = application.injector.instanceOf[SchemaMigration]
    schemaMigration.resetDatabase()
      .flatMap(_ => schemaMigration.run(devHatMigrations))
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
}
