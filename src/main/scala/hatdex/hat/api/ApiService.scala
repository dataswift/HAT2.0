/*
 * Copyright (c) 2015.
 *
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package hatdex.hat.api

import akka.actor.{ActorLogging, ActorRefFactory}
import akka.event.LoggingAdapter
import spray.routing._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiService extends HttpServiceActor with ActorLogging with Cors {
  val api = new Api {
    override val logger: LoggingAdapter = log
    override implicit def actorRefFactory: ActorRefFactory = context
  }

  def receive = runRoute(api.routes)
}

