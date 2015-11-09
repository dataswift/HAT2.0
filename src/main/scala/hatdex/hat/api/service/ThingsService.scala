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
                                                   (implicit session: Session) : Try[Int] = {
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
                                                  (implicit session: Session) : Try[Int] = {
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
                              (implicit session: Session) : Try[Int] = {
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

   protected def getPropertyStaticValues(thingId: Int, propertyRelationshipId: Int)
                                        (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {
     val crossrefQuery = ThingsSystempropertystaticcrossref.filter(_.thingId === thingId).filter(_.id === propertyRelationshipId)
     val propertyRelationships = getPropertiesStaticQuery(crossrefQuery)
     propertyRelationships.map(getPropertyRelationshipValues)
   }

   protected def getPropertyDynamicValues(thingId: Int, propertyRelationshipId: Int)
                                         (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {
     val crossrefQuery = ThingsSystempropertydynamiccrossref.filter(_.thingId === thingId).filter(_.id === propertyRelationshipId)
     val propertyRelationships = getPropertiesDynamicQuery(crossrefQuery)
     propertyRelationships.map(getPropertyRelationshipValues)
   }

   protected def getPropertiesStatic(thingId: Int)
                                    (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipStatic] = {

     val crossrefQuery = ThingsSystempropertystaticcrossref.filter(_.thingId === thingId)
     val properties = getPropertiesStaticQuery(crossrefQuery)
     getValues match {
       case false =>
         properties
       case true =>
         properties.map(getPropertyRelationshipValues)
     }
   }

   protected def getPropertiesDynamic(thingId: Int)
                                     (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipDynamic] = {

     val crossrefQuery = ThingsSystempropertydynamiccrossref.filter(_.thingId === thingId)
     val properties = getPropertiesDynamicQuery(crossrefQuery)
     getValues match {
       case false =>
         properties
       case true =>
         properties.map(getPropertyRelationshipValues)
     }
   }

   private def getPropertiesStaticQuery(crossrefQuery: Query[ThingsSystempropertystaticcrossref, ThingsSystempropertystaticcrossrefRow, Seq])
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
       case (crossref: ThingsSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
       propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) =>
         ApiPropertyRelationshipStatic.fromDbModel(crossref, property, propertyType, propertyUom, field, record)
     }
   }

   private def getPropertiesDynamicQuery(crossrefQuery: Query[ThingsSystempropertydynamiccrossref, ThingsSystempropertydynamiccrossrefRow, Seq])
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
       case (crossref: ThingsSystempropertydynamiccrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
       propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow) =>
         ApiPropertyRelationshipDynamic.fromDbModel(crossref, property, propertyType, propertyUom, field)
     }
   }
 }