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

package org.hatdex.hat.utils

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

object FutureTransformations {
  def transform[A](o: Option[Future[A]])(implicit ec: ExecutionContext): Future[Option[A]] =
    o.map(f => f.map(Option(_))).getOrElse(Future.successful(None))

  def transform[A](o: Option[Future[Option[A]]]): Future[Option[A]] =
    o.getOrElse(Future.successful(None))

  def transform[A](t: Try[A])(implicit ec: ExecutionContext): Future[A] = {
    Future {
      t
    }.flatMap {
      case Success(s)     => Future.successful(s)
      case Failure(error) => Future.failed(error)
    }
  }

  def futureToFutureTry[T](f: Future[T])(implicit ec: ExecutionContext): Future[Try[T]] =
    f.map(x ⇒ Success(x))
      .recover({
        case x ⇒ Failure(x)
      })
}
