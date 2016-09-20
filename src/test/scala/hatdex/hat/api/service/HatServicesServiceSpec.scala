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

package hatdex.hat.api.service

import java.util.UUID

import akka.actor.ActorRefFactory
import akka.event.{ Logging, LoggingAdapter }
import hatdex.hat.api.{DatabaseInfo, TestDataCleanup}
import hatdex.hat.api.endpoints._
import hatdex.hat.api.endpoints.jsonExamples.{ BundleExamples, DataDebitExamples }
import hatdex.hat.api.models._
import hatdex.hat.authentication.{JwtTokenHandler, HatAuthTestHandler}
import hatdex.hat.authentication.authenticators.AccessTokenHandler
import hatdex.hat.dal.Tables._
import hatdex.hat.dal.SlickPostgresDriver.api._
import org.joda.time.LocalDateTime
import org.mindrot.jbcrypt.BCrypt
import org.specs2.execute.Result
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import org.specs2.specification.{ BeforeAfterAll, Scope }
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{ HttpRequest, _ }
import spray.json._
import spray.testkit.Specs2RouteTest

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration.Duration

class HatServicesServiceSpec extends Specification with Specs2RouteTest with BeforeAfterAll with HatServicesService with JwtTokenHandler {
  override val logger: LoggingAdapter = Logging.getLogger(system, "tests")
  lazy val testLogger = logger

  val ownerUser = HatAuthTestHandler.validUsers.find(_.role == "owner").get

  // Prepare the data to create test bundles on
  def beforeAll() = {
    Await.result(TestDataCleanup.cleanupAll, Duration("40 seconds"))

    val validUsers = Seq(
      UserUserRow(ownerUser.userId,
        LocalDateTime.now(), LocalDateTime.now(),
        ownerUser.email, ownerUser.pass,
        ownerUser.name, ownerUser.role, enabled = true))

    DatabaseInfo.db.run(UserUser.forceInsertAll(validUsers))
  }

  // Clean up all data
  def afterAll() = {
  }

  sequential

  def awaiting[T]: Future[MatchResult[T]] => Result = { _.await }

  "Existing HAT Services" should {
    "Contain the core services" in awaiting {
      val eventualServices = hatServices(Set("app", "dataplug", "testapp"))

      eventualServices map { services =>
        services.filter(_.category == "app") must not(beEmpty)
        services.filter(_.category == "dataplug") must not(beEmpty)
        services.filter(_.category == "testapp") must not(beEmpty)

        services.find(_.title == "Rumpel") must beSome
        services.find(_.title == "MarketSquare") must beSome
        services.find(_.title == "Hatters") must beSome

        services.find(_.title == "Calendar") must beSome
      }
    }

    "Find existing services" in awaiting {
      findOrCreateHatService("Rumpel", "https://rumpel.hubofallthings.com/users/authenticate") map { service =>
        service.browser must beTrue
      }
      findOrCreateHatService("NewApp", "https://rumpel.hubofallthings.com/users/authenticate") map { service =>
        service.browser must beFalse
        service.category must be equalTo "app"
      }
      findOrCreateHatService("Rumpel", "http://rumpel-stage.hubofallthings.com.s3-website-eu-west-1.amazonaws.com/users/authenticate") map { service =>
        service.browser must beTrue
      }
    }

    "Correctly generate tokens" in awaiting {
      val eventualRumpelToken = for {
        service <- findOrCreateHatService("Rumpel", "https://rumpel.hubofallthings.com/users/authenticate")
        token <- hatServiceToken(ownerUser, service)
      } yield token.accessToken

      eventualRumpelToken map { token =>
        val maybeScope = getTokenAccessScope(token)
        maybeScope must beSome
        maybeScope.get must be equalTo "owner"
      }

      val eventualValidateToken = for {
        service <- findOrCreateHatService("UnknownService", "https://service.example.com/")
        token <- hatServiceToken(ownerUser, service)
      } yield token.accessToken

      eventualValidateToken map { token =>
        val maybeScope = getTokenAccessScope(token)
        maybeScope must beSome
        maybeScope.get must be equalTo "validate"
      }
    }
  }

}