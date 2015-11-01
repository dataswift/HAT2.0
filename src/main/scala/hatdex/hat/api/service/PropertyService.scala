package hatdex.hat.api.service

import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Success, Try}


// this trait defines our service behavior independently from the service actor
trait PropertyService extends HttpService with DatabaseInfo with HatServiceAuthHandler {

  val dataService: DataService

  val routes = {
    pathPrefix("property") {
      createProperty ~
        getPropertyApi ~
        getPropertiesApi
    }
  }

  import JsonProtocol._

  def createProperty = {
    post {
      userPassHandler { implicit user: User =>
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
  }

  def getPropertyApi = path(IntNumber) { (propertyId: Int) =>
    get {
      userPassHandler { implicit user: User =>
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
  }

  def getPropertiesApi = {
    get {
      userPassHandler { implicit user: User =>
        parameters('name.?) { (maybePropertyName: Option[String]) =>
          db.withSession { implicit session =>
            val propertiesNamed = maybePropertyName match {
              case Some(proeprtyName) =>
                SystemProperty.filter(_.name === proeprtyName)
              case None =>
                SystemProperty
            }

            val propertiesQuery = for {
              property <- propertiesNamed
              systemType <- property.systemTypeFk
              uom <- property.systemUnitofmeasurementFk
            } yield (property, systemType, uom)

            val properties = propertiesQuery.run
            session.close()

            complete {
              properties map {
                case (property: SystemPropertyRow, systemType: SystemTypeRow, uom: SystemUnitofmeasurementRow) =>
                  ApiProperty.fromDbModel(property)(ApiSystemType.fromDbModel(systemType), ApiSystemUnitofmeasurement.fromDbModel(uom))
              }
            }
          }
        }
      }
    }
  }

  def getPropertyRelationshipValues(propertyRel: ApiPropertyRelationshipStatic)
                                   (implicit session: Session): ApiPropertyRelationshipStatic = {
    // For each property relationship (should only ever be one)
    val fieldDataRetrieved = (propertyRel.field.id, propertyRel.record.id) match {
      // that has both data field with id and data record with id
      case (Some(propertyFieldId), Some(propertyRecordId)) =>
        // get the values
        dataService.getFieldRecordValue(propertyFieldId, propertyRecordId)
      case _ =>
        None
    }
    fieldDataRetrieved match {
      case Some(fieldData) =>
        // Copy the new Data Field with data if found
        propertyRel.copy(field = fieldData)
      case None =>
        // Otherwise leave as is
        propertyRel
    }
  }

  def getPropertyRelationshipValues(propertyRel: ApiPropertyRelationshipDynamic)
                                   (implicit session: Session): ApiPropertyRelationshipDynamic = {
    // For each property relationship (should only ever be one)
    val fieldDataRetrieved = propertyRel.field.id match {
      // that has both data field with id and data record with id
      case Some(propertyFieldId) =>
        // get the values
        dataService.getFieldValues(propertyFieldId)
      case _ =>
        None
    }
    fieldDataRetrieved match {
      case Some(fieldData) =>
        // Copy the new Data Field with data if found
        propertyRel.copy(field = fieldData)
      case None =>
        // Otherwise leave as is
        propertyRel
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

  protected def getProperty(propertyId: Int)(implicit session: Session): Option[ApiProperty] = {
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

