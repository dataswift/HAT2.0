package hatdex.hat.api.endpoints

import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.ThingsService
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

import scala.util.{Failure, Success, Try}


// this trait defines our service behavior independently from the service actor
trait Thing extends ThingsService with AbstractEntity {
  val entityKind = "thing"

  val routes = {
    pathPrefix(entityKind) {
      userPassHandler { implicit user: User =>
        createApi ~
          getApi ~
          getApiValues ~
          getAllApi ~
          linkToPerson ~
          linkToThing ~
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
    entity(as[ApiThing]) { thing =>
      db.withSession { implicit session =>
        val thingsthingRow = new ThingsThingRow(0, LocalDateTime.now(), LocalDateTime.now(), thing.name)
        val result = Try((ThingsThing returning ThingsThing) += thingsthingRow)
        val entity = result map { createdThing =>
          val newEntity = new EntityRow(createdThing.id, LocalDateTime.now(), LocalDateTime.now(), createdThing.name, "thing", None, Some(createdThing.id), None, None, None)
          val entityCreated = Try(Entity += newEntity)
          logger.debug("Creating new entity for thing:" + entityCreated)
        }

        complete {
          result match {
            case Success(createdThing) =>
              (Created, ApiThing.fromDbModel(createdThing))
            case Failure(e) =>
              (BadRequest, e.getMessage)
          }
        }
      }
    }
  }
}

