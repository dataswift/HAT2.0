package hatdex.hat.api

import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

object TestFixtures {
  def prepareEverything(implicit session: Session) = {
    val (dataFields, dataRecords) = prepareDataStructures
    val (systemUoms, systemTypes) = prepareSystemTypes
    val systemProperties = prepareSystemProperties(systemUoms, systemTypes)
    val (things, people, locations, organisations, events, entities) = prepareEntities
    contextualiseEntities(things, people, locations, organisations, events)
    linkEntityData(things, people, locations, organisations, events, systemProperties, systemTypes, dataFields, dataRecords)
  }
  def prepareDataStructures(implicit session: Session) = {
    // Data tables
    val dataTables = Seq(
      DataTableRow(1, LocalDateTime.now(), LocalDateTime.now(), "Facebook", "facebook"),
      DataTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "events", "facebook"),
      DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "me", "facebook"),
      DataTableRow(4, LocalDateTime.now(), LocalDateTime.now(), "Fibaro", "Fibaro"),
      DataTableRow(5, LocalDateTime.now(), LocalDateTime.now(), "Fitbit", "Fitbit"),
      DataTableRow(6, LocalDateTime.now(), LocalDateTime.now(), "locations", "facebook"))

    DataTable.forceInsertAll(dataTables: _*)

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

    DataTabletotablecrossref.forceInsertAll(dataTableToTableCrossRefRows: _*)

    // Adding data fields to data tables
    val dataFields = Seq(
      DataFieldRow(1, LocalDateTime.now(), LocalDateTime.now(), "weight", meTableId),
      DataFieldRow(2, LocalDateTime.now(), LocalDateTime.now(), "elevation", locationsTableId),
      DataFieldRow(3, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", eventsTableId),
      DataFieldRow(4, LocalDateTime.now(), LocalDateTime.now(), "size", fibaroTableId),
      DataFieldRow(5, LocalDateTime.now(), LocalDateTime.now(), "temperature", fibaroTableId),
      DataFieldRow(6, LocalDateTime.now(), LocalDateTime.now(), "cars speed", fibaroTableId),
      DataFieldRow(7, LocalDateTime.now(), LocalDateTime.now(), "number of employees", facebookTableId))

    DataField.forceInsertAll(dataFields: _*)

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
      DataRecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "car journey")
    )

    DataRecord.forceInsertAll(dataRecords: _*)

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

    DataValue.forceInsertAll(dataValueRows: _*)

    (dataFields, dataRecords)
  }

  def prepareSystemTypes(implicit sesion: Session) = {
    // contextualisation tools
    val systemUOMs = Seq(
      SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "meters", Some( "distance measurement"), Some("m")),
      SystemUnitofmeasurementRow(2, LocalDateTime.now(), LocalDateTime.now(), "kilograms", Some( "weight measurement"), Some("kg")),
      SystemUnitofmeasurementRow(3, LocalDateTime.now(), LocalDateTime.now(), "meters cubed", Some( "3d space"), Some("m^3")),
      SystemUnitofmeasurementRow(4, LocalDateTime.now(), LocalDateTime.now(), "Kilowatt hours", Some("electricity measurement"), Some("KwH")),
      SystemUnitofmeasurementRow(5, LocalDateTime.now(), LocalDateTime.now(), "centigrade", Some("heat measurement"), Some("C")),
      SystemUnitofmeasurementRow(6, LocalDateTime.now(), LocalDateTime.now(), "miles per hour", Some("speed measurement"), Some("mph")),
      SystemUnitofmeasurementRow(7, LocalDateTime.now(), LocalDateTime.now(), "number", Some("amount of something"), Some("n")))

    SystemUnitofmeasurement.forceInsertAll(systemUOMs: _*)

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

    SystemType.forceInsertAll(systemTypes: _*)

    (systemUOMs, systemTypes)
  }

  def prepareSystemProperties(systemUOMs: Seq[SystemUnitofmeasurementRow], systemTypes: Seq[SystemTypeRow])
                             (implicit sesion: Session) = {
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
        systemUOMs.find(_.name equals "number").get.id)
    )

    SystemProperty.forceInsertAll(systemProperties: _*)

    systemProperties
  }

  def prepareEntities(implicit session: Session) = {
    // Entities
    val things = Seq(
      ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "cupbord"),
      ThingsThingRow(2, LocalDateTime.now(), LocalDateTime.now(), "car"),
      ThingsThingRow(3, LocalDateTime.now(), LocalDateTime.now(), "shower"),
      ThingsThingRow(4, LocalDateTime.now(), LocalDateTime.now(), "scales"))

    ThingsThing.forceInsertAll(things: _*)

    val people = Seq(
      PeoplePersonRow(1, LocalDateTime.now(), LocalDateTime.now(), "Martin", "martinID"),
      PeoplePersonRow(2, LocalDateTime.now(), LocalDateTime.now(), "Andrius", "andriusID"),
      PeoplePersonRow(3, LocalDateTime.now(), LocalDateTime.now(), "Xiao", "xiaoID"))

    PeoplePerson.forceInsertAll(people: _*)

    val locations = Seq(
      LocationsLocationRow(1, LocalDateTime.now(), LocalDateTime.now(), "kitchen"),
      LocationsLocationRow(2, LocalDateTime.now(), LocalDateTime.now(), "bathroom"),
      LocationsLocationRow(3, LocalDateTime.now(), LocalDateTime.now(), "WMG"),
      LocationsLocationRow(4, LocalDateTime.now(), LocalDateTime.now(), "Coventry")
    )

    LocationsLocation.forceInsertAll(locations: _*)

    val organisations = Seq(
      OrganisationsOrganisationRow(1, LocalDateTime.now(), LocalDateTime.now(), "seventrent"),
      OrganisationsOrganisationRow(2, LocalDateTime.now(), LocalDateTime.now(), "WMG"),
      OrganisationsOrganisationRow(3, LocalDateTime.now(), LocalDateTime.now(), "Coventry")
    )

    OrganisationsOrganisation.forceInsertAll(organisations: _*)

    val events = Seq(
      EventsEventRow(1, LocalDateTime.now(), LocalDateTime.now(), "having a shower"),
      EventsEventRow(2, LocalDateTime.now(), LocalDateTime.now(), "driving"),
      EventsEventRow(3, LocalDateTime.now(), LocalDateTime.now(), "going to work"),
      EventsEventRow(4, LocalDateTime.now(), LocalDateTime.now(), "cooking")
    )

    EventsEvent.forceInsertAll(events: _*)

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

    Entity.forceInsertAll(entities: _*)

    (things, people, locations, organisations, events, entities)
  }

  def contextualiseEntities(things: Seq[ThingsThingRow], people: Seq[PeoplePersonRow],
                            locations: Seq[LocationsLocationRow], organisations: Seq[OrganisationsOrganisationRow],
                            events: Seq[EventsEventRow])
                           (implicit session: Session) = {

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
      SystemRelationshiprecordRow(29, LocalDateTime.now(), LocalDateTime.now(), "car is at")
    )

    SystemRelationshiprecord.forceInsertAll(relationshipRecords: _*)

    // Event Relationships

    val eventsEventToEventCrossRefRows = Seq(
      EventsEventtoeventcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "going to work").get.id,
        events.find(_.name equals "driving").get.id,
        "Driving to work", true,
        relationshipRecords.find(_.name equals "Driving to Work").get.id)
    )
    EventsEventtoeventcrossref.forceInsertAll(eventsEventToEventCrossRefRows: _*)

    val eventsEventtothingcrossrefRows = Seq(
      EventsEventthingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "having a shower").get.id,
        things.find(_.name equals "shower").get.id,
        "Used_During", true,
        relationshipRecords.find(_.name equals "Shower Used_During having a shower").get.id)
    )


    EventsEventthingcrossref.forceInsertAll(eventsEventtothingcrossrefRows: _*)

    val eventsEventToLocationCrossRefRows = Seq(
      EventsEventlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "having a shower").get.id,
        locations.find(_.name equals "bathroom").get.id,
        "Is_At", true,
        relationshipRecords.find(_.name equals "having a shower Is_At bathrom").get.id)
    )


    EventsEventlocationcrossref.forceInsertAll(eventsEventToLocationCrossRefRows: _*)

    val eventsEventToPersonCrossRefRows = Seq(
      EventsEventpersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "having a shower").get.id,
        people.find(_.name equals "Martin").get.id,
        "Is", true,
        relationshipRecords.find(_.name equals "Martin Is having a shower").get.id)
    )


    EventsEventpersoncrossref.forceInsertAll(eventsEventToPersonCrossRefRows: _*)

    val eventsEventToOrganisationCrossRefRows = Seq(
      EventsEventorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "going to work").get.id,
        organisations.find(_.name equals "WMG").get.id,
        "Going to Organisation", true,
        relationshipRecords.find(_.name equals "Going to Organisation WMG").get.id)
    )


    EventsEventorganisationcrossref.forceInsertAll(eventsEventToOrganisationCrossRefRows: _*)

    //  Thing Relationships

    val thingsThingtothingcrossrefRows = Seq(
      ThingsThingtothingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "cupbord").get.id,
        things.find(_.name equals "shower").get.id,
        "Parent_Child", true,
        relationshipRecords.find(_.name equals "Shower Parent_Child cupbord").get.id)
    )

    ThingsThingtothingcrossref.forceInsertAll(thingsThingtothingcrossrefRows: _*)

    val thingsThingToPersonCrossRefRows = Seq(
      ThingsThingpersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "car").get.id,
        people.find(_.name equals "Martin").get.id,
        "Owns", true,
        relationshipRecords.find(_.name equals "Martin Owns Car").get.id)
    )


    ThingsThingpersoncrossref.forceInsertAll(thingsThingToPersonCrossRefRows: _*)

    // Location Relationships

    val locationsLocationToLocationCrossRefRows = Seq(
      LocationsLocationtolocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "kitchen").get.id,
        locations.find(_.name equals "bathroom").get.id,
        "Next_To", true,
        relationshipRecords.find(_.name equals "Kitchen Next_To bathroom").get.id),
      LocationsLocationtolocationcrossrefRow(2, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "WMG").get.id,
        locations.find(_.name equals "Coventry").get.id,
        "Is_At", true,
        relationshipRecords.find(_.name equals "WMG location Is_At Coventry").get.id)
    )

    LocationsLocationtolocationcrossref.forceInsertAll(locationsLocationToLocationCrossRefRows: _*)

    val locationsLocationtothingcrossrefRows = Seq(
      LocationsLocationthingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "WMG").get.id,
        things.find(_.name equals "car").get.id,
        "Is_At", true,
        relationshipRecords.find(_.name equals "car is at").get.id)
    )

    LocationsLocationthingcrossref.forceInsertAll(locationsLocationtothingcrossrefRows: _*)


    // Organisation Relationships

    val organisationsOrganisationToOrganisationCrossRefRows = Seq(
      OrganisationsOrganisationtoorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "seventrent").get.id,
        organisations.find(_.name equals "WMG").get.id,
        "Buys_From", true,
        relationshipRecords.find(_.name equals "WMG Buys_From seventrent").get.id)
    )

    OrganisationsOrganisationtoorganisationcrossref.forceInsertAll(organisationsOrganisationToOrganisationCrossRefRows: _*)


    val organisationOrganisationLocationCrossRefRows = Seq(
      OrganisationsOrganisationlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "Coventry").get.id,
        organisations.find(_.name equals "WMG").get.id,
        "Is_At", true,
        relationshipRecords.find(_.name equals "WMG organisation Is_At Coventry").get.id)
    )


    OrganisationsOrganisationlocationcrossref.forceInsertAll(organisationOrganisationLocationCrossRefRows: _*)


    val organisationOrganisationThingCrossRefRows = Seq(
      OrganisationsOrganisationthingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        things.find(_.name equals "car").get.id,
        "Rents", true,
        relationshipRecords.find(_.name equals "WMG organisation Rents car").get.id)
    )


    OrganisationsOrganisationthingcrossref.forceInsertAll(organisationOrganisationThingCrossRefRows: _*)

    //People Relationships

    val personRelationshipTypes = Seq (
      PeoplePersontopersonrelationshiptypeRow(1, LocalDateTime.now(), LocalDateTime.now(),
        "Colleague With", Some("Working Together"))
    )

    PeoplePersontopersonrelationshiptype.forceInsertAll(personRelationshipTypes: _*)

    val peoplePersonToPersonCrossRefRows = Seq(
      PeoplePersontopersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        people.find(_.name equals "Andrius").get.id,
        personRelationshipTypes.find(_.name equals "Colleague With").get.id,
        true,
        relationshipRecords.find(_.name equals "Martin Colleague_With Andrius").get.id)
    )

    PeoplePersontopersoncrossref.forceInsertAll(peoplePersonToPersonCrossRefRows: _*)

    val peoplePersonOrganisationCrossRefRows = Seq(
      PeoplePersonorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        organisations.find(_.name equals "WMG").get.id,
        "Works_at", true,
        relationshipRecords.find(_.name equals "Martin Works at WMG").get.id)
    )

    val peoplePersonLocationCrossRefCrossRefRows = Seq(
      PeoplePersonlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "Coventry").get.id,
        people.find(_.name equals "Xiao").get.id,
        "Is_at", true,
        relationshipRecords.find(_.name equals "Xiao Is_at Coventry").get.id)
    )

    PeoplePersonlocationcrossref.forceInsertAll(peoplePersonLocationCrossRefCrossRefRows: _*)
  }

  def linkEntityData(
                     things: Seq[ThingsThingRow], people: Seq[PeoplePersonRow], locations: Seq[LocationsLocationRow],
                     organisations: Seq[OrganisationsOrganisationRow], events: Seq[EventsEventRow],
                     systemProperties: Seq[SystemPropertyRow], systemTypes: Seq[SystemTypeRow],
                     dataFields: Seq[DataFieldRow], dataRecords: Seq[DataRecordRow])
                    (implicit session: Session) = {

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
      SystemPropertyrecordRow(9, LocalDateTime.now(), LocalDateTime.now(), "number of employees")
    )

    SystemPropertyrecord.forceInsertAll(propertyRecords: _*)


    // location Property/type Relationships


    val locationsSystempropertydynamiccrossrefRows = Seq(
      LocationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "Coventry").get.id,
        systemProperties.find(_.name equals "size").get.id,
        dataFields.find(_.name equals "size").get.id,
        "Size of City Location", true,
        propertyRecords.find(_.name equals "city size").get.id)
    )


    LocationsSystempropertydynamiccrossref.forceInsertAll(locationsSystempropertydynamiccrossrefRows: _*)

    val locationsSystempropertystaticcrossrefRows = Seq(
      LocationsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "Coventry").get.id,
        systemProperties.find(_.name equals "size").get.id,
        dataRecords.find(_.name equals "FacebookLocation").get.id,
        dataFields.find(_.name equals "size").get.id,
        "Size of City Location", true,
        propertyRecords.find(_.name equals "city size").get.id)
    )


    LocationsSystempropertystaticcrossref.forceInsertAll(locationsSystempropertystaticcrossrefRows: _*)

    val locationSystemTypes = Seq(
      LocationsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name equals "bathroom").get.id,
        systemTypes.find(_.name equals "room").get.id, "Is", true)
    )

    LocationsSystemtypecrossref.forceInsertAll(locationSystemTypes: _*)

    // things Property/type Relationships

    val thingsSystempropertystaticcrossrefRows = Seq(
      ThingsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "cupbord").get.id,
        systemProperties.find(_.name equals "size").get.id,
        dataRecords.find(_.name equals "FacebookLocation").get.id,
        dataFields.find(_.name equals "size").get.id,
        "Parent Child", true,
        propertyRecords.find(_.name equals "city size").get.id))



    ThingsSystempropertystaticcrossref.forceInsertAll(thingsSystempropertystaticcrossrefRows: _*)

    val thingsSystempropertydynamiccrossrefRows = Seq(
      ThingsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "car").get.id,
        systemProperties.find(_.name equals "temperature").get.id,
        dataFields.find(_.name equals "temperature").get.id,
        "Parent Child", true,
        propertyRecords.find(_.name equals "temperature").get.id))


    ThingsSystempropertydynamiccrossref.forceInsertAll(thingsSystempropertydynamiccrossrefRows: _*)

    val thingsSystemtypecrossrefRows = Seq(
      ThingsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name equals "car").get.id,
        systemTypes.find(_.name equals "vehicle").get.id, "Is", true))

    ThingsSystemtypecrossref.forceInsertAll(thingsSystemtypecrossrefRows: _*)

    // people Property/type Relationships

    val peopleSystempropertystaticcrossrefRows = Seq(
      PeopleSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        systemProperties.find(_.name equals "weight").get.id,
        dataRecords.find(_.name equals "FibaroBathroom").get.id,
        dataFields.find(_.name equals "weight").get.id,
        "Parent_Child", true,
        propertyRecords.find(_.name equals "weight").get.id))


    PeopleSystempropertystaticcrossref.forceInsertAll(peopleSystempropertystaticcrossrefRows: _*)

    val peopleSystempropertydynamiccrossrefRows = Seq(
      PeopleSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        systemProperties.find(_.name equals "weight").get.id,
        dataFields.find(_.name equals "weight").get.id,
        "Parent_Child", true,
        propertyRecords.find(_.name equals "weight").get.id))



    PeopleSystempropertydynamiccrossref.forceInsertAll(peopleSystempropertydynamiccrossrefRows: _*)

    val peopleSystemTypeRows = Seq(
      PeopleSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name equals "Martin").get.id,
        systemTypes.find(_.name equals "male").get.id, "Is", true))

    PeopleSystemtypecrossref.forceInsertAll(peopleSystemTypeRows: _*)

    // events Property/type Relationships

    val eventsSystempropertystaticcrossrefRows = Seq(
      EventsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "driving").get.id,
        systemProperties.find(_.name equals "cars speed").get.id,
        dataRecords.find(_.name equals "car journey").get.id,
        dataFields.find(_.name equals "cars speed").get.id,
        "Parent_Child", true,
        propertyRecords.find(_.name equals "speed of car").get.id))


    EventsSystempropertystaticcrossref.forceInsertAll(eventsSystempropertystaticcrossrefRows: _*)

    val eventsSystempropertydynamiccrossrefRows = Seq(
      EventsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "driving").get.id,
        systemProperties.find(_.name equals "cars speed").get.id,
        dataFields.find(_.name equals "cars speed").get.id,
        "Parent_Child", true,
        propertyRecords.find(_.name equals "speed of car").get.id))


    EventsSystempropertydynamiccrossref.forceInsertAll(eventsSystempropertydynamiccrossrefRows: _*)

    val eventsSystemTypeRows = Seq(
      EventsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name equals "having a shower").get.id,
        systemTypes.find(_.name equals "dayily activities").get.id, "Is", true))

    EventsSystemtypecrossref.forceInsertAll(eventsSystemTypeRows: _*)

    // organisation Property/type Relationships

    val organisationsSystempropertystaticcrossrefRows = Seq(
      OrganisationsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        systemProperties.find(_.name equals "employees").get.id,
        dataRecords.find(_.name equals "FacebookMe").get.id,
        dataFields.find(_.name equals "cars speed").get.id,
        "Parent_Child", true,
        propertyRecords.find(_.name equals "speed of car").get.id))


    OrganisationsSystempropertystaticcrossref.forceInsertAll(organisationsSystempropertystaticcrossrefRows: _*)

    val organisationsSystempropertydynamiccrossrefRows = Seq(
      OrganisationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        systemProperties.find(_.name equals "employees").get.id,
        dataFields.find(_.name equals "number of employees").get.id,
        "Parent_Child", true,
        propertyRecords.find(_.name equals "number of employees").get.id))

    OrganisationsSystempropertydynamiccrossref.forceInsertAll(organisationsSystempropertydynamiccrossrefRows: _*)

    val organisationsSystemTypeRows = Seq(
      OrganisationsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name equals "WMG").get.id,
        systemTypes.find(_.name equals "department").get.id, "Is", true))

    OrganisationsSystemtypecrossref.forceInsertAll(organisationsSystemTypeRows: _*)
  }
}
