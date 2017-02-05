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
 * 2 / 2017
 */

package org.hatdex.hat.utils

import javax.inject.{ Inject, Named }

import play.api.mvc.{ Filter, RequestHeader, Result }
import play.api.Logger
import play.api.routing.Router.Tags

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import javax.inject.Inject

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.Materializer
import org.hatdex.hat.resourceManagement.actors.HatServerProviderActor
import play.api.http.DefaultHttpFilters
import play.filters.gzip.GzipFilter
import scala.concurrent.duration._

class Filters @Inject() (
  gzip: GzipFilter,
  log: LoggingFilter
) extends DefaultHttpFilters(gzip, log)

class LoggingFilter @Inject() (@Named("hatServerProviderActor") serverProviderActor: ActorRef, implicit val mat: Materializer) extends Filter {
  val logger = Logger("http")

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

    val startTime = System.currentTimeMillis
    implicit val timeout: akka.util.Timeout = 200 milliseconds

    for {
      result <- nextFilter(requestHeader)
      activeHats <- serverProviderActor.ask(HatServerProviderActor.GetHatServersActive()).mapTo[HatServerProviderActor.HatServersActive]
    } yield {
      val endTime = System.currentTimeMillis
      val requestTime = endTime - startTime

      logger.info(s"[${requestHeader.method}:${requestHeader.host}${requestHeader.uri}] [${result.header.status}] [TIME ${requestTime}ms] [HATs ${activeHats.active}]")

      result.withHeaders("Request-Time" -> requestTime.toString)
    }
  }
}