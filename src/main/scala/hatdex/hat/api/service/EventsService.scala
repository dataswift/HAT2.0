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

import scala.util.Try


// this trait defines our service behavior independently from the service actor
trait EventsService extends AbstractEntityService with PropertyService {

  protected def createLinkLocation(entityId: Int, locationId: Int, relationshipType: String, recordId: Int)
                                  (implicit session: Session): Try[Int] = {
    val crossref = new EventsEventlocationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      locationId, entityId, relationshipType, true, recordId)
    Try((EventsEventlocationcrossref returning EventsEventlocationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkOrganisation(entityId: Int, organisationId: Int, relationshipType: String, recordId: Int)
                                      (implicit session: Session): Try[Int] = {
    val crossref = new EventsEventorganisationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      organisationId, entityId, relationshipType, true, recordId)
    Try((EventsEventorganisationcrossref returning EventsEventorganisationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkPerson(entityId: Int, personId: Int, relationshipType: String, recordId: Int)
                                (implicit session: Session): Try[Int] = {
    val crossref = new EventsEventpersoncrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      personId, entityId, relationshipType, true, recordId)
    Try((EventsEventpersoncrossref returning EventsEventpersoncrossref.map(_.id)) += crossref)
  }

  protected def createLinkThing(entityId: Int, thingId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int] = {
    val crossref = new EventsEventthingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      thingId, entityId, relationshipType, true, recordId)
    Try((EventsEventthingcrossref returning EventsEventthingcrossref.map(_.id)) += crossref)
  }

  protected def createLinkEvent(entityId: Int, eventId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int] = {
    val crossref = new EventsEventtoeventcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      eventId, entityId, relationshipType, true, recordId)
    Try((EventsEventtoeventcrossref returning EventsEventtoeventcrossref.map(_.id)) += crossref)
  }

  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                          fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                         (implicit session: Session): Try[Int] = {
    val crossref = new EventsSystempropertydynamiccrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      fieldId, relationshipType,
      true, propertyRecordId)

    Try((EventsSystempropertydynamiccrossref returning EventsSystempropertydynamiccrossref.map(_.id)) += crossref)
  }

  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                         recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                        (implicit session: Session): Try[Int] = {
    val crossref = new EventsSystempropertystaticcrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      recordId, fieldId, relationshipType,
      true, propertyRecordId)

    Try((EventsSystempropertystaticcrossref returning EventsSystempropertystaticcrossref.map(_.id)) += crossref)
  }

  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship)
                             (implicit session: Session): Try[Int] = {
    val entityType = new EventsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, typeId, relationship.relationshipType, true)
    Try((EventsSystemtypecrossref returning EventsSystemtypecrossref.map(_.id)) += entityType)
  }

  protected def getLocations(eventId: Int)
                            (implicit session: Session, getValues: Boolean): Seq[ApiLocationRelationship] = {

    val locationLinks = EventsEventlocationcrossref.filter(_.eventId === eventId).run

    locationLinks flatMap { link: EventsEventlocationcrossrefRow =>
      val apiLocation = getLocation(link.locationId)
      apiLocation.map { location =>
        new ApiLocationRelationship(link.relationshipType, location)
      }
    }
  }

  protected def getOrganisations(eventID: Int)
                                (implicit session: Session, getValues: Boolean): Seq[ApiOrganisationRelationship] = {
    val links = EventsEventorganisationcrossref.filter(_.eventId === eventID).run

    links flatMap { link: EventsEventorganisationcrossrefRow =>
      val apiOrganisation = getOrganisation(link.organisationId)
      apiOrganisation.map { organisation =>
        new ApiOrganisationRelationship(link.relationshipType, organisation)
      }
    }
  }

  protected def getPeople(eventID: Int)
                         (implicit session: Session, getValues: Boolean): Seq[ApiPersonRelationship] = {
    val links = EventsEventpersoncrossref.filter(_.eventId === eventID).run

    links flatMap { link: EventsEventpersoncrossrefRow =>
      val apiPerson = getPerson(link.personId)
      apiPerson.map { person =>
        new ApiPersonRelationship(link.relationshipType, person)
      }
    }
  }

  protected def getThings(eventID: Int)
                         (implicit session: Session, getValues: Boolean): Seq[ApiThingRelationship] = {
    val links = EventsEventthingcrossref.filter(_.eventId === eventID).run

    links flatMap { link: EventsEventthingcrossrefRow =>
      val apiThing = getThing(link.thingId)
      apiThing.map { thing =>
        new ApiThingRelationship(link.relationshipType, thing)
      }
    }
  }

  protected def getEvents(eventID: Int)
                         (implicit session: Session, getValues: Boolean): Seq[ApiEventRelationship] = {
    val eventLinks = EventsEventtoeventcrossref.filter(_.eventOneId === eventID).run
    var eventIds = eventLinks.map(_.eventTwoId)

    eventLinks flatMap { link: EventsEventtoeventcrossrefRow =>
      val apiEvent = getEvent(link.eventTwoId)
      apiEvent.map { event =>
        new ApiEventRelationship(link.relationshipType, event)
      }
    }
  }

  protected def getPropertiesStatic(eventId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])
                                   (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipStatic] = {
    // Get all property selectors that are not market for dynamic properties
    val nonDynamicPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("dynamic"))
    )

    // FIXME: other solutions possible, but such explicit naming of properties prevents the use of generics
    val crossrefQuery = EventsSystempropertystaticcrossref.filter(_.eventId === eventId)
    val properties = getPropertiesStaticQuery(crossrefQuery, nonDynamicPropertySelectors)
    getValues match {
      case false =>
        properties
      case true =>
        properties.map(getPropertyRelationshipValues)
    }
  }

  protected def getPropertiesDynamic(eventId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])
                                    (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipDynamic] = {
    // Get all property selectors that are not market for static properties
    val nonStaticPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("static"))
    )
    // FIXME: other solutions possible, but such explicit naming of properties prevents the use of generics
    val crossrefQuery = EventsSystempropertydynamiccrossref.filter(_.eventId === eventId)
    val properties = getPropertiesDynamicQuery(crossrefQuery, nonStaticPropertySelectors)
    getValues match {
      case false =>
        properties
      case true =>
        properties.map(getPropertyRelationshipValues)
    }
  }

  protected def getPropertyStaticValues(eventId: Int, propertyRelationshipId: Int)
                                       (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {
    val crossrefQuery = EventsSystempropertystaticcrossref.filter(_.eventId === eventId).filter(_.id === propertyRelationshipId)
    val propertyRelationships = getPropertiesStaticQuery(crossrefQuery, None)
    propertyRelationships.map(getPropertyRelationshipValues)
  }

  protected def getPropertyDynamicValues(eventId: Int, propertyRelationshipId: Int)
                                        (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {
    val crossrefQuery = EventsSystempropertydynamiccrossref.filter(_.eventId === eventId).filter(_.id === propertyRelationshipId)
    val propertyRelationships = getPropertiesDynamicQuery(crossrefQuery, None)
    propertyRelationships.map(getPropertyRelationshipValues)
  }

  // Private methods

  private def getPropertiesStaticQuery(crossrefQuery: Query[EventsSystempropertystaticcrossref, EventsSystempropertystaticcrossrefRow, Seq],
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
      case (crossref: EventsSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) =>
        ApiPropertyRelationshipStatic.fromDbModel(crossref, property, propertyType, propertyUom, field, record)
    }
  }

  private def getPropertiesDynamicQuery(crossrefQuery: Query[EventsSystempropertydynamiccrossref, EventsSystempropertydynamiccrossrefRow, Seq],
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
      case (crossref: EventsSystempropertydynamiccrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow) =>
        ApiPropertyRelationshipDynamic.fromDbModel(crossref, property, propertyType, propertyUom, field)
    }
  }

}

