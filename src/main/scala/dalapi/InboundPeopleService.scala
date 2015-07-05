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
trait InboundPeopleService extends HttpService {

  val routes = {
    pathPrefix("inbound") {
      createPerson
    }
  }

  val conf = ConfigFactory.load()
  val dbconfig = conf.getString("applicationDb")
  val db = Database.forConfig(dbconfig)

  import InboundJsonProtocol._

  def createPerson = path("person") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiPerson]) { person =>
          db.withSession { implicit session =>
            val personRow = new PeoplePersonRow(0, LocalDateTime.now(), LocalDateTime.now(), person.name, person.personId)
            val personId = (PeoplePerson returning PeoplePerson.map(_.id)) += personRow
            complete(Created, {
              person.copy(id = Some(personId))
            })
          }

        }
      }
    }
  }
}

