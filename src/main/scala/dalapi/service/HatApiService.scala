package dalapi.service

import com.typesafe.config.ConfigFactory
import dal.SlickPostgresDriver.simple._
import dal.Tables._
import org.joda.time.LocalDateTime

import scala.util.{Failure, Success, Try}


trait HatApiService {
  val conf = ConfigFactory.load()
  val dbconfig = conf.getString("applicationDb")
  val db = Database.forConfig(dbconfig)

  protected def createRelationshipRecord(relationshipName: String) = {
    db.withSession { implicit session =>
      val newRecord = new SystemRelationshiprecordRow(0, LocalDateTime.now(), LocalDateTime.now(), relationshipName)
      val recordId = (SystemRelationshiprecord returning SystemRelationshiprecord.map(_.id)) += newRecord
      recordId
    }
  }

  protected def createPropertyRecord(relationshipName: String) = {
    db.withSession { implicit session =>
      val newRecord = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), relationshipName)
      val recordId = (SystemPropertyrecord returning SystemRelationshiprecord.map(_.id)) += newRecord
      recordId
    }
  }

  protected def flatten[T](xs: Seq[Try[T]]): Try[Seq[T]] = {
    val (ss: Seq[Success[T]]@unchecked, fs: Seq[Failure[T]]@unchecked) =
      xs.partition(_.isSuccess)

    if (fs.isEmpty) Success(ss map (_.get))
    else Failure[Seq[T]](fs(0).exception) // Only keep the first failure
  }
}
