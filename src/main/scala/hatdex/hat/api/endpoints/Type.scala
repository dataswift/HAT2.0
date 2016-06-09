package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import spray.http.StatusCode

//import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{ Failure, Success, Try }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

// this trait defines our service behavior independently from the service actor
trait Type extends HttpService with HatServiceAuthHandler {

  val logger: LoggingAdapter

  val db = DatabaseInfo.db

  val routes = {
    pathPrefix("type") {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner", "platform")) {
          createType ~
            linkTypeToType ~
            createUnitOfMeasurement
        } ~
          getTypes ~
          getUnitsOfMeasurement
      }
    }
  }

  import JsonProtocol._

  def createType = path("type") {
    pathEnd {
      post {
        entity(as[ApiSystemType]) { systemType =>
          val typeRow = new SystemTypeRow(0, LocalDateTime.now(), LocalDateTime.now(), systemType.name, systemType.description)
          val typeMaybeInserted = db.run {
            ((SystemType returning SystemType) += typeRow).asTry
          } flatMap {
            case Success(typeInserted) => Future { Success(typeInserted) }
            case Failure(e) =>
              db.run {
                SystemType.filter(_.name === typeRow.name).take(1).result
              } map { types =>
                Try { types.headOption.get }
              }
          }

          val typeResponse = typeMaybeInserted map {
            case Success(typeInserted) => (Created, ApiSystemType.fromDbModel(typeInserted))
            case Failure(e)            => throw ApiError(BadRequest, ErrorMessage("Error when creating type", e.getMessage))
          }

          onComplete(typeResponse) {
            case Success((statusCode: StatusCode, value)) => complete((statusCode, value))
            case Failure(e: ApiError)                     => complete((e.statusCode, e.message))
            case Failure(e)                               => complete((InternalServerError, ErrorMessage("Error while creating type", "Unknown error occurred")))
          }
        }
      }
    }
  }

  def linkTypeToType = path(IntNumber / "type" / IntNumber) { (typeId: Int, toTypeId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        val crossref = new SystemTypetotypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), typeId, toTypeId, relationship.relationshipType)
        val maybeCrossrefId = db.run(((SystemTypetotypecrossref returning SystemTypetotypecrossref.map(_.id)) += crossref).asTry)

        val typeResponse = maybeCrossrefId map {
          case Success(crossrefId) => (Created, ApiGenericId(crossrefId))
          case Failure(e)          => throw ApiError(BadRequest, ErrorMessage("Error linking Types", e.getMessage))
        }

        onComplete(typeResponse) {
          case Success((statusCode: StatusCode, value)) => complete((statusCode, value))
          case Failure(e: ApiError)                     => complete((e.statusCode, e.message))
          case Failure(e)                               => complete((InternalServerError, ErrorMessage("Error while linking types", "Unknown error occurred")))
        }
      }
    }

  }

  def getTypes = path("type") {
    pathEnd {
      get {
        parameters('name.?) { (maybeTypeName: Option[String]) =>
          logger.debug(s"Looking for type $maybeTypeName")
          val typesQuery = SystemType
          val typesNamed = maybeTypeName match {
            case Some(typeName) => typesQuery.filter(_.name === typeName)
            case None           => typesQuery
          }

          val fTypes = db.run(typesNamed.result)
          val apiTypes = fTypes.map(_.map(ApiSystemType.fromDbModel))
          onComplete(apiTypes) {
            case Success(types) =>
              logger.debug(s"Found types: $types")
              complete(types)
            case Failure(e) => complete((InternalServerError, ErrorMessage("Error while fetching types", "Unknown error")))
          }
        }
      }
    }
  }

  def createUnitOfMeasurement = path("unitofmeasurement") {
    post {
      entity(as[ApiSystemUnitofmeasurement]) { systemUom =>
        val uomRow = new SystemUnitofmeasurementRow(0, LocalDateTime.now(), LocalDateTime.now(), systemUom.name, systemUom.description, systemUom.symbol)
        val uomMaybeInserted = db.run {
          ((SystemUnitofmeasurement returning SystemUnitofmeasurement) += uomRow).asTry
        } flatMap {
          case Success(uomInserted) => Future { Success(uomInserted) }
          case Failure(e) =>
            db.run {
              SystemUnitofmeasurement.filter(_.name === uomRow.name).take(1).result
            } map { uoms =>
              Try { uoms.headOption.get }
            }
        }

        val uomResponse = uomMaybeInserted map {
          case Success(uomInserted) => (Created, ApiSystemUnitofmeasurement.fromDbModel(uomInserted))
          case Failure(e)           => throw ApiError(BadRequest, ErrorMessage("Error creating Unit of Measurement", e.getMessage))
        }

        onComplete(uomResponse) {
          case Success((statusCode: StatusCode, value)) => complete((statusCode, value))
          case Failure(e: ApiError)                     => complete((e.statusCode, e.message))
          case Failure(e)                               => complete((InternalServerError, ErrorMessage("Error while creating Unit of Measurement", "Unknown error occurred")))
        }
      }
    }

  }

  def getUnitsOfMeasurement = path("unitofmeasurement") {
    pathEnd {
      get {
        parameters('name.?) { (maybeUomName: Option[String]) =>
          val uomsQuery = SystemUnitofmeasurement
          val uomsNamed = maybeUomName match {
            case Some(uomName) => uomsQuery.filter(_.name === uomName)
            case None          => uomsQuery
          }
          val uoms = db.run(uomsNamed.result)
            .map(_.map(ApiSystemUnitofmeasurement.fromDbModel))

          onComplete(uoms) {
            case Success(uoms) => complete(uoms)
            case Failure(e)    => complete((InternalServerError, ErrorMessage("Error while fetching Units of Measurement", "Unknown error")))
          }
        }
      }
    }
  }

}