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

package org.hatdex.hat.she.service

import com.google.inject.{ AbstractModule, Provides }
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.HATTestContext
import org.hatdex.hat.api.models.EndpointDataBundle
import org.hatdex.hat.resourceManagement.FakeHatConfiguration
import org.hatdex.hat.she.functions.DataFeedDirectMapper
import org.hatdex.hat.she.models._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.{ ExecutionContext, Future }

trait FunctionServiceContext extends HATTestContext {

  /**
   * A fake Guice module.
   */
  class CustomisedFakeModule extends AbstractModule with ScalaModule {
    def configure() = {
    }

    @Provides
    def provideFunctionExecutableRegistry(): FunctionExecutableRegistry = {
      new FunctionExecutableRegistry(Seq(registeredFunction, registeredDummyFunction, registeredDummyFunctionAvailable))
    }
  }

  override lazy val application: Application = new GuiceApplicationBuilder()
    .configure(FakeHatConfiguration.config)
    .overrides(new FakeModule)
    .overrides(new ExtrasModule)
    .overrides(new CustomisedFakeModule)
    .build()

  val dummyFunctionConfiguration = FunctionConfiguration("test-dummy-function", "Dummy Function",
    FunctionTrigger.TriggerIndividual(), available = false, enabled = false,
    dataBundle = EndpointDataBundle("test-data-feed-dummy-mapper", Map()),
    None)

  val dummyFunctionConfigurationAvailable = FunctionConfiguration("test-dummy-function-available", "Dummy Function",
    FunctionTrigger.TriggerIndividual(), available = true, enabled = false,
    dataBundle = EndpointDataBundle("test-data-feed-dummy-mapper-vailable", Map()),
    None)

  val dummyFunctionConfigurationUpdated = FunctionConfiguration("test-dummy-function", "Updated Function",
    FunctionTrigger.TriggerIndividual(), available = false, enabled = true,
    dataBundle = EndpointDataBundle("test-data-feed-dummy-mapper", Map()),
    None)

  val unavailableFunctionConfiguration = FunctionConfiguration("test-test-unavailable-function", "Unavailable Function",
    FunctionTrigger.TriggerIndividual(), available = false, enabled = false,
    dataBundle = EndpointDataBundle("test-unavailable-function-bundler", Map()),
    None)

  val registeredFunction = new DataFeedDirectMapper()
  val registeredDummyFunction: FunctionExecutable = new DummyFunctionExecutable(dummyFunctionConfiguration)
  val registeredDummyFunctionAvailable: FunctionExecutable = new DummyFunctionExecutable(dummyFunctionConfigurationAvailable)
}

class DummyFunctionExecutable(conf: FunctionConfiguration) extends FunctionExecutable {
  val configuration = conf

  override def execute(configuration: FunctionConfiguration, request: Request)(implicit ec: ExecutionContext): Future[Seq[Response]] = {
    Future.failed(new RuntimeException("Dummy Function"))
  }
}
