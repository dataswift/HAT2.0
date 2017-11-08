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

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeEach
import play.api.Logger
import play.api.test.PlaySpecification

import scala.concurrent.duration._

class FunctionServiceSpec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito with FunctionServiceContext with BeforeEach {

  val logger = Logger(this.getClass)

  def before: Unit = {
    await(databaseReady)(10.seconds)
  }

  sequential

  "The `get` method" should {
    "return `None` when no such function exists" in {
      val service = application.injector.instanceOf[FunctionService]
      service.get("non-existing-function") must beNone.await(3, 10.seconds)
    }

    "return saved function by name" in {
      val service = application.injector.instanceOf[FunctionService]

      val saved = for {
        _ <- service.save(unavailableFunctionConfiguration)
        saved <- service.get(unavailableFunctionConfiguration.name)
      } yield saved

      saved.map { mSaved =>
        mSaved must beSome
        val c = mSaved.get
        c.name must be equalTo unavailableFunctionConfiguration.name
        c.available must beFalse
      }.await(3, 10.seconds)
    }

    "return available, not saved function by name" in {
      val service = application.injector.instanceOf[FunctionService]

      val saved = for {
        _ <- service.save(dummyFunctionConfiguration)
        available <- service.get(registeredFunction.configuration.name)
      } yield available

      saved.map { mAvailable =>
        mAvailable must beSome
        val c2 = mAvailable.get
        c2.name must be equalTo registeredFunction.configuration.name
        c2.available must beTrue
      }.await(3, 10.seconds)
    }
  }

  "The `saved` method" should {
    "List saved functions only" in {
      val service = application.injector.instanceOf[FunctionService]

      val all = for {
        _ <- service.save(unavailableFunctionConfiguration)
        all <- service.saved()
      } yield all

      all.map { functions =>
        functions.length must be equalTo 1
        val dummy = functions.find(_.name == unavailableFunctionConfiguration.name)
        dummy must beSome
        dummy.get.available must beFalse
      }.await(3, 10.seconds)
    }

    "List multiple saved functions" in {
      val service = application.injector.instanceOf[FunctionService]

      val all = for {
        _ <- service.save(unavailableFunctionConfiguration)
        _ <- service.save(registeredFunction.configuration)
        all <- service.saved()
      } yield all

      all.map { functions =>
        functions.length must be equalTo 2
        val dummy = functions.find(_.name == unavailableFunctionConfiguration.name)
        dummy must beSome
        dummy.get.available must beFalse

        val available = functions.find(_.name == registeredFunction.configuration.name)
        available must beSome
        available.get.available must beTrue
      }.await(3, 10.seconds)
    }
  }

  "The `all` method" should {
    "List all functions, bot available and previously saved" in {
      val service = application.injector.instanceOf[FunctionService]

      val all = for {
        _ <- service.save(dummyFunctionConfiguration)
        all <- service.all(active = false)
      } yield all

      all.map { functions =>
        val dummy = functions.find(_.name == dummyFunctionConfiguration.name)
        dummy must beSome
        dummy.get.available must beFalse

        val available = functions.find(_.name == registeredFunction.configuration.name)
        available must beSome
        available.get.available must beTrue
      }.await(3, 10.seconds)
    }
  }

  "The `save` method" should {
    "return the saved function configuration" in {
      val service = application.injector.instanceOf[FunctionService]

      service.save(dummyFunctionConfiguration)
        .map { c =>
          c.name must be equalTo dummyFunctionConfiguration.name
          c.available must beFalse
        }.await(3, 10.seconds)
    }

    "update function configuration with matching name, keeping configuration paramters as higher priority" in {
      val service = application.injector.instanceOf[FunctionService]

      val saved = for {
        _ <- service.save(dummyFunctionConfiguration)
        c <- service.save(dummyFunctionConfigurationUpdated)
      } yield c

      saved.map { c =>
        c.name must be equalTo dummyFunctionConfiguration.name
        c.enabled must beTrue
        c.description must be equalTo dummyFunctionConfiguration.description
      }.await(3, 10.seconds)
    }
  }

}

