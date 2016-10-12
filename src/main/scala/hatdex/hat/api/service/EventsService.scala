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

import scala.concurrent.{ExecutionContext, Future}

// this trait defines our service behavior independently from the service actor
trait EventsService extends AbstractEntityService with PropertyService {
  implicit val dalExecutionContext: ExecutionContext

  protected def createLinkLocation(entityId: Int, locationId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    val crossref = new EventsEventlocationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      locationId, entityId, relationshipType, true, recordId)
    DatabaseInfo.db.run((EventsEventlocationcrossref returning EventsEventlocationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkOrganisation(entityId: Int, organisationId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    val crossref = new EventsEventorganisationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      organisationId, entityId, relationshipType, true, recordId)
    DatabaseInfo.db.run((EventsEventorganisationcrossref returning EventsEventorganisationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkPerson(entityId: Int, personId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    val crossref = new EventsEventpersoncrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      personId, entityId, relationshipType, true, recordId)
    DatabaseInfo.db.run((EventsEventpersoncrossref returning EventsEventpersoncrossref.map(_.id)) += crossref)
  }

  protected def createLinkThing(entityId: Int, thingId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    val crossref = new EventsEventthingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      thingId, entityId, relationshipType, true, recordId)
    DatabaseInfo.db.run((EventsEventthingcrossref returning EventsEventthingcrossref.map(_.id)) += crossref)
  }

  protected def createLinkEvent(entityId: Int, eventId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    val crossref = new EventsEventtoeventcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      eventId, entityId, relationshipType, true, recordId)
    DatabaseInfo.db.run((EventsEventtoeventcrossref returning EventsEventtoeventcrossref.map(_.id)) += crossref)
  }

  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                          fieldId: Int, relationshipType: String, propertyRecordId: Int): Future[Int] = {
    val crossref = new EventsSystempropertydynamiccrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      fieldId, relationshipType,
      true, propertyRecordId)

    DatabaseInfo.db.run((EventsSystempropertydynamiccrossref returning EventsSystempropertydynamiccrossref.map(_.id)) += crossref)
  }

  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                         recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int): Future[Int] = {
    val crossref = new EventsSystempropertystaticcrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      recordId, fieldId, relationshipType,
      true, propertyRecordId)

    DatabaseInfo.db.run((EventsSystempropertystaticcrossref returning EventsSystempropertystaticcrossref.map(_.id)) += crossref)
  }

  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship): Future[Int] = {
    val entityType = new EventsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, typeId, relationship.relationshipType, true)
    DatabaseInfo.db.run((EventsSystemtypecrossref returning EventsSystemtypecrossref.map(_.id)) += entityType)
  }

  protected def getLocations(eventID: Int)(implicit getValues: Boolean): Future[Seq[ApiLocationRelationship]] = {
    val eventualLocationLinks = DatabaseInfo.db.run(EventsEventlocationcrossref.filter(_.eventId === eventID).result)

    eventualLocationLinks flatMap { links =>
      // Sequence of futures to future of sequence
      Future.sequence {
        links map { link =>
          getLocation(link.locationId) map { maybeLocation =>
            maybeLocation.map { t => ApiLocationRelationship(link.relationshipType, t) }
          }
        }
      } map (_.flatten)
    }
  }

  protected def getOrganisations(eventID: Int)(implicit getValues: Boolean): Future[Seq[ApiOrganisationRelationship]] = {
    val eventualOrganisationLinks = DatabaseInfo.db.run(EventsEventorganisationcrossref.filter(_.eventId === eventID).result)

    eventualOrganisationLinks flatMap { links =>
      // Sequence of futures to future of sequence
      Future.sequence {
        links map { link =>
          getOrganisation(link.organisationId) map { maybeOrganisation =>
            maybeOrganisation.map { t => ApiOrganisationRelationship(link.relationshipType, t) }
          }
        }
      } map (_.flatten)
    }
  }

  protected def getPeople(eventID: Int)(implicit getValues: Boolean): Future[Seq[ApiPersonRelationship]] = {
    val eventualPersonLinks = DatabaseInfo.db.run(EventsEventpersoncrossref.filter(_.eventId === eventID).result)

    eventualPersonLinks flatMap { links =>
      // Sequence of futures to future of sequence
      Future.sequence {
        links map { link =>
          getPerson(link.personId) map { maybePerson =>
            maybePerson.map { t => ApiPersonRelationship(link.relationshipType, t) }
          }
        }
      } map (_.flatten)
    }
  }

  protected def getThings(eventID: Int)(implicit getValues: Boolean): Future[Seq[ApiThingRelationship]] = {
    val eventualThingLinks = DatabaseInfo.db.run(EventsEventthingcrossref.filter(_.eventId === eventID).result)

    eventualThingLinks flatMap { links =>
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

  protected def getEvents(eventID: Int)(implicit getValues: Boolean): Future[Seq[ApiEventRelationship]] = {
    val eventualEventLinks = DatabaseInfo.db.run(EventsEventtoeventcrossref.filter(_.eventOneId === eventID).result)

    eventualEventLinks flatMap { links =>
      // Sequence of futures to future of sequence
      Future.sequence {
        links map { link =>
          getEvent(link.eventTwoId) map { maybeEvent =>
            maybeEvent.map { e => ApiEventRelationship(link.relationshipType, e) }
          }
        }
      } map (_.flatten)
    }
  }

  protected def getPropertiesStatic(eventId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])(implicit getValues: Boolean): Future[Seq[ApiPropertyRelationshipStatic]] = {
    // Get all property selectors that are not market for dynamic properties
    val nonDynamicPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("dynamic")))

    // FIXME: other solutions possible, but such explicit naming of properties prevents the use of generics
    val crossrefQuery = EventsSystempropertystaticcrossref.filter(_.eventId === eventId)
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

  protected def getPropertiesDynamic(eventId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])(implicit getValues: Boolean): Future[Seq[ApiPropertyRelationshipDynamic]] = {
    // Get all property selectors that are not market for static properties
    val nonStaticPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("static")))
    // FIXME: other solutions possible, but such explicit naming of properties prevents the use of generics
    val crossrefQuery = EventsSystempropertydynamiccrossref.filter(_.eventId === eventId)

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

  private def getPropertiesDynamicQuery(crossrefQuery: Query[EventsSystempropertydynamiccrossref, EventsSystempropertydynamiccrossrefRow, Seq],
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

  protected def getPropertyStaticValues(eventId: Int, propertyRelationshipId: Int): Future[Seq[ApiPropertyRelationshipStatic]] = {
    val crossrefQuery = EventsSystempropertystaticcrossref.filter(_.eventId === eventId).filter(_.id === propertyRelationshipId)
    for {
      propertyRelationships <- getPropertiesStaticQuery(crossrefQuery, None)
      values <- Future.sequence(propertyRelationships.map(getPropertyRelationshipValues))
    } yield values
  }

  // Private methods

  private def getPropertiesStaticQuery(crossrefQuery: Query[EventsSystempropertystaticcrossref, EventsSystempropertystaticcrossrefRow, Seq],
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

  protected def getPropertyDynamicValues(eventId: Int, propertyRelationshipId: Int): Future[Seq[ApiPropertyRelationshipDynamic]] = {
    val crossrefQuery = EventsSystempropertydynamiccrossref.filter(_.eventId === eventId).filter(_.id === propertyRelationshipId)
    for {
      propertyRelationships <- getPropertiesDynamicQuery(crossrefQuery, None)
      values <- Future.sequence(propertyRelationships.map(getPropertyRelationshipValues))
    } yield values
  }

}

