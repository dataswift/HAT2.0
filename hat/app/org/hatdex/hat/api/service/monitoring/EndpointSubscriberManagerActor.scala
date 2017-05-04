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
import javax.inject.{ Inject, Named }

import akka.NotUsed
import akka.actor.{ Actor, ActorLogging, ActorNotFound, ActorRef, ActorSystem, Props, Scheduler, Stash }
import akka.event.{ EventBus, Logging, LookupClassification, SubchannelClassification }
import akka.pattern.{ Backoff, BackoffSupervisor }
import com.google.inject.Singleton
import com.google.inject.assistedinject.Assisted
import org.hatdex.hat.api.models.{ EndpointData, EndpointDataBundle, PropertyQuery, User }
import org.hatdex.hat.resourceManagement.{ HatServer, HatServerProvider }
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.ws.WSClient

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.util.Try
import akka.pattern.pipe
import akka.stream.{ ActorMaterializer, Attributes, OverflowStrategy, ThrottleMode }
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.{ Index, Subclassification }
import org.hatdex.hat.authentication.models.HatUser

object HatDataEventBus {
  sealed trait HatDataEvent
  case class DataCreatedEvent(hat: String, user: User, logEntry: String, data: Seq[EndpointData]) extends HatDataEvent
  case class HatDataSubscriber(hat: HatServer)
}

/**
 * Publishes the payload of the MsgEnvelope when the topic of the
 * MsgEnvelope equals the String specified when subscribing.
 */
@Singleton
class HatDataEventBus extends EventBus with SubchannelClassification {
  import HatDataEventBus._

  type Event = HatDataEvent
  type Classifier = Class[_ <: HatDataEvent]
  type Subscriber = ActorRef

  protected def compareSubscribers(a: Subscriber, b: Subscriber) = a compareTo b

  /**
   * The logic to form sub-class hierarchy
   */
  override protected implicit val subclassification = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier): Boolean = x == y
    def isSubclass(x: Classifier, y: Classifier): Boolean = y.isAssignableFrom(x)
  }

  /**
   * Publishes the given Event to the given Subscriber.
   *
   * @param event The Event to publish.
   * @param subscriber The Subscriber to which the Event should be published.
   */
  override protected def publish(event: Event, subscriber: Subscriber): Unit = subscriber ! event

  /**
   * Returns the Classifier associated with the given Event.
   *
   * @param event The event for which the Classifier should be returned.
   * @return The Classifier for the given Event.
   */
  override protected def classify(event: Event): Classifier = event.getClass
}

class HatDataStatsProcessor extends Actor with ActorLogging {
  import HatDataEventBus._

  def receive: Receive = {
    case d: Seq[DataCreatedEvent] =>
      log.warning(s"Received data: $d")
      val computedStats = d.groupBy(_.hat) map {
        case (hat, stats) =>
          val computed = stats.flatMap(computeStats)
          hat -> computed
      }

      computedStats.foreach {
        case (hat, hatStats) =>
          publishStats(hat, hatStats)
      }
    case m =>
      log.warning(s"Received something else: $m")
  }

  def computeStats(event: DataCreatedEvent): Iterable[InboundDataStats] = {
    JsonStatsService.endpointDataCounts(event.data, event.user, event.logEntry)
  }

  def publishStats(hat: String, stats: Iterable[InboundDataStats]) = {
    log.error("stats publishing not implemented!")
  }
}

trait HatDataEventRouter {
  def init(): Unit
}

class HatDataEventRouterImpl @Inject() (
    dataEventBus: HatDataEventBus,
    @Named("hatDataStatsProcessor") statsProcessor: ActorRef,
    implicit val actorSystem: ActorSystem) extends HatDataEventRouter {

  private implicit val materializer = ActorMaterializer()

  init()

  def init(): Unit = {
    dataEventBus.subscribe(buffer(statsProcessor), classOf[HatDataEventBus.DataCreatedEvent])
  }

  private def buffer(target: ActorRef): ActorRef =
    Source.actorRef(bufferSize = 100, OverflowStrategy.dropNew)
      .groupedWithin(100, 60.seconds)
      .to(Sink.actorRef(target, NotUsed))
      .run()

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