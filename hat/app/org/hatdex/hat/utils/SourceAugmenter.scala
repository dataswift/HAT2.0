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

import akka.NotUsed
import akka.stream.scaladsl.{ GraphDSL, Source }
import akka.stream.stage.GraphStage
import akka.stream.{ FanInShape2, SourceShape }

class SourceAugmenter {
  def augment[T, U](source: Source[T, NotUsed], extrasSource: Source[U, NotUsed], augmentFunction: (T, U) ⇒ Either[T, U]): Source[T, NotUsed] = {
    augmentSource(new AugmentWith(augmentFunction), source, extrasSource) { (_, _) => NotUsed }
  }

  private def augmentSource[T, U, MatIn0, MatIn1, Mat](
    combinator: GraphStage[FanInShape2[T, U, T]],
    s0: Source[T, MatIn0],
    s1: Source[U, MatIn1])(combineMat: (MatIn0, MatIn1) => Mat): Source[T, Mat] =

    Source.fromGraph(GraphDSL.create(s0, s1)(combineMat) { implicit builder => (s0, s1) =>
      import GraphDSL.Implicits._
      val merge = builder.add(combinator)
      s0 ~> merge.in0
      s1 ~> merge.in1
      SourceShape(merge.out)
    })
}
