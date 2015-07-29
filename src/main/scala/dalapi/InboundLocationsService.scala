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
trait InboundLocationsService extends HttpService with InboundService {

  val routes = {
    pathPrefix("inbound") {
      createLocation ~
      linkLocationToLocation ~
      linkLocationToThing ~
      linkLocationToPropertyStatic ~
      linkLocationToPropertyDynamic ~
      addLocationType
    }
  }

  import InboundJsonProtocol._

  def createLocation = path("location") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiLocation]) { location =>
          db.withSession { implicit session =>
            val locationslocationRow = new LocationsLocationRow(0, LocalDateTime.now(), LocalDateTime.now(), location.name)
            val locationId = (LocationsLocation returning LocationsLocation.map(_.id)) += locationslocationRow
            complete(Created, {
              location.copy(id = Some(locationId))
            })
          }

        }
      }
    }
  }

  def linkLocationToLocation = path("location" / IntNumber / "location" / IntNumber) { (locationId: Int, location2Id: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"location/$locationId/location/$location2Id:${relationship.relationshipType}")

          val crossref = new LocationsLocationtolocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), locationId, location2Id, relationship.relationshipType, true, recordId)

          val crossrefId = (LocationsLocationtolocationcrossref returning LocationsLocationtolocationcrossref.map(_.id)) += crossref

          complete {
            ApiGenericId(crossrefId)
          }
        }
      }
    }
  }

  def linkLocationToThing = path("location" / IntNumber / "thing" / IntNumber) { (locationId : Int, thingId : Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"location/$locationId/thing/$thingId:${relationship.relationshipType}")

          val crossref = new LocationsLocationthingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), thingId, locationId, relationship.relationshipType, true, recordId)
          val crossrefId = (LocationsLocationthingcrossref returning LocationsLocationthingcrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }

        }
      }
    }
  }

  /*
   * Link location to a property statically (tying it in with a specific record ID)
   */
  def linkLocationToPropertyStatic = path("location" / IntNumber / "property" / IntNumber / "static") { (locationId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipStatic]) { relationship =>

        val recordId = createPropertyRecord(s"location/$locationId/property/$propertyId:${relationship.relationshipType}(${relationship.fieldId},${relationship.recordId},${relationship.relationshipType})")

        // Create the crossreference record and insert into db
        val crossref = new LocationsSystempropertystaticcrossrefRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          locationId, propertyId,
          relationship.fieldId, relationship.recordId, relationship.relationshipType,
          true, recordId
        )

        db.withSession { implicit session =>
          val crossrefId = (LocationsSystempropertystaticcrossref returning LocationsSystempropertystaticcrossref.map(_.id)) += crossref
          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }
        }
      }
    }
  }

  /*
   * Link location to a property dynamically
   */
  def linkLocationToPropertyDynamic = path("location" / IntNumber / "property" / IntNumber / "dynamic") { (locationId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipDynamic]) { relationship =>

        val recordId = createPropertyRecord(s"location/$locationId/property/$propertyId:${relationship.relationshipType}(${relationship.fieldId},${relationship.relationshipType})")

        // Create the crossreference record and insert into db
        val crossref = new LocationsSystempropertydynamiccrossrefRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          locationId, propertyId,
          relationship.fieldId, relationship.relationshipType,
          true, recordId
        )

        db.withSession { implicit session =>
          val crossrefId = (LocationsSystempropertydynamiccrossref returning LocationsSystempropertydynamiccrossref.map(_.id)) += crossref
          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }
        }
      }
    }
  }

  /*
   * Tag location with a type
   */
  def addLocationType = path("location" / IntNumber / "type" / IntNumber) { (locationId: Int, typeId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val locationType = new LocationsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, typeId, relationship.relationshipType, true)
          val locationTypeId = (LocationsSystemtypecrossref returning LocationsSystemtypecrossref.map(_.id)) += locationType
          complete {
            ApiGenericId(locationTypeId)
          }
        }
      }

    }
  }
}

