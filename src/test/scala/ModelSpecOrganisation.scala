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

  "organisations tables" should {
    db.withSession { implicit session =>
      "be empty" in {
        val result = OrganisationsOrganisation.run
        result must have size (0)
      }
      "accept data" in {

        val organisationsorganisationRow = new OrganisationsOrganisationRow(1, LocalDateTime.now(), LocalDateTime.now(), "WMG")
        val organisationId = (OrganisationsOrganisation returning OrganisationsOrganisation.map(_.id)) += organisationsorganisationRow

        val relationshiptype = Some("Relationship description")
        val findorganisationId = OrganisationsOrganisation.filter(_.name === "WMG").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "Organisation Name").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "name").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head
  
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


}