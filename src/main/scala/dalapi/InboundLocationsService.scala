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
trait InboundLocationsService extends HttpService {

  val routes = {
    pathPrefix("inbound") {
      createLocation
    }
  }

  val conf = ConfigFactory.load()
  val dbconfig = conf.getString("applicationDb")
  val db = Database.forConfig(dbconfig)

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
}

