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

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.util.{ Clock, Credentials, PasswordHasherRegistry }
import com.mohiva.play.silhouette.api.{ LoginEvent, Silhouette }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.{ HatServicesService, UsersService }
import org.hatdex.hat.authentication.models.{ HatUser, Owner, Platform, UserRole }
import org.hatdex.hat.authentication.{ HatApiController, WithRole, _ }
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.resourceManagement._
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.Future

class Users @Inject() (
    val messagesApi: MessagesApi,
    passwordHasherRegistry: PasswordHasherRegistry,
    configuration: Configuration,
    credentialsProvider: CredentialsProvider[HatServer],
    silhouette: Silhouette[HatApiAuthEnvironment],
    hatServerProvider: HatServerProvider,
    clock: Clock,
    hatServicesService: HatServicesService,
    usersService: UsersService) extends HatApiController(silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  val logger = Logger(this.getClass)

  def listUsers(): Action[AnyContent] = SecuredAction.async { implicit request =>
    logger.warn(s"Requesting users for ${request.host} ${request.identity}, ${request.dynamicEnvironment}")
    usersService.listUsers map { users =>
      Ok(Json.toJson(users.map(ModelTranslation.fromInternalModel)))
    }

  }

  def createUser(): Action[User] = SecuredAction(WithRole(Owner(), Platform())).async(BodyParsers.parse.json[User]) { implicit request =>
    val user = request.body
    val permittedRoles = Seq("dataDebit", "dataCredit")
    if (permittedRoles.contains(user.role)) {
      usersService.getUser(user.email).flatMap { maybeExistingUser =>
        maybeExistingUser map {
          case _ =>
            Future.successful(BadRequest(Json.toJson(ErrorMessage("Error creating user", s"User ${user.email} already exists"))))
        } getOrElse {
          val hatUser = HatUser(user.userId, user.email, user.pass, user.name, Seq(UserRole.userRoleDeserialize(user.role, None, true)._1), enabled = true)
          usersService.saveUser(hatUser).map { created =>
            Created(Json.toJson(ModelTranslation.fromInternalModel(created)))
          } recover {
            case e =>
              BadRequest(Json.toJson(ErrorMessage("Error creating user", s"User not created: ${e.getMessage}")))
          }
        }
      }

    }
    else {
      Future.successful(BadRequest(Json.toJson(ErrorMessage("Invalid User", s"Only users with certain roles can be created: ${permittedRoles.mkString(",")}"))))
    }
  }

  def deleteUser(userId: UUID): Action[AnyContent] = SecuredAction(WithRole(Owner(), Platform())).async { implicit request =>
    usersService.deleteUser(userId) map { _ =>
      Ok(Json.toJson(SuccessResponse(s"Account deleted")))
    } recover {
      case e =>
        BadRequest(Json.toJson(ErrorMessage("Error deleting account", e.getMessage)))
    }
  }

  def publicKey(): Action[AnyContent] = UserAwareAction.async { implicit request =>
    val publicKey = hatServerProvider.toString(request.dynamicEnvironment.publicKey)
    Future.successful(Ok(publicKey))
  }

  def validateToken(): Action[AnyContent] = SecuredAction.async { implicit request =>
    Future.successful(Ok(Json.toJson(SuccessResponse("Authenticated"))))
  }

  private val hatService = HatService(
    "hat", "hat", "HAT API",
    "", "", "",
    browser = true,
    category = "api",
    setup = true,
    loginAvailable = true)
  def accessToken(): Action[AnyContent] = UserAwareAction.async { implicit request =>
    val eventuallyAuthenticatedUser = for {
      username <- request.getQueryString("username").orElse(request.headers.get("username"))
      password <- request.getQueryString("password").orElse(request.headers.get("password"))
    } yield {
      logger.info(s"Authenticating $username:$password")
      credentialsProvider.authenticate(Credentials(username, password))
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

  def applicationToken(name: String, resource: String): Action[AnyContent] = SecuredAction(WithRole(Owner())).async { implicit request =>
    for {
      service <- hatServicesService.findOrCreateHatService(name, resource)
      token <- hatServicesService.hatServiceToken(request.identity, service)
      result <- env.authenticatorService.embed(token.accessToken, Ok(Json.toJson(token)))
    } yield {
      result
    }
  }

  def enableUser(userId: UUID): Action[AnyContent] = SecuredAction(WithRole(Owner(), Platform())).async { implicit request =>
    implicit val db = request.dynamicEnvironment.asInstanceOf[HatServer].db
    usersService.getUser(userId) flatMap { maybeUser =>
      maybeUser map { user =>
        if (user.roles.intersect(Seq(Owner(), Platform())).nonEmpty) {
          Future.successful(Forbidden(Json.toJson(ErrorMessage("Forbidden", s"Owner account can not be enabled or disabled"))))
        }
        else {
          usersService.changeUserState(userId, enabled = true).map { _ => Ok(Json.toJson(SuccessResponse("Enabled"))) }
        }
      } getOrElse {
        Future.successful(NotFound(Json.toJson(ErrorMessage("No such User", s"User $userId does not exist"))))
      }
    }
  }

  def disableUser(userId: UUID): Action[AnyContent] = SecuredAction(WithRole(Owner(), Platform())).async { implicit request =>
    implicit val db = request.dynamicEnvironment.asInstanceOf[HatServer].db
    usersService.getUser(userId) flatMap { maybeUser =>
      maybeUser map { user =>
        if (user.roles.intersect(Seq(Owner(), Platform())).nonEmpty) {
          Future.successful(Forbidden(Json.toJson(ErrorMessage("Forbidden", s"Owner account can not be enabled or disabled"))))
        }
        else {
          usersService.changeUserState(userId, enabled = true).map { _ => Ok(Json.toJson(SuccessResponse("Enabled"))) }
        }
      } getOrElse {
        Future.successful(NotFound(Json.toJson(ErrorMessage("No such User", s"User $userId does not exist"))))
      }
    }
  }

}
