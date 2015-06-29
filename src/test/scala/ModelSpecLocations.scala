import dal.Tables._
import org.specs2.specification.{AfterAll, BeforeAfterAll}

//import Tables._
//import Tables.profile.simple._
import dal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

class ModelSpecLocations extends Specification with AfterAll {
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
          "events_event",
          "people_person",
          "locations_location",
          "data_field",
          "system_properties",
          "things_thing"
        )

        val tables = db.run(getTables)
        tables must containAllOf[String](requiredTables).await
      }
    }
  }

  "Locations tables" should {
    db.withSession { implicit session =>
      "be empty" in {
        val result = LocationsLocation.run
        result must have size (0)
      }
      "accept data" in {

        val locationsLocationRow = new LocationsLocationRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test")
        val locationId = (LocationsLocation returning LocationsLocation.map(_.id)) += locationsLocationRow

        val relationshiptype = Some("Relationship description")
        val findlocationId = LocationsLocation.filter(_.name === "WMG, University of Warwick").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "cover").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "cover").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "cover").map(_.id).run.head
  

        val locationsLocationtolocationcrossrefRow = new LocationsLocationtolocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findlocationId, findlocationId, relationshiptype)
        val locationsLocationtolocationcrossrefId = (LocationsLocationtolocationcrossref returning LocationsLocationtolocationcrossref.map(_.id)) += locationsLocationtolocationcrossrefRow

        val description = Some("An example SystemUnitofmeasurement")

        val locationssystempropertystaticcrossrefRow = new LocationsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findlocationId, findpropertyId, findfieldId, findrecordId, relationshiptype, true)
        val locationssystempropertystaticcrossrefId = (LocationsSystempropertystaticcrossref returning LocationsSystempropertystaticcrossref.map(_.id)) += locationssystempropertystaticcrossrefRow

        val locationssystempropertydynamiccrossrefRow = new LocationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findlocationId, findpropertyId, findfieldId, relationshiptype, true)
        val locationssystempropertydynamiccrossrefId = (LocationsSystempropertydynamiccrossref returning LocationsSystempropertydynamiccrossref.map(_.id)) += locationssystempropertydynamiccrossrefRow

        LocationsLocation += locationsLocationRow

        val result = LocationsLocation.run
        result must have size (1)
      }
      "allow data to be removed" in {
        LocationsLocation.delete
        LocationsLocation.run must have size (0)

        LocationsLocationtolocationcrossref.delete
        LocationsLocationtolocationcrossref.run must have size (0)

        LocationsSystempropertydynamiccrossref.delete
        LocationsSystempropertydynamiccrossref.run must have size (0)

        LocationsSystempropertystaticcrossref.delete
        LocationsSystempropertystaticcrossref.run must have size (0)

      }
    }
  }

  "Facebook Locations structures" should {
    db.withSession { implicit session =>
      "have Locations created" in {
        val localdatetime = Some(LocalDateTime.now())

        val locationsLocationRows = Seq(
          new LocationsLocationRow(1, LocalDateTime.now(), LocalDateTime.now(), "WMG, University of Warwick")
        )

        LocationsLocation ++= locationsLocationRows

        val result = LocationsLocation.run
        result must have size (1)
      }

      "have Locationssystempropertystaticcrossref created" in {
        val relationshipdescription = Some("Property Cross Reference for a Facebook Cover")

        val findLocationId = LocationsLocation.filter(_.name === "Cover").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "cover").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "cover").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "cover").map(_.id).run.head

        val LocationssystempropertystaticcrossrefRows = Seq(
          new LocationsSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findLocationId, findpropertyId, findfieldId, findrecordId, relationshipdescription, true)
        )

        LocationsSystempropertystaticcrossref ++= LocationssystempropertystaticcrossrefRows

        val result = LocationsSystempropertystaticcrossref.run
        result must have size (1)

        "allow data to be removed" in {
        LocationsLocation.delete
        LocationsLocation.run must have size (0)

        LocationsLocationtolocationcrossref.delete
        LocationsLocationtolocationcrossref.run must have size (0)

        LocationsSystempropertydynamiccrossref.delete
        LocationsSystempropertydynamiccrossref.run must have size (0)

        LocationsSystempropertystaticcrossref.delete
        LocationsSystempropertystaticcrossref.run must have size (0)

        }
      }
    }
  }
}