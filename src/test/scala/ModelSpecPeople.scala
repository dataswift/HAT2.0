import dal.Tables._
//import Tables._
//import Tables.profile.simple._
import autodal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

class ModelSpecPeople extends Specification {
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
        "People_Person",
        "events_event",
        "people_person",
        "People_Person",
        "data_field",
        "system_properties"
      )

      val tables = db.run(getTables)
      tables must containAllOf[String](requiredTables).await
    }
  }

  sequential

  "People tables" should {
    "be empty" in {
      val result = PeoplePerson.run
      result must have size(0)
    }
    "accept data" in {
      
      val PeoplePersonRow = new PeoplePersonRow(1, "Martin", LocalDateTime.now(), LocalDateTime.now(), "Abc-123-def-456")
      val PersonId = (PeoplePerson returning PeoplePerson.map(_.id)) += PeoplePersonRow

      val relationshiptype = Some("Relationship description")

      val PeoplePersontopersoncrossrefRow = new PeoplePersontopersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 1, 1)
      val PeoplePersontopersoncrossrefId = (PeoplePersontopersoncrossref returning PeoplePersontopersoncrossref.map(_.id)) += PeoplePersontopersoncrossrefRow

      val description = Some("An example SystemUnitofmeasurement")

      val PeoplesystempropertystaticcrossrefRow = new PeopleSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, 1, 1, relationshiptype, true)
      val PeoplesystempropertystaticcrossrefId = (PeopleSystempropertystaticcrossref returning PeopleSystempropertystaticcrossref.map(_.id)) += PeoplesystempropertystaticcrossrefRow

      val PeoplesystempropertydynamiccrossrefRow = new PeopleSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, 1, relationshiptype, true)
      val PeoplesystempropertydynamiccrossrefId = (PeopleSystempropertydynamiccrossref returning PeopleSystempropertydynamiccrossref.map(_.id)) +=  PeoplesystempropertydynamiccrossrefRow

      PeoplePerson += PeoplePersonRow

      val result = PeoplePerson.run
      result must have size(1)
    }
    "allow data to be removed" in {
      PeoplePerson.delete
      PeoplePerson.run must have size(0)

      PeoplePersontopersoncrossref.delete
      PeoplePersontopersoncrossref.run must have size(0)

      PeopleSystempropertydynamiccrossref.delete
      PeopleSystempropertydynamiccrossref.run must have size(0)

      PeopleSystempropertystaticcrossref.delete
      PeopleSystempropertystaticcrossref.run must have size(0)

    }
  }

  "Facebook People structures" should {
    
    "have People created" in {
      val localdatetime = Some(LocalDateTime.now())
      
      val PeoplePersonRows = Seq(
        new PeoplePersonRow(1, "Martin", LocalDateTime.now(), LocalDateTime.now(), "Abc-123-def-456-ghj-789")
        )

      PeoplePerson ++= PeoplePersonRows

      val result = PeoplePerson.run
      result must have size(1)
    }

    "have Peoplesystempropertystaticcrossref created" in {
      val relationshipdescription = Some("Property Cross Reference for a Facebook Cover")

      val findPersonId = PeoplePerson.filter(_.name === "Martin").map(_.id).run.head
      val findpropertyId = SystemProperty.filter(_.name === "cover").map(_.id).run.head
      val findfieldId = DataField.filter(_.name === "cover").map(_.id).run.head
      val findrecordId = DataRecord.filter(_.name === "cover").map(_.id).run.head

      val PeoplesystempropertystaticcrossrefRows = Seq(
        new PeopleSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findPersonId, findpropertyId, findfieldId, findrecordId, relationshipdescription, true)
      )
      
      PeopleSystempropertystaticcrossref ++= PeoplesystempropertystaticcrossrefRows

      val result = PeopleSystempropertystaticcrossref.run
      result must have size(1)
    }
  }
}