package hatdex.hat.api.endpoints

import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.LocationsService
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

import scala.util.{Failure, Success, Try}


// this trait defines our service behavior independently from the service actor
trait Location extends LocationsService with AbstractEntity {
  val entityKind = "location"

  val routes = {
    pathPrefix(entityKind) {
      userPassHandler { implicit user: User =>
        createApi ~
          getApi ~
          getApiValues ~
          getAllApi ~
          linkToLocation ~
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
    entity(as[ApiLocation]) { location =>
      db.withSession { implicit session =>
        val locationslocationRow = new LocationsLocationRow(0, LocalDateTime.now(), LocalDateTime.now(), location.name)
        val result = Try((LocationsLocation returning LocationsLocation) += locationslocationRow)

        complete {
          result match {
            case Success(createdLocation) =>
              val newEntity = new EntityRow(0, LocalDateTime.now(), LocalDateTime.now(), createdLocation.name, entityKind, Some(createdLocation.id), None, None, None, None)
              Try(Entity += newEntity)
              (Created, ApiLocation.fromDbModel(createdLocation))
            case Failure(e) =>
              (BadRequest, e.getMessage)
          }
        }
      }
    }
  }
}

