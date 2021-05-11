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

package org.hatdex.hat.api

import akka.Done
import akka.stream.Materializer
import com.amazonaws.services.s3.AmazonS3
import com.atlassian.jwt.core.keys.KeyUtils
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.google.inject.Provides
import com.mohiva.play.silhouette.api.{ Environment, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.test.FakeEnvironment
import io.dataswift.integrationtest.common.PostgresqlSpec
import io.dataswift.models.hat.{ DataCredit, DataDebitOwner, Owner }
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.FakeCache
import org.hatdex.hat.api.service._
import org.hatdex.hat.api.service.applications.{ TestApplicationProvider, TrustedApplicationProvider }
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.HatDbSchemaMigration
import org.hatdex.hat.phata.models.MailTokenUser
import org.hatdex.hat.resourceManagement.{ FakeHatServerProvider, HatServer, HatServerProvider }
import org.hatdex.hat.utils.{ ErrorHandler, HatMailer, LoggingProvider, MockLoggingProvider }
import org.hatdex.libs.dal.HATPostgresProfile.backend.Database
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatestplus.mockito.MockitoSugar
import play.api.cache.AsyncCacheApi
import play.api.http.HttpErrorHandler
import play.api.i18n.{ Lang, MessagesApi }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{ Application, Configuration, Logger }
import play.cache.NamedCacheImpl

import java.io.StringReader
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

abstract class HATTestContext extends PostgresqlSpec with MockitoSugar with BeforeAndAfter {

  lazy val conf: Configuration = containerToConfig(postgresqlContainer)

  override def afterStart(): Unit =
    Await.result(databaseReady(), 60.seconds)

  val hatAddress     = "hat.hubofallthings.net"
  val hatUrl: String = s"http://$hatAddress"

  def containerToConfig(c: PostgreSQLContainer): Configuration =
    Configuration
      .load(play.api.Environment.simple())
      .withFallback(
        Configuration.from(
          Map(
            "hat" -> Map(
                  "hat.hubofallthings.net" -> Map(
                        "database" ->
                            Map(
                              "properties" -> Map("user" -> c.username, "password" -> c.password, "url" -> c.jdbcUrl)
                            )
                      )
                )
          )
        )
      )

  implicit lazy val db: Database = Database.forURL(
    url = postgresqlContainer.jdbcUrl,
    user = postgresqlContainer.username,
    password = postgresqlContainer.password
  )

  // Build up the FakeEnvironment for authentication testing
  private val keyUtils = new KeyUtils()

  implicit lazy val hatServer: HatServer = {
    val hatConfig = conf.get[Configuration](s"hat.$hatAddress")
    HatServer(
      hatAddress,
      "hat",
      "user@hat.org",
      keyUtils.readRsaPrivateKeyFromPem(new StringReader(hatConfig.get[String]("privateKey"))),
      keyUtils.readRsaPublicKeyFromPem(new StringReader(hatConfig.get[String]("publicKey"))),
      db
    )
  }

  // Setup default users for testing
  val owner: HatUser = HatUser(UUID.randomUUID(),
                               "hatuser",
                               Some("$2a$06$QprGa33XAF7w8BjlnKYb3OfWNZOuTdzqKeEsF7BZUfbiTNemUW/n."),
                               "hatuser",
                               Seq(Owner()),
                               enabled = true
  )
  val dataDebitUser: HatUser = HatUser(
    UUID.randomUUID(),
    "dataDebitUser",
    Some("$2a$06$QprGa33XAF7w8BjlnKYb3OfWNZOuTdzqKeEsF7BZUfbiTNemUW/n."),
    "dataDebitUser",
    Seq(DataDebitOwner("")),
    enabled = true
  )
  val dataCreditUser: HatUser = HatUser(
    UUID.randomUUID(),
    "dataCreditUser",
    Some("$2a$06$QprGa33XAF7w8BjlnKYb3OfWNZOuTdzqKeEsF7BZUfbiTNemUW/n."),
    "dataCreditUser",
    Seq(DataCredit(""), DataCredit("namespace")),
    enabled = true
  )
  implicit lazy val environment: Environment[HatApiAuthEnvironment] = FakeEnvironment[HatApiAuthEnvironment](
    Seq(owner.loginInfo -> owner, dataDebitUser.loginInfo -> dataDebitUser, dataCreditUser.loginInfo -> dataCreditUser),
    hatServer
  )

  // Helpers to (re-)initialize the test database and await for it to be ready
  val devHatMigrations: Seq[String] = Seq(
    "evolutions/hat-database-schema/11_hat.sql",
    "evolutions/hat-database-schema/12_hatEvolutions.sql",
    "evolutions/hat-database-schema/13_liveEvolutions.sql",
    "evolutions/hat-database-schema/14_newHat.sql"
  )

  def databaseReady(): Future[Unit] = {
    val schemaMigration = new HatDbSchemaMigration(conf, db, global)
    schemaMigration
      .resetDatabase()
      .flatMap(_ => schemaMigration.run(devHatMigrations))
      .flatMap { _ =>
        val userService = application.injector.instanceOf[UserService]
        for {
          _ <- userService.saveUser(dataCreditUser)
          _ <- userService.saveUser(dataDebitUser)
          _ <- userService.saveUser(owner)
        } yield ()
      }
  }

  val mockLogger: Logger    = MockitoSugar.mock[play.api.Logger]
  val mockMailer: HatMailer = MockitoSugar.mock[HatMailer]
  when(mockMailer.passwordReset(any[String], any[String])(any[MessagesApi], any[Lang], any[HatServer])).thenReturn(Done)

  val fileManagerS3Mock: FileManagerS3Mock = new FileManagerS3Mock

  val expectedS3UrlPrefix = "https://s3.eu-west-1.amazonaws.com/hat.hubofallthings.net/testFile"

  lazy val remoteEC = new RemoteExecutionContext(application.actorSystem)

  /**
    * A fake Guice module.
    */
  class IntegrationSpecModule extends ScalaModule {
    override def configure(): Unit = {
      bind[Environment[HatApiAuthEnvironment]].toInstance(environment)
      bind[Silhouette[HatApiAuthEnvironment]].to[SilhouetteProvider[HatApiAuthEnvironment]]
      bind[HatServerProvider].toInstance(new FakeHatServerProvider(hatServer))
      bind[FileManager].to[FileManagerS3]
      bind[MailTokenService[MailTokenUser]].to[MailTokenUserService]
      bind[HatMailer].toInstance(mockMailer)
      bind[HttpErrorHandler].to[ErrorHandler]
      bind[AsyncCacheApi].annotatedWith(new NamedCacheImpl("user-cache")).to[FakeCache]
      bind[AsyncCacheApi].to[FakeCache]
      bind[LoggingProvider].toInstance(new MockLoggingProvider(mockLogger))
    }

    @Provides
    def provides3Client(): AmazonS3 =
      fileManagerS3Mock.mockS3client

  }

  class EmptyAppProviderModule extends ScalaModule {
    override def configure(): Unit =
      bind[TrustedApplicationProvider].toInstance(new TestApplicationProvider(Seq()))
  }

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(conf)
    .overrides(new IntegrationSpecModule)
    .overrides(new EmptyAppProviderModule)
    .build()

  implicit lazy val materializer: Materializer = application.materializer

}
