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
      createProperty ~
        getPropertyApi
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

  def getPropertyApi = path(IntNumber) { (propertyId: Int) =>
    get {
      db.withSession { implicit session =>
        val propertyOption = getProperty(propertyId)
        complete {
          propertyOption match {
            case Some(property) =>
              property
            case None =>
              (NotFound, s"Property $propertyId not found")
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

  protected def getProperty(propertyId: Int)(implicit session: Session) : Option[ApiProperty] = {
    val propertyQuery = for {
      property <- SystemProperty.filter(_.id === propertyId)
      systemType <- property.systemTypeFk
      uom <- property.systemUnitofmeasurementFk
    } yield (property, systemType, uom)

    val property = propertyQuery.run.headOption

    property map {
      case (property: SystemPropertyRow, systemType: SystemTypeRow, uom: SystemUnitofmeasurementRow) =>
        ApiProperty.fromDbModel(property)(ApiSystemType.fromDbModel(systemType), ApiSystemUnitofmeasurement.fromDbModel(uom))
    }
  }

}

