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


  def afterAll() = {
    db.close
  }

}