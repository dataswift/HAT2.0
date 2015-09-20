package dalapi.service

import dal.SlickPostgresDriver.simple._
import dal.Tables._
import dalapi.models._
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import spray.routing._

import scala.util.{Failure, Success, Try}


// this trait defines our service behavior independently from the service actor
trait EventsService extends EntityServiceApi {
  import JsonProtocol._

  val entityKind = "event"

  val routes = {
    pathPrefix(entityKind) {
      create ~
        linkEventToLocation ~
        linkEventToOrganisation ~
        linkEventToPerson ~
        linkEventToThing ~
        linkEventToEvent ~
        linkToPropertyStatic ~
        linkToPropertyDynamic ~
        addType ~
        getPropertiesStaticApi ~
        getPropertiesDynamicApi
    }
  }

  /*
   * Create a simple event, containing only the name
   */
  def createEntity = entity(as[ApiEvent]) { event =>
    db.withSession { implicit session =>
      val eventseventRow = new EventsEventRow(0, LocalDateTime.now(), LocalDateTime.now(), event.name)
      val result = Try((EventsEvent returning EventsEvent) += eventseventRow)

      complete {
        result match {
          case Success(createdEvent) =>
            ApiEvent.fromDbModel(createdEvent)
          case Failure(e) =>
            (BadRequest, e.getMessage)
        }
      }
    }
  }


  def createEntity (event: ApiEvent)(implicit session: Session) = {
    val eventseventRow = new EventsEventRow(0, LocalDateTime.now(), LocalDateTime.now(), event.name)
    Try((EventsEvent returning EventsEvent.map(_.id)) += eventseventRow)
  }

  def linkEventToLocation = path(IntNumber / "location" / IntNumber) { (eventId: Int, locationId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$eventId/location/$locationId:${relationship.relationshipType}")

          val crossref = new EventsEventlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
            locationId, eventId, relationship.relationshipType, true, recordId)

          val result = Try((EventsEventlocationcrossref returning EventsEventlocationcrossref.map(_.id)) += crossref)

          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }

        }
      }
    }
  }

  def linkEventToOrganisation = path(IntNumber / "organisation" / IntNumber) { (eventId: Int, organisationId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$eventId/organisation/$organisationId:${relationship.relationshipType}")

          val crossref = new EventsEventorganisationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
            organisationId, eventId, relationship.relationshipType, true, recordId)
          val result = Try((EventsEventorganisationcrossref returning EventsEventorganisationcrossref.map(_.id)) += crossref)

          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }

        }
      }
    }
  }

  def linkEventToPerson = path(IntNumber / "person" / IntNumber) { (eventId: Int, personId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$eventId/person/$personId:${relationship.relationshipType}")

          val crossref = new EventsEventpersoncrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
            personId, eventId, relationship.relationshipType, true, recordId)
          val result = Try((EventsEventpersoncrossref returning EventsEventpersoncrossref.map(_.id)) += crossref)

          // Return the created crossref
          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }

        }
      }
    }
  }

  def linkEventToThing = path(IntNumber / "thing" / IntNumber) { (eventId: Int, thingId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$eventId/thing/$thingId:${relationship.relationshipType}")

          val crossref = new EventsEventthingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
            thingId, eventId, relationship.relationshipType, true, recordId)
          val result = Try((EventsEventthingcrossref returning EventsEventthingcrossref.map(_.id)) += crossref)

          // Return the created crossref
          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }

        }
      }
    }
  }

  def linkEventToEvent = path(IntNumber / "event" / IntNumber) { (eventId: Int, event2Id: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$eventId/event/$event2Id:${relationship.relationshipType}")

          val crossref = new EventsEventtoeventcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
            eventId, event2Id, relationship.relationshipType, true, recordId)
          val result = Try((EventsEventtoeventcrossref returning EventsEventtoeventcrossref.map(_.id)) += crossref)

          // Return the created crossref
          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }

        }
      }
    }
  }

  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
      fieldId: Int, relationshipType: String, propertyRecordId: Int)
      (implicit session: Session) : Try[Int] = {
    val crossref = new EventsSystempropertydynamiccrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      fieldId, relationshipType,
      true, propertyRecordId)

    Try((EventsSystempropertydynamiccrossref returning EventsSystempropertydynamiccrossref.map(_.id)) += crossref)
  }

  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
      recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int)
      (implicit session: Session) : Try[Int] = {
    val crossref = new EventsSystempropertystaticcrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      recordId, fieldId, relationshipType,
      true, propertyRecordId)

    Try((EventsSystempropertystaticcrossref returning EventsSystempropertystaticcrossref.map(_.id)) += crossref)
  }

  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship)
                           (implicit session: Session) : Try[Int] = {
    val entityType = new EventsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, typeId, relationship.relationshipType, true)
    Try((EventsSystemtypecrossref returning EventsSystemtypecrossref.map(_.id)) += entityType)
  }

  protected def getLocations(eventId: Int)
                  (implicit session: Session): Seq[ApiLocationRelationship] = {

    val locationLinks = EventsEventlocationcrossref.filter(_.eventId === eventId).run

    locationLinks flatMap { link: EventsEventlocationcrossrefRow =>
      val apiLocation = getLocation(link.locationId)
      apiLocation.map { location =>
        new ApiLocationRelationship(link.relationshipType, location)
      }
    }
  }

  protected def getOrganisations(eventID: Int)
                      (implicit session: Session): Seq[ApiOrganisationRelationship] = {
    val links = EventsEventorganisationcrossref.filter(_.eventOd === eventID).run

    links flatMap { link: EventsEventorganisationcrossrefRow =>
      val apiOrganisation = getOrganisation(link.organisationId)
      apiOrganisation.map { organisation =>
        new ApiOrganisationRelationship(link.relationshipType, organisation)
      }
    }
  }

  protected def getPeople(eventID: Int)
               (implicit session: Session): Seq[ApiPersonRelationship] = {
    val links = EventsEventpersoncrossref.filter(_.eventOd === eventID).run

    links flatMap { link: EventsEventpersoncrossrefRow =>
      val apiPerson = getPerson(link.personId)
      apiPerson.map { person =>
        new ApiPersonRelationship(link.relationshipType, person)
      }
    }
  }

  protected def getThings(eventID: Int)
               (implicit session: Session): Seq[ApiThingRelationship] = {
    val links = EventsEventthingcrossref.filter(_.eventId === eventID).run

    links flatMap { link: EventsEventthingcrossrefRow =>
      val apiThing = getThing(link.thingId)
      apiThing.map { thing =>
        new ApiThingRelationship(link.relationshipType, thing)
      }
    }
  }

  protected def getEvents(eventID: Int)
               (implicit session: Session): Seq[ApiEventRelationship] = {
    val eventLinks = EventsEventtoeventcrossref.filter(_.eventOneId === eventID).run
    var eventIds = eventLinks.map(_.eventTwoId)

    eventLinks flatMap { link: EventsEventtoeventcrossrefRow =>
      val apiEvent = getEvent(link.eventTwoId)
      apiEvent.map { event =>
        new ApiEventRelationship(link.relationshipType, event)
      }
    }
  }

  protected def getPropertiesStatic(eventId: Int)
                                   (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {

    val crossrefQuery = EventsSystempropertystaticcrossref.filter(_.eventId === eventId)

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
      case (crossref: EventsSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) =>
        ApiPropertyRelationshipStatic.fromDbModel(crossref, property, propertyType, propertyUom, field, record)
    }
  }

  protected def getPropertiesDynamic(eventId: Int)
                                    (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {

    val crossrefQuery = EventsSystempropertydynamiccrossref.filter(_.eventId === eventId)

    val dataQuery = for {
      crossref <- crossrefQuery
      property <- crossref.systemPropertyFk
      propertyType <- property.systemTypeFk
      propertyUom <- property.systemUnitofmeasurementFk
      field <- crossref.dataFieldFk
    } yield (crossref, property, propertyType, propertyUom, field)

    val data = dataQuery.run

    data.map {
      case (crossref: EventsSystempropertydynamiccrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow) =>
        ApiPropertyRelationshipDynamic.fromDbModel(crossref, property, propertyType, propertyUom, field)
    }
  }


}

