package dalapi.service

import com.typesafe.config.ConfigFactory
import dal.SlickPostgresDriver.simple._
import dal.Tables._
import org.joda.time.LocalDateTime


trait InboundService {
  val conf = ConfigFactory.load()
  val dbconfig = conf.getString("applicationDb")
  val db = Database.forConfig(dbconfig)

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

  protected[service] def seqOption[T](seq: Seq[T]) : Option[Seq[T]] = {
    if (seq.isEmpty)
      None
    else
      Some(seq)
  }
}
