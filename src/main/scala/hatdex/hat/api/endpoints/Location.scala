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

import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.LocationsService
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

import scala.util.{ Failure, Success, Try }

// this trait defines our service behavior independently from the service actor
trait Location extends LocationsService with AbstractEntity {
  val entityKind = "location"

  val routes = {
    pathPrefix(entityKind) {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          createApi ~
            addTypeApi ~
            getApi ~
            getApiValues ~
            getAllApi ~
            linkToLocation ~
            linkToThing ~
            linkToPerson ~
            linkToOrganisation ~
            linkToEvent ~
            linkToPropertyStatic ~
            linkToPropertyDynamic ~
            getPropertiesStaticApi ~
            getPropertiesDynamicApi ~
            getPropertyStaticValueApi ~
            getPropertyDynamicValueApi
        }
      }
    }
  }

  import JsonProtocol._

  def createEntity = pathEnd {
    entity(as[ApiLocation]) { location =>
      db.withSession { implicit session =>
        val locationslocationRow = new LocationsLocationRow(0, LocalDateTime.now(), LocalDateTime.now(), location.name)
        val result = Try((LocationsLocation returning LocationsLocation) += locationslocationRow)
        val entity = result map { createdLocation =>
          val newEntity = new EntityRow(createdLocation.id, LocalDateTime.now(), LocalDateTime.now(), createdLocation.name, entityKind, Some(createdLocation.id), None, None, None, None)
          val entityCreated = Try(Entity += newEntity)
          logger.debug("Creating new entity for location:"+entityCreated)
        }

        complete {
          result match {
            case Success(createdLocation) =>
              (Created, ApiLocation.fromDbModel(createdLocation))
            case Failure(e) =>
              (BadRequest, e.getMessage)
          }
        }
      }
    }
  }
}

