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

import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.resourceManagement.HatServer
import play.api.UsefulException
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import org.hatdex.hat.phata.views

import scala.util.Try

abstract class Mailer @Inject() (configuration: play.api.Configuration, ms: MailService) {

  import scala.language.implicitConversions

  implicit def html2String(html: Html): String = html.toString

  def serverErrorNotify(request: RequestHeader, exception: UsefulException)(implicit m: Messages): Unit

  def serverExceptionNotify(request: RequestHeader, exception: Throwable)(implicit m: Messages): Unit
}

class HatMailer @Inject() (configuration: play.api.Configuration, ms: MailService) extends Mailer(configuration, ms) {
  def serverErrorNotify(request: RequestHeader, exception: UsefulException)(implicit m: Messages): Unit = {
    // wrap any errors
    Try {
      val emailFrom = configuration.getString("play.mailer.from").get
      val adminEmails = configuration.getStringSeq("exchange.admin").getOrElse(Seq())
      ms.sendEmailAsync(adminEmails: _*)(
        subject = s"HAT server ${request.host} errorr #${exception.id}",
        bodyHtml = views.html.mails.emailServerError(request, exception),
        bodyText = views.html.mails.emailServerError(request, exception).toString()
      )
    }
  }

  def serverExceptionNotify(request: RequestHeader, exception: Throwable)(implicit m: Messages): Unit = {
    // wrap any errors
    Try {
      val emailFrom = configuration.getString("play.mailer.from").get
      val adminEmails = configuration.getStringSeq("exchange.admin").getOrElse(Seq())
      ms.sendEmailAsync(adminEmails: _*)(
        subject = s"HAT server ${request.host} error: ${exception.getMessage} for ${request.path + request.rawQueryString}",
        bodyHtml = views.html.mails.emailServerThrowable(request, exception),
        bodyText = views.html.mails.emailServerThrowable(request, exception).toString()
      )
    }
  }

  def passwordReset(email: String, user: HatUser, resetLink: String)(implicit m: Messages, server: HatServer): Unit = {
    Try {
      val emailFrom = configuration.getString("play.mailer.from").get
      ms.sendEmailAsync(email)(
        subject = s"HAT ${server.hatName}.${server.domain} - reset your password",
        bodyHtml = views.html.mails.emailPasswordReset(user, resetLink),
        bodyText = views.txt.mails.emailPasswordReset(user, resetLink).toString()
      )
    }
  }
}

