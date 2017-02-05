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

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.resourceManagement.HatServer

import scala.concurrent.Future

/**
 * Handles actions to users.
 *
 * @param usersService The underlying database User Service implementation
 */
class AuthUserServiceImpl @Inject() (usersService: UsersService) extends AuthUserService {
  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo)(implicit dyn: HatServer): Future[Option[HatUser]] = {
    usersService.getUser(loginInfo.providerKey)(dyn.db)
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: HatUser)(implicit dyn: HatServer) = usersService.saveUser(user)(dyn.db)

  /**
   * Link user profiles together
   *
   * @param mainUser The user to link to.
   * @param mainUser The linked user
   */
  def link(mainUser: HatUser, linkedUser: HatUser)(implicit dyn: HatServer) = Future.failed(new RuntimeException("Profile linking not implemented"))
}