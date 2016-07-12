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
import hatdex.hat.api.service.AbstractEntityService
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }
import scala.reflect.runtime.universe._

trait AbstractEntity extends HttpService with AbstractEntityService with HatServiceAuthHandler {

  import JsonProtocol._

  val db = DatabaseInfo.db

  def createApi = {
    post {
      createEntity
    }
  }

  def getApi = path(IntNumber) { (entityId: Int) =>
    get {
      implicit val getValues: Boolean = false
      getEntity(entityId)
    }
  }

  private def getEntity(entityId: Int)(implicit getValues: Boolean) = {
    val result = entityKind match {
      case "person"       => getPerson(entityId, recursive = true)
      case "thing"        => getThing(entityId, recursive = true)
      case "event"        => getEvent(entityId, recursive = true)
      case "location"     => getLocation(entityId, recursive = true)
      case "organisation" => getOrganisation(entityId, recursive = true)
      case _              => Future.successful(None)
    }

    onComplete(result) {
      case Success(Some(entity: ApiPerson))       => complete(entity)
      case Success(Some(entity: ApiThing))        => complete(entity)
      case Success(Some(entity: ApiEvent))        => complete(entity)
      case Success(Some(entity: ApiLocation))     => complete(entity)
      case Success(Some(entity: ApiOrganisation)) => complete(entity)
      case _                                      => complete((NotFound, ErrorMessage("NotFound", s"$entityKind with ID $entityId not found")))
    }
  }

  def getAllApi = pathEnd {
    respondWithMediaType(`application/json`) {
      get {
        getAllEntitiesSimple
      }
    }
  }

  private def getAllEntitiesSimple = {
    val result = entityKind match {
      case "person"       => DatabaseInfo.db.run(PeoplePerson.result).map(_.map(ApiPerson.fromDbModel)).map(e => complete(e))
      case "thing"        => DatabaseInfo.db.run(ThingsThing.result).map(_.map(ApiThing.fromDbModel)).map(e => complete(e))
      case "event"        => DatabaseInfo.db.run(EventsEvent.result).map(_.map(ApiEvent.fromDbModel)).map(e => complete(e))
      case "location"     => DatabaseInfo.db.run(LocationsLocation.result).map(_.map(ApiLocation.fromDbModel)).map(e => complete(e))
      case "organisation" => DatabaseInfo.db.run(OrganisationsOrganisation.result).map(_.map(ApiOrganisation.fromDbModel)).map(e => complete(e))
      case _              => Future.successful(Seq[ApiPerson]()).map(e => complete(e))
    }

    onComplete(result) {
      case Success(entities) => entities
      case Failure(e)        => complete((InternalServerError, ErrorMessage("Error getting Entities", e.getMessage)))
    }
  }

  def getApiValues = path(IntNumber / "values") { (entityId: Int) =>
    get {
      implicit val getValues: Boolean = true
      getEntity(entityId)
    }
  }

  def linkToLocation = path(IntNumber / "location" / IntNumber) { (entityId: Int, locationId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        val eventualResult = for {
          recordId <- createRelationshipRecord(s"$entityKind/$entityId/location/$locationId:${relationship.relationshipType}")
          result <- createLinkLocation(entityId, locationId, relationship.relationshipType, recordId).map(ApiGenericId)
        } yield result

        onComplete(eventualResult) {
          case Success(crossrefId) => complete((Created, crossrefId))
          case Failure(e)          => complete((BadRequest, ErrorMessage(s"Error Linking ${entityKind} to Location", e.getMessage)))
        }
      }
    }
  }

  def linkToOrganisation = path(IntNumber / "organisation" / IntNumber) { (entityId: Int, organisationId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        val eventualResult = for {
          recordId <- createRelationshipRecord(s"$entityKind/$entityId/organisation/$organisationId:${relationship.relationshipType}")
          result <- createLinkOrganisation(entityId, organisationId, relationship.relationshipType, recordId).map(ApiGenericId)
        } yield result

        onComplete(eventualResult) {
          case Success(crossrefId) => complete((Created, crossrefId))
          case Failure(e)          => complete((BadRequest, ErrorMessage(s"Error Linking ${entityKind} to Organisation", e.getMessage)))
        }
      }
    }
  }

  def linkToPerson = path(IntNumber / "person" / IntNumber) { (entityId: Int, personId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        val eventualResult = for {
          recordId <- createRelationshipRecord(s"$entityKind/$entityId/person/$personId:${relationship.relationshipType}")
          result <- createLinkPerson(entityId, personId, relationship.relationshipType, recordId).map(ApiGenericId)
        } yield result

        onComplete(eventualResult) {
          case Success(crossrefId) => complete((Created, crossrefId))
          case Failure(e)          => complete((BadRequest, ErrorMessage(s"Error Linking ${entityKind} to Person", e.getMessage)))
        }
      }
    }
  }

  def linkToThing = path(IntNumber / "thing" / IntNumber) { (entityId: Int, thingId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        val eventualResult = for {
          recordId <- createRelationshipRecord(s"$entityKind/$entityId/thing/$thingId:${relationship.relationshipType}")
          result <- createLinkThing(entityId, thingId, relationship.relationshipType, recordId).map(ApiGenericId)
        } yield result

        onComplete(eventualResult) {
          case Success(crossrefId) => complete((Created, crossrefId))
          case Failure(e)          => complete((BadRequest, ErrorMessage(s"Error Linking ${entityKind} to Person", e.getMessage)))
        }
      }
    }
  }

  def linkToEvent = path(IntNumber / "event" / IntNumber) { (entityId: Int, eventId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        val eventualResult = for {
          recordId <- createRelationshipRecord(s"$entityKind/$entityId/event/$eventId:${relationship.relationshipType}")
          result <- createLinkEvent(entityId, eventId, relationship.relationshipType, recordId).map(ApiGenericId)
        } yield result

        onComplete(eventualResult) {
          case Success(crossrefId) => complete((Created, crossrefId))
          case Failure(e)          => complete((BadRequest, ErrorMessage(s"Error Linking $entityKind to Person", e.getMessage)))
        }
      }
    }
  }

  def getPropertiesStaticApi = path(IntNumber / "property" / "static") { (entityId: Int) =>
    get {
      implicit val getValues: Boolean = false
      onComplete(getPropertiesStatic(entityId, None)) {
        case Success(properties) => complete((OK, properties))
        case Failure(e)          => complete((InternalServerError, ErrorMessage("Error getting properties", e.getMessage)))
      }
    }
  }

  def getPropertiesDynamicApi = path(IntNumber / "property" / "dynamic") { (entityId: Int) =>
    get {
      implicit val getValues: Boolean = false
      onComplete(getPropertiesDynamic(entityId, None)) {
        case Success(properties) => complete((OK, properties))
        case Failure(e)          => complete((InternalServerError, ErrorMessage("Error getting properties", e.getMessage)))
      }
    }
  }

  def getPropertiesStaticValuesApi = path(IntNumber / "property" / "static" / "values") { (entityId: Int) =>
    get {
      implicit val getValues: Boolean = true
      onComplete(getPropertiesStatic(entityId, None)) {
        case Success(properties) => complete((OK, properties))
        case Failure(e)          => complete((InternalServerError, ErrorMessage("Error getting properties", e.getMessage)))
      }
    }
  }

  def getPropertiesDynamicValuesApi = path(IntNumber / "property" / "dynamic" / "values") { (entityId: Int) =>
    get {
      implicit val getValues: Boolean = true
      onComplete(getPropertiesDynamic(entityId, None)) {
        case Success(properties) => complete((OK, properties))
        case Failure(e)          => complete((InternalServerError, ErrorMessage("Error getting properties", e.getMessage)))
      }
    }
  }

  // staticproperty as a way of differentiating between a property that is linked statically 
  // and the propertyRelatonship link
  def getPropertyStaticValueApi = path(IntNumber / "property" / "static" / IntNumber / "values") { (entityId: Int, propertyRelationshipId: Int) =>
    get {
      implicit val getValues: Boolean = true
      onComplete(getPropertyStaticValues(entityId, propertyRelationshipId)) {
        case Success(properties) => complete((OK, properties))
        case Failure(e)          => complete((InternalServerError, ErrorMessage("Error getting properties", e.getMessage)))
      }
    }
  }

  // dynamicproperty as a way of differentiating between a property that is linked dynamically 
  // and the propertyRelatonship link
  def getPropertyDynamicValueApi = path(IntNumber / "property" / "dynamic" / IntNumber / "values") { (entityId: Int, propertyRelationshipId: Int) =>
    get {
      implicit val getValues: Boolean = true
      onComplete(getPropertyDynamicValues(entityId, propertyRelationshipId)) {
        case Success(properties) => complete((OK, properties))
        case Failure(e)          => complete((InternalServerError, ErrorMessage("Error getting properties", e.getMessage)))
      }
    }
  }

  /*
   * Tag event with a type
   */
  def addTypeApi = path(IntNumber / "type" / IntNumber) { (entityId: Int, typeId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        val eventualCrossref = addEntityType(entityId, typeId, relationship).map(ApiGenericId)

        onComplete(eventualCrossref) {
          case Success(crossrefId) => complete((Created, crossrefId))
          case Failure(e)          => complete((BadRequest, ErrorMessage(s"Error Adding Type to $entityKind $entityId", e.getMessage)))
        }
      }
    }
  }

  /*
   * Link event to a property statically (tying it in with a specific record ID)
   */
  def linkToPropertyStatic = path(IntNumber / "property" / "static" / IntNumber) { (entityId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipStatic]) { relationship =>
        val result = (relationship.field.id, relationship.record.id) match {
          case (Some(fieldId), Some(recordId)) =>
            for {
              propertyRecordId <- createPropertyRecord(s"$entityKind/$entityId/property/static/$propertyId:${relationship.relationshipType}($fieldId,$recordId,${relationship.relationshipType}")
              propertyLink <- createPropertyLinkStatic(entityId, propertyId, recordId, fieldId, relationship.relationshipType, propertyRecordId)
            } yield propertyLink
          case (None, _) => Future.failed(new IllegalArgumentException("Property relationship must have an existing Data Field with ID"))
          case (_, None) => Future.failed(new IllegalArgumentException("Static Property relationship must have an existing Data Record with ID"))
        }

        onComplete(result) {
          case Success(crossrefId) => complete((Created, ApiGenericId(crossrefId)))
          case Failure(e)          => complete((BadRequest, ErrorMessage("Error Linking Property Statically", e.getMessage)))
        }
      }
    }
  }

  /*
   * Link event to a property dynamically
   */
  def linkToPropertyDynamic = path(IntNumber / "property" / "dynamic" / IntNumber) { (entityId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipDynamic]) { relationship =>
        val result = (relationship.field.id) match {
          case Some(fieldId) =>
            for {
              propertyRecordId <- createPropertyRecord(s"$entityKind/$entityId/property/dynamic/$propertyId:${relationship.relationshipType}($fieldId,${relationship.relationshipType}")
              propertyLink <- createPropertyLinkDynamic(entityId, propertyId, fieldId, relationship.relationshipType, propertyRecordId)
            } yield propertyLink
          case None => Future.failed(new IllegalArgumentException("Property relationship must have an existing Data Field with ID"))
        }

        onComplete(result) {
          case Success(crossrefId) => complete((Created, ApiGenericId(crossrefId)))
          case Failure(e)          => complete((BadRequest, ErrorMessage("Error Linking Property Dynamically", e.getMessage)))
        }
      }
    }
  }
}
