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
import hatdex.hat.api.service.{ BundleContextService, BundleService }
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

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
            db.withSession { implicit session =>
              val result = storeBundleContext(bundleContext)
              session.close()
              complete {
                result match {
                  case Success(storedBundleContext) => (Created, storedBundleContext)
                  case Failure(e)                   => (BadRequest, ErrorMessage("Could not create Contextual Bundle", e.getMessage))
                }
              }
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
          db.withSession { implicit session =>
            val maybeBundleContext = getBundleContextById(bundleContextId)
            session.close()
            complete {
              maybeBundleContext match {
                case Some(bundleContext) => bundleContext
                case None                => (NotFound, ErrorMessage("Bundle Not Found", s"Bundle ${bundleContextId} not found or empty"))
              }
            }
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
          val maybeBundleData = db.withSession { implicit session =>
            val bundleData = getBundleContextData(bundleContextId)
            session.close()
            bundleData
          }
          complete {
            maybeBundleData
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
            db.withSession { implicit session =>
              val maybeInsertedSelection = storeBundleContextEntitySelection(bundleId, entitySelection)
              session.close()

              complete {
                maybeInsertedSelection match {
                  case Success(insertedSelection) => (Created, insertedSelection)
                  case Failure(e)                 => (BadRequest, ErrorMessage("Could not add Entity Selection", e.getMessage))
                }
              }
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
            db.withSession { implicit session =>
              val maybeInsertedSelection = storeBundlePropertySelection(entitySelectionId, propertySelection)
              session.close()

              complete {
                maybeInsertedSelection match {
                  case Success(insertedSelection) => (Created, insertedSelection)
                  case Failure(e)                 => (BadRequest, ErrorMessage("Could not add Property Selection", e.getMessage))
                }
              }
            }
          }
        }
      }
    }
  }
}