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
package hatdex.hat.authentication.authenticators

import hatdex.hat.authentication.models.User

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import hatdex.hat.authentication._
import spray.routing.HttpService._
import spray.routing._

object UserPassHandler {

  case class UserPassAuthenticator(val keys: List[String] = defaultKeys,
                                   val authenticator: Map[String, String] => Future[Option[User]] = defaultAuthenticator)
    extends RestAuthenticator[User] {

    def apply(): Directive1[User] = authenticate(this)
  }

  val defaultKeys = List("username", "password")
  val defaultAuthenticator = authFunction _

  def authFunction(params: Map[String, String]): Future[Option[User]] = Future {
    val emailOpt = params.get(defaultKeys(0))
    val passwordOpt = params.get(defaultKeys(1))

    val mayBeUser = for {
      email <- emailOpt
      password <- passwordOpt
      user <- {
        //get user form database , replace None with proper method once database service is ready.
        //getUserByCredential(email, password)
        None
      }
    } yield user
    mayBeUser
  }
}