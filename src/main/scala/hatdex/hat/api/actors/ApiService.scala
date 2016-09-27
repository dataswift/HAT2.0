/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
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
 */
package hatdex.hat.api.actors

import akka.actor.ActorDSL._
import akka.actor._
import akka.event.Logging
import akka.io.IO
import akka.io.Tcp.Bound
import com.typesafe.config.ConfigFactory
import hatdex.hat.api.endpoints._
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.ErrorMessage
import hatdex.hat.api.service._
import hatdex.hat.api.{Api, Cors}
import hatdex.hat.phata.Phata
import spray.can.Http
import spray.http.{StatusCodes, Timedout, HttpRequest, HttpResponse}
import spray.httpx.marshalling
import spray.routing._
import spray.routing.directives.LogEntry
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.httpx.marshalling



// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiService extends HttpServiceActor with ActorLogging with Cors {
  implicit val apiLogger = Logging.getLogger(context.system, "API-Access")

  val smtpConfig = SmtpConfig(conf.getBoolean("mail.smtp.tls"),
    conf.getBoolean("mail.smtp.ssl"),
    conf.getInt("mail.smtp.port"),
    conf.getString("mail.smtp.host"),
    conf.getString("mail.smtp.username"),
    conf.getString("mail.smtp.password"))
  val apiEmailService = new EmailService(context.system, smtpConfig)

  override def preStart() = {
    val conf = ConfigFactory.load()
    val port = conf.getInt("applicationPort")
    val host = conf.getString("applicationHost")
    val ioListener = actor("ioListener")(new Act with ActorLogging {
      become {
        case b @ Bound(connection) => log.debug(b.toString)
      }
    })
    IO(Http)(context.system).tell(Http.Bind(self, host, port), ioListener)
  }


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
    def actorRefFactory: ActorRefFactory = context
    val logger = apiLogger
  }

  val api: Api = new Api {
    implicit def actorRefFactory: ActorRefFactory = context

    // Initialise all the service the actor handles
    val helloService = new Phata with LoggingHttpService with DalExecutionContext{
      val emailService = apiEmailService
    }
    val apiDataService = new Data with LoggingHttpService with DalExecutionContext
    val apiBundleService = new Bundles with LoggingHttpService with DalExecutionContext
    val apiPropertyService = new Property with LoggingHttpService with DalExecutionContext
    val eventsService = new Event with LoggingHttpService with DalExecutionContext
    val locationsService = new Location with LoggingHttpService with DalExecutionContext
    val peopleService = new Person with LoggingHttpService with DalExecutionContext
    val thingsService = new Thing with LoggingHttpService with DalExecutionContext
    val organisationsService = new Organisation with LoggingHttpService with DalExecutionContext

    val apiBundlesContextService = new BundlesContext with LoggingHttpService with DalExecutionContext {
      def eventsService: EventsService = ApiService.this.api.eventsService
      def peopleService: PeopleService = ApiService.this.api.peopleService
      def thingsService: ThingsService = ApiService.this.api.thingsService
      def locationsService: LocationsService = ApiService.this.api.locationsService
      def organisationsService: OrganisationsService = ApiService.this.api.organisationsService
    }

    val dataDebitService = new DataDebit with LoggingHttpService with DalExecutionContext {
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

  import JsonProtocol._
  def handleTimeouts: Receive = {
    case Timedout(x: HttpRequest) =>
      sender ! HttpResponse(StatusCodes.RequestTimeout, marshalling.marshalUnsafe(ErrorMessage("The server was not able to produce a timely response to your request.", "")))
  }

  def receive = handleTimeouts orElse runRoute(routes)
}