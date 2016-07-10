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
import hatdex.hat.api.service.BundleService
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

// this trait defines our service behavior independently from the service actor
trait Bundles extends HttpService with BundleService with HatServiceAuthHandler {

  val db = DatabaseInfo.db

  val logger: LoggingAdapter

  val routes = {
    pathPrefix("bundles" / "contextless") {
      createBundleContextless ~
        getBundleContextless ~
        getBundleContextlessValuesApi
    }
  }

  import JsonProtocol._

  /*
   * Creates a new virtual table for storing arbitrary incoming data
   */
  def createBundleContextless = pathEnd {
    post {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          logger.debug(s"BundleService POST /bundles/contextless authenticated")
          entity(as[ApiBundleContextless]) { bundle =>
            logger.debug(s"BundleService POST /bundles/contextless parsed")
            val result = storeBundleContextless(bundle)

            onComplete(result) {
              case Success(storedBundle) => complete((Created, storedBundle))
              case Failure(e)            => complete((BadRequest, ErrorMessage("Error creating bundle", e.getMessage)))
            }
          }
        }
      }
    }
  }

  /*
   * Retrieves contextless bundle structure by ID
   */
  def getBundleContextless = path(IntNumber) { (bundleId: Int) =>
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          logger.debug(s"BundleService GET /bundles/contextless/$bundleId")
          val bundle = getBundleContextlessById(bundleId)
          onComplete(bundle) {
            case Success(Some(foundBundle)) => complete(foundBundle)
            case Success(None)              => complete((NotFound, ErrorMessage("Contextless bundle not found", s"Contextless Bundle $bundleId not found")))
            case Failure(e)                 => complete((BadRequest, ErrorMessage("Contextless bundle not retrieved", e.getMessage)))
          }
        }
      }
    }
  }

  /*
   * Retrieves contextless bundle data
   */
  def getBundleContextlessValuesApi = path(IntNumber / "values") { (bundleId: Int) =>
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          logger.debug(s"BundleService GET /bundles/contextless/$bundleId/values")
          val someResults = getBundleContextlessValues(bundleId, None, LocalDateTime.now().minusDays(7), LocalDateTime.now())
          onComplete(someResults) {
            case Success(foundBundle) => complete(foundBundle)
            case Failure(e)           => complete((BadRequest, ErrorMessage("Contextless bundle not retrieved", e.getMessage)))
          }
        }
      }
    }
  }
}