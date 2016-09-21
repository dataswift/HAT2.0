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

import akka.event.LoggingAdapter
import hatdex.hat.api.actors.{EmailService, SmtpConfig}
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.testkit.Specs2RouteTest

class HelloSpec extends Specification with Specs2RouteTest with Hello {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  val smtpConfig = SmtpConfig(conf.getBoolean("mail.smtp.tls"),
    conf.getBoolean("mail.smtp.ssl"),
    conf.getInt("mail.smtp.port"),
    conf.getString("mail.smtp.host"),
    conf.getString("mail.smtp.username"),
    conf.getString("mail.smtp.password"))
  val apiEmailService = new EmailService(system, smtpConfig)

  val emailService = apiEmailService

  val ownerAuthToken = HatAuthTestHandler.validUsers.find(_.role == "owner").map(_.userId).flatMap { ownerId =>
    HatAuthTestHandler.validAccessTokens.find(_.userId == ownerId).map(_.accessToken)
  } getOrElse ("")
  val ownerAuthHeader = RawHeader("X-Auth-Token", ownerAuthToken)

  sequential

  "Hello Service" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> sealRoute(routes) ~>
        check {
          responseAs[String] must contain("Welcome to the HAT")
        }
    }

    "disallow GET requests to hat path without credentials" in {
      Get("/hat") ~> sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Unauthorized
          }
        }
    }

    "disallow GET requests to hat path with incorrect credentials" in {
      Get("/hat")
        .withHeaders(RawHeader("X-Auth-Token", "asdasd")) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Unauthorized
          }
        }
    }

    "accept access_token authenticated GET requests to hat path" in {
      Get("/hat").withHeaders(ownerAuthHeader) ~> sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("HAT - Welcome back Test User")
          }
        }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> routes ~>
        check {
          handled must beFalse
        }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(routes) ~>
        check {
          response.status should be equalTo MethodNotAllowed
          responseAs[String] must contain("HTTP method not allowed")
        }
    }
  }
}
