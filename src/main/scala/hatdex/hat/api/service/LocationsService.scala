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

import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.util.{Failure, Try}


// this trait defines our service behavior independently from the service actor
trait LocationsService extends AbstractEntityService with PropertyService {

  protected def createLinkLocation(entityId: Int, locationId: Int, relationshipType: String, recordId: Int)
                                  (implicit session: Session): Try[Int] = {
    val crossref = new LocationsLocationtolocationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      locationId, entityId, relationshipType, true, recordId)
    Try((LocationsLocationtolocationcrossref returning LocationsLocationtolocationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkThing(entityId: Int, thingId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int] = {
    val crossref = new LocationsLocationthingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      thingId, entityId, relationshipType, true, recordId)
    Try((LocationsLocationthingcrossref returning LocationsLocationthingcrossref.map(_.id)) += crossref)
  }

  protected def createLinkOrganisation(entityId: Int, organisationId: Int, relationshipType: String, recordId: Int)
                                      (implicit session: Session): Try[Int] = {
    Failure(new NotImplementedError("Operation Not Supprted"))
  }

  protected def createLinkPerson(entityId: Int, personId: Int, relationshipType: String, recordId: Int)
                                (implicit session: Session): Try[Int] = {
    Failure(new NotImplementedError("Operation Not Supprted"))
  }

  protected def createLinkEvent(entityId: Int, eventId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int] = {
    Failure(new NotImplementedError("Operation Not Supprted"))
  }

  /*
   * Link location to a property statically (tying it in with a specific record ID)
   */
  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                          fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                         (implicit session: Session): Try[Int] = {
    val crossref = new LocationsSystempropertydynamiccrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      fieldId, relationshipType,
      true, propertyRecordId)

    Try((LocationsSystempropertydynamiccrossref returning LocationsSystempropertydynamiccrossref.map(_.id)) += crossref)
  }

  /*
   * Link location to a property dynamically
   */
  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                         recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                        (implicit session: Session): Try[Int] = {
    val crossref = new LocationsSystempropertystaticcrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      recordId, fieldId, relationshipType,
      true, propertyRecordId)

    Try((LocationsSystempropertystaticcrossref returning LocationsSystempropertystaticcrossref.map(_.id)) += crossref)
  }

  /*
   * Tag location with a type
   */
  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship)
                             (implicit session: Session): Try[Int] = {

    val entityType = new LocationsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, typeId, relationship.relationshipType, true)
    Try((LocationsSystemtypecrossref returning LocationsSystemtypecrossref.map(_.id)) += entityType)
  }

  def getThings(locationID: Int)
               (implicit session: Session, getValues: Boolean): Seq[ApiThingRelationship] = {
    val locationLinks = LocationsLocationthingcrossref.filter(_.locationId === locationID).run

    locationLinks flatMap { link: LocationsLocationthingcrossrefRow =>
      getThing(link.thingId) map { thing =>
        new ApiThingRelationship(link.relationshipType, thing)
      }
    }
  }

  def getLocations(locationID: Int)
                  (implicit session: Session, getValues: Boolean): Seq[ApiLocationRelationship] = {
    val locationLinks = LocationsLocationtolocationcrossref.filter(_.locOneId === locationID).run

    locationLinks flatMap { link: LocationsLocationtolocationcrossrefRow =>
      val apiLocation = getLocation(link.locTwoId)
      apiLocation.map { location =>
        new ApiLocationRelationship(link.relationshipType, location)
      }
    }
  }

  protected def getOrganisations(entityId: Int)(implicit session: Session, getValues: Boolean): Seq[ApiOrganisationRelationship] = {
    // No links directly from Location
    Seq()
  }

  protected def getPeople(entityId: Int)(implicit session: Session, getValues: Boolean): Seq[ApiPersonRelationship] = {
    // No links directly from Location
    Seq()
  }

  protected def getEvents(entityId: Int)(implicit session: Session, getValues: Boolean): Seq[ApiEventRelationship] = {
    // No links directly from Location
    Seq()
  }

  protected def getPropertiesStatic(locationId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])
                                   (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipStatic] = {
    // Get all property selectors that are not market for dynamic properties
    val nonDynamicPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("dynamic"))
    )

    // FIXME: other solutions possible, but such explicit naming of properties prevents the use of generics
    val crossrefQuery = LocationsSystempropertystaticcrossref.filter(_.locationId === locationId)
    val properties = getPropertiesStaticQuery(crossrefQuery, nonDynamicPropertySelectors)
    getValues match {
      case false =>
        properties
      case true =>
        properties.map(getPropertyRelationshipValues)
    }
  }

  protected def getPropertiesDynamic(locationId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])
                                    (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipDynamic] = {
    // Get all property selectors that are not market for static properties
    val nonStaticPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("static"))
    )
    // FIXME: other solutions possible, but such explicit naming of properties prevents the use of generics
    val crossrefQuery = LocationsSystempropertydynamiccrossref.filter(_.locationId === locationId)
    val properties = getPropertiesDynamicQuery(crossrefQuery, nonStaticPropertySelectors)
    getValues match {
      case false =>
        properties
      case true =>
        properties.map(getPropertyRelationshipValues)
    }
  }

  protected def getPropertyStaticValues(locationId: Int, propertyRelationshipId: Int)
                                       (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {
    val crossrefQuery = LocationsSystempropertystaticcrossref.filter(_.locationId === locationId).filter(_.id === propertyRelationshipId)
    val propertyRelationships = getPropertiesStaticQuery(crossrefQuery, None)
    propertyRelationships.map(getPropertyRelationshipValues)
  }

  protected def getPropertyDynamicValues(locationId: Int, propertyRelationshipId: Int)
                                        (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {
    val crossrefQuery = LocationsSystempropertydynamiccrossref.filter(_.locationId === locationId).filter(_.id === propertyRelationshipId)
    val propertyRelationships = getPropertiesDynamicQuery(crossrefQuery, None)
    propertyRelationships.map(getPropertyRelationshipValues)
  }

  // Private methods

  private def getPropertiesStaticQuery(crossrefQuery: Query[LocationsSystempropertystaticcrossref, LocationsSystempropertystaticcrossrefRow, Seq],
                                       propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])
                                      (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {
    val dataQuery = propertySelectors match {
      case Some(selectors) =>
        val queries = selectors map { selector =>
          for {
            crossref <- crossrefQuery
            property <- selector.propertyName match {
              case None =>
                crossref.systemPropertyFk
              case Some(propertyName) =>
                crossref.systemPropertyFk.filter(_.name === propertyName)
            }
            propertyType <- selector.propertyType match {
              case None =>
                property.systemTypeFk
              case Some(propertyTypeName) =>
                property.systemTypeFk.filter(_.name === propertyTypeName)
            }
            propertyUom <- selector.propertyUnitofmeasurement match {
              case None =>
                property.systemUnitofmeasurementFk
              case Some(propertyUomName) =>
                property.systemUnitofmeasurementFk.filter(_.name === propertyUomName)
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

    val data = dataQuery.run

    data.map {
      case (crossref: LocationsSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) =>
        ApiPropertyRelationshipStatic.fromDbModel(crossref, property, propertyType, propertyUom, field, record)
    }
  }

  private def getPropertiesDynamicQuery(crossrefQuery: Query[LocationsSystempropertydynamiccrossref, LocationsSystempropertydynamiccrossrefRow, Seq],
                                        propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])
                                       (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {

    val dataQuery = propertySelectors match {
      case Some(selectors) =>
        val queries = selectors map { selector =>
          for {
            crossref <- crossrefQuery
            property <- selector.propertyName match {
              case None =>
                crossref.systemPropertyFk
              case Some(propertyName) =>
                crossref.systemPropertyFk.filter(_.name === propertyName)
            }
            propertyType <- selector.propertyType match {
              case None =>
                property.systemTypeFk
              case Some(propertyTypeName) =>
                property.systemTypeFk.filter(_.name === propertyTypeName)
            }
            propertyUom <- selector.propertyUnitofmeasurement match {
              case None =>
                property.systemUnitofmeasurementFk
              case Some(propertyUomName) =>
                property.systemUnitofmeasurementFk.filter(_.name === propertyUomName)
            }
            field <- crossref.dataFieldFk
          } yield (crossref, property, propertyType, propertyUom, field)
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
        } yield (crossref, property, propertyType, propertyUom, field)
    }

    val data = dataQuery.run

    data.map {
      case (crossref: LocationsSystempropertydynamiccrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow) =>
        ApiPropertyRelationshipDynamic.fromDbModel(crossref, property, propertyType, propertyUom, field)
    }
  }
}

