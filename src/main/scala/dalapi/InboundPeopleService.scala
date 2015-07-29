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
trait InboundPeopleService extends HttpService with InboundService {

  val routes = {
    pathPrefix("inbound") {
      createPerson ~
      createPersonRelationshipType ~
      linkPersonToPerson ~
      linkPersonToLocation ~
      linkPersonToOrganisation ~
      linkPersonToPropertyStatic ~
      linkPersonToPropertyDynamic ~
      addPersonType
    }
  }

  import InboundJsonProtocol._

  def createPerson = path("person") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiPerson]) { person =>
          db.withSession { implicit session =>
            val personRow = new PeoplePersonRow(0, LocalDateTime.now(), LocalDateTime.now(), person.name, person.personId)
            val personId = (PeoplePerson returning PeoplePerson.map(_.id)) += personRow
            complete(Created, {
              person.copy(id = Some(personId))
            })
          }

        }
      }
    }
  }

  def createPersonRelationshipType = path("person" / "relationshipType") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiPersonRelationshipType]) { relationship =>
          db.withSession { implicit session =>
            val reltypeRow = new PeoplePersontopersonrelationshiptypeRow(0, LocalDateTime.now(), LocalDateTime.now(), relationship.name, relationship.description)
            val reltypeId = (PeoplePersontopersonrelationshiptype returning PeoplePersontopersonrelationshiptype.map(_.id)) += reltypeRow
            complete(Created, {
              relationship.copy(id = Some(reltypeId))
            })
          }

        }
      }
    }
  }

  /*
   * Link two people together, e.g. as one person part of another person with a given relationship type
   */
  def linkPersonToPerson = path("person" / IntNumber / "person" / IntNumber ) { (personId : Int, toPersonId : Int) =>
    post {
      entity(as[ApiPersonRelationshipType]) { relationshipType =>
        db.withSession { implicit session =>

          relationshipType.id match {
            case Some(relationshipTypeId) =>
              val recordId = createRelationshipRecord(s"person/$personId/person/$toPersonId:${relationshipType.name}")

              // Create the crossreference record and insert into db
              val crossref = new PeoplePersontopersoncrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), personId, toPersonId, true, recordId, relationshipTypeId)
              val crossrefId = (PeoplePersontopersoncrossref returning PeoplePersontopersoncrossref.map(_.id)) += crossref

              // Return the created crossref
              complete {
                ApiGenericId(crossrefId)
              }

            case None =>
              complete (BadRequest, "People can only be linked with an existing relationship type")
          }


        }
      }
    }
  }

  def linkPersonToLocation = path("person" / IntNumber / "location" / IntNumber) { (personId : Int, locationId : Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"person/$personId/location/$locationId:${relationship.relationshipType}")

          val crossref = new PeoplePersonlocationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, personId, relationship.relationshipType, true, recordId)
          val crossrefId = (PeoplePersonlocationcrossref returning PeoplePersonlocationcrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }

        }
      }
    }
  }

  def linkPersonToOrganisation = path("person" / IntNumber / "organisation" / IntNumber) { (personId : Int, organisationId : Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"person/$personId/organisation/$organisationId:${relationship.relationshipType}")

          val crossref = new PeoplePersonorganisationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), organisationId, personId, relationship.relationshipType, true, recordId)
          val crossrefId = (PeoplePersonorganisationcrossref returning PeoplePersonorganisationcrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }

        }
      }
    }
  }

  /*
   * Link person to a property statically (tying it in with a specific record ID)
   */
  def linkPersonToPropertyStatic = path("person" / IntNumber / "property" / IntNumber / "static") { (personId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipStatic]) { relationship =>

        val recordId = createPropertyRecord(s"person/$personId/property/$propertyId:${relationship.relationshipType}(${relationship.fieldId},${relationship.recordId},${relationship.relationshipType})")

        // Create the crossreference record and insert into db
        val crossref = new PeopleSystempropertystaticcrossrefRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          personId, propertyId,
          relationship.fieldId, relationship.recordId, relationship.relationshipType,
          true, recordId
        )

        db.withSession { implicit session =>
          val crossrefId = (PeopleSystempropertystaticcrossref returning PeopleSystempropertystaticcrossref.map(_.id)) += crossref
          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }
        }
      }
    }
  }

  /*
   * Link person to a property dynamically
   */
  def linkPersonToPropertyDynamic = path("person" / IntNumber / "property" / IntNumber / "dynamic") { (personId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipDynamic]) { relationship =>

        val recordId = createPropertyRecord(s"person/$personId/property/$propertyId:${relationship.relationshipType}(${relationship.fieldId},${relationship.relationshipType})")

        // Create the crossreference record and insert into db
        val crossref = new PeopleSystempropertydynamiccrossrefRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          personId, propertyId,
          relationship.fieldId, relationship.relationshipType,
          true, recordId
        )

        db.withSession { implicit session =>
          val crossrefId = (PeopleSystempropertydynamiccrossref returning PeopleSystempropertydynamiccrossref.map(_.id)) += crossref
          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }
        }
      }
    }
  }

  /*
   * Tag person with a type
   */
  def addPersonType = path("person" / IntNumber / "type" / IntNumber) { (personId: Int, typeId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val personType = new PeopleSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), personId, typeId, relationship.relationshipType, true)
          val personTypeId = (PeopleSystemtypecrossref returning PeopleSystemtypecrossref.map(_.id)) += personType
          complete {
            ApiGenericId(personTypeId)
          }
        }
      }

    }
  }
}

