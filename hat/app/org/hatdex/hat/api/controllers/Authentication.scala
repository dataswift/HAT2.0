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

import java.net.{URLDecoder, URLEncoder}

import akka.Done
import javax.inject.Inject
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.api.{LoginEvent, Silhouette}
import com.mohiva.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.applications.ApplicationsService
import org.hatdex.hat.api.service.{HatServicesService, LogService, MailTokenService, UsersService}
import org.hatdex.hat.authentication._
import org.hatdex.hat.phata.models._
import org.hatdex.hat.resourceManagement.{HatServerProvider, _}
import org.hatdex.hat.utils.{HatBodyParsers, HatMailer}
import play.api.{Configuration, Logger}
import play.api.cache.{Cached, CachedBuilder}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Authentication @Inject() (
    components: ControllerComponents,
    cached: Cached,
    configuration: Configuration,
    parsers: HatBodyParsers,
    hatServerProvider: HatServerProvider,
    silhouette: Silhouette[HatApiAuthEnvironment],
    credentialsProvider: CredentialsProvider[HatServer],
    hatServicesService: HatServicesService,
    passwordHasherRegistry: PasswordHasherRegistry,
    authInfoRepository: AuthInfoRepository[HatServer],
    usersService: UsersService,
    applicationsService: ApplicationsService,
    logService: LogService,
    mailer: HatMailer,
    tokenService: MailTokenService[MailTokenUser],
    wsClient: WSClient,
    limiter: UserLimiter) extends HatApiController(components, silhouette) with HatJsonFormats {

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

  /**
   * Sends an email to the owner with a link to claim the hat
   */
  def handleClaimStart(): Action[ApiClaimHatRequest] = UserAwareAction.async(parsers.json[ApiClaimHatRequest]) { implicit request =>

    val claimHatRequest = request.body
    val email = request.dynamicEnvironment.ownerEmail
    val response = Ok(Json.toJson(SuccessResponse("You will shortly receive an email with claim instructions")))

    if (claimHatRequest.email == email) {
      usersService.listUsers.map(_.find(_.roles.contains(Owner()))).flatMap {
        case Some(user) =>
          applicationsService.applicationStatus()(request.dynamicEnvironment, user, request).flatMap { applications =>
            val maybeApplication = applications.find(_.application.id.equals(claimHatRequest.applicationId))
            val maybeAppDetails = maybeApplication.map { app =>
              ((app.application.info.name, app.application.developer.logo.map(_.normal).getOrElse("#")),
                (app.application.id, app.application.info.version.toString))
            }

            val scheme = if (request.secure) {
              "https://"
            }
            else {
              "http://"
            }

            tokenService.retrieve(email, isSignup = true).flatMap {
              case Some(existingTokenUser) if !existingTokenUser.isExpired =>
                val claimLink = s"$scheme${request.host}/#/hat/claim/${existingTokenUser.id}?email=${URLEncoder.encode(email, "UTF-8")}"
                mailer.claimHat(email, claimLink, maybeAppDetails.map(_._1))

                Future.successful(response)
              case Some(_) => Future.successful(Ok(Json.toJson(SuccessResponse("The HAT is already claimed"))))

              case None =>
                val token = MailClaimTokenUser(email)

                val eventualResult = for {
                  _ <- tokenService.create(token)
                  _ <- logService
                    .logAction(request.dynamicEnvironment.domain, LogRequest("unclaimed", None, None), maybeAppDetails.map(_._2))
                    .recover {
                      case e =>
                        logger.error(s"LogActionError::unclaimed. Reason: ${e.getMessage}")
                        Done
                    }
                } yield {

                  val claimLink = s"$scheme${request.host}/#/hat/claim/${token.id}?email=${URLEncoder.encode(email, "UTF-8")}"
                  mailer.claimHat(email, claimLink, maybeAppDetails.map(_._1))

                  response
                }

                eventualResult.recover {
                  case e =>
                    logger.error(s"Could not create new HAT claim token. Reason: ${e.getMessage}")
                    InternalServerError(Json.toJson(ErrorMessage("Internal Server Error", "Failed to initialize HAT claim process")))
                }
            }
          }
        case None => Future.successful(response)
      }
    }
    else {
      Future.successful(response)
    }
  }

  def handleClaimComplete(claimToken: String): Action[HatClaimCompleteRequest] = UserAwareAction.async(parsers.json[HatClaimCompleteRequest]) { implicit request =>
    implicit val hatClaimComplete: HatClaimCompleteRequest = request.body

    tokenService.retrieve(claimToken).flatMap {
      case Some(token) if token.isSignUp && !token.isExpired && token.email == request.dynamicEnvironment.ownerEmail =>
        usersService.listUsers.map(_.find(_.roles.contains(Owner()))).flatMap {
          case Some(user) =>
            val eventualResult = for {
              _ <- updateHatMembership(hatClaimComplete)
              _ <- authInfoRepository.update(user.loginInfo, passwordHasherRegistry.current.hash(request.body.password))
              _ <- tokenService.expire(token.id)
              authenticator <- env.authenticatorService.create(user.loginInfo)
              result <- env.authenticatorService.renew(authenticator, Ok(Json.toJson(SuccessResponse("HAT claimed"))))
              _ <- logService.logAction(request.dynamicEnvironment.domain, LogRequest("claimed", None, None), None).recover {
                case e =>
                  logger.error(s"LogActionError::unclaimed. Reason: ${e.getMessage}")
                  Done
              }
            } yield {
              //env.eventBus.publish(LoginEvent(user, request))
              //mailer.passwordChanged(token.email, user)

              result
            }

            eventualResult.recover {
              case e =>
                logger.error(s"HAT claim process failed with error ${e.getMessage}")
                BadRequest(Json.toJson(ErrorMessage("Bad Request", "HAT claim process failed")))
            }

          case None => Future.successful(Unauthorized(Json.toJson(ErrorMessage("HAT claim unauthorized", "No user matching token"))))
        }

      case Some(_) =>
        Future.successful(Unauthorized(Json.toJson(ErrorMessage("Invalid Token", "Token expired or invalid"))))

      case None =>
        Future.successful(Unauthorized(Json.toJson(ErrorMessage("Invalid Token", "Token does not exist"))))
    }
  }

  private def updateHatMembership(claim: HatClaimCompleteRequest): Future[Done] = {
    val path = "api/products/hat/claim"
    val hattersUrl = s"${configuration.underlying.getString("hatters.scheme")}${configuration.underlying.getString("hatters.address")}"

    logger.info(s"Proxy POST request to $hattersUrl/$path with parameters: $claim")

    val futureResponse = wsClient.url(s"$hattersUrl/$path")
      //.withHttpHeaders("x-auth-token" → token.accessToken)
      .post(Json.toJson(claim.copy(password = "")))

    futureResponse.flatMap { response =>
      response.status match {
        case OK =>
          Future.successful(Done)
        case _ =>
          logger.error(s"Failed to claim HAT with Hatters. Claim details:\n$claim\nHatters response: ${response.body}")
          Future.failed(new UnknownError("HAT claim failed"))
      }
    }
  }
}
