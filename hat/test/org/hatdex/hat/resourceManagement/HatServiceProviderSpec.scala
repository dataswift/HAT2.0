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
 * 11 / 2017
 */

package org.hatdex.hat.resourceManagement

import io.dataswift.test.common.BaseSpec
import org.hatdex.hat.api.HATTestContext
import org.mockito.{ Mockito => MockitoMockito }
import play.api.Logger
import play.api.test.FakeRequest

import scala.concurrent.Await
import scala.concurrent.duration._

class HatServiceProviderSpec extends HATTestContext {

  import scala.concurrent.ExecutionContext.Implicits.global
  val logger: Logger = Logger(this.getClass)

  "The `retrieve` method" should "Return HAT Server configuration for registered address" in {
    val request = FakeRequest("GET", "http://hat.hubofallthings.net")
    val service = application.injector.instanceOf[HatServerProvider]

    val result = service.retrieve(request) map { maybeServer =>
      maybeServer must not be empty
      val server = maybeServer.get
      server.hatName must equal("hat")
      server.domain must equal("hat.hubofallthings.net")
      server.ownerEmail must equal("user@hat.org")
      server.privateKey.getAlgorithm must equal("RSA")
      server.publicKey.getAlgorithm must equal("RSA")
    //there was one(mockLogger).debug(s"Got back server $server")
    }
    Await.result(result, 10.seconds)
  }

  it should "Return HAT Server configuration for a repeated request, cached" in {
    val request = FakeRequest("GET", "http://hat.hubofallthings.net")
    val service = application.injector.instanceOf[HatServerProvider]
    MockitoMockito.reset(mockLogger)

    val result = for {
      _ <- service.retrieve(request)
      maybeServer <- service.retrieve(request)
    } yield {
      maybeServer must not be empty
      val server = maybeServer.get
      server.hatName must equal("hat")
      server.domain must equal("hat.hubofallthings.net")
      server.ownerEmail must equal("user@hat.org")
      server.privateKey.getAlgorithm must equal("RSA")
      server.publicKey.getAlgorithm must equal("RSA")
      //there was no(mockLogger).debug(any)(any)
    }
    Await.result(result, 10.seconds)
  }

  it should "Return a failure for an unknown address" in {
    val request = FakeRequest("GET", "http://nohat.hubofallthings.net")
    val service = application.injector.instanceOf[HatServerProvider]

    try service.retrieve(request)
    catch {
      case (hsde: HatServerDiscoveryException) => true
      case _                                   => fail()
    }
  }
}

// I believe this is no longer required.
// trait HatServerProviderContext {
//   import scala.concurrent.ExecutionContext.Implicits.global
//   //val mockLogger = MockitoSugar.mock[Logger]

//   class FakeModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {
//     override def configure(): Unit = {
//       bindActor[HatServerProviderActor]("hatServerProviderActor")
//       bindActorFactory[HatServerActor, HatServerActor.Factory]

//       bind[HatDatabaseProvider].to[HatDatabaseProviderConfig]
//       bind[HatKeyProvider].to[HatKeyProviderConfig]
//       bind[HatServerProvider].to[HatServerProviderImpl]
//       bind[AsyncCacheApi].to[FakeCache]
//       bind[LoggingProvider].toInstance(new MockLoggingProvider(mockLogger))
//       bind[TrustedApplicationProvider].toInstance(new TestApplicationProvider(Seq()))
//     }

//     @Provides @play.cache.NamedCache("hatserver-cache")
//     def provideHatServerCache(): AsyncCacheApi =
//       new FakeCache()
//   }

//   lazy val application: Application = new GuiceApplicationBuilder()
//     .configure(FakeHatConfiguration.config)
//     .overrides(new FakeModule)
//     .build()
// }
