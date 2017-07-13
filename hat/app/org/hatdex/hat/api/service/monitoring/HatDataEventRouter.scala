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
 * 5 / 2017
 */

package org.hatdex.hat.api.service.monitoring

import javax.inject.{ Inject, Named }

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.{ ActorMaterializer, OverflowStrategy }

import scala.concurrent.duration._

trait HatDataEventRouter {
  def init(): Unit
}

class HatDataEventRouterImpl @Inject() (
    dataEventBus: HatDataEventBus,
    @Named("hatDataStatsProcessor") statsProcessor: ActorRef,
    implicit val actorSystem: ActorSystem) extends HatDataEventRouter {

  private implicit val materializer = ActorMaterializer()

  init()

  def init(): Unit = {
    dataEventBus.subscribe(buffer(statsProcessor), classOf[HatDataEventBus.DataCreatedEvent])
  }

  private def buffer(target: ActorRef): ActorRef =
    Source.actorRef(bufferSize = 1000, OverflowStrategy.dropNew)
      .groupedWithin(100, 60.seconds)
      .to(Sink.actorRef(target, NotUsed))
      .run()

}
