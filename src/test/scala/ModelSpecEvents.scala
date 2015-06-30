import dal.Tables._
import org.specs2.specification.{AfterAll, BeforeAfterAll}

//import Tables._
//import Tables.profile.simple._
import dal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

class ModelSpecEvents extends Specification with AfterAll {
  val db = Database.forConfig("devdb")
  sequential

  def afterAll = {
    db.close()
  }

  "Core Tables" should {
    db.withSession { implicit session =>
      "be created" in {

        val getTables = MTable.getTables(None, Some("public"), None, None).map { ts =>
          ts.map { t =>
            t.name.name
          }
        }

        val requiredTables: Seq[String] = Seq(
          "data_table",
          "organisations_organisation",
          "people_person",
          "events_event",
          "data_field",
          "system_properties",
          "things_thing"
        )

        val tables = db.run(getTables)
        tables must containAllOf[String](requiredTables).await
      }
    }
  }

  "events tables" should {
    db.withSession { implicit session =>
      "be empty" in {
        val result = EventsEvent.run
        result must have size (0)
      }
      "accept data" in {

        val eventseventRow = new EventsEventRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test Event for HAT")
        val eventId = (EventsEvent returning EventsEvent.map(_.id)) += eventseventRow

        val relationshiptype = Some("Relationship description")
        val findeventId = EventsEvent.filter(_.name === "Test Event for HAT").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "timezone").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "timezone").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head
  

        val eventseventtoeventcrossrefRow = new EventsEventtoeventcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findeventId, findeventId, relationshiptype)
        val eventseventtoeventcrossrefId = (EventsEventtoeventcrossref returning EventsEventtoeventcrossref.map(_.id)) += eventseventtoeventcrossrefRow

        val description = Some("An example SystemUnitofmeasurement")

        val eventssystempropertystaticcrossrefRow = new EventsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findeventId, findpropertyId, findfieldId, findrecordId, relationshiptype, true)
        val eventssystempropertystaticcrossrefId = (EventsSystempropertystaticcrossref returning EventsSystempropertystaticcrossref.map(_.id)) += eventssystempropertystaticcrossrefRow

        val eventssystempropertydynamiccrossrefRow = new EventsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findeventId, findpropertyId, findfieldId, relationshiptype, true)
        val eventssystempropertydynamiccrossrefId = (EventsSystempropertydynamiccrossref returning EventsSystempropertydynamiccrossref.map(_.id)) += eventssystempropertydynamiccrossrefRow

        EventsEvent += eventseventRow

        val result = EventsEvent.run
        result must have size (1)
      }
      "allow data to be removed" in {
        EventsEvent.delete
        EventsEvent.run must have size (0)

        EventsEventtoeventcrossref.delete
        EventsEventtoeventcrossref.run must have size (0)

        EventsSystempropertydynamiccrossref.delete
        EventsSystempropertydynamiccrossref.run must have size (0)

        EventsSystempropertystaticcrossref.delete
        EventsSystempropertystaticcrossref.run must have size (0)

      }
    }
  }

}