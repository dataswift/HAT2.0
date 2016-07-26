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

import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.OrganisationsService
import hatdex.hat.authentication.models.User
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

// this trait defines our service behavior independently from the service actor
trait Organisation extends OrganisationsService with AbstractEntity {
  val entityKind = "organisation"

  val routes = {
    pathPrefix(entityKind) {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          createApi ~
            getApi ~
            getApiValues ~
            getAllApi ~
            linkToLocation ~
            linkToOrganisation ~
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
    entity(as[ApiOrganisation]) { organisation =>
      onComplete(createOrganisation(organisation.name)) {
        case Success(created) => complete((Created, created))
        case Failure(e)       => complete((BadRequest, ErrorMessage("Bad Request", e.getMessage)))
      }
    }
  }

  def createOrganisation(name: String): Future[ApiOrganisation] = {
    val q = for {
      organisation <- (OrganisationsOrganisation returning OrganisationsOrganisation) += OrganisationsOrganisationRow(0, LocalDateTime.now(), LocalDateTime.now(), name)
      entity <- Entity += EntityRow(organisation.id, LocalDateTime.now(), LocalDateTime.now(), organisation.name, entityKind, None, None, None, Some(organisation.id), None)
    } yield organisation
    DatabaseInfo.db.run(q).map(ApiOrganisation.fromDbModel)
  }
}

