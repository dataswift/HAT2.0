package dalapi

import com.typesafe.config.ConfigFactory
import dal.SlickPostgresDriver.simple._
import dal.Tables._
import dalapi.models._
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._


// this trait defines our service behavior independently from the service actor
trait InboundThingsService extends HttpService with InboundService {

  val routes = {
    pathPrefix("inbound") {
      respondWithMediaType(`application/json`) {
        createThing ~ linkThingToPerson ~ linkThingToThing ~ linkThingToPropertyStatic ~ linkThingToPropertyDynamic
      }
    }
  }

  import InboundJsonProtocol._

  def createThing = path("thing") {
    post {
        entity(as[ApiEvent]) { thing =>
          db.withSession { implicit session =>
            val thingRow = new ThingsThingRow(0, LocalDateTime.now(), LocalDateTime.now(), thing.name)
            val thingId = (ThingsThing returning ThingsThing.map(_.id)) += thingRow
            complete(Created, {
              thing.copy(id = Some(thingId))
            })
          }

        }

    }
  }

  def linkThingToPerson = path("thing" / IntNumber / "person" / IntNumber) { (thingId : Int, personId : Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"thing/$thingId/person/$personId:${relationship.relationshipType}")

          val crossref = new ThingsThingpersoncrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), personId, thingId, relationship.relationshipType, true, recordId)
          val crossrefId = (ThingsThingpersoncrossref returning ThingsThingpersoncrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }

        }
      }
    }
  }

  /*
   * Link two things together, e.g. as one thing part of another thing with a parentChild relationship type
   */
  def linkThingToThing = path("thing" / IntNumber / "thing" / IntNumber) { (thingId : Int, toThingId : Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"thing/$thingId/thing/$toThingId:${relationship.relationshipType}")

          // Create the crossreference record and insert into db
          val crossref = new ThingsThingtothingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), thingId, toThingId, relationship.relationshipType, true, recordId)
          val crossrefId = (ThingsThingtothingcrossref returning ThingsThingtothingcrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }

        }
      }
    }
  }

  /*
   * Link thing to a property statically (tying it in with a specific record ID)
   */
  def linkThingToPropertyStatic = path("thing" / IntNumber / "property" / IntNumber) { (thingId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipStatic]) { relationship =>

        val recordId = createPropertyRecord(s"thing/$thingId/property/$propertyId:${relationship.relationshipType}(${relationship.fieldId},${relationship.recordId},${relationship.relationshipType})")

        // Create the crossreference record and insert into db
        val crossref = new ThingsSystempropertystaticcrossrefRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          thingId, propertyId,
          relationship.fieldId, relationship.recordId, relationship.relationshipType,
          true, recordId
        )

        db.withSession { implicit session =>
          val crossrefId = (ThingsSystempropertystaticcrossref returning ThingsSystempropertystaticcrossref.map(_.id)) += crossref
          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }
        }
      }
    }
  }

  /*
   * Link thing to a property dynamically
   */
  def linkThingToPropertyDynamic = path("thing" / IntNumber / "property" / IntNumber) { (thingId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipDynamic]) { relationship =>

        val recordId = createPropertyRecord(s"thing/$thingId/property/$propertyId:${relationship.relationshipType}(${relationship.fieldId},${relationship.relationshipType})")

        // Create the crossreference record and insert into db
        val crossref = new ThingsSystempropertydynamiccrossrefRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          thingId, propertyId,
          relationship.fieldId, relationship.relationshipType,
          true, recordId
        )

        db.withSession { implicit session =>
          val crossrefId = (ThingsSystempropertydynamiccrossref returning ThingsSystempropertydynamiccrossref.map(_.id)) += crossref
          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }
        }
      }
    }
  }

  /*
   * Tag thing with a type
   */
  def addThingType = path("thing" / IntNumber / "type" / IntNumber) { (thingId: Int, typeId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val thingType = new ThingsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), thingId, typeId, relationship.relationshipType, true)
          val thingTypeId = (ThingsSystemtypecrossref returning ThingsSystemtypecrossref.map(_.id)) += thingType
          complete {
            ApiGenericId(thingTypeId)
          }
        }
      }

    }
  }

}

