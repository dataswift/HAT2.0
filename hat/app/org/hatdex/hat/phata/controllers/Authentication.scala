/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
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
 */
package org.hatdex.hat.phata.controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials, PasswordHasherRegistry }
import com.mohiva.play.silhouette.api.{ LoginEvent, LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.JWTRS256Authenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import org.hatdex.hat.api.actors.{ EmailMessage, EmailService }
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.authentication.{ HasFrontendRole, HatFrontendAuthEnvironment, HatFrontendController, WithRole }
import org.hatdex.hat.phata.models.{ LoginDetails, PasswordChange }
import org.hatdex.hat.phata.service.{ HatServicesService, NotablesService, UserProfileService }
import org.hatdex.hat.resourceManagement.{ HatServerProvider, _ }
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

import org.hatdex.hat.phata.{ views => phataViews }

class Authentication @Inject() (
    val messagesApi: MessagesApi,
    configuration: Configuration,
    silhouette: Silhouette[HatFrontendAuthEnvironment],
    hatServerProvider: HatServerProvider,
    clock: Clock,
    credentialsProvider: CredentialsProvider[HatServer],
    hatServicesService: HatServicesService,
    userProfileService: UserProfileService,
    usersService: UsersService,
    emailService: EmailService,
    notablesService: NotablesService,
    passwordHasherRegistry: PasswordHasherRegistry,
    jwtAuthenticatorService: AuthenticatorService[JWTRS256Authenticator, HatServer],
    authInfoRepository: AuthInfoRepository[HatServer]
) extends HatFrontendController(silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  import org.hatdex.hat.phata.models.HatPublicInfo.hatServer2PublicInfo

  private val logger = Logger(this.getClass)
  private val emailForm = Form(single("email" -> email))

  def signin: Action[AnyContent] = UserAwareAction { implicit request =>
    Ok(phataViews.html.simpleLogin(LoginDetails.loginForm))
  }

  def hatLogin(name: String, redirectUrl: String) = UserAwareAction.async { implicit request =>
    request.identity.filter(i => HasFrontendRole.isAuthorized(i, "owner")) map { implicit identity =>
      processHatLogin(name, redirectUrl, identity)
    } getOrElse {
      val loginDetails = LoginDetails(username = request.dynamicEnvironment.hatName, remember = None, password = "", name = Some(name), redirect = Some(redirectUrl))
      Future.successful(Ok(phataViews.html.simpleLogin(LoginDetails.loginForm.fill(loginDetails))))
    }
  }

  def processHatLogin(name: String, redirectUrl: String, identity: HatUser)(implicit hatServer: HatServer, request: RequestHeader): Future[Result] = {
    hatServicesService.hatServices(Set("app", "dataplug", "testapp")) flatMap { approvedHatServices =>
      for {
        service <- hatServicesService.findOrCreateHatService(name, redirectUrl)
        linkedService <- hatServicesService.hatServiceLink(identity, service)
      } yield {
        if (service.setup) {
          Redirect(linkedService.url)
        }
        else {
          val services = Seq(linkedService)
          Ok(phataViews.html.authenticated(identity, services))
        }
      }
    }
  }

  def login: Action[AnyContent] = UserAwareAction.async { implicit request =>
    LoginDetails.loginForm.bindFromRequest.fold(
      formWithErrors => {
        logger.info(s"Form with errors: $formWithErrors")
        Future.successful(BadRequest(phataViews.html.simpleLogin(formWithErrors)))
      },
      loginDetails =>
        credentialsProvider.authenticate(Credentials(loginDetails.username, loginDetails.password))
          .flatMap { loginInfo =>
            usersService.getUser(loginInfo.providerKey).flatMap {
              case Some(user) =>
                val eventualLoginResult = loginDetails match {
                  case LoginDetails(_, _, _, Some(name), Some(redirect)) => processHatLogin(name, redirect, user)
                  case _ => Future.successful(Redirect(routes.Phata.home().url))
                }

                for {
                  response <- eventualLoginResult
                  authenticator <- env.authenticatorService.create(loginInfo).map(authenticatorWithRememberMe(_, loginDetails.remember.getOrElse(false)))
                  cookie <- env.authenticatorService.init(authenticator)
                  result <- env.authenticatorService.embed(cookie, response)
                } yield {
                  env.eventBus.publish(LoginEvent(user, request))
                  result
                }
              case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
            }
          } recover {
            case e =>
              Ok(phataViews.html.simpleLogin(LoginDetails.loginForm, Some("Invalid Credentials!")))
          }
    )
  }

  def logout: Action[AnyContent] = UserAwareAction.async { implicit request =>
    request.identity map { identity =>
      env.eventBus.publish(LogoutEvent(identity, request))
      env.authenticatorService.discard(request.authenticator.get, Redirect(routes.Phata.home().url))
    } getOrElse {
      Future.successful(Redirect(routes.Phata.home().url))
    }
  }

  def passwordChangeStart() = SecuredAction(HasFrontendRole("owner")).async { implicit request =>
    Future.successful(Ok(phataViews.html.passwordChange(request.identity, Seq())))
  }

  def passwordChangeProcess() = SecuredAction(HasFrontendRole("owner")).async { implicit request =>
    PasswordChange.passwordChangeForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.debug(s"Form with errors: $formWithErrors")
        Future.successful(BadRequest(phataViews.html.passwordChange(request.identity, Seq("Passwords do not match"))))
      },

      loginDetails => {
        for {
          _ <- authInfoRepository.update(request.identity.loginInfo, passwordHasherRegistry.current.hash(loginDetails.newPassword))
          authenticator <- env.authenticatorService.create(request.identity.loginInfo)
          result <- env.authenticatorService.renew(
            authenticator,
            Ok(phataViews.html.passwordChange(request.identity, Seq(), changed = true))
          )
        } yield {
          env.eventBus.publish(LoginEvent(request.identity, request))
          result
        }
      }
    )
  }

  def passwordResetStart = UserAwareAction { implicit request =>
    request.identity map { identity =>
      Redirect(routes.Phata.home().url)
    } getOrElse {
      Ok(phataViews.html.passwordReset(emailForm))
    }
  }

  def passwordResetProcess = UserAwareAction.async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(phataViews.html.passwordReset(emailForm))),
      email => {
        logger.error(s"PAssword reset for email $email")
        if (email == request.dynamicEnvironment.ownerEmail) {
          usersService.listUsers.map(_.find(_.role == "owner")).map(_.get) map {
            case user =>
              logger.error(s"Found owner user $user")
              val protocol = if (configuration.getBoolean("hat.tls").get) {
                "https://"
              }
              else {
                "http://"
              }
              val resetLink = s"$protocol${request.dynamicEnvironment.domain}/passwordreset/confirm?X-Auth-Token="
              val message = EmailMessage(
                "HAT - reset your password",
                email, // to
                s"owner@${request.dynamicEnvironment.domain}", // from
                phataViews.txt.emailPasswordReset.render(user, resetLink).toString(),
                phataViews.html.emailPasswordReset.render(user, resetLink).toString(),
                1 minute, 5
              )

              logger.error(s"Sending password reset email $message")
              emailService.send(message)
          } recover {
            case e =>
              logger.error(s"Finding user failed ${e.getMessage}")
          }

          Future.successful(Ok(phataViews.html.simpleMessage("If the email you have entered is correct, you will shortly receive an email with password reset instructions")))
        }
        else {
          logger.error(s"email doesn't match: $email ${request.dynamicEnvironment.ownerEmail}")
          Future.successful(Ok(phataViews.html.simpleMessage("If the email you have entered is correct, you will shortly receive an email with password reset instructions")))
        }
      }
    )
  }

  def passwordResetConfirmStart = SecuredAction { implicit request =>
    Ok(phataViews.html.passwordResetConfirm(request.identity, Seq(), token = request.getQueryString("X-Auth-Token").getOrElse("")))
  }

  def passwordResetConfirmProcess = SecuredAction { implicit request =>
    PasswordChange.passwordChangeForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.debug(s"Form with errors: $formWithErrors")
        Ok(phataViews.html.passwordResetConfirm(request.identity, Seq("Passwords do not match"), token = request.getQueryString("X-Auth-Token").getOrElse("")))
      },

      loginDetails => {
        for {
          _ <- authInfoRepository.update(request.identity.loginInfo, passwordHasherRegistry.current.hash(loginDetails.newPassword))
          authenticator <- env.authenticatorService.create(request.identity.loginInfo)
          result <- env.authenticatorService.renew(
            authenticator,
            Ok(phataViews.html.passwordResetConfirm(request.identity, Seq(), changed = true, token = request.getQueryString("X-Auth-Token").get))
          )
        } yield {
          env.eventBus.publish(LoginEvent(request.identity, request))
          result
        }

        Ok(phataViews.html.passwordResetConfirm(request.identity, Seq(), changed = true, token = request.getQueryString("X-Auth-Token").get))
      }
    )
  }
}
