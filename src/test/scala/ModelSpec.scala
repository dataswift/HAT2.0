import dal.Tables
import dal.Tables._
import dal.Tables.profile.simple._
import org.joda.time.LocalDate
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
}