package hatdex.hat.api.service

import akka.event.LoggingAdapter
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
import scala.util.Try
import scala.util.Success
import scala.util.Failure


// this trait defines our service behavior independently from the service actor
trait TypeService extends HttpService with DatabaseInfo with HatServiceAuthHandler {

  val logger: LoggingAdapter

  val routes = {
    pathPrefix("type") {
      createType ~
        linkTypeToType ~
        createUnitOfMeasurement
    }
  }

  import JsonProtocol._

  def createType = {
    post {
      userPassHandler { implicit user: User =>
        entity(as[ApiSystemType]) { systemType =>
          db.withSession { implicit session =>
            val typeRow = new SystemTypeRow(0, LocalDateTime.now(), LocalDateTime.now(), systemType.name, systemType.description)
            val typeMaybeInserted = Try((SystemType returning SystemType) += typeRow)
            complete {
              typeMaybeInserted match {
                case Success(typeInserted) =>
                  (Created, ApiSystemType.fromDbModel(typeInserted))
                case Failure(e) =>
                  (BadRequest, ErrorMessage("Error creating Type", e.getMessage))
              }
            }
          }

        }

      }
    }
  }

  def linkTypeToType = path(IntNumber / "type" / IntNumber) { (typeId: Int, toTypeId: Int) =>
    post {
      userPassHandler { implicit user: User =>
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
  }

  def createUnitOfMeasurement = path("unitofmeasurement") {
    post {
      userPassHandler { implicit user: User =>
        entity(as[ApiSystemUnitofmeasurement]) { systemUom =>
          db.withSession { implicit session =>
            val uomRow = new SystemUnitofmeasurementRow(0, LocalDateTime.now(), LocalDateTime.now(), systemUom.name, systemUom.description, systemUom.symbol)
            val uomMaybeInserted = Try((SystemUnitofmeasurement returning SystemUnitofmeasurement) += uomRow)
            complete(Created, {
              uomMaybeInserted.map { uomInserted =>
                ApiSystemUnitofmeasurement.fromDbModel(uomInserted)
              }
            })
          }

        }
      }
    }
  }

}