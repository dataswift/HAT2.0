package hatdex.hat.api

import hatdex.hat.dal.Tables._
import org.specs2.specification.AfterAll

//import Tables._
//import Tables.profile.simple._

import hatdex.hat.dal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification

class FacebookExampleSpec extends Specification with AfterAll {
  val db = Database.forConfig("devdb")

  sequential

  "Facebook data structures" should {
    db.withSession { implicit session =>
      "have virtual tables created" in {

        val eventsTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "events", "facebook")
        val meTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "me", "facebook")
        val coverTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "cover", "facebook")
        val ownerTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "owner", "facebook")
        val placeTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "place", "facebook")
        val locationTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "location", "facebook")


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
          new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "Hour", description, symbol),
          new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "Longitude", description, symbol),
          new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "Latitude", description, symbol)
        )

        SystemUnitofmeasurement ++= systemUnitofmeasurementRows

        val result = SystemUnitofmeasurement.run
        result must have size (4)
      }

      "have system types created" in {
        val symbol = Some("Example")
        val description = Some("An example System Type")

        val systemTypeRows = Seq(
          new SystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "Example", description),
          new SystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "Timezone", description),
          new SystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "GPS Coordinate", description)
        )

        SystemType ++= systemTypeRows

        val result = SystemType.run
        result must have size (3)
      }

      "have properties created" in {
        // Get the Units of Measurement to be used
        val unitofmeasurementExampleId = SystemUnitofmeasurement.filter(_.name === "Example").map(_.id).run.head
        val unitofmeasurementHourId = SystemUnitofmeasurement.filter(_.name === "Hour").map(_.id).run.head
        val unitofmeasurementLongitudeId = SystemUnitofmeasurement.filter(_.name === "Longitude").map(_.id).run.head
        val unitofmeasurementLatitudeId = SystemUnitofmeasurement.filter(_.name === "Latitude").map(_.id).run.head

        // Get the types to be used
        val typeExampleId = SystemType.filter(_.name === "Example").map(_.id).run.head
        val typeTimezoneId = SystemType.filter(_.name === "Timezone").map(_.id).run.head
        val typeGpsId = SystemType.filter(_.name === "GPS Coordinate").map(_.id).run.head

        val systemPropertyDescription = Some("test")

        val systemPropertyRows = Seq(
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "attendingcount", systemPropertyDescription, typeExampleId, unitofmeasurementExampleId),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "cover", systemPropertyDescription, typeExampleId, unitofmeasurementExampleId),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "timezone", systemPropertyDescription, typeTimezoneId, unitofmeasurementHourId),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "placename", systemPropertyDescription, typeExampleId, unitofmeasurementExampleId),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "longitude", systemPropertyDescription, typeGpsId, unitofmeasurementLongitudeId),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "latitude", systemPropertyDescription, typeGpsId, unitofmeasurementLatitudeId),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "postcode", systemPropertyDescription, typeExampleId, unitofmeasurementExampleId),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Country", systemPropertyDescription, typeExampleId, unitofmeasurementExampleId),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Organisation Name", systemPropertyDescription, typeExampleId, unitofmeasurementExampleId),
          new SystemPropertyRow(0, LocalDateTime.now(), LocalDateTime.now(), "Owner", systemPropertyDescription, typeExampleId, unitofmeasurementExampleId)
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

      "have systempropertyrecord created" in {
        val localdatetime = Some(LocalDateTime.now())

        val systempropertyRecordRows = Seq(
          new SystemPropertyrecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "Location Property")
        )

        SystemPropertyrecord ++= systempropertyRecordRows

        val result = SystemPropertyrecord.run
        result must have size (1)
      }
      "have Locationssystempropertystaticcrossref created" in {
        val relationshiptype = "Property Cross Reference for a Facebook location"

        val findLocationId = LocationsLocation.filter(_.name === "WMG Warwick University").map(_.id).run.head
        val findlatpropertyId = SystemProperty.filter(_.name === "latitude").map(_.id).run.head
        val findlongpropertyId = SystemProperty.filter(_.name === "longitude").map(_.id).run.head
        val findlatfieldId = DataField.filter(_.name === "latitude").map(_.id).run.head
        val findlongfieldId = DataField.filter(_.name === "longitude").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head

        val locationLatStaticPR = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "Facebook latitude property record")
        val locationLatStaticPRId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += locationLatStaticPR

        val locationLonStaticPR = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "Facebook longitude property record")
        val locationLonStaticPRId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += locationLonStaticPR

        val LocationssystempropertystaticcrossrefRows = Seq(
          new LocationsSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findLocationId, findlatpropertyId, findrecordId, findlatfieldId, relationshiptype, true, locationLonStaticPRId),
          new LocationsSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findLocationId, findlongpropertyId, findrecordId, findlongfieldId, relationshiptype, true, locationLatStaticPRId)
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
        val relationshiptype = "Property Cross Reference for a Facebook Cover"

        val findorganisationId = OrganisationsOrganisation.filter(_.name === "WMG, University of Warwick").map(_.id).run.head
        val findlocationpropertyId = SystemProperty.filter(_.name === "Organisation Name").map(_.id).run.head
        val findlocationfieldId = DataField.filter(_.name === "location").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head

        val osps = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "Facebook organisation property record")
        val ospsId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += osps

        val organisationssystempropertystaticcrossref = new OrganisationsSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findorganisationId, findlocationpropertyId, findrecordId, findlocationfieldId, relationshiptype, true, ospsId)

        OrganisationsSystempropertystaticcrossref += organisationssystempropertystaticcrossref

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
        val relationshiptype = "Property Cross Reference for a Facebook Cover"

        val findeventId = EventsEvent.filter(_.name === "WMG Event").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "timezone").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "timezone").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head

        val systemPropertyStaticRecord = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "Facebook event property record")
        val systemPropertyStaticRecordId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += systemPropertyStaticRecord
        val eventssystempropertystaticcrossref = new EventsSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findeventId, findpropertyId, findrecordId, findfieldId, relationshiptype, true, systemPropertyStaticRecordId)


        EventsSystempropertystaticcrossref += eventssystempropertystaticcrossref

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
        val relationshiptype = "Property Cross Reference for a Facebook Cover"

        val findPersonId = PeoplePerson.filter(_.name === "Martin").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "Owner").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "owner").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head

        val psps = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "peoplesystempropertyStaticCrossref")
        val pspsId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += psps

        val peoplesystempropertystaticcrossref = new PeopleSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findPersonId, findpropertyId, findrecordId, findfieldId, relationshiptype, true, pspsId)

        PeopleSystempropertystaticcrossref += peoplesystempropertystaticcrossref

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
        val relationshiptype = "Property Cross Reference for a Facebook Cover"

        val findthingId = ThingsThing.filter(_.name === "Cover").map(_.id).run.head
        val findpropertyId = SystemProperty.filter(_.name === "cover").map(_.id).run.head
        val findfieldId = DataField.filter(_.name === "cover").map(_.id).run.head
        val findrecordId = DataRecord.filter(_.name === "FacebookEvent1").map(_.id).run.head

        val tsps = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "Facebook Thing Property Static")
        val tspsId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += tsps

        val thingssystempropertystaticcrossref = new ThingsSystempropertystaticcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), findthingId, findpropertyId, findfieldId, findrecordId, relationshiptype, true, tspsId)

        ThingsSystempropertystaticcrossref += thingssystempropertystaticcrossref

        val result = ThingsSystempropertystaticcrossref.run
        result must have size (1)
      }
    }
  }

  "Facebook eventseventpersoncrossref structures" should {
    db.withSession { implicit session =>
      "have eventseventpersoncrossref created" in {
        val localdatetime = Some(LocalDateTime.now())
        val eventseventpersonrelationshiprecord = new SystemRelationshiprecordRow(0,
          LocalDateTime.now(), LocalDateTime.now(), "Facebook Thing Property Static")
        val eventseventpersonrelationshiprecordId = (SystemRelationshiprecord returning SystemRelationshiprecord.map(_.id)) +=
          eventseventpersonrelationshiprecord

        val findPersonId = PeoplePerson.filter(_.name === "Martin").map(_.id).run.head
        val findeventId = EventsEvent.filter(_.name === "WMG Event").map(_.id).run.head

        val eventseventpersoncrossrefRows = Seq(
          new EventsEventpersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
            findPersonId, findeventId, "Relationship_Type", true, eventseventpersonrelationshiprecordId)
        )

        EventsEventpersoncrossref ++= eventseventpersoncrossrefRows

        val result = EventsEventpersoncrossref.run
        result must have size (1)
      }

      "have peoplepersonlocationcrossref created" in {
        val localdatetime = Some(LocalDateTime.now())
        val peoplepersonlocationrelationshiprecord = new SystemRelationshiprecordRow(0,
          LocalDateTime.now(), LocalDateTime.now(), "Facebook Thing Property Static")
        val peoplepersonlocationrelationshiprecordId = (SystemRelationshiprecord returning SystemRelationshiprecord.map(_.id)) +=
          peoplepersonlocationrelationshiprecord

        val findPersonId = PeoplePerson.filter(_.name === "Martin").map(_.id).run.head
        val findLocationId = LocationsLocation.filter(_.name === "WMG Warwick University").map(_.id).run.head

        val peoplepersonlocationcrossrefRows = Seq(
          new PeoplePersonlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
            findLocationId, findPersonId, "Relationship_Type", true, peoplepersonlocationrelationshiprecordId)
        )

        PeoplePersonlocationcrossref ++= peoplepersonlocationcrossrefRows

        val result = PeoplePersonlocationcrossref.run
        result must have size (1)
      }

      "have thingsthingpersoncrossref created" in {
        val localdatetime = Some(LocalDateTime.now())
        val thingsthingpersonrelationshiprecord = new SystemRelationshiprecordRow(0,
          LocalDateTime.now(), LocalDateTime.now(), "Facebook Thing Property Static")
        val thingsthingpersonrelationshiprecordId = (SystemRelationshiprecord returning SystemRelationshiprecord.map(_.id)) +=
          thingsthingpersonrelationshiprecord

        val findPersonId = PeoplePerson.filter(_.name === "Martin").map(_.id).run.head
        val findthingId = ThingsThing.filter(_.name === "Cover").map(_.id).run.head

        val thingsthingpersoncrossrefRows = Seq(
          new ThingsThingpersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
            findPersonId, findthingId, "Relationship_Type", true, thingsthingpersonrelationshiprecordId)
        )

        ThingsThingpersoncrossref ++= thingsthingpersoncrossrefRows

        val result = ThingsThingpersoncrossref.run
        result must have size (1)

      }

      "have peoplepersonorganisationcrossref created" in {
        val localdatetime = Some(LocalDateTime.now())
        val peoplepersonorganisationrelationshiprecord = new SystemRelationshiprecordRow(0,
          LocalDateTime.now(), LocalDateTime.now(), "Facebook Thing Property Static")
        val peoplepersonorganisationrelationshiprecordId = (SystemRelationshiprecord returning SystemRelationshiprecord.map(_.id)) +=
          peoplepersonorganisationrelationshiprecord

        val findPersonId = PeoplePerson.filter(_.name === "Martin").map(_.id).run.head
        val findorganisationId = OrganisationsOrganisation.filter(_.name === "WMG, University of Warwick").map(_.id).run.head

        val peoplepersonorganisationcrossrefRows = Seq(
          new PeoplePersonorganisationcrossrefRow(1,
            LocalDateTime.now(), LocalDateTime.now(), findorganisationId, findPersonId, "Relationship_Type",
            true, peoplepersonorganisationrelationshiprecordId)
        )

        PeoplePersonorganisationcrossref ++= peoplepersonorganisationcrossrefRows

        val result = PeoplePersonorganisationcrossref.run
        result must have size (1)

      }
    }
  }

  //  "Facebook bundle structures" should {
  //    db.withSession { implicit session =>
  //      "have bundle created" in {
  //        val localdatetime = Some(LocalDateTime.now())
  //        val systemrelationshiprecordtobundlecrossref = new SystemRelationshiprecordtobundlecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), "Facebook Thing Property Static")
  //        val systemrelationshiprecordtobundlecrossrefId = (SystemRelationshiprecordtobundlecrossref returning SystemRelationshiprecordtobundlecrossref.map(_.id)) += systemrelationshiprecordtobundlecrossref
  //
  //        val systempropertyrecordtobundlecrrossref = new SystemPropertyrecordtobundlecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), "Facebook Thing Property Static")
  //        val systempropertyrecordtobundlecrrossrefId = (SystemPropertyrecordtobundlecrossref returning SystemPropertyrecordtobundlecrossref.map(_.id)) += systempropertyrecordtobundlecrrossref
  //
  //
  //
  //        val databundleRows = Seq(
  //          new DataBundleRow(1, LocalDateTime.now(), LocalDateTime.now(), "Facebook Event bundle")
  //        )
  //
  //        DataBundle ++= databundleRows
  //
  //        val result = DataBundle.run
  //        result must have size (1)
  //
  //      }
  //    }
  //  }

  //  "Facebook bundle structures" should {
  //    db.withSession { implicit session =>
  //      "have datadebit created" in {
  //        val localdatetime = Some(LocalDateTime.now())
  //        val datadebit = new DataDebitRow(0, LocalDateTime.now(), LocalDateTime.now, "Facebook Event Data Debit", LocalDateTime.now(), LocalDateTime.now(), TRUE, FALSE, 3.25, "Abc-123-def-456-ghj-789","martin.noggin.com", "terry.noggin.com")
  //        val datadebitId = (DataDebit returning DataDebit.map(_.id)) += datadebit
  //      }
  //    }
  //  }

  "Structure cleanup" should {
    db.withSession { implicit session =>
      "allow crossref system data to be removed" in {
        ThingsSystempropertydynamiccrossref.delete
        ThingsSystempropertydynamiccrossref.run must have size (0)

        ThingsSystempropertystaticcrossref.delete
        ThingsSystempropertystaticcrossref.run must have size (0)

        PeopleSystempropertydynamiccrossref.delete
        PeopleSystempropertydynamiccrossref.run must have size (0)

        PeopleSystempropertystaticcrossref.delete
        PeopleSystempropertystaticcrossref.run must have size (0)

        OrganisationsSystempropertydynamiccrossref.delete
        OrganisationsSystempropertydynamiccrossref.run must have size (0)

        OrganisationsSystempropertystaticcrossref.delete
        OrganisationsSystempropertystaticcrossref.run must have size (0)

        LocationsSystempropertydynamiccrossref.delete
        LocationsSystempropertydynamiccrossref.run must have size (0)

        LocationsSystempropertystaticcrossref.delete
        LocationsSystempropertystaticcrossref.run must have size (0)

        EventsSystempropertydynamiccrossref.delete
        EventsSystempropertydynamiccrossref.run must have size (0)

        EventsSystempropertystaticcrossref.delete
        EventsSystempropertystaticcrossref.run must have size (0)

        SystemPropertyrecord.delete
        SystemPropertyrecord.run must have size (0)
      }

      "allow entity crossrefs to be removed" in {
        EventsEventlocationcrossref.delete
        EventsEventorganisationcrossref.delete
        EventsEventtoeventcrossref.delete
        EventsEventpersoncrossref.delete
        EventsEventthingcrossref.delete

        OrganisationsOrganisationtoorganisationcrossref.delete
        OrganisationsOrganisationthingcrossref.delete
        OrganisationsOrganisationlocationcrossref.delete

        PeoplePersontopersoncrossref.delete
        PeoplePersonlocationcrossref.delete
        PeoplePersonorganisationcrossref.delete
        PeoplePersontopersonrelationshiptype.delete

        LocationsLocationthingcrossref.delete
        LocationsLocationtolocationcrossref.delete

        ThingsThingtothingcrossref.delete
        ThingsThingpersoncrossref.delete

        ThingsThingpersoncrossref.run must have size (0)
      }

      "allow entities to be removed" in {
        ThingsThing.delete
        ThingsThing.run must have size (0)

        PeoplePerson.delete
        PeoplePerson.run must have size (0)

        OrganisationsOrganisation.delete
        OrganisationsOrganisation.run must have size (0)

        LocationsLocation.delete
        LocationsLocation.run must have size (0)

        EventsEvent.delete
        EventsEvent.run must have size (0)
      }

      "allow system data to be removed" in {


        SystemTypetotypecrossref.delete
        SystemTypetotypecrossref.run must have size (0)

        SystemProperty.delete
        SystemProperty.run must have size (0)

        SystemType.delete
        SystemType.run must have size (0)

        SystemUnitofmeasurement.delete
        SystemUnitofmeasurement.run must have size (0)


      }

      "allow data to be removed" in {

        DataValue.delete
        DataRecord.delete
        DataField.delete
        DataTabletotablecrossref.delete
        DataTable.delete

        DataValue.run must have size (0)
        DataRecord.run must have size (0)
        DataField.run must have size (0)
        DataTabletotablecrossref.run must have size (0)
        DataTable.run must have size (0)

      }
    }
  }

  def afterAll() = {
    db.withSession { implicit session =>
      TestDataCleanup.cleanupAll
    }
    db.close
  }


}