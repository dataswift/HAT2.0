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

package org.hatdex.hat.api.controllers

import java.io.StringReader
import java.util.UUID

import akka.stream.Materializer
import com.amazonaws.services.s3.AmazonS3Client
import com.atlassian.jwt.core.keys.KeyUtils
import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.{ Environment, LoginInfo }
import com.mohiva.play.silhouette.test._
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.service._
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.{ DataCredit, DataDebitOwner, HatUser, Owner }
import org.hatdex.hat.dal.SchemaMigration
import org.hatdex.hat.dal.SlickPostgresDriver.backend.Database
import org.hatdex.hat.phata.models.{ ApiPasswordChange, ApiPasswordResetRequest, MailTokenUser }
import org.hatdex.hat.resourceManagement.{ FakeHatConfiguration, FakeHatServerProvider, HatServer, HatServerProvider }
import org.hatdex.hat.utils.{ ErrorHandler, HatMailer }
import org.joda.time.DateTime
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.{ BeforeEach, Scope }
import play.api.http.HttpErrorHandler
import play.api.i18n.Messages
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.Result
import play.api.test.{ FakeHeaders, FakeRequest, Helpers, PlaySpecification }
import play.api.{ Application, Configuration, Logger }
import play.mvc.Http.{ HeaderNames, MimeTypes }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class AuthenticationSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with AuthenticationContext with BeforeEach {

  val logger = Logger(this.getClass)

  import ApiPasswordChange.passwordChangeApiWrites

  def before: Unit = {
    await(databaseReady)(30.seconds)
  }

  sequential

  "The `hatLogin` method" should {
    "return status 401 if authenticator but no identity was found" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(LoginInfo("xing", "comedian@watchmen.com"))

      val controller = application.injector.instanceOf[Authentication]
      val result: Future[Result] = controller.hatLogin("TestService", "http://testredirect").apply(request)

      status(result) must equalTo(UNAUTHORIZED)
    }

    "return status 403 if authenticator and existing identity but wrong role" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(dataDebitUser.loginInfo)

      val controller = application.injector.instanceOf[Authentication]
      val result: Future[Result] = controller.hatLogin("TestService", "http://testredirect").apply(request)

      status(result) must equalTo(FORBIDDEN)
    }

    "return redirect url for authenticated owner" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val controller = application.injector.instanceOf[Authentication]
      val result: Future[Result] = controller.hatLogin("TestService", "http://testredirect").apply(request)

      status(result) must equalTo(OK)
      contentAsString(result) must contain("testredirect")
      contentAsString(result) must contain("token=")
    }
  }

  "The `passwordChangeProcess` method should" in {
    "return status 403 if not owner" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(dataDebitUser.loginInfo)
        .withJsonBody(Json.toJson(passwordChangeIncorrect))

      val controller = application.injector.instanceOf[Authentication]
      val result: Future[Result] = Helpers.call(controller.passwordChangeProcess(), request)

      status(result) must equalTo(FORBIDDEN)
    }

    "return status 403 if old password incorrect" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)
        .withJsonBody(Json.toJson(passwordChangeIncorrect))

      val controller = application.injector.instanceOf[Authentication]
      val result: Future[Result] = Helpers.call(controller.passwordChangeProcess(), request)

      status(result) must equalTo(FORBIDDEN)
    }

    "return status 400 if new password too weak" in {
      val request = FakeRequest[JsValue](
        Helpers.POST,
        org.hatdex.hat.api.controllers.routes.Authentication.passwordChangeProcess().url,
        headers = FakeHeaders(Seq((HeaderNames.ACCEPT, MimeTypes.JSON), (HeaderNames.CONTENT_TYPE, MimeTypes.JSON))),
        body = Json.toJson(passwordChangeSimple),
        remoteAddress = "hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      val maybeResult: Option[Future[Result]] = Helpers.route(application, request)
      //      val controller = application.injector.instanceOf[Authentication]
      //      val result: Future[Result] = Helpers.call(controller.passwordChangeProcess(), request)
      maybeResult must beSome
      val result = maybeResult.get

      status(result) must equalTo(BAD_REQUEST)
      //      contentType(result) must beSome("application/json")
      (contentAsJson(result) \ "error").as[String] must equalTo("Bad Request")
    }

    "Change password if it is sufficiently strong" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)
        .withJsonBody(Json.toJson(passwordChangeStrong))

      val controller = application.injector.instanceOf[Authentication]
      val result: Future[Result] = Helpers.call(controller.passwordChangeProcess(), request)

      status(result) must equalTo(OK)
      (contentAsJson(result) \ "message").as[String] must equalTo("Password changed")
    }
  }

  "The `handleForgotPassword` method should" in {
    "Hide the fact that email doesn't match by returning status 200" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(dataDebitUser.loginInfo)
        .withJsonBody(Json.toJson(passwordForgottenIncorrect))

      val controller = application.injector.instanceOf[Authentication]
      val result: Future[Result] = Helpers.call(controller.handleForgotPassword, request)

      status(result) must equalTo(OK)
    }

    "Send email to the owner if provided email matches" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(dataDebitUser.loginInfo)
        .withJsonBody(Json.toJson(passwordForgottenOwner))

      val controller = application.injector.instanceOf[Authentication]
      val result: Future[Result] = Helpers.call(controller.handleForgotPassword, request)

      status(result) must equalTo(OK)
      there was one(mockMailer).passwordReset(any[String], any[HatUser], any[String])(any[Messages], any[HatServer])
    }
  }

  "The `handleResetPassword` method should" in {
    "Return status 401 if no such token exists" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(dataDebitUser.loginInfo)
        .withJsonBody(Json.toJson(passwordResetStrong))

      val controller = application.injector.instanceOf[Authentication]
      val result: Future[Result] = Helpers.call(controller.handleResetPassword("nosuchtoken"), request)

      status(result) must equalTo(UNAUTHORIZED)
      (contentAsJson(result) \ "cause").as[String] must equalTo("Token does not exist")
    }

    "Return status 401 if token has expired" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)
        .withJsonBody(Json.toJson(passwordResetStrong))

      val controller = application.injector.instanceOf[Authentication]
      val tokenService = application.injector.instanceOf[MailTokenUserService]
      val tokenId = UUID.randomUUID().toString
      tokenService.create(MailTokenUser(tokenId, "hat@hat.org", DateTime.now().minusHours(1), isSignUp = false))
      val result: Future[Result] = Helpers.call(controller.handleResetPassword(tokenId), request)

      status(result) must equalTo(UNAUTHORIZED)
      (contentAsJson(result) \ "cause").as[String] must equalTo("Token expired or invalid")
    }

    "Return status 401 if token email doesn't match owner" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)
        .withJsonBody(Json.toJson(passwordResetStrong))

      val controller = application.injector.instanceOf[Authentication]
      val tokenService = application.injector.instanceOf[MailTokenUserService]
      val tokenId = UUID.randomUUID().toString
      tokenService.create(MailTokenUser(tokenId, "email@hat.org", DateTime.now().plusHours(1), isSignUp = false))
      val result: Future[Result] = Helpers.call(controller.handleResetPassword(tokenId), request)

      status(result) must equalTo(UNAUTHORIZED)
      (contentAsJson(result) \ "cause").as[String] must equalTo("Only HAT owner can reset their password")
    }

    "Return status 401 if no owner exists (should never happen)" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)
        .withJsonBody(Json.toJson(passwordResetStrong))

      val controller = application.injector.instanceOf[Authentication]
      val tokenService = application.injector.instanceOf[MailTokenUserService]
      val tokenId = UUID.randomUUID().toString
      tokenService.create(MailTokenUser(tokenId, "user@hat.org", DateTime.now().plusHours(1), isSignUp = false))
      val usersService = application.injector.instanceOf[UsersService]
      val result: Future[Result] = usersService.saveUser(owner.copy(roles = Seq(DataDebitOwner("")))) // forcing owner user to a different role for the test
        .flatMap {
          case _ =>
            Helpers.call(controller.handleResetPassword(tokenId), request)
        }

      status(result) must equalTo(UNAUTHORIZED)
      (contentAsJson(result) \ "cause").as[String] must equalTo("No user matching token")
    }

    "Reset password" in {
      val request = FakeRequest("POST", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)
        .withJsonBody(Json.toJson(passwordResetStrong))

      val controller = application.injector.instanceOf[Authentication]
      val tokenService = application.injector.instanceOf[MailTokenUserService]
      val tokenId = UUID.randomUUID().toString
      tokenService.create(MailTokenUser(tokenId, "user@hat.org", DateTime.now().plusHours(1), isSignUp = false))
      val result: Future[Result] = Helpers.call(controller.handleResetPassword(tokenId), request)

      status(result) must equalTo(OK)
    }
  }

}

trait AuthenticationContext extends Scope with Mockito {
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
  val owner = HatUser(UUID.randomUUID(), "hatuser", Some("$2a$06$QprGa33XAF7w8BjlnKYb3OfWNZOuTdzqKeEsF7BZUfbiTNemUW/n."), "hatuser", Seq(Owner()), enabled = true)
  val dataDebitUser = HatUser(UUID.randomUUID(), "dataDebitUser", Some("$2a$06$QprGa33XAF7w8BjlnKYb3OfWNZOuTdzqKeEsF7BZUfbiTNemUW/n."), "dataDebitUser", Seq(DataDebitOwner("")), enabled = true)
  val dataCreditUser = HatUser(UUID.randomUUID(), "dataCreditUser", Some("$2a$06$QprGa33XAF7w8BjlnKYb3OfWNZOuTdzqKeEsF7BZUfbiTNemUW/n."), "dataCreditUser", Seq(DataCredit("")), enabled = true)
  implicit val environment: Environment[HatApiAuthEnvironment] = FakeEnvironment[HatApiAuthEnvironment](
    Seq(owner.loginInfo -> owner, dataDebitUser.loginInfo -> dataDebitUser, dataCreditUser.loginInfo -> dataCreditUser),
    hatServer)

  // Helpers to (re-)initialize the test database and await for it to be ready
  val devHatMigrations = Seq(
    "evolutions/hat-database-schema/11_hat.sql",
    "evolutions/hat-database-schema/12_hatEvolutions.sql",
    "evolutions/hat-database-schema/13_liveEvolutions.sql")

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

  val mockMailer: HatMailer = mock[HatMailer]
  doNothing.when(mockMailer).passwordReset(any[String], any[HatUser], any[String])(any[Messages], any[HatServer])

  /**
   * A fake Guice module.
   */
  class FakeModule extends AbstractModule with ScalaModule {
    val fileManagerS3Mock = FileManagerS3Mock()

    def configure(): Unit = {
      bind[Environment[HatApiAuthEnvironment]].toInstance(environment)
      bind[HatServerProvider].toInstance(new FakeHatServerProvider(hatServer))
      bind[AwsS3Configuration].toInstance(fileManagerS3Mock.s3Configuration)
      bind[AmazonS3Client].toInstance(fileManagerS3Mock.mockS3client)
      bind[FileManager].toInstance(new FileManagerS3(fileManagerS3Mock.s3Configuration, fileManagerS3Mock.mockS3client))
      bind[MailTokenService[MailTokenUser]].to[MailTokenUserService]
      bind[HatMailer].toInstance(mockMailer)
      bind[HttpErrorHandler].to[ErrorHandler]
    }
  }

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(FakeHatConfiguration.config)
    .overrides(new FakeModule)
    .build()

  implicit lazy val materializer: Materializer = application.materializer

  val passwordChangeIncorrect = ApiPasswordChange("some-passwords-are-better-than-others", Some("wrongOldPassword"))
  val passwordChangeSimple = ApiPasswordChange("simple", Some("pa55w0rd"))
  val passwordChangeStrong = ApiPasswordChange("some-passwords-are-better-than-others", Some("pa55w0rd"))
  val passwordResetStrong = ApiPasswordChange("some-passwords-are-better-than-others", None)

  val passwordForgottenIncorrect = ApiPasswordResetRequest("email@example.com")
  val passwordForgottenOwner = ApiPasswordResetRequest("user@hat.org")
}