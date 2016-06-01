package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.PropertyService
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.models.User
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success, Try }

// this trait defines our service behavior independently from the service actor
trait Property extends HttpService with PropertyService with HatServiceAuthHandler {
  val logger: LoggingAdapter

  val routes = {
    pathPrefix("property") {
      createProperty ~
        getPropertyApi ~
        getPropertiesApi
    }
  }

  import JsonProtocol._

  def createProperty = pathEnd {
    post {
      userPassHandler { implicit user: User =>
        entity(as[ApiProperty]) { property =>
          onComplete(storeProperty(property)) {
            case Success(created) => complete((Created, created))
            case Failure(e)       => complete((BadRequest, ErrorMessage("Error creating property", e.getMessage)))
          }
        }
      }
    }
  }

  def getPropertyApi = path(IntNumber) { (propertyId: Int) =>
    get {
      userPassHandler { implicit user: User =>
        onComplete(getProperty(propertyId)) {
          case Success(Some(property)) => complete((OK, property))
          case Success(None)           => complete((NotFound, s"Property $propertyId not found"))
          case Failure(e)              => complete((InternalServerError, ErrorMessage("Error fetching property", e.getMessage)))
        }
      }
    }
  }

  def getPropertiesApi = pathEnd {
    get {
      userPassHandler { implicit user: User =>
        parameters('name.?) { (maybePropertyName: Option[String]) =>
          onComplete(getProperties(maybePropertyName)) {
            case Success(properties) => complete((OK, properties))
            case Failure(e)          => complete((InternalServerError, ErrorMessage("Error fetching property", e.getMessage)))
          }
        }
      }
    }
  }
}

