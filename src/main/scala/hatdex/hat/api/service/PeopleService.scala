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
trait PeopleService extends AbstractEntityService with PropertyService {

  def getLocations(personId: Int)(implicit getValues: Boolean): Future[Seq[ApiLocationRelationship]] = {
    val eventualLocationLinks = DatabaseInfo.db.run(PeoplePersonlocationcrossref.filter(_.personId === personId).result)

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

  def getOrganisations(personId: Int)(implicit getValues: Boolean): Future[Seq[ApiOrganisationRelationship]] = {
    val eventualOrganisationLinks = DatabaseInfo.db.run(PeoplePersonorganisationcrossref.filter(_.personId === personId).result)

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

  def getPeople(personID: Int)(implicit getValues: Boolean): Future[Seq[ApiPersonRelationship]] = {
    val query = for {
      link <- PeoplePersontopersoncrossref.filter(_.personOneId === personID)
      relationship <- link.peoplePersontopersonrelationshiptypeFk
    } yield (link, relationship)
    val eventualPersonLinks = DatabaseInfo.db.run(query.result)

    eventualPersonLinks flatMap { links =>
      // Sequence of futures to future of sequence
      Future.sequence {
        links map {
          case (link, relationship) =>
            getPerson(link.personTwoId) map { maybePerson =>
              maybePerson.map { e => ApiPersonRelationship(relationship.name, e) }
            }
        }
      } map (_.flatten)
    }
  }

  def getThings(eventID: Int)(implicit getValues: Boolean): Future[Seq[ApiThingRelationship]] = {
    Future.successful(Seq())
  }

  def getEvents(eventID: Int)(implicit getValues: Boolean): Future[Seq[ApiEventRelationship]] = {
    Future.successful(Seq())
  }

  protected def createLinkPerson(entityId: Int, personId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    Future.failed(new IllegalArgumentException("People must be linked via a defined relationship type"))
  }

  protected def createLinkPerson(entityId: Int, personId: Int, relationshipTypeId: Int, recordId: Int): Future[Int] = {
    val crossref = new PeoplePersontopersoncrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, personId, relationshipTypeId, true, recordId)
    DatabaseInfo.db.run((PeoplePersontopersoncrossref returning PeoplePersontopersoncrossref.map(_.id)) += crossref)
  }

  protected def createLinkLocation(entityId: Int, locationId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    val crossref = new PeoplePersonlocationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      locationId, entityId, relationshipType, true, recordId)
    DatabaseInfo.db.run((PeoplePersonlocationcrossref returning PeoplePersonlocationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkOrganisation(entityId: Int, organisationId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    val crossref = new PeoplePersonorganisationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      organisationId, entityId, relationshipType, true, recordId)
    DatabaseInfo.db.run((PeoplePersonorganisationcrossref returning PeoplePersonorganisationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkEvent(entityId: Int, eventId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    Future.failed(new RuntimeException("Operation Not Supprted"))
  }

  protected def createLinkThing(entityId: Int, thingId: Int, relationshipType: String, recordId: Int): Future[Int] = {
    Future.failed(new RuntimeException("Operation Not Supprted"))
  }

  /*
   * Link person to a property statically (tying it in with a specific record ID)
   */
  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                          fieldId: Int, relationshipType: String, propertyRecordId: Int): Future[Int] = {
    val crossref = new PeopleSystempropertydynamiccrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      fieldId, relationshipType,
      true, propertyRecordId)

    DatabaseInfo.db.run((PeopleSystempropertydynamiccrossref returning PeopleSystempropertydynamiccrossref.map(_.id)) += crossref)
  }

  /*
  * Link person to a property dynamically
  */
  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                         recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int): Future[Int] = {
    val crossref = new PeopleSystempropertystaticcrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      recordId, fieldId, relationshipType,
      true, propertyRecordId)

    DatabaseInfo.db.run((PeopleSystempropertystaticcrossref returning PeopleSystempropertystaticcrossref.map(_.id)) += crossref)
  }

  /*
   * Tag person with a type
   */
  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship): Future[Int] = {
    val entityType = new PeopleSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, typeId, relationship.relationshipType, true)
    DatabaseInfo.db.run((PeopleSystemtypecrossref returning PeopleSystemtypecrossref.map(_.id)) += entityType)
  }

  protected def getPropertiesStatic(personId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])(implicit getValues: Boolean): Future[Seq[ApiPropertyRelationshipStatic]] = {
    // Get all property selectors that are not market for dynamic properties
    val nonDynamicPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("dynamic")))

    // FIXME: other solutions possible, but such explicit naming of properties prpersons the use of generics
    val crossrefQuery = PeopleSystempropertystaticcrossref.filter(_.personId === personId)
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

  protected def getPropertiesDynamic(personId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])(implicit getValues: Boolean): Future[Seq[ApiPropertyRelationshipDynamic]] = {
    // Get all property selectors that are not market for static properties
    val nonStaticPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("static")))
    // FIXME: other solutions possible, but such explicit naming of properties prpersons the use of generics
    val crossrefQuery = PeopleSystempropertydynamiccrossref.filter(_.personId === personId)

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

  private def getPropertiesDynamicQuery(crossrefQuery: Query[PeopleSystempropertydynamiccrossref, PeopleSystempropertydynamiccrossrefRow, Seq],
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

  protected def getPropertyStaticValues(personId: Int, propertyRelationshipId: Int): Future[Seq[ApiPropertyRelationshipStatic]] = {
    val crossrefQuery = PeopleSystempropertystaticcrossref.filter(_.personId === personId).filter(_.id === propertyRelationshipId)
    for {
      propertyRelationships <- getPropertiesStaticQuery(crossrefQuery, None)
      values <- Future.sequence(propertyRelationships.map(getPropertyRelationshipValues))
    } yield values
  }

  // Private methods

  private def getPropertiesStaticQuery(crossrefQuery: Query[PeopleSystempropertystaticcrossref, PeopleSystempropertystaticcrossrefRow, Seq],
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

  protected def getPropertyDynamicValues(personId: Int, propertyRelationshipId: Int): Future[Seq[ApiPropertyRelationshipDynamic]] = {
    val crossrefQuery = PeopleSystempropertydynamiccrossref.filter(_.personId === personId).filter(_.id === propertyRelationshipId)
    for {
      propertyRelationships <- getPropertiesDynamicQuery(crossrefQuery, None)
      values <- Future.sequence(propertyRelationships.map(getPropertyRelationshipValues))
    } yield values
  }
}

