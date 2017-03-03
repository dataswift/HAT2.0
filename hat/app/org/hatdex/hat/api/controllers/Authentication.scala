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

import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials, PasswordHasherRegistry }
import com.mohiva.play.silhouette.api.{ LoginEvent, Silhouette }
import com.mohiva.play.silhouette.impl.exceptions.InvalidPasswordException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models.{ ErrorMessage, SuccessResponse }
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.authentication._
import org.hatdex.hat.phata.models.{ ApiPasswordChange, ApiPasswordResetRequest, MailTokenUser }
import org.hatdex.hat.phata.service.{ HatServicesService, MailTokenService }
import org.hatdex.hat.resourceManagement.{ HatServerProvider, _ }
import org.hatdex.hat.utils.{ HatBodyParsers, HatMailer }
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Authentication @Inject() (
    val messagesApi: MessagesApi,
    configuration: Configuration,
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
    tokenService: MailTokenService[MailTokenUser]) extends HatApiController(silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  private val logger = Logger(this.getClass)

  def hatLogin(name: String, redirectUrl: String): Action[AnyContent] = SecuredAction(WithRole("owner")).async { implicit request =>
    for {
      service <- hatServicesService.findOrCreateHatService(name, redirectUrl)
      linkedService <- hatServicesService.hatServiceLink(request.identity, service, Some(redirectUrl))
      - <- usersService.logLogin(request.identity, "hatLogin", linkedService.category, Some(name), Some(redirectUrl))
    } yield {
      Ok(Json.toJson(SuccessResponse(linkedService.url)))
    }
  }

  def passwordChangeProcess: Action[ApiPasswordChange] = SecuredAction(WithRole("owner")).async(parsers.json[ApiPasswordChange]) { implicit request =>
    val eventualResult = for {
      _ <- credentialsProvider.authenticate(Credentials(request.identity.email, request.body.password.get))
      _ <- authInfoRepository.update(request.identity.loginInfo, passwordHasherRegistry.current.hash(request.body.newPassword))
      authenticator <- env.authenticatorService.create(request.identity.loginInfo)
      result <- env.authenticatorService.renew(authenticator, Ok(Json.toJson(SuccessResponse("Password changed"))))
    } yield {
      env.eventBus.publish(LoginEvent(request.identity, request))
      mailer.passwordChanged(request.identity.email, request.identity)
      result
    }

    eventualResult recover {
      case e: InvalidPasswordException => Forbidden(Json.toJson(ErrorMessage("Invalid password", "Old password invalid")))
    }
  }

  /**
   * Sends an email to the user with a link to reset the password
   */
  def handleForgotPassword: Action[ApiPasswordResetRequest] = UserAwareAction.async(parsers.json[ApiPasswordResetRequest]) { implicit request =>
    val email = request.body.email
    val response = Ok(Json.toJson(SuccessResponse("If the email you have entered is correct, you will shortly receive an email with password reset instructions")))
    if (email == request.dynamicEnvironment.ownerEmail) {
      usersService.listUsers.map(_.find(_.role == "owner")).flatMap {
        case Some(user) =>
          val token = MailTokenUser(email, isSignUp = false)
          tokenService.create(token).map { _ =>
            // TODO generate password reset link for frontend to handle
            val resetLink = "" //routes.Authentication.resetPassword(token.id).absoluteURL()
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
          usersService.listUsers.map(_.find(_.role == "owner")).flatMap {
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
