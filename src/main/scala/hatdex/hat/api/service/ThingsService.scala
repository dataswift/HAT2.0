package hatdex.hat.api.service

import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.util.{Failure, Try}


// this trait defines our service behavior independently from the service actor
trait ThingsService extends AbstractEntityService with PropertyService {

  protected def createLinkThing(entityId: Int, thingId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int] = {
    val crossref = new ThingsThingtothingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      thingId, entityId, relationshipType, true, recordId)
    Try((ThingsThingtothingcrossref returning ThingsThingtothingcrossref.map(_.id)) += crossref)
  }

  protected def createLinkPerson(entityId: Int, personId: Int, relationshipType: String, recordId: Int)
                                (implicit session: Session): Try[Int] = {
    // FIXME: personID and thingID swapped around in the DB!
    val crossref = new ThingsThingpersoncrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      personId, entityId, relationshipType, true, recordId)
    Try((ThingsThingpersoncrossref returning ThingsThingpersoncrossref.map(_.id)) += crossref)
  }

  protected def createLinkEvent(entityId: Int, eventId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int] = {
    Failure(new NotImplementedError("Operation Not Supprted"))
  }

  protected def createLinkOrganisation(entityId: Int, organisationId: Int, relationshipType: String, recordId: Int)
                                      (implicit session: Session): Try[Int] = {
    Failure(new NotImplementedError("Operation Not Supprted"))
  }

  protected def createLinkLocation(entityId: Int, locationId: Int, relationshipType: String, recordId: Int)
                                  (implicit session: Session): Try[Int] = {
    Failure(new NotImplementedError("Operation Not Supprted"))
  }

  /*
   * Link thing to a property statically (tying it in with a specific record ID)
   */
  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                          fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                         (implicit session: Session): Try[Int] = {
    val crossref = new ThingsSystempropertydynamiccrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      fieldId, relationshipType,
      true, propertyRecordId)

    Try((ThingsSystempropertydynamiccrossref returning ThingsSystempropertydynamiccrossref.map(_.id)) += crossref)
  }

  /*
   * Link thing to a property dynamically
   */
  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                         recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                        (implicit session: Session): Try[Int] = {
    val crossref = new ThingsSystempropertystaticcrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      recordId, fieldId, relationshipType,
      true, propertyRecordId)

    Try((ThingsSystempropertystaticcrossref returning ThingsSystempropertystaticcrossref.map(_.id)) += crossref)
  }

  /*
   * Tag thing with a type
   */
  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship)
                             (implicit session: Session): Try[Int] = {
    val entityType = new ThingsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, typeId, relationship.relationshipType, true)
    Try((ThingsSystemtypecrossref returning ThingsSystemtypecrossref.map(_.id)) += entityType)
  }

  def getLocations(thingId: Int)
                  (implicit session: Session, getValues: Boolean): Seq[ApiLocationRelationship] = {

    Seq()
  }

  def getOrganisations(thingID: Int)
                      (implicit session: Session, getValues: Boolean): Seq[ApiOrganisationRelationship] = {
    Seq()
  }

  def getPeople(thingID: Int)
               (implicit session: Session, getValues: Boolean): Seq[ApiPersonRelationship] = {
    val links = ThingsThingpersoncrossref.filter(_.thingId === thingID).run

    links flatMap { link: ThingsThingpersoncrossrefRow =>
      val apiPerson = getPerson(link.personId)
      apiPerson.map { person =>
        new ApiPersonRelationship(link.relationshipType, person)
      }
    }
  }

  def getEvents(eventID: Int)
               (implicit session: Session, getValues: Boolean): Seq[ApiEventRelationship] = {
    Seq()
  }

  def getThings(thingID: Int)
               (implicit session: Session, getValues: Boolean): Seq[ApiThingRelationship] = {
    val thingLinks = ThingsThingtothingcrossref.filter(_.thingOneId === thingID).run
    var thingIds = thingLinks.map(_.thingTwoId)

    thingLinks flatMap { link: ThingsThingtothingcrossrefRow =>
      val apiThing = getThing(link.thingTwoId)
      apiThing.map { thing =>
        new ApiThingRelationship(link.relationshipType, thing)
      }
    }
  }

  protected def getPropertiesStatic(thingId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])
                                   (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipStatic] = {
    // Get all property selectors that are not market for dynamic properties
    val nonDynamicPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("dynamic"))
    )

    // FIXME: other solutions possible, but such explicit naming of properties prevents the use of generics
    val crossrefQuery = ThingsSystempropertystaticcrossref.filter(_.thingId === thingId)
    val properties = getPropertiesStaticQuery(crossrefQuery, nonDynamicPropertySelectors)
    getValues match {
      case false =>
        properties
      case true =>
        properties.map(getPropertyRelationshipValues)
    }
  }

  protected def getPropertiesDynamic(thingId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])
                                    (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipDynamic] = {
    // Get all property selectors that are not market for static properties
    val nonStaticPropertySelectors = propertySelectors.map(
      _.filterNot(_.propertyRelationshipKind.contains("static"))
    )
    // FIXME: other solutions possible, but such explicit naming of properties prevents the use of generics
    val crossrefQuery = ThingsSystempropertydynamiccrossref.filter(_.thingId === thingId)
    val properties = getPropertiesDynamicQuery(crossrefQuery, nonStaticPropertySelectors)
    getValues match {
      case false =>
        properties
      case true =>
        properties.map(getPropertyRelationshipValues)
    }
  }

  protected def getPropertyStaticValues(thingId: Int, propertyRelationshipId: Int)
                                       (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {
    val crossrefQuery = ThingsSystempropertystaticcrossref.filter(_.thingId === thingId).filter(_.id === propertyRelationshipId)
    val propertyRelationships = getPropertiesStaticQuery(crossrefQuery, None)
    propertyRelationships.map(getPropertyRelationshipValues)
  }

  protected def getPropertyDynamicValues(thingId: Int, propertyRelationshipId: Int)
                                        (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {
    val crossrefQuery = ThingsSystempropertydynamiccrossref.filter(_.thingId === thingId).filter(_.id === propertyRelationshipId)
    val propertyRelationships = getPropertiesDynamicQuery(crossrefQuery, None)
    propertyRelationships.map(getPropertyRelationshipValues)
  }

  // Private methods

  private def getPropertiesStaticQuery(crossrefQuery: Query[ThingsSystempropertystaticcrossref, ThingsSystempropertystaticcrossrefRow, Seq],
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
      case (crossref: ThingsSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) =>
        ApiPropertyRelationshipStatic.fromDbModel(crossref, property, propertyType, propertyUom, field, record)
    }
  }

  private def getPropertiesDynamicQuery(crossrefQuery: Query[ThingsSystempropertydynamiccrossref, ThingsSystempropertydynamiccrossrefRow, Seq],
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
      case (crossref: ThingsSystempropertydynamiccrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow) =>
        ApiPropertyRelationshipDynamic.fromDbModel(crossref, property, propertyType, propertyUom, field)
    }
  }
}