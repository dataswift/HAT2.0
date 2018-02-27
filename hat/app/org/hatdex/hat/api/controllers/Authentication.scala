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

import java.net.URLDecoder
import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials, PasswordHasherRegistry }
import com.mohiva.play.silhouette.api.{ LoginEvent, Silhouette }
import com.mohiva.play.silhouette.impl.exceptions.{ IdentityNotFoundException, InvalidPasswordException }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.{ HatServicesService, MailTokenService, UsersService }
import org.hatdex.hat.authentication._
import org.hatdex.hat.phata.models.{ ApiPasswordChange, ApiPasswordResetRequest, MailTokenUser }
import org.hatdex.hat.resourceManagement.{ HatServerProvider, _ }
import org.hatdex.hat.utils.{ HatBodyParsers, HatMailer }
import play.api.cache.{ Cached, CachedBuilder }
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Authentication @Inject() (
    components: ControllerComponents,

    configuration: Configuration,
    cached: Cached,
    parsers: HatBodyParsers,
    hatServerProvider: HatServerProvider,
    silhouette: Silhouette[HatApiAuthEnvironment],
    credentialsProvider: CredentialsProvider[HatServer],
    hatServicesService: HatServicesService,
    passwordHasherRegistry: PasswordHasherRegistry,
    authInfoRepository: AuthInfoRepository[HatServer],
    usersService: UsersService,
    clock: Clock,
    mailer: HatMailer,
    tokenService: MailTokenService[MailTokenUser],
    limiter: UserLimiter) extends HatApiController(components, silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  private val logger = Logger(this.getClass)

  private val indefiniteSuccessCaching: CachedBuilder = cached
    .status(req => s"${req.host}${req.path}", 200)
    .includeStatus(404, 600)

  def publicKey(): EssentialAction = indefiniteSuccessCaching {
    UserAwareAction.async { implicit request =>
      val publicKey = hatServerProvider.toString(request.dynamicEnvironment.publicKey)
      Future.successful(Ok(publicKey))
    }
  }

  def validateToken(): Action[AnyContent] = SecuredAction.async { implicit request =>
    Future.successful(Ok(Json.toJson(SuccessResponse("Authenticated"))))
  }

  def hatLogin(name: String, redirectUrl: String): Action[AnyContent] = SecuredAction(WithRole(Owner())).async { implicit request =>
    for {
      service <- hatServicesService.findOrCreateHatService(name, redirectUrl)
      linkedService <- hatServicesService.hatServiceLink(request.identity, service, Some(redirectUrl))
      - <- usersService.logLogin(request.identity, "hatLogin", linkedService.category, Some(name), Some(redirectUrl))
    } yield {
      Ok(Json.toJson(SuccessResponse(linkedService.url)))
    }
  }

  def applicationToken(name: String, resource: String): Action[AnyContent] = SecuredAction(WithRole(Owner())).async { implicit request =>
    for {
      service <- hatServicesService.findOrCreateHatService(name, resource)
      token <- hatServicesService.hatServiceToken(request.identity, service)
    } yield {
      Ok(Json.toJson(token))
    }
  }

  private val hatService = HatService(
    "hat", "hat", "HAT API",
    "", "", "",
    browser = true,
    category = "api",
    setup = true,
    loginAvailable = true)

  def accessToken(): Action[AnyContent] = (UserAwareAction andThen limiter.UserAwareRateLimit).async { implicit request =>
    val eventuallyAuthenticatedUser = for {
      usernameParam <- request.headers.get("username")
      passwordParam <- request.headers.get("password")
    } yield {
      val username = URLDecoder.decode(usernameParam, "UTF-8")
      val password = URLDecoder.decode(passwordParam, "UTF-8")
      credentialsProvider.authenticate(Credentials(username, password))
        .map(_.copy(request.dynamicEnvironment.id))
        .flatMap { loginInfo =>
          usersService.getUser(loginInfo.providerKey).flatMap {
            case Some(user) =>
              val customClaims = hatServicesService.generateUserTokenClaims(user, hatService)
              for {
                authenticator <- env.authenticatorService.create(loginInfo)
                token <- env.authenticatorService.init(authenticator.copy(customClaims = Some(customClaims)))
                _ <- usersService.logLogin(user, "api", user.roles.filter(_.extra.isEmpty).map(_.title).mkString(":"), None, None)
                result <- env.authenticatorService.embed(token, Ok(Json.toJson(AccessToken(token, user.userId))))
              } yield {
                env.eventBus.publish(LoginEvent(user, request))
                result
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }
    }
    eventuallyAuthenticatedUser getOrElse {
      Future.successful(Unauthorized(Json.toJson(ErrorMessage("Credentials required", "No username or password provided to retrieve token"))))
    }
  }

  def passwordChangeProcess: Action[ApiPasswordChange] = SecuredAction(WithRole(Owner())).async(parsers.json[ApiPasswordChange]) { implicit request =>
    logger.debug("Processing password change request")
    request.body.password map { oldPassword =>
      val eventualResult = for {
        _ <- credentialsProvider.authenticate(Credentials(request.identity.email, oldPassword))
        _ <- authInfoRepository.update(request.identity.loginInfo, passwordHasherRegistry.current.hash(request.body.newPassword))
        authenticator <- env.authenticatorService.create(request.identity.loginInfo)
        result <- env.authenticatorService.renew(authenticator, Ok(Json.toJson(SuccessResponse("Password changed"))))
      } yield {
        env.eventBus.publish(LoginEvent(request.identity, request))
        mailer.passwordChanged(request.identity.email, request.identity)
        result
      }

      eventualResult recover {
        case _: InvalidPasswordException => Forbidden(Json.toJson(ErrorMessage("Invalid password", "Old password invalid")))
      }
    } getOrElse {
      Future.successful(Forbidden(Json.toJson(ErrorMessage("Invalid password", "Old password missing"))))
    }
  }

  /**
   * Sends an email to the user with a link to reset the password
   */
  def handleForgotPassword: Action[ApiPasswordResetRequest] = UserAwareAction.async(parsers.json[ApiPasswordResetRequest]) { implicit request =>
    logger.debug("Processing forgotten password request")
    val email = request.body.email
    val response = Ok(Json.toJson(SuccessResponse("If the email you have entered is correct, you will shortly receive an email with password reset instructions")))
    if (email == request.dynamicEnvironment.ownerEmail) {
      usersService.listUsers.map(_.find(_.roles.contains(Owner()))).flatMap {
        case Some(user) =>
          val token = MailTokenUser(email, isSignUp = false)
          tokenService.create(token).map { _ =>
            val scheme = if (request.secure) {
              "https://"
            }
            else {
              "http://"
            }
            val resetLink = s"$scheme${request.host}/#/user/password/change/${token.id}"
            mailer.passwordReset(email, user, resetLink)
            response
          }

        case None => Future.successful(response)
      }
    }
    else {
      Future.successful(response)
    }

  }

  /**
   * Saves the new password and authenticates the user
   */
  def handleResetPassword(tokenId: String): Action[ApiPasswordChange] = UserAwareAction.async(parsers.json[ApiPasswordChange]) { implicit request =>
    tokenService.retrieve(tokenId).flatMap {
      case Some(token) if !token.isSignUp && !token.isExpired =>
        if (token.email == request.dynamicEnvironment.ownerEmail) {
          usersService.listUsers.map(_.find(_.roles.contains(Owner()))).flatMap {
            case Some(user) =>
              for {
                _ <- authInfoRepository.update(user.loginInfo, passwordHasherRegistry.current.hash(request.body.newPassword))
                authenticator <- env.authenticatorService.create(user.loginInfo)
                result <- env.authenticatorService.renew(authenticator, Ok(Json.toJson(SuccessResponse("Password reset"))))
              } yield {
                tokenService.consume(tokenId)
                env.eventBus.publish(LoginEvent(user, request))
                mailer.passwordChanged(token.email, user)
                result
              }
            case None => Future.successful(Unauthorized(Json.toJson(ErrorMessage("Password reset unauthorized", "No user matching token"))))
          }
        }
        else {
          Future.successful(Unauthorized(Json.toJson(ErrorMessage("Password reset unauthorized", "Only HAT owner can reset their password"))))
        }
      case Some(_) =>
        tokenService.consume(tokenId)
        Future.successful(Unauthorized(Json.toJson(ErrorMessage("Invalid Token", "Token expired or invalid"))))
      case None =>
        Future.successful(Unauthorized(Json.toJson(ErrorMessage("Invalid Token", "Token does not exist"))))
    }
  }
}
