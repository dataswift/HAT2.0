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
package hatdex.hat.api.service

import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import hatdex.hat.api.service.IoExecutionContext.ioThreadPool
import scala.concurrent.Future

// this trait defines our service behavior independently from the service actor
trait LocationsService extends AbstractEntityService with PropertyService {

  def getThings(locationID: Int)(implicit getValues: Boolean): Future[Seq[ApiThingRelationship]] = {
    val locationualThingLinks = DatabaseInfo.db.run(LocationsLocationthingcrossref.filter(_.locationId === locationID).result)

    locationualThingLinks flatMap { links =>
      // Sequence of futures to future of sequence
      Future.sequence {
        links map { link =>
          getThing(link.thingId) map { maybeThing =>
            maybeThing.map { t => ApiThingRelationship(link.relationshipType, t) }
          }
        }
      } map (_.flatten)
    }
  }

  def getLocations(locationID: Int)(implicit getValues: Boolean): Future[Seq[ApiLocationRelationship]] = {
    val locationualLocationLinks = DatabaseInfo.db.run(LocationsLocationtolocationcrossref.filter(_.locOneId === locationID).result)

    locationualLocationLinks flatMap { links =>
      // Sequence of futures to future of sequence
      Future.sequence {
        links map { link =>
          getLocation(link.locTwoId) map { maybeLocation =>
            maybeLocation.map { e => ApiLocationRelationship(link.relationshipType, e) }
          }
        }
      } map (_.flatten)
    }
  }

  protected def createLinkLocation(entityId: Int, locationId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    val crossref = new LocationsLocationtolocationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      locationId, entityId, relationshipType, true, recordId)
    DatabaseInfo.db.run((LocationsLocationtolocationcrossref returning LocationsLocationtolocationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkThing(entityId: Int, thingId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    val crossref = new LocationsLocationthingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      thingId, entityId, relationshipType, true, recordId)
    DatabaseInfo.db.run((LocationsLocationthingcrossref returning LocationsLocationthingcrossref.map(_.id)) += crossref)
  }

  protected def createLinkOrganisation(entityId: Int, organisationId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    Future.failed(new RuntimeException("Operation Not Supprted"))
  }

  protected def createLinkPerson(entityId: Int, personId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    Future.failed(new RuntimeException("Operation Not Supprted"))
  }

  protected def createLinkEvent(entityId: Int, eventId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    Future.failed(new RuntimeException("Operation Not Supprted"))
  }

  /*
   * Link location to a property statically (tying it in with a specific record ID)
   */
  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                          fieldId: Int, relationshipType: String, propertyRecordId: Int): Future[Int] = {
    val crossref = new LocationsSystempropertydynamiccrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      fieldId, relationshipType,
      true, propertyRecordId)

    DatabaseInfo.db.run((LocationsSystempropertydynamiccrossref returning LocationsSystempropertydynamiccrossref.map(_.id)) += crossref)
  }

  /*
   * Link location to a property dynamically
   */
  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                         recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int): Future[Int] = {
    val crossref = new LocationsSystempropertystaticcrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      recordId, fieldId, relationshipType,
      true, propertyRecordId)

    DatabaseInfo.db.run((LocationsSystempropertystaticcrossref returning LocationsSystempropertystaticcrossref.map(_.id)) += crossref)
  }

  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship): Future[Int] = {
    val entityType = new LocationsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, typeId, relationship.relationshipType, true)
    DatabaseInfo.db.run((LocationsSystemtypecrossref returning LocationsSystemtypecrossref.map(_.id)) += entityType)
  }

  protected def getOrganisations(entityId: Int)(implicit getValues: Boolean): Future[Seq[ApiOrganisationRelationship]] = {
    // No links directly from Location
    Future.successful(Seq())
  }

  protected def getPeople(entityId: Int)(implicit getValues: Boolean): Future[Seq[ApiPersonRelationship]] = {
    // No links directly from Location
    Future.successful(Seq())
  }

  protected def getEvents(entityId: Int)(implicit getValues: Boolean): Future[Seq[ApiEventRelationship]] = {
    // No links directly from Location
    Future.successful(Seq())
  }

  protected def getPropertiesStatic(locationId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])(implicit getValues: Boolean): Future[Seq[ApiPropertyRelationshipStatic]] = {
    // Get all property selectors that are not market for dynamic properties
    val nonDynamicPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("dynamic")))

    // FIXME: other solutions possible, but such explicit naming of properties prlocations the use of generics
    val crossrefQuery = LocationsSystempropertystaticcrossref.filter(_.locationId === locationId)
    for {
      pq <- getPropertiesStaticQuery(crossrefQuery, nonDynamicPropertySelectors)
      p <- if (getValues) {
        Future.sequence(pq.map(getPropertyRelationshipValues))
      }
      else {
        Future.successful(pq)
      }
    } yield p
  }

  private def getPropertiesStaticQuery(crossrefQuery: Query[LocationsSystempropertystaticcrossref, LocationsSystempropertystaticcrossrefRow, Seq],
                                       propertySelectors: Option[Seq[ApiBundleContextPropertySelection]]): Future[Seq[ApiPropertyRelationshipStatic]] = {
    val dataQuery = propertySelectors match {
      case Some(selectors) =>
        val queries = selectors map { selector =>
          for {
            crossref <- crossrefQuery
            property <- selector.propertyName match {
              case None               => crossref.systemPropertyFk
              case Some(propertyName) => crossref.systemPropertyFk.filter(_.name === propertyName)
            }
            propertyType <- selector.propertyType match {
              case None                   => property.systemTypeFk
              case Some(propertyTypeName) => property.systemTypeFk.filter(_.name === propertyTypeName)
            }
            propertyUom <- selector.propertyUnitofmeasurement match {
              case None                  => property.systemUnitofmeasurementFk
              case Some(propertyUomName) => property.systemUnitofmeasurementFk.filter(_.name === propertyUomName)
            }
            field <- crossref.dataFieldFk
            record <- crossref.dataRecordFk
          } yield (crossref, property, propertyType, propertyUom, field, record)
        }

        queries reduceLeft { (query, propertySelectionQuery) =>
          query union propertySelectionQuery
        }

      case None =>
        for {
          crossref <- crossrefQuery
          property <- crossref.systemPropertyFk
          propertyType <- property.systemTypeFk
          propertyUom <- property.systemUnitofmeasurementFk
          field <- crossref.dataFieldFk
          record <- crossref.dataRecordFk
        } yield (crossref, property, propertyType, propertyUom, field, record)
    }

    DatabaseInfo.db.run(dataQuery.result).map { results =>
      results.map {
        case (crossref, property, propertyType, propertyUom, field, record) =>
          ApiPropertyRelationshipStatic.fromDbModel(crossref, property, propertyType, propertyUom, field, record)
      }
    }
  }

  protected def getPropertiesDynamic(locationId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])(implicit getValues: Boolean): Future[Seq[ApiPropertyRelationshipDynamic]] = {
    // Get all property selectors that are not market for static properties
    val nonStaticPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("static")))
    // FIXME: other solutions possible, but such explicit naming of properties prlocations the use of generics
    val crossrefQuery = LocationsSystempropertydynamiccrossref.filter(_.locationId === locationId)

    for {
      pq <- getPropertiesDynamicQuery(crossrefQuery, nonStaticPropertySelectors)
      p <- if (getValues) {
        Future.sequence(pq.map(getPropertyRelationshipValues))
      }
      else {
        Future.successful(pq)
      }
    } yield p
  }

  protected def getPropertyStaticValues(locationId: Int, propertyRelationshipId: Int): Future[Seq[ApiPropertyRelationshipStatic]] = {
    val crossrefQuery = LocationsSystempropertystaticcrossref.filter(_.locationId === locationId).filter(_.id === propertyRelationshipId)
    for {
      propertyRelationships <- getPropertiesStaticQuery(crossrefQuery, None)
      values <- Future.sequence(propertyRelationships.map(getPropertyRelationshipValues))
    } yield values
  }

  // Private methods

  protected def getPropertyDynamicValues(locationId: Int, propertyRelationshipId: Int): Future[Seq[ApiPropertyRelationshipDynamic]] = {
    val crossrefQuery = LocationsSystempropertydynamiccrossref.filter(_.locationId === locationId).filter(_.id === propertyRelationshipId)
    for {
      propertyRelationships <- getPropertiesDynamicQuery(crossrefQuery, None)
      values <- Future.sequence(propertyRelationships.map(getPropertyRelationshipValues))
    } yield values
  }

  private def getPropertiesDynamicQuery(crossrefQuery: Query[LocationsSystempropertydynamiccrossref, LocationsSystempropertydynamiccrossrefRow, Seq],
                                        propertySelectors: Option[Seq[ApiBundleContextPropertySelection]]): Future[Seq[ApiPropertyRelationshipDynamic]] = {
    val dataQuery = propertySelectors match {
      case Some(selectors) =>
        val queries = selectors map { selector =>
          for {
            crossref <- crossrefQuery
            property <- selector.propertyName match {
              case None               => crossref.systemPropertyFk
              case Some(propertyName) => crossref.systemPropertyFk.filter(_.name === propertyName)
            }
            propertyType <- selector.propertyType match {
              case None                   => property.systemTypeFk
              case Some(propertyTypeName) => property.systemTypeFk.filter(_.name === propertyTypeName)
            }
            propertyUom <- selector.propertyUnitofmeasurement match {
              case None                  => property.systemUnitofmeasurementFk
              case Some(propertyUomName) => property.systemUnitofmeasurementFk.filter(_.name === propertyUomName)
            }
            field <- crossref.dataFieldFk
          } yield (crossref, property, propertyType, propertyUom, field)
        }

        queries reduceLeft { (query, propertySelectionQuery) =>
          query ++ propertySelectionQuery
        }

      case None =>
        for {
          crossref <- crossrefQuery
          property <- crossref.systemPropertyFk
          propertyType <- property.systemTypeFk
          propertyUom <- property.systemUnitofmeasurementFk
          field <- crossref.dataFieldFk
        } yield (crossref, property, propertyType, propertyUom, field)
    }

    DatabaseInfo.db.run(dataQuery.result).map { results =>
      results.map {
        case (crossref, property, propertyType, propertyUom, field) =>
          ApiPropertyRelationshipDynamic.fromDbModel(crossref, property, propertyType, propertyUom, field)
      }
    }
  }
}

