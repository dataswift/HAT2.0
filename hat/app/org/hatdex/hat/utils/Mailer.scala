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

import akka.Done
import akka.actor.ActorSystem
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model._
import org.hatdex.hat.api.service.RemoteExecutionContext
import org.hatdex.hat.phata.views
import org.hatdex.hat.resourceManagement.HatServer
import play.api.i18n.{ Lang, Messages, MessagesApi }
import play.api.mvc.RequestHeader
import play.api.{ Configuration, Logger, UsefulException }

import java.nio.charset.StandardCharsets
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import scala.jdk.CollectionConverters._

trait Mailer {
  protected val configuration: Configuration
  protected val system: ActorSystem
  protected val mailerClient: AmazonSimpleEmailService

  private val logger = Logger(getClass)

  val emailFrom: String = configuration.get[String]("mailer.from")
  val mock: Boolean     = configuration.get[Boolean]("mailer.mock")

  def sendEmail(
      recipients: String*
    )(from: String,
      subject: String,
      bodyHtml: String,
      bodyText: String
    )(implicit
      ec: ExecutionContext): Future[Done] =
    if (!mock) {
      val message = new Message(content(subject), new Body(content(bodyText)).withHtml(content(bodyHtml)))
      val request = new SendEmailRequest(from, new Destination(recipients.asJava), message)
      Future(mailerClient.sendEmail(request)).map(_ => Done)
    } else {
      logger.info(s"mocking enabled, not sending email about '$subject' to ${recipients.mkString(", ")}")
      Future.successful(Done)
    }

  private def content(s: String): Content =
    new Content(s).withCharset(StandardCharsets.UTF_8.name())

  def serverErrorNotify(
      request: RequestHeader,
      exception: UsefulException
    )(implicit m: Messages): Unit

  def serverExceptionNotify(
      request: RequestHeader,
      exception: Throwable
    )(implicit m: Messages): Done

}

case class ApplicationMailDetails(
    name: String,
    logo: String,
    url: Option[String])

trait HatMailer extends Mailer {
  def serverErrorNotify(
      request: RequestHeader,
      exception: UsefulException
    )(implicit m: Messages): Unit

  def serverExceptionNotify(
      request: RequestHeader,
      exception: Throwable
    )(implicit m: Messages): Done

  def passwordReset(
      email: String,
      resetLink: String
    )(implicit m: MessagesApi,
      lang: Lang,
      server: HatServer): Done

  def passwordChanged(
      email: String
    )(implicit m: MessagesApi,
      lang: Lang,
      server: HatServer): Done

  def verifyEmail(
      email: String,
      verificationLink: String
    )(implicit m: MessagesApi,
      lang: Lang,
      server: HatServer): Done

  def customWelcomeEmail(
      email: String,
      appName: Option[String],
      appLogoUrl: Option[String],
      verificationLink: String
    )(implicit m: MessagesApi,
      lang: Lang): Done

  def emailVerified(
      email: String,
      loginLink: String
    )(implicit m: MessagesApi,
      lang: Lang,
      server: HatServer): Done
}

class HatMailerImpl @Inject() (
    val configuration: Configuration,
    val system: ActorSystem,
    val mailerClient: AmazonSimpleEmailService
  )(implicit ec: RemoteExecutionContext)
    extends HatMailer {
  private val adminEmails = configuration.get[Seq[String]]("exchange.admin")

  def serverErrorNotify(
      request: RequestHeader,
      exception: UsefulException
    )(implicit m: Messages): Unit = {
    sendEmail(adminEmails: _*)(
      from = emailFrom,
      subject = s"HAT server ${request.host} error #${exception.id}",
      bodyHtml = views.html.mails.emailServerError(request, exception).toString(),
      bodyText = views.html.mails.emailServerError(request, exception).toString()
    )

  }

  def serverExceptionNotify(
      request: RequestHeader,
      exception: Throwable
    )(implicit m: Messages): Done = {
    sendEmail(adminEmails: _*)(
      from = emailFrom,
      subject =
        s"HAT server ${request.host} error: ${exception.getMessage} for ${request.path + request.rawQueryString}",
      bodyHtml = views.html.mails.emailServerThrowable(request, exception).toString(),
      bodyText = views.html.mails.emailServerThrowable(request, exception).toString()
    )
    Done
  }

  def passwordReset(
      email: String,
      resetLink: String
    )(implicit messages: MessagesApi,
      lang: Lang,
      server: HatServer): Done = {
    sendEmail(email)(
      from = emailFrom,
      subject = messages("email.dataswift.auth.subject.resetPassword"),
      bodyHtml = views.html.mails.emailAuthResetPassword(email, resetLink).toString(),
      bodyText = views.txt.mails.emailAuthResetPassword(email, resetLink).toString()
    )
    Done
  }

  def passwordChanged(
      email: String
    )(implicit messages: MessagesApi,
      lang: Lang,
      server: HatServer): Done = {
    sendEmail(email)(
      from = emailFrom,
      subject = messages("email.dataswift.auth.subject.passwordChanged"),
      bodyHtml = views.html.mails.emailAuthPasswordChanged().toString(),
      bodyText = views.txt.mails.emailAuthPasswordChanged().toString()
    )
    Done
  }

  def verifyEmail(
      email: String,
      verificationLink: String
    )(implicit messages: MessagesApi,
      lang: Lang,
      server: HatServer): Done = {
    sendEmail(email)(
      from = emailFrom,
      subject = messages("email.dataswift.auth.subject.verifyEmail"),
      bodyHtml = views.html.mails.emailAuthVerifyEmail(email, verificationLink).toString(),
      bodyText = views.txt.mails.emailAuthVerifyEmail(email, verificationLink).toString()
    )
    Done
  }

  def customWelcomeEmail(
      email: String,
      appName: Option[String],
      appLogoUrl: Option[String],
      verificationLink: String
    )(implicit messages: MessagesApi,
      lang: Lang): Done = {
    sendEmail(email)(
      from = emailFrom,
      subject = messages("email.dataswift.auth.subject.verifyEmail"),
      bodyHtml = views.html.mails.emailAuthCustomWelcome(email, appName, appLogoUrl, verificationLink).toString(),
      bodyText = views.txt.mails.emailAuthCustomWelcome(email, appName, appLogoUrl, verificationLink).toString()
    )
    Done
  }

  def emailVerified(
      email: String,
      loginLink: String
    )(implicit messages: MessagesApi,
      lang: Lang,
      server: HatServer): Done = {
    sendEmail(email)(
      from = emailFrom,
      subject = messages("email.dataswift.auth.subject.verifyEmail"),
      bodyHtml = views.html.mails.emailHatClaimed(email, loginLink).toString(),
      bodyText = views.txt.mails.emailHatClaimed(email, loginLink).toString()
    )
    Done
  }
}
