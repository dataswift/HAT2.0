import dal.Tables._
import org.specs2.specification.{AfterAll, BeforeAfterAll}

//import Tables._
//import Tables.profile.simple._
import dal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

class ModelSpecPeople extends Specification with AfterAll {
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
          "events_event",
          "people_person",
          "data_field",
          "system_properties"
        )

        val tables = db.run(getTables)
        tables must containAllOf[String](requiredTables).await
      }
    }
  }

  "People tables" should {
    db.withSession { implicit session =>
      "be empty" in {
        val result = PeoplePerson.run
        result must have size (0)
      }
      "accept data" in {

        val PeoplePersonRow = new PeoplePersonRow(1, "Martin", LocalDateTime.now(), LocalDateTime.now(), "Abc-123-def-456")
        val PersonId = (PeoplePerson returning PeoplePerson.map(_.id))

        val relationshiptype = Some("Relationship description")
        val findpeopleId = PeoplePerson.filter(_.name === "Martin").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "cover").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "cover").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "cover").map(_.id).run.head


        val peoplePersontopersonrelationshiptypeRow = new PeoplePersontopersonrelationshiptypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "Martin's Martin", relationshiptype)
        val peoplePersontopersonrelationshiptypeId = (PeoplePersontopersonrelationshiptype returning PeoplePersontopersonrelationshiptype.map(_.id)) += peoplePersontopersonrelationshiptypeRow

        val peoplePersontopersoncrossrefRow = new PeoplePersontopersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findpeopleId, findpeopleId, peoplePersontopersonrelationshiptypeId)
        val peoplePersontopersoncrossrefId = (PeoplePersontopersoncrossref returning PeoplePersontopersoncrossref.map(_.id)) += peoplePersontopersoncrossrefRow

        val description = Some("An example SystemUnitofmeasurement")

        val peoplesystempropertystaticcrossrefRow = new PeopleSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findpeopleId, findpropertyId, findfieldId, findrecordId, relationshiptype, true)
        val peoplesystempropertystaticcrossrefId = (PeopleSystempropertystaticcrossref returning PeopleSystempropertystaticcrossref.map(_.id)) += peoplesystempropertystaticcrossrefRow

        val peoplesystempropertydynamiccrossrefRow = new PeopleSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findpeopleId, findpropertyId, findfieldId, relationshiptype, true)
        val peoplesystempropertydynamiccrossrefId = (PeopleSystempropertydynamiccrossref returning PeopleSystempropertydynamiccrossref.map(_.id)) += peoplesystempropertydynamiccrossrefRow

        PeoplePerson += PeoplePersonRow

        val result = PeoplePerson.run
        result must have size (1)
      }
      "allow data to be removed" in {
        PeoplePerson.delete
        PeoplePerson.run must have size (0)

        PeoplePersontopersonrelationshiptype.delete
        PeoplePersontopersonrelationshiptype.run must have size (0)

        PeoplePersontopersoncrossref.delete
        PeoplePersontopersoncrossref.run must have size (0)

        PeopleSystempropertydynamiccrossref.delete
        PeopleSystempropertydynamiccrossref.run must have size (0)

        PeopleSystempropertystaticcrossref.delete
        PeopleSystempropertystaticcrossref.run must have size (0)

      }
    }
  }

  "Facebook People structures" should {
    db.withSession { implicit session =>
      "have People created" in {
        val localdatetime = Some(LocalDateTime.now())

        val PeoplePersonRows = Seq(
          new PeoplePersonRow(1, "Martin", LocalDateTime.now(), LocalDateTime.now(), "Abc-123-def-456-ghj-789")
        )

        PeoplePerson ++= PeoplePersonRows

        val result = PeoplePerson.run
        result must have size (1)
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
        result must have size (1)

        "allow for tables to be cleaned up" in {
        PeoplePerson.delete
        PeoplePerson.run must have size (0)

        PeoplePersontopersonrelationshiptype.delete
        PeoplePersontopersonrelationshiptype.run must have size (0)

        PeoplePersontopersoncrossref.delete
        PeoplePersontopersoncrossref.run must have size (0)

        PeopleSystempropertydynamiccrossref.delete
        PeopleSystempropertydynamiccrossref.run must have size (0)

        PeopleSystempropertystaticcrossref.delete
        PeopleSystempropertystaticcrossref.run must have size (0)
        }
      }
    }
  }
}