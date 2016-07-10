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
import hatdex.hat.api.endpoints.jsonExamples.UserExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.authentication.models.{ AccessToken, User }
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import org.mindrot.jbcrypt.BCrypt
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class UsersSpec extends Specification with Specs2RouteTest with BeforeAfterAll with Users {
  val logger: LoggingAdapter = system.log
  var validAccessTokens = Future {
    Seq[AccessToken]()
  }
  var platformAccessToken: String = ""
  var unprivilegedAccessToken: String = ""

  def actorRefFactory = system

  def beforeAll() = {
    val validUsers = Seq(
      UserUserRow(UUID.fromString("6096eba1-0e9e-4607-9dfd-072bfa106bf4"),
        LocalDateTime.now(), LocalDateTime.now(),
        "bob@example.com", Some(BCrypt.hashpw("pa55w0rd", BCrypt.gensalt())),
        "Test User", "owner", enabled = true),
      UserUserRow(UUID.fromString("17380a13-16c3-49f7-968b-30df0eefbe0f"),
        LocalDateTime.now(), LocalDateTime.now(),
        "alice@example.com", Some(BCrypt.hashpw("dr0w55ap", BCrypt.gensalt())),
        "Test Debit User", "dataDebit", enabled = true),
      UserUserRow(UUID.fromString("dd15948d-18ef-4062-a3c0-33f21b3cffd1"),
        LocalDateTime.now(), LocalDateTime.now(),
        "carol@example.com", Some(BCrypt.hashpw("p4ssWOrD", BCrypt.gensalt())),
        "Test Credit User", "dataCredit", enabled = false),
      UserUserRow(UUID.fromString("bae66f37-8421-4932-9755-ed7ffa865e0f"),
        LocalDateTime.now(), LocalDateTime.now(),
        "platform@platform.com", Some(BCrypt.hashpw("p4ssWOrD", BCrypt.gensalt())),
        "Platform User", "platform", enabled = true))

    db.withSession { implicit session =>
      UserUser.forceInsertAll(validUsers: _*)
    }

    validAccessTokens = Future.sequence {
      validUsers.map { user => fetchOrGenerateToken(User.fromDbModel(user), "hat.hubofallthings.net", user.role) }
    }

    val fPlatformToken = validAccessTokens.map { tokens =>
      tokens.find(_.userId == UUID.fromString("bae66f37-8421-4932-9755-ed7ffa865e0f")).map(_.accessToken).get
    }

    val fUnprivilegedToken = validAccessTokens.map { tokens =>
      tokens.find(_.userId == UUID.fromString("17380a13-16c3-49f7-968b-30df0eefbe0f")).map(_.accessToken).get
    }

    platformAccessToken = Await.result(fPlatformToken, 10 seconds)
    unprivilegedAccessToken = Await.result(fUnprivilegedToken, 10 seconds)

  }

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      val userIds = Seq(
        UUID.fromString("17380a13-16c3-49f7-968b-30df0eefbe0f"),
        UUID.fromString("dd15948d-18ef-4062-a3c0-33f21b3cffd1"),
        UUID.fromString("6096eba1-0e9e-4607-9dfd-072bfa106bf4"),
        UUID.fromString("bae66f37-8421-4932-9755-ed7ffa865e0f"))
      UserAccessToken.filter(_.userId inSet userIds).delete
      UserUser.filter(_.userId inSet userIds).delete
      session.close()
    }
  }

  import JsonProtocol._

  sequential

  "User Service" should {
    "Let Platform user create new users" in {
      logger.debug(s"Platform access token: $platformAccessToken")

      val user = HttpRequest(POST, "/users/user")
        .withHeaders(RawHeader("X-Auth-Token", platformAccessToken)) // platform user
        .withEntity(HttpEntity(MediaTypes.`application/json`, UserExamples.userExample)) ~>
        sealRoute(routes) ~> check {
          response.status should be equalTo Created
          responseAs[String] must contain("apiclient")
          responseAs[User]
        }
      //Cleanup right away
      db.withSession { implicit session =>
        UserAccessToken.filter(_.userId === user.userId).delete
        UserUser.filter(_.userId === user.userId).delete
      }
      user.role must be equalTo "dataDebit"
    }

    "Forbid Platform user from creating new owner" in {
      HttpRequest(POST, "/users/user")
        .withHeaders(RawHeader("X-Auth-Token", platformAccessToken)) // platform user
        .withEntity(HttpEntity(MediaTypes.`application/json`, UserExamples.ownerUserExample)) ~>
        sealRoute(routes) ~> check {
          response.status should be equalTo Unauthorized
        }
    }

    "Forbid unprivileged user from creating new owner" in {
      HttpRequest(POST, "/users/user")
        .withHeaders(RawHeader("X-Auth-Token", unprivilegedAccessToken))
        .withEntity(HttpEntity(MediaTypes.`application/json`, UserExamples.userExample)) ~>
        sealRoute(routes) ~> check {
          response.status should be equalTo Forbidden
        }
    }

    "Provide access tokens for existing users" in {
      HttpRequest(GET, "/users/access_token")
        .withHeaders(RawHeader("username", "bob@example.com"), RawHeader("password", "pa55w0rd")) ~>
        sealRoute(routes) ~> check {
          response.status should be equalTo OK
          validateJwtToken(responseAs[AccessToken].accessToken) must be equalTo true
        }
    }

    "Reject disabled user's authentication" in {
      HttpRequest(GET, "/users/access_token")
        .withHeaders(RawHeader("username", "carol@example.com"), RawHeader("password", "p4ssWOrD")) ~>
        sealRoute(routes) ~> check {
          response.status should be equalTo Unauthorized
        }
    }

    "Allow platform user to enable users" in {
      HttpRequest(PUT, "/users/user/dd15948d-18ef-4062-a3c0-33f21b3cffd1/enable")
        .withHeaders(RawHeader("X-Auth-Token", platformAccessToken)) ~> // platform user
        sealRoute(routes) ~> check {
          response.status should be equalTo OK
        }

      HttpRequest(GET, "/users/access_token")
        .withHeaders(RawHeader("username", "carol@example.com"), RawHeader("password", "p4ssWOrD")) ~>
        sealRoute(routes) ~> check {
          response.status should be equalTo OK
        }
    }

    "Let newly created user retrieve their access token" in {
      val user = HttpRequest(POST, "/users/user")
        .withHeaders(RawHeader("X-Auth-Token", platformAccessToken)) // platform user
        .withEntity(HttpEntity(MediaTypes.`application/json`, UserExamples.userExample)) ~>
        sealRoute(routes) ~> check {
          response.status should be equalTo Created
          responseAs[String] must contain("apiclient")
          responseAs[User]
        }

      HttpRequest(GET, "/users/access_token?username=apiClient@platform.com&password=simplepass")
        .withHeaders(RawHeader("username", "apiClient@platform.com"), RawHeader("password", "simplepass")) ~>
        sealRoute(routes) ~> check {
          response.status should be equalTo OK
          responseAs[String] must contain("accessToken")
        }

      //Cleanup right away
      db.withSession { implicit session =>
        UserAccessToken.filter(_.userId === user.userId).delete
        UserUser.filter(_.userId === user.userId).delete
      }

      user.role must be equalTo "dataDebit"
    }
  }

}

