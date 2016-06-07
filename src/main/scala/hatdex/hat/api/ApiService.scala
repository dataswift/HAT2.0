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
import akka.event.{Logging, LoggingAdapter}
import hatdex.hat.api.endpoints._
import hatdex.hat.api.service._
import spray.http.{HttpResponse, HttpRequest}
import spray.routing._
import spray.routing.directives.{LogEntry, LoggingMagnet}
import spray.util.LoggingContext

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiService extends HttpServiceActor with ActorLogging with Cors {
  implicit val apiLogger = Logging.getLogger(context.system, "API-Access")

  // logs request method, uri and response status at debug level
  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): Any => Option[LogEntry] = {
    case res: HttpResponse => Some(LogEntry(req.method + ":" + req.uri + ":" + res.message.status, Logging.InfoLevel))
    case Rejected(rejections) => Some(LogEntry(req.method + ":" + req.uri + ":" + rejections.toString(), Logging.ErrorLevel)) // log rejections
    case _ => None // other kind of responses
  }

  // The HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test.
  // We also want logging, hence the abstract logger member is included
  // in HAT services as well

  trait LoggingHttpService {
    def actorRefFactory = context
    val logger = apiLogger
  }

  val api: Api = new Api {
    implicit def actorRefFactory: ActorRefFactory = context

    // Initialise all the service the actor handles
    val helloService = new Hello with LoggingHttpService
    val apiDataService = new Data with LoggingHttpService
    val apiBundleService = new Bundles with LoggingHttpService
    val apiPropertyService = new Property with LoggingHttpService
    val eventsService = new Event with LoggingHttpService
    val locationsService = new Location with LoggingHttpService
    val peopleService = new Person with LoggingHttpService
    val thingsService = new Thing with LoggingHttpService
    val organisationsService = new Organisation with LoggingHttpService

    val apiBundlesContextService = new BundlesContext with LoggingHttpService {
      def eventsService: EventsService = ApiService.this.api.eventsService
      def peopleService: PeopleService = ApiService.this.api.peopleService
      def thingsService: ThingsService = ApiService.this.api.thingsService
      def locationsService: LocationsService = ApiService.this.api.locationsService
      def organisationsService: OrganisationsService = ApiService.this.api.organisationsService
    }

    val dataDebitService = new DataDebit with LoggingHttpService {
      val bundlesService: BundleService = apiBundleService
      val bundleContextService: BundleContextService = apiBundlesContextService
    }
    val userService = new Users with LoggingHttpService
    val typeService = new Type with LoggingHttpService
  }

//  val logRequestResponseStatus = (LoggingMagnet(api.printRequestMethod))

  val routes = logRequestResponse(requestMethodAndResponseStatusAsInfo _) {
    api.routes
  }

  def receive = runRoute(routes)
}