/*
 * Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 10 2016
 */

package org.hatdex.hat.modules

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.resourceManagement.actors.{ HatServerActor, HatServerProviderActor }
import org.hatdex.hat.resourceManagement.{ HatDatabaseProvider, HatDatabaseProviderMilliner, HatKeyProvider, HatKeyProviderMilliner }
import play.api.libs.concurrent.AkkaGuiceSupport

class HatServerProviderModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  def configure = {
    bindActor[HatServerProviderActor]("hatServerProviderActor")
    bindActorFactory[HatServerActor, HatServerActor.Factory]

    bind[HatDatabaseProvider].to[HatDatabaseProviderMilliner]
    bind[HatKeyProvider].to[HatKeyProviderMilliner]
  }

}
