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
trait InboundOrganisationsService extends HttpService {

  val routes = {
    pathPrefix("inbound") {
      createOrganisation
    }
  }

  val conf = ConfigFactory.load()
  val dbconfig = conf.getString("applicationDb")
  val db = Database.forConfig(dbconfig)

  import InboundJsonProtocol._

  def createOrganisation = path("organisation") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiOrganisation]) { organisation =>
          db.withSession { implicit session =>
            val organisationRow = new OrganisationsOrganisationRow(0, LocalDateTime.now(), LocalDateTime.now(), organisation.name)
            val orgId = (OrganisationsOrganisation returning OrganisationsOrganisation.map(_.id)) += organisationRow
            complete(Created, {
              organisation.copy(id = Some(orgId))
            })
          }

        }
      }
    }
  }
}

