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

import java.util.Comparator
import javax.inject.Inject

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorNotFound, ActorRef, Props, Scheduler, Stash}
import akka.event.{EventBus, LookupClassification}
import akka.pattern.{Backoff, BackoffSupervisor}
import com.google.inject.Singleton
import com.google.inject.assistedinject.Assisted
import org.hatdex.hat.api.models.{EndpointData, PropertyQuery, EndpointDataBundle}
import org.hatdex.hat.resourceManagement.{HatServer, HatServerProvider}
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try
import akka.pattern.pipe
import akka.stream.{OverflowStrategy, ThrottleMode}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Index


case class HatDataEvent(hat: String, eventType: String, data: EndpointData)
case class HatDataSubscriber(hat: HatServer)

class HatDataEventRouter @Inject() (
  hatServerProvider: HatServerProvider,
  dataEventBus: HatDataEventBus
) extends Actor with ActorLogging with LookupClassification {
  type Classifier = String
  type Subscriber = ActorRef
  type Event = String

  def ensureSubscriber(event: String) = {
    val i = subscribers.valueIterator(classify(event))
    if (!i.hasNext) {
      subscribe(throttler, event)
    }
  }

  def receive: Receive = {
    case d: HatDataEvent =>
      ensureSubscriber(d.hat)
      publish(d.hat)
      dataEventBus publish d
  }

  def throttler(target:ActorRef): ActorRef =
    Source.actorRef(bufferSize = 10, OverflowStrategy.dropNew)
      .throttle(1, 60.seconds, 1, ThrottleMode.Shaping)
      .to(Sink.actorRef(target, NotUsed))
      .run()
}

class EndpointSubscriberManagerActor @Inject() (
  subscriberFactory: InjectedEndpointSubscriberActor.Factory,
  dataEventBus: HatDataEventBus)
  extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContext = context.dispatcher
  val scheduler: Scheduler = context.system.scheduler

  def receive: Receive = {
    case HatDataEvent(hat, eventType, data) =>
      self ! HatDataSubscriber(hat)
    case HatDataSubscriber(hat) =>
      startSyncerActor(hat, hat.hatName) map { actor =>
        log.debug(s"Actor ${actor.path} created for data bundle ${hat.hatName}")
      } recover { case e =>
        log.warning(s"Creating actor for ${hat.hatName} failed: ${e.getMessage}")
      }
  }

  private def startSyncerActor(hat: HatServer, actorKey: String): Future[ActorRef] = {
    context.actorSelection(s"$actorKey-supervisor").resolveOne(5.seconds) map { subscriberActorSupervisor =>
      subscriberActorSupervisor
    } recover {
      case ActorNotFound(selection) =>
        log.warning(s"Starting syncer actor $actorKey with supervisor - no existing $selection")

        val supervisor = BackoffSupervisor.props(
          Backoff.onStop(
            Props(subscriberFactory(hat)),
            childName = actorKey,
            minBackoff = 3.seconds,
            maxBackoff = 30.seconds,
            randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
          ))

        context.actorOf(supervisor, s"$actorKey-supervisor")
    }
  }
}

class EndpointSubscriberActor(wSClient: WSClient, hat: String) extends Actor with ActorLogging {
  def receive: Receive = {
    case HatDataEvent(hat, eventType, data) =>
      EndpointSubscriberService.matchesBundle(data, bundle)
      log.warning(s"Not implemented")
  }
}

object InjectedEndpointSubscriberActor {
  trait Factory {
    def apply(hat: HatServer): Actor
  }

  def props(wsClient: WSClient, hat: String, bundle: EndpointDataBundle): Props =
    Props(new EndpointSubscriberActor(wsClient, hat))
}

class InjectedEndpointSubscriberActor @Inject() (
  wsClient: WSClient,
  @Assisted hat: String)
  extends EndpointSubscriberActor(wsClient, hat)

/**
 * Publishes the payload of the MsgEnvelope when the topic of the
 * MsgEnvelope equals the String specified when subscribing.
 */
@Singleton
class HatDataEventBus extends EventBus with LookupClassification {
  type Event = HatDataEvent
  type Classifier = String
  type Subscriber = ActorRef

  // is used for extracting the classifier from the incoming events
  override protected def classify(event: Event): Classifier = event.hat

  // will be invoked for each event for all subscribers which registered themselves
  // for the event’s classifier
  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event
  }

  // must define a full order over the subscribers, expressed as expected from
  // `java.lang.Comparable.compare`
  override protected def compareSubscribers(a: Subscriber, b: Subscriber): Int =
  a.compareTo(b)

  // determines the initial size of the index data structure
  // used internally (i.e. the expected number of different classifiers)
  override protected def mapSize: Int = 128

}