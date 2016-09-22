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

import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

object TestFixtures {
  def prepareEverything = {
    val (dataFields, dataRecords) = prepareDataStructures
    val (systemUoms, systemTypes) = prepareSystemTypes
    val systemProperties = prepareSystemProperties(systemUoms, systemTypes)
    val (things, people, locations, organisations, events, entities) = prepareEntities
    contextualiseEntities(things, people, locations, organisations, events)
    linkEntityData(things, people, locations, organisations, events, systemProperties, systemTypes, dataFields, dataRecords)
  }
  def prepareDataStructures = {
    // Data tables
    val dataTables = Seq(
      DataTableRow(1, LocalDateTime.now(), LocalDateTime.now(), "Facebook", "facebook"),
      DataTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "events", "facebook"),
      DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "me", "facebook"),
      DataTableRow(4, LocalDateTime.now(), LocalDateTime.now(), "Fibaro", "Fibaro"),
      DataTableRow(5, LocalDateTime.now(), LocalDateTime.now(), "Fitbit", "Fitbit"),
      DataTableRow(6, LocalDateTime.now(), LocalDateTime.now(), "locations", "facebook"))

    DatabaseInfo.db.run(DataTable.forceInsertAll(dataTables))

    val facebookTableId = dataTables.find(_.name equals "Facebook").get.id
    val eventsTableId = dataTables.find(_.name equals "events").get.id
    val meTableId = dataTables.find(_.name equals "me").get.id
    val locationsTableId = dataTables.find(_.name equals "locations").get.id
    val fibaroTableId = dataTables.find(_.name equals "Fibaro").get.id
    val fitbitTableId = dataTables.find(_.name equals "Fitbit").get.id

    // Nesting of data tables
    val dataTableToTableCrossRefRows = Seq(
      DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", facebookTableId, eventsTableId),
      DataTabletotablecrossrefRow(2, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", facebookTableId, locationsTableId),
      DataTabletotablecrossrefRow(3, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", facebookTableId, meTableId))

    DatabaseInfo.db.run(DataTabletotablecrossref.forceInsertAll(dataTableToTableCrossRefRows))

    // Adding data fields to data tables
    val dataFields = Seq(
      DataFieldRow(1, LocalDateTime.now(), LocalDateTime.now(), "weight", meTableId),
      DataFieldRow(2, LocalDateTime.now(), LocalDateTime.now(), "elevation", locationsTableId),
      DataFieldRow(3, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", eventsTableId),
      DataFieldRow(4, LocalDateTime.now(), LocalDateTime.now(), "size", fibaroTableId),
      DataFieldRow(5, LocalDateTime.now(), LocalDateTime.now(), "temperature", fibaroTableId),
      DataFieldRow(6, LocalDateTime.now(), LocalDateTime.now(), "cars speed", fibaroTableId),
      DataFieldRow(7, LocalDateTime.now(), LocalDateTime.now(), "number of employees", facebookTableId))

    DatabaseInfo.db.run(DataField.forceInsertAll(dataFields))

    val weightFieldId = dataFields.find(_.name equals "weight").get.id
    val elevationFieldId = dataFields.find(_.name equals "elevation").get.id
    val kichenElectricityFieldId = dataFields.find(_.name equals "kichenElectricity").get.id
    val sizeFieldId = dataFields.find(_.name equals "size").get.id
    val temperatureFieldId = dataFields.find(_.name equals "temperature").get.id

    // Data records to connect data items together
    val dataRecords = Seq(
      DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "FacebookMe"),
      DataRecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "FacebookLocation"),
      DataRecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "FibaroKitchen"),
      DataRecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent"),
      DataRecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "FibaroBathroom"),
      DataRecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "car journey"))

    DatabaseInfo.db.run(DataRecord.forceInsertAll(dataRecords))

    val facebookMeRecordId = dataRecords.find(_.name equals "FacebookMe").get.id
    val facebookLocationRecordId = dataRecords.find(_.name equals "FacebookLocation").get.id
    val fibaroKitchenRecordId = dataRecords.find(_.name equals "FibaroKitchen").get.id
    val facebookEventRecordId = dataRecords.find(_.name equals "FacebookEvent").get.id
    val fibaroBathroomRecordId = dataRecords.find(_.name equals "FibaroBathroom").get.id

    val dataValueRows = Seq(
      DataValueRow(1, LocalDateTime.now(), LocalDateTime.now(), "62", weightFieldId, facebookMeRecordId),
      DataValueRow(2, LocalDateTime.now(), LocalDateTime.now(), "300", elevationFieldId, facebookLocationRecordId),
      DataValueRow(3, LocalDateTime.now(), LocalDateTime.now(), "20", kichenElectricityFieldId, fibaroKitchenRecordId),
      DataValueRow(4, LocalDateTime.now(), LocalDateTime.now(), "Having a Shower", facebookEventRecordId, fibaroBathroomRecordId),
      DataValueRow(5, LocalDateTime.now(), LocalDateTime.now(), "25", temperatureFieldId, facebookEventRecordId))

    DatabaseInfo.db.run(DataValue.forceInsertAll(dataValueRows))

    (dataFields, dataRecords)
  }

  def prepareSystemTypes = {
    // contextualisation tools
    val systemUOMs = Seq(
      SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "meters", Some("distance measurement"), Some("m")),
      SystemUnitofmeasurementRow(2, LocalDateTime.now(), LocalDateTime.now(), "kilograms", Some("weight measurement"), Some("kg")),
      SystemUnitofmeasurementRow(3, LocalDateTime.now(), LocalDateTime.now(), "meters cubed", Some("3d space"), Some("m^3")),
      SystemUnitofmeasurementRow(4, LocalDateTime.now(), LocalDateTime.now(), "Kilowatt hours", Some("electricity measurement"), Some("KwH")),
      SystemUnitofmeasurementRow(5, LocalDateTime.now(), LocalDateTime.now(), "centigrade", Some("heat measurement"), Some("C")),
      SystemUnitofmeasurementRow(6, LocalDateTime.now(), LocalDateTime.now(), "miles per hour", Some("speed measurement"), Some("mph")),
      SystemUnitofmeasurementRow(7, LocalDateTime.now(), LocalDateTime.now(), "number", Some("amount of something"), Some("n")))

    DatabaseInfo.db.run(SystemUnitofmeasurement.forceInsertAll(systemUOMs))

    val systemTypes = Seq(
      SystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "room dimensions", Some("Fibaro")),
      SystemTypeRow(2, LocalDateTime.now(), LocalDateTime.now(), "dayily activities", Some("Google Calendar")),
      SystemTypeRow(3, LocalDateTime.now(), LocalDateTime.now(), "utilities", Some("Fibaro")),
      SystemTypeRow(4, LocalDateTime.now(), LocalDateTime.now(), "personattributes", Some("Fitbit")),
      SystemTypeRow(5, LocalDateTime.now(), LocalDateTime.now(), "locationattributes", Some("GPS application")),
      SystemTypeRow(6, LocalDateTime.now(), LocalDateTime.now(), "measurement", Some("measurement of something")),
      SystemTypeRow(7, LocalDateTime.now(), LocalDateTime.now(), "room", Some("building seperator")),
      SystemTypeRow(8, LocalDateTime.now(), LocalDateTime.now(), "vehicle", Some("type of transport")),
      SystemTypeRow(9, LocalDateTime.now(), LocalDateTime.now(), "male", Some("gender type")),
      SystemTypeRow(10, LocalDateTime.now(), LocalDateTime.now(), "department", Some("faculty of a university")))

    DatabaseInfo.db.run(SystemType.forceInsertAll(systemTypes))

    (systemUOMs, systemTypes)
  }

  def prepareSystemProperties(systemUOMs: Seq[SystemUnitofmeasurementRow], systemTypes: Seq[SystemTypeRow]) = {
    val systemProperties = Seq(
      SystemPropertyRow(1, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", Some("Electricity use in a kitchen"),
        systemTypes.find(_.name equals "utilities").get.id,
        systemUOMs.find(_.name equals "Kilowatt hours").get.id),
      SystemPropertyRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse", Some("Use of a shower"),
        systemTypes.find(_.name equals "utilities").get.id,
        systemUOMs.find(_.name equals "Kilowatt hours").get.id),
      SystemPropertyRow(3, LocalDateTime.now(), LocalDateTime.now(), "size", Some("Size of an object"),
        systemTypes.find(_.name equals "measurement").get.id,
        systemUOMs.find(_.name equals "meters cubed").get.id),
      SystemPropertyRow(4, LocalDateTime.now(), LocalDateTime.now(), "elevation", Some("Height of location or thing"),
        systemTypes.find(_.name equals "measurement").get.id,
        systemUOMs.find(_.name equals "meters").get.id),
      SystemPropertyRow(5, LocalDateTime.now(), LocalDateTime.now(), "temperature", Some("Current temperature"),
        systemTypes.find(_.name equals "measurement").get.id,
        systemUOMs.find(_.name equals "centigrade").get.id),
      SystemPropertyRow(6, LocalDateTime.now(), LocalDateTime.now(), "weight", Some("Weight of a person"),
        systemTypes.find(_.name equals "measurement").get.id,
        systemUOMs.find(_.name equals "kilograms").get.id),
      SystemPropertyRow(7, LocalDateTime.now(), LocalDateTime.now(), "cars speed", Some("Car speed"),
        systemTypes.find(_.name equals "measurement").get.id,
        systemUOMs.find(_.name equals "miles per hour").get.id),
      SystemPropertyRow(8, LocalDateTime.now(), LocalDateTime.now(), "employees", Some("number of employees"),
        systemTypes.find(_.name equals "measurement").get.id,
        systemUOMs.find(_.name equals "number").get.id))

    DatabaseInfo.db.run(SystemProperty.forceInsertAll(systemProperties))

    systemProperties
  }

  def prepareEntities = {
    // Entities
    val things = Seq(
      ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "cupbord"),
      ThingsThingRow(2, LocalDateTime.now(), LocalDateTime.now(), "car"),
      ThingsThingRow(3, LocalDateTime.now(), LocalDateTime.now(), "shower"),
      ThingsThingRow(4, LocalDateTime.now(), LocalDateTime.now(), "scales"))

    DatabaseInfo.db.run(ThingsThing.forceInsertAll(things))

    val people = Seq(
      PeoplePersonRow(1, LocalDateTime.now(), LocalDateTime.now(), "Martin", "martinID"),
      PeoplePersonRow(2, LocalDateTime.now(), LocalDateTime.now(), "Andrius", "andriusID"),
      PeoplePersonRow(3, LocalDateTime.now(), LocalDateTime.now(), "Xiao", "xiaoID"))

    DatabaseInfo.db.run(PeoplePerson.forceInsertAll(people))

    val locations = Seq(
      LocationsLocationRow(1, LocalDateTime.now(), LocalDateTime.now(), "kitchen"),
      LocationsLocationRow(2, LocalDateTime.now(), LocalDateTime.now(), "bathroom"),
      LocationsLocationRow(3, LocalDateTime.now(), LocalDateTime.now(), "WMG"),
      LocationsLocationRow(4, LocalDateTime.now(), LocalDateTime.now(), "Coventry"))

    DatabaseInfo.db.run(LocationsLocation.forceInsertAll(locations))

    val organisations = Seq(
      OrganisationsOrganisationRow(1, LocalDateTime.now(), LocalDateTime.now(), "seventrent"),
      OrganisationsOrganisationRow(2, LocalDateTime.now(), LocalDateTime.now(), "WMG"),
      OrganisationsOrganisationRow(3, LocalDateTime.now(), LocalDateTime.now(), "Coventry"))

    DatabaseInfo.db.run(OrganisationsOrganisation.forceInsertAll(organisations))

    val events = Seq(
      EventsEventRow(1, LocalDateTime.now(), LocalDateTime.now(), "having a shower"),
      EventsEventRow(2, LocalDateTime.now(), LocalDateTime.now(), "driving"),
      EventsEventRow(3, LocalDateTime.now(), LocalDateTime.now(), "going to work"),
      EventsEventRow(4, LocalDateTime.now(), LocalDateTime.now(), "cooking"))

    DatabaseInfo.db.run(EventsEvent.forceInsertAll(events))

    var entityId = 1
    val entities = things.map { thing =>
      entityId += 1
      EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), thing.name, "thing", None, Some(thing.id), None, None, None)
    } ++ people.map { person =>
      entityId += 1
      EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), person.name, "person", None, None, None, None, Some(person.id))
    } ++ locations.map { location =>
      entityId += 1
      EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), location.name, "location", Some(location.id), None, None, None, None)
    } ++ organisations.map { organisation =>
      entityId += 1
      EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), organisation.name, "organisation", None, None, None, Some(organisation.id), None)
    } ++ events.map { event =>
      entityId += 1
      EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), event.name, "event", None, None, Some(event.id), None, None)
    }

    DatabaseInfo.db.run(Entity.forceInsertAll(entities))

    (things, people, locations, organisations, events, entities)
  }

  def contextualiseEntities(things: Seq[ThingsThingRow], people: Seq[PeoplePersonRow],
                            locations: Seq[LocationsLocationRow], organisations: Seq[OrganisationsOrganisationRow],
                            events: Seq[EventsEventRow]) = {

    // Relationship Record

    val relationshipRecords = Seq(
      SystemRelationshiprecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "Driving to Work"),
      SystemRelationshiprecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "Shower Used_During having a shower"),
      SystemRelationshiprecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "having a shower Is_At bathrom"),
      SystemRelationshiprecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "Uses_Utility"),
      SystemRelationshiprecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child"),
      SystemRelationshiprecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "Owns"),
      SystemRelationshiprecordRow(7, LocalDateTime.now(), LocalDateTime.now(), "Next_To"),
      SystemRelationshiprecordRow(8, LocalDateTime.now(), LocalDateTime.now(), "Buys_From"),
      SystemRelationshiprecordRow(9, LocalDateTime.now(), LocalDateTime.now(), "Car Ownership"),
      SystemRelationshiprecordRow(10, LocalDateTime.now(), LocalDateTime.now(), "Martin Is having a shower"),
      SystemRelationshiprecordRow(11, LocalDateTime.now(), LocalDateTime.now(), "Going to Organisation WMG"),
      SystemRelationshiprecordRow(12, LocalDateTime.now(), LocalDateTime.now(), "Shower Parent_Child cupbord"),
      SystemRelationshiprecordRow(13, LocalDateTime.now(), LocalDateTime.now(), "Martin Owns Car"),
      SystemRelationshiprecordRow(14, LocalDateTime.now(), LocalDateTime.now(), "Kitchen Next_To bathroom"),
      SystemRelationshiprecordRow(15, LocalDateTime.now(), LocalDateTime.now(), "WMG location Is_At Coventry"),
      SystemRelationshiprecordRow(16, LocalDateTime.now(), LocalDateTime.now(), "WMG Buys_From seventrent"),
      SystemRelationshiprecordRow(17, LocalDateTime.now(), LocalDateTime.now(), "WMG organisation Is_At Coventry"),
      SystemRelationshiprecordRow(18, LocalDateTime.now(), LocalDateTime.now(), "WMG organisation Rents car"),
      SystemRelationshiprecordRow(19, LocalDateTime.now(), LocalDateTime.now(), "Martin Works at WMG"),
      SystemRelationshiprecordRow(20, LocalDateTime.now(), LocalDateTime.now(), "Martin Colleague_With Andrius"),
      SystemRelationshiprecordRow(21, LocalDateTime.now(), LocalDateTime.now(), "Xiao Is_at Coventry"),
      SystemRelationshiprecordRow(22, LocalDateTime.now(), LocalDateTime.now(), "size"),
      SystemRelationshiprecordRow(23, LocalDateTime.now(), LocalDateTime.now(), "Car Ownership"),
      SystemRelationshiprecordRow(24, LocalDateTime.now(), LocalDateTime.now(), "elevation"),
      SystemRelationshiprecordRow(25, LocalDateTime.now(), LocalDateTime.now(), "Driving to Work"),
      SystemRelationshiprecordRow(26, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      SystemRelationshiprecordRow(27, LocalDateTime.now(), LocalDateTime.now(), "size"),
      SystemRelationshiprecordRow(28, LocalDateTime.now(), LocalDateTime.now(), "weight"),
      SystemRelationshiprecordRow(29, LocalDateTime.now(), LocalDateTime.now(), "car is at"))

    DatabaseInfo.db.run(SystemRelationshiprecord.forceInsertAll(relationshipRecords))

    // Event Relationships

    val eventsEventToEventCrossRefRows = Seq(
      EventsEventtoeventcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "going to work").get.id,
        events.find(_.name equals "driving").get.id,
        "Driving to work", isCurrent = true,
        relationshipRecords.find(_.name equals "Driving to Work").get.id))
    DatabaseInfo.db.run(EventsEventtoeventcrossref.forceInsertAll(eventsEventToEventCrossRefRows))

    val eventsEventtothingcrossrefRows = Seq(
      EventsEventthingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "having a shower").get.id,
        things.find(_.name equals "shower").get.id,
        "Used_During", isCurrent = true,
        relationshipRecords.find(_.name equals "Shower Used_During having a shower").get.id))

    DatabaseInfo.db.run(EventsEventthingcrossref.forceInsertAll(eventsEventtothingcrossrefRows))

    val eventsEventToLocationCrossRefRows = Seq(
      EventsEventlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "having a shower").get.id,
        locations.find(_.name equals "bathroom").get.id,
        "Is_At", isCurrent = true,
        relationshipRecords.find(_.name equals "having a shower Is_At bathrom").get.id))

    DatabaseInfo.db.run(EventsEventlocationcrossref.forceInsertAll(eventsEventToLocationCrossRefRows))

    val eventsEventToPersonCrossRefRows = Seq(
      EventsEventpersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "having a shower").get.id,
        people.find(_.name equals "Martin").get.id,
        "Is", isCurrent = true,
        relationshipRecords.find(_.name equals "Martin Is having a shower").get.id))

    DatabaseInfo.db.run(EventsEventpersoncrossref.forceInsertAll(eventsEventToPersonCrossRefRows))

    val eventsEventToOrganisationCrossRefRows = Seq(
      EventsEventorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "going to work").get.id,
        organisations.find(_.name equals "WMG").get.id,
        "Going to Organisation", isCurrent = true,
        relationshipRecords.find(_.name equals "Going to Organisation WMG").get.id))

    DatabaseInfo.db.run(EventsEventorganisationcrossref.forceInsertAll(eventsEventToOrganisationCrossRefRows))

    //  Thing Relationships

    val thingsThingtothingcrossrefRows = Seq(
      ThingsThingtothingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "cupbord").get.id,
        things.find(_.name equals "shower").get.id,
        "Parent_Child", isCurrent = true,
        relationshipRecords.find(_.name equals "Shower Parent_Child cupbord").get.id))

    DatabaseInfo.db.run(ThingsThingtothingcrossref.forceInsertAll(thingsThingtothingcrossrefRows))

    val thingsThingToPersonCrossRefRows = Seq(
      ThingsThingpersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "car").get.id,
        people.find(_.name equals "Martin").get.id,
        "Owns", isCurrent = true,
        relationshipRecords.find(_.name equals "Martin Owns Car").get.id))

    DatabaseInfo.db.run(ThingsThingpersoncrossref.forceInsertAll(thingsThingToPersonCrossRefRows))

    // Location Relationships

    val locationsLocationToLocationCrossRefRows = Seq(
      LocationsLocationtolocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "kitchen").get.id,
        locations.find(_.name equals "bathroom").get.id,
        "Next_To", isCurrent = true,
        relationshipRecords.find(_.name equals "Kitchen Next_To bathroom").get.id),
      LocationsLocationtolocationcrossrefRow(2, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "WMG").get.id,
        locations.find(_.name equals "Coventry").get.id,
        "Is_At", isCurrent = true,
        relationshipRecords.find(_.name equals "WMG location Is_At Coventry").get.id))

    DatabaseInfo.db.run(LocationsLocationtolocationcrossref.forceInsertAll(locationsLocationToLocationCrossRefRows))

    val locationsLocationtothingcrossrefRows = Seq(
      LocationsLocationthingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "WMG").get.id,
        things.find(_.name equals "car").get.id,
        "Is_At", isCurrent = true,
        relationshipRecords.find(_.name equals "car is at").get.id))

    DatabaseInfo.db.run(LocationsLocationthingcrossref.forceInsertAll(locationsLocationtothingcrossrefRows))

    // Organisation Relationships

    val organisationsOrganisationToOrganisationCrossRefRows = Seq(
      OrganisationsOrganisationtoorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "seventrent").get.id,
        organisations.find(_.name equals "WMG").get.id,
        "Buys_From", isCurrent = true,
        relationshipRecords.find(_.name equals "WMG Buys_From seventrent").get.id))

    DatabaseInfo.db.run(OrganisationsOrganisationtoorganisationcrossref.forceInsertAll(organisationsOrganisationToOrganisationCrossRefRows))

    val organisationOrganisationLocationCrossRefRows = Seq(
      OrganisationsOrganisationlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "Coventry").get.id,
        organisations.find(_.name equals "WMG").get.id,
        "Is_At", isCurrent = true,
        relationshipRecords.find(_.name equals "WMG organisation Is_At Coventry").get.id))

    DatabaseInfo.db.run(OrganisationsOrganisationlocationcrossref.forceInsertAll(organisationOrganisationLocationCrossRefRows))

    val organisationOrganisationThingCrossRefRows = Seq(
      OrganisationsOrganisationthingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        things.find(_.name equals "car").get.id,
        "Rents", isCurrent = true,
        relationshipRecords.find(_.name equals "WMG organisation Rents car").get.id))

    DatabaseInfo.db.run(OrganisationsOrganisationthingcrossref.forceInsertAll(organisationOrganisationThingCrossRefRows))

    //People Relationships

    val personRelationshipTypes = Seq(
      PeoplePersontopersonrelationshiptypeRow(1, LocalDateTime.now(), LocalDateTime.now(),
        "Colleague With", Some("Working Together")))

    DatabaseInfo.db.run(PeoplePersontopersonrelationshiptype.forceInsertAll(personRelationshipTypes))

    val peoplePersonToPersonCrossRefRows = Seq(
      PeoplePersontopersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        people.find(_.name equals "Andrius").get.id,
        personRelationshipTypes.find(_.name equals "Colleague With").get.id,
        true,
        relationshipRecords.find(_.name equals "Martin Colleague_With Andrius").get.id))

    DatabaseInfo.db.run(PeoplePersontopersoncrossref.forceInsertAll(peoplePersonToPersonCrossRefRows))

    val peoplePersonOrganisationCrossRefRows = Seq(
      PeoplePersonorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        organisations.find(_.name equals "WMG").get.id,
        "Works_at", isCurrent = true,
        relationshipRecords.find(_.name equals "Martin Works at WMG").get.id))

    val peoplePersonLocationCrossRefCrossRefRows = Seq(
      PeoplePersonlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "Coventry").get.id,
        people.find(_.name equals "Xiao").get.id,
        "Is_at", isCurrent = true,
        relationshipRecords.find(_.name equals "Xiao Is_at Coventry").get.id))

    DatabaseInfo.db.run(PeoplePersonlocationcrossref.forceInsertAll(peoplePersonLocationCrossRefCrossRefRows))
  }

  def linkEntityData(
    things: Seq[ThingsThingRow], people: Seq[PeoplePersonRow], locations: Seq[LocationsLocationRow],
    organisations: Seq[OrganisationsOrganisationRow], events: Seq[EventsEventRow],
    systemProperties: Seq[SystemPropertyRow], systemTypes: Seq[SystemTypeRow],
    dataFields: Seq[DataFieldRow], dataRecords: Seq[DataRecordRow]) = {

    // Entity - Property linking

    val propertyRecords = Seq(
      SystemPropertyrecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity"),
      SystemPropertyrecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      SystemPropertyrecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "size"),
      SystemPropertyrecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "weight"),
      SystemPropertyrecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "elevation"),
      SystemPropertyrecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "city size"),
      SystemPropertyrecordRow(7, LocalDateTime.now(), LocalDateTime.now(), "temperature"),
      SystemPropertyrecordRow(8, LocalDateTime.now(), LocalDateTime.now(), "speed of car"),
      SystemPropertyrecordRow(9, LocalDateTime.now(), LocalDateTime.now(), "number of employees"))

    DatabaseInfo.db.run(SystemPropertyrecord.forceInsertAll(propertyRecords))

    // location Property/type Relationships

    val locationsSystempropertydynamiccrossrefRows = Seq(
      LocationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "Coventry").get.id,
        systemProperties.find(_.name equals "size").get.id,
        dataFields.find(_.name equals "size").get.id,
        "Size of City Location", isCurrent = true,
        propertyRecords.find(_.name equals "city size").get.id))

    DatabaseInfo.db.run(LocationsSystempropertydynamiccrossref.forceInsertAll(locationsSystempropertydynamiccrossrefRows))

    val locationsSystempropertystaticcrossrefRows = Seq(
      LocationsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "Coventry").get.id,
        systemProperties.find(_.name equals "size").get.id,
        dataRecords.find(_.name equals "FacebookLocation").get.id,
        dataFields.find(_.name equals "size").get.id,
        "Size of City Location", isCurrent = true,
        propertyRecords.find(_.name equals "city size").get.id))

    DatabaseInfo.db.run(LocationsSystempropertystaticcrossref.forceInsertAll(locationsSystempropertystaticcrossrefRows))

    val locationSystemTypes = Seq(
      LocationsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "bathroom").get.id,
        systemTypes.find(_.name equals "room").get.id, "Is", isCurrent = true))

    DatabaseInfo.db.run(LocationsSystemtypecrossref.forceInsertAll(locationSystemTypes))

    // things Property/type Relationships

    val thingsSystempropertystaticcrossrefRows = Seq(
      ThingsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "cupbord").get.id,
        systemProperties.find(_.name equals "size").get.id,
        dataRecords.find(_.name equals "FacebookLocation").get.id,
        dataFields.find(_.name equals "size").get.id,
        "Parent Child", isCurrent = true,
        propertyRecords.find(_.name equals "city size").get.id))

    DatabaseInfo.db.run(ThingsSystempropertystaticcrossref.forceInsertAll(thingsSystempropertystaticcrossrefRows))

    val thingsSystempropertydynamiccrossrefRows = Seq(
      ThingsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "car").get.id,
        systemProperties.find(_.name equals "temperature").get.id,
        dataFields.find(_.name equals "temperature").get.id,
        "Parent Child", isCurrent = true,
        propertyRecords.find(_.name equals "temperature").get.id))

    DatabaseInfo.db.run(ThingsSystempropertydynamiccrossref.forceInsertAll(thingsSystempropertydynamiccrossrefRows))

    val thingsSystemtypecrossrefRows = Seq(
      ThingsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "car").get.id,
        systemTypes.find(_.name equals "vehicle").get.id, "Is", isCurrent = true))

    DatabaseInfo.db.run(ThingsSystemtypecrossref.forceInsertAll(thingsSystemtypecrossrefRows))

    // people Property/type Relationships

    val peopleSystempropertystaticcrossrefRows = Seq(
      PeopleSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        systemProperties.find(_.name equals "weight").get.id,
        dataRecords.find(_.name equals "FibaroBathroom").get.id,
        dataFields.find(_.name equals "weight").get.id,
        "Parent_Child", isCurrent = true,
        propertyRecords.find(_.name equals "weight").get.id))

    DatabaseInfo.db.run(PeopleSystempropertystaticcrossref.forceInsertAll(peopleSystempropertystaticcrossrefRows))

    val peopleSystempropertydynamiccrossrefRows = Seq(
      PeopleSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        systemProperties.find(_.name equals "weight").get.id,
        dataFields.find(_.name equals "weight").get.id,
        "Parent_Child", isCurrent = true,
        propertyRecords.find(_.name equals "weight").get.id))

    DatabaseInfo.db.run(PeopleSystempropertydynamiccrossref.forceInsertAll(peopleSystempropertydynamiccrossrefRows))

    val peopleSystemTypeRows = Seq(
      PeopleSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        systemTypes.find(_.name equals "male").get.id, "Is", isCurrent = true))

    DatabaseInfo.db.run(PeopleSystemtypecrossref.forceInsertAll(peopleSystemTypeRows))

    // events Property/type Relationships

    val eventsSystempropertystaticcrossrefRows = Seq(
      EventsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "driving").get.id,
        systemProperties.find(_.name equals "cars speed").get.id,
        dataRecords.find(_.name equals "car journey").get.id,
        dataFields.find(_.name equals "cars speed").get.id,
        "Parent_Child", isCurrent = true,
        propertyRecords.find(_.name equals "speed of car").get.id))

    DatabaseInfo.db.run(EventsSystempropertystaticcrossref.forceInsertAll(eventsSystempropertystaticcrossrefRows))

    val eventsSystempropertydynamiccrossrefRows = Seq(
      EventsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "driving").get.id,
        systemProperties.find(_.name equals "cars speed").get.id,
        dataFields.find(_.name equals "cars speed").get.id,
        "Parent_Child", isCurrent = true,
        propertyRecords.find(_.name equals "speed of car").get.id))

    DatabaseInfo.db.run(EventsSystempropertydynamiccrossref.forceInsertAll(eventsSystempropertydynamiccrossrefRows))

    val eventsSystemTypeRows = Seq(
      EventsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "having a shower").get.id,
        systemTypes.find(_.name equals "dayily activities").get.id, "Is", isCurrent = true))

    DatabaseInfo.db.run(EventsSystemtypecrossref.forceInsertAll(eventsSystemTypeRows))

    // organisation Property/type Relationships

    val organisationsSystempropertystaticcrossrefRows = Seq(
      OrganisationsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        systemProperties.find(_.name equals "employees").get.id,
        dataRecords.find(_.name equals "FacebookMe").get.id,
        dataFields.find(_.name equals "cars speed").get.id,
        "Parent_Child", isCurrent = true,
        propertyRecords.find(_.name equals "speed of car").get.id))

    DatabaseInfo.db.run(OrganisationsSystempropertystaticcrossref.forceInsertAll(organisationsSystempropertystaticcrossrefRows))

    val organisationsSystempropertydynamiccrossrefRows = Seq(
      OrganisationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        systemProperties.find(_.name equals "employees").get.id,
        dataFields.find(_.name equals "number of employees").get.id,
        "Parent_Child", isCurrent = true,
        propertyRecords.find(_.name equals "number of employees").get.id))

    DatabaseInfo.db.run(OrganisationsSystempropertydynamiccrossref.forceInsertAll(organisationsSystempropertydynamiccrossrefRows))

    val organisationsSystemTypeRows = Seq(
      OrganisationsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        systemTypes.find(_.name equals "department").get.id, "Is", isCurrent = true))

    DatabaseInfo.db.run(OrganisationsSystemtypecrossref.forceInsertAll(organisationsSystemTypeRows))
  }

  def contextlessBundleContext = {
      val dataTableRows = Seq(
        new DataTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "kitchen", "bundlefibaro"),
        new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "kitchenElectricity", "bundlefibaro"),
        new DataTableRow(4, LocalDateTime.now(), LocalDateTime.now(), "event", "bundlefacebook"))

      val dataTableCrossrefs = Seq(
        new DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "contains", 2, 3))

      val dataFieldRows = Seq(
        new DataFieldRow(10, LocalDateTime.now(), LocalDateTime.now(), "timestamp", 3),
        new DataFieldRow(11, LocalDateTime.now(), LocalDateTime.now(), "value", 3),
        new DataFieldRow(12, LocalDateTime.now(), LocalDateTime.now(), "name", 4),
        new DataFieldRow(13, LocalDateTime.now(), LocalDateTime.now(), "location", 4),
        new DataFieldRow(14, LocalDateTime.now(), LocalDateTime.now(), "startTime", 4),
        new DataFieldRow(15, LocalDateTime.now(), LocalDateTime.now(), "endTime", 4))

      val dataRecordRows = Seq(
        new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 1"),
        new DataRecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 2"),
        new DataRecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 3"),
        new DataRecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "event record 1"),
        new DataRecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "event record 2"),
        new DataRecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "event record 3"))

      val dataValues = Seq(
        new DataValueRow(1, LocalDateTime.now(), LocalDateTime.now(), "kitchen time 1", 10, 1),
        new DataValueRow(2, LocalDateTime.now(), LocalDateTime.now(), "kitchen value 1", 11, 1),

        new DataValueRow(3, LocalDateTime.now(), LocalDateTime.now(), "kitchen time 2", 10, 2),
        new DataValueRow(4, LocalDateTime.now(), LocalDateTime.now(), "kitchen value 2", 11, 2),

        new DataValueRow(5, LocalDateTime.now(), LocalDateTime.now(), "kitchen time 3", 10, 3),
        new DataValueRow(6, LocalDateTime.now(), LocalDateTime.now(), "kitchen value 3", 11, 3),

        new DataValueRow(7, LocalDateTime.now(), LocalDateTime.now(), "event name 1", 12, 4),
        new DataValueRow(8, LocalDateTime.now(), LocalDateTime.now(), "event location 1", 13, 4),
        new DataValueRow(9, LocalDateTime.now(), LocalDateTime.now(), "event startTime 1", 14, 4),
        new DataValueRow(10, LocalDateTime.now(), LocalDateTime.now(), "event endTime 1", 15, 4),

        new DataValueRow(11, LocalDateTime.now(), LocalDateTime.now(), "event name 2", 12, 5),
        new DataValueRow(12, LocalDateTime.now(), LocalDateTime.now(), "event location 2", 13, 5),
        new DataValueRow(13, LocalDateTime.now(), LocalDateTime.now(), "event startTime 2", 14, 5),
        new DataValueRow(14, LocalDateTime.now(), LocalDateTime.now(), "event endTime 2", 15, 5),

        new DataValueRow(15, LocalDateTime.now(), LocalDateTime.now(), "event name 3", 12, 6),
        new DataValueRow(16, LocalDateTime.now(), LocalDateTime.now(), "event location 3", 13, 6),
        new DataValueRow(17, LocalDateTime.now(), LocalDateTime.now(), "event startTime 3", 14, 6),
        new DataValueRow(18, LocalDateTime.now(), LocalDateTime.now(), "event endTime 3", 15, 6))

    def restartSequences: DBIO[Int] =
      sqlu"""
         |ALTER SEQUENCE hat.data_table_id_seq RESTART WITH 10;
         |ALTER SEQUENCE hat.data_field_id_seq RESTART WITH 20;
         |ALTER SEQUENCE hat.data_record_id_seq RESTART WITH 10;
         |ALTER SEQUENCE hat.data_tabletotablecrossref_id_seq RESTART WITH 2;
         |ALTER SEQUENCE hat.data_value_id_seq RESTART WITH 20;
          """

      DatabaseInfo.db.run {
        DBIO.seq(
          DataTable.forceInsertAll(dataTableRows),
          DataTabletotablecrossref.forceInsertAll(dataTableCrossrefs),
          DataField.forceInsertAll(dataFieldRows),
          DataRecord.forceInsertAll(dataRecordRows),
          // Don't _foce_ insert all data values -- IDs don't particularly matter to us
          DataValue.forceInsertAll(dataValues),
          restartSequences
        ).asTry
      }
  }
}
