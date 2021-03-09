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

import javax.inject.{Inject, Singleton}

import scala.concurrent.{Future, ExecutionContext}
import scala.util.Try
import akka.stream.Materializer
import com.nimbusds.jose.JWSObject
import com.nimbusds.jwt.JWTClaimsSet
import io.dataswift.log.Component
import io.dataswift.log.play.Slf4jLoggingFilter
import play.api.http.HttpErrorHandler
import play.api.mvc.{Filter, Result, RequestHeader}
import play.api.{Configuration, Logger}

@Singleton
class ActiveHatCounter() {
  // Careful! Mutable state
  private var count: Long = 0

  def get(): Long      = count
  def increase(): Unit = this.synchronized(count += 1)
  def decrease(): Unit = this.synchronized(count -= 1)
}

class LoggingFilter @Inject() (configuration: Configuration,
                               implicit override val mat: Materializer,
                               implicit override val ec: ExecutionContext)
  extends Slf4jLoggingFilter(configuration, Component.Hat)

