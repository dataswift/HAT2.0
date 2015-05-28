import dal.Tables
import Tables._
import Tables.profile.simple._
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import autodal.SlickPostgresDriver.simple._
import slick.jdbc.meta.MTable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ModelSpec extends Specification {
  val db = Database.forConfig("devdb")
  implicit val session: Session = db.createSession()

  "Core Tables" should {
    "be created" in {

      val getTables = MTable.getTables(None, Some("public"), None, None).map { ts =>
        ts.map { t =>
          t.name.name
        }
      }

      val requiredTables: Seq[String] = Seq(
        "data_table",
        "things_thing",
        "events_event",
        "people_person",
        "locations_location"
      )

      val tables = db.run(getTables)
      tables must containAllOf[String](requiredTables).await
    }
  }

  sequential

  "Data tables" should {
    "be empty" in {
      val result = DataValue.run
      result must have size(0)
    }
    "accept data" in {
      val dataRecordRow = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test")
      DataRecord += dataRecordRow

      // The ID value is actually ignored and auto-incremented
      val dataRow = new DataValueRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test", 1)
      DataValue += dataRow

      val result = DataValue.run
      result must have size(1)
    }
    "allow data to be removed" in {
      DataValue.delete
      DataRecord.delete
      val result = DataValue.run
      result must have size(0)
    }
  }
}