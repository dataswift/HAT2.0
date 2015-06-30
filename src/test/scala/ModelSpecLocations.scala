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

  "Locations tables" should {
    db.withSession { implicit session =>
      "be empty" in {
        val result = LocationsLocation.run
        result must have size (0)
      }
      "accept data" in {

        val locationsLocationRow = new LocationsLocationRow(1, LocalDateTime.now(), LocalDateTime.now(), "WMG, University of Warwick")
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
}