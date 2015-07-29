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
trait InboundPropertyService extends HttpService {

  val routes = {
    pathPrefix("inbound") {
      creteProperty
    }
  }

  val conf = ConfigFactory.load()
  val dbconfig = conf.getString("applicationDb")
  val db = Database.forConfig(dbconfig)

  import InboundJsonProtocol._

  def creteProperty = path("property") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiProperty]) { property =>
          db.withSession { implicit session =>
            val row = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(),
              property.name, property.description, property.typeId, property.unitOfMeasurementId)
            val pid = (SystemProperty returning SystemProperty.map(_.id)) += row
            complete(Created, {
              property.copy(id = Some(pid))
            })
            complete(404, {})
          }

        }
      }
    }
  }
}

