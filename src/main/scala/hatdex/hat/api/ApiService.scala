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
import hatdex.hat.api.endpoints._
import spray.routing._
import spray.util.LoggingContext

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiService extends HttpServiceActor with ActorLogging with Cors {
  val apiLogger = LoggingContext.fromActorRefFactory

  // The HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test.
  // We also want logging, hence the abstract logger member is included
  // in HAT services as well

  trait LoggingHttpService {
    def actorRefFactory = context
    val logger = log
  }

  val api = new Api {
    implicit def actorRefFactory: ActorRefFactory = context

    // Initialise all the service the actor handles
    val helloService = new Hello with LoggingHttpService
    val apiDataService = new Data with LoggingHttpService
    val apiBundleService = new Bundles with LoggingHttpService
    val dataDebitService = new DataDebit with LoggingHttpService
    val apiPropertyService = new Property with LoggingHttpService
    val eventsService = new Event with LoggingHttpService
    val locationsService = new Location with LoggingHttpService
    val peopleService = new Person with LoggingHttpService
    val thingsService = new Thing with LoggingHttpService
    val organisationsService = new Organisation with LoggingHttpService
    val userService = new Users with LoggingHttpService
    val typeService = new Type with LoggingHttpService
  }

  val routes = logRequestResponse(api.requestMethodAndResponseStatusAsInfo _) {
    api.routes
  }

  def receive = runRoute(routes)
}

