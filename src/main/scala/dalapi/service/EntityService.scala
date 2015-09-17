package dalapi.service

import dal.Tables._
import dalapi.models._
import dal.SlickPostgresDriver.simple._

trait EntityService {
  protected def getEvent(eventID: Int)(implicit session: Session): Option[ApiEvent] = {
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

  protected def getLocation(locationID: Int)(implicit session: Session): Option[ApiLocation] = {
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

  protected def getOrganisation(organisationId: Int)(implicit session: Session): Option[ApiOrganisation] = {
    var organisation = OrganisationsOrganisation.filter(_.id === organisationId).run.headOption

    organisation.map { e =>
      new ApiOrganisation(
        Some(e.id),
        e.name,
        seqOption(getPropertiesStatic(e.id)),
        seqOption(getPropertiesDynamic(e.id)),
        seqOption(getOrganisations(e.id)),
        seqOption(getLocations(e.id))
      )
    }
  }

  protected def getPerson(personId: Int)(implicit session: Session): Option[ApiPerson] = {
    var person = PeoplePerson.filter(_.id === personId).run.headOption

    person.map { e =>
      new ApiPerson(
        Some(e.id),
        e.name,
        seqOption(getPropertiesStatic(e.id)),
        seqOption(getPropertiesDynamic(e.id)),
        seqOption(getPeople(e.id)),
        seqOption(getLocations(e.id)),
        seqOption(getOrganisations(e.id))
      )
    }
  }

  protected def getThing(thingId: Int)(implicit session: Session): Option[ApiThing] = {
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

  protected def getLocations(entityId: Int)(implicit session: Session) : Seq[ApiLocationRelationship]

  protected def getOrganisations(entityId: Int)(implicit session: Session) : Seq[ApiOrganisationRelationship]

  protected def getPeople(entityId: Int)(implicit session: Session) : Seq[ApiPersonRelationship]

  protected def getThings(entityId: Int)(implicit session: Session) : Seq[ApiThingRelationship]

  protected def getEvents(entityId: Int)(implicit session: Session) : Seq[ApiEventRelationship]

  protected def getPropertiesStatic(eventId: Int)(implicit session: Session): Seq[ApiPropertyRelationshipStatic]

  protected def getPropertiesDynamic(eventId: Int)(implicit session: Session): Seq[ApiPropertyRelationshipDynamic]

  private def seqOption[T](seq: Seq[T]) : Option[Seq[T]] = {
    if (seq.isEmpty)
      None
    else
      Some(seq)
  }

}
