/*
 * Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 10 2016
 */

package org.hatdex.hat.utils

import javax.inject.Inject

import akka.actor.ActorSystem
import com.google.inject.ImplementedBy
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.mailer._

import scala.concurrent.duration._

@ImplementedBy(classOf[MailServiceImpl])
trait MailService {
  def sendEmailAsync(recipients: String*)(subject: String, bodyHtml: String, bodyText: String): Unit
  def sendEmail(recipients: String*)(subject: String, bodyHtml: String, bodyText: String): Unit
}

class MailServiceImpl @Inject() (system: ActorSystem, mailerClient: MailerClient, val conf: Configuration) extends MailService {

  lazy val from = conf.getString("play.mailer.from").get

  def sendEmailAsync(recipients: String*)(subject: String, bodyHtml: String, bodyText: String): Unit = {
    system.scheduler.scheduleOnce(100.milliseconds) {
      sendEmail(recipients: _*)(subject, bodyHtml, bodyText)
    }
  }

  def sendEmail(recipients: String*)(subject: String, bodyHtml: String, bodyText: String): Unit =
    mailerClient.send(Email(subject, from, recipients, Some(bodyText), Some(bodyHtml)))
}