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
trait OrganisationsService extends AbstractEntityService with PropertyService {

  protected def createLinkLocation(entityId: Int, locationId: Int, relationshipType: String, recordId: Int)
                                  (implicit session: Session): Try[Int] = {
    val crossref = new OrganisationsOrganisationlocationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      locationId, entityId, relationshipType, true, recordId)
    Try((OrganisationsOrganisationlocationcrossref returning OrganisationsOrganisationlocationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkThing(entityId: Int, thingId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int] = {
    //    val crossref = new OrganisationsOrganisationthingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
    //      entityId, thingId, relationshipType, true, recordId)
    //    Try((OrganisationsOrganisationthingcrossref returning OrganisationsOrganisationthingcrossref.map(_.id)) += crossref)
    Failure(new NotImplementedError("Operation Not Supprted"))
  }

  protected def createLinkOrganisation(entityId: Int, organisationId: Int, relationshipType: String, recordId: Int)
                                      (implicit session: Session): Try[Int] = {
    val crossref = new OrganisationsOrganisationtoorganisationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      organisationId, entityId, relationshipType, true, recordId)
    Try((OrganisationsOrganisationtoorganisationcrossref returning OrganisationsOrganisationtoorganisationcrossref.map(_.id)) += crossref)
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
   * Link organisation to a property statically (tying it in with a specific record ID)
   */
  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                          fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                         (implicit session: Session): Try[Int] = {
    val crossref = new OrganisationsSystempropertydynamiccrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      fieldId, relationshipType,
      true, propertyRecordId)

    Try((OrganisationsSystempropertydynamiccrossref returning OrganisationsSystempropertydynamiccrossref.map(_.id)) += crossref)
  }

  /*
   * Link organisation to a property dynamically
   */
  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                         recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                        (implicit session: Session): Try[Int] = {
    val crossref = new OrganisationsSystempropertystaticcrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      recordId, fieldId, relationshipType,
      true, propertyRecordId)

    Try((OrganisationsSystempropertystaticcrossref returning OrganisationsSystempropertystaticcrossref.map(_.id)) += crossref)
  }

  /*
   * Tag organisation with a type
   */
  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship)
                             (implicit session: Session): Try[Int] = {

    val organisationType = new OrganisationsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), entityId, typeId, relationship.relationshipType, true)
    Try((OrganisationsSystemtypecrossref returning OrganisationsSystemtypecrossref.map(_.id)) += organisationType)
  }

  def getLocations(organisationId: Int)
                  (implicit session: Session, getValues: Boolean): Seq[ApiLocationRelationship] = {

    val locationLinks = OrganisationsOrganisationlocationcrossref.filter(_.organisationId === organisationId).run

    locationLinks flatMap { link: OrganisationsOrganisationlocationcrossrefRow =>
      val apiLocation = getLocation(link.locationId)
      apiLocation.map { location =>
        new ApiLocationRelationship(link.relationshipType, location)
      }
    }
  }

  def getOrganisations(organisationID: Int)
                      (implicit session: Session, getValues: Boolean): Seq[ApiOrganisationRelationship] = {
    val organisationLinks = OrganisationsOrganisationtoorganisationcrossref.filter(_.organisationOneId === organisationID).run

    organisationLinks flatMap { link: OrganisationsOrganisationtoorganisationcrossrefRow =>
      val apiOrganisation = getOrganisation(link.organisationTwoId)
      apiOrganisation.map { organisation =>
        new ApiOrganisationRelationship(link.relationshipType, organisation)
      }
    }
  }

  def getPeople(entityId: Int)
               (implicit session: Session, getValues: Boolean): Seq[ApiPersonRelationship] = {
    Seq()
  }

  def getThings(entityId: Int)
               (implicit session: Session, getValues: Boolean): Seq[ApiThingRelationship] = {
    val thingLinks = OrganisationsOrganisationthingcrossref.filter(_.organisationId === entityId).run

    thingLinks flatMap { link: OrganisationsOrganisationthingcrossrefRow =>
      val apiThing = getThing(link.thingId)
      apiThing.map { thing =>
        new ApiThingRelationship(link.relationshipType, thing)
      }
    }
  }

  def getEvents(eventID: Int)
               (implicit session: Session, getValues: Boolean): Seq[ApiEventRelationship] = {
    Seq()
  }

  protected def getPropertiesStatic(organisationId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])
                                   (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipStatic] = {
    // Get all property selectors that are not market for dynamic properties
    val nonDynamicPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("dynamic"))
    )

    // FIXME: other solutions possible, but such explicit naming of properties prevents the use of generics
    val crossrefQuery = OrganisationsSystempropertystaticcrossref.filter(_.organisationId === organisationId)
    val properties = getPropertiesStaticQuery(crossrefQuery, nonDynamicPropertySelectors)
    getValues match {
      case false =>
        properties
      case true =>
        properties.map(getPropertyRelationshipValues)
    }
  }

  protected def getPropertiesDynamic(organisationId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])
                                    (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipDynamic] = {
    // Get all property selectors that are not market for static properties
    val nonStaticPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("static"))
    )
    // FIXME: other solutions possible, but such explicit naming of properties prevents the use of generics
    val crossrefQuery = OrganisationsSystempropertydynamiccrossref.filter(_.organisationId === organisationId)
    val properties = getPropertiesDynamicQuery(crossrefQuery, nonStaticPropertySelectors)
    getValues match {
      case false =>
        properties
      case true =>
        properties.map(getPropertyRelationshipValues)
    }
  }

  protected def getPropertyStaticValues(organisationId: Int, propertyRelationshipId: Int)
                                       (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {
    val crossrefQuery = OrganisationsSystempropertystaticcrossref.filter(_.organisationId === organisationId).filter(_.id === propertyRelationshipId)
    val propertyRelationships = getPropertiesStaticQuery(crossrefQuery, None)
    propertyRelationships.map(getPropertyRelationshipValues)
  }

  protected def getPropertyDynamicValues(organisationId: Int, propertyRelationshipId: Int)
                                        (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {
    val crossrefQuery = OrganisationsSystempropertydynamiccrossref.filter(_.organisationId === organisationId).filter(_.id === propertyRelationshipId)
    val propertyRelationships = getPropertiesDynamicQuery(crossrefQuery, None)
    propertyRelationships.map(getPropertyRelationshipValues)
  }

  // Private methods

  private def getPropertiesStaticQuery(crossrefQuery: Query[OrganisationsSystempropertystaticcrossref, OrganisationsSystempropertystaticcrossrefRow, Seq],
                                       propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])
                                      (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {
    val dataQuery = propertySelectors match {
      case Some(selectors) =>
        val queries = selectors map { selector =>
          for {
            crossref <- crossrefQuery
            property <- crossref.systemPropertyFk if selector.propertyName.isEmpty || selector.propertyName == Some(property.name)
            propertyType <- property.systemTypeFk if selector.propertyType.isEmpty || selector.propertyType == Some(propertyType.name)
            propertyUom <- property.systemUnitofmeasurementFk if selector.propertyUnitofmeasurement.isEmpty || selector.propertyUnitofmeasurement == Some(propertyUom.name)
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
      case (crossref: OrganisationsSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) =>
        ApiPropertyRelationshipStatic.fromDbModel(crossref, property, propertyType, propertyUom, field, record)
    }
  }

  private def getPropertiesDynamicQuery(crossrefQuery: Query[OrganisationsSystempropertydynamiccrossref, OrganisationsSystempropertydynamiccrossrefRow, Seq],
                                        propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])
                                       (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {

    val dataQuery = propertySelectors match {
      case Some(selectors) =>
        val queries = selectors map { selector =>
          for {
            crossref <- crossrefQuery
            property <- crossref.systemPropertyFk if selector.propertyName.isEmpty || selector.propertyName == Some(property.name)
            propertyType <- property.systemTypeFk if selector.propertyType.isEmpty || selector.propertyType == Some(propertyType.name)
            propertyUom <- property.systemUnitofmeasurementFk if selector.propertyUnitofmeasurement.isEmpty || selector.propertyUnitofmeasurement == Some(propertyUom.name)
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
      case (crossref: OrganisationsSystempropertydynamiccrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow) =>
        ApiPropertyRelationshipDynamic.fromDbModel(crossref, property, propertyType, propertyUom, field)
    }
  }
}

