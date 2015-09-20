package dalapi.service

import dal.SlickPostgresDriver.simple._
import dal.Tables._
import dalapi.DatabaseInfo
import dalapi.models._
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Success, Try}


// this trait defines our service behavior independently from the service actor
trait PropertyService extends HttpService with DatabaseInfo {

  val routes = {
    pathPrefix("property") {
      createProperty
    }
  }

  import JsonProtocol._

  def createProperty = path("") {
    post {
      entity(as[ApiProperty]) { property =>
        db.withSession { implicit session =>
          val result = storeProperty(property)
          complete {
            result match {
              case Success(created) =>
                (Created, created)
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }
        }
      }
    }
  }

  protected def storeProperty(property: ApiProperty)(implicit session: Session): Try[ApiProperty] = {
    (property.propertyType.id, property.unitOfMeasurement.id) match {
      case (Some(typeId: Int), Some(uomId: Int)) =>
        val row = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(),
          property.name, property.description,
          typeId, uomId)
        val createdTry = Try((SystemProperty returning SystemProperty) += row)
        createdTry map { created =>
          ApiProperty.fromDbModel(created)(property.propertyType, property.unitOfMeasurement)
        }
      case (Some(typeId), None) =>
        Failure(new IllegalArgumentException("Property must have an existing Type with ID"))
      case (None, _) =>
        Failure(new IllegalArgumentException("Property must have an existing Unit of Measurement with ID"))
    }
  }
}

