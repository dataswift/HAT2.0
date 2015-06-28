//import dal.Tables._
//import org.specs2.specification.{AfterAll, BeforeAfterAll}
//
////import Tables._
////import Tables.profile.simple._
//import dal.SlickPostgresDriver.simple._
//import org.joda.time.LocalDateTime
//import org.specs2.mutable.Specification
//import slick.jdbc.meta.MTable
//
//import scala.concurrent.ExecutionContext.Implicits.global
//
//class ModelSpecProperties extends Specification with AfterAll {
//  val db = Database.forConfig("devdb")
//
//  sequential
//
//  def afterAll = {
//    db.close()
//  }
//
//  "Core Tables" should {
//    db.withSession { implicit session =>
//      "be created" in {
//
//        val getTables = MTable.getTables(None, Some("public"), None, None).map { ts =>
//          ts.map { t =>
//            t.name.name
//          }
//        }
//
//        val requiredTables: Seq[String] = Seq(
//          "data_table",
//          "things_thing",
//          "events_event",
//          "people_person",
//          "locations_location"
//        )
//
//        val tables = db.run(getTables)
//        tables must containAllOf[String](requiredTables).await
//      }
//    }
//  }
//
//  "System tables" should {
//    db.withSession { implicit session =>
//      "be empty" in {
//        val result = SystemProperty.run
//        result must have size (0)
//      }
//      "accept data" in {
//
//        val systemTypeRow = new SystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test1", "Test2")
//        val typeId = (SystemType returning SystemType.map(_.id)) += systemTypeRow
//
//        val relationshipdescription = Some("Relationship description")
//
//        val systemtypetotypecrossrefRow = new SystemTypetotypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), typeId, typeId, relationshipdescription)
//        val typetotypecrossrefId = (SystemTypetotypecrossref returning SystemTypetotypecrossref.map(_.id)) += systemtypetotypecrossrefRow
//
//        val symbol = Some("Example")
//        val description = Some("An example SystemUnitofmeasurement")
//
//        val systemUnitofmeasurementRow = new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "Example", description, symbol)
//        val unitofmeasurementId = (SystemUnitofmeasurement returning SystemUnitofmeasurement.map(_.id)) += systemUnitofmeasurementRow
//
//        val systemPropertyRow = new SystemPropertyRow(1, LocalDateTime.now(), LocalDateTime.now(), "test", "test")
//        val propertyId = (SystemProperty returning SystemProperty.map(_.id)) += systemPropertyRow
//
//        SystemProperty += systemPropertyRow
//
//        val result = SystemProperty.run
//        result must have size (1)
//      }
//      "allow data to be removed" in {
//        SystemProperty.delete
//        SystemProperty.run must have size (0)
//
//        SystemType.delete
//        SystemType.run must have size (0)
//
//        SystemTypetotypecrossref.delete
//        SystemTypetotypecrossref.run must have size (0)
//
//        SystemUnitofmeasurement.delete
//        SystemUnitofmeasurement.run must have size (0)
//
//      }
//    }
//  }
//
//  "Facebook system structures" should {
//    db.withSession { implicit session =>
//      "have fields created and linked to the right tables" in {
//        val symbol = Some("Example")
//        val description = Some("An example SystemUnitofmeasurement")
//
//        val systemUnitofmeasurementRows = Seq(
//          new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "Example", description, symbol)
//        )
//
//        SystemUnitofmeasurement ++= systemUnitofmeasurementRows
//
//        val result = SystemUnitofmeasurement.run
//        result must have size (1)
//      }
//
//      "have virtual tables created" in {
//
//        val unitofmeasurementId = SystemUnitofmeasurement.filter(_.name === "Example")
//
//        val attendingcount = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "AttendingCount", "Number of people attending an event")
//        val cover = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Cover", "A facebook cover image")
//        val timezone = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Timezone", "A timezone")
//        val placename = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Place Name", "A facebook Place Name")
//
//        val AttendingCountId = (SystemProperty returning SystemProperty.map(_.id)) += attendingcount
//        val CoverId = (SystemProperty returning SystemProperty.map(_.id)) += cover
//        val TimezoneId = (SystemProperty returning SystemProperty.map(_.id)) += timezone
//        val PlaceNameId = (SystemProperty returning SystemProperty.map(_.id)) += placename
//
//        val systemPropertyRows = Seq(
//          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "attendingcount", "test"),
//          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "cover", "test"),
//          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "timezone", "test"),
//          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "placename", "test")
//        )
//
//        SystemProperty ++= systemPropertyRows
//
//        val result = SystemProperty.run
//        result must have size (4)
//      }
//
//      /* "have fields created and linked to the right tables" in {
//      val findFacebookTableId = DataTable.filter(_.sourceName === "facebook")
//      val findCoverTableId = DataTable.filter(_.sourceName === "cover")
//      val recordId = DataRecord.filter(_.name === "FacebookEvent_1").map(_.id).run.head
//
//      val fieldId = DataField.filter(_.name === "attending_count").map(_.id).run.head
//      val fieldId = DataField.filter(_.name === "cover").map(_.id).run.head
//      val fieldId = DataField.filter(_.name === "cover_id").map(_.id).run.head
//      val fieldId = DataField.filter(_.name === "offset_x").map(_.id).run.head
//      val fieldId = DataField.filter(_.name === "offset_y").map(_.id).run.head
//      val fieldId = DataField.filter(_.name === "source").map(_.id).run.head
//      val fieldId = DataField.filter(_.name === "id").map(_.id).run.head
//      val fieldId = DataField.filter(_.name === "declined_count").map(_.id).run.head
//      val fieldId = DataField.filter(_.name === "description").map(_.id).run.head
//
//    val dataRows = Seq(
//        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "2", fieldId, recordId),
//        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "", fieldId, recordId),
//        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "812728954390780", fieldId, recordId),
//        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "0", fieldId, recordId),
//        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "84", fieldId, recordId),
//        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "http://link.com", fieldId, recordId),
//        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "812728954390780", fieldId, recordId),
//        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "1", fieldId, recordId),
//        new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "Test event for HAT", fieldId, recordId)
//      )
//      DataValue ++= dataRows
//
//      val result = DataValue.run
//      result must have size(9)
//    }
//  */
//
//    }
//  }
//}