/*
 * Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 10 2016
 */

package org.hatdex.hat.modules

import com.google.inject.{ AbstractModule, Provides }
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.actors.SmtpConfig
import org.hatdex.hat.resourceManagement.actors.{ HatServerActor, HatServerProviderActor }
import play.api.Configuration
import play.api.libs.concurrent.AkkaGuiceSupport
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

class HatServerProviderModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  def configure = {
    bindActor[HatServerProviderActor]("hatServerProviderActor")
    bindActorFactory[HatServerActor, HatServerActor.Factory]
  }

  @Provides
  def provideSmtpConfiguration(
    configuration: Configuration): SmtpConfig = {
    configuration.underlying.as[SmtpConfig]("mail.smtp")
  }
}
