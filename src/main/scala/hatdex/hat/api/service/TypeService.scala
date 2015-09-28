package hatdex.hat.api.service

import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._


// this trait defines our service behavior independently from the service actor
trait TypeService extends HttpService with DatabaseInfo {

  val routes = {
    pathPrefix("type") {
      createType ~
        linkTypeToType ~
        createUnitOfMeasurement
    }
  }

  import JsonProtocol._

  def createType = path("") {
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

  def linkTypeToType = path(IntNumber / "type" / IntNumber) { (typeId : Int, toTypeId : Int) =>
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