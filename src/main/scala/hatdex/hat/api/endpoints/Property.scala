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

import scala.util.{Failure, Success}


// this trait defines our service behavior independently from the service actor
trait Property extends HttpService with PropertyService with HatServiceAuthHandler {

  val db = DatabaseInfo.db
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

  def getPropertiesApi = pathEnd {
    get {
      userPassHandler { implicit user: User =>
        parameters('name.?) { (maybePropertyName: Option[String]) =>
          db.withSession { implicit session =>
            complete {
              val properties = getProperties(maybePropertyName)
              session.close()
              properties
            }
          }
        }
      }
    }
  }
}

