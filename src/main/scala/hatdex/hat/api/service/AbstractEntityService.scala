package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.routing

import scala.util.Try

trait AbstractEntityService {
  val entityKind: String
  val logger: LoggingAdapter

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

  protected[api] def getEvent(eventID: Int, recursive: Boolean = false, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]] = None)(implicit session: Session, getValues: Boolean): Option[ApiEvent] = {
    var event = EventsEvent.filter(_.id === eventID).run.headOption
    logger.debug(s"For ${entityKind} get Event ${eventID}")
    event.map { e =>
      recursive match {
        case true =>
          // We have a list of property selectors (Some), which is empty matching no properties
          val recursivePropertySelectors = Some(Seq[ApiBundleContextPropertySelection]())
          new ApiEvent(
            Some(e.id),
            e.name,
            seqOption(getPropertiesStatic(e.id, propertySelectors)),
            seqOption(getPropertiesDynamic(e.id, propertySelectors)),
            seqOption(getEvents(e.id)),
            seqOption(getLocations(e.id)),
            seqOption(getPeople(e.id)),
            seqOption(getThings(e.id)),
            seqOption(getOrganisations(e.id))
          )
        case false =>
          new ApiEvent(
            Some(e.id),
            e.name,
            seqOption(getPropertiesStatic(e.id, propertySelectors)),
            seqOption(getPropertiesDynamic(e.id, propertySelectors)),
            None,
            None,
            None,
            None,
            None
          )
      }

    }
  }

  protected[api] def getLocation(locationID: Int, recursive: Boolean = false, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]] = None)(implicit session: Session, getValues: Boolean): Option[ApiLocation] = {
    var location = LocationsLocation.filter(_.id === locationID).run.headOption
    logger.debug(s"For ${entityKind} get Location ${locationID}")
    location.map { l =>
      recursive match {
        case true =>
          new ApiLocation(
            Some(l.id),
            l.name,
            seqOption(getPropertiesStatic(l.id, propertySelectors)),
            seqOption(getPropertiesDynamic(l.id, propertySelectors)),
            seqOption(getLocations(l.id)),
            seqOption(getThings(l.id))
          )
        case false =>
          new ApiLocation(
            Some(l.id),
            l.name,
            seqOption(getPropertiesStatic(l.id, propertySelectors)),
            seqOption(getPropertiesDynamic(l.id, propertySelectors)),
            None,
            None
          )
      }
    }
  }

  protected[api] def getOrganisation(organisationId: Int, recursive: Boolean = false, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]] = None)(implicit session: Session, getValues: Boolean): Option[ApiOrganisation] = {
    var organisation = OrganisationsOrganisation.filter(_.id === organisationId).run.headOption
    logger.debug(s"For ${entityKind} get Organisation ${organisationId}")
    organisation.map { e =>
      recursive match {
        case true =>
          new ApiOrganisation(
            Some(e.id),
            e.name,
            seqOption(getPropertiesStatic(e.id, propertySelectors)),
            seqOption(getPropertiesDynamic(e.id, propertySelectors)),
            seqOption(getOrganisations(e.id)),
            seqOption(getLocations(e.id)),
            seqOption(getThings(e.id))
          )
        case false =>
          new ApiOrganisation(
            Some(e.id),
            e.name,
            seqOption(getPropertiesStatic(e.id, propertySelectors)),
            seqOption(getPropertiesDynamic(e.id, propertySelectors)),
            None,
            None,
            None
          )
      }

    }
  }

  protected[api] def getPerson(personId: Int, recursive: Boolean = false, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]] = None)(implicit session: Session, getValues: Boolean): Option[ApiPerson] = {
    var maybePerson = PeoplePerson.filter(_.id === personId).run.headOption
    logger.debug(s"For ${entityKind} get Person ${personId}")
    maybePerson.map { person =>
      recursive match {
        case true =>
          new ApiPerson(
            Some(person.id),
            person.name,
            person.personId,
            seqOption(getPropertiesStatic(person.id, propertySelectors)),
            seqOption(getPropertiesDynamic(person.id, propertySelectors)),
            seqOption(getPeople(person.id)),
            seqOption(getLocations(person.id)),
            seqOption(getOrganisations(person.id))
          )
        case false =>
          new ApiPerson(
            Some(person.id),
            person.name,
            person.personId,
            seqOption(getPropertiesStatic(person.id, propertySelectors)),
            seqOption(getPropertiesDynamic(person.id, propertySelectors)),
            None,
            None,
            None
          )
      }

    }
  }

  protected[api] def getThing(thingId: Int, recursive: Boolean = false, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]] = None)(implicit session: Session, getValues: Boolean): Option[ApiThing] = {
    var thing = ThingsThing.filter(_.id === thingId).run.headOption
    logger.debug(s"For ${entityKind} get Thing ${thingId}")

    thing.map { e =>
      recursive match {
        case true =>
          new ApiThing(
            Some(e.id),
            e.name,
            seqOption(getPropertiesStatic(e.id, propertySelectors)),
            seqOption(getPropertiesDynamic(e.id, propertySelectors)),
            seqOption(getThings(e.id)),
            seqOption(getPeople(e.id))
          )
        case false =>
          new ApiThing(
            Some(e.id),
            e.name,
            seqOption(getPropertiesStatic(e.id, propertySelectors)),
            seqOption(getPropertiesDynamic(e.id, propertySelectors)),
            None,
            None
          )
      }

    }
  }

  protected def createRelationshipRecord(relationshipName: String)(implicit session: Session) = {
    val newRecord = new SystemRelationshiprecordRow(0, LocalDateTime.now(), LocalDateTime.now(), relationshipName)
    val record = (SystemRelationshiprecord returning SystemRelationshiprecord) += newRecord
    record.id
  }

  protected def createPropertyRecord(relationshipName: String)(implicit session: Session) = {
    val newRecord = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), relationshipName)
    val record = (SystemPropertyrecord returning SystemPropertyrecord) += newRecord
    record.id
  }

  protected def getLocations(entityId: Int)(implicit session: Session, getValues: Boolean): Seq[ApiLocationRelationship]

  protected def getOrganisations(entityId: Int)(implicit session: Session, getValues: Boolean): Seq[ApiOrganisationRelationship]

  protected def getPeople(entityId: Int)(implicit session: Session, getValues: Boolean): Seq[ApiPersonRelationship]

  protected def getThings(entityId: Int)(implicit session: Session, getValues: Boolean): Seq[ApiThingRelationship]

  protected def getEvents(entityId: Int)(implicit session: Session, getValues: Boolean): Seq[ApiEventRelationship]

  protected def getPropertiesStatic(eventId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])(implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipStatic]

  protected def getPropertiesDynamic(eventId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])(implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipDynamic]

  protected def getPropertyStaticValues(eventId: Int, propertyRelationshipId: Int)(implicit session: Session): Seq[ApiPropertyRelationshipStatic]

  protected def getPropertyDynamicValues(eventId: Int, propertyRelationshipId: Int)(implicit session: Session): Seq[ApiPropertyRelationshipDynamic]

  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship)(implicit session: Session): Try[Int]

  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                         recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                        (implicit session: Session): Try[Int]

  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                          fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                         (implicit session: Session): Try[Int]

  // Utility function to return None for empty sequences
  private def seqOption[T](seq: Seq[T]): Option[Seq[T]] = {
    if (seq.isEmpty)
      None
    else
      Some(seq)
  }

}
