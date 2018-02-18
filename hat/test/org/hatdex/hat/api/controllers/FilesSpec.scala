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

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.test._
import org.hatdex.hat.api.HATTestContext
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service._
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.{ BeforeAll, BeforeEach }
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.{ FakeRequest, Helpers, PlaySpecification }

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

class FilesSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with FilesContext with BeforeAll with BeforeEach {

  val logger = Logger(this.getClass)

  import org.hatdex.hat.api.json.HatJsonFormats._

  sequential

  def beforeAll: Unit = {
    Await.result(databaseReady, 60.seconds)
  }

  override def before: Unit = {
    import org.hatdex.hat.dal.Tables._
    import org.hatdex.libs.dal.HATPostgresProfile.api._

    val testFilesQuery = HatFile.filter(_.source.like("test%"))

    val action = DBIO.seq(
      HatFileAccess.filter(_.fileId in testFilesQuery.map(_.id)).delete,
      testFilesQuery.delete)

    Await.result(hatDatabase.run(action), 60.seconds)
  }

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
        logger.debug(s"GET content response:  $response")
      }

      redirectLocation(result) must beSome
      redirectLocation(result).get must startWith("https://hat-storage-test.s3.eu-west-1.amazonaws.com/hat.hubofallthings.net/testFile")
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
      redirectLocation(result).get must startWith("https://hat-storage-test.s3.eu-west-1.amazonaws.com/hat.hubofallthings.net/testFile")
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

trait FilesContext extends HATTestContext {
  val hatFileSimple = ApiHatFile(Some("testFile"), "testFile", "test", None, None, None, None, None, None, Some(HatFileStatus.New()))
  val hatFileSimpleComplete = ApiHatFile(Some("testFile"), "testFile", "test", None, None, None, None, None, None, Some(HatFileStatus.Completed(123456L)))
  val hatFileSimpleCompletePublic = ApiHatFile(Some("testFile"), "testFile", "test", None, None, None, None, None, None, Some(HatFileStatus.Completed(123456L)), None, Some(true))
  val hatFileSimplePng = ApiHatFile(Some("testtestFile.png"), "testFile.png", "test", None, None, None, None, None, None, Some(HatFileStatus.New()))
}