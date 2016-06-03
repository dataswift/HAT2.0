package hatdex.hat.api.endpoints

import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.OrganisationsService
import hatdex.hat.authentication.models.User
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

import scala.util.{Failure, Success, Try}


// this trait defines our service behavior independently from the service actor
trait Organisation extends OrganisationsService with AbstractEntity {
  val entityKind = "organisation"

  val routes = {
    pathPrefix(entityKind) {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          createApi ~
            getApi ~
            getApiValues ~
            getAllApi ~
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
  }

  import JsonProtocol._

  def createEntity = pathEnd {
    entity(as[ApiOrganisation]) { organisation =>
      db.withSession { implicit session =>
        val organisationsorganisationRow = new OrganisationsOrganisationRow(0, LocalDateTime.now(), LocalDateTime.now(), organisation.name)
        val result = Try((OrganisationsOrganisation returning OrganisationsOrganisation) += organisationsorganisationRow)
        val entity = result map { createdOrganisation =>
          val newEntity = new EntityRow(createdOrganisation.id, LocalDateTime.now(), LocalDateTime.now(), createdOrganisation.name, "organisation", None, None, None, Some(createdOrganisation.id), None)
          val entityCreated = Try(Entity += newEntity)
          logger.debug("Creating new entity for organisation:" + entityCreated)
        }

        complete {
          result match {
            case Success(createdOrganisation) =>
              (Created, ApiOrganisation.fromDbModel(createdOrganisation))
            case Failure(e) =>
              (BadRequest, e.getMessage)
          }
        }
      }
    }
  }
}

