/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package hatdex.hat.api

import hatdex.hat.dal.Tables._
import org.specs2.execute._
import org.specs2.matcher.MatchResult
import org.specs2.specification.BeforeAfterAll

import hatdex.hat.dal.SlickPostgresDriver.api._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ModelSpec extends Specification with BeforeAfterAll {
  val db = Database.forConfig("devdb")

  sequential

  def awaiting[T]: Future[MatchResult[T]] => Result = { _.await }

  "Core Tables" should {
    "be created" in {

      val getTables = MTable.getTables(None, Some("hat"), None, None).map { ts =>
        ts.map { t =>
          t.name.name
        }
      }

      val requiredTables: Seq[String] = Seq(
        "data_table",
        "things_thing",
        "events_event",
        "people_person",
        "locations_location",
        "organisations_organisation",
        "data_field",
        "system_property")

      val tables = db.run(getTables)
      tables must containAllOf[String](requiredTables).await
    }
  }

  "Data tables" should {
    "be empty" in awaiting {
      db.run(DataValue.result) map { result =>
        result.length must beEqualTo(0)
      }
    }
    "accept data" in awaiting {

      val q = for {
        tableId <- (DataTable returning DataTable.map(_.id)) += DataTableRow(1, LocalDateTime.now(), LocalDateTime.now(), "testTable", "test")
        fieldId <- (DataField returning DataField.map(_.id)) += DataFieldRow(1, LocalDateTime.now(), LocalDateTime.now(), "testField", tableId)
        recordId <- (DataRecord returning DataRecord.map(_.id)) += new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "testRecord")
        tableToTablecrossrefId <- (DataTabletotablecrossref returning DataTabletotablecrossref.map(_.id)) += new DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", tableId, tableId)
        _ <- DataValue += new DataValueRow(1, LocalDateTime.now(), LocalDateTime.now(), "testData", fieldId, recordId)
      } yield (tableId, fieldId, recordId, tableToTablecrossrefId)

      db.run(q).flatMap(_ => db.run(DataValue.result)) map { result =>
        result.length must beEqualTo(1)
      }
    }

    "auto-increment record rows" in awaiting {
      val dataRecordRow = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "record 1")
      val dataRecordRow2 = new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "record 2")

      val q = for {
        recordId <- (DataRecord returning DataRecord.map(_.id)) += dataRecordRow
        recordId2 <- (DataRecord returning DataRecord.map(_.id)) += dataRecordRow2
      } yield (recordId, recordId2)

      DatabaseInfo.db.run(q).map {
        case (r1, r2) =>
          r2 must beGreaterThan(r1)
      }
    }
  }

  "System tables" should {
    "be empty" in awaiting {
      DatabaseInfo.db.run(SystemProperty.result) map { result =>
        result.length must beEqualTo(0)
      }
    }

    "accept data" in awaiting {
      val symbol = Some("Example")
      val description = Some("An example SystemUnitofmeasurement")
      val relationshiptype = "Relationship description"

      val insertQ = for {
        typeId <- (SystemType returning SystemType.map(_.id)) += SystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "testTypeName", Some("Type Description"))
        typetotypecrossrefId <- (SystemTypetotypecrossref returning SystemTypetotypecrossref.map(_.id)) += SystemTypetotypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), typeId, typeId, relationshiptype)
        unitofmeasurementId <- (SystemUnitofmeasurement returning SystemUnitofmeasurement.map(_.id)) += new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "Example", description, symbol)
        propertyId <- (SystemProperty returning SystemProperty.map(_.id)) += SystemPropertyRow(1, LocalDateTime.now(), LocalDateTime.now(), "testProperty", Some("property description"), typeId, unitofmeasurementId)
      } yield (typeId, typetotypecrossrefId, unitofmeasurementId, propertyId)

      DatabaseInfo.db.run(insertQ).flatMap(_ => DatabaseInfo.db.run(SystemProperty.result)) map { result =>
        result must have size (1)
      }
    }
  }

  //  "events tables" should {
  //    db.withSession { implicit session =>
  //      "be empty" in {
  //        val result = EventsEvent.run
  //        result must have size (0)
  //      }
  //      "accept data" in {
  //
  //        val eventseventRow = new EventsEventRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test Event for HAT")
  //        val eventId = (EventsEvent returning EventsEvent.map(_.id)) += eventseventRow
  //
  //        val relationshiptype = "Relationship type"
  //        val findeventId = EventsEvent.filter(_.name === "Test Event for HAT").map(_.id).run.head
  //        val findpropertyId = SystemProperty.filter(_.name === "testProperty").map(_.id).run.head
  //        val findfieldId = DataField.filter(_.name === "testField").map(_.id).run.head
  //        val findrecordId = DataRecord.filter(_.name === "record 1").map(_.id).run.head
  //
  //        val eventRelationshipRecord = new SystemRelationshiprecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "eventToeventCrossref")
  //        val eventRelationshipRecordId = (SystemRelationshiprecord returning SystemRelationshiprecord.map(_.id)) += eventRelationshipRecord
  //        val eventseventtoeventcrossrefRow = new EventsEventtoeventcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findeventId, findeventId, relationshiptype, true, eventRelationshipRecordId)
  //        val eventseventtoeventcrossrefId = (EventsEventtoeventcrossref returning EventsEventtoeventcrossref.map(_.id)) += eventseventtoeventcrossrefRow
  //
  //        val description = Some("An example SystemUnitofmeasurement")
  //
  //        // Link event to a property statically
  //        val systemPropertyStaticRecord = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "eventssystempropertystaticcrossref")
  //        val systemPropertyStaticRecordId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += systemPropertyStaticRecord
  //        val eventssystempropertystaticcrossrefRow = new EventsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findeventId, findpropertyId, findrecordId, findfieldId, relationshiptype, true, systemPropertyStaticRecordId)
  //        val eventssystempropertystaticcrossrefId = (EventsSystempropertystaticcrossref returning EventsSystempropertystaticcrossref.map(_.id)) += eventssystempropertystaticcrossrefRow
  //
  //        // Link event to a property dynamically
  //        val systemPropertyStaticRecordDyn = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "eventssystempropertydynamiccrossref")
  //        val systemPropertyStaticRecordDynId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += systemPropertyStaticRecordDyn
  //        val eventssystempropertydynamiccrossrefRow = new EventsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findeventId, findpropertyId, findfieldId, relationshiptype, true, systemPropertyStaticRecordDynId)
  //        val eventssystempropertydynamiccrossrefId = (EventsSystempropertydynamiccrossref returning EventsSystempropertydynamiccrossref.map(_.id)) += eventssystempropertydynamiccrossrefRow
  //
  //        val result = EventsEvent.run
  //        result must have size (1)
  //      }
  //    }
  //  }
  //
  //  "Locations tables" should {
  //    db.withSession { implicit session =>
  //      "be empty" in {
  //        val result = LocationsLocation.run
  //        result must have size (0)
  //      }
  //      "accept data" in {
  //
  //        val locationsLocationRow = new LocationsLocationRow(1, LocalDateTime.now(), LocalDateTime.now(), "WMG, University of Warwick")
  //        val locationId = (LocationsLocation returning LocationsLocation.map(_.id)) += locationsLocationRow
  //
  //        val relationshiptype = "Relationship type"
  //        val findlocationId = LocationsLocation.filter(_.name === "WMG, University of Warwick").map(_.id).run.head
  //        val findpropertyId = SystemProperty.filter(_.name === "testProperty").map(_.id).run.head
  //        val findfieldId = DataField.filter(_.name === "testField").map(_.id).run.head
  //        val findrecordId = DataRecord.filter(_.name === "record 1").map(_.id).run.head
  //
  //        val locationRelationshipRecord = new SystemRelationshiprecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "locationToLocationCrossref")
  //        val locationRelationshipRecordId = (SystemRelationshiprecord returning SystemRelationshiprecord.map(_.id)) += locationRelationshipRecord
  //        val locationsLocationtolocationcrossrefRow = new LocationsLocationtolocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findlocationId, findlocationId, relationshiptype, true, locationRelationshipRecordId)
  //        val locationsLocationtolocationcrossrefId = (LocationsLocationtolocationcrossref returning LocationsLocationtolocationcrossref.map(_.id)) += locationsLocationtolocationcrossrefRow
  //
  //        val description = Some("An example SystemUnitofmeasurement")
  //
  //        val lsps = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "locationssystempropertystaticcrossref")
  //        val lspsId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += lsps
  //        val locationssystempropertystaticcrossrefRow = new LocationsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findlocationId, findpropertyId, findrecordId, findfieldId, relationshiptype, true, lspsId)
  //        val locationssystempropertystaticcrossrefId = (LocationsSystempropertystaticcrossref returning LocationsSystempropertystaticcrossref.map(_.id)) += locationssystempropertystaticcrossrefRow
  //
  //        val lspd = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "locationssystempropertydynamiccrossref")
  //        val lspdId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += lspd
  //        val locationssystempropertydynamiccrossrefRow = new LocationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findlocationId, findpropertyId, findfieldId, relationshiptype, true, lspdId)
  //        val locationssystempropertydynamiccrossrefId = (LocationsSystempropertydynamiccrossref returning LocationsSystempropertydynamiccrossref.map(_.id)) += locationssystempropertydynamiccrossrefRow
  //
  //        val result = LocationsLocation.run
  //        result must have size (1)
  //      }
  //
  //    }
  //  }
  //
  //  "organisations tables" should {
  //    db.withSession { implicit session =>
  //      "be empty" in {
  //        val result = OrganisationsOrganisation.run
  //        result must have size (0)
  //      }
  //      "accept data" in {
  //
  //        val organisationsorganisationRow = new OrganisationsOrganisationRow(1, LocalDateTime.now(), LocalDateTime.now(), "WMG")
  //        val organisationId = (OrganisationsOrganisation returning OrganisationsOrganisation.map(_.id)) += organisationsorganisationRow
  //
  //        val relationshiptype = "Relationship type"
  //        val findorganisationId = OrganisationsOrganisation.filter(_.name === "WMG").map(_.id).run.head
  //        val findpropertyId = SystemProperty.filter(_.name === "testProperty").map(_.id).run.head
  //        val findfieldId = DataField.filter(_.name === "testField").map(_.id).run.head
  //        val findrecordId = DataRecord.filter(_.name === "record 1").map(_.id).run.head
  //
  //        // Organisation to Organisation link
  //        val ooRelRecord = new SystemRelationshiprecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "organisationorganisationtoorganisationcrossref")
  //        val ooRelRecordId = (SystemRelationshiprecord returning SystemRelationshiprecord.map(_.id)) += ooRelRecord
  //        val organisationorganisationtoorganisationcrossrefRow = new OrganisationsOrganisationtoorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findorganisationId, findorganisationId, "relationshiptype", true, ooRelRecordId)
  //        val organisationorganisationtoorganisationcrossrefId = (OrganisationsOrganisationtoorganisationcrossref returning OrganisationsOrganisationtoorganisationcrossref.map(_.id)) += organisationorganisationtoorganisationcrossrefRow
  //
  //        val description = Some("An example SystemUnitofmeasurement")
  //
  //        // Organisation Property Static crossref
  //        val osps = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "organisationssystempropertystaticcrossref")
  //        val ospsId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += osps
  //        val organisationssystempropertystaticcrossrefRow = new OrganisationsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findorganisationId, findpropertyId, findrecordId, findfieldId, relationshiptype, true, ospsId)
  //        val organisationssystempropertystaticcrossrefId = (OrganisationsSystempropertystaticcrossref returning OrganisationsSystempropertystaticcrossref.map(_.id)) += organisationssystempropertystaticcrossrefRow
  //
  //        // Organisation Property Dynamic crossref
  //        val ospd = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "organisationssystempropertydynamiccrossref")
  //        val ospdId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += ospd
  //        val organisationssystempropertydynamiccrossrefRow = new OrganisationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findorganisationId, findpropertyId, findfieldId, relationshiptype, true, ospdId)
  //        val organisationssystempropertydynamiccrossrefId = (OrganisationsSystempropertydynamiccrossref returning OrganisationsSystempropertydynamiccrossref.map(_.id)) += organisationssystempropertydynamiccrossrefRow
  //
  //        // Check the organisation has been added
  //        val result = OrganisationsOrganisation.run
  //        result must have size (1)
  //      }
  //
  //    }
  //  }
  //
  //  "People tables" should {
  //    db.withSession { implicit session =>
  //      "be empty" in {
  //        val result = PeoplePerson.run
  //        result must have size (0)
  //      }
  //      "accept data" in {
  //
  //        val PeoplePersonRow = new PeoplePersonRow(1, LocalDateTime.now(), LocalDateTime.now(), "Martin", "Abc-123-def-456")
  //        val PersonId = (PeoplePerson returning PeoplePerson.map(_.id)) += PeoplePersonRow
  //
  //        val relationshiptype = "Relationship type"
  //        val findpeopleId = PeoplePerson.filter(_.name === "Martin").map(_.id).run.head
  //        val findpropertyId = SystemProperty.filter(_.name === "testProperty").map(_.id).run.head
  //        val findfieldId = DataField.filter(_.name === "testField").map(_.id).run.head
  //        val findrecordId = DataRecord.filter(_.name === "record 1").map(_.id).run.head
  //
  //        // Person to Person link
  //        val ppRelRecord = new SystemRelationshiprecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "peoplePersontopersonrelationshiptype")
  //        val ppRelRecordId = (SystemRelationshiprecord returning SystemRelationshiprecord.map(_.id)) += ppRelRecord
  //
  //        val personRelationshipType = new PeoplePersontopersonrelationshiptypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "Martin's Martin", Some(relationshiptype))
  //        val personRelationshipTypeId = (PeoplePersontopersonrelationshiptype returning PeoplePersontopersonrelationshiptype.map(_.id)) += personRelationshipType
  //
  //        val peoplePersontopersoncrossrefRow = new PeoplePersontopersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findpeopleId, findpeopleId, personRelationshipTypeId, true, ppRelRecordId)
  //        val peoplePersontopersoncrossrefId = (PeoplePersontopersoncrossref returning PeoplePersontopersoncrossref.map(_.id)) += peoplePersontopersoncrossrefRow
  //
  //        // Person Property Static crossref
  //        val psps = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "peoplesystempropertyStaticCrossref")
  //        val pspsId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += psps
  //        val peoplesystempropertystaticcrossrefRow = new PeopleSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findpeopleId, findpropertyId, findrecordId, findfieldId, relationshiptype, true, pspsId)
  //        val peoplesystempropertystaticcrossrefId = (PeopleSystempropertystaticcrossref returning PeopleSystempropertystaticcrossref.map(_.id)) += peoplesystempropertystaticcrossrefRow
  //
  //        // Person Property Dynamic crossref
  //        val pspd = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "peoplesystempropertyDynamicCrossref")
  //        val pspdId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += pspd
  //        val peoplesystempropertydynamiccrossrefRow = new PeopleSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findpeopleId, findpropertyId, findfieldId, relationshiptype, true, pspdId)
  //        val peoplesystempropertydynamiccrossrefId = (PeopleSystempropertydynamiccrossref returning PeopleSystempropertydynamiccrossref.map(_.id)) += peoplesystempropertydynamiccrossrefRow
  //
  //        // Check the person has been added
  //        val result = PeoplePerson.run
  //        result must have size (1)
  //      }
  //
  //    }
  //  }
  //
  //  "Things tables" should {
  //    db.withSession { implicit session =>
  //      "be empty" in {
  //        val result = ThingsThing.run
  //        result must have size (0)
  //      }
  //      "accept data" in {
  //
  //        val thingsthingRow = new ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "Test Thing")
  //        val thingId = (ThingsThing returning ThingsThing.map(_.id)) += thingsthingRow
  //
  //        val findthingId = ThingsThing.filter(_.name === "Test Thing").map(_.id).run.head
  //        val findpropertyId = SystemProperty.filter(_.name === "testProperty").map(_.id).run.head
  //        val findfieldId = DataField.filter(_.name === "testField").map(_.id).run.head
  //        val findrecordId = DataRecord.filter(_.name === "record 1").map(_.id).run.head
  //        val relationshiptype = "Relationship type"
  //
  //        // Thing to Thing link
  //        val ttRelRecord = new SystemRelationshiprecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "ThingToThing")
  //        val ttRelRecordId = (SystemRelationshiprecord returning SystemRelationshiprecord.map(_.id)) += ttRelRecord
  //        val thingsthingtothingcrossrefRow = new ThingsThingtothingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findthingId, findthingId, relationshiptype, true, ttRelRecordId)
  //        val thingsthingtothingcrossrefId = (ThingsThingtothingcrossref returning ThingsThingtothingcrossref.map(_.id)) += thingsthingtothingcrossrefRow
  //
  //        // Thing Property Static crossref
  //        val tsps = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "ThingPropertyStatic")
  //        val tspsId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += tsps
  //        val thingssystempropertystaticcrossrefRow = new ThingsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findthingId, findpropertyId, findrecordId, findfieldId, relationshiptype, true, tspsId)
  //        val thingssystempropertystaticcrossrefId = (ThingsSystempropertystaticcrossref returning ThingsSystempropertystaticcrossref.map(_.id)) += thingssystempropertystaticcrossrefRow
  //
  //        // Thing Property Dynamic crossref
  //        val tspd = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), "ThingPropertyStatic")
  //        val tspdId = (SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += tspd
  //        val thingssystempropertydynamiccrossrefRow = new ThingsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), findthingId, findpropertyId, findfieldId, relationshiptype, true, tspdId)
  //        val thingssystempropertydynamiccrossrefId = (ThingsSystempropertydynamiccrossref returning ThingsSystempropertydynamiccrossref.map(_.id)) += thingssystempropertydynamiccrossrefRow
  //
  //        val result = ThingsThing.run
  //        result must have size (1)
  //      }
  //
  //    }
  //  }

  def beforeAll() = {
    TestDataCleanup.cleanupAll
  }

  def afterAll() = {
    TestDataCleanup.cleanupAll
  }

}