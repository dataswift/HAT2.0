package dalapi.service

import dal.SlickPostgresDriver.simple._
import dal.Tables._
import dalapi.InboundService
import dalapi.models.{ApiDataField, ApiDataRecord, _}
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Success, Try}


// this trait defines our service behavior independently from the service actor
trait EventsService extends HttpService with InboundService {

  val routes = {
    pathPrefix("") {
      createEvent ~
        linkEventToLocation ~
        linkEventToOrganisation ~
        linkEventToPerson ~
        linkEventToThing ~
        linkEventToEvent ~
        linkEventToPropertyStatic ~
        linkEventToPropertyDynamic ~
        addEventType
    }
  }

  import ApiJsonProtocol._

  /*
   * Create a simple event, containing only the name
   */
  def createEvent = path("event") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiEvent]) { event =>
          db.withSession { implicit session =>
            val eventseventRow = new EventsEventRow(0, LocalDateTime.now(), LocalDateTime.now(), event.name)
            val eventId = (EventsEvent returning EventsEvent.map(_.id)) += eventseventRow
            complete(Created, {
              event.copy(id = Some(eventId))
            })
          }

        }
      }
    }
  }

  def linkEventToLocation = path("event" / IntNumber / "location" / IntNumber) { (eventId: Int, locationId: Int) =>
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

  def linkEventToOrganisation = path("event" / IntNumber / "organisation" / IntNumber) { (eventId : Int, organisationId : Int) =>
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

  def linkEventToPerson = path("event" / IntNumber / "person" / IntNumber) { (eventId : Int, personId : Int) =>
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

  def linkEventToThing = path("event" / IntNumber / "thing" / IntNumber) { (eventId : Int, thingId : Int) =>
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

  def linkEventToEvent = path("event" / IntNumber / "event" / IntNumber) { (eventId : Int, event2Id : Int) =>
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
  def linkEventToPropertyStatic = path("event" / IntNumber / "property" / IntNumber / "static") { (eventId: Int, propertyId: Int) =>
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
  def linkEventToPropertyDynamic = path("event" / IntNumber / "property" / IntNumber / "dynamic") { (eventId: Int, propertyId: Int) =>
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
  def addEventType = path("event" / IntNumber / "type" / IntNumber) { (eventId: Int, typeId: Int) =>
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

  private def getPropertiesStatic(eventId:Int)
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

    val apiProperties = data.map {
      case (crossref: EventsSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow, propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) =>
        val apiUom = ApiSystemUnitofmeasurement.fromDbModel(propertyUom)
        val apiType = ApiSystemType.fromDbModel(propertyType)
        val apiProperty = ApiProperty.fromDbModel(property)(apiType, apiUom)

        val apiRecord = ApiDataRecord.fromDataRecord(record)(None)
        val apiDataField = ApiDataField.fromDataField(field)
        ApiPropertyRelationshipStatic.fromDbModel(crossref)(apiProperty, apiDataField, apiRecord)
    }

    apiProperties
  }
}

