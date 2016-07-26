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
import hatdex.hat.api.service.ThingsService
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCode
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success, Try }

// this trait defines our service behavior independently from the service actor
trait Thing extends ThingsService with AbstractEntity {
  val entityKind = "thing"

  val routes = {
    pathPrefix(entityKind) {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          createApi ~
            getApi ~
            getApiValues ~
            getAllApi ~
            linkToPerson ~
            linkToThing ~
            linkToEvent ~
            linkToOrganisation ~
            linkToLocation ~
            linkToPropertyStatic ~
            linkToPropertyDynamic ~
            addTypeApi ~
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
    entity(as[ApiThing]) { thing =>
      val thingsthingRow = new ThingsThingRow(0, LocalDateTime.now(), LocalDateTime.now(), thing.name)
      val result = db.run {
        ((ThingsThing returning ThingsThing) += thingsthingRow).asTry
      }
      val fEntity = result flatMap {
        case Success(createdThing) =>
          val newEntity = new EntityRow(createdThing.id, LocalDateTime.now(), LocalDateTime.now(), createdThing.name, "thing", None, Some(createdThing.id), None, None, None)
          db.run(Entity += newEntity).map { case entity => (Created, ApiThing.fromDbModel(createdThing)) }
        case Failure(e) =>
          throw ApiError(BadRequest, ErrorMessage("Error when creating thing", e.getMessage))
      }

      onComplete(fEntity) {
        case Success((statusCode, value)) => complete((statusCode, value))
        case Failure(e: ApiError)         => complete((e.statusCode, e.message))
        case Failure(e)                   => complete((InternalServerError, ErrorMessage("Error while creating thing", "Unknown error occurred")))
      }
    }
  }
}

