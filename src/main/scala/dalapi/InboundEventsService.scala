package dalapi

import javax.ws.rs.Path

import akka.actor.ActorLogging
import com.wordnik.swagger.annotations._
import dal.Tables._
import dal.SlickPostgresDriver.simple._
import dalapi.models.{ApiDataTable, ApiDataRecord, ApiDataField, ApiDataValue}
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.json._
import spray.routing._
import spray.httpx.SprayJsonSupport._
import spray.util.LoggingContext
import spray.http.StatusCodes._
import com.typesafe.config.{Config, ConfigFactory}
import scala.annotation.meta.field
import scala.concurrent.ExecutionContext.Implicits.global
import dalapi.models._


// this trait defines our service behavior independently from the service actor
trait InboundEventsService extends HttpService with InboundService {

  val routes = {
    pathPrefix("inbound") {
      createEvent
    }
  }

  import InboundJsonProtocol._

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

  def linkEventLocation = path("event" / IntNumber / "location" / IntNumber) { (eventId: Int, locationId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"event/$eventId/location/$locationId:${relationship.relationshipType}")

          val crossref = new EventsEventlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), locationId, eventId, relationship.relationshipType, true, recordId)

          val crossrefId = (EventsEventlocationcrossref returning EventsEventlocationcrossref.map(_.id)) += crossref

          complete {
            ApiGenericId(crossrefId)
          }
        }
      }
    }
  }

//  def linkEventOrganisation = path("event") {
//
//  }

  def linkEventToOrganisation = path("event" / IntNumber / "organisation" / IntNumber) { (eventId : Int, organisationId : Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"event/$eventId/organisation/$organisationId:${relationship.relationshipType}")

          val crossref = new EventsEventorganisationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), organisationId, eventId, relationship.relationshipType, true, recordId)
          val crossrefId = (EventsEventorganisationcrossref returning EventsEventorganisationcrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
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

          val crossref = new EventsEventpersoncrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), personId, eventId, relationship.relationshipType, true, recordId)
          val crossrefId = (EventsEventpersoncrossref returning EventsEventpersoncrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
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

          val crossref = new EventsEventthingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), thingId, eventId, relationship.relationshipType, true, recordId)
          val crossrefId = (EventsEventthingcrossref returning EventsEventthingcrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
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

          val crossref = new EventsEventtoeventcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), eventId, event2Id, relationship.relationshipType, true, recordId)
          val crossrefId = (EventsEventtoeventcrossref returning EventsEventtoeventcrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
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

        val recordId = createPropertyRecord(s"event/$eventId/property/$propertyId:${relationship.relationshipType}(${relationship.fieldId},${relationship.recordId},${relationship.relationshipType})")

        // Create the crossreference record and insert into db
        val crossref = new EventsSystempropertystaticcrossrefRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          eventId, propertyId,
          relationship.fieldId, relationship.recordId, relationship.relationshipType,
          true, recordId
        )

        db.withSession { implicit session =>
          val crossrefId = (EventsSystempropertystaticcrossref returning EventsSystempropertystaticcrossref.map(_.id)) += crossref
          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
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

        val recordId = createPropertyRecord(s"event/$eventId/property/$propertyId:${relationship.relationshipType}(${relationship.fieldId},${relationship.relationshipType})")

        // Create the crossreference record and insert into db
        val crossref = new EventsSystempropertydynamiccrossrefRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          eventId, propertyId,
          relationship.fieldId, relationship.relationshipType,
          true, recordId
        )

        db.withSession { implicit session =>
          val crossrefId = (EventsSystempropertydynamiccrossref returning EventsSystempropertydynamiccrossref.map(_.id)) += crossref
          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }
        }
      }
    }
  }

  /*
   * Tag event with a type
   */
  def addThingType = path("event" / IntNumber / "type" / IntNumber) { (eventId: Int, typeId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val eventType = new EventsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), eventId, typeId, relationship.relationshipType, true)
          val eventTypeId = (EventsSystemtypecrossref returning EventsSystemtypecrossref.map(_.id)) += eventType
          complete {
            ApiGenericId(eventTypeId)
          }
        }
      }

    }
  }

}

