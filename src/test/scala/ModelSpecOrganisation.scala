import dal.Tables._
import org.specs2.specification.{AfterAll, BeforeAfterAll}

//import Tables._
//import Tables.profile.simple._
import dal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

class ModelSpecOrganisations extends Specification with AfterAll {
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
          "organisations_organisation",
          "data_field",
          "system_properties"
        )

        val tables = db.run(getTables)
        tables must containAllOf[String](requiredTables).await
      }
    }
  }

  "organisations tables" should {
    db.withSession { implicit session =>
      "be empty" in {
        val result = OrganisationsOrganisation.run
        result must have size (0)
      }
      "accept data" in {

        val organisationsorganisationRow = new OrganisationsOrganisationRow(1, "WMG", LocalDateTime.now(), LocalDateTime.now())
        val organisationId = (OrganisationsOrganisation returning OrganisationsOrganisation.map(_.id)) += organisationsorganisationRow

        val relationshiptype = Some("Relationship description")
        val findorganisationId = OrganisationsOrganisation.filter(_.name === "WMG, University of Warwick").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "cover").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "cover").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "cover").map(_.id).run.head
  
        val organisationorganisationtoorganisationcrossrefRow = new OrganisationOrganisationtoorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findorganisationId, findorganisationId, "relationshiptype", true)
        val organisationorganisationtoorganisationcrossrefId = (OrganisationOrganisationtoorganisationcrossref returning OrganisationOrganisationtoorganisationcrossref.map(_.id)) += organisationorganisationtoorganisationcrossrefRow

        val description = Some("An example SystemUnitofmeasurement")

        val organisationssystempropertystaticcrossrefRow = new OrganisationsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findorganisationId, findpropertyId, findfieldId, findrecordId, relationshiptype, true)
        val organisationssystempropertystaticcrossrefId = (OrganisationsSystempropertystaticcrossref returning OrganisationsSystempropertystaticcrossref.map(_.id)) += organisationssystempropertystaticcrossrefRow

        val organisationssystempropertydynamiccrossrefRow = new OrganisationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findorganisationId, findpropertyId, findfieldId, relationshiptype, true)
        val organisationssystempropertydynamiccrossrefId = (OrganisationsSystempropertydynamiccrossref returning OrganisationsSystempropertydynamiccrossref.map(_.id)) += organisationssystempropertydynamiccrossrefRow

        OrganisationsOrganisation += organisationsorganisationRow

        val result = OrganisationsOrganisation.run
        result must have size (1)
      }
      "allow data to be removed" in {
        OrganisationsOrganisation.delete
        OrganisationsOrganisation.run must have size (0)

        OrganisationOrganisationtoorganisationcrossref.delete
        OrganisationOrganisationtoorganisationcrossref.run must have size (0)

        OrganisationsSystempropertydynamiccrossref.delete
        OrganisationsSystempropertydynamiccrossref.run must have size (0)

        OrganisationsSystempropertystaticcrossref.delete
        OrganisationsSystempropertystaticcrossref.run must have size (0)

      }
    }
  }

  "Facebook organisations structures" should {
    db.withSession { implicit session =>
      "have organisations created" in {
        val localdatetime = Some(LocalDateTime.now())

        val organisationsorganisationRows = Seq(
          new OrganisationsOrganisationRow(1, "WMG, University of Warwick", LocalDateTime.now(), LocalDateTime.now())
        )

        OrganisationsOrganisation ++= organisationsorganisationRows

        val result = OrganisationsOrganisation.run
        result must have size (1)
      }

      "have organisationssystempropertystaticcrossref created" in {
        val relationshipdescription = Some("Property Cross Reference for a Facebook Cover")

        val findorganisationId = OrganisationsOrganisation.filter(_.name === "Cover").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "cover").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "cover").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "cover").map(_.id).run.head

        val organisationssystempropertystaticcrossrefRows = Seq(
          new OrganisationsSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findorganisationId, findpropertyId, findfieldId, findrecordId, relationshipdescription, true)
        )

        OrganisationsSystempropertystaticcrossref ++= organisationssystempropertystaticcrossrefRows

        val result = OrganisationsSystempropertystaticcrossref.run
        result must have size (1)

        "allow data to be removed" in {
        OrganisationsOrganisation.delete
        OrganisationsOrganisation.run must have size (0)

        OrganisationOrganisationtoorganisationcrossref.delete
        OrganisationOrganisationtoorganisationcrossref.run must have size (0)

        OrganisationsSystempropertydynamiccrossref.delete
        OrganisationsSystempropertydynamiccrossref.run must have size (0)

        OrganisationsSystempropertystaticcrossref.delete
        OrganisationsSystempropertystaticcrossref.run must have size (0)

        }
      }
    }
  }
}