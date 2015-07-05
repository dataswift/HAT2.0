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
trait InboundThingsService extends HttpService {

  val routes = {
    pathPrefix("inbound") {
      respondWithMediaType(`application/json`) {
        createThing ~ linkThingToPerson ~ linkThingToThing
      }
    }
  }

  val conf = ConfigFactory.load()
  val dbconfig = conf.getString("applicationDb")
  val db = Database.forConfig(dbconfig)

  import InboundJsonProtocol._

  def createThing = path("thing") {
    post {
        entity(as[ApiEvent]) { thing =>
          db.withSession { implicit session =>
            val thingRow = new ThingsThingRow(0, LocalDateTime.now(), LocalDateTime.now(), thing.name)
            val thingId = (ThingsThing returning ThingsThing.map(_.id)) += thingRow
            complete(Created, {
              thing.copy(id = Some(thingId))
            })
          }

        }

    }
  }

  def linkThingToPerson = path("thing" / IntNumber / "linkToPerson" / IntNumber) { (thingId : Int, personId : Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          // Create the crossreference record and insert into db
          val crossref = new ThingsThingpersoncrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), relationship.description, personId, thingId, relationship.relationshipType, true)
          val crossrefId = (ThingsThingpersoncrossref returning ThingsThingpersoncrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }

        }
      }
    }
  }

  def linkThingToThing = path("thing" / IntNumber / "linkToThing" / IntNumber) { (thingId : Int, toThingId : Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          // Create the crossreference record and insert into db
          // FIXME: inconsistent crossref - doesn't take description
          val crossref = new ThingsThingtothingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), /*relationship.description,*/ thingId, toThingId, relationship.relationshipType/*, true*/)
          val crossrefId = (ThingsThingtothingcrossref returning ThingsThingtothingcrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }

        }
      }
    }
  }
}

