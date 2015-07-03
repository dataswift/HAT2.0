import dal.Tables._
import org.specs2.specification.{AfterAll, BeforeAfterAll}

//import Tables._
//import Tables.profile.simple._

import dal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

class FacebookExampleSpec extends Specification with AfterAll {
  val db = Database.forConfig("devdb")

  sequential

  def afterAll() = {
    db.close
  }

  "Facebook data structures" should {
    db.withSession { implicit session =>
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
        result must have size (6)
      }

      "have fields created and linked to the right tables" in {
        val findTableId = DataTable.filter(_.sourceName === "facebook")

        val eventsId = findTableId.filter(_.name === "events").map(_.id).run.head
        val coverId = findTableId.filter(_.name === "cover").map(_.id).run.head
        val ownerId = findTableId.filter(_.name === "owner").map(_.id).run.head
        val placeId = findTableId.filter(_.name === "place").map(_.id).run.head
        val locationId = findTableId.filter(_.name === "location").map(_.id).run.head

        val dataFieldRows = Seq(
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "attending_count", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "cover", coverId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "declined_count", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "description", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "end_time", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "feed_targeting", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "id", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "invited_count", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "is_date_only", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "maybe_count", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "name", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "noreply_count", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "owner", ownerId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "parent_group", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "place", placeId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "privacy", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "rvsp_status", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "start_time", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "end_time", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "ticket_url", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "timezone", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "updated_time", eventsId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "cover_id", coverId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "offset_x", coverId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "offset_y", coverId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "source", coverId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "id", coverId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "id", ownerId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "name", ownerId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "name", placeId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "location", locationId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "city", locationId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "country", locationId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "longitude", locationId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "latitude", locationId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "street", locationId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "zip", locationId),
          new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), "id", placeId)
        )
        DataField ++= dataFieldRows

        val result = DataField.run
        result must have size (38)
      }

      "accept new records" in {
        val dataRecordRow = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent1")
        val recordId = (DataRecord returning DataRecord.map(_.id)) += dataRecordRow

        val dataRecordRow2 = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent2")
        val recordId2 = (DataRecord returning DataRecord.map(_.id)) += dataRecordRow2
        recordId2 must beEqualTo(recordId + 1)

        val result = DataRecord.run
        result must have size (2)
      }

      "accept new values linked to the right fields and records" in {
        val findTableId = DataTable.filter(_.sourceName === "facebook")
        val findFacebookTableId = DataTable.filter(_.sourceName === "facebook")
        val findCoverTableId = DataTable.filter(_.sourceName === "cover")

        val dataRecords = DataRecord
        val recordId = dataRecords.filter(_.name === "FacebookEvent1").map(_.id).run.head
        //      val recordId = db.run(DataRecord.filter(_.name === "FacebookEvent1").map(_.id).result).head

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
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "Test event for HAT", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "2015-06-03T18:50:00+0100", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "1054634501233012", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "4", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "FALSE", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "1", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "Test event", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "0", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "Martin Talbot", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "WMG, University of Warwick", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "Coventry", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "United Kingdom", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "52.383122197503", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "52.383122197503", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "International Manufacturing Centre, Gibbet Hill Road, University of Warwick", descriptionId, recordId),
          new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "CV4 7AL", descriptionId, recordId)
        )
        DataValue ++= dataRows

        val result = DataValue.run
        result must have size (28)
      }
    }
  }

  "Facebook system structures" should {
    db.withSession { implicit session =>
      "have unitofmeasurement created" in {

        val symbol = Some("Example")
        val description = Some("An example SystemUnitofmeasurement")

        val systemUnitofmeasurementRows = Seq(
          new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "Example", description, symbol),
          new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "Timezone", description, symbol),
          new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "GPS Co-ordinate", description, symbol)
        )

        SystemUnitofmeasurement ++= systemUnitofmeasurementRows

        val result = SystemUnitofmeasurement.run
        result must have size (3)
      }

      "have properties created" in {

        //        val unitofmeasurementId = SystemUnitofmeasurement.filter(_.name === "Example").map(_.id).run.head
        //
        //        val attendingcount = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "AttendingCount", "Number of people attending an event")
        //        val cover = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Cover", "A facebook cover image")
        //        val timezone = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Timezone", "A timezone")
        //        val placename = new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Place Name", "A facebook Place Name")


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

    }
  }

  "Facebook Locations structures" should {
    db.withSession { implicit session =>
      "have Locations created" in {
        val localdatetime = Some(LocalDateTime.now())

        val locationsLocationRows = Seq(
          new LocationsLocationRow(1, LocalDateTime.now(), LocalDateTime.now(), "WMG Warwick University")
        )

        LocationsLocation ++= locationsLocationRows

        val result = LocationsLocation.run
        result must have size (1)
      }

      "have Locationssystempropertystaticcrossref created" in {
        val relationshipdescription = Some("Property Cross Reference for a Facebook location")

        val findLocationId = LocationsLocation.filter(_.name === "WMG Warwick University").map(_.id).run.head
        val findlatpropertyId = SystemProperty.filter(_.name === "latitude").map(_.id).run.head
        val findlongpropertyId = SystemProperty.filter(_.name === "longitude").map(_.id).run.head
        val findlatfieldId = DataField.filter(_.name === "latitude").map(_.id).run.head
        val findlongfieldId = DataField.filter(_.name === "longitude").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head

        val LocationssystempropertystaticcrossrefRows = Seq(
          new LocationsSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findLocationId, findlatpropertyId, findrecordId, findlatfieldId, relationshipdescription, true),
          new LocationsSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findLocationId, findlongpropertyId, findrecordId, findlongfieldId, relationshipdescription, true)

        )

        LocationsSystempropertystaticcrossref ++= LocationssystempropertystaticcrossrefRows

        val result = LocationsSystempropertystaticcrossref.run
        result must have size (2)
      }
    }
  }

  "Facebook organisations structures" should {
    db.withSession { implicit session =>
      "have organisations created" in {
        val localdatetime = Some(LocalDateTime.now())

        val organisationsorganisationRows = Seq(
          new OrganisationsOrganisationRow(1, LocalDateTime.now(), LocalDateTime.now(), "WMG, University of Warwick")
        )

        OrganisationsOrganisation ++= organisationsorganisationRows

        val result = OrganisationsOrganisation.run
        result must have size (1)
      }

      "have organisationssystempropertystaticcrossref created" in {
        val relationshipdescription = Some("Property Cross Reference for a Facebook Cover")

        val findorganisationId = OrganisationsOrganisation.filter(_.name === "WMG, University of Warwick").map(_.id).run.head
        val findlocationpropertyId = SystemProperty.filter(_.name === "Organisation Name").map(_.id).run.head
        val findlocationfieldId = DataField.filter(_.name === "location").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head

        val organisationssystempropertystaticcrossrefRows = Seq(
          new OrganisationsSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findorganisationId, findlocationpropertyId, findrecordId, findlocationfieldId, relationshipdescription, true)
        )

        OrganisationsSystempropertystaticcrossref ++= organisationssystempropertystaticcrossrefRows

        val result = OrganisationsSystempropertystaticcrossref.run
        result must have size (1)

      }
    }
  }

  "Facebook events structures" should {
    db.withSession { implicit session =>
      "have events created" in {
        val localdatetime = Some(LocalDateTime.now())

        val eventseventRows = Seq(
          new EventsEventRow(1, LocalDateTime.now(), LocalDateTime.now(), "WMG Event")
        )

        EventsEvent ++= eventseventRows

        val result = EventsEvent.run
        result must have size (1)
      }

      "have eventssystempropertystaticcrossref created" in {
        val relationshipdescription = Some("Property Cross Reference for a Facebook Cover")

        val findeventId = EventsEvent.filter(_.name === "WMG Event").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "timezone").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "timezone").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head

        val eventssystempropertystaticcrossrefRows = Seq(
          new EventsSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findeventId, findpropertyId, findrecordId, findfieldId, relationshipdescription, true)
        )

        EventsSystempropertystaticcrossref ++= eventssystempropertystaticcrossrefRows

        val result = EventsSystempropertystaticcrossref.run
        result must have size (1)
      }
    }
  }

  "Facebook People structures" should {
    db.withSession { implicit session =>
      "have People created" in {
        val localdatetime = Some(LocalDateTime.now())

        val PeoplePersonRows = Seq(
          new PeoplePersonRow(1, LocalDateTime.now(), LocalDateTime.now(), "Martin", "Abc-123-def-456-ghj-789")
        )

        PeoplePerson ++= PeoplePersonRows

        val result = PeoplePerson.run
        result must have size (1)
      }

      "have Peoplesystempropertystaticcrossref created" in {
        val relationshipdescription = Some("Property Cross Reference for a Facebook Cover")

        val findPersonId = PeoplePerson.filter(_.name === "Martin").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "Owner").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "owner").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head

        val PeoplesystempropertystaticcrossrefRows = Seq(
          new PeopleSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findPersonId, findpropertyId, findrecordId, findfieldId, relationshipdescription, true)
        )

        PeopleSystempropertystaticcrossref ++= PeoplesystempropertystaticcrossrefRows

        val result = PeopleSystempropertystaticcrossref.run
        result must have size (1)

      }
    }
  }

  "Facebook things structures" should {
    db.withSession { implicit session =>
      "have things created" in {
        val localdatetime = Some(LocalDateTime.now())

        val thingsthingRows = Seq(
          new ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "Cover")
        )

        ThingsThing ++= thingsthingRows

        val result = ThingsThing.run
        result must have size (1)
      }

      "have thingssystempropertystaticcrossref created" in {
        val relationshipdescription = Some("Property Cross Reference for a Facebook Cover")

        val findthingId = ThingsThing.filter(_.name === "Cover").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "cover").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "cover").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head

        val thingssystempropertystaticcrossrefRows = Seq(
          new ThingsSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findthingId, findpropertyId, findfieldId, findrecordId, relationshipdescription, true)
        )

        ThingsSystempropertystaticcrossref ++= thingssystempropertystaticcrossrefRows

        val result = ThingsSystempropertystaticcrossref.run
        result must have size (1)
      }
    }
  }

  "Facebook structures" should {
    db.withSession { implicit session =>

      "allow proeprty crossrefs to be removed" in {
        LocationsSystempropertydynamiccrossref.delete
        LocationsSystempropertydynamiccrossref.run must have size (0)

        LocationsSystempropertystaticcrossref.delete
        LocationsSystempropertystaticcrossref.run must have size (0)

        OrganisationsSystempropertydynamiccrossref.delete
        OrganisationsSystempropertydynamiccrossref.run must have size (0)

        OrganisationsSystempropertystaticcrossref.delete
        OrganisationsSystempropertystaticcrossref.run must have size (0)

        EventsSystempropertydynamiccrossref.delete
        EventsSystempropertydynamiccrossref.run must have size (0)

        EventsSystempropertystaticcrossref.delete
        EventsSystempropertystaticcrossref.run must have size (0)

        PeopleSystempropertydynamiccrossref.delete
        PeopleSystempropertydynamiccrossref.run must have size (0)

        PeopleSystempropertystaticcrossref.delete
        PeopleSystempropertystaticcrossref.run must have size (0)

        ThingsSystempropertydynamiccrossref.delete
        ThingsSystempropertydynamiccrossref.run must have size (0)

        ThingsSystempropertystaticcrossref.delete
        ThingsSystempropertystaticcrossref.run must have size (0)
      }

      "allow Property tables to be cleaned up" in {

        SystemTypetotypecrossref.delete
        SystemTypetotypecrossref.run must have size (0)

        SystemProperty.delete
        SystemProperty.run must have size (0)

        SystemType.delete
        SystemType.run must have size (0)

        SystemUnitofmeasurement.delete
        SystemUnitofmeasurement.run must have size (0)

      }

      "allow location data to be cleaned up" in {

        LocationsLocationtolocationcrossref.delete
        LocationsLocationtolocationcrossref.run must have size (0)

        LocationsLocation.delete
        LocationsLocation.run must have size (0)

      }

      "allow organisation data to be removed" in {


        OrganisationOrganisationtoorganisationcrossref.delete
        OrganisationOrganisationtoorganisationcrossref.run must have size (0)

        OrganisationsOrganisation.delete
        OrganisationsOrganisation.run must have size (0)

      }

      "allow event data to be removed" in {
        EventsEventtoeventcrossref.delete
        EventsEventtoeventcrossref.run must have size (0)

        EventsEvent.delete
        EventsEvent.run must have size (0)


      }

      "allow person data to be removed" in {


        PeoplePersontopersonrelationshiptype.delete
        PeoplePersontopersonrelationshiptype.run must have size (0)

        PeoplePersontopersoncrossref.delete
        PeoplePersontopersoncrossref.run must have size (0)

        PeoplePerson.delete
        PeoplePerson.run must have size (0)

      }

      "allow things tables to be cleaned up" in {


        ThingsThingtothingcrossref.delete
        ThingsThingtothingcrossref.run must have size (0)

        ThingsThing.delete
        ThingsThing.run must have size (0)
      }

      "be cleaned up after testing" in {
        DataValue.delete
        DataValue.run must have size (0)

        DataRecord.delete
        DataRecord.run must have size (0)

        DataField.delete
        DataField.run must have size (0)

        DataTabletotablecrossref.delete
        DataTabletotablecrossref.run must have size (0)

        DataTable.delete
        DataTable.run must have size (0)
      }
    }
  }


}