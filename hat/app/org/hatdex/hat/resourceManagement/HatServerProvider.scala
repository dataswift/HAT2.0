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

package org.hatdex.hat.resourceManagement

import java.io.StringWriter
import java.security.interfaces.RSAPublicKey
import javax.inject.{ Inject, Named, Singleton }

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.mohiva.play.silhouette.api.services.DynamicEnvironmentProviderService
import org.bouncycastle.util.io.pem.{ PemObject, PemWriter }
import org.hatdex.hat.resourceManagement.actors.HatServerProviderActor
import org.hatdex.hat.utils.Utils
import play.api.{ Configuration, Logger }
import play.api.cache.CacheApi
import play.api.mvc.Request
import net.ceedubs.ficus.Ficus._

import scala.concurrent.Future
import scala.concurrent.duration._

trait HatServerProvider extends DynamicEnvironmentProviderService[HatServer] {
  def retrieve[B](request: Request[B]): Future[Option[HatServer]]
  def retrieve(hatAddress: String): Future[Option[HatServer]]
  def toString(publicKey: RSAPublicKey): String = {
    val pemObject = new PemObject("PUBLIC KEY", publicKey.getEncoded)
    val stringPemWriter = new StringWriter()
    val pemWriter: PemWriter = new PemWriter(stringPemWriter)
    pemWriter.writeObject(pemObject)
    pemWriter.flush()
    val pemPublicKey = stringPemWriter.toString
    pemPublicKey
  }
}

@Singleton
class HatServerProviderImpl @Inject() (
    configuration: Configuration,
    @Named("hatServerProviderActor") serverProviderActor: ActorRef) extends HatServerProvider {

  import org.hatdex.hat.api.service.IoExecutionContext.ioThreadPool

  private val logger = Logger(this.getClass)
  private val serverInfoExpiry = 1.hour
  private val serverErrorExpiry = 2.minutes

  def retrieve[B](request: Request[B]): Future[Option[HatServer]] = {
    val hatAddress = request.host //.split(':').headOption.getOrElse(request.host)
    retrieve(hatAddress)
  }

  def retrieve(hatAddress: String): Future[Option[HatServer]] = {
    logger.info(s"Retrieving environment for $hatAddress")
    implicit val timeout: Timeout = configuration.underlying.as[FiniteDuration]("resourceManagement.serverProvisioningTimeout")
    (serverProviderActor ? HatServerProviderActor.HatServerRetrieve(hatAddress)) flatMap {
      case server: HatServer =>
        logger.debug(s"Got back server $server")
        Future.successful(Some(server))
      case error: HatServerDiscoveryException =>
        logger.debug(s"Got back error $error")
        Future.failed(error)
      case message =>
        logger.warn(s"Unknown message $message from HAT Server provider actor")
        val error = new HatServerDiscoveryException("Unknown message")
        Future.failed(error)
    } recoverWith {
      case e =>
        logger.warn(s"Error while retrieving HAT Server info: ${e.getMessage}")
        val error = new HatServerDiscoveryException("HAT Server info retrieval failed", e)
        Future.failed(error)
    }
  }

}

