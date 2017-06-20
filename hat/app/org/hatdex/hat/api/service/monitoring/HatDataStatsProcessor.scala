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
import org.hatdex.hat.api.models.{ DataStats, InboundDataStats, OutboundDataStats }
import org.hatdex.hat.api.service.{ IoExecutionContext, StatsReporter }
import org.hatdex.hat.resourceManagement.{ HatServer, HatServerDiscoveryException, HatServerProvider }

import scala.concurrent.{ ExecutionContext, Future }

class HatDataStatsProcessor @Inject() (
    statsReporter: StatsReporter,
    hatServerProvider: HatServerProvider) extends Actor with ActorLogging {

  import HatDataEventBus._

  def receive: Receive = {
    case d: DataCreatedEvent   => processBatchedStats(Seq(d))
    case d: DataRetrievedEvent => processBatchedStats(Seq(d))
    case d: Seq[_]             => processBatchedStats(d)
    case m                     => log.warning(s"Received something else: $m")
  }

  private def processBatchedStats(d: Seq[Any]) = {
    d.filter(_.isInstanceOf[HatDataEvent])
      .map(_.asInstanceOf[HatDataEvent])
      .groupBy(_.hat) map {
        case (hat, stats) =>
          val aggregated = stats.collect {
            case s: DataCreatedEvent   => computeInboundStats(s)
            case s: DataRetrievedEvent => computeOutboundStats(s)
          }
          hat -> aggregated
      } foreach {
        case (hat, hatStats) =>
          if (hatStats.nonEmpty) {
            publishStats(hat, hatStats)
          }
      }
  }

  def computeInboundStats(event: DataCreatedEvent): InboundDataStats = {
    val endpointStats = JsonStatsService.endpointDataCounts(event.data)
    InboundDataStats("inbound", event.time.toLocalDateTime, event.user,
      endpointStats.toSeq, event.logEntry)
  }

  def computeOutboundStats(event: DataRetrievedEvent): OutboundDataStats = {
    val endpointStats = JsonStatsService.endpointDataCounts(event.data)
    OutboundDataStats("outbound", event.time.toLocalDateTime, event.user,
      event.dataDebit.dataDebitKey, endpointStats.toSeq, event.logEntry)
  }

  def publishStats(hat: String, stats: Iterable[DataStats]): Unit = {
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
