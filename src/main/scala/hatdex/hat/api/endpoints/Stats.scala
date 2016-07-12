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

import akka.actor.{ActorRefFactory, ActorContext}
import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import hatdex.hat.api.service.DataService
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import spray.http.StatusCode

import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.routing._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

// this trait defines our service behavior independently from the service actor
trait Stats extends HttpService with HatServiceAuthHandler with DataService {

  val logger: LoggingAdapter
  def actorRefFactory: ActorRefFactory

  val routes = {
    pathPrefix("stats") {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("platform")) {
          apiGetTableStats
        }
      }
    }
  }

  def apiGetTableStats = path( "table" / IntNumber) { (tableId: Int) =>
      get {
        complete((NotImplemented, "Not yet implemented"))
      }
  }


}