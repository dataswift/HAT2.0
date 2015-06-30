import dal.Tables._
import org.specs2.specification.{AfterAll, BeforeAfterAll}

//import Tables._
//import Tables.profile.simple._
import dal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

class ModelSpecProperties extends Specification with AfterAll {
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
          "locations_location"
        )

        val tables = db.run(getTables)
        tables must containAllOf[String](requiredTables).await
      
    }
  }

  "System tables" should {
    db.withSession { implicit session =>
      "be empty" in {
        val result = SystemProperty.run
        result must have size (0)
      }
    }
      "accept data" in {
        db.withSession { implicit session =>

        val systemTypeRow = new SystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test1", "Test2")
        val typeId = (SystemType returning SystemType.map(_.id)) += systemTypeRow

        val relationshipdescription = Some("Relationship description")

        val systemtypetotypecrossrefRow = new SystemTypetotypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), typeId, typeId, relationshipdescription)
        val typetotypecrossrefId = (SystemTypetotypecrossref returning SystemTypetotypecrossref.map(_.id)) += systemtypetotypecrossrefRow
      

        val symbol = Some("Example")
        val description = Some("An example SystemUnitofmeasurement")

        val systemUnitofmeasurementRow = new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "Example", description, symbol)
        val unitofmeasurementId = (SystemUnitofmeasurement returning SystemUnitofmeasurement.map(_.id)) += systemUnitofmeasurementRow

        val systemPropertyRow = new SystemPropertyRow(1, LocalDateTime.now(), LocalDateTime.now(), "test1", "test2")
        SystemProperty += systemPropertyRow
        val propertyId = (SystemProperty returning SystemProperty.map(_.id))


        val result = SystemProperty.run
        result must have size (1)
      }
    }
      "allow data to be removed" in {
        db.withSession { implicit session =>

        SystemTypetotypecrossref.delete
        SystemTypetotypecrossref.run must have size (0)

        SystemProperty.delete
        SystemProperty.run must have size (0)

        SystemType.delete
        SystemType.run must have size (0)

        SystemUnitofmeasurement.delete
        SystemUnitofmeasurement.run must have size (0)

      }
    }
  }

  "Facebook system structures" should {
    db.withSession { implicit session =>
      "have unitofmeasurement created" in {
        
        val symbol = Some("Example")
        val description = Some("An example SystemUnitofmeasurement")

        val systemUnitofmeasurementRows = Seq(
          new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "Example", description, symbol)
        )

        SystemUnitofmeasurement ++= systemUnitofmeasurementRows

        val result = SystemUnitofmeasurement.run
        result must have size (1)
      }

      "have properties created" in {

        val unitofmeasurementId = SystemUnitofmeasurement.filter(_.name === "Example")

        val attendingcount = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "AttendingCount", "Number of people attending an event")
        val cover = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Cover", "A facebook cover image")
        val timezone = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Timezone", "A timezone")
        val placename = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Place Name", "A facebook Place Name")

       

        val systemPropertyRows = Seq(
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "attendingcount", "test"),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "cover", "test"),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "timezone", "test"),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "placename", "test"),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "longitude", "test"),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "latitude", "test"),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "postcode", "test"),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Country", "test"),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Organisation Name", "test"),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Owner", "test")
        )

        SystemProperty ++= systemPropertyRows

        val result = SystemProperty.run
        result must have size (10)
      }

    "allow for tables to be cleaned up" in {

        SystemTypetotypecrossref.delete
        SystemTypetotypecrossref.run must have size (0)

        SystemProperty.delete
        SystemProperty.run must have size (0)

        SystemType.delete
        SystemType.run must have size (0)

        SystemUnitofmeasurement.delete
        SystemUnitofmeasurement.run must have size (0)

      } 
    }
  }
  def afterAll() = {
    db.close
  }

}