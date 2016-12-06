/*
 * Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 10 2016
 */

package org.hatdex.hat.utils

import akka.actor.Scheduler
import akka.pattern.after

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

object FutureRetries {
  def retry[T](f: => Future[T], delays: List[FiniteDuration])(implicit ec: ExecutionContext, s: Scheduler): Future[T] = {
    f recoverWith { case _ if delays.nonEmpty => after(delays.head, s)(retry(f, delays.tail)) }
  }

  def withDefault(delays: List[FiniteDuration], retries: Int, default: FiniteDuration): List[FiniteDuration] = {
    if (delays.length > retries) {
      delays take retries
    }
    else {
      delays ++ List.fill(retries - delays.length)(default)
    }
  }

  def withJitter(delays: List[FiniteDuration], maxJitter: Double, minJitter: Double): List[FiniteDuration] = {
    delays.map { delay =>
      val jitter = delay * (minJitter + (maxJitter - minJitter) * Random.nextDouble)
      jitter match {
        case d: FiniteDuration => d
        case _                 => delay
      }
    }
  }

  val fibonacci: Stream[FiniteDuration] = 0.seconds #:: 1.seconds #:: (fibonacci zip fibonacci.tail).map { t => t._1 + t._2 }
}
