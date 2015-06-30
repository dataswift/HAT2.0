import dal.Tables._
import org.specs2.specification.{AfterAll, BeforeAfterAll}

//import Tables._
//import Tables.profile.simple._
import dal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

class ModelSpecThings extends Specification with AfterAll {
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
          "things_thing",
          "events_event",
          "people_person",
          "locations_location",
          "data_field",
          "system_properties"
        )

        val tables = db.run(getTables)
        tables must containAllOf[String](requiredTables).await
      }
    }
  }

  "Things tables" should {
    db.withSession { implicit session =>
      "be empty" in {
        val result = ThingsThing.run
        result must have size (0)
      }
      "accept data" in {

        val thingsthingRow = new ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test")
        val thingId = (ThingsThing returning ThingsThing.map(_.id)) += thingsthingRow

        val findthingId = ThingsThing.filter(_.name === "Cover").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "cover").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "cover").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "cover").map(_.id).run.head

        val relationshiptype = Some("Relationship description")

        val thingsthingtothingcrossrefRow = new ThingsThingtothingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findthingId, findthingId, relationshiptype)
        val thingsthingtothingcrossrefId = (ThingsThingtothingcrossref returning ThingsThingtothingcrossref.map(_.id)) += thingsthingtothingcrossrefRow

        val description = Some("An example SystemUnitofmeasurement")

        val thingssystempropertystaticcrossrefRow = new ThingsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findthingId, findpropertyId, findfieldId, findrecordId, relationshiptype, true)
        val thingssystempropertystaticcrossrefId = (ThingsSystempropertystaticcrossref returning ThingsSystempropertystaticcrossref.map(_.id)) += thingssystempropertystaticcrossrefRow

        val thingssystempropertydynamiccrossrefRow = new ThingsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findthingId, findpropertyId, findfieldId, relationshiptype, true)
        val thingssystempropertydynamiccrossrefId = (ThingsSystempropertydynamiccrossref returning ThingsSystempropertydynamiccrossref.map(_.id))

        ThingsThing += thingsthingRow

        val result = ThingsThing.run
        result must have size (1)
      }
      "allow data to be removed" in {
        ThingsThing.delete
        ThingsThing.run must have size (0)

        ThingsThingtothingcrossref.delete
        ThingsThingtothingcrossref.run must have size (0)

        ThingsSystempropertydynamiccrossref.delete
        ThingsSystempropertydynamiccrossref.run must have size (0)

        ThingsSystempropertystaticcrossref.delete
        ThingsSystempropertystaticcrossref.run must have size (0)

      }
    }
  }
}