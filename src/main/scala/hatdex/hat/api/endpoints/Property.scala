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
package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.PropertyService
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success, Try }

// this trait defines our service behavior independently from the service actor
trait Property extends HttpService with PropertyService with HatServiceAuthHandler {
  val logger: LoggingAdapter

  val routes = {
    pathPrefix("property") {
      createProperty ~
        getPropertyApi ~
        getPropertiesApi
    }
  }

  import JsonProtocol._

  def createProperty = pathEnd {
    post {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform")) {
          entity(as[ApiProperty]) { property =>
            onComplete(storeProperty(property)) {
              case Success(created) => complete((Created, created))
              case Failure(e) => complete((BadRequest, ErrorMessage("Error creating property", e.getMessage)))
            }
          }
        }
      }
    }
  }

  def getPropertyApi = path(IntNumber) { (propertyId: Int) =>
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataDebit", "dataCredit")) {
          onComplete(getProperty(propertyId)) {
            case Success(Some(property)) => complete((OK, property))
            case Success(None) => complete((NotFound, s"Property $propertyId not found"))
            case Failure(e) => complete((InternalServerError, ErrorMessage("Error fetching property", e.getMessage)))
          }
        }
      }
    }
  }

  def getPropertiesApi = pathEnd {
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform", "dataDebit", "dataCredit")) {
          parameters('name.?) { (maybePropertyName: Option[String]) =>
            onComplete(getProperties(maybePropertyName)) {
              case Success(properties) => complete((OK, properties))
              case Failure(e) => complete((InternalServerError, ErrorMessage("Error fetching property", e.getMessage)))
            }
          }
        }
      }
    }
  }
}

