package hatdex.hat.api.endpoints

import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.PeopleService
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

import scala.util.{Failure, Success, Try}


// this trait defines our service behavior independently from the service actor
trait Person extends PeopleService with AbstractEntity {
  val entityKind = "person"

  val routes = {
    pathPrefix(entityKind) {
      userPassHandler { implicit user: User =>
        createApi ~
          getApi ~
          getApiValues ~
          getAllApi ~
          createPersonRelationshipType ~
          getPersonRelationshipTypes ~
          linkToPerson ~
          linkToLocation ~
          linkToOrganisation ~
          linkToPropertyStatic ~
          linkToPropertyDynamic ~
          addTypeApi ~
          getPropertiesStaticApi ~
          getPropertiesDynamicApi ~
          getPropertyStaticValueApi ~
          getPropertyDynamicValueApi
      }
    }
  }

  import JsonProtocol._

  def createEntity = pathEnd {
    entity(as[ApiPerson]) { person =>
      db.withSession { implicit session =>
        val personspersonRow = new PeoplePersonRow(0, LocalDateTime.now(), LocalDateTime.now(), person.name, person.personId)
        val result = Try((PeoplePerson returning PeoplePerson) += personspersonRow)
        session.close()
        complete {
          result match {
            case Success(createdPerson) =>
              val newEntity = new EntityRow(0, LocalDateTime.now(), LocalDateTime.now(), createdPerson.name, "person", None, None, None, None, Some(createdPerson.id))
              Try(Entity += newEntity)
              (Created, ApiPerson.fromDbModel(createdPerson))
            case Failure(e) =>
              (BadRequest, e.getMessage)
          }
        }
      }
    }
  }

  def createPersonRelationshipType = path("relationshipType") {
    logger.debug("Relationship type")
    post {
      logger.debug("Creating Relationship type")
      entity(as[ApiPersonRelationshipType]) { relationship =>
        logger.debug("Creating Relationship type PARSED")
        db.withSession { implicit session =>
          logger.debug("Creating Relationship SESSION")
          val reltypeRow = new PeoplePersontopersonrelationshiptypeRow(0, LocalDateTime.now(), LocalDateTime.now(), relationship.name, relationship.description)
          val maybeReltype = Try((PeoplePersontopersonrelationshiptype returning PeoplePersontopersonrelationshiptype) += reltypeRow)
          session.close()
          complete {
            maybeReltype match {
              case Success(reltype) =>
                (Created, ApiPersonRelationshipType.fromDbModel(reltype))
              case Failure(e) =>
                (BadRequest, ErrorMessage("Error creating relationship type", e.getMessage))
            }

          }
        }

      }
    }
  }

  def getPersonRelationshipTypes = path("relationshipType") {
    get {
      logger.debug("Getting Relationship types")
      db.withSession { implicit session =>
        val relTypes = PeoplePersontopersonrelationshiptype.run
        complete {
          (OK, relTypes.map(ApiPersonRelationshipType.fromDbModel))
        }
      }
    }
  }

  /*
   * Link two people together, e.g. as one person part of another person with a given relationship type
   */
  override def linkToPerson = path(IntNumber / "person" / IntNumber) { (personId: Int, person2Id: Int) =>
    post {
      entity(as[ApiPersonRelationshipType]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$personId/person/$person2Id:${relationship.name}")

          val result = relationship.id match {
            case Some(relationshipTypeId) =>
              createLinkPerson(personId, person2Id, relationshipTypeId, recordId)
            case None =>
              Failure(new IllegalArgumentException("People can only be linked with an existing relationship type"))
          }

          session.close()

          // Return the created crossref
          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, ErrorMessage(s"Error linking ${entityKind} to person", e.getMessage))
            }
          }

        }
      }
    }
  }
}

