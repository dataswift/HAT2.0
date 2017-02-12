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

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.User
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}
import com.mohiva.play.silhouette.test._
import org.hatdex.hat.api.controllers.Users
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser

class UserSpec extends PlaySpecification {

  "The `user` method" should {
    "return status 401 if authenticator but no identity was found" in new WithApplication {
      val identity = HatUser(UUID.randomUUID(), "user@hat.org", Some("pa55w0rd"), "hatuser", "owner", true)
      implicit val env = FakeEnvironment[HatApiAuthEnvironment](Seq(identity.loginInfo -> identity))
      val request = FakeRequest()
        .withAuthenticator(LoginInfo("xing", "comedian@watchmen.com"))

      val controller = app.injector.instanceOf[Users]
      val result = controller.accessToken(request)

      status(result) must equalTo(UNAUTHORIZED)
    }
  }

}