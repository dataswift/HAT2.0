package hatdex.hat.api.endpoints

import akka.actor.{ActorRefFactory, ActorContext}
import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import hatdex.hat.api.service.DataService
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import spray.http.StatusCode

import hatdex.hat.dal.SlickPostgresDriver.simple._
//import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.routing._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

// this trait defines our service behavior independently from the service actor
trait Stats extends HttpService with HatServiceAuthHandler with DataService {

  val logger: LoggingAdapter
  def actorRefFactory: ActorRefFactory

  val db = DatabaseInfo.db

  val routes = {
    pathPrefix("stats") {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("platform")) {
          apiGetTableStats
        }
      }
    }
  }

  def apiGetTableStats = path( "table" / IntNumber) { (tableId: Int) =>
      get {
        val valuesQuery = DataValue
        db.withSession { implicit session =>
          val maybeTable = getTableStructure(tableId)
          maybeTable map { tableStructure =>
            val fieldsOfInterest = getStructureFields(tableStructure)
            val fieldRecordCounts = DataValue.filter(_.fieldId inSet fieldsOfInterest)
                .groupBy(v => v.fieldId)
                .map{ case (fieldId, values) => (fieldId, values.map(_.recordId).countDistinct) }
                .run

            None
          }
        }
        complete((NotImplemented, "Not yet implemented"))

      }
  }


}