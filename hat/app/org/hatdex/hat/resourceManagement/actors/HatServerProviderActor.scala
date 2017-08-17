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

package org.hatdex.hat.resourceManagement.actors

import javax.inject.Inject

import akka.actor.{ Props, _ }
import akka.util.Timeout
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.libs.concurrent.InjectedActorSupport

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._

class HatServerProviderActor @Inject() (hatServerActorFactory: HatServerActor.Factory, configuration: Configuration) extends Actor with ActorLogging with InjectedActorSupport {
  import HatServerProviderActor._
  import org.hatdex.hat.api.service.IoExecutionContext.ioThreadPool

  private val activeServers = mutable.HashMap[String, ActorRef]()
  private implicit val hatServerTimeout: Timeout = configuration.underlying.as[FiniteDuration]("resourceManagement.serverProvisioningTimeout")
  private var hatsActive: Long = 0L

  def receive: Receive = {
    case HatServerRetrieve(hat) =>
      log.debug(s"Retrieve HAT server $hat for $sender")
      val retrievingSender = sender
      //      getHatServerActor(hat) map { hatServerActor =>
      //        log.debug(s"Got HAT server provider actor, forwarding retrieval message with sender $sender $retrievingSender")
      getHatServerActor(hat) tell (HatServerActor.HatRetrieve(), retrievingSender)
    //      } recover {
    //        case e =>
    //          log.warning(s"Error while getting HAT server provider actor: ${e.getMessage}")
    //      }

    case HatServerStarted(_) =>
      hatsActive += 1
      log.debug(s"Total HATs active: $hatsActive")

    case HatServerStopped(_) =>
      hatsActive -= 1

    case GetHatServersActive() =>
      sender ! HatServersActive(hatsActive)

    case message =>
      log.debug(s"Received unexpected message $message")
      log.debug(s"Total HATs active: $hatsActive")
  }

  private def getHatServerActor(hat: String): ActorSelection = {
    val actorSelection = context.actorSelection(s"/user/hatServerProviderActor/hat:$hat")
    actorSelection.resolveOne(hatServerTimeout.duration / 2) map { hatServerActor =>
      log.debug(s"HAT server $hat actor resolved")
      hatServerActor
    } recover {
      case ActorNotFound(selection) =>
        log.debug(s"HAT server $hat actor ($selection) not found, injecting child")
        val hatServerActor = injectedChild(hatServerActorFactory(hat), s"hat:$hat", props = (props: Props) => props.withDispatcher("hat-server-provider-actor-dispatcher"))
        activeServers(hat) = hatServerActor
        log.debug(s"Injected actor $hatServerActor")
        hatServerActor
      case e =>
        log.warning(s"HAT server $hat actor error: ${e.getMessage}")
        throw e
    }
    actorSelection
  }

}

object HatServerProviderActor {
  case class HatServerRetrieve(hat: String)

  case class HatServerStarted(hat: String)
  case class HatServerStopped(hat: String)

  case class GetHatServersActive()
  case class HatServersActive(active: Long)
}

