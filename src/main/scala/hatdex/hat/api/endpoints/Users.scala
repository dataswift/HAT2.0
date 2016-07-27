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
package hatdex.hat.api.endpoints

import java.util.UUID

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models.{ SuccessResponse, ApiError, ErrorMessage }
import hatdex.hat.authentication.{ JwtTokenHandler, HatServiceAuthHandler }
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.{ AccessToken, User }
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.Duration._

import org.joda.time.{ LocalDateTime }
import spray.http.StatusCode
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._
import spray.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success, Try }

trait Users extends HttpService with HatServiceAuthHandler with JwtTokenHandler {

  val db = DatabaseInfo.db

  val logger: LoggingAdapter

  val routes = {
    pathPrefix("users") {
      createApiUserAccount ~ getAccessToken ~ enableUserAccount ~ suspendUserAccount ~ validateAccessToken
    }
  }

  import hatdex.hat.api.json.JsonProtocol._

  def createApiUserAccount = path("user") {
    accessTokenHandler { implicit systemUser: User =>
      authorize(UserAuthorization.withRole("owner", "platform")) {
        post {
          entity(as[User]) { implicit newUser =>
            // Only two types of users can be created via the api
            val maybeUserRole = Future {
              newUser.role match {
                case "dataDebit"  => Some("dataDebit")
                case "dataCredit" => Some("dataCredit")
                case _            => None
              }
            }

            val fUser = maybeUserRole flatMap {
              case Some(userRole) =>
                val newUserDb = UserUserRow(newUser.userId, LocalDateTime.now(), LocalDateTime.now(),
                  newUser.email, newUser.pass, // The password is assumed to come in hashed, hence stored as is!
                  newUser.name, userRole, enabled = true)
                val createdUser = db.run {
                  ((UserUser returning UserUser) += newUserDb).asTry
                }

                createdUser map {
                  case Success(userDb) => (Created, User(userDb.userId, userDb.email, None, userDb.name, userDb.role))
                  case Failure(e)      => throw ApiError(BadRequest, ErrorMessage("Error when creating user", e.getMessage))
                }
              case None => throw ApiError(Unauthorized, ErrorMessage("Invalid User Role", "Only HAT dataDebit and dataCredit roles can be created via this API"))
            }

            onComplete(fUser) {
              case Success((statusCode, value)) => complete((statusCode, value))
              case Failure(e: ApiError)         => complete((e.statusCode, e.message))
              case Failure(e)                   => complete((InternalServerError, ErrorMessage("Error while creating user", "Unknown error occurred")))
            }
          }
        }
      }
    }
  }

  def suspendUserAccount = path("user" / JavaUUID / "disable") { userId: UUID =>
    accessTokenHandler { implicit systemUser: User =>
      authorize(UserAuthorization.withRole("owner", "platform")) {
        put {
          val temp = UserUser.filter(_.userId === userId)
            .map(u => (u.enabled)) //            .map(u => (u.enabled, u.lastUpdated))
            .update((false)) //.update((false, LocalDateTime.now()))

          onComplete(db.run(temp.asTry)) {
            case Success(_) => complete((OK, SuccessResponse("Account suspended")))
            case Failure(e) => complete((BadRequest, ErrorMessage("Error suspending account", "Not Suspended")))
          }
        }
      }
    }
  }

  def enableUserAccount = path("user" / JavaUUID / "enable") { userId: UUID =>
    accessTokenHandler { implicit systemUser: User =>
      authorize(UserAuthorization.withRole("owner", "platform")) {
        put {
          val temp = UserUser.filter(_.userId === userId)
            .map(u => (u.enabled)) //            .map(u => (u.enabled, u.lastUpdated))
            .update((true)) //.update((false, LocalDateTime.now()))

          onComplete(db.run(temp.asTry)) {
            case Success(_) => complete((OK, SuccessResponse("Account enabled")))
            case Failure(e) => complete((BadRequest, ErrorMessage("Error enabling account", "Not Enabled")))
          }
        }
      }
    }
  }

  def getAccessToken = path("access_token") {
    // Any password-authenticated user (not only owner)
    userPassHandler { implicit user: User =>
      get {
        val response = fetchOrGenerateToken(user, issuer, accessScope = user.role, validity = standardHours(2))
        onComplete(response) {
          case Success(value) => complete((OK, value))
          case Failure(e: ApiError) =>
            logger.error(s"API Error while fetching Access Token: ${e.message}")
            complete((e.statusCode, e.message))
          case Failure(e) =>
            logger.error(s"Unexpected Error while fetching Access Token: ${e.getMessage}")
            complete((InternalServerError, ErrorMessage("Error while retrieving access token", "Unknown error occurred")))
        }
      }
    }
  }

  def validateAccessToken = path("access_token" / "validate") {
    get {
      accessTokenHandler { implicit systemUser: User =>
        complete((OK, SuccessResponse("Authenticated")))
      }
    }
  }
}

