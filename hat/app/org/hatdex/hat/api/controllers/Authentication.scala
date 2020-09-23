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

import java.net.{ URLDecoder, URLEncoder }

import akka.Done
import javax.inject.Inject
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{
  Credentials,
  PasswordHasherRegistry
}
import com.mohiva.play.silhouette.api.{ LoginEvent, Silhouette }
import com.mohiva.play.silhouette.impl.exceptions.{
  IdentityNotFoundException,
  InvalidPasswordException
}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.applications.ApplicationsService
import org.hatdex.hat.api.service.{
  HatServicesService,
  LogService,
  MailTokenService,
  UsersService
}
import org.hatdex.hat.authentication._
import org.hatdex.hat.phata.models._
import org.hatdex.hat.resourceManagement.{ HatServerProvider, _ }
import org.hatdex.hat.utils.{
  ApplicationMailDetails,
  HatBodyParsers,
  HatMailer
}
import play.api.{ Configuration, Logger }
import play.api.cache.{ Cached, CachedBuilder }
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{ Action, _ }

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
    limiter: UserLimiter)
    extends HatApiController(components, silhouette)
    with HatJsonFormats {

  private val logger = Logger(this.getClass)

  private val indefiniteSuccessCaching: CachedBuilder = cached
    .status(req => s"${req.host}${req.path}", 200)
    .includeStatus(404, 600)

  // * Error Responses *
  // Extracted as these messages increased the length of functions.
  // So as a small effort to increase readability, I pulled them out.
  private def unauthorizedMessage(title: String, body: String) = 
    Unauthorized(Json.toJson(ErrorMessage(s"${title}", s"${body}")))    

  val noUserMatchingToken = 
    unauthorizedMessage("Password reset unauthorized", "No user matching token")

  val onlyHatOwnerCanReset = 
    unauthorizedMessage("Password reset unauthorized", "Only HAT owner can reset their password")

  val expiredToken = 
    unauthorizedMessage("Invalid Token", "Token expired or invalid")
            
  val invalidToken = 
    unauthorizedMessage("Invalid Token", "Token does not exist")

  val noClaimNoMatchingToken = 
    unauthorizedMessage("HAT claim unauthorized", "No user matching token")

  val noUserOrPass = 
    unauthorizedMessage("Credentials required", "No username or password provided to retrieve token")

  val claimProcessFailed = BadRequest(Json.toJson(ErrorMessage("Bad Request", "HAT claim process failed")))

  val iSEClaimFailed = InternalServerError(Json.toJson(ErrorMessage("Internal Server Error", "Failed to initialize HAT claim process")))
                        
          
  // * Ok Responses *
  private def okMessage(body: String) = Ok(Json.toJson(SuccessResponse(s"${body}")))  
  val hatIsAlreadyClaimed = 
    okMessage("The HAT is already claimed")

  val resendValidationEmail = 
    okMessage("If the email you have entered is correct and your HAT is not validated, you will receive an email with a link to validate your HAT.")



  def publicKey(): EssentialAction =
    indefiniteSuccessCaching {
      UserAwareAction.async { implicit request =>
        val publicKey =
          hatServerProvider.toString(request.dynamicEnvironment.publicKey)
        Future.successful(Ok(publicKey))
      }
    }

  // TODO: Should this remove tokens?
  def validateToken(): Action[AnyContent] =
    SecuredAction.async { _ =>
      Future.successful(Ok(Json.toJson(SuccessResponse("Authenticated"))))
    }

  def hatLogin(
      name: String,
      redirectUrl: String
    ): Action[AnyContent] =
    SecuredAction(WithRole(Owner())).async { implicit request =>
      for {
        service <- hatServicesService.findOrCreateHatService(name, redirectUrl)
        linkedService <- hatServicesService.hatServiceLink(
          request.identity,
          service,
          Some(redirectUrl)
        )
        _ <- usersService.logLogin(
          request.identity,
          "hatLogin",
          linkedService.category,
          Some(name),
          Some(redirectUrl)
        )
      } yield {
        Ok(Json.toJson(SuccessResponse(linkedService.url)))
      }
    }

  def applicationToken(
      name: String,
      resource: String
    ): Action[AnyContent] =
    SecuredAction(WithRole(Owner())).async { implicit request =>
      for {
        service <- hatServicesService.findOrCreateHatService(name, resource)
        token <- hatServicesService.hatServiceToken(request.identity, service)
      } yield {
        Ok(Json.toJson(token))
      }
    }

  // ???: this seems weird. :)
  private val hatService = HatService(
    "hat",
    "hat",
    "HAT API",
    "",
    "",
    "",
    browser = true,
    category = "api",
    setup = true,
    loginAvailable = true
  )

  // Trade username and password for an access_token
  def accessToken(): Action[AnyContent] =
    (UserAwareAction andThen limiter.UserAwareRateLimit).async {
      implicit request =>
        // pull details from the headers
        val eventuallyAuthenticatedUser = for {
          usernameParam <- request.headers.get("username")
          passwordParam <- request.headers.get("password")
        } yield {
          val username = URLDecoder.decode(usernameParam, "UTF-8")
          val password = URLDecoder.decode(passwordParam, "UTF-8")
          credentialsProvider
            .authenticate(Credentials(username, password))
            .map(_.copy(request.dynamicEnvironment.id))
            .flatMap { loginInfo =>
              usersService.getUser(loginInfo.providerKey).flatMap {
                // If we find a user, create and return an access token (JWT)
                case Some(user) => {
                  val customClaims = hatServicesService.generateUserTokenClaims(user, hatService)
                  for {
                    // JWT Authenticator
                    authenticator <- env.authenticatorService.create(loginInfo)
                    // JWT serialized as a String
                    token <- env.authenticatorService.init(
                      authenticator.copy(customClaims = Some(customClaims))
                    )
                    // Logging
                    _ <- usersService.logLogin(
                      user,
                      "api",
                      user.roles
                        .filter(_.extra.isEmpty)
                        .map(_.title)
                        .mkString(":"),
                      None,
                      None
                    )
                    // AuthenticatorResult
                    result <- env.authenticatorService.embed(
                      token,
                      Ok(Json.toJson(AccessToken(token, user.userId)))
                    )
                  } yield {
                    // ???: Learn about the event bus
                    env.eventBus.publish(LoginEvent(user, request))
                    // AuthenticatorResult
                    result
                  }
                }
                // No user found
                case None => {
                  Future.failed(new IdentityNotFoundException("Couldn't find user"))
                }
              }
            }
        }
        // Credentials are no good --> Error
        eventuallyAuthenticatedUser getOrElse {
          Future.successful(noUserOrPass)
        }
    }

  /**
    * This is the authenticated, "I have the password, but I want to change it."
    */
  def passwordChangeProcess: Action[ApiPasswordChange] =
    SecuredAction(WithRole(Owner())).async(parsers.json[ApiPasswordChange]) {
      implicit request =>
        logger.debug("Processing password change request")
        request.body.password map { oldPassword =>
          val eventualResult = for {
            _ <- credentialsProvider.authenticate(
              Credentials(request.identity.email, oldPassword)
            )
            _ <- authInfoRepository.update(
              request.identity.loginInfo,
              passwordHasherRegistry.current.hash(request.body.newPassword)
            )
            authenticator <-
              env.authenticatorService.create(request.identity.loginInfo)
            result <- env.authenticatorService.renew(
              authenticator,
              Ok(Json.toJson(SuccessResponse("Password changed")))
            )
          } yield {
            env.eventBus.publish(LoginEvent(request.identity, request))
            mailer.passwordChanged(request.identity.email, request.identity)
            result
          }

          eventualResult recover {
            case _: InvalidPasswordException =>
              Forbidden(
                Json.toJson(
                  ErrorMessage("Invalid password", "Old password invalid")
                )
              )
          }
        } getOrElse {
          Future.successful(
            Forbidden(
              Json.toJson(
                ErrorMessage("Invalid password", "Old password missing")
              )
            )
          )
        }
    }

  /**
    * Sends an email to the user with a link to reset the password
    * This is unauthenticated.
    */
  def handleForgotPassword: Action[ApiPasswordResetRequest] =
    UserAwareAction.async(parsers.json[ApiPasswordResetRequest]) {
      implicit request =>
        logger.debug("Processing forgotten password request")
        val email = request.body.email
        val response = Ok(
          Json.toJson(
            SuccessResponse(
              "If the email you have entered is correct, you will shortly receive an email with password reset instructions"
            )
          )
        )
        if (email == request.dynamicEnvironment.ownerEmail) {
          // Find the specific user who is the owner.
          usersService.listUsers
            .map(_.find(_.roles.contains(Owner())))
            .flatMap {
              case Some(user) => {
                // Create a token for the reset with a 24 hour expiry
                // isSignUp is potentially the issue here.
                val token = MailTokenUser(email, isSignUp = false)
                // Store that token
                tokenService.create(token).map { _ =>
                  val scheme = if (request.secure) "https://" else "http://"
                  val resetLink = s"$scheme${request.host}/#/user/password/change/${token.id}"
                  mailer.passwordReset(email, user, resetLink)
                  response
                }
              }
              // The user was not found, but return the "If we found an email address, we'll send the link."
              case None => Future.successful(response)
            }
        } else {
          Future.successful(response)
        }
    }


  /**
    * Saves the new password and authenticates the user
    */
  def handleResetPassword(tokenId: String): Action[ApiPasswordChange] =
    UserAwareAction.async(parsers.json[ApiPasswordChange]) { implicit request =>
      tokenService.retrieve(tokenId).flatMap {
        // Token was found, is not signup nor expired
        case Some(token) if !token.isSignUp && !token.isExpired => {
          // Token.email matches the dynamicEnv (what is this)
          if (token.email == request.dynamicEnvironment.ownerEmail) {
            // Find the users with the owner role
            // ???: Why not using the email
            usersService.listUsers
              .map(_.find(_.roles.contains(Owner())))
              .flatMap {
                case Some(user) =>
                  for {
                    // ???: authInfoRepo
                    // Update with the new password info
                    _ <- authInfoRepository.update(
                      user.loginInfo,
                      passwordHasherRegistry.current
                        .hash(request.body.newPassword)
                    )
                    // ???: Get the authenticator - JWT maker
                    authenticator <-
                      env.authenticatorService.create(user.loginInfo)
                    result <- env.authenticatorService.renew(
                      authenticator,
                      Ok(Json.toJson(SuccessResponse("Password reset")))
                    )
                  } yield {
                    // Delete the token
                    tokenService.consume(tokenId)
                    // ???: I must know more about the EventBus, for stats and logs
                    // Push a loginEvent on the bus
                    env.eventBus.publish(LoginEvent(user, request))
                    // Mail the user, telling them the password changed
                    mailer.passwordChanged(token.email, user)
                    // ???: return an AuthenticatorResult, why
                    result
                  }
                case None =>
                  Future.successful(noUserMatchingToken)
              }
          } else {
            Future.successful(onlyHatOwnerCanReset)
          }
        }
        case Some(_) =>
          tokenService.consume(tokenId)
          Future.successful(expiredToken)
        case None =>
          Future.successful(invalidToken)
      }
    }

  /**
    * Sends an email to the owner with a link to claim the hat
    * /control/v2/auth/claim
    */
  def handleClaimStart(lang: Option[String]): Action[ApiClaimHatRequest] =
    UserAwareAction.async(parsers.json[ApiClaimHatRequest]) {
      implicit request =>
        
        implicit val language: Lang = Lang.get(lang.getOrElse("en")).getOrElse(Lang.defaultLang)

        val claimHatRequest = request.body
        // ???: Not sure how this is hydrated.
        val email = request.dynamicEnvironment.ownerEmail
        val response = Ok(Json.toJson(SuccessResponse("You will shortly receive an email with claim instructions")))
        val scheme = if (request.secure) "https://" else "http://"

        // (email, applicationId) in the body
        // Match the mail to that of the ENV
        // Look up the application (Is this in the HAT itself?  Not DEX)
        if (claimHatRequest.email == email) {
          usersService.listUsers
            .map(_.find(_.roles.contains(Owner())))
            .flatMap {
              case Some(user) =>
                applicationsService
                  .applicationStatus()(
                    request.dynamicEnvironment,
                    user,
                    request
                  )
                  .flatMap { applications =>
                    val maybeApplication = applications.find(
                      _.application.id.equals(claimHatRequest.applicationId)
                    )

                    val appMailDetails: Option[ApplicationMailDetails] =
                      maybeApplication.map { app =>
                        ApplicationMailDetails(
                          app.application.info.name,
                          app.application.info.graphics.logo.normal,
                          app.application.info.url
                        )
                      }

                    val appLogDetails: Option[(String, String)] =
                      maybeApplication.map { app =>
                        (
                          app.application.id,
                          app.application.info.version.toString
                        )
                      }

                    tokenService.retrieve(email, isSignup = true).flatMap {
                      case Some(existingTokenUser)
                          if !existingTokenUser.isExpired =>
                        val claimLink =
                          s"$scheme${request.host}/hat/claim/${existingTokenUser.id}?email=${URLEncoder
                            .encode(email, "UTF-8")}"
                        mailer.claimHat(email, claimLink, appMailDetails)

                        Future.successful(response)
                      case Some(_) =>
                        Future.successful(hatIsAlreadyClaimed)

                      case None =>
                        val token = MailClaimTokenUser(email)

                        val eventualResult = for {
                          _ <- tokenService.create(token)
                          _ <-
                            logService
                              .logAction(
                                request.dynamicEnvironment.domain,
                                LogRequest("unclaimed", None, None),
                                appLogDetails
                              )
                              .recover {
                                case e =>
                                  logger.error(
                                    s"LogActionError::unclaimed. Reason: ${e.getMessage}"
                                  )
                                  Done
                              }
                        } yield {
                          val claimLink =
                            s"$scheme${request.host}/hat/claim/${token.id}?email=${URLEncoder
                              .encode(email, "UTF-8")}"
                          mailer.claimHat(email, claimLink, appMailDetails)

                          response
                        }

                        eventualResult.recover {
                          case e =>
                            logger.error(
                              s"Could not create new HAT claim token. Reason: ${e.getMessage}"
                            )
                            iSEClaimFailed
                        }
                    }
                  }
              case None => Future.successful(response)
            }
        } else {
          Future.successful(response)
        }
    }

  def handleClaimComplete(claimToken: String): Action[HatClaimCompleteRequest] =
    UserAwareAction.async(parsers.json[HatClaimCompleteRequest]) {
      implicit request =>
        implicit val hatClaimComplete: HatClaimCompleteRequest = request.body

        tokenService.retrieve(claimToken).flatMap {
          case Some(token)
              if token.isSignUp && !token.isExpired && token.email == request.dynamicEnvironment.ownerEmail =>
            usersService.listUsers
              .map(_.find(_.roles.contains(Owner())))
              .flatMap {
                case Some(user) =>
                  val eventualResult = for {
                    _ <- updateHatMembership(hatClaimComplete)
                    _ <- authInfoRepository.update(
                      user.loginInfo,
                      passwordHasherRegistry.current.hash(request.body.password)
                    )
                    _ <- tokenService.expire(token.id)
                    authenticator <-
                      env.authenticatorService.create(user.loginInfo)
                    result <- env.authenticatorService.renew(
                      authenticator,
                      Ok(Json.toJson(SuccessResponse("HAT claimed")))
                    )
                    _ <-
                      logService
                        .logAction(
                          request.dynamicEnvironment.domain,
                          LogRequest("claimed", None, None),
                          None
                        )
                        .recover {
                          case e =>
                            logger.error(
                              s"LogActionError::unclaimed. Reason: ${e.getMessage}"
                            )
                            Done
                        }
                  } yield {
                    // ???: this is fishy
                    //env.eventBus.publish(LoginEvent(user, request))
                    //mailer.passwordChanged(token.email, user)

                    result
                  }

                  eventualResult.recover {
                    case e =>
                      logger.error(s"HAT claim process failed with error ${e.getMessage}")
                      claimProcessFailed
                  }

                case None =>
                  Future.successful(noClaimNoMatchingToken)
              }

          case Some(_) =>
            Future.successful(expiredToken)

          case None =>
            Future.successful(invalidToken)
        }
    }

  private def updateHatMembership(
      claim: HatClaimCompleteRequest
    ): Future[Done] = {
    val path = "api/services/daas/claim"
    val hattersUrl =
      s"${configuration.underlying.getString("hatters.scheme")}${configuration.underlying.getString("hatters.address")}"
    val hattersClaimPayload = HattersClaimPayload(claim)

    logger.info(s"Proxy POST request to $hattersUrl/$path with parameters: $claim")

    // Tell Hatters this HAT is claimed
    val futureResponse = wsClient
      .url(s"$hattersUrl/$path")
      //.withHttpHeaders("x-auth-token" -> token.accessToken)
      .post(Json.toJson(hattersClaimPayload))

    futureResponse.flatMap { response =>
      response.status match {
        case OK =>
          Future.successful(Done)
        case _ =>
          logger.error(
            s"Failed to claim HAT with Hatters. Claim details: $claim Hatters response: ${response.body}"
          )
          Future.failed(new UnknownError("HAT claim failed"))
      }
    }
  }



    /**
    * Resends the email to claim/validate the HAT
    * This is unauthenticated request
    */
  def handleRevalidation: Action[ApiValidationRequest] =
    UserAwareAction.async(parsers.json[ApiValidationRequest]) {
      implicit request =>
        logger.debug("Processing resend validation request")

        val email = request.body.email
        val applicationId = request.body.applicationId
        val isSigupToken = true

        // Generic message regardless if the user is found or not.
        val uniformResponse = resendValidationEmail

        tokenService.retrieve(email, isSigupToken).flatMap {
          // There is a token that is a signUp Token, meaning the hat is not claimed.
          // I don't think it matters if it has expired.
          case Some(token@_) => {
            // Match the email from the request and the Silhouette Env
            if (email == request.dynamicEnvironment.ownerEmail) {
              // Find the specific user who is the owner, only owners can resend
              usersService.listUsers.map(_.find(_.roles.contains(Owner()))).flatMap {
                case Some(user) => {
                  sendRevalidationEmail(user.email, applicationId, request.host, request.lang)
                  Future.successful(uniformResponse)
                }
                // The user is not an owner, but return the "If we found an email address, we'll send the link."
                case None => {
                  Future.successful(uniformResponse)
                }
              }
            } else {
              // Email in the request does not match the dynamic env.
              Future.successful(uniformResponse)
            } 
          }
          case None => {
            // User does not have an existing isSignup Token
            Future.successful(uniformResponse)
          }
      }
    }

  // applicationId is currently unused
  def sendRevalidationEmail(email: String, applicationId: String, requestHost: String, lang: Lang)(implicit hatServer: HatServer): Future[Done] = { 
    implicit val l = lang
    // Create a signup token
    val token = MailTokenUser(email, isSignUp = true)
    // Store that token
    tokenService.create(token).map { _ =>
      // Assume https now
      val claimLink = s"https://${requestHost}/hat/claim/${token.id}?email=${URLEncoder.encode(email, "UTF-8")}"
      mailer.claimHat(email, claimLink, None)
    }
  }

}
