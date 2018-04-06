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

import javax.inject.Inject

import akka.actor.{ Actor, PoisonPill, Stash }
import akka.pattern.pipe
import com.google.inject.assistedinject.Assisted
import org.hatdex.hat.resourceManagement.{ HatServerDiscoveryException, HatServerProvider }
import org.hatdex.hat.she.models.FunctionConfiguration
import org.joda.time.DateTime
import play.api.Logger

import scala.concurrent.{ ExecutionContext, Future }

object FunctionExecutorActor {
  sealed trait FunctionExecutorActorMessage
  case class Execute(since: Option[DateTime]) extends FunctionExecutorActorMessage
  case class ExecutionFinished() extends FunctionExecutorActorMessage
  case class ExecutionFailed(e: Throwable) extends FunctionExecutorActorMessage

  trait Factory {
    def apply(hat: String, conf: FunctionConfiguration): Actor
  }
}

class FunctionExecutorActor @Inject() (
    @Assisted hat: String,
    @Assisted conf: FunctionConfiguration,
    hatServerProvider: HatServerProvider,
    functionService: FunctionService)(
    implicit
    val ec: ExecutionContext) extends Actor with Stash {

  private val logger = Logger(this.getClass)

  import FunctionExecutorActor._

  def receive: Receive = {
    case Execute(since) =>
      logger.debug(s"RECEIVE Execute, executing")
      stash()
      startExecution(since).pipeTo(self)
      context.become(executing)

    case message =>
      logger.warn(s"RECEIVE Received unknown message: $message")
  }

  def executing: Actor.Receive = {
    case Execute(_) =>
      logger.debug(s"EXECUTING Execute, stashing")
      stash()
    case ExecutionFinished() =>
      logger.debug(s"EXECUTING ExecutionFinished, stashing")
      context.become(finished)
      unstashAll()
      self ! PoisonPill
    case ExecutionFailed(error) =>
      context.become(failed(error))
      unstashAll()
      self ! PoisonPill
    case message =>
      logger.warn(s"EXECUTING Received unknown message: $message")
  }

  def finished: Actor.Receive = {
    case message =>
      logger.debug(s"FINISHED received a message: $message")
      sender ! ExecutionFinished()
  }

  def failed(error: Throwable): Actor.Receive = {
    case message =>
      logger.debug(s"FINISHED received a message: $message")
      sender ! ExecutionFailed(error)
  }

  def startExecution(since: Option[DateTime]): Future[FunctionExecutorActorMessage] = {
    hatServerProvider.retrieve(hat)
      .flatMap {
        _.map { implicit hatServer =>
          functionService.run(conf, since)(hatServer.db)
            .map(_ => ExecutionFinished())
            .recover {
              case e => ExecutionFailed(e)
            }
        } getOrElse {
          Future.successful(ExecutionFailed(new HatServerDiscoveryException(s"HAT $hat discovery failed for function execution")))
        }
      }
  }
}
