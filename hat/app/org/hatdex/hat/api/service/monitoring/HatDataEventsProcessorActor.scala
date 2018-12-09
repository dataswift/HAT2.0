/*
 * Copyright (C) 2018 HAT Data Exchange Ltd
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
 * Written by Terry Lee <terry.lee@hatdex.org>
 * 12 / 2018
 */
package org.hatdex.hat.api.service.monitoring

import akka.Done
import akka.actor.Actor
import javax.inject.Inject
import org.hatdex.hat.api.models.EndpointData
import org.hatdex.hat.resourceManagement.{ HatServer, HatServerDiscoveryException, HatServerProvider }
import play.api.Logger

import scala.concurrent.{ ExecutionContext, Future }

class HatDataEventsProcessorActor @Inject() (processor: HatDataEventsProcessor) extends Actor {
  private val logger: Logger = Logger(this.getClass)

  import HatDataEventBus._

  def receive: Receive = {
    case d: DataCreatedEvent => processEvents(Seq(d))
    case d: Seq[_]           => processEvents(d)
    case m                   => logger.warn(s"Received something else: $m")
  }

  private def processEvents(events: Seq[Any]) = {
    logger.debug(s"Processing events: ${events}")
    events.filter(_.isInstanceOf[DataCreatedEvent])
      .foreach {
        case event: DataCreatedEvent => processor.sendNotification(event.hat, event.data)
      }
  }
}

class HatDataEventsProcessor @Inject() (
    notifier: HatDataEventsNotifier,
    hatServerProvider: HatServerProvider)(
    implicit
    val ec: ExecutionContext) {

  protected val logger = Logger(this.getClass)

  def sendNotification(hat: String, endpoints: Seq[EndpointData]): Future[Done] = {
    logger.debug(s"Notify Data Created for $hat: $endpoints")
    hatServerProvider.retrieve(hat) flatMap {
      _ map { implicit hatServer: HatServer =>
        notifier.sendNotification(endpoints)
      } getOrElse {
        logger.error(s"No HAT $hat found to report statistics for")
        Future.failed(new HatServerDiscoveryException(s"HAT $hat discovery failed for stats reporting"))
      }
    }
  }
}