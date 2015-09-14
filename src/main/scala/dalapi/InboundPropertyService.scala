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

import scala.util.{Failure, Success, Try}


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

  import ApiJsonProtocol._

  def creteProperty = path("property") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiProperty]) { property =>
          db.withSession { implicit session =>

            val result: Try[Int] = {
              (property.propertyType.id, property.unitOfMeasurement.id) match {
                case (Some(typeId:Int), Some(uomId:Int)) =>
                  val row = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(),
                    property.name, property.description,
                    typeId, uomId)
                  Try((SystemProperty returning SystemProperty.map(_.id)) += row)
                case (Some(typeId), None) =>
                  Failure(new IllegalArgumentException("Property must have an existing Type with ID"))
                case (None, _) =>
                  Failure(new IllegalArgumentException("Property must have an existing Unit of Measurement with ID"))
              }
            }

            complete {
              result match {
                case Success(pid) =>
                  (Created, property.copy(id = Some(pid)))
                case Failure(e) =>
                  (BadRequest, e.getMessage)
              }
            }
          }

        }
      }
    }
  }
}

