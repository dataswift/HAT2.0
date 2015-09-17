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
trait ThingsService extends HttpService with InboundService with EntityService {

  val routes = {
    pathPrefix("thing") {
      createThing ~
        linkThingToPerson ~
        linkThingToThing ~
        linkThingToPropertyStatic ~
        linkThingToPropertyDynamic
    }
  }

  import JsonProtocol._

  def createThing = path("") {
    post {
      entity(as[ApiThing]) { thing =>
        db.withSession { implicit session =>
          val thingsthingRow = new ThingsThingRow(0, LocalDateTime.now(), LocalDateTime.now(), thing.name)
          val result = Try((ThingsThing returning ThingsThing.map(_.id)) += thingsthingRow)
          complete {
            result match {
              case Success(thingId) =>
                thing.copy(id = Some(thingId))
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }
        }
      }
    }
  }

  def linkThingToPerson = path(IntNumber / "person" / IntNumber) { (thingId: Int, personId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"thing/$thingId/person/$personId:${relationship.relationshipType}")

          val crossref = new ThingsThingpersoncrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
            personId, thingId, relationship.relationshipType, true, recordId)
          val result = Try((ThingsThingpersoncrossref returning ThingsThingpersoncrossref.map(_.id)) += crossref)

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
   * Link two things together, e.g. as one thing part of another thing with a parentChild relationship type
   */
  def linkThingToThing = path(IntNumber / "thing" / IntNumber) { (thingId: Int, thing2Id: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"thing/$thingId/thing/$thing2Id:${relationship.relationshipType}")

          val crossref = new ThingsThingtothingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
            thingId, thing2Id, relationship.relationshipType, true, recordId)
          val result = Try((ThingsThingtothingcrossref returning ThingsThingtothingcrossref.map(_.id)) += crossref)

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
   * Link thing to a property statically (tying it in with a specific record ID)
   */
  def linkThingToPropertyStatic = path(IntNumber / "property" / "static" / IntNumber) { (thingId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipStatic]) { relationship =>
        val result: Try[Int] = (relationship.field.id, relationship.record.id) match {
          case (Some(fieldId), Some(recordId)) =>
            val propertyRecordId = createPropertyRecord(
              s"thing/$thingId/property/$propertyId:${relationship.relationshipType}(${fieldId},${recordId},${relationship.relationshipType}")

            // Create the crossreference record and insert into db
            val crossref = new ThingsSystempropertystaticcrossrefRow(
              0, LocalDateTime.now(), LocalDateTime.now(),
              thingId, propertyId,
              recordId, fieldId, relationship.relationshipType,
              true, propertyRecordId
            )

            db.withSession { implicit session =>
              Try((ThingsSystempropertystaticcrossref returning ThingsSystempropertystaticcrossref.map(_.id)) += crossref)
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
   * Link thing to a property dynamically
   */
  def linkThingToPropertyDynamic = path(IntNumber / "property" / "dynamic" / IntNumber) { (thingId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipDynamic]) { relationship =>
        val result: Try[Int] = relationship.field.id match {
          case Some(fieldId) =>
            val propertyRecordId = createPropertyRecord(
              s"""thing/$thingId/property/$propertyId:${relationship.relationshipType}
                  |(${fieldId},${relationship.relationshipType})""".stripMargin)

            // Create the crossreference record and insert into db
            val crossref = new ThingsSystempropertydynamiccrossrefRow(
              0, LocalDateTime.now(), LocalDateTime.now(),
              thingId, propertyId,
              fieldId, relationship.relationshipType,
              true, propertyRecordId)

            db.withSession { implicit session =>
              Try((ThingsSystempropertydynamiccrossref returning ThingsSystempropertydynamiccrossref.map(_.id)) += crossref)
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
   * Tag thing with a type
   */
  def addThingType = path(IntNumber / "type" / IntNumber) { (thingId: Int, typeId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val thingType = new ThingsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
            thingId, typeId, relationship.relationshipType, true)
          val result = Try((ThingsSystemtypecrossref returning ThingsSystemtypecrossref.map(_.id)) += thingType)

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

}

