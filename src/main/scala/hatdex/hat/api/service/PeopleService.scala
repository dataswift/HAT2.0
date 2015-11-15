package hatdex.hat.api.service

import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.util.{Failure, Try}


// this trait defines our service behavior independently from the service actor
trait PeopleService extends AbstractEntityService with PropertyService {

   protected def createLinkPerson(entityId: Int, personId: Int, relationshipType: String, recordId: Int)
                                 (implicit session: Session): Try[Int] = {
     Failure(new IllegalArgumentException("People must be linked via a defined relationship type"))
   }

   protected def createLinkPerson(entityId: Int, personId: Int, relationshipTypeId: Int, recordId: Int)
                                       (implicit session: Session): Try[Int] = {
     val crossref = new PeoplePersontopersoncrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
       entityId, personId, relationshipTypeId, true, recordId)
     Try((PeoplePersontopersoncrossref returning PeoplePersontopersoncrossref.map(_.id)) += crossref)
   }

   protected def createLinkLocation(entityId: Int, locationId: Int, relationshipType: String, recordId: Int)
                                   (implicit session: Session): Try[Int] = {
     val crossref = new PeoplePersonlocationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
       locationId, entityId, relationshipType, true, recordId)
     Try((PeoplePersonlocationcrossref returning PeoplePersonlocationcrossref.map(_.id)) += crossref)
   }

   protected def createLinkOrganisation(entityId: Int, organisationId: Int, relationshipType: String, recordId: Int)
                                       (implicit session: Session): Try[Int] = {
     val crossref = new PeoplePersonorganisationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
       organisationId, entityId, relationshipType, true, recordId)
     Try((PeoplePersonorganisationcrossref returning PeoplePersonorganisationcrossref.map(_.id)) += crossref)
   }

   protected def createLinkEvent(entityId: Int, eventId: Int, relationshipType: String, recordId: Int)
                                (implicit session: Session): Try[Int] = {
     Failure(new NotImplementedError("Operation Not Supprted"))
   }

   protected def createLinkThing(entityId: Int, thingId: Int, relationshipType: String, recordId: Int)
                                (implicit session: Session): Try[Int] = {
     Failure(new NotImplementedError("Operation Not Supprted"))
   }

   /*
    * Link person to a property statically (tying it in with a specific record ID)
    */
   protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                                    fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                                   (implicit session: Session) : Try[Int] = {
     val crossref = new PeopleSystempropertydynamiccrossrefRow(
       0, LocalDateTime.now(), LocalDateTime.now(),
       entityId, propertyId,
       fieldId, relationshipType,
       true, propertyRecordId)

     Try((PeopleSystempropertydynamiccrossref returning PeopleSystempropertydynamiccrossref.map(_.id)) += crossref)
   }

   /*
   * Link person to a property dynamically
   */
   protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                                   recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                                  (implicit session: Session) : Try[Int] = {
     val crossref = new PeopleSystempropertystaticcrossrefRow(
       0, LocalDateTime.now(), LocalDateTime.now(),
       entityId, propertyId,
       recordId, fieldId, relationshipType,
       true, propertyRecordId)

     Try((PeopleSystempropertystaticcrossref returning PeopleSystempropertystaticcrossref.map(_.id)) += crossref)
   }

   /*
    * Tag person with a type
    */
   protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship)
                              (implicit session: Session) : Try[Int] = {

     val entityType = new PeopleSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
       entityId, typeId, relationship.relationshipType, true)
     Try((PeopleSystemtypecrossref returning PeopleSystemtypecrossref.map(_.id)) += entityType)
   }

   def getLocations(personId: Int)
                   (implicit session: Session, getValues: Boolean): Seq[ApiLocationRelationship] = {

     val locationLinks = PeoplePersonlocationcrossref.filter(_.personId === personId).run

     locationLinks flatMap { link: PeoplePersonlocationcrossrefRow =>
       val apiLocation = getLocation(link.locationId)
       apiLocation.map { location =>
         new ApiLocationRelationship(link.relationshipType, location)
       }
     }
   }

   def getOrganisations(personID: Int)
                       (implicit session: Session, getValues: Boolean): Seq[ApiOrganisationRelationship] = {
     val links = PeoplePersonorganisationcrossref.filter(_.personId === personID).run

     links flatMap { link: PeoplePersonorganisationcrossrefRow =>
       val apiOrganisation = getOrganisation(link.organisationId)
       apiOrganisation.map { organisation =>
         new ApiOrganisationRelationship(link.relationshipType, organisation)
       }
     }
   }

   def getPeople(personID: Int)
                (implicit session: Session, getValues: Boolean): Seq[ApiPersonRelationship] = {
     val links = for {
       link <- PeoplePersontopersoncrossref.filter(_.personOneId === personID)
       relationship <- link.peoplePersontopersonrelationshiptypeFk
     } yield (link, relationship)

     links.run flatMap {
       case (link: PeoplePersontopersoncrossrefRow, rel: PeoplePersontopersonrelationshiptypeRow) =>
         val apiPerson = getPerson(link.personTwoId)
         apiPerson.map { person =>
           new ApiPersonRelationship(rel.name, person)
         }
     }
   }

   def getThings(eventID: Int)
                (implicit session: Session, getValues: Boolean): Seq[ApiThingRelationship] = {
     Seq()
   }

   def getEvents(eventID: Int)
                (implicit session: Session, getValues: Boolean): Seq[ApiEventRelationship] = {
     Seq()
   }


   protected def getPropertiesStatic(personId: Int)
                                    (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipStatic] = {

     val crossrefQuery = PeopleSystempropertystaticcrossref.filter(_.personId === personId)
     val properties = getPropertiesStaticQuery(crossrefQuery)
     getValues match {
       case false =>
         properties
       case true =>
         properties.map(getPropertyRelationshipValues)
     }
   }

   protected def getPropertiesDynamic(personId: Int)
                                     (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipDynamic] = {

     val crossrefQuery = PeopleSystempropertydynamiccrossref.filter(_.personId === personId)
     val properties = getPropertiesDynamicQuery(crossrefQuery)
     getValues match {
       case false =>
         properties
       case true =>
         properties.map(getPropertyRelationshipValues)
     }
   }

   protected def getPropertyStaticValues(personId: Int, propertyRelationshipId: Int)
                                        (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {
     val crossrefQuery = PeopleSystempropertystaticcrossref.filter(_.personId === personId).filter(_.id === propertyRelationshipId)
     val propertyRelationships = getPropertiesStaticQuery(crossrefQuery)
     propertyRelationships.map(getPropertyRelationshipValues)
   }

   protected def getPropertyDynamicValues(personId: Int, propertyRelationshipId: Int)
                                         (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {
     val crossrefQuery = PeopleSystempropertydynamiccrossref.filter(_.personId === personId).filter(_.id === propertyRelationshipId)
     val propertyRelationships = getPropertiesDynamicQuery(crossrefQuery)
     propertyRelationships.map(getPropertyRelationshipValues)
   }

   private def getPropertiesStaticQuery(crossrefQuery: Query[PeopleSystempropertystaticcrossref, PeopleSystempropertystaticcrossrefRow, Seq])
                                       (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {
     val dataQuery = for {
       crossref <- crossrefQuery
       property <- crossref.systemPropertyFk
       propertyType <- property.systemTypeFk
       propertyUom <- property.systemUnitofmeasurementFk
       field <- crossref.dataFieldFk
       record <- crossref.dataRecordFk
     } yield (crossref, property, propertyType, propertyUom, field, record)

     val data = dataQuery.run

     data.map {
       case (crossref: PeopleSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
       propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) =>
         ApiPropertyRelationshipStatic.fromDbModel(crossref, property, propertyType, propertyUom, field, record)
     }
   }

   private def getPropertiesDynamicQuery(crossrefQuery: Query[PeopleSystempropertydynamiccrossref, PeopleSystempropertydynamiccrossrefRow, Seq])
                                        (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {
     val dataQuery = for {
       crossref <- crossrefQuery
       property <- crossref.systemPropertyFk
       propertyType <- property.systemTypeFk
       propertyUom <- property.systemUnitofmeasurementFk
       field <- crossref.dataFieldFk
     } yield (crossref, property, propertyType, propertyUom, field)

     val data = dataQuery.run

     data.map {
       case (crossref: PeopleSystempropertydynamiccrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
       propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow) =>
         ApiPropertyRelationshipDynamic.fromDbModel(crossref, property, propertyType, propertyUom, field)
     }
   }
 }

