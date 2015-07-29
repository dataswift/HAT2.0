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


trait InboundService {
  val conf = ConfigFactory.load()
  val dbconfig = conf.getString("applicationDb")
  val db = Database.forConfig(dbconfig)

  import InboundJsonProtocol._

  def createRelationshipRecord(relationshipName: String) = {
    db.withSession { implicit session =>
      val newRecord = new SystemRelationshiprecordRow(0, LocalDateTime.now(), LocalDateTime.now(), relationshipName)
      val recordId = (SystemRelationshiprecord returning SystemRelationshiprecord.map(_.id)) += newRecord
      recordId
    }
  }

  def createPropertyRecord(relationshipName: String) = {
    db.withSession { implicit session =>
      val newRecord = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), relationshipName)
      val recordId = (SystemPropertyrecord returning SystemRelationshiprecord.map(_.id)) += newRecord
      recordId
    }
  }
}
