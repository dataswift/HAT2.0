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

import akka.Done
import akka.actor.ActorSystem
import org.hatdex.hat.api.service.RemoteExecutionContext
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.phata.views
import org.hatdex.hat.resourceManagement.HatServer
import play.api.i18n.Messages
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc.RequestHeader
import play.api.{ Configuration, UsefulException }
import play.twirl.api.Html

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

trait Mailer {
  protected val configuration: Configuration
  protected val system: ActorSystem
  protected val mailerClient: MailerClient

  import scala.language.implicitConversions

  implicit def html2String(html: Html): String = html.toString

  def serverErrorNotify(request: RequestHeader, exception: UsefulException)(implicit m: Messages): Unit

  def serverExceptionNotify(request: RequestHeader, exception: Throwable)(implicit m: Messages): Unit

  def sendEmail(recipients: String*)(from: String, subject: String, bodyHtml: String, bodyText: String)(implicit ec: ExecutionContext): Future[Done] = {
    Future(mailerClient.send(Email(subject, from, recipients, Some(bodyText), Some(bodyHtml))))
      .map(_ => Done)
  }
}

trait HatMailer extends Mailer {
  def serverErrorNotify(request: RequestHeader, exception: UsefulException)(implicit m: Messages): Unit
  def serverExceptionNotify(request: RequestHeader, exception: Throwable)(implicit m: Messages): Unit
  def passwordReset(email: String, user: HatUser, resetLink: String)(implicit m: Messages, server: HatServer): Unit
  def passwordChanged(email: String, user: HatUser)(implicit m: Messages, server: HatServer): Unit
}

class HatMailerImpl @Inject() (
    val configuration: Configuration,
    val system: ActorSystem,
    val mailerClient: MailerClient)(implicit ec: RemoteExecutionContext) extends HatMailer {
  private val emailFrom = configuration.get[String]("play.mailer.from")
  private val adminEmails = configuration.get[Seq[String]]("exchange.admin")

  def serverErrorNotify(request: RequestHeader, exception: UsefulException)(implicit m: Messages): Unit = {
    // wrap any errors
    Try {
      sendEmail(adminEmails: _*)(
        from = emailFrom,
        subject = s"HAT server ${request.host} errorr #${exception.id}",
        bodyHtml = views.html.mails.emailServerError(request, exception),
        bodyText = views.html.mails.emailServerError(request, exception).toString())
    }
  }

  def serverExceptionNotify(request: RequestHeader, exception: Throwable)(implicit m: Messages): Unit = {
    // wrap any errors
    Try {
      sendEmail(adminEmails: _*)(
        from = emailFrom,
        subject = s"HAT server ${request.host} error: ${exception.getMessage} for ${request.path + request.rawQueryString}",
        bodyHtml = views.html.mails.emailServerThrowable(request, exception),
        bodyText = views.html.mails.emailServerThrowable(request, exception).toString())
    }
  }

  def passwordReset(email: String, user: HatUser, resetLink: String)(implicit m: Messages, server: HatServer): Unit = {
    Try {
      sendEmail(email)(
        from = emailFrom,
        subject = s"HAT ${server.domain} - reset your password",
        bodyHtml = views.html.mails.emailPasswordReset(user, resetLink),
        bodyText = views.txt.mails.emailPasswordReset(user, resetLink).toString())
    }
  }

  def passwordChanged(email: String, user: HatUser)(implicit m: Messages, server: HatServer): Unit = {
    Try {
      sendEmail(email)(
        from = emailFrom,
        subject = s"HAT ${server.domain} - password changed",
        bodyHtml = views.html.mails.emailPasswordChanged(user),
        bodyText = views.txt.mails.emailPasswordChanged(user).toString())
    }
  }
}

