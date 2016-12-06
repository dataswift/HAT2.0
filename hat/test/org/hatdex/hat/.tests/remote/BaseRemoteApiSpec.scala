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

package org.hatdex.hat.tests.remote

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.specs2.main.CommandLine
import org.specs2.matcher.Matcher
import org.specs2.mutable.Specification
import org.specs2.specification.mutable.CommandLineArguments

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class Retry(value: Int) extends AnyVal
case class Timeout(value: FiniteDuration) extends AnyVal

trait BaseRemoteApiSpec extends Specification with CommandLineArguments {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val callRetries = Retry(2)
  implicit val callTimeout = Timeout(5 seconds)

  implicit class MatherWithImplicitValues[A](m: Matcher[A]) {
    def awaitWithTimeout(implicit r: Retry, t: Timeout) = {
      m.await(retries = r.value, timeout = t.value)
    }
  }

  def is(commandLine: CommandLine) = {
    val hatAddress: String = commandLine.value("host").getOrElse("http://localhost:8080")
    val ownerAuthParams = Map(
      "username" -> commandLine.value("username").getOrElse("andrius"),
      "password" -> commandLine.value("password").getOrElse("pa55w0rd")
    )

    testspec(hatAddress, ownerAuthParams)
  }

  def testspec(hatAddress: String, ownerAuthParams: Map[String, String])
}
