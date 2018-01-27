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

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.FakeCache
import org.hatdex.hat.resourceManagement.actors.{ HatServerActor, HatServerProviderActor }
import org.specs2.concurrent.ExecutionEnv
import org.specs2.specification.Scope
import play.api.cache.AsyncCacheApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.test.{ FakeRequest, PlaySpecification }
import play.api.{ Application, Logger }
import play.cache.NamedCacheImpl

import scala.concurrent.duration._

class HatServiceProviderSpec(implicit ee: ExecutionEnv) extends PlaySpecification with HatServerProviderContext {

  val logger = Logger(this.getClass)

  sequential

  "The `retrieve` method" should {
    "Return HAT Server configuration for registered address" in {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
      val service = application.injector.instanceOf[HatServerProvider]

      service.retrieve(request) map { maybeServer =>
        maybeServer must beSome
        val server = maybeServer.get
        server.hatName must be equalTo "hat"
        server.domain must be equalTo "hat.hubofallthings.net"
        server.ownerEmail must be equalTo "user@hat.org"
        server.privateKey.getAlgorithm must be equalTo "RSA"
        server.publicKey.getAlgorithm must be equalTo "RSA"
      } await (1, 30.seconds)
    }

    "Return a failure for an unknown address" in {
      val request = FakeRequest("GET", "http://nohat.hubofallthings.net")
      val service = application.injector.instanceOf[HatServerProvider]

      service.retrieve(request) must throwA[HatServerDiscoveryException].await(1, 30.seconds)
    }
  }
}

trait HatServerProviderContext extends Scope {

  class FakeModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {
    override def configure(): Unit = {
      bindActor[HatServerProviderActor]("hatServerProviderActor")
      bindActorFactory[HatServerActor, HatServerActor.Factory]

      bind[HatDatabaseProvider].to[HatDatabaseProviderConfig]
      bind[HatKeyProvider].to[HatKeyProviderConfig]
      bind[HatServerProvider].to[HatServerProviderImpl]
      bind[AsyncCacheApi].to[FakeCache]
    }
  }

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(FakeHatConfiguration.config)
    .overrides(new FakeModule)
    .build()
}

