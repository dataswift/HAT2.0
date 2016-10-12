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
package hatdex.hat.api

import akka.actor.ActorRefFactory
import akka.event.{Logging, LoggingAdapter}
import hatdex.hat.api.endpoints._
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.ErrorMessage
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.phata.Phata
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.httpx.marshalling
import spray.routing.directives.LogEntry
import spray.routing._
import spray.util.LoggingContext

trait Api extends HttpService with Cors {
  implicit def actorRefFactory: ActorRefFactory

  // Initialise all the service the actor handles
  def helloService: Phata
  def apiDataService: Data
  def apiBundleService: Bundles
  def apiBundlesContextService: BundlesContext
  def dataDebitService: DataDebit
  def apiPropertyService: Property
  def eventsService:Event
  def locationsService:Location
  def peopleService: Person
  def thingsService: Thing
  def organisationsService: Organisation
  def userService:Users
  def typeService: Type

  // Wraps rejections in JSON to be sent back to the client

  import JsonProtocol._

  def jsonRejectionHandler = RejectionHandler {
    case MalformedRequestContentRejection(msg, cause) :: Nil =>
      complete {
        (BadRequest, ErrorMessage("The request content was malformed", msg))
      }

    // default case that wraps the Default error message into json
    case x if RejectionHandler.Default.isDefinedAt(x) =>
      ctx => RejectionHandler.Default(x) {
        ctx.withHttpResponseMapped {
          case resp@HttpResponse(_, HttpEntity.NonEmpty(ContentType(MediaTypes.`text/plain`, _), msg), _, _) =>
            resp.withEntity(marshalling.marshalUnsafe(ErrorMessage(msg.asString, "")))
        }
      }
  }

  override def timeoutRoute: Route = complete((InternalServerError, ErrorMessage("Timeout.", "")))

  // Concatenate all the handled routes
  def routes = handleRejections(jsonRejectionHandler) {
    cors {
      helloService.routes ~
      userService.getPublicKey ~
        respondWithMediaType(`application/json`) {
          apiDataService.routes ~
            apiPropertyService.routes ~
            //        apiBundleService.routes ~
            apiBundlesContextService.routes ~
            eventsService.routes ~
            locationsService.routes ~
            peopleService.routes ~
            thingsService.routes ~
            organisationsService.routes ~
            userService.routes ~
            typeService.routes ~
            dataDebitService.routes
        }
    }
  }


}
