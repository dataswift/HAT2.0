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

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.crypto.Base64AuthenticatorEncoder
import com.mohiva.play.silhouette.impl.authenticators.{ JWTRS256Authenticator, JWTRS256AuthenticatorSettings }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.test._
import io.dataswift.models.hat.DataDebitOwner
import org.hatdex.hat.api.HATTestContext
import org.hatdex.hat.api.service._
import org.hatdex.hat.phata.models.{ ApiPasswordChange, ApiPasswordResetRequest, ApiValidationRequest, MailTokenUser }
import org.joda.time.DateTime
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{ FakeHeaders, FakeRequest, Helpers }
import play.mvc.Http.{ HeaderNames, MimeTypes }

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import org.hatdex.hat.phata.models.ApiVerificationRequest

class AuthenticationSpec extends AuthenticationContext {

  "The `publicKey` method" should "Return public key of the HAT" in {
    val request = FakeRequest("GET", "http://hat.hubofallthings.net")

    val controller = application.injector.instanceOf[Authentication]
    val result     = controller.publicKey().apply(request)

    Helpers.status(result) must equal(OK)
    contentAsString(result) must startWith("-----BEGIN PUBLIC KEY-----\n")
  }

  "The `validateToken` method" should "return status 401 if authenticator but no identity was found" in {
    val request = FakeRequest("GET", "http://hat.hubofallthings.net")
      .withAuthenticator(LoginInfo("xing", "comedian@watchmen.com"))

    val controller = application.injector.instanceOf[Authentication]
    val result     = controller.validateToken().apply(request)

    Helpers.status(result) must equal(UNAUTHORIZED)
  }

  it should "Return simple success message for a valid token" in {
    val request = FakeRequest("GET", "http://hat.hubofallthings.net")
      .withAuthenticator(owner.loginInfo)

    val controller = application.injector.instanceOf[Authentication]
    val result     = controller.validateToken().apply(request)

    play.api.test.Helpers.status(result) must equal(OK)
    (contentAsJson(result) \ "message").as[String] must equal("Authenticated")
  }

  "The `hatLogin` method" should "return status 401 if authenticator but no identity was found" in {
    val request = FakeRequest("GET", "http://hat.hubofallthings.net")
      .withAuthenticator(LoginInfo("xing", "comedian@watchmen.com"))

    val controller             = application.injector.instanceOf[Authentication]
    val result: Future[Result] = controller.hatLogin("TestService", "http://testredirect").apply(request)

    Helpers.status(result) must equal(UNAUTHORIZED)
  }

  it should "return status 403 if authenticator and existing identity but wrong role" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(dataDebitUser.loginInfo)

    val controller             = application.injector.instanceOf[Authentication]
    val result: Future[Result] = controller.hatLogin("TestService", "http://testredirect").apply(request)

    Helpers.status(result) must equal(FORBIDDEN)
  }

  it should "return redirect url for authenticated owner" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(owner.loginInfo)

    val controller             = application.injector.instanceOf[Authentication]
    val result: Future[Result] = controller.hatLogin("TestService", "http://testredirect").apply(request)

    Helpers.status(result) must equal(OK)
    contentAsString(result).contains("testredirect")
    contentAsString(result).contains("token\\=")
  }

  "The `accessToken` method" should "return status 401 if no credentials provided" in {
    val request = FakeRequest("GET", "http://hat.hubofallthings.net")

    val controller             = application.injector.instanceOf[Authentication]
    val result: Future[Result] = controller.accessToken().apply(request)

    Helpers.status(result) must equal(UNAUTHORIZED)
  }

  it should "return status 401 if credentials but no matching identity" in {
    val request = FakeRequest("GET", "http://hat.hubofallthings.net")
      .withHeaders("username" -> "test", "password" -> "test")

    val controller = application.injector.instanceOf[Authentication]

    an[IdentityNotFoundException] should be thrownBy Await.result(controller
                                                                    .accessToken()
                                                                    .apply(request),
                                                                  30.seconds
    )
  }

  it should "return Access Token for the authenticated user" in {
    val request = FakeRequest("GET", "http://hat.hubofallthings.net")
      .withHeaders("username" -> "hatuser", "password" -> "pa55w0rd")

    val controller = application.injector.instanceOf[Authentication]

    val encoder  = new Base64AuthenticatorEncoder()
    val settings = JWTRS256AuthenticatorSettings("X-Auth-Token", None, "hat.org", Some(3.days), 3.days)

    val result: Future[Result] = controller.accessToken().apply(request)

    Helpers.status(result) must equal(OK)
    val token        = (contentAsJson(result) \ "accessToken").as[String]
    val unserialized = JWTRS256Authenticator.unserialize(token, encoder, settings)
    //unserialized must beSuccessfulTry
    unserialized.get.loginInfo must equal(owner.loginInfo)
  }

  "The passwordChangeProcess method should" should "return status 403 if not owner" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(dataDebitUser.loginInfo)
      .withJsonBody(Json.toJson(passwordChangeIncorrect))

    val controller             = application.injector.instanceOf[Authentication]
    val result: Future[Result] = Helpers.call(controller.passwordChangeProcess(), request)

    Helpers.status(result) must equal(FORBIDDEN)
  }

  it should "return status 403 if old password incorrect" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(owner.loginInfo)
      .withJsonBody(Json.toJson(passwordChangeIncorrect))

    val controller             = application.injector.instanceOf[Authentication]
    val result: Future[Result] = Helpers.call(controller.passwordChangeProcess(), request)

    Helpers.status(result) must equal(FORBIDDEN)
  }

  it should "return status 400 if new password too weak" in {
    val request = FakeRequest[JsValue](
      Helpers.POST,
      "/control/v2/auth/password",
      headers = FakeHeaders(Seq((HeaderNames.ACCEPT, MimeTypes.JSON), (HeaderNames.CONTENT_TYPE, MimeTypes.JSON))),
      body = Json.toJson(passwordChangeSimple),
      remoteAddress = "hat.hubofallthings.net"
    )
      .withAuthenticator(owner.loginInfo)

    val maybeResult: Option[Future[Result]] = Helpers.route(application, request)
    // What is this?
    //      val controller = application.injector.instanceOf[Authentication]
    //      val result: Future[Result] = Helpers.call(controller.passwordChangeProcess(), request)
    maybeResult must not be empty
    val result = maybeResult.get

    Helpers.status(result) must equal(BAD_REQUEST)
    (contentAsJson(result) \ "error").as[String] must equal("Bad Request")
  }

  it should "Change password if it is sufficiently strong" in {

    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(owner.loginInfo)
      .withJsonBody(Json.toJson(passwordChangeStrong))

    val controller             = application.injector.instanceOf[Authentication]
    val result: Future[Result] = Helpers.call(controller.passwordChangeProcess(), request)

    Helpers.status(result) must equal(OK)
    (contentAsJson(result) \ "message").as[String] must equal("Password changed")
  }

  "The `handleForgotPassword` method" should "Hide the fact that email doesn't match by returning status 200" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(dataDebitUser.loginInfo)
      .withJsonBody(Json.toJson(passwordForgottenIncorrect))

    val controller             = application.injector.instanceOf[Authentication]
    val result: Future[Result] = Helpers.call(controller.handleForgotPassword, request)

    Helpers.status(result) must equal(OK)
  }

  // Times out
  it should "Send email to the owner if provided email matches" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(dataDebitUser.loginInfo)
      .withJsonBody(Json.toJson(passwordForgottenOwner))

    val controller             = application.injector.instanceOf[Authentication]
    val result: Future[Result] = Helpers.call(controller.handleForgotPassword, request)

    Helpers.status(result) must equal(OK)
  }

  "The `handleResetPassword` method" should "Return status 401 if no such token exists" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(dataDebitUser.loginInfo)
      .withJsonBody(Json.toJson(passwordResetStrong))

    val controller             = application.injector.instanceOf[Authentication]
    val result: Future[Result] = Helpers.call(controller.handleResetPassword("nosuchtoken"), request)

    Helpers.status(result) must equal(UNAUTHORIZED)
    (contentAsJson(result) \ "cause").as[String] must equal("Token does not exist")
  }

  it should "Return status 401 if token has expired" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(owner.loginInfo)
      .withJsonBody(Json.toJson(passwordResetStrong))

    val controller   = application.injector.instanceOf[Authentication]
    val tokenService = application.injector.instanceOf[MailTokenUserService]
    val tokenId      = UUID.randomUUID().toString

    val result: Future[Result] = for {
      _ <- tokenService.create(MailTokenUser(tokenId, "hat@hat.org", DateTime.now().minusHours(1), isSignUp = false))
      result <- Helpers.call(controller.handleResetPassword(tokenId), request)
    } yield result

    Helpers.status(result) must equal(UNAUTHORIZED)
    (contentAsJson(result) \ "cause").as[String] must equal("Token expired or invalid")
  }

  it should "Return status 401 if token email doesn't match owner" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(owner.loginInfo)
      .withJsonBody(Json.toJson(passwordResetStrong))

    val controller   = application.injector.instanceOf[Authentication]
    val tokenService = application.injector.instanceOf[MailTokenUserService]
    val tokenId      = UUID.randomUUID().toString

    val result: Future[Result] = for {
      _ <- tokenService.create(MailTokenUser(tokenId, "email@hat.org", DateTime.now().plusHours(1), isSignUp = false))
      result <- Helpers.call(controller.handleResetPassword(tokenId), request)
    } yield result

    Helpers.status(result) must equal(UNAUTHORIZED)
    (contentAsJson(result) \ "cause").as[String] must equal("Only HAT owner can reset their password")
  }

  // Times out
  it should "Reset password" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(owner.loginInfo)
      .withJsonBody(Json.toJson(passwordResetStrong))

    val controller   = application.injector.instanceOf[Authentication]
    val tokenService = application.injector.instanceOf[MailTokenUserService]
    val tokenId      = UUID.randomUUID().toString
    val result: Future[Result] = for {
      _ <- tokenService.create(MailTokenUser(tokenId, "user@hat.org", DateTime.now().plusHours(1), isSignUp = false))
      result <- Helpers.call(controller.handleResetPassword(tokenId), request)
    } yield result

    Helpers.status(result) must equal(OK)
  }

  it should "Return status 401 if no owner exists (should never happen)" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(owner.loginInfo)
      .withJsonBody(Json.toJson(passwordResetStrong))

    val controller   = application.injector.instanceOf[Authentication]
    val tokenService = application.injector.instanceOf[MailTokenUserService]
    val tokenId      = UUID.randomUUID().toString
    val userService  = application.injector.instanceOf[UserService]

    val result: Future[Result] = for {
      _ <- tokenService.create(MailTokenUser(tokenId, "user@hat.org", DateTime.now().plusHours(1), isSignUp = false))
      _ <- userService.saveUser(
             owner.copy(roles = Seq())
           ) // forcing owner user to a different role for the test
      result <- Helpers.call(controller.handleResetPassword(tokenId), request)
    } yield result

    Helpers.status(result) must equal(UNAUTHORIZED)
    (contentAsJson(result) \ "cause").as[String] must equal("No user matching token")
  }

  "The `handleVerificationRequest` method" should "flexibly handle matching redirectURL" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(owner.loginInfo)
      .withJsonBody(Json.toJson(apiVerificationRequestMatching))

    val controller             = application.injector.instanceOf[Authentication]
    val result: Future[Result] = Helpers.call(controller.handleVerificationRequest(None), request)

    Helpers.status(result) must equal(OK)
  }

  it should "not match a different redirect URL base" in {
    val request = FakeRequest("POST", "http://hat.hubofallthings.net")
      .withAuthenticator(owner.loginInfo)
      .withJsonBody(Json.toJson(apiVerificationRequestNotMatching))

    val controller             = application.injector.instanceOf[Authentication]
    val result: Future[Result] = Helpers.call(controller.handleVerificationRequest(None), request)

    Helpers.status(result) must equal(OK)
  }

}

class AuthenticationContext extends HATTestContext {
  val passwordChangeIncorrect: ApiPasswordChange =
    ApiPasswordChange("some-passwords-are-better-than-others", Some("wrongOldPassword"))
  val passwordChangeSimple: ApiPasswordChange = ApiPasswordChange("simple", Some("pa55w0rd"))
  val passwordChangeStrong: ApiPasswordChange =
    ApiPasswordChange("some-passwords-are-better-than-others", Some("pa55w0rd"))
  val passwordResetStrong: ApiPasswordChange = ApiPasswordChange("some-passwords-are-better-than-others", None)

  val passwordForgottenIncorrect: ApiPasswordResetRequest = ApiPasswordResetRequest("email@example.com")
  val passwordForgottenOwner: ApiPasswordResetRequest     = ApiPasswordResetRequest("user@hat.org")

  val passwordValidationIncorrect: ApiValidationRequest = ApiValidationRequest("email@example.com", "appId")
  val passwordValidationOwner: ApiValidationRequest     = ApiValidationRequest("user@hat.org", "appId")

  val apiVerificationRequestMatching: ApiVerificationRequest = 
    ApiVerificationRequest("notablesAuth", "user@hat.org", "https://api.onezero-me.com/auth/redirectfromhat/scoring/facebook/avante/aruz3sc/8e4df03ffdffe0034da0c9008813f2a7:fd43cdc55b52fa31b47fa797ceef5424d52d269f5c3ed8b82858112c1a098a56a6689e101e5c8d499bc79eb28fe61b4d32ed2a9686727bd6f8f93e987b7a4e79")

  val apiVerificationRequestNotMatching: ApiVerificationRequest = 
    ApiVerificationRequest("notablesAuth", "user@hat.org", "https://api2.onezero-me.com/auth/redirectfromhat/scoring/facebook/avante/aruz3sc/8e4df03ffdffe0034da0c9008813f2a7:fd43cdc55b52fa31b47fa797ceef5424d52d269f5c3ed8b82858112c1a098a56a6689e101e5c8d499bc79eb28fe61b4d32ed2a9686727bd6f8f93e987b7a4e79")
}
