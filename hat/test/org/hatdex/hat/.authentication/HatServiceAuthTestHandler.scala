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

package org.hatdex.hat.authentication

import java.util.UUID

import org.hatdex.hat.authentication.models.{AccessToken, HatUser}
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object HatAuthTestHandler extends TestData {

  object AccessTokenHandler {
    val authenticator = authFunction _

    private def authFunction(params: Map[String, String]) = Future {
//            println (s"### Running test access_token authenticator. Params: $params")
      val mayBeToken = params.get("x-auth-token").orElse(params.get("X-Auth-Token"))
      mayBeToken flatMap { token =>
        validAccessTokens.find(_.accessToken equals token).map(_.userId) flatMap { userId =>
          validUsers.find(_.userId equals userId)
        }
      }
    }
  }

  object UserPassHandler {
    val authenticator = authFunction _

    private def authFunction(params: Map[String, String]) = Future {
      val emailOpt = params.get("username")
      val passwordOpt = params.get("password")
      //      println ("### Running test user_pass authenticator")
      val mayBeUser = for {
        email <- emailOpt
        password <- passwordOpt
        user <- validUsers.find(x =>
          // Only HAT Owner allowed to authenticate as a user
          (x.email equals email) && BCrypt.checkpw(password, x.pass.getOrElse("")) && (x.role equals "owner")
        )
      } yield user
      mayBeUser
    }
  }

}

trait TestData {
  // Following are the hard coded values
  val validUsers = Seq(
    HatUser(UUID.fromString("afc5a06f-c351-4e7e-8755-765770c56bb6"), "bob@gmail.com", Some(BCrypt.hashpw("pa55w0rd", BCrypt.gensalt())), "Test User", "owner"),
    HatUser(UUID.fromString("e1e8e1b7-30e2-4cd2-8407-e5321bf0e9ee"), "alice@gmail.com", Some(BCrypt.hashpw("dr0w55ap", BCrypt.gensalt())), "Test Debit User", "dataDebit"),
    HatUser(UUID.fromString("08867a9c-4969-481f-8830-01d73ac45068"), "carol@gmail.com", Some(BCrypt.hashpw("p4ssWOrD", BCrypt.gensalt())), "Test Credit User", "dataCredit"),
    HatUser(UUID.fromString("860774bc-99f9-4238-b661-c3492e95d800"), "platform@platform.com", Some(BCrypt.hashpw("p4ssWOrD", BCrypt.gensalt())), "Platform User", "platform")
  )

  val validAccessTokens = Seq(
    AccessToken("df4545665drgdfg", validUsers.find(_.email equals "bob@gmail.com").get.userId),
    AccessToken("df4545665drgdff", validUsers.find(_.email equals "alice@gmail.com").get.userId),
    AccessToken("df4545665drgdfh", validUsers.find(_.email equals "carol@gmail.com").get.userId),
    AccessToken("df4545665drgdfh", validUsers.find(_.email equals "platform@platform.com").get.userId)
  )
}