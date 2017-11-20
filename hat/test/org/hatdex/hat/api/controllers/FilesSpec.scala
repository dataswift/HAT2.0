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

package org.hatdex.hat.api.controllers

import java.io.StringReader
import java.util.UUID

import akka.stream.Materializer
import com.amazonaws.services.s3.AmazonS3
import com.atlassian.jwt.core.keys.KeyUtils
import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.{ Environment, LoginInfo }
import com.mohiva.play.silhouette.test._
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service._
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SchemaMigration
import org.hatdex.hat.resourceManagement.{ FakeHatConfiguration, FakeHatServerProvider, HatServer, HatServerProvider }
import org.hatdex.libs.dal.HATPostgresProfile.backend.Database
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.{ BeforeEach, Scope }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.{ FakeRequest, Helpers, PlaySpecification }
import play.api.{ Application, Configuration, Logger }

import scala.concurrent.Future
import scala.concurrent.duration._

class FilesSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with FilesContext with BeforeEach {

  val logger = Logger(this.getClass)

  import org.hatdex.hat.api.json.HatJsonFormats._

  def before: Unit = {
    await(databaseReady)(30.seconds)
  }

  sequential

  "The `startUpload` method" should {
    "return status 401 if authenticator but no identity was found" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(LoginInfo("xing", "comedian@watchmen.com"))
        .withJsonBody(Json.toJson(hatFileSimple))

      val controller = application.injector.instanceOf[Files]
      val result: Future[Result] = Helpers.call(controller.startUpload, request)

      status(result) must equalTo(UNAUTHORIZED)
    }

    "return status 403 if authenticator and existing identity but wrong role" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(dataDebitUser.loginInfo)
        .withJsonBody(Json.toJson(hatFileSimple))

      val controller = application.injector.instanceOf[Files]
      val result: Future[Result] = Helpers.call(controller.startUpload, request)

      status(result) must equalTo(FORBIDDEN)
    }

    "return accepted file details if authenticator for matching `dataCredit` identity" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(dataCreditUser.loginInfo)
        .withJsonBody(Json.toJson(hatFileSimple))

      val controller = application.injector.instanceOf[Files]
      val result: Future[Result] = Helpers.call(controller.startUpload, request)

      status(result) must equalTo(OK)
      val file = contentAsJson(result).as[ApiHatFile]
      file.fileId must beSome
      file.contentPublic must beSome(false)
      file.dateCreated must beSome
      file.lastUpdated must beSome
      file.contentUrl must beSome
      file.permissions must beSome
      file.status must beSome(HatFileStatus.New())
      file.permissions.get must contain(ApiHatFilePermissions(dataCreditUser.userId, contentReadable = true))
    }
  }

  "The `completeUpload` method" should {
    "return status 404 if no such file exists" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Files]
      val result: Future[Result] = controller.completeUpload("testFile.png").apply(request)

      status(result) must equalTo(NOT_FOUND)
      (contentAsJson(result) \ "message").as[String] must equalTo("No such file")
    }

    "return status 404 if user has no permission to get file contents" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(dataCreditUser.loginInfo)

      val controller = application.injector.instanceOf[Files]

      val result: Future[Result] = controller.completeUpload("testFile.png").apply(request)

      status(result) must equalTo(NOT_FOUND)
      (contentAsJson(result) \ "message").as[String] must equalTo("No such file")
    }

    "return status 400 if file incomplete" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Files]
      val fileMetadataService = application.injector.instanceOf[FileMetadataService]

      val result: Future[Result] = for {
        _ <- fileMetadataService.save(hatFileSimplePng)
        result <- controller.completeUpload("testtestFile.png").apply(request)
      } yield result

      status(result) must equalTo(BAD_REQUEST)
      (contentAsJson(result) \ "message").as[String] must equalTo("File not available")
    }

    "mark file completed when it is successfully uploaded" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Files]
      val fileMetadataService = application.injector.instanceOf[FileMetadataService]

      val result: Future[Result] = for {
        _ <- fileMetadataService.save(hatFileSimple)
        result <- controller.completeUpload("testFile").apply(request)
      } yield result

      status(result) must equalTo(OK)
      val file = contentAsJson(result).as[ApiHatFile]
      file.status must beSome(HatFileStatus.Completed(123456L))
    }
  }

  "The `getDetail` method" should {
    "return status 401 if authenticator but no identity was found" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(LoginInfo("xing", "comedian@watchmen.com"))

      val controller = application.injector.instanceOf[Files]
      val result: Future[Result] = databaseReady.flatMap(_ => controller.getDetail("testFile.png").apply(request))

      status(result) must equalTo(UNAUTHORIZED)
    }

    "return status 404 if authenticator for matching identity but no such file exists" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Files]
      val result: Future[Result] = databaseReady.flatMap(_ => controller.getDetail("testFile.png").apply(request))

      status(result) must equalTo(NOT_FOUND)
      (contentAsJson(result) \ "message").as[String] must equalTo("No such file")
    }

    "return status 404 if file exists but user is not allowed access" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(dataCreditUser.loginInfo)

      val controller = application.injector.instanceOf[Files]
      val fileMetadataService = application.injector.instanceOf[FileMetadataService]

      val result: Future[Result] = for {
        _ <- fileMetadataService.save(hatFileSimple)
        result <- controller.getDetail(hatFileSimple.fileId.get).apply(request)
      } yield result

      status(result) must equalTo(NOT_FOUND)
      (contentAsJson(result) \ "message").as[String] must equalTo("File not available")
    }

    "return file details but no content URL for allowed user" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(dataCreditUser.loginInfo)

      val controller = application.injector.instanceOf[Files]
      val fileMetadataService = application.injector.instanceOf[FileMetadataService]

      val result: Future[Result] = for {
        _ <- fileMetadataService.save(hatFileSimpleComplete)
        _ <- fileMetadataService.grantAccess(hatFileSimple, dataCreditUser, content = false)
        result <- controller.getDetail(hatFileSimple.fileId.get).apply(request)
      } yield result

      status(result) must equalTo(OK)
      val file = contentAsJson(result).as[ApiHatFile]
      file.status must beSome(HatFileStatus.Completed(123456L))
      file.permissions must beNone
      file.contentUrl must beNone
    }

    "return file details and content URL for allowed user" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(dataCreditUser.loginInfo)

      val controller = application.injector.instanceOf[Files]
      val fileMetadataService = application.injector.instanceOf[FileMetadataService]

      val result: Future[Result] = for {
        _ <- fileMetadataService.save(hatFileSimpleComplete)
        _ <- fileMetadataService.grantAccess(hatFileSimple, dataCreditUser, content = true)
        result <- controller.getDetail(hatFileSimple.fileId.get).apply(request)
      } yield result

      status(result) must equalTo(OK)
      val file = contentAsJson(result).as[ApiHatFile]
      file.status must beSome(HatFileStatus.Completed(123456L))
      file.permissions must beNone
      file.contentUrl must beSome
    }
  }

  "The `getContent` method" should {
    "Return 404 for files that do not exist" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")

      val controller = application.injector.instanceOf[Files]

      val result: Future[Result] = controller.getContent(hatFileSimple.fileId.get).apply(request)

      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must beEmpty[String]
    }

    "Return 404 for files that user is not allowed to read content of" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(dataCreditUser.loginInfo)

      val controller = application.injector.instanceOf[Files]
      val fileMetadataService = application.injector.instanceOf[FileMetadataService]

      val result: Future[Result] = for {
        _ <- fileMetadataService.save(hatFileSimpleComplete)
        _ <- fileMetadataService.grantAccess(hatFileSimple, dataCreditUser, content = false)
        result <- controller.getContent(hatFileSimple.fileId.get).apply(request)
      } yield result

      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must beEmpty[String]
    }

    "Redirect to file url for publicly readable files" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")

      val controller = application.injector.instanceOf[Files]
      val fileMetadataService = application.injector.instanceOf[FileMetadataService]

      val result: Future[Result] = for {
        _ <- fileMetadataService.save(hatFileSimpleCompletePublic)
        result <- controller.getContent(hatFileSimpleCompletePublic.fileId.get).apply(request)
      } yield result

      result map { response =>
        logger.info(s"GET content response:  $response")
      }

      redirectLocation(result) must beSome
      redirectLocation(result).get must startWith("https://hat-storage-test.s3-eu-west-1.amazonaws.com/hat.hubofallthings.net/testFile")
    }

    "Redirect to file url for permitted files files" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(dataCreditUser.loginInfo)

      val controller = application.injector.instanceOf[Files]
      val fileMetadataService = application.injector.instanceOf[FileMetadataService]

      val result: Future[Result] = for {
        _ <- fileMetadataService.save(hatFileSimpleComplete)
        _ <- fileMetadataService.grantAccess(hatFileSimpleComplete, dataCreditUser, content = true)
        result <- controller.getContent(hatFileSimpleComplete.fileId.get).apply(request)
      } yield result

      redirectLocation(result) must beSome
      redirectLocation(result).get must startWith("https://hat-storage-test.s3-eu-west-1.amazonaws.com/hat.hubofallthings.net/testFile")
    }
  }

  "The `changePublicAccess` method" should {
    "Let `owner` user make file publicly accessible" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Files]
      val fileMetadataService = application.injector.instanceOf[FileMetadataService]

      val result: Future[Result] = for {
        file <- fileMetadataService.save(hatFileSimpleComplete)
        result <- controller.changePublicAccess(file.fileId.get, public = true).apply(request)
      } yield result

      val file = contentAsJson(result).as[ApiHatFile]
      file.contentPublic must beSome(true)
    }

    "Let `owner` user restrict file publicly accessibility" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Files]
      val fileMetadataService = application.injector.instanceOf[FileMetadataService]

      val result: Future[Result] = for {
        file <- fileMetadataService.save(hatFileSimpleCompletePublic)
        result <- controller.changePublicAccess(file.fileId.get, public = false).apply(request)
      } yield result

      val file = contentAsJson(result).as[ApiHatFile]
      file.contentPublic must beSome(false)
    }

    "Return 401 for not `owner` user making file publicly accessible" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(dataCreditUser.loginInfo)

      val controller = application.injector.instanceOf[Files]
      val fileMetadataService = application.injector.instanceOf[FileMetadataService]

      val result: Future[Result] = for {
        file <- fileMetadataService.save(hatFileSimpleComplete)
        result <- controller.changePublicAccess(file.fileId.get, public = true).apply(request)
      } yield result

      status(result) must equalTo(FORBIDDEN)
    }
  }

}

trait FilesContext extends Scope {
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

  val hatFileSimple = ApiHatFile(Some("testFile"), "testFile", "test", None, None, None, None, None, None, Some(HatFileStatus.New()))
  val hatFileSimpleComplete = ApiHatFile(Some("testFile"), "testFile", "test", None, None, None, None, None, None, Some(HatFileStatus.Completed(123456L)))
  val hatFileSimpleCompletePublic = ApiHatFile(Some("testFile"), "testFile", "test", None, None, None, None, None, None, Some(HatFileStatus.Completed(123456L)), None, Some(true))
  val hatFileSimplePng = ApiHatFile(Some("testtestFile.png"), "testFile.png", "test", None, None, None, None, None, None, Some(HatFileStatus.New()))
}