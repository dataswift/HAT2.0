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

import javax.inject.Inject

import akka.stream.Materializer
import play.api.Environment
import play.api.http.DefaultHttpFilters
import play.api.mvc._
import play.filters.cors.CORSFilter
import play.filters.gzip.GzipFilter

import scala.concurrent.{ ExecutionContext, Future }

class Filters @Inject() (
    tlsFilter: TLSFilter,
    gzip: GzipFilter,
    corsFilter: CORSFilter,
    log: LoggingFilter) extends DefaultHttpFilters(log, tlsFilter, gzip, corsFilter)

class DevFilters @Inject() (
    gzip: GzipFilter,
    corsFilter: CORSFilter,
    log: LoggingFilter) extends DefaultHttpFilters(log, gzip, corsFilter)

class TLSFilter @Inject() (
    implicit
    val mat: Materializer, ec: ExecutionContext, env: Environment) extends Filter {
  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    if (requestHeader.headers.get("X-Forwarded-Proto").getOrElse("http") != "https" && env.mode == play.api.Mode.Prod) {
      if (requestHeader.method == "GET") {
        Future.successful(Results.MovedPermanently("https://" + requestHeader.host + requestHeader.uri))
      }
      else {
        Future.successful(Results.BadRequest("This service requires strict transport security"))
      }
    }
    else {
      nextFilter(requestHeader).map(_.withHeaders("Strict-Transport-Security" -> "max-age=31536000"))
    }
  }
}
