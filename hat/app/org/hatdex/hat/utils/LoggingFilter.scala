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

import javax.inject.{ Inject, Singleton }

import akka.stream.Materializer
import play.api.Logger
import play.api.mvc.{ Filter, RequestHeader, Result }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ActiveHatCounter() {
  // Careful! Mutable state
  private var count: Long = 0

  def get(): Long = count
  def increase(): Unit = this.synchronized(count += 1)
  def decrease(): Unit = this.synchronized(count -= 1)
}

class LoggingFilter @Inject() (hatCounter: ActiveHatCounter)(
    implicit
    ec: ExecutionContext,
    val mat: Materializer) extends Filter {
  val logger = Logger("http")

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

    val startTime = System.currentTimeMillis

    nextFilter(requestHeader) map { result =>
      val active = hatCounter.get()
      val requestTime = System.currentTimeMillis - startTime

      logger.info(s"[${requestHeader.remoteAddress}] [${requestHeader.method}:${requestHeader.host}${requestHeader.uri}] [${result.header.status}] TIME [$requestTime]ms HATs [$active]")

      result.withHeaders("Request-Time" -> requestTime.toString)
    }
  }
}