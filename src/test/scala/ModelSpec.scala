import dal.Tables._
//import Tables._
//import Tables.profile.simple._
import autodal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

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
      result must have size(1)
    }
    "allow data to be removed" in {
      DataValue.delete
      DataValue.run must have size(0)

      DataRecord.delete
      DataRecord.run must have size(0)

      DataField.delete
      DataField.run must have size(0)

      DataTabletotablecrossref.delete
      DataTabletotablecrossref.run must have size(0)

      DataTable.delete
      DataTable.run must have size(0)
    }
  }

  "Facebook data structures" should {
    "have virtual tables created" in {

      val eventsTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "events", false, "facebook")
      val meTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "me", false, "facebook")
      val coverTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "cover", false, "facebook")
      val ownerTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "owner", false, "facebook")
      val placeTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "place", false, "facebook")
      val locationTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "location", false, "facebook")


      val eventsId = (DataTable returning DataTable.map(_.id)) += eventsTable
      val meId = (DataTable returning DataTable.map(_.id)) += meTable
      val coverId = (DataTable returning DataTable.map(_.id)) += coverTable
      val ownerId = (DataTable returning DataTable.map(_.id)) += ownerTable
      val placeId = (DataTable returning DataTable.map(_.id)) += placeTable
      val locationId = (DataTable returning DataTable.map(_.id)) += locationTable

      val dataTableToTableCrossRefRows = Seq(
        new DataTabletotablecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", eventsId, coverId),
        new DataTabletotablecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", eventsId, ownerId),
        new DataTabletotablecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", eventsId, placeId),
        new DataTabletotablecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", placeId, locationId)
      )

      DataTabletotablecrossref ++= dataTableToTableCrossRefRows

      val result = DataTable.filter(_.sourceName.startsWith("facebook")).run
      result must have size(6)
    }

    "have fields created and linked to the right tables" in {
      val findTableId = DataTable.filter(_.sourceName === "facebook")

      val eventsId = findTableId.filter(_.name === "events").map(_.id).run.head
      val coverId = findTableId.filter(_.name === "cover").map(_.id).run.head
      val ownerId = findTableId.filter(_.name === "owner").map(_.id).run.head
      val placeId = findTableId.filter(_.name === "place").map(_.id).run.head
      val locationId = findTableId.filter(_.name === "location").map(_.id).run.head

      val dataFieldRows = Seq(
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "attending_count"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), coverId, "cover"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "declined_count"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "description"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "end_time"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "feed_targeting"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "id"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "invited_count"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "is_date_only"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "maybe_count"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "name"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "noreply_count"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), ownerId, "owner"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "parent_group"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), placeId, "place"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "privacy"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "rvsp_status"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "start_time"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "end_time"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "ticket_url"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "timezone"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), eventsId, "updated_time"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), coverId, "cover_id"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), coverId, "offset_x"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), coverId, "offset_y"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), coverId, "source"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), coverId, "id"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), ownerId, "id"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), ownerId, "name"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), placeId, "name"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "location"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "city"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "country"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "longitude"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "latitude"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "street"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, "zip"),
        new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), placeId, "id")
      )
      DataField ++= dataFieldRows

      val result = DataField.run
      result must have size(38)
    }


    "have fields created and linked to the right tables" in {
      val dataRecordRows = Seq(
        new DataRecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent1"),
        new DataRecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent2")
        )
      DataRecord ++= dataRecordRows
      
      val result = DataRecord.run
      result must have size(2)
      }

      "have fields created and linked to the right tables" in {
      val findFacebookTableId = DataTable.filter(_.sourceName === "facebook")
      val findCoverTableId = DataTable.filter(_.sourceName === "cover")

      val recordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head
      val attending_countId = DataField.filter(_.name === "attending_count").map(_.id).run.head
      val coverId = DataField.filter(_.name === "cover").map(_.id).run.head
      val coveridId = DataField.filter(_.name === "cover_id").map(_.id).run.head
      val offsetxId = DataField.filter(_.name === "offset_x").map(_.id).run.head
      val offsetyId = DataField.filter(_.name === "offset_y").map(_.id).run.head
      val sourceId = DataField.filter(_.name === "source").map(_.id).run.head
      val idId = DataField.filter(_.name === "id").map(_.id).run.head
      val declinedcountId = DataField.filter(_.name === "declined_count").map(_.id).run.head
      val descriptionId = DataField.filter(_.name === "description").map(_.id).run.head
      
    val dataRows = Seq(
        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "2", attending_countId, recordId),
        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "", coverId, recordId),
        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "812728954390780", coveridId, recordId),
        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "0", offsetxId, recordId),
        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "84", offsetyId, recordId),
        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "http://link.com", sourceId, recordId),
        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "812728954390780", idId, recordId),
        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "1", declinedcountId, recordId),
        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "Test event for HAT", descriptionId, recordId)
      )
      DataValue ++= dataRows

      val result = DataValue.run
      result must have size(9)
    }
    
    "auto-increment record rows" in {
      val dataRecordRow = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent1")
      val recordId = (DataRecord returning DataRecord.map(_.id)) += dataRecordRow

      val dataRecordRow2 = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent2")
      val recordId2 = (DataRecord returning DataRecord.map(_.id)) += dataRecordRow2
      recordId2 must beEqualTo(recordId + 1)
    }
  }
}