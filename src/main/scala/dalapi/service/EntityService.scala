package dalapi.service

import dal.Tables._
import dalapi.models._
import dal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import spray.routing

import scala.util.{Failure, Try}

trait EntityService {
  val entityKind: String
  val dataService: DataService
  val propertyService: PropertyService

  protected def createEntity: routing.Route

  protected def createLinkLocation(entityId: Int, locationId: Int, relationshipType: String, recordId: Int)
                                  (implicit session: Session): Try[Int]

  protected def createLinkOrganisation(entityId: Int, organisationId: Int, relationshipType: String, recordId: Int)
                                      (implicit session: Session): Try[Int]

  protected def createLinkPerson(entityId: Int, personId: Int, relationshipType: String, recordId: Int)
                                (implicit session: Session): Try[Int]

  protected def createLinkThing(entityId: Int, thingId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int]

  protected def createLinkEvent(entityId: Int, eventId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int]

  protected def getEvent(eventID: Int)(implicit session: Session, getValues: Boolean): Option[ApiEvent] = {
    var event = EventsEvent.filter(_.id === eventID).run.headOption

    event.map { e =>
      new ApiEvent(
        Some(e.id),
        e.name,
        seqOption(getPropertiesStatic(e.id)),
        seqOption(getPropertiesDynamic(e.id)),
        seqOption(getEvents(e.id)),
        seqOption(getLocations(e.id)),
        seqOption(getPeople(e.id)),
        seqOption(getThings(e.id)),
        seqOption(getOrganisations(e.id))
      )
    }
  }

  protected def getLocation(locationID: Int)(implicit session: Session, getValues: Boolean): Option[ApiLocation] = {
    var location = LocationsLocation.filter(_.id === locationID).run.headOption

    location.map { l =>
      new ApiLocation(
        Some(l.id),
        l.name,
        seqOption(getPropertiesStatic(l.id)),
        seqOption(getPropertiesDynamic(l.id)),
        seqOption(getLocations(l.id)),
        seqOption(getThings(l.id))
      )
    }
  }

  protected def getOrganisation(organisationId: Int)(implicit session: Session, getValues: Boolean): Option[ApiOrganisation] = {
    var organisation = OrganisationsOrganisation.filter(_.id === organisationId).run.headOption

    organisation.map { e =>
      new ApiOrganisation(
        Some(e.id),
        e.name,
        seqOption(getPropertiesStatic(e.id)),
        seqOption(getPropertiesDynamic(e.id)),
        seqOption(getOrganisations(e.id)),
        seqOption(getLocations(e.id)),
        seqOption(getThings(e.id))
      )
    }
  }

  protected def getPerson(personId: Int)(implicit session: Session, getValues: Boolean): Option[ApiPerson] = {
    var person = PeoplePerson.filter(_.id === personId).run.headOption

    person.map { e =>
      new ApiPerson(
        Some(e.id),
        e.name,
        e.personId,
        seqOption(getPropertiesStatic(e.id)),
        seqOption(getPropertiesDynamic(e.id)),
        seqOption(getPeople(e.id)),
        seqOption(getLocations(e.id)),
        seqOption(getOrganisations(e.id))
      )
    }
  }

  protected def getThing(thingId: Int)(implicit session: Session, getValues: Boolean): Option[ApiThing] = {
    var thing = ThingsThing.filter(_.id === thingId).run.headOption

    thing.map { e =>
      new ApiThing(
        Some(e.id),
        e.name,
        seqOption(getPropertiesStatic(e.id)),
        seqOption(getPropertiesDynamic(e.id)),
        seqOption(getThings(e.id)),
        seqOption(getPeople(e.id))
      )
    }
  }

  protected def getLocations(entityId: Int)(implicit session: Session, getValues: Boolean) : Seq[ApiLocationRelationship]

  protected def getOrganisations(entityId: Int)(implicit session: Session, getValues: Boolean) : Seq[ApiOrganisationRelationship]

  protected def getPeople(entityId: Int)(implicit session: Session, getValues: Boolean) : Seq[ApiPersonRelationship]

  protected def getThings(entityId: Int)(implicit session: Session, getValues: Boolean) : Seq[ApiThingRelationship]

  protected def getEvents(entityId: Int)(implicit session: Session, getValues: Boolean) : Seq[ApiEventRelationship]

  protected def getPropertiesStatic(eventId: Int)(implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipStatic]

  protected def getPropertiesDynamic(eventId: Int)(implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipDynamic]

  protected def getPropertyStaticValues(eventId: Int, propertyRelationshipId: Int)(implicit session: Session): Seq[ApiPropertyRelationshipStatic]

  protected def getPropertyDynamicValues(eventId: Int, propertyRelationshipId: Int)(implicit session: Session): Seq[ApiPropertyRelationshipDynamic]

  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship)(implicit session: Session) : Try[Int]

  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                         recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                        (implicit session: Session) : Try[Int]

  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                          fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                         (implicit session: Session) : Try[Int]

  // Utility function to return None for empty sequences
  private def seqOption[T](seq: Seq[T]) : Option[Seq[T]] = {
    if (seq.isEmpty)
      None
    else
      Some(seq)
  }

}
