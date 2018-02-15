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
 * 2 / 2018
 */

package org.hatdex.hat.api.service.applications

import javax.inject.Inject

import org.hatdex.dex.apiV2.services.DexClient
import org.hatdex.hat.api.models.applications.Application
import org.hatdex.hat.api.service.RemoteExecutionContext
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.WSClient

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

trait TrustedApplicationProvider {
  def applications: Future[Seq[Application]]

  def application(id: String): Future[Option[Application]]
}

class TrustedApplicationProviderDex @Inject() (
    wsClient: WSClient,
    configuration: Configuration,
    cache: AsyncCacheApi)(implicit val rec: RemoteExecutionContext) extends TrustedApplicationProvider {

  private val dexClient = new DexClient(
    wsClient,
    configuration.underlying.getString("exchange.address"),
    configuration.underlying.getString("exchange.scheme"))

  private val dexApplicationsCacheDuration: FiniteDuration = 30.minutes

  def applications: Future[Seq[Application]] = {
    cache.getOrElseUpdate("apps:dexApplications", dexApplicationsCacheDuration) {
      dexClient.applications()
    }
  }

  def application(id: String): Future[Option[Application]] = {
    applications.map(_.find(_.id == id))
  }
}
