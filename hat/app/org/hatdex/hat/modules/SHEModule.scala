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

package org.hatdex.hat.modules

import com.google.inject.{ AbstractModule, Provides }
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.models.applications.Version
import org.hatdex.hat.she.models.LambdaFunctionExecutable
import org.hatdex.hat.she.service.{ FunctionExecutableRegistry, FunctionExecutionTriggerHandler }
import play.api.{ ConfigLoader, Configuration, Logger }
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.libs.ws.WSClient

import scala.collection.JavaConverters._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._

class SHEModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {
  val logger = Logger(this.getClass)

  def configure() = {
    bind[FunctionExecutionTriggerHandler].asEagerSingleton()
    ()
  }

  implicit val seqFunctionConfigLoader: ConfigLoader[Seq[FunctionConfig]] = new ConfigLoader[Seq[FunctionConfig]] {
    def load(config: Config, path: String): Seq[FunctionConfig] = {
      val configs = config.getConfigList(path).asScala
      logger.warn(s"Got SHE function configs: $configs")
      configs.map { config ⇒
        val f = FunctionConfig(config.getString("id"), Version(config.getString("version")), config.getString("baseUrl"))
        logger.warn(s"Got SHE function configuration: $config -> $f")
        f
      }
    }
  }

  @Provides
  def provideFunctionExecutableRegistry(
    config: Configuration,
    wsClient: WSClient)(implicit ec: ExecutionContext): FunctionExecutableRegistry = {
    val eventuallyFunctionsLoaded = Future.sequence(
      config.get[Seq[FunctionConfig]]("she.functions")
        .map(c ⇒ LambdaFunctionExecutable(wsClient)(c.id, c.version, c.baseUrl)))

    val functionsLoaded = Await.result(eventuallyFunctionsLoaded, 30.seconds)

    new FunctionExecutableRegistry(functionsLoaded)
  }

  case class FunctionConfig(id: String, version: Version, baseUrl: String)
}
