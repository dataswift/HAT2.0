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
 * 2 / 2018
 */

package org.hatdex.hat.api.service.applications

import com.mohiva.play.silhouette.api.crypto.Base64AuthenticatorEncoder
import com.mohiva.play.silhouette.impl.authenticators.{ JWTRS256Authenticator, JWTRS256AuthenticatorSettings }
import org.hatdex.hat.api.models.EndpointData
import org.hatdex.hat.api.models.applications.{ ApplicationStatus, HatApplication, Version }
import org.hatdex.hat.api.service.richData.{ DataDebitContractService, RichDataService }
import org.joda.time.DateTime
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.{ BeforeAll, BeforeEach }
import play.api.libs.json._
import play.api.test.PlaySpecification
import play.api.Logger

import scala.concurrent.Await
import scala.concurrent.duration._

class ApplicationsServiceSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with ApplicationsServiceContext with BeforeEach with BeforeAll {

  val logger = Logger(this.getClass)

  sequential

  def beforeAll: Unit = {
    Await.result(databaseReady, 60.seconds)
  }

  override def before: Unit = {
    import org.hatdex.hat.dal.Tables
    import org.hatdex.libs.dal.HATPostgresProfile.api._

    val action = DBIO.seq(
      Tables.ApplicationStatus.delete)

    Await.result(hatDatabase.run(action), 60.seconds)
  }

  "The `applicationStatus` parameterless method" should {
    "List all available applications" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val result = for {
        apps <- service.applicationStatus()
      } yield {
        apps.length must be equalTo 5
        apps.find(_.application.id == notablesApp.id) must beSome
        apps.find(_.application.id == notablesAppDebitless.id) must beSome
        apps.find(_.application.id == notablesAppIncompatible.id) must beSome
      }

      result await (1, 20.seconds)
    }

    "Include setup applications" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val result = for {
        _ ← service.setup(HatApplication(notablesApp, setup = false, active = false, None, None, None))
        apps ← service.applicationStatus()
      } yield {
        apps.length must be equalTo 5
        apps.find(_.application.id == notablesAppDebitless.id) must beSome
        apps.find(_.application.id == notablesAppIncompatible.id) must beSome
        val setupApp = apps.find(_.application.id == notablesApp.id)
        setupApp must beSome
        setupApp.get.setup must beTrue
      }

      result await (1, 20.seconds)
    }
  }

  "The `applicationStatus` method" should {

    "Provide status for a specific application" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val result = for {
        app ← service.applicationStatus(notablesApp.id)
      } yield {
        app must beSome
        app.get.application.id must be equalTo notablesApp.id
      }

      result await (1, 20.seconds)
    }

    "Return `None` when application is not found by ID" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val result = for {
        app ← service.applicationStatus("randomid")
      } yield {
        app must beNone
      }

      result await (1, 20.seconds)
    }

    "Return `active=false` status for Internal status check apps that are not setup" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val result = for {
        app ← service.applicationStatus(notablesApp.id)
      } yield {
        app must beSome
        app.get.active must beFalse
      }

      result await (1, 20.seconds)
    }

    "Return `active=true` status and most recent data timestamp for active app" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val dataService = application.injector.instanceOf[RichDataService]
      val result = for {
        app ← service.applicationStatus(notablesApp.id)
        _ ← service.setup(app.get)
        _ ← dataService.saveData(
          owner.userId,
          Seq(EndpointData(notablesApp.status.recentDataCheckEndpoint.get, None,
            JsObject(Map("test" -> JsString("test"))), None)), skipErrors = true)
        app <- service.applicationStatus(notablesApp.id)
      } yield {
        app must beSome
        app.get.setup must beTrue
        app.get.needsUpdating must beSome(false)
        app.get.mostRecentData must beSome[DateTime]
      }

      result await (1, 10.seconds)
    }

    "Return `active=false` status for External status check apps that are setup but respond with wrong status" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val result = for {
        app ← service.applicationStatus(notablesAppExternalFailing.id)
        _ ← service.setup(app.get)
        setup ← service.applicationStatus(notablesAppExternalFailing.id)
      } yield {
        setup must beSome
        setup.get.setup must beTrue
        setup.get.active must beFalse
      }

      result await (1, 20.seconds)
    }

    "Return `active=true` status for External status check apps that are setup" in {
      val service = application.injector.instanceOf[ApplicationsService]

      val result = for {
        app <- service.applicationStatus(notablesAppExternal.id)
        _ <- service.setup(app.get)
        setup <- service.applicationStatus(notablesAppExternal.id)
      } yield {
        setup must beSome
        setup.get.setup must beTrue
        setup.get.active must beTrue
      }

      result await (1, 20.seconds)
    }

    "Return `active=false` status for apps where current version is not compatible with one setup" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val result = for {
        _ <- service.setup(HatApplication(notablesAppIncompatible, setup = false, active = false, None, None, None))
        app <- service.applicationStatus(notablesAppIncompatibleUpdated.id)
      } yield {
        app must beSome
        app.get.setup must beTrue
        app.get.needsUpdating must beSome(true)
      }

      result await (1, 20.seconds)
    }

    "Return `active=false` status for apps where data debit has been disabled" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val dataDebitService = application.injector.instanceOf[DataDebitContractService]
      val result = for {
        app <- service.applicationStatus(notablesApp.id)
        _ <- service.setup(app.get)(hatServer, owner, fakeRequest)
        _ <- dataDebitService.dataDebitDisable(app.get.application.dataDebitId.get)
        setup <- service.applicationStatus(app.get.application.id)
      } yield {
        setup must beSome
        setup.get.setup must beTrue
        setup.get.active must beFalse
      }

      result await (1, 20.seconds)
    }
  }

  "The `setup` method" should {
    "Enable an application and update its status as well as enable data debit if set up" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val dataDebitService = application.injector.instanceOf[DataDebitContractService]
      val result = for {
        app <- service.applicationStatus(notablesApp.id)
        setup <- service.setup(app.get)
        dd <- dataDebitService.dataDebit(notablesApp.dataDebitId.get)
      } yield {
        setup.active must beTrue
        dd must beSome
        dd.get.activeBundle must beSome
        dd.get.activeBundle.get.bundle.name must be equalTo notablesApp.permissions.dataRequired.get.bundle.name
      }

      result await (1, 20.seconds)
    }

    "Enable an application and update its status with no data debit required" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val result = for {
        app <- service.applicationStatus(notablesAppDebitless.id)
        setup <- service.setup(app.get)
      } yield {
        setup.active must beTrue
      }

      result await (1, 20.seconds)
    }

    "Return failure for a made-up Application Information" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val result = for {
        setup ← service.setup(HatApplication(notablesAppMissing, true, true, None, None, None))
      } yield setup

      result must throwA[RuntimeException].await(1, 20.seconds)
    }
  }

  "The `disable` method" should {
    "Disable an application with associated data debit" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val dataDebitService = application.injector.instanceOf[DataDebitContractService]
      val result = for {
        app ← service.applicationStatus(notablesApp.id)
        _ ← service.setup(app.get)
        setup ← service.disable(app.get)
        dd ← dataDebitService.dataDebit(app.get.application.dataDebitId.get)
      } yield {
        setup.active must beFalse
        dd must beSome
        dd.get.activeBundle must beNone
      }

      result await (1, 20.seconds)
    }

    "Disable an application without a data debit" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val result = for {
        app ← service.applicationStatus(notablesAppDebitless.id)
        _ ← service.setup(app.get)
        setup ← service.disable(app.get)
      } yield {
        setup.active must beFalse
      }

      result await (1, 20.seconds)
    }

    "Return failure for a made-up Application Information" in {
      val service = application.injector.instanceOf[ApplicationsService]
      val result = for {
        setup ← service.disable(HatApplication(notablesAppMissing, true, true, None, None, None))
      } yield setup

      result must throwA[RuntimeException].await(1, 20.seconds)
    }
  }

  "The `applicationToken` method" should {
    "Create a token that includes application and its version among custom claims" in {

      val service = application.injector.instanceOf[ApplicationsService]
      val result = for {
        token ← service.applicationToken(owner, notablesApp)
      } yield {
        token.accessToken mustNotEqual ""
        val encoder = new Base64AuthenticatorEncoder()
        val settings = JWTRS256AuthenticatorSettings("X-Auth-Token", None, "hat.org", Some(3.days), 3.days)
        val unserialized = JWTRS256Authenticator.unserialize(token.accessToken, encoder, settings)

        unserialized must beSuccessfulTry
        (unserialized.get.customClaims.get \ "application").get must be equalTo JsString(notablesApp.id)
        (unserialized.get.customClaims.get \ "applicationVersion").get must be equalTo JsString(notablesApp.info.version.toString)
      }

      result await (1, 20.seconds)

    }
  }

  "The `ApplicationStatusCheckService` `status` method" should {
    "Return `true` for internal status checks" in {
      withMockWsClient { client ⇒
        val service = new ApplicationStatusCheckService(client)(remoteEC)
        service.status(ApplicationStatus.Internal(Version("1.0.0"), None), "token")
          .map { result ⇒
            result must beTrue
          }
          .await(1, 10.seconds)
      }
    }

    "Return `true` for external check with matching status" in {
      withMockWsClient { client ⇒
        val service = new ApplicationStatusCheckService(client)(remoteEC)
        service.status(ApplicationStatus.External(Version("1.0.0"), "/status", 200, None), "token")
          .map { result ⇒
            result must beTrue
          }
          .await(1, 10.seconds)
      }
    }

    "Return `false` for external check with non-matching status" in {
      withMockWsClient { client ⇒
        val service = new ApplicationStatusCheckService(client)(remoteEC)
        service.status(ApplicationStatus.External(Version("1.0.0"), "/failing", 200, None), "token")
          .map { result ⇒
            result must beFalse
          }
          .await(1, 10.seconds)
      }
    }
  }

}

