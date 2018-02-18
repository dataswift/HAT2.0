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

import akka.util.ByteString
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.HATTestContext
import org.hatdex.hat.api.models.applications.{ Application, ApplicationStatus, Version }
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.resourceManagement.FakeHatConfiguration
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.Logger
import play.api.http.HttpEntity
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.test.FakeRequest
import play.core.server.Server
import play.api.{ Application ⇒ PlayApplication }

import scala.concurrent.Future

trait ApplicationsServiceContext extends HATTestContext {
  override lazy val application: PlayApplication = new GuiceApplicationBuilder()
    .configure(FakeHatConfiguration.config)
    .overrides(new FakeModule)
    .overrides(new CustomisedFakeModule)
    .build()

  import scala.concurrent.ExecutionContext.Implicits.global
  private val logger = Logger(this.getClass)
  private val sampleNotablesAppJson =
    """
      |
      |    {
      |        "id": "notables",
      |        "kind": {
      |            "url": "https://itunes.apple.com/gb/app/notables/id1338778866?mt=8",
      |            "iosUrl": "https://itunes.apple.com/gb/app/notables/id1338778866?mt=8",
      |            "kind": "App"
      |        },
      |        "info": {
      |            "version": "1.0.0",
      |            "published": true,
      |            "name": "Notables",
      |            "headline": "All your words",
      |            "description": {
      |                "text": "\n Anything you write online is your data – searches, social media posts, comments and notes.\n\n Start your notes here on Notables, where they will be stored completely privately in your HAT.\n\n Use Notables to draft and share social media posts. You can set how long they stay on Twitter or Facebook – a day, a week or a month. You can always set them back to private later: it will disappear from your social media but you won’t lose it because it’s saved in your HAT.\n\n Add images or pin locations as reminders of where you were or what you saw.\n          ",
      |                "markdown": "\n Anything you write online is your data – searches, social media posts, comments and notes.\n\n Start your notes here on Notables, where they will be stored completely privately in your HAT.\n\n Use Notables to draft and share social media posts. You can set how long they stay on Twitter or Facebook – a day, a week or a month. You can always set them back to private later: it will disappear from your social media but you won’t lose it because it’s saved in your HAT.\n\n Add images or pin locations as reminders of where you were or what you saw.\n          ",
      |                "html": "\n <p>Anything you write online is your data – searches, social media posts, comments and notes.</p>\n\n <p>Start your notes here on Notables, where they will be stored completely privately in your HAT.</p>\n\n <p>Use Notables to draft and share social media posts. You can set how long they stay on Twitter or Facebook – a day, a week or a month. You can always set them back to private later: it will disappear from your social media but you won’t lose it because it’s saved in your HAT.</p>\n\n <p>Add images or pin locations as reminders of where you were or what you saw.</p>\n          "
      |            },
      |            "dataPreview": [
      |                {
      |                    "source": "notables",
      |                    "date": {
      |                        "iso": "2018-02-15T03:52:37.000Z",
      |                        "unix": 1518666757
      |                    },
      |                    "types": [
      |                        "note"
      |                    ],
      |                    "title": {
      |                        "text": "leila.hubat.net",
      |                        "action": "private"
      |                    },
      |                    "content": {
      |                        "text": "Notes are live!"
      |                    }
      |                },
      |                {
      |                    "source": "notables",
      |                    "date": {
      |                        "iso": "2018-02-15T03:52:37.317Z",
      |                        "unix": 1518666757
      |                    },
      |                    "types": [
      |                        "note"
      |                    ],
      |                    "title": {
      |                        "text": "leila.hubat.net",
      |                        "action": "private"
      |                    },
      |                    "content": {
      |                        "text": "And I love 'em!"
      |                    }
      |                }
      |            ],
      |            "graphics": {
      |                "banner": {
      |                    "normal": ""
      |                },
      |                "logo": {
      |                    "normal": "https://s3-eu-west-1.amazonaws.com/hubofallthings-com-dexservi-dexpublicassetsbucket-kex8hb7fsdge/notablesapp/0x0ss.png"
      |                },
      |                "screenshots": [
      |                    {
      |                        "normal": "https://s3-eu-west-1.amazonaws.com/hubofallthings-com-dexservi-dexpublicassetsbucket-kex8hb7fsdge/notablesapp/0x0ss.jpg",
      |                        "large": "https://s3-eu-west-1.amazonaws.com/hubofallthings-com-dexservi-dexpublicassetsbucket-kex8hb7fsdge/notablesapp/0x0ss-5.jpg"
      |                    },
      |                    {
      |                        "normal": "https://s3-eu-west-1.amazonaws.com/hubofallthings-com-dexservi-dexpublicassetsbucket-kex8hb7fsdge/notablesapp/0x0ss-2.jpg",
      |                        "large": "https://s3-eu-west-1.amazonaws.com/hubofallthings-com-dexservi-dexpublicassetsbucket-kex8hb7fsdge/notablesapp/0x0ss-6.jpg"
      |                    },
      |                    {
      |                        "normal": "https://s3-eu-west-1.amazonaws.com/hubofallthings-com-dexservi-dexpublicassetsbucket-kex8hb7fsdge/notablesapp/0x0ss-3.jpg",
      |                        "large": "https://s3-eu-west-1.amazonaws.com/hubofallthings-com-dexservi-dexpublicassetsbucket-kex8hb7fsdge/notablesapp/0x0ss-7.jpg"
      |                    }
      |                ]
      |            }
      |        },
      |        "permissions": {
      |            "rolesGranted": [
      |                {
      |                    "role": "namespacewrite",
      |                    "detail": "rumpel"
      |                },
      |                {
      |                    "role": "namespaceread",
      |                    "detail": "rumpel"
      |                },
      |                {
      |                    "role": "datadebit",
      |                    "detail": "app-notables"
      |                }
      |            ],
      |            "dataRequired": {
      |                "bundle": {
      |                    "name": "notablesapp",
      |                    "bundle": {
      |                        "profile": {
      |                            "endpoints": [
      |                                {
      |                                    "endpoint": "rumpel/profile",
      |                                    "filters": [
      |                                        {
      |                                            "field": "shared",
      |                                            "operator": {
      |                                                "value": true,
      |                                                "operator": "contains"
      |                                            }
      |                                        }
      |                                    ]
      |                                }
      |                            ],
      |                            "orderBy": "dateCreated",
      |                            "ordering": "descending",
      |                            "limit": 1
      |                        },
      |                        "notables": {
      |                            "endpoints": [
      |                                {
      |                                    "endpoint": "rumpel/notablesv1",
      |                                    "mapping": {
      |                                        "name": "personal.preferredName",
      |                                        "nick": "personal.nickName",
      |                                        "photo_url": "photo.avatar"
      |                                    },
      |                                    "filters": [
      |                                        {
      |                                            "field": "shared",
      |                                            "operator": {
      |                                                "value": true,
      |                                                "operator": "contains"
      |                                            }
      |                                        }
      |                                    ]
      |                                }
      |                            ],
      |                            "orderBy": "updated_time",
      |                            "ordering": "descending",
      |                            "limit": 1
      |                        }
      |                    }
      |                },
      |                "startDate": "2018-02-15T03:52:38+0000",
      |                "endDate": "2019-02-15T03:52:38+0000",
      |                "rolling": true
      |            }
      |        },
      |        "setup": {
      |            "iosUrl": "notablesapp://notablesapphost",
      |            "kind": "External"
      |        },
      |        "status": {
      |            "compatibility": "1.0.0",
      |            "recentDataCheckEndpoint": "/rumpel/notablesv1",
      |            "kind": "Internal"
      |        }
      |    }
      |
    """.stripMargin

  import org.hatdex.hat.api.json.ApplicationJsonProtocol.applicationFormat
  import play.api.mvc._
  import play.api.routing.sird._

  val notablesApp: Application = Json.parse(sampleNotablesAppJson).as[Application]
  val notablesAppDebitless: Application = notablesApp.copy(
    id = "notables-debitless",
    permissions = notablesApp.permissions.copy(dataRequired = None))
  val notablesAppMissing: Application = notablesAppDebitless.copy(
    id = "notables-missing",
    permissions = notablesApp.permissions.copy(
      dataRequired = Some(notablesApp.permissions.dataRequired.get.copy(
        bundle = notablesApp.permissions.dataRequired.get.bundle.copy(name = "notables-missing-bundle")))))
  val notablesAppIncompatible: Application = notablesApp.copy(
    id = "notables-incompatible",
    permissions = notablesApp.permissions.copy(
      dataRequired = Some(notablesApp.permissions.dataRequired.get.copy(
        bundle = notablesApp.permissions.dataRequired.get.bundle.copy(name = "notables-incompatible-bundle")))))
  val notablesAppIncompatibleUpdated: Application = notablesAppIncompatible.copy(
    info = notablesApp.info.copy(version = Version("1.1.0")),
    status = ApplicationStatus.Internal(Version("1.1.0"), None))

  val notablesAppExternal: Application = notablesApp.copy(
    id = "notables-external",
    status = ApplicationStatus.External(Version("1.0.0"), "/status", 200, None),
    permissions = notablesApp.permissions.copy(
      dataRequired = Some(notablesApp.permissions.dataRequired.get.copy(
        bundle = notablesApp.permissions.dataRequired.get.bundle.copy(name = "notables-external")))))
  val notablesAppExternalFailing: Application = notablesApp.copy(
    id = "notables-external-failing",
    status = ApplicationStatus.External(Version("1.0.0"), "/failing", 200, None),
    permissions = notablesApp.permissions.copy(
      dataRequired = Some(notablesApp.permissions.dataRequired.get.copy(
        bundle = notablesApp.permissions.dataRequired.get.bundle.copy(name = "notables-external-failing")))))

  def withMockWsClient[T](block: WSClient => T): T = {
    Server.withRouterFromComponents() { components =>
      import components.{ defaultActionBuilder ⇒ Action }
      {
        case GET(p"/status") => Action {
          Results.Ok.sendEntity(HttpEntity.Strict(ByteString("OK"), Some("text/plain")))
        }
        case GET(p"/failing") => Action {
          Results.Forbidden.sendEntity(HttpEntity.Strict(ByteString("FORBIDDEN"), Some("text/plain")))
        }
      }
    } { implicit port =>
      logger.info(s"Creating test client at port $port")
      play.api.test.WsTestClient.withClient { client =>
        block(client)
      }
    }
  }

  lazy val mockStatusChecker = {
    case class StatusCheck() extends Answer[Future[Boolean]] {
      override def answer(invocation: InvocationOnMock) = {
        val s = invocation.getArguments()(0).asInstanceOf[ApplicationStatus.Status]
        s match {
          case ApplicationStatus.Internal(_, _) ⇒ Future.successful(true)
          case ApplicationStatus.External(_, "/status", _, _) ⇒ Future.successful(true)
          case _ ⇒ Future.successful(false)
        }
      }
    }

    val mockStatusChecker = mock[ApplicationStatusCheckService]
    mockStatusChecker.status(any[ApplicationStatus.Internal], any[String]) returns Future.successful(true)
    when(mockStatusChecker.status(any[ApplicationStatus.Status], any[String]))
      .thenAnswer(StatusCheck())

    mockStatusChecker

  }

  class CustomisedFakeModule extends AbstractModule with ScalaModule {
    def configure(): Unit = {
      bind[TrustedApplicationProvider].toInstance(new TestApplicationProvider(
        Seq(notablesApp, notablesAppDebitless, notablesAppIncompatibleUpdated,
          notablesAppExternal, notablesAppExternalFailing)))

      bind[ApplicationStatusCheckService].toInstance(mockStatusChecker)
    }
  }

  implicit val fakeRequest: RequestHeader = FakeRequest("GET", "http://hat.hubofallthings.net")
  implicit val ownerImplicit: HatUser = owner
}
