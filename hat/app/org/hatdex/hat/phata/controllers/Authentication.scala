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
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.authentication.{ HasFrontendRole, HatFrontendAuthEnvironment, HatFrontendController }
import org.hatdex.hat.phata.models.{ LoginDetails, MailTokenUser, PasswordChange }
import org.hatdex.hat.phata.service.{ HatServicesService, MailTokenService, NotablesService, UserProfileService }
import org.hatdex.hat.phata.{ views => phataViews }
import org.hatdex.hat.resourceManagement.{ HatServerProvider, _ }
import org.hatdex.hat.utils.HatMailer
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
    mailer: HatMailer,
    notablesService: NotablesService,
    passwordHasherRegistry: PasswordHasherRegistry,
    tokenService: MailTokenService[MailTokenUser],
    jwtAuthenticatorService: AuthenticatorService[JWTRS256Authenticator, HatServer],
    authInfoRepository: AuthInfoRepository[HatServer]) extends HatFrontendController(silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  import org.hatdex.hat.phata.models.HatPublicInfo.hatServer2PublicInfo

  private val logger = Logger(this.getClass)
  private val emailForm = Form(single("email" -> email))

  private val protocol = if (configuration.getBoolean("hat.tls").get) {
    "https://"
  }
  else {
    "http://"
  }

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
          })
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
    Future.successful(Ok(phataViews.html.passwordChange(request.identity, PasswordChange.passwordChangeForm, Seq())))
  }

  def passwordChangeProcess() = SecuredAction(HasFrontendRole("owner")).async { implicit request =>
    PasswordChange.passwordChangeForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.debug(s"Form with errors: $formWithErrors")
        Future.successful(BadRequest(phataViews.html.passwordChange(request.identity, formWithErrors, Seq("Passwords do not match"))))
      },

      loginDetails => {
        for {
          _ <- authInfoRepository.update(request.identity.loginInfo, passwordHasherRegistry.current.hash(loginDetails.newPassword))
          authenticator <- env.authenticatorService.create(request.identity.loginInfo)
          result <- env.authenticatorService.renew(
            authenticator,
            Ok(phataViews.html.passwordChange(request.identity, PasswordChange.passwordChangeForm.fill(loginDetails), Seq(), changed = true)))
        } yield {
          env.eventBus.publish(LoginEvent(request.identity, request))
          result
        }
      })
  }

  /**
   * Starts the reset password mechanism if the user has forgot his password. It shows a form to insert his email address.
   */
  def forgotPassword: Action[AnyContent] = UserAwareAction.async { implicit request =>
    Future.successful(request.identity match {
      case Some(_) => Redirect(routes.Phata.home().url)
      case None    => Ok(phataViews.html.passwordForgot(emailForm))
    })
  }

  /**
   * Sends an email to the user with a link to reset the password
   */
  def handleForgotPassword: Action[AnyContent] = UserAwareAction.async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(phataViews.html.passwordForgot(formWithErrors))),
      email => {
        val response = Ok(phataViews.html.simpleMessage("If the email you have entered is correct, you will shortly receive an email with password reset instructions"))
        if (email == request.dynamicEnvironment.ownerEmail) {
          usersService.listUsers.map(_.find(_.role == "owner")).flatMap {
            case Some(user) =>
              val token = MailTokenUser(email, isSignUp = false)
              tokenService.create(token).map { _ =>
                val resetLink = routes.Authentication.resetPassword(token.id).absoluteURL()
                logger.error(s"Sending password reset email for $email")
                mailer.passwordReset(email, user, resetLink)
                response
              }

            case None => Future.successful(response)
          }
        }
        else {
          logger.error(s"email doesn't match: $email ${request.dynamicEnvironment.ownerEmail}")
          Future.successful(response)
        }
      })
  }

  /**
   * Confirms the user's link based on the token and shows him a form to reset the password
   */
  def resetPassword(tokenId: String): Action[AnyContent] = UserAwareAction.async { implicit request =>
    tokenService.retrieve(tokenId).flatMap {
      case Some(token) if !token.isSignUp && !token.isExpired =>
        Future.successful(Ok(phataViews.html.passwordReset(tokenId, PasswordChange.passwordChangeForm)))
      case Some(token) =>
        tokenService.consume(tokenId)
        Future.successful(Redirect(routes.Phata.home().url))
      case None => Future.successful(Redirect(routes.Phata.home().url))
    }
  }

  /**
   * Saves the new password and authenticates the user
   */
  def handleResetPassword(tokenId: String): Action[AnyContent] = UserAwareAction.async { implicit request =>
    PasswordChange.passwordChangeForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(phataViews.html.passwordReset(tokenId, formWithErrors))),
      passwords => {
        tokenService.retrieve(tokenId).flatMap {
          case Some(token) if !token.isSignUp && !token.isExpired =>
            if (token.email == request.dynamicEnvironment.ownerEmail) {
              usersService.listUsers.map(_.find(_.role == "owner")).flatMap {
                case Some(user) =>
                  for {
                    _ <- authInfoRepository.update(user.loginInfo, passwordHasherRegistry.current.hash(passwords.newPassword))
                    authenticator <- env.authenticatorService.create(user.loginInfo)
                    result <- env.authenticatorService.renew(authenticator, Ok(phataViews.html.passwordResetSuccess(user)))
                  } yield {
                    tokenService.consume(tokenId)
                    env.eventBus.publish(LoginEvent(user, request))
                    result
                  }
                case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
              }
            }
            else {
              Future.successful(Redirect(routes.Phata.home().url))
            }
          case Some(token) =>
            tokenService.consume(tokenId)
            Future.successful(Redirect(routes.Phata.home().url))
          case None => Future.successful(Redirect(routes.Phata.home().url))
        }
      })
  }
}
