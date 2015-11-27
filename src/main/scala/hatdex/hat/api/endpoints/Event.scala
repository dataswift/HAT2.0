package hatdex.hat.api.endpoints

import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.EventsService
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

import scala.util.{Failure, Success, Try}


// this trait defines our service behavior independently from the service actor
trait Event extends EventsService with AbstractEntity {
  import JsonProtocol._

  val entityKind = "event"

  val routes = {
    pathPrefix(entityKind) {
      userPassHandler { implicit user: User =>
        createApi ~
          getApi ~
          getApiValues ~
          getAllApi ~
          linkToLocation ~
          linkToOrganisation ~
          linkToPerson ~
          linkToThing ~
          linkToEvent ~
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

  /*
   * Create a simple event, containing only the name
   */
  def createEntity = pathEnd {
    entity(as[ApiEvent]) { event =>
      db.withSession { implicit session =>
        val eventseventRow = new EventsEventRow(0, LocalDateTime.now(), LocalDateTime.now(), event.name)
        val result = Try((EventsEvent returning EventsEvent) += eventseventRow)
        val entity = result map { createdEvent =>
          val newEntity = new EntityRow(createdEvent.id, LocalDateTime.now(), LocalDateTime.now(), createdEvent.name, "event", None, None, Some(createdEvent.id), None, None)
          val entityCreated = Try(Entity += newEntity)
          logger.debug("Creating new entity for event:" + entityCreated)
        }

        complete {
          result match {
            case Success(createdEvent) =>
              (Created, ApiEvent.fromDbModel(createdEvent))
            case Failure(e) =>
              (BadRequest, e.getMessage)
          }
        }
      }
    }
  }

}