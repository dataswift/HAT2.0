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
 * 11 / 2017
 */

package org.hatdex.hat.she.service

import javax.inject.{ Inject, Singleton }
import akka.actor.{ Actor, ActorNotFound, ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.util.Timeout
import akka.{ Done, NotUsed }
import org.hatdex.hat.api.service.monitoring.HatDataEventBus
import org.hatdex.hat.api.service.monitoring.HatDataEventBus.DataCreatedEvent
import org.hatdex.hat.resourceManagement.{ HatServerDiscoveryException, HatServerProvider }
import org.hatdex.hat.she.models.{ FunctionConfiguration, FunctionTrigger }
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.InjectedActorSupport

import scala.concurrent.duration.{ FiniteDuration, _ }
import scala.concurrent.{ ExecutionContext, Future }

class FunctionTriggerLogger extends Actor {
  private val logger: Logger = Logger(this.getClass)
  def receive: Receive = {
    case (hat: String, configuration: FunctionConfiguration, _: Done) =>
      logger.info(s"Successfully executed function ${configuration.name} for $hat")
  }
}

class FunctionExecutionTriggerHandler @Inject() (
    dataEventBus: HatDataEventBus,
    hatServerProvider: HatServerProvider,
    functionService: FunctionService,
    dispatcher: FunctionExecutionDispatcher,
    actorSystem: ActorSystem)(implicit ec: ExecutionContext) {

  private val logger: Logger = Logger(this.getClass)
  private implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  protected def findMatchingFunctions(hat: String, endpoints: Set[String]): Future[Iterable[FunctionConfiguration]] = {
    logger.debug(s"Finding matching functions: $hat for $endpoints")
    hatServerProvider.retrieve(hat) flatMap {
      case Some(hatServer) ⇒
        functionService.all(active = true)(hatServer.db)
          .map(
            _.filter({
              case FunctionConfiguration(_, _, _, _, FunctionTrigger.TriggerPeriodic(period), true, true, _, Some(lastExecution), Seq(), _) if lastExecution.isBefore(DateTime.now().minus(period)) ⇒ true
              case FunctionConfiguration(_, _, _, _, FunctionTrigger.TriggerPeriodic(_), true, true, _, None, Seq(), _) ⇒ true // no execution recoded yet
              case FunctionConfiguration(_, _, _, _, FunctionTrigger.TriggerIndividual(), true, true, _, _, Seq(), _) ⇒ true
              case _ ⇒ false
            })
              .filter(_.dataBundle.flatEndpointQueries.map(_.endpoint).toSet
                .intersect(endpoints)
                .nonEmpty))

      case None ⇒
        Future.failed(new HatServerDiscoveryException(s"HAT $hat discovery failed for function execution"))
    }
  }

  protected val maxHats: Int = 1000
  protected val messageBatch: Int = 100
  protected val messagePeriod: FiniteDuration = 120.seconds
  protected val matchingFunctionParallelism: Int = 10
  protected val functionExecutionParallelism: Int = 10
  protected implicit val functionExecutionTimeout: FiniteDuration = 5.minutes

  protected val functionTriggerLogger: ActorRef = actorSystem.actorOf(Props[FunctionTriggerLogger])

  protected val functionTriggerFlow: Flow[(String, Set[String]), (String, FunctionConfiguration, Done), _] = Flow[(String, Set[String])]
    .groupBy[String](maxHats, (event: (String, Set[String])) => event._1)
    .groupedWithin(messageBatch, messagePeriod)
    .map(d => d.reduce((l, r) => l._1 -> (l._2 ++ r._2)))
    .mapAsyncUnordered[Iterable[(String, FunctionConfiguration)]](matchingFunctionParallelism)(
      d => findMatchingFunctions(d._1, d._2).map(c => c.map((d._1, _))))
    .mapConcat[(String, FunctionConfiguration)](_.asInstanceOf[scala.collection.immutable.Iterable[(String, FunctionConfiguration)]])
    .mergeSubstreams
    .mapAsyncUnordered(functionExecutionParallelism)(d => dispatcher.trigger(d._1, d._2).map((d._1, d._2, _)))

  // A flow that chunks information about data coming through into separate substreams to pass through the bounded function trigger flow
  // mitigates th issue with groupBy needing to know maximum number of different keys (K) by ensuring that no more than K items
  // will go through the subflow
  protected val dataEventShaperFlow: Flow[(String, Set[String]), (String, FunctionConfiguration, Done), _] = Flow[(String, Set[String])]
    .zipWithIndex
    .splitAfter(i ⇒ i._2 % maxHats == 0)
    .map(_._1)
    .via(functionTriggerFlow)
    .mergeSubstreams

  protected val triggerStream: ActorRef = Source.actorRef[DataCreatedEvent](bufferSize = 1000, OverflowStrategy.dropNew)
    .collect({
      case DataCreatedEvent(hat, _, _, _, data) => (hat, data.map(_.endpoint).toSet) // only interested in new data coming in
    })
    .via(dataEventShaperFlow)
    .to(Sink.actorRef(functionTriggerLogger, NotUsed))
    .run()

  logger.info("Function Executor Trigger Handler starting")
  dataEventBus.subscribe(triggerStream, classOf[HatDataEventBus.DataCreatedEvent])
}

@Singleton
class FunctionExecutionDispatcher @Inject() (
    functionExecutorActorFactory: FunctionExecutorActor.Factory,
    actorSystem: ActorSystem) extends InjectedActorSupport {
  private val logger = Logger(this.getClass)

  def trigger(hat: String, conf: FunctionConfiguration)(implicit timeout: FiniteDuration, ec: ExecutionContext): Future[Done] = {
    logger.debug(s"Triggered function ${conf.name} by $hat")
    implicit val resultTimeout: Timeout = timeout
    doFindOrCreate(hat, conf, timeout) flatMap { actor =>
      actor.ask(FunctionExecutorActor.Execute(None)) map {
        case _: FunctionExecutorActor.ExecutionFinished =>
          logger.debug(s"Finished executing function ${conf.name} by $hat")
          Done
        case FunctionExecutorActor.ExecutionFailed(e) =>
          throw e
      }
    }
  }

  private val maxAttempts = 3
  def doFindOrCreate(hat: String, conf: FunctionConfiguration, timeout: FiniteDuration, depth: Int = 0)(implicit ec: ExecutionContext): Future[ActorRef] = {
    val actorName = s"function:$hat:${conf.name}"
    if (depth >= maxAttempts) {
      logger.error(s"Function executor actor for $actorName not resolved")
      throw new RuntimeException(s"Can not create actor for executor $actorName and reached max attempts of $maxAttempts")
    }
    val selection = s"/user/$actorName"

    actorSystem.actorSelection(selection).resolveOne(timeout) map { hatServerActor =>
      logger.debug(s"HAT function executor actor $selection resolved")
      hatServerActor
    } recoverWith {
      case ActorNotFound(_) =>
        logger.debug(s"HAT function executor actor ($selection) not found, injecting child")
        val functionExecutorActor = actorSystem.actorOf(Props(functionExecutorActorFactory(hat, conf))
          .withDispatcher("she-function-execution-actor-dispatcher"), actorName)
        logger.debug(s"Injected actor $functionExecutorActor")
        doFindOrCreate(hat, conf, timeout, depth + 1)
    }
  }
}
