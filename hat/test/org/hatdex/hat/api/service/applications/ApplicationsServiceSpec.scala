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

import akka.Done
import com.mohiva.play.silhouette.api.crypto.Base64AuthenticatorEncoder
import com.mohiva.play.silhouette.impl.authenticators.{ JWTRS256Authenticator, JWTRS256AuthenticatorSettings }
import io.dataswift.models.hat.EndpointData
import io.dataswift.models.hat.applications.{ ApplicationStatus, HatApplication, Version }
import org.hatdex.hat.api.service.applications.ApplicationExceptions.HatApplicationSetupException
import org.hatdex.hat.api.service.richData.{ DataDebitService, RichDataService }
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter
import play.api.cache.AsyncCacheApi
import play.api.libs.json._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ApplicationsServiceSpec extends ApplicationsServiceContext with BeforeAndAfter {

  "The `applicationStatus` parameterless method" should "List all available applications" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val result = for {
      apps <- service.applicationStatus()
    } yield {
      apps.length must equal(8)
      apps.find(_.application.id == notablesApp.id) must not be empty
      apps.find(_.application.id == notablesAppDebitless.id) must not be empty
      apps.find(_.application.id == notablesAppIncompatible.id) must not be empty
    }
    Await.result(result, 20.seconds)
  }

  "The `applicationStatus` method" should "Return `active=false` status for Internal status check apps that are not setup" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val app     = Await.result(service.applicationStatus(notablesApp.id), 20.seconds).value
    app.active mustBe false
  }

  "The `applicationStatus` parameterless method" should "Include setup applications" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val result = for {
      _ <- service.setup(HatApplication(notablesApp, setup = false, enabled = false, active = false, None, None, None))
      apps <- service.applicationStatus()
    } yield {
      apps.length must equal(8)
      apps.find(_.application.id == notablesAppDebitless.id) must not be empty
      apps.find(_.application.id == notablesAppIncompatible.id) must not be empty
      val setupApp = apps.find(_.application.id == notablesApp.id)
      setupApp.value.setup mustBe true
    }
    Await.result(result, 20.seconds)
  }

  "The `applicationStatus` method" should "Provide status for a specific application" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val result = for {
      Some(app) <- service.applicationStatus(notablesApp.id)
    } yield app.application.id mustBe notablesApp.id
    Await.result(result, 20.seconds)
  }

  it should "Return `None` when application is not found by ID" in {
    val service = application.injector.instanceOf[ApplicationsService]
    Await.result(service.applicationStatus("randomid"), 20.seconds) mustBe None
  }

  it should "Return `active=true` status and most recent data timestamp for active app" in {
    val service     = application.injector.instanceOf[ApplicationsService]
    val dataService = application.injector.instanceOf[RichDataService]
    val result = for {
      Some(initialApp) <- service.applicationStatus(notablesApp.id)
      _ <- service.setup(initialApp)
      _ <- dataService.saveData(
             owner.userId,
             Seq(
               EndpointData(notablesApp.status.recentDataCheckEndpoint.get,
                            None,
                            None,
                            None,
                            JsObject(Map("test" -> JsString("test"))),
                            None
               )
             ),
             skipErrors = true
           )
      Some(app) <- service.applicationStatus(notablesApp.id, bustCache = true)
    } yield {
      app.setup mustBe true
      app.needsUpdating.value mustBe false
    }
    Await.result(result, 10.seconds)
  }

  it should "Return `active=false` status for External status check apps that are setup but respond with wrong status" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val result = for {
      Some(app) <- service.applicationStatus(notablesAppExternalFailing.id)
      appSetupResponse <- service.setup(app)
      Some(setup) <- service.applicationStatus(notablesAppExternalFailing.id)
    } yield {
      appSetupResponse.setup mustBe true
      setup.setup mustBe true
      setup.active mustBe false
    }
    Await.result(result, 10.seconds)
  }

  it should "Return `active=true` status for External status check apps that are setup" in {
    val service = application.injector.instanceOf[ApplicationsService]

    val result = for {
      Some(app) <- service.applicationStatus(notablesAppExternal.id)
      _ <- service.setup(app)
      Some(setup) <- service.applicationStatus(notablesAppExternal.id)
    } yield {
      setup.setup mustBe true
      setup.active mustBe true
    }

    Await.result(result, 10.seconds)
  }

  it should "Return `active=false` status for apps where current version is not compatible with one setup" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val result = for {
      _ <- service.setup(
             HatApplication(notablesAppIncompatible, setup = false, enabled = false, active = false, None, None, None)
           )
      Some(app) <- service.applicationStatus(notablesAppIncompatibleUpdated.id)
    } yield {
      app.setup mustBe true
      app.needsUpdating mustBe Some(true)
    }
    Await.result(result, 10.seconds)
  }

  it should "Return `active=false` status for apps where data debit has been disabled" in {
    val service          = application.injector.instanceOf[ApplicationsService]
    val dataDebitService = application.injector.instanceOf[DataDebitService]
    val cache            = application.injector.instanceOf[AsyncCacheApi]
    val result = for {
      Some(app) <- service.applicationStatus(notablesApp.id)
      _ <- service.setup(app)(hatServer, owner, fakeRequest)
      _ <- dataDebitService.dataDebitDisable(app.application.dataDebitId.get, cancelAtPeriodEnd = false)
      _ <- cache.remove(
             service.appCacheKey(app.application.id)
           ) //cache.remove(s"apps:${hatServer.domain}:${app.application.id}")
      _ <- cache.get(service.appCacheKey(app.application.id))
      Some(setup) <- service.applicationStatus(app.application.id)
    } yield {
      setup.setup mustBe true
      setup.active mustBe false
    }
    Await.result(result, 10.seconds)
  }

  "The `setup` method" should "Enable an application and update its status as well as enable data debit if set up" in {
    val service          = application.injector.instanceOf[ApplicationsService]
    val dataDebitService = application.injector.instanceOf[DataDebitService]
    val result = for {
      Some(app) <- service.applicationStatus(notablesApp.id)
      setup <- service.setup(app)
      dd <- dataDebitService.dataDebit(notablesApp.dataDebitId.value)
      _ <- dataDebitService.dataDebit(app.application.dataDebitId.value)
    } yield {
      setup.active mustBe true
      dd.value.activePermissions.value.bundle.name must equal(notablesApp.permissions.dataRequired.get.bundle.name)
    }
    Await.result(result, 10.seconds)
  }

  it should "Enable an application and update its status with no data debit required" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val result = for {
      Some(app) <- service.applicationStatus(notablesAppDebitless.id)
      setup <- service.setup(app)
    } yield setup.active mustBe true

    Await.result(result, 10.seconds)
  }

  it should "Return failure for a made-up Application Information" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val result = for {
      setup <- service.setup(HatApplication(notablesAppMissing, true, true, true, None, None, None))
    } yield setup

    an[HatApplicationSetupException] should be thrownBy Await.result(result, 10.seconds)
  }

  "Application `setup` method for applications with dependencies" should "Enable plug dependencies" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val result = for {
      Some(app) <- service.applicationStatus(notablesAppDebitlessWithPlugDependency.id)
      setup <- service.setup(app)
      dependency <- service.applicationStatus(plugApp.id)
    } yield {
      setup.active mustBe true
      setup.enabled mustBe true
      setup.dependenciesEnabled must not be empty
      dependency.value.enabled === true
    }

    Await.result(result, 10.seconds)
  }

  it should "Return partial success for application with invalid dependencies" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val result = for {
      Some(app) <- service.applicationStatus(notablesAppDebitlessWithInvalidDependency.id)
      setup <- service.setup(app)
    } yield {
      setup.active mustBe true
      setup.enabled mustBe true
      setup.dependenciesEnabled === false
    }

    Await.result(result, 10.seconds)
  }

  "The `disable` method" should "Disable an application with associated data debit" in {
    val service          = application.injector.instanceOf[ApplicationsService]
    val dataDebitService = application.injector.instanceOf[DataDebitService]
    val result = for {
      Some(app) <- service.applicationStatus(notablesApp.id)
      _ <- service.setup(app)
      setup <- service.disable(app)
      dd <- dataDebitService.dataDebit(app.application.dataDebitId.get)
    } yield {
      setup.active mustBe false
      dd.value.activePermissions mustBe None
    }

    Await.result(result, 10.seconds)
  }

  it should "Disable an application without a data debit" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val result = for {
      Some(app) <- service.applicationStatus(notablesAppDebitless.id)
      _ <- service.setup(app)
      setup <- service.disable(app)
    } yield setup.active mustBe false

    Await.result(result, 10.seconds)
  }

  it should "Return failure for a made-up Application Information" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val result = for {
      setup <- service.disable(HatApplication(notablesAppMissing, true, true, true, None, None, None))
    } yield setup

    an[RuntimeException] should be thrownBy Await.result(result, 10.seconds)
  }

  "The `applicationToken` method" should "Create a token that includes application and its version among custom claims" in {
    val service = application.injector.instanceOf[ApplicationsService]
    val result = for {
      token <- service.applicationToken(owner, notablesApp)
    } yield {
      token.accessToken.lastOption must not be None
      val encoder      = new Base64AuthenticatorEncoder()
      val settings     = JWTRS256AuthenticatorSettings("X-Auth-Token", None, "hat.org", Some(3.days), 3.days)
      val unserialized = JWTRS256Authenticator.unserialize(token.accessToken, encoder, settings)

      // TODO: How do I match this?
      //unserialized must beSuccessfulTry
      (unserialized.get.customClaims.get \ "application").get must equal(JsString(notablesApp.id))
      (unserialized.get.customClaims.get \ "applicationVersion").get must equal(
        JsString(
          notablesApp.info.version.toString
        )
      )
    }

    Await.result(result, 10.seconds)

  }

  "The `ApplicationStatusCheckService` `status` method" should "Return `true` for internal status checks" in {
    withMockWsClient { client =>
      val service = new ApplicationStatusCheckService(client)(remoteEC)
      val result = service
        .status(ApplicationStatus
                  .Internal(Version("1.0.0"), None, None, None, DateTime.now()),
                "token"
        )
        .map { result =>
          result mustBe true
        }
      Await.result(result, 10.seconds)
    }
  }

  it should "Return `true` for external check with matching status" in {
    withMockWsClient { client =>
      val service = new ApplicationStatusCheckService(client)(remoteEC)
      val result = service
        .status(ApplicationStatus.External(Version("1.0.0"), "/status", 200, None, None, None, DateTime.now()), "token")
        .map { result =>
          result mustBe true
        }
      Await.result(result, 10.seconds)
    }
  }

  it should "Return `false` for external check with non-matching status" in {
    withMockWsClient { client =>
      val service = new ApplicationStatusCheckService(client)(remoteEC)
      val result = service
        .status(ApplicationStatus.External(Version("1.0.0"), "/failing", 200, None, None, None, DateTime.now()),
                "token"
        )
        .map { result =>
          result mustBe false
        }
      Await.result(result, 10.seconds)
    }
  }

  "JoinContract" should "not run unless the application template is a Contract" in {
    val service = application.injector.instanceOf[ApplicationsService]

    val result = for {
      _ <- service.joinContract(fakeContract, "hatName")
      notablesApp <- service.joinContract(notablesApp, "hatName")
    } yield notablesApp must equal(Done)
    //contractApp must beLeft(ServiceRespondedWithFailure("The Adjudicator Service responded with an error: Internal Server Error"))
    Await.result(result, 10.seconds)
  }

  // Commented until I figure out how to Mock it.
  //    "Adding a Contract should succeed" in {
  //      val service = application.injector.instanceOf[ApplicationsService]

  //      val result = for {
  //        _ <- service.setup(
  //          HatApplication(
  //            fakeContract,
  //            setup = false,
  //            enabled = false,
  //            active = false,
  //            None,
  //            None,
  //            None))
  //        apps <- service.applicationStatus()
  //      } yield {
  //        apps.length must be equal 8
  //        val setupApp = apps.find(_.application.id == notablesApp.id)
  //        setupApp must not be empty
  //        setupApp.get.setup must beTrue
  //      }

  //      result await (1, 20.seconds)
  //    }
}
