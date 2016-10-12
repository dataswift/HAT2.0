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
package hatdex.hat.api

import akka.actor.ActorDSL._
import akka.actor.{ ActorLogging, ActorSystem, Props }
import akka.io.IO
import akka.io.Tcp.Bound
import akka.pattern.{ BackoffSupervisor, Backoff }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hatdex.hat.api.actors.{ StatsReporter, ApiService }
import hatdex.hat.dal.SchemaMigration
import spray.can.Http
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  val migration = new SchemaMigration(system)
  migration.run()

  // create and start our service actor
  val dalapiServiceProps = Props[ApiService]

  val supervisor = BackoffSupervisor.props(
    Backoff.onFailure(
      dalapiServiceProps,
      childName = "hatdex.hat.dalapi-service",
      minBackoff = 3.seconds,
      maxBackoff = 30.seconds,
      randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
      ))

  system.actorOf(supervisor, name = "dalapi-service-supervisor")

  val statsReporterSupervisor = BackoffSupervisor.props(
    Backoff.onStop(
      StatsReporter.props,
      childName = "hatdex.marketplace.stats-service",
      minBackoff = 10.seconds,
      maxBackoff = 60.seconds,
      randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
      ))

  system.actorOf(statsReporterSupervisor, name = "stats-service-supervisor")
}