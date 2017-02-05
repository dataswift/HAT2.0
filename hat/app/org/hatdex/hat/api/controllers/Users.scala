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
import javax.inject.{ Inject, Named }

import akka.actor.ActorRef
import com.mohiva.play.silhouette.api.{ LoginEvent, Silhouette }
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials, PasswordHasherRegistry }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models.{ ErrorMessage, SuccessResponse, User }
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.api.models.AccessToken
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.authentication.{ HatApiController, _ }
import org.hatdex.hat.dal.ModelTranslation
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{ JsObject, Json }
import play.api.{ Configuration, Logger }
import org.hatdex.hat.authentication.WithRole
import org.hatdex.hat.resourceManagement._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._

class Users @Inject() (
    val messagesApi: MessagesApi,
    passwordHasherRegistry: PasswordHasherRegistry,
    configuration: Configuration,
    credentialsProvider: CredentialsProvider[HatServer],
    silhouette: Silhouette[HatApiAuthEnvironment],
    hatServerProvider: HatServerProvider,
    clock: Clock,
    usersService: UsersService) extends HatApiController(silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  val logger = Logger(this.getClass)

  def listUsers(): Action[AnyContent] = SecuredAction.async { implicit request =>
    logger.warn(s"Requesting users for ${request.host} ${request.identity}, ${request.dynamicEnvironment}")
    usersService.listUsers map { users =>
      Ok(Json.toJson(users.map(ModelTranslation.fromInternalModel)))
    }

  }

  def createUser(): Action[User] = SecuredAction(WithRole("owner", "platform")).async(BodyParsers.parse.json[User]) { implicit request =>
    val user = request.body
    val permittedRoles = Seq("dataDebit", "dataCredit")
    if (permittedRoles.contains(user.role)) {
      usersService.getUser(user.email).flatMap { maybeExistingUser =>
        maybeExistingUser map {
          case _ =>
            Future.successful(BadRequest(Json.toJson(ErrorMessage("Error creating user", s"User ${user.email} already exists"))))
        } getOrElse {
          val hatUser = HatUser(user.userId, user.email, user.pass, user.name, user.role, enabled = true)
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

  def deleteUser(userId: UUID): Action[AnyContent] = SecuredAction(WithRole("owner", "platform")).async { implicit request =>
    usersService.deleteUser(userId) map { _ =>
      Ok(Json.toJson(SuccessResponse(s"Account deleted")))
    } recover {
      case e =>
        BadRequest(Json.toJson(ErrorMessage("Error deleting account", e.getMessage)))
    }
  }

  def publicKey(): Action[AnyContent] = UserAwareAction.async { implicit request =>
    val publicKey = hatServerProvider.toString(request.dynamicEnvironment.asInstanceOf[HatServer].publicKey)
    Future.successful(Ok(publicKey))
  }

  def validateToken(): Action[AnyContent] = SecuredAction.async { implicit request =>
    Future.successful(Ok(Json.toJson(SuccessResponse("Authenticated"))))
  }

  def applicationToken(name: String, resource: String): Action[AnyContent] = SecuredAction(WithRole("owner")).async { implicit request =>
    val customClaims = Map("resource" -> Json.toJson(resource), "accessScope" -> Json.toJson("validate"))
    for {
      authenticator <- env.authenticatorService.create(request.identity.loginInfo)
      token <- env.authenticatorService.init(authenticator.copy(customClaims = Some(JsObject(customClaims))))
      result <- env.authenticatorService.embed(token, Ok(Json.toJson(AccessToken(token, request.identity.userId))))
    } yield {
      result
    }
  }

  def accessToken(): Action[AnyContent] = UserAwareAction.async { implicit request =>
    val eventuallyAuthenticatedUser = for {
      username <- request.getQueryString("username").orElse(request.headers.get("username"))
      password <- request.getQueryString("password").orElse(request.headers.get("password"))
    } yield {
      Logger("org.hatdex.hat.authentication").info(s"Authenticating $username:$password")
      credentialsProvider.authenticate(Credentials(username, password))
        .flatMap { loginInfo =>
          usersService.getUser(loginInfo.providerKey).flatMap {
            case Some(user) =>
              // FIXME: resource asd??
              val customClaims = Map("resource" -> Json.toJson(request.dynamicEnvironment.domain), "accessScope" -> Json.toJson(user.role))
              for {
                authenticator <- env.authenticatorService.create(loginInfo)
                token <- env.authenticatorService.init(authenticator.copy(customClaims = Some(JsObject(customClaims))))
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

  def enableUser(userId: UUID): Action[AnyContent] = SecuredAction(WithRole("owner", "platform")).async { implicit request =>
    implicit val db = request.dynamicEnvironment.asInstanceOf[HatServer].db
    usersService.getUser(userId) flatMap { maybeUser =>
      maybeUser map { user =>
        user.role match {
          case "owner"    => Future.successful(Forbidden(Json.toJson(ErrorMessage("Forbidden", s"Owner account can not be enabled or disabled"))))
          case "platform" => Future.successful(Forbidden(Json.toJson(ErrorMessage("Forbidden", s"Platform account can not be enabled or disabled"))))
          case _          => usersService.changeUserState(userId, enabled = true).map { case _ => Ok(Json.toJson(SuccessResponse("Enabled"))) }
        }
      } getOrElse {
        Future.successful(NotFound(Json.toJson(ErrorMessage("No such User", s"User $userId does not exist"))))
      }
    }
  }

  def disableUser(userId: UUID): Action[AnyContent] = SecuredAction(WithRole("owner", "platform")).async { implicit request =>
    implicit val db = request.dynamicEnvironment.asInstanceOf[HatServer].db
    usersService.getUser(userId) flatMap { maybeUser =>
      maybeUser map { user =>
        user.role match {
          case "owner"    => Future.successful(Forbidden(Json.toJson(ErrorMessage("Forbidden", s"Owner account can not be enabled or disabled"))))
          case "platform" => Future.successful(Forbidden(Json.toJson(ErrorMessage("Forbidden", s"Platform account can not be enabled or disabled"))))
          case _          => usersService.changeUserState(userId, enabled = false).map { case _ => Ok(Json.toJson(SuccessResponse("Enabled"))) }
        }
      } getOrElse {
        Future.successful(NotFound(Json.toJson(ErrorMessage("No such User", s"User $userId does not exist"))))
      }
    }
  }

}
