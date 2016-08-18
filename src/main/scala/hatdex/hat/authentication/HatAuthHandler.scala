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
package hatdex.hat.authentication

import com.typesafe.config.ConfigFactory
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.authentication.models.{AccessToken, User}
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._

/**
 * Here we need to make authenticate function of AuthHandlers
 */

object HatAuthHandler {

  object AccessTokenHandler extends JwtTokenHandler {
    val authenticator = authFunction _
    private def authFunction(params: Map[String, String]): Future[Option[User]] = {
      val mayBeToken = Future {
        params.get("x-auth-token").orElse(params.get("X-Auth-Token"))
      }

      mayBeToken flatMap {
        case Some(token: String) if validateJwtToken(token) && verifyResource(token, issuer) =>
          val userQuery = UserAccessToken.filter(_.accessToken === token).flatMap(_.userUserFk).filter(_.enabled === true).take(1)
          val matchingUsers = DatabaseInfo.db.run(userQuery.result)
          val maybeRole = getTokenAccessScope(token)
          matchingUsers.map { users =>
            users.headOption
              .map(User.fromDbModel)
              .map(_.copy(role = maybeRole.getOrElse("")))
          }
        case Some(_) =>
          // Invalid token
          Future.successful(None)
        case None =>
          Future.successful(None)
      }
    }
  }

  // Normally only allowing the owner to authenticate with password
  object UserPassHandler {
    val authenticator = authFunction _
    private def authFunction(params: Map[String, String]): Future[Option[User]] = {
      val emailOpt = params.get("username")
      val passwordOpt = params.get("password")

      val maybeCredentials = Future {
        for {
          email <- emailOpt
          password <- passwordOpt
        } yield (email, password)
      }

      maybeCredentials flatMap {
        case Some((email, password)) =>
          val userQuery = UserUser.filter(_.email === email).filter(_.enabled === true).take(1)
          val matchingUsers = DatabaseInfo.db.run(userQuery.result)
          matchingUsers.map { users =>
            users.headOption
              .filter(user => BCrypt.checkpw(password, user.pass.getOrElse("")))
              .map(User.fromDbModel)
          }
        case None =>
          Future.successful(None)
      }
    }
  }
}