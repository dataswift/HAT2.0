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
trait InboundTypeService extends HttpService with InboundService {

  val routes = {
    pathPrefix("inbound") {
      respondWithMediaType(`application/json`) {
        createType ~ linkTypeToType
      }
    }
  }

  import ApiJsonProtocol._

  def createType = path("type") {
    post {
      entity(as[ApiSystemType]) { systemType =>
        db.withSession { implicit session =>
          val typeRow = new SystemTypeRow(0, LocalDateTime.now(), LocalDateTime.now(), systemType.name, systemType.description)
          val typeId = (SystemType returning SystemType.map(_.id)) += typeRow
          complete(Created, {
            systemType.copy(id = Some(typeId))
          })
        }

      }

    }
  }

  def linkTypeToType = path("type" / IntNumber / "type" / IntNumber) { (typeId : Int, toTypeId : Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val crossref = new SystemTypetotypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), typeId, toTypeId, relationship.relationshipType)
          val crossrefId = (SystemTypetotypecrossref returning SystemTypetotypecrossref.map(_.id)) += crossref

          complete {
            ApiGenericId(crossrefId)
          }
        }

      }
    }
  }

  def createUnitOfMeasurement = path("unitofmeasurement") {
    post {
      entity(as[ApiSystemUnitofmeasurement]) { systemUom =>
        db.withSession { implicit session =>
          val uomRow = new SystemUnitofmeasurementRow(0, LocalDateTime.now(), LocalDateTime.now(), systemUom.name, systemUom.description, systemUom.symbol)
          val uomId = (SystemUnitofmeasurement returning SystemUnitofmeasurement.map(_.id)) += uomRow
          complete(Created, {
            systemUom.copy(id = Some(uomId))
          })
        }

      }

    }
  }

}