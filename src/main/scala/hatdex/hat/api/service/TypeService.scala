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
        getTypes ~
        createUnitOfMeasurement ~
        getUnitsOfMeasurement
    }
  }

  import JsonProtocol._

  def createType = path("type") {
    post {
      userPassHandler { implicit user: User =>
        entity(as[ApiSystemType]) { systemType =>
          db.withSession { implicit session =>
            val typeRow = new SystemTypeRow(0, LocalDateTime.now(), LocalDateTime.now(), systemType.name, systemType.description)
            val typeMaybeInserted = Try((SystemType returning SystemType) += typeRow)
            session.close()
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
            val maybeCrossrefId = Try((SystemTypetotypecrossref returning SystemTypetotypecrossref.map(_.id)) += crossref)
            session.close()
            complete {
              maybeCrossrefId match {
                case Success(crossrefId) =>
                  (Created, ApiGenericId(crossrefId))
                case Failure(e) =>
                  (BadRequest, ErrorMessage("Error linking Types", e.getMessage))
              }

            }
          }

        }
      }
    }
  }

  def getTypes = path("type") {
    get {
      userPassHandler { implicit user =>
        parameters('name.?) { (maybeTypeName: Option[String]) =>
          db.withSession { implicit session =>
            val typesQuery = SystemType
            val typesNamed = maybeTypeName match {
              case Some(typeName) =>
                typesQuery.filter(_.name === typeName)
              case None =>
                typesQuery
            }

            val types = typesNamed.run

            session.close()
            complete {
              types.map(ApiSystemType.fromDbModel)
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
            session.close()
            complete(
              uomMaybeInserted match {
                case Success(uomInserted) =>
                  (Created, ApiSystemUnitofmeasurement.fromDbModel(uomInserted))
                case Failure(e) =>
                  (BadRequest, ErrorMessage("Error creating Unit of Measurement", e.getMessage))
              }
            )
          }

        }
      }
    }
  }

  def getUnitsOfMeasurement = path("unitofmeasurement") {
    get {
      userPassHandler { implicit user =>
        parameters('name.?) { (maybeUomName: Option[String]) =>
          db.withSession { implicit session =>
            val uomsQuery = SystemUnitofmeasurement
            val uomsNamed = maybeUomName match {
              case Some(uomName) =>
                uomsQuery.filter(_.name === uomName)
              case None =>
                uomsQuery
            }
            val uoms = uomsNamed.run
            session.close()
            complete {
              uoms.map(ApiSystemUnitofmeasurement.fromDbModel)
            }
          }
        }
      }
    }
  }

}