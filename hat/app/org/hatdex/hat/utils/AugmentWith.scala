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
 * 4 / 2018
 */

package org.hatdex.hat.utils

import akka.stream.stage.{ GraphStage, GraphStageLogic }
import akka.stream.{ Attributes, FanInShape2, Inlet, Outlet }

final class AugmentWith[T, U](augmentFunction: (T, U) ⇒ Either[T, U]) extends GraphStage[FanInShape2[T, U, T]] {
  private val left = Inlet[T]("left")
  private val right = Inlet[U]("right")
  private val out = Outlet[T]("out")

  override val shape = new FanInShape2(left, right, out)

  override def createLogic(attr: Attributes) = new GraphStageLogic(shape) {
    setHandler(left, eagerTerminateInput)
    setHandler(right, ignoreTerminateInput)
    setHandler(out, eagerTerminateOutput)

    var retainedL: T = _
    var retainedR: U = _

    def dispatch(l: T, r: U): Unit = {
      augmentFunction(l, r).fold(
        augmented ⇒ {
          retainedR = r
          emit(out, augmented, readL)
        },
        _ ⇒ {
          retainedL = l
          readR()
        })

    }

    val dispatchR = dispatch(retainedL, _: U)
    val dispatchL = dispatch(_: T, retainedR)
    val passL = () ⇒ emit(out, retainedL, () ⇒ { passAlong(left, out, doPull = true) })
    val readR = () ⇒ read(right)(dispatchR, passL)
    val readL = () ⇒ read(left)(dispatchL, readR)

    override def preStart(): Unit = {
      // all fan-in stages need to eagerly pull all inputs to get cycles started
      pull(right)
      read(left)(l ⇒ {
        retainedL = l
        readR()
      }, () ⇒ {
        abortReading(right)
      })
    }
  }
}
