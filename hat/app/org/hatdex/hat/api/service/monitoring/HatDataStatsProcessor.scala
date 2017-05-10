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

import javax.inject.Inject

import akka.actor.{ Actor, ActorLogging }
import org.hatdex.hat.api.models.InboundDataStats
import org.hatdex.hat.api.service.{ IoExecutionContext, StatsReporter }
import org.hatdex.hat.resourceManagement.{ HatServer, HatServerDiscoveryException, HatServerProvider }

import scala.concurrent.{ ExecutionContext, Future }

class HatDataStatsProcessor @Inject() (
    statsReporter: StatsReporter,
    hatServerProvider: HatServerProvider) extends Actor with ActorLogging {

  import HatDataEventBus._

  def receive: Receive = {
    case d: DataCreatedEvent =>
      val computedStats = computeStats(d)
      publishStats(d.hat, computedStats)

    case d: Seq[_] =>
      d.filter(_.isInstanceOf[DataCreatedEvent])
        .map(_.asInstanceOf[DataCreatedEvent])
        .groupBy(_.hat) map {
          case (hat, stats) =>
            hat -> stats.flatMap(computeStats)
        } foreach {
          case (hat, hatStats) =>
            publishStats(hat, hatStats)
        }

    case m =>
      log.warning(s"Received something else: $m")
  }

  def computeStats(event: DataCreatedEvent): Iterable[InboundDataStats] = {
    JsonStatsService.endpointDataCounts(event.data, event.user, event.logEntry)
  }

  def publishStats(hat: String, stats: Iterable[InboundDataStats]): Unit = {
    implicit val ec: ExecutionContext = IoExecutionContext.ioThreadPool
    log.debug(s"Publish stats for $hat: $stats")

    hatServerProvider.retrieve(hat) foreach {
      _ map { implicit hatServer: HatServer =>
        statsReporter.reportStatistics(stats.toSeq)
      } getOrElse {
        log.error(s"No HAT $hat found to report statistics for")
        Future.failed(new HatServerDiscoveryException(s"HAT $hat discovery failed for stats reporting"))
      }
    }
  }
}

//class EndpointSubscriberManagerActor @Inject() (
//  subscriberFactory: InjectedEndpointSubscriberActor.Factory,
//  dataEventBus: HatDataEventBus)
//  extends Actor with ActorLogging {
//
//  implicit val executionContext: ExecutionContext = context.dispatcher
//  val scheduler: Scheduler = context.system.scheduler
//
//  def receive: Receive = {
//    case HatDataEvent(hat, eventType, data) =>
//      self ! HatDataSubscriber(hat)
//    case HatDataSubscriber(hat) =>
//      startSyncerActor(hat, hat.hatName) map { actor =>
//        log.debug(s"Actor ${actor.path} created for data bundle ${hat.hatName}")
//      } recover { case e =>
//        log.warning(s"Creating actor for ${hat.hatName} failed: ${e.getMessage}")
//      }
//  }
//
//  private def startSyncerActor(hat: HatServer, actorKey: String): Future[ActorRef] = {
//    context.actorSelection(s"$actorKey-supervisor").resolveOne(5.seconds) map { subscriberActorSupervisor =>
//      subscriberActorSupervisor
//    } recover {
//      case ActorNotFound(selection) =>
//        log.warning(s"Starting syncer actor $actorKey with supervisor - no existing $selection")
//
//        val supervisor = BackoffSupervisor.props(
//          Backoff.onStop(
//            Props(subscriberFactory(hat)),
//            childName = actorKey,
//            minBackoff = 3.seconds,
//            maxBackoff = 30.seconds,
//            randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
//          ))
//
//        context.actorOf(supervisor, s"$actorKey-supervisor")
//    }
//  }
//}
//
//class EndpointSubscriberActor(wSClient: WSClient, hat: String) extends Actor with ActorLogging {
//  def receive: Receive = {
//    case HatDataEvent(hat, eventType, data) =>
//      EndpointSubscriberService.matchesBundle(data, bundle)
//      log.warning(s"Not implemented")
//  }
//}
//
//object InjectedEndpointSubscriberActor {
//  trait Factory {
//    def apply(hat: HatServer): Actor
//  }
//
//  def props(wsClient: WSClient, hat: String, bundle: EndpointDataBundle): Props =
//    Props(new EndpointSubscriberActor(wsClient, hat))
//}
//
//class InjectedEndpointSubscriberActor @Inject() (
//  wsClient: WSClient,
//  @Assisted hat: String)
//  extends EndpointSubscriberActor(wsClient, hat)