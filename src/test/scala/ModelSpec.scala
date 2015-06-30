import dal.Tables._
import org.specs2.specification.{AfterAll, BeforeAfterAll}

//import Tables._
//import Tables.profile.simple._
import dal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

class ModelSpec extends Specification with AfterAll {
  val db = Database.forConfig("devdb")

  sequential

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
          "locations_location",
          "organisations_organisation",
          "data_field",
          "system_properties"
        )

        val tables = db.run(getTables)
        tables must containAllOf[String](requiredTables).await
    }
  }

  "Data tables" should {
    "be empty" in {
// FIXME: attempt to use the new slick API
//      val allValues = for (value <- DataValue) yield value.id
//      val values = db.run(DataValue.result)
//      values must haveSize[Seq[_]](0).await
      db.withSession { implicit session =>
        val result = DataValue.run
        result must have size (0)
      }
    }
    "accept data" in {
      db.withSession { implicit session =>
        val dataTableRow = new DataTableRow(1, LocalDateTime.now(), LocalDateTime.now(), "test", false, "test")
        val tableId = (DataTable returning DataTable.map(_.id)) += dataTableRow

        val dataFieldRow = new DataFieldRow(1, LocalDateTime.now(), LocalDateTime.now(), tableId, "Test")
        val fieldId = (DataField returning DataField.map(_.id)) += dataFieldRow

        val dataRecordRow = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test")
        val recordId = (DataRecord returning DataRecord.map(_.id)) += dataRecordRow

        val dataTableToTableCrossRefRow = new DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", tableId, tableId)
        val tableToTablecrossrefId = (DataTabletotablecrossref returning DataTabletotablecrossref.map(_.id)) += dataTableToTableCrossRefRow

        // The ID value is actually ignored and auto-incremented
        val dataRow = new DataValueRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test", fieldId, recordId)
        DataValue += dataRow

        val result = DataValue.run
        result must have size (1)
      }
    }

    "auto-increment record rows" in {
      db.withSession { implicit session =>
        val dataRecordRow = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "record 1")
        val recordId = (DataRecord returning DataRecord.map(_.id)) += dataRecordRow

        val dataRecordRow2 = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "record 2")
        val recordId2 = (DataRecord returning DataRecord.map(_.id)) += dataRecordRow2
        recordId2 must beEqualTo(recordId + 1)

        val result = DataRecord.run
        result must have size (2)
      }
    }

    "allow data to be removed" in {
      db.withSession { implicit session =>
        DataValue.delete
        DataRecord.delete
        DataField.delete
        DataTabletotablecrossref.delete
        DataTable.delete

        DataValue.run must have size (0)
        DataRecord.run must have size (0)
        DataField.run must have size (0)
        DataTabletotablecrossref.run must have size (0)
        DataTable.run must have size (0)
      }
    }
  }

  def afterAll() = {
    db.close
  }

}