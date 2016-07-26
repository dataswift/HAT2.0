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
import hatdex.hat.api.service.BundleContextService
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

// this trait defines our service behavior independently from the service actor
trait BundlesContext extends HttpService with BundleContextService with HatServiceAuthHandler {

  val db = DatabaseInfo.db

  val logger: LoggingAdapter

  val routes = {
    pathPrefix("bundles" / "context") {
      createBundleContext ~
        getBundleContext ~
        getBundleContextValues ~
        addEntitySelectionApi ~
        addPropertySelectionApi
    }
  }

  import JsonProtocol._

  /*
   * Creates a bundle table as per POST'ed data, including table information and its slicing conditions
   */
  def createBundleContext = pathEnd {
    post {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          entity(as[ApiBundleContext]) { bundleContext =>
            onComplete(storeBundleContext(bundleContext)) {
              case Success(storedBundleContext) => complete((Created, storedBundleContext))
              case Failure(e)                   => complete((BadRequest, ErrorMessage("Could not create Contextual Bundle", e.getMessage)))
            }
          }
        }
      }
    }
  }

  /*
   * Retrieves bundle table structure by ID
   */
  def getBundleContext = path(IntNumber) { (bundleContextId: Int) =>
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          onComplete(getBundleContextById(bundleContextId)) {
            case Success(Some(bundleContext)) => complete((OK, bundleContext))
            case Success(None)                => complete((NotFound, ErrorMessage("Bundle Not Found", s"Bundle ${bundleContextId} not found or empty")))
            case Failure(e)                   => complete((NotFound, ErrorMessage("Bundle Not Found", e.getMessage)))
          }
        }
      }
    }
  }

  /*
   * Retrieves bundle table data
   */
  def getBundleContextValues = path(IntNumber / "values") { (bundleContextId: Int) =>
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          onComplete(getBundleContextData(bundleContextId)) {
            case Success(data) => complete((OK, data))
            case Failure(e)    => complete((InternalServerError, ErrorMessage("Error Fetching data", e.getMessage)))
          }
        }
      }
    }
  }

  /*
   * Retrieves Conext bundle structure by ID
   */
  def addEntitySelectionApi = path(IntNumber / "entitySelection") { (bundleId: Int) =>
    post {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          entity(as[ApiBundleContextEntitySelection]) { entitySelection =>
            onComplete(storeBundleContextEntitySelection(bundleId, entitySelection)) {
              case Success(insertedSelection) => complete((Created, insertedSelection))
              case Failure(e)                 => complete((BadRequest, ErrorMessage("Could not add Entity Selection", e.getMessage)))
            }
          }
        }
      }
    }
  }

  def addPropertySelectionApi = path(IntNumber / "entitySelection" / IntNumber / "propertySelection") { (bundleId: Int, entitySelectionId: Int) =>
    post {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          entity(as[ApiBundleContextPropertySelection]) { propertySelection =>
            onComplete(storeBundlePropertySelection(entitySelectionId, propertySelection)) {
              case Success(insertedSelection) => complete((Created, insertedSelection))
              case Failure(e)                 => complete((BadRequest, ErrorMessage("Could not add Property Selection", e.getMessage)))
            }
          }
        }
      }
    }
  }
}