/*
 * Copyright (C) 2019 HAT Data Exchange Ltd
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
 * Written by Terry Lee <terry.lee@hatdex.org>
 * 2 / 2019
 */

package org.hatdex.hat.api.service

import java.util.UUID

import javax.inject.Inject
import play.api.Logger

import scala.concurrent.Future

class LogService @Inject() (implicit val ec: DalExecutionContext) {
  val logger = Logger(this.getClass)

  def logAction(hat: String, actionCode: String, message: Option[String], logGroup: Option[String], applicationData: Option[(String, String)]): Future[Unit] = {
    Future {
      val logId = UUID.randomUUID()
      val applicationDataLog = applicationData match {
        case Some(appData) => s"[${appData._1}]@[${appData._2}]"
        case None          => "[_]@[_]"
      }
      logger.info(s"[${logGroup.getOrElse("STATS")}] [$hat] [$logId] [$actionCode] [${message.getOrElse("")}] $applicationDataLog")
    }
  }
}
