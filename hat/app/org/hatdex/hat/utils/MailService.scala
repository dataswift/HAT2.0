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

import javax.inject.Inject

import akka.actor.ActorSystem
import com.google.inject.ImplementedBy
import play.api.Configuration
import play.api.libs.mailer._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@ImplementedBy(classOf[MailServiceImpl])
trait MailService {
  def sendEmailAsync(recipients: String*)(from: String, subject: String, bodyHtml: String, bodyText: String): Unit
  def sendEmail(recipients: String*)(from: String, subject: String, bodyHtml: String, bodyText: String): Unit
}

class MailServiceImpl @Inject() (
    system: ActorSystem,
    mailerClient: MailerClient,
    val conf: Configuration,
    implicit val ec: ExecutionContext) extends MailService {

  def sendEmailAsync(recipients: String*)(from: String, subject: String, bodyHtml: String, bodyText: String): Unit = {
    system.scheduler.scheduleOnce(100.milliseconds) {
      sendEmail(recipients: _*)(from, subject, bodyHtml, bodyText)
    }
  }

  def sendEmail(recipients: String*)(from: String, subject: String, bodyHtml: String, bodyText: String): Unit =
    mailerClient.send(Email(subject, from, recipients, Some(bodyText), Some(bodyHtml)))
}