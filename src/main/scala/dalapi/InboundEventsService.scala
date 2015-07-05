package dalapi

import javax.ws.rs.Path

import akka.actor.ActorLogging
import com.wordnik.swagger.annotations._
import dal.Tables._
import dal.SlickPostgresDriver.simple._
import dalapi.models.{ApiDataTable, ApiDataRecord, ApiDataField, ApiDataValue}
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.json._
import spray.routing._
import spray.httpx.SprayJsonSupport._
import spray.util.LoggingContext
import spray.http.StatusCodes._
import com.typesafe.config.{Config, ConfigFactory}
import scala.annotation.meta.field
import scala.concurrent.ExecutionContext.Implicits.global
import dalapi.models._


// this trait defines our service behavior independently from the service actor
trait InboundEventsService extends HttpService {

  val routes = {
    pathPrefix("inbound") {
      createEvent
    }
  }

  val conf = ConfigFactory.load()
  val dbconfig = conf.getString("applicationDb")
  val db = Database.forConfig(dbconfig)

  import InboundJsonProtocol._

  def createEvent = path("event") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiEvent]) { event =>
          db.withSession { implicit session =>
            val eventseventRow = new EventsEventRow(0, LocalDateTime.now(), LocalDateTime.now(), event.name)
            val eventId = (EventsEvent returning EventsEvent.map(_.id)) += eventseventRow
            complete(Created, {
              event.copy(id = Some(eventId))
            })
          }

        }
      }
    }
  }
}

