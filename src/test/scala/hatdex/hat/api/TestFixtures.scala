package hatdex.hat.api

import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

object TestFixtures {
  def prepareDataStructures(implicit session: Session) = {
    // Data tables
    val dataTables = Seq(
      new DataTableRow(1, LocalDateTime.now(), LocalDateTime.now(), "Facebook", "facebook"),
      new DataTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "events", "facebook"),
      new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "me", "facebook"),
      new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "Fibaro", "Fibaro"),
      new DataTableRow(4, LocalDateTime.now(), LocalDateTime.now(), "Fitbit", "Fitbit"),
      new DataTableRow(5, LocalDateTime.now(), LocalDateTime.now(), "locations", "facebook"))

    DataTable.forceInsertAll(dataTables: _*)

    val facebookTableId = dataTables.find(_.name equals "Facebook").get.id
    val eventsTableId = dataTables.find(_.name equals "events").get.id
    val meTableId = dataTables.find(_.name equals "me").get.id
    val locationsTableId = dataTables.find(_.name equals "locations").get.id
    val fibaroTableId = dataTables.find(_.name equals "Fibaro").get.id
    val fitbitTableId = dataTables.find(_.name equals "Fitbit").get.id

    // Nesting of data tables
    val dataTableToTableCrossRefRows = Seq(
      new DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", facebookTableId, eventsTableId),
      new DataTabletotablecrossrefRow(2, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", facebookTableId, locationsTableId),
      new DataTabletotablecrossrefRow(3, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", facebookTableId, meTableId))

    DataTabletotablecrossref.forceInsertAll(dataTableToTableCrossRefRows: _*)

    // Adding data fields to data tables
    val dataFields = Seq(
      new DataFieldRow(1, LocalDateTime.now(), LocalDateTime.now(), "weight", meTableId),
      new DataFieldRow(2, LocalDateTime.now(), LocalDateTime.now(), "elevation", locationsTableId),
      new DataFieldRow(3, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", eventsTableId),
      new DataFieldRow(4, LocalDateTime.now(), LocalDateTime.now(), "size", fibaroTableId),
      new DataFieldRow(5, LocalDateTime.now(), LocalDateTime.now(), "temperature", fibaroTableId),
      new DataFieldRow(6, LocalDateTime.now(), LocalDateTime.now(), "cars speed", fibaroTableId),
      new DataFieldRow(6, LocalDateTime.now(), LocalDateTime.now(), "number of employees", facebookTableId))

    DataField.forceInsertAll(dataFields: _*)

    val weightFieldId = dataTables.find(_.name equals "weight").get.id
    val elevationFieldId = dataTables.find(_.name equals "elevation").get.id
    val kichenElectricityFieldId = dataTables.find(_.name equals "kichenElectricity").get.id
    val sizeFieldId = dataTables.find(_.name equals "size").get.id
    val temperatureFieldId = dataTables.find(_.name equals "temperature").get.id

    // Data records to connect data items together
    val dataRecords = Seq(
      new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "FacebookMe"),
      new DataRecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "FacebookLocation"),
      new DataRecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "FibaroKitchen"),
      new DataRecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent"),
      new DataRecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "FibaroBathroom"),
      new DataRecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "car journey")
    )

    DataRecord.forceInsertAll(dataRecords: _*)

    val facebookMeRecordId = dataTables.find(_.name equals "FacebookMe").get.id
    val facebookLocationRecordId = dataTables.find(_.name equals "FacebookLocation").get.id
    val fibaroKitchenRecordId = dataTables.find(_.name equals "FibaroKitchen").get.id
    val facebookEventRecordId = dataTables.find(_.name equals "FacebookEvent").get.id
    val fibaroBathroomRecordId = dataTables.find(_.name equals "FibaroBathroom").get.id

    val dataValueRows = Seq(
      new DataValueRow(1, LocalDateTime.now(), LocalDateTime.now(), "62", weightFieldId, facebookMeRecordId),
      new DataValueRow(2, LocalDateTime.now(), LocalDateTime.now(), "300", elevationFieldId, facebookLocationRecordId),
      new DataValueRow(3, LocalDateTime.now(), LocalDateTime.now(), "20", kichenElectricityFieldId, fibaroKitchenRecordId),
      new DataValueRow(4, LocalDateTime.now(), LocalDateTime.now(), "Having a Shower", facebookEventRecordId, fibaroBathroomRecordId),
      new DataValueRow(5, LocalDateTime.now(), LocalDateTime.now(), "25", temperatureFieldId, facebookEventRecordId))

    DataValue.forceInsertAll(dataValueRows: _*)

    prepareContextualStructures(dataFields, dataRecords)
  }

  def prepareContextualStructures(dataFields: Seq[DataFieldRow], dataRecords: Seq[DataRecordRow])
                                 (implicit sesion: Session) = {
    // contextualisation tools
    val systemUOMs = Seq(
      new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "meters", Some( "distance measurement"), Some("m")),
      new SystemUnitofmeasurementRow(2, LocalDateTime.now(), LocalDateTime.now(), "kilograms", Some( "weight measurement"), Some("kg")),
      new SystemUnitofmeasurementRow(3, LocalDateTime.now(), LocalDateTime.now(), "meters cubed", Some( "3d space"), Some("m^3")),
      new SystemUnitofmeasurementRow(4, LocalDateTime.now(), LocalDateTime.now(), "Kilowatt hours", Some("electricity measurement"), Some("KwH")),
      new SystemUnitofmeasurementRow(5, LocalDateTime.now(), LocalDateTime.now(), "centigrade", Some("heat measurement"), Some("C")),
      new SystemUnitofmeasurementRow(6, LocalDateTime.now(), LocalDateTime.now(), "miles per hour", Some("speed measurement"), Some("mph")),
      new SystemUnitofmeasurementRow(7, LocalDateTime.now(), LocalDateTime.now(), "number", Some("amount of something"), Some("n")))

    SystemUnitofmeasurement.forceInsertAll(systemUOMs: _*)

    val systemTypes = Seq(
      new SystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "room dimensions", Some("Fibaro")),
      new SystemTypeRow(2, LocalDateTime.now(), LocalDateTime.now(), "dayily activities", Some("Google Calendar")),
      new SystemTypeRow(3, LocalDateTime.now(), LocalDateTime.now(), "utilities", Some("Fibaro")),
      new SystemTypeRow(4, LocalDateTime.now(), LocalDateTime.now(), "personattributes", Some("Fitbit")),
      new SystemTypeRow(5, LocalDateTime.now(), LocalDateTime.now(), "locationattributes", Some("GPS application")),
      new SystemTypeRow(6, LocalDateTime.now(), LocalDateTime.now(), "measurement", Some("measurement of something")),
      new SystemTypeRow(7, LocalDateTime.now(), LocalDateTime.now(), "room", Some("building seperator")),
      new SystemTypeRow(8, LocalDateTime.now(), LocalDateTime.now(), "vehicle", Some("type of transport")),
      new SystemTypeRow(9, LocalDateTime.now(), LocalDateTime.now(), "male", Some("gender type")),
      new SystemTypeRow(10, LocalDateTime.now(), LocalDateTime.now(), "department", Some("faculty of a university")))

    SystemType.forceInsertAll(systemTypes: _*)

    // Entities
    val things = Seq(
      new ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "cupbord"),
      new ThingsThingRow(2, LocalDateTime.now(), LocalDateTime.now(), "car"),
      new ThingsThingRow(3, LocalDateTime.now(), LocalDateTime.now(), "shower"),
      new ThingsThingRow(4, LocalDateTime.now(), LocalDateTime.now(), "scales"))

    ThingsThing.forceInsertAll(things: _*)

    val people = Seq(
      new PeoplePersonRow(1, LocalDateTime.now(), LocalDateTime.now(), "Martin", "martinID"),
      new PeoplePersonRow(2, LocalDateTime.now(), LocalDateTime.now(), "Andrius", "andriusID"),
      new PeoplePersonRow(3, LocalDateTime.now(), LocalDateTime.now(), "Xiao", "xiaoID"))

    PeoplePerson.forceInsertAll(people: _*)

    val locations = Seq(
      new LocationsLocationRow(1, LocalDateTime.now(), LocalDateTime.now(), "kitchen"),
      new LocationsLocationRow(2, LocalDateTime.now(), LocalDateTime.now(), "bathroom"),
      new LocationsLocationRow(3, LocalDateTime.now(), LocalDateTime.now(), "WMG"))

    LocationsLocation.forceInsertAll(locations: _*)

    val organisations = Seq(
      new OrganisationsOrganisationRow(1, LocalDateTime.now(), LocalDateTime.now(), "seventrent"),
      new OrganisationsOrganisationRow(2, LocalDateTime.now(), LocalDateTime.now(), "WMG"),
      new OrganisationsOrganisationRow(3, LocalDateTime.now(), LocalDateTime.now(), "Coventry")
    )

    OrganisationsOrganisation.forceInsertAll(organisations: _*)

    val events = Seq(
      new EventsEventRow(1, LocalDateTime.now(), LocalDateTime.now(), "having a shower"),
      new EventsEventRow(2, LocalDateTime.now(), LocalDateTime.now(), "driving"),
      new EventsEventRow(3, LocalDateTime.now(), LocalDateTime.now(), "going to work"),
      new EventsEventRow(4, LocalDateTime.now(), LocalDateTime.now(), "cooking")
    )

    EventsEvent.forceInsertAll(events: _*)

    var entityId = 1
    val entities = things.map { thing =>
      entityId += 1
      new EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), thing.name, "thing", None, Some(thing.id), None, None, None)
    } ++ people.map { person =>
      entityId += 1
      new EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), person.name, "person", None, None, None, None, Some(person.id))
    } ++ locations.map { location =>
      entityId += 1
      new EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), location.name, "location", Some(location.id), None, None, None, None)
    } ++ organisations.map { organisation =>
      entityId += 1
      new EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), organisation.name, "organisation", None, None, None, Some(organisation.id), None)
    } ++ events.map { event =>
      entityId += 1
      new EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), event.name, "event", None, None, Some(event.id), None, None)
    }

    Entity.forceInsertAll(entities: _*)

    contextualiseEntities(systemUOMs, systemTypes, things, people, locations,
      organisations, events, entities, dataFields, dataRecords)
  }

  def contextualiseEntities(systemUOMs: Seq[SystemUnitofmeasurementRow], systemTypes: Seq[SystemTypeRow],
                            things: Seq[ThingsThingRow], people: Seq[PeoplePersonRow], locations: Seq[LocationsLocationRow],
                            organisations: Seq[OrganisationsOrganisationRow], events: Seq[EventsEventRow],
                            entities: Seq[EntityRow], dataFields: Seq[DataFieldRow], dataRecords: Seq[DataRecordRow])
                           (implicit session: Session) = {

    // Relationship Record

    val relationshipRecords = Seq(
      new SystemRelationshiprecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "Driving to Work"),
      new SystemRelationshiprecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "Shower Used_During having a shower"),
      new SystemRelationshiprecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "having a shower Is_At bathrom"),
      new SystemRelationshiprecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "Uses_Utility"),
      new SystemRelationshiprecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child"),
      new SystemRelationshiprecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "Owns"),
      new SystemRelationshiprecordRow(7, LocalDateTime.now(), LocalDateTime.now(), "Next_To"),
      new SystemRelationshiprecordRow(8, LocalDateTime.now(), LocalDateTime.now(), "Buys_From"),
      new SystemRelationshiprecordRow(9, LocalDateTime.now(), LocalDateTime.now(), "Car Ownership"),
      new SystemRelationshiprecordRow(10, LocalDateTime.now(), LocalDateTime.now(), "Martin Is having a shower"),
      new SystemRelationshiprecordRow(11, LocalDateTime.now(), LocalDateTime.now(), "Going to Organisation WMG"),
      new SystemRelationshiprecordRow(12, LocalDateTime.now(), LocalDateTime.now(), "Shower Parent_Child cupbord"),
      new SystemRelationshiprecordRow(13, LocalDateTime.now(), LocalDateTime.now(), "Martin Owns Car"),
      new SystemRelationshiprecordRow(14, LocalDateTime.now(), LocalDateTime.now(), "Kitchen Next_To bathroom"),
      new SystemRelationshiprecordRow(15, LocalDateTime.now(), LocalDateTime.now(), "WMG location Is_At Coventry"),
      new SystemRelationshiprecordRow(16, LocalDateTime.now(), LocalDateTime.now(), "WMG Buys_From seventrent"),
      new SystemRelationshiprecordRow(17, LocalDateTime.now(), LocalDateTime.now(), "WMG organisation Is_At Coventry"),
      new SystemRelationshiprecordRow(18, LocalDateTime.now(), LocalDateTime.now(), "WMG organisation Rents car"),
      new SystemRelationshiprecordRow(19, LocalDateTime.now(), LocalDateTime.now(), "Martin Works at WMG"),
      new SystemRelationshiprecordRow(20, LocalDateTime.now(), LocalDateTime.now(), "Martin Colleague_With Andrius"),
      new SystemRelationshiprecordRow(21, LocalDateTime.now(), LocalDateTime.now(), "Xiao Is_at Coventry"),
      new SystemRelationshiprecordRow(22, LocalDateTime.now(), LocalDateTime.now(), "size"),
      new SystemRelationshiprecordRow(23, LocalDateTime.now(), LocalDateTime.now(), "Car Ownership"),
      new SystemRelationshiprecordRow(24, LocalDateTime.now(), LocalDateTime.now(), "elevation"),
      new SystemRelationshiprecordRow(25, LocalDateTime.now(), LocalDateTime.now(), "Driving to Work"),
      new SystemRelationshiprecordRow(26, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      new SystemRelationshiprecordRow(27, LocalDateTime.now(), LocalDateTime.now(), "size"),
      new SystemRelationshiprecordRow(28, LocalDateTime.now(), LocalDateTime.now(), "weight")
    )

    SystemRelationshiprecord.forceInsertAll(relationshipRecords: _*)

    // Event Relationships

    val eventsEventToEventCrossRefRows = Seq(
      new EventsEventtoeventcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "going to work").get.id,
        events.find(_.name equals "driving").get.id,
        "Driving to work", true,
        relationshipRecords.find(_.name equals "Driving to Work").get.id)
    )
    EventsEventtoeventcrossref.forceInsertAll(eventsEventToEventCrossRefRows: _*)

    val eventsEventtothingcrossrefRows = Seq(
      new EventsEventthingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "having a shower").get.id,
        things.find(_.name equals "shower").get.id,
        "Used_During", true,
        relationshipRecords.find(_.name equals "Shower Used_During having a shower").get.id)
    )


    EventsEventthingcrossref.forceInsertAll(eventsEventtothingcrossrefRows: _*)

    val eventsEventToLocationCrossRefRows = Seq(
      new EventsEventlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "having a shower").get.id,
        locations.find(_.name equals "bathroom").get.id,
        "Is_At", true,
        relationshipRecords.find(_.name equals "having a shower Is_At bathrom").get.id)
    )


    EventsEventlocationcrossref.forceInsertAll(eventsEventToLocationCrossRefRows: _*)

    val eventsEventToPersonCrossRefRows = Seq(
      new EventsEventpersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "having a shower").get.id,
        people.find(_.name equals "Martin").get.id,
        "Is", true,
        relationshipRecords.find(_.name equals "Martin Is having a shower").get.id)
    )


    EventsEventpersoncrossref.forceInsertAll(eventsEventToPersonCrossRefRows: _*)

    val eventsEventToOrganisationCrossRefRows = Seq(
      new EventsEventorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "going to work").get.id,
        organisations.find(_.name equals "WMG").get.id,
        "Going to Organisation", true,
        relationshipRecords.find(_.name equals "Going to Organisation WMG").get.id)
    )


    EventsEventorganisationcrossref.forceInsertAll(eventsEventToOrganisationCrossRefRows: _*)

    //  Thing Relationships

    val thingsThingtothingcrossrefRows = Seq(
      new ThingsThingtothingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "cupbord").get.id,
        things.find(_.name equals "shower").get.id,
        "Parent_Child", true,
        relationshipRecords.find(_.name equals "Shower Parent_Child cupbord").get.id)
    )

    ThingsThingtothingcrossref.forceInsertAll(thingsThingtothingcrossrefRows: _*)

    val thingsThingToPersonCrossRefRows = Seq(
      new ThingsThingpersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "car").get.id,
        people.find(_.name equals "Martin").get.id,
        "Owns", true,
        relationshipRecords.find(_.name equals "Martin Owns Car").get.id)
    )


    ThingsThingpersoncrossref.forceInsertAll(thingsThingToPersonCrossRefRows: _*)

    // Location Relationships

    val locationsLocationToLocationCrossRefRows = Seq(
      new LocationsLocationtolocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "kitchen").get.id,
        locations.find(_.name equals "bathroom").get.id,
        "Next_To", true,
        relationshipRecords.find(_.name equals "Kitchen Next_To bathroom").get.id)
    )

    LocationsLocationtolocationcrossref.forceInsertAll(locationsLocationToLocationCrossRefRows: _*)

    val locationsLocationtothingcrossrefRows = Seq(
      new LocationsLocationthingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "WMG").get.id,
        locations.find(_.name equals "Coventry").get.id,
        "Is_At", true,
        relationshipRecords.find(_.name equals "WMG location Is_At Coventry").get.id)
    )

    LocationsLocationthingcrossref.forceInsertAll(locationsLocationtothingcrossrefRows: _*)


    // Organisation Relationships

    val organisationsOrganisationToOrganisationCrossRefRows = Seq(
      new OrganisationsOrganisationtoorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "seventrent").get.id,
        organisations.find(_.name equals "WMG").get.id,
        "Buys_From", true,
        relationshipRecords.find(_.name equals "WMG Buys_From seventrent").get.id)
    )

    OrganisationsOrganisationtoorganisationcrossref.forceInsertAll(organisationsOrganisationToOrganisationCrossRefRows: _*)


    val organisationOrganisationLocationCrossRefRows = Seq(
      new OrganisationsOrganisationlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        locations.find(_.name equals "Coventry").get.id,
        "Is_At", true,
        relationshipRecords.find(_.name equals "WMG organisation Is_At Coventry").get.id)
    )


    OrganisationsOrganisationlocationcrossref.forceInsertAll(organisationOrganisationLocationCrossRefRows: _*)


    val organisationOrganisationThingCrossRefRows = Seq(
      new OrganisationsOrganisationthingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        things.find(_.name equals "car").get.id,
        "Rents", true,
        relationshipRecords.find(_.name equals "WMG organisation Rents car").get.id)
    )


    OrganisationsOrganisationthingcrossref.forceInsertAll(organisationOrganisationThingCrossRefRows: _*)

    //People Relationships

    val personRelationshipTypes = Seq (
      new PeoplePersontopersonrelationshiptypeRow(1, LocalDateTime.now(), LocalDateTime.now(),
        "Colleague With", Some("Working Together"))
    )

    PeoplePersontopersonrelationshiptype.forceInsertAll(personRelationshipTypes: _*)

    val peoplePersonToPersonCrossRefRows = Seq(
      new PeoplePersontopersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        people.find(_.name equals "Andrius").get.id,
        personRelationshipTypes.find(_.name equals "Working Together").get.id,
        true,
        relationshipRecords.find(_.name equals "Martin Colleague_With Andrius").get.id)
    )

    PeoplePersontopersoncrossref.forceInsertAll(peoplePersonToPersonCrossRefRows: _*)

    val peoplePersonOrganisationCrossRefRows = Seq(
      new PeoplePersonorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        organisations.find(_.name equals "WMG").get.id,
        "Works_at", true,
        relationshipRecords.find(_.name equals "Martin Works at WMG").get.id)
    )

    val peoplePersonLocationCrossRefCrossRefRows = Seq(
      new PeoplePersonlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Xiao").get.id,
        locations.find(_.name equals "Coventry").get.id,
        "Is_at", true,
        relationshipRecords.find(_.name equals "Xiao Is_at Coventry").get.id)
    )

    PeoplePersonlocationcrossref.forceInsertAll(peoplePersonLocationCrossRefCrossRefRows: _*)

    linkEntityData(systemUOMs, systemTypes, things, people, locations,
      organisations, events, entities, dataFields, dataRecords)
  }

  def linkEntityData(systemUOMs: Seq[SystemUnitofmeasurementRow], systemTypes: Seq[SystemTypeRow],
                     things: Seq[ThingsThingRow], people: Seq[PeoplePersonRow], locations: Seq[LocationsLocationRow],
                     organisations: Seq[OrganisationsOrganisationRow], events: Seq[EventsEventRow],
                     entities: Seq[EntityRow], dataFields: Seq[DataFieldRow], dataRecords: Seq[DataRecordRow])
                    (implicit session: Session) = {
    // Entity - Property linking

    val systemProperties = Seq(
      new SystemPropertyRow(1, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", Some("Electricity use in a kitchen"),
        systemTypes.find(_.name equals "utilities").get.id,
        systemUOMs.find(_.name equals "Kilowatt hours").get.id),
      new SystemPropertyRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse", Some("Use of a shower"),
        systemTypes.find(_.name equals "utilities").get.id,
        systemUOMs.find(_.name equals "Kilowatt hours").get.id),
      new SystemPropertyRow(3, LocalDateTime.now(), LocalDateTime.now(), "size", Some("Size of an object"),
        systemTypes.find(_.name equals "measurement").get.id,
        systemUOMs.find(_.name equals "meters cubed").get.id),
      new SystemPropertyRow(4, LocalDateTime.now(), LocalDateTime.now(), "elevation", Some("Height of location or thing"),
        systemTypes.find(_.name equals "measurement").get.id,
        systemUOMs.find(_.name equals "meters").get.id),
      new SystemPropertyRow(5, LocalDateTime.now(), LocalDateTime.now(), "temperature", Some("Current temperature"),
        systemTypes.find(_.name equals "measurement").get.id,
        systemUOMs.find(_.name equals "centigrade").get.id),
      new SystemPropertyRow(6, LocalDateTime.now(), LocalDateTime.now(), "weight", Some("Weight of a person"),
        systemTypes.find(_.name equals "measurement").get.id,
        systemUOMs.find(_.name equals "kilograms").get.id),
      new SystemPropertyRow(7, LocalDateTime.now(), LocalDateTime.now(), "cars speed", Some("Car speed"),
        systemTypes.find(_.name equals "measurement").get.id,
        systemUOMs.find(_.name equals "miles per hour").get.id),
      new SystemPropertyRow(8, LocalDateTime.now(), LocalDateTime.now(), "employees", Some("number of employees"),
        systemTypes.find(_.name equals "measurement").get.id,
        systemUOMs.find(_.name equals "number").get.id)
    )

    SystemProperty.forceInsertAll(systemProperties: _*)

    val propertyRecords = Seq(
      new SystemPropertyrecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity"),
      new SystemPropertyrecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      new SystemPropertyrecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "size"),
      new SystemPropertyrecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "weight"),
      new SystemPropertyrecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "elevation"),
      new SystemPropertyrecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "city size"),
      new SystemPropertyrecordRow(7, LocalDateTime.now(), LocalDateTime.now(), "temperature"),
      new SystemPropertyrecordRow(8, LocalDateTime.now(), LocalDateTime.now(), "speed of car"),
      new SystemPropertyrecordRow(9, LocalDateTime.now(), LocalDateTime.now(), "number of employees")
    )

    SystemPropertyrecord.forceInsertAll(propertyRecords: _*)


    // location Property/type Relationships


    val locationsSystempropertydynamiccrossrefRows = Seq(
      new LocationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "Coventry").get.id,
        systemProperties.find(_.name equals "size").get.id,
        dataFields.find(_.name equals "size").get.id,
        "Size of City Location", true,
        propertyRecords.find(_.name equals "city size").get.id)
    )


    LocationsSystempropertydynamiccrossref.forceInsertAll(locationsSystempropertydynamiccrossrefRows: _*)

    val locationsSystempropertystaticcrossrefRows = Seq(
      new LocationsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "Coventry").get.id,
        systemProperties.find(_.name equals "size").get.id,
        dataRecords.find(_.name equals "FacebookLocation").get.id,
        dataFields.find(_.name equals "size").get.id,
        "Size of City Location", true,
        propertyRecords.find(_.name equals "city size").get.id)
    )


    LocationsSystempropertystaticcrossref.forceInsertAll(locationsSystempropertystaticcrossrefRows: _*)

    val locationSystemTypes = Seq(
      new LocationsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "bathroom").get.id,
        systemTypes.find(_.name equals "room").get.id, "Is", true)
    )

    LocationsSystemtypecrossref.forceInsertAll(locationSystemTypes: _*)

    // things Property/type Relationships

    val thingsSystempropertystaticcrossrefRows = Seq(
      new ThingsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "Coventry").get.id,
        systemProperties.find(_.name equals "size").get.id,
        dataRecords.find(_.name equals "FacebookLocation").get.id,
        dataFields.find(_.name equals "size").get.id,
        "Parent Child", true,
        propertyRecords.find(_.name equals "city size").get.id))



    ThingsSystempropertystaticcrossref.forceInsertAll(thingsSystempropertystaticcrossrefRows: _*)

    val thingsSystempropertydynamiccrossrefRows = Seq(
      new ThingsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "car").get.id,
        systemProperties.find(_.name equals "temperature").get.id,
        dataFields.find(_.name equals "temperature").get.id,
        "Parent Child", true,
        propertyRecords.find(_.name equals "temperature").get.id))


    ThingsSystempropertydynamiccrossref.forceInsertAll(thingsSystempropertydynamiccrossrefRows: _*)

    val thingsSystemtypecrossrefRows = Seq(
      new ThingsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "car").get.id,
        systemTypes.find(_.name equals "vehicle").get.id, "Is", true))

    ThingsSystemtypecrossref.forceInsertAll(thingsSystemtypecrossrefRows: _*)

    // people Property/type Relationships

    val peopleSystempropertystaticcrossrefRows = Seq(
      new PeopleSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "martin").get.id,
        systemProperties.find(_.name equals "kilograms").get.id,
        dataRecords.find(_.name equals "FibaroBathroom").get.id,
        dataFields.find(_.name equals "weight").get.id,
        "Parent_Child", true,
        propertyRecords.find(_.name equals "weight").get.id))


    PeopleSystempropertystaticcrossref.forceInsertAll(peopleSystempropertystaticcrossrefRows: _*)

    val peopleSystempropertydynamiccrossrefRows = Seq(
      new PeopleSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "martin").get.id,
        systemProperties.find(_.name equals "kilograms").get.id,
        dataFields.find(_.name equals "weight").get.id,
        "Parent_Child", true,
        propertyRecords.find(_.name equals "weight").get.id))



    PeopleSystempropertydynamiccrossref.forceInsertAll(peopleSystempropertydynamiccrossrefRows: _*)

    val peopleSystemTypeRows = Seq(
      new PeopleSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        systemTypes.find(_.name equals "male").get.id, "Is", true))

    PeopleSystemtypecrossref.forceInsertAll(peopleSystemTypeRows: _*)

    // events Property/type Relationships

    val eventsSystempropertystaticcrossrefRows = Seq(
      new EventsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "driving").get.id,
        systemProperties.find(_.name equals "cars speed").get.id,
        dataRecords.find(_.name equals "car journey").get.id,
        dataFields.find(_.name equals "cars speed").get.id,
        "Parent_Child", true,
        propertyRecords.find(_.name equals "speed of car").get.id))


    EventsSystempropertystaticcrossref.forceInsertAll(eventsSystempropertystaticcrossrefRows: _*)

    val eventsSystempropertydynamiccrossrefRows = Seq(
      new EventsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "driving").get.id,
        systemProperties.find(_.name equals "cars speed").get.id,
        dataFields.find(_.name equals "cars speed").get.id,
        "Parent_Child", true,
        propertyRecords.find(_.name equals "speed of car").get.id))


    EventsSystempropertydynamiccrossref.forceInsertAll(eventsSystempropertydynamiccrossrefRows: _*)

    val eventsSystemTypeRows = Seq(
      new EventsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "having a shower").get.id,
        systemTypes.find(_.name equals "dayily activities").get.id, "Is", true))

    EventsSystemtypecrossref.forceInsertAll(eventsSystemTypeRows: _*)

    // organisation Property/type Relationships

    val organisationsSystempropertystaticcrossrefRows = Seq(
      new OrganisationsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        systemProperties.find(_.name equals "employees").get.id,
        dataRecords.find(_.name equals "FacebookMe").get.id,
        dataFields.find(_.name equals "cars speed").get.id,
        "Parent_Child", true,
        propertyRecords.find(_.name equals "speed of car").get.id))


    OrganisationsSystempropertystaticcrossref.forceInsertAll(organisationsSystempropertystaticcrossrefRows: _*)

    val organisationsSystempropertydynamiccrossrefRows = Seq(
      new OrganisationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        systemProperties.find(_.name equals "employees").get.id,
        dataFields.find(_.name equals "number of employees").get.id,
        "Parent_Child", true,
        propertyRecords.find(_.name equals "number of employees").get.id))

    OrganisationsSystempropertydynamiccrossref.forceInsertAll(organisationsSystempropertydynamiccrossrefRows: _*)

    val organisationsSystemTypeRows = Seq(
      new OrganisationsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        systemTypes.find(_.name equals "department").get.id, "Is", true))

    OrganisationsSystemtypecrossref.forceInsertAll(organisationsSystemTypeRows: _*)
  }
}
