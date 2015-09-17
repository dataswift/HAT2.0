package dalapi.service

import dal.SlickPostgresDriver.simple._
import dal.Tables._
import dalapi.models._
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Success, Try}


// this trait defines our service behavior independently from the service actor
trait EventsService extends HttpService with InboundService with EntityService {

  val routes = {
    pathPrefix("event") {
      createEvent ~
        linkEventToLocation ~
        linkEventToOrganisation ~
        linkEventToPerson ~
        linkEventToThing ~
        linkEventToEvent ~
        linkEventToPropertyStatic ~
        linkEventToPropertyDynamic ~
        addEventType ~
        getPropertiesStaticApi ~
        getPropertiesDynamicApi
    }
  }

  import JsonProtocol._

  /*
   * Create a simple event, containing only the name
   */
  def createEvent = path("") {
    post {
      entity(as[ApiEvent]) { event =>
        db.withSession { implicit session =>
          val eventseventRow = new EventsEventRow(0, LocalDateTime.now(), LocalDateTime.now(), event.name)
          val result = Try((EventsEvent returning EventsEvent.map(_.id)) += eventseventRow)

          complete {
            result match {
              case Success(eventId) =>
                event.copy(id = Some(eventId))
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }

        }

      }
    }
  }

  def linkEventToLocation = path(IntNumber / "location" / IntNumber) { (eventId: Int, locationId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"event/$eventId/location/$locationId:${relationship.relationshipType}")

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
          val recordId = createRelationshipRecord(s"event/$eventId/organisation/$organisationId:${relationship.relationshipType}")

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
          val recordId = createRelationshipRecord(s"event/$eventId/person/$personId:${relationship.relationshipType}")

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
          val recordId = createRelationshipRecord(s"event/$eventId/thing/$thingId:${relationship.relationshipType}")

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
          val recordId = createRelationshipRecord(s"event/$eventId/event/$event2Id:${relationship.relationshipType}")

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

  /*
   * Link event to a property statically (tying it in with a specific record ID)
   */
  def linkEventToPropertyStatic = path(IntNumber / "property" / "static" / IntNumber) { (eventId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipStatic]) { relationship =>
        val result: Try[Int] = (relationship.field.id, relationship.record.id) match {
          case (Some(fieldId), Some(recordId)) =>
            val propertyRecordId = createPropertyRecord(
              s"event/$eventId/property/$propertyId:${relationship.relationshipType}(${fieldId},${recordId},${relationship.relationshipType}")

            // Create the crossreference record and insert into db
            val crossref = new EventsSystempropertystaticcrossrefRow(
              0, LocalDateTime.now(), LocalDateTime.now(),
              eventId, propertyId,
              recordId, fieldId, relationship.relationshipType,
              true, propertyRecordId
            )

            db.withSession { implicit session =>
              Try((EventsSystempropertystaticcrossref returning EventsSystempropertystaticcrossref.map(_.id)) += crossref)
            }
          case (None, _) =>
            Failure(new IllegalArgumentException("Property relationship must have an existing Data Field with ID"))
          case (_, None) =>
            Failure(new IllegalArgumentException("Static Property relationship must have an existing Data Record with ID"))
        }

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

  /*
   * Link event to a property dynamically
   */
  def linkEventToPropertyDynamic = path(IntNumber / "property" / "dynamic" / IntNumber) { (eventId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipDynamic]) { relationship =>
        val result: Try[Int] = relationship.field.id match {
          case Some(fieldId) =>
            val propertyRecordId = createPropertyRecord(
              s"""event/$eventId/property/$propertyId:${relationship.relationshipType}
                  |(${fieldId},${relationship.relationshipType})""".stripMargin)

            // Create the crossreference record and insert into db
            val crossref = new EventsSystempropertydynamiccrossrefRow(
              0, LocalDateTime.now(), LocalDateTime.now(),
              eventId, propertyId,
              fieldId, relationship.relationshipType,
              true, propertyRecordId)

            db.withSession { implicit session =>
              Try((EventsSystempropertydynamiccrossref returning EventsSystempropertydynamiccrossref.map(_.id)) += crossref)
            }
          case None =>
            Failure(new IllegalArgumentException("Property relationship must have an existing Data Field with ID"))
        }

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

  /*
   * Tag event with a type
   */
  def addEventType = path(IntNumber / "type" / IntNumber) { (eventId: Int, typeId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val eventType = new EventsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
            eventId, typeId, relationship.relationshipType, true)
          val result = Try((EventsSystemtypecrossref returning EventsSystemtypecrossref.map(_.id)) += eventType)

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

  def getPropertiesStaticApi = path(IntNumber / "property" / "static") {
    (eventId: Int) =>
      get {
        db.withSession { implicit session =>
          complete {
            getPropertiesStatic(eventId)
          }
        }
      }
  }

  def getPropertiesDynamicApi = path(IntNumber / "property" / "dynamic") {
    (eventId: Int) =>
      get {
        db.withSession { implicit session =>
          complete {
            getPropertiesDynamic(eventId)
          }
        }
      }
  }

  def getLocations(eventId: Int)
                  (implicit session: Session): Seq[ApiLocationRelationship] = {

    val locationLinks = EventsEventlocationcrossref.filter(_.eventId === eventId).run

    locationLinks flatMap { link: EventsEventlocationcrossrefRow =>
      val apiLocation = getLocation(link.locationId)
      apiLocation.map { location =>
        new ApiLocationRelationship(link.relationshipType, location)
      }
    }
  }

  def getOrganisations(eventID: Int)
                      (implicit session: Session): Seq[ApiOrganisationRelationship] = {
    val links = EventsEventorganisationcrossref.filter(_.eventOd === eventID).run

    links flatMap { link: EventsEventorganisationcrossrefRow =>
      val apiOrganisation = getOrganisation(link.organisationId)
      apiOrganisation.map { organisation =>
        new ApiOrganisationRelationship(link.relationshipType, organisation)
      }
    }
  }

  def getPeople(eventID: Int)
               (implicit session: Session): Seq[ApiPersonRelationship] = {
    val links = EventsEventpersoncrossref.filter(_.eventOd === eventID).run

    links flatMap { link: EventsEventpersoncrossrefRow =>
      val apiPerson = getPerson(link.personId)
      apiPerson.map { person =>
        new ApiPersonRelationship(link.relationshipType, person)
      }
    }
  }

  def getThings(eventID: Int)
               (implicit session: Session): Seq[ApiThingRelationship] = {
    val links = EventsEventthingcrossref.filter(_.eventId === eventID).run

    links flatMap { link: EventsEventthingcrossrefRow =>
      val apiThing = getThing(link.thingId)
      apiThing.map { thing =>
        new ApiThingRelationship(link.relationshipType, thing)
      }
    }
  }

  def getEvents(eventID: Int)
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

