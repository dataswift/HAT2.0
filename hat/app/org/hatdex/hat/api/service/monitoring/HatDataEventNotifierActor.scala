package org.hatdex.hat.api.service.monitoring

import akka.actor.Actor
import play.api.Logger

class HatDataEventNotifierActor extends Actor {
  private val log = Logger(this.getClass)

  import HatDataEventBus._

  def receive: Receive = {
    case d: DataCreatedEvent => processBatchedStats(Seq(d))
    case d: Seq[_]           => processBatchedStats(d)
    case m                   => log.warn(s"Received something else: $m")
  }

  private def processBatchedStats(d: Seq[Any]) = {
    log.debug(s"Process batched stas: $d")
  }
}
