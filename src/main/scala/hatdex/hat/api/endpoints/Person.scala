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
import hatdex.hat.api.service.PeopleService
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

// this trait defines our service behavior independently from the service actor
trait Person extends PeopleService with AbstractEntity {
  val entityKind = "person"

  val routes = {
    pathPrefix(entityKind) {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          createApi ~
            getApi ~
            getApiValues ~
            getAllApi ~
            createPersonRelationshipType ~
            getPersonRelationshipTypes ~
            linkToPerson ~
            linkToLocation ~
            linkToOrganisation ~
            linkToThing ~
            linkToEvent ~
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
    entity(as[ApiPerson]) { person =>
      onComplete(createPersonRow(person.name, person.personId)) {
        case Success(created) => complete((Created, created))
        case Failure(e)       => complete((BadRequest, ErrorMessage("Bad Request", e.getMessage)))
      }
    }
  }

  def createPersonRow(name: String, personId: String): Future[ApiPerson] = {
    val q = for {
      person <- (PeoplePerson returning PeoplePerson) += PeoplePersonRow(0, LocalDateTime.now(), LocalDateTime.now(), name, personId)
      entity <- Entity += EntityRow(person.id, LocalDateTime.now(), LocalDateTime.now(), person.name, entityKind, None, None, None, None, Some(person.id))
    } yield (person, entity)
    DatabaseInfo.db.run(q.transactionally).map(r => ApiPerson.fromDbModel(r._1))
  }

  def createPersonRelationshipType = path("relationshipType") {
    logger.debug("Relationship type")
    post {
      logger.debug("Creating Relationship type")
      entity(as[ApiPersonRelationshipType]) { relationship =>
        val reltypeRow = new PeoplePersontopersonrelationshiptypeRow(0, LocalDateTime.now(), LocalDateTime.now(), relationship.name, relationship.description)
        val relType = DatabaseInfo.db.run((PeoplePersontopersonrelationshiptype returning PeoplePersontopersonrelationshiptype) += reltypeRow)
          .map(ApiPersonRelationshipType.fromDbModel)

        onComplete(relType) {
          case Success(reltype) => complete((Created, reltype))
          case Failure(e)       => complete((BadRequest, ErrorMessage("Error creating relationship type", e.getMessage)))
        }
      }
    }
  }

  def getPersonRelationshipTypes = path("relationshipType") {
    get {
      logger.debug("Getting Relationship types")
      val eventualReltypes = DatabaseInfo.db.run(PeoplePersontopersonrelationshiptype.result)
        .map(_.map(ApiPersonRelationshipType.fromDbModel))
      onComplete(eventualReltypes) {
        case Success(relTypes) => complete((OK, relTypes))
        case Failure(e)        => complete((InternalServerError, ErrorMessage("Error fetching person relationship types", e.getMessage)))
      }
    }
  }

  /*
   * Link two people together, e.g. as one person part of another person with a given relationship type
   */
  override def linkToPerson = path(IntNumber / "person" / IntNumber) { (personId: Int, person2Id: Int) =>
    post {
      entity(as[ApiPersonRelationshipType]) { relationship =>

        val eventualResult = for {
          recordId <- createRelationshipRecord(s"$entityKind/$personId/person/$person2Id:${relationship.name}")
          result <- relationship.id map { relationshipTypeId =>
            createLinkPerson(personId, person2Id, relationshipTypeId, recordId)
          } getOrElse {
            Future.failed(new IllegalArgumentException("People can only be linked with an existing relationship type"))
          }
        } yield ApiGenericId(result)

        onComplete(eventualResult) {
          case Success(id) => complete( (Created, id) )
          case Failure(e)  => complete( (BadRequest, ErrorMessage(s"Error linking ${entityKind} to person", e.getMessage)) )
        }
      }
    }
  }
}

