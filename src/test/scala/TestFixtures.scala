import dal.Tables._
import org.joda.time.LocalDateTime
import dal.SlickPostgresDriver.simple._

object TestFixtures {
  def prepareDataStructures(implicit session: Session) = {
    // Data tables
    val dataTablesRows = Seq(
      new DataTableRow(1, LocalDateTime.now(), LocalDateTime.now(), "Facebook", "facebook"),
      new DataTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "events", "facebook"),
      new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "me", "facebook"),
      new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "Fibaro", "Fibaro"),
      new DataTableRow(4, LocalDateTime.now(), LocalDateTime.now(), "Fitbit", "Fitbit"),
      new DataTableRow(5, LocalDateTime.now(), LocalDateTime.now(), "locations", "facebook"))

    DataTables.forceInsertAll(dataTablesRows: _*)
    
    val FacebookTableId = dataTablesRows.find(_.name === "Facebook").get.id
    val EventsTableId = dataTablesRows.find(_.name === "events").get.id
    val MeTableId = dataTablesRows.find(_.name === "me").get.id
    val locationsTableId = dataTablesRows.find(_.name === "locations").get.id
    val FibaroTableId = dataTablesRows.find(_.name === "Fibaro").get.id
    val FitbitTableId = dataTablesRows.find(_.name === "Fitbit").get.id
    
    // Nesting of data tables
    val dataTableToTableCrossRefRows = Seq(
      new DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", FacebookTableId, EventsTableId),
      new DataTabletotablecrossrefRow(2, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", FacebookTableId, locationsTableId),
      new DataTabletotablecrossrefRow(3, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", FacebookTableId, MeTableId))

    DataTabletotablecrossref.forceInsertAll(dataTableToTableCrossRefRows: _*)

    // Adding data fields to data tables
    val dataFields = Seq(
      new DataFieldRow(1, LocalDateTime.now(), LocalDateTime.now(), "weight", MeTableId),
      new DataFieldRow(2, LocalDateTime.now(), LocalDateTime.now(), "elevation", locationsTableId),
      new DataFieldRow(3, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", EventsTableId),
      new DataFieldRow(4, LocalDateTime.now(), LocalDateTime.now(), "size", FibaroTableId),
      new DataFieldRow(5, LocalDateTime.now(), LocalDateTime.now(), "temperature", FibaroTableId))

    DataField.forceInsertAll(dataFields: _*)

    val WeightFieldId = dataTables.find(_.name === "weight").get.id
    val ElevationFieldId = dataTables.find(_.name === "elevation").get.id
    val KichenElectricityFieldId = dataTables.find(_.name === "kichenElectricity").get.id
    val SizeFieldId = dataTables.find(_.name === "size").get.id
    val TemperatureFieldId = dataTables.find(_.name === "temperature").get.id

    // Data records to connect data items together
    val dataRecordRows = Seq(
      new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "FacebookMe"),
      new DataRecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "FacebookLocation"),
      new DataRecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "FibaroKitchen"),
      new DataRecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent"),
      new DataRecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "FibaroBathroom"))

    DataRecord.forceInsertAll(dataRecordRows: _*)
    
    val FacebookMeRecordId = dataTables.find(_.name === "FacebookMe").get.id
    val FacebookLocationRecordId = dataTables.find(_.name === "FacebookLocation").get.id
    val FibaroKitchenRecordId = dataTables.find(_.name === "FibaroKitchen").get.id
    val FacebookEventRecordId = dataTables.find(_.name === "FacebookEvent").get.id
    val FibaroBathroomRecordId = dataTables.find(_.name === "FibaroBathroom").get.id
    
    val dataValueRows = Seq(
      new DataValueRow(1, LocalDateTime.now(), LocalDateTime.now(), "62", WeightFieldId, FacebookMeRecordId),
      new DataValueRow(2, LocalDateTime.now(), LocalDateTime.now(), "300", ElevationFieldId, FacebookLocationRecordId),
      new DataValueRow(3, LocalDateTime.now(), LocalDateTime.now(), "20KwH", KichenElectricityFieldId, FibaroKitchenRecordId),
      new DataValueRow(4, LocalDateTime.now(), LocalDateTime.now(), "Having a Shower", FacebookEventRecordId, FibaroBathroomRecordId),
      new DataValueRow(5, LocalDateTime.now(), LocalDateTime.now(), "25", TemperatureFieldId, FacebookEventRecordId))

    DataValue.forceInsertAll(dataValueRows: _*)

    prepareContextualStructures(dataFields)
  }

  def prepareContextualStructures(dataFields: Seq[DataFieldRow])
                                 (implicit sesion: Session) = {
    // contextualisation tools 
    val systemUOMs = Seq(
      new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "meters", "distance measurement", "m"),
      new SystemUnitofmeasurementRow(2, LocalDateTime.now(), LocalDateTime.now(), "kilograms", "weight measurement", "kg"),
      new SystemUnitofmeasurementRow(3, LocalDateTime.now(), LocalDateTime.now(), "meters cubed", "3d space", "m^3"),
      new SystemUnitofmeasurementRow(4, LocalDateTime.now(), LocalDateTime.now(), "Kilowatt hours","electricity measurement", "KwH"), 
      new SystemUnitofmeasurementRow(5, LocalDateTime.now(), LocalDateTime.now(), "centigrade","heat measurement", "C")) 

    SystemUnitOfMeasurement.forceInsertAll(systemUOMs: _*)

    val systemTypes = Seq(
      new SystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "room dimensions", "Fibaro"),
      new SystemTypeRow(2, LocalDateTime.now(), LocalDateTime.now(), "dayily activities", "Google Calendar"),
      new SystemTypeRow(3, LocalDateTime.now(), LocalDateTime.now(), "utilities", "Fibaro"),
      new SystemTypeRow(4, LocalDateTime.now(), LocalDateTime.now(), "personattributes", "Fitbit"),
      new SystemTypeRow(5, LocalDateTime.now(), LocalDateTime.now(), "locationattributes", "GPS application"))

    SystemType.forceInsertAll(systemTypes: _*)

    // Entities
    val things = Seq(
      new ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "cupbord"),
      new ThingsThingRow(2, LocalDateTime.now(), LocalDateTime.now(), "car"),
      new ThingsThingRow(3, LocalDateTime.now(), LocalDateTime.now(), "shower"),
      new ThingsThingRow(4, LocalDateTime.now(), LocalDateTime.now(), "scales"))

    ThingsThing.forceInsertAll(things: _*)

    val people = Seq(
      new PeoplePersonRow(1, LocalDateTime.now(), LocalDateTime.now(), "Martin"),
      new PeoplePersonRow(2, LocalDateTime.now(), LocalDateTime.now(), "Andrius"),
      new PeoplePersonRow(3, LocalDateTime.now(), LocalDateTime.now(), "Xiao"))

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
      entity += 1
      new EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), thing.name, "thing", None, thing.id, None, None, None)
    } ++ people.map { person =>
      entity += 1
      new EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), person.name, "person", None, None, None, None, person.id)
    } ++ locations.map { location =>
      entity += 1
      new EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), location.name, "location", location.id, None, None, None, None)
    } ++ organisations.map { organisation =>
      entity += 1
      new EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), organisation.name, "organisation", None, None, None, organisation.id, None)
    } ++ events.map { event =>
      entity += 1
      new EntityRow(entityId, LocalDateTime.now(), LocalDateTime.now(), event.name, "event", None, None, Some(event.id), None, None)
    }

    Entity.forceInsertAll(entities: _*)

    contextualiseEntities(systemUOMs, systemTypes, systemProperties, propertyRecords, things, people, locatons,
      organisations, events, entities, dataFields)
  }

  def contextualiseEntities(systemUOMs: Seq[SystemUnitofmeasurementRow], systemTypes: Seq[SystemTypeRow],
                            systemProperties: Seq[SystemPropertyRow], propertyRecords: Seq[SystemPropertyrecordRow],
                            things: Seq[ThingsThingRow], people: Seq[PeoplePersonRow], locations: Seq[LocationsLocationRow],
                            organisations: Seq[OrganisationsOrganisationRow], events: Seq[EventsEventRow],
                            entities: Seq[EntityRow], dataFields: Seq[DataFieldRow])
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
      new SystemRelationshiprecordRow(14, LocalDateTime.now(), LocalDateTime.now(), "Kitchen Next_To bathroom")
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

    SystemRelationshipRecord.forceInsertAll(relationshipRecords: _*)

    // Event Relationships

    // FIXME: for clarity and consistency, do it this way
    val eventsEventToEventCrossRefRows = Seq(
      new EventsEventtoeventcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name === "going to work").get.id,
        events.find(_.name === "driving").get.id,
        "Driving to work", true,
        relationshipRecords.find(_.name === "Driving to Work").get.id)
    )
    EventsEventToEventCrossRef.forceInsertAll(eventsEventToEventCrossRefRows: _*)

    val eventsEventToThingCrossRefRows = Seq(
      new EventsEvent(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name === "having a shower").get.id,
        things.find(_.name === "shower").get.id,
        "Used_During", true, 
        relationshipRecords.find(_.name === "Shower Used_During having a shower").get.id)
    )


    EventsEventToThingCrossRef.forceInsertAll(eventsEventToThingCrossRefRows: _*)

    val eventsEventToLocationCrossRefRows = Seq(
      new EventsEventlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name === "having a shower").get.id,
        locations.find(_.name === "bathroom").get.id,
        "Is_At", true, 
        relationshipRecords.find(_.name === "having a shower Is_At bathrom").get.id)
    )


    EventsEventToLocationCrossRef.forceInsertAll(eventsEventToLocationCrossRefRows: _*)

    val eventsEventToPersonCrossRefRows = Seq(
      new EventsEventpersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name === "having a shower").get.id,
        people.find(_.name === "Martin").get.id,
        "Is", true, 
        relationshipRecords.find(_.name === "Martin Is having a shower").get.id)
    )


    EventsEventToPersonCrossRef.forceInsertAll(eventsEventToPersonCrossRefRows: _*)

    val eventsEventToOrganisationCrossRefRows = Seq(
      new EventsEventorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        events.find(_.name === "going to work").get.id,
        organisations.find(_.name === "WMG").get.id,
        "Going to Organisation", true, 
        relationshipRecords.find(_.name === "Going to Organisation WMG").get.id)
    )


    EventsEventToOrganisationCrossRef.forceInsertAll(eventsEventToOrganisationCrossRefRows: _*)

    //  Thing Relationships

    val thingsThingToThingCrossRefRows = Seq(
      new ThingsThingtothingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name === "cupbord"), 
        things.find(_.name === "shower"), 
        "Parent_Child", true, 
        relationshipRecords.find(_.name === "Shower Parent_Child cupbord").get.id)  

    ThingsThingToThingCrossRefCrossRef.forceInsertAll(thingsThingToThingCrossRefRows: _*)

    val thingsThingToPersonCrossRefRows = Seq(
      new ThingsThingpersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name === "car"), 
        people.find(_.name === "Martin").get.id,
        "Owns", true, 
        relationshipRecords.find(_.name === "Martin Owns Car").get.id)


    ThingsThingToPersonCrossRef.forceInsertAll(thingsThingToPersonCrossRefRows: _*)

    // Location Relationships

    val locationsLocationToLocationCrossRefRows = Seq(
      new LocationsLocationtolocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name === "kitchen").get.id,
        locations.find(_.name === "bathroom").get.id,
        "Next_To", true, 
        relationshipRecords.find(_.name === "Kitchen Next_To bathroom").get.id)
    )

    LocationsLocationToLocationCrossRef.forceInsertAll(locationsLocationToLocationCrossRefRows: _*)

    val locationsLocationToThingCrossRefRows = Seq(
      new LocationsLocationthingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        locations.find(_.name === "WMG").get.id,
        locations.find(_.name === "Coventry").get.id,
        "Is_At", true, 
        relationshipRecords.find(_.name === "WMG location Is_At Coventry").get.id)
    )

    LocationsLocationToThingCrossRef.forceInsertAll(locationsLocationToThingCrossRefRows: _*)


    // Organisation Relationships

    val organisationsOrganisationToOrganisationCrossRefRows = Seq(
      new OrganisationsOrganisationtoorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name === "seventrent").get.id,
        organisations.find(_.name === "WMG").get.id,
        "Buys_From", true, 
        relationshipRecords.find(_.name === "WMG Buys_From seventrent").get.id)
    )

    OrganisationsOrganisationToOrganisationCrossRef.forceInsertAll(organisationsOrganisationToOrganisationCrossRefRows: _*)


    val organisationOrganisationLocationCrossRefRows = Seq(
      new OrganisationsOrganisationlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name === "WMG").get.id,
        locations.find(_.name === "Coventry").get.id,
        "Is_At", true, 
        relationshipRecords.find(_.name === "WMG organisation Is_At Coventry").get.id)
    )


    OrganisationOrganisationLocationCrossRef.forceInsertAll(organisationOrganisationLocationCrossRefRows: _*)


    val organisationOrganisationThingCrossRefRows = Seq(
      new OrganisationsOrganisationthingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        organisations.find(_.name === "WMG").get.id,
        things.find(_.name === "car").get.id,
        "Rents", true, 
        relationshipRecords.find(_.name === "WMG organisation Rents car").get.id)
    )


    OrganisationOrganisationThingCrossRef.forceInsertAll(organisationOrganisationThingCrossRefRows: _*)

    //People Relationships

    val peoplePersonToPersonCrossRefRows = Seq(
      new PeoplePersontopersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name === "Martin").get.id,
        people.find(_.name === "Andrius").get.id,
        "Colleague_With", true,
        relationshipRecords.find(_.name === "Martin Colleague_With Andrius").get.id)
    )

    PeoplePersonToPersonCrossRef.forceInsertAll(peoplePersonToPersonCrossRefRows: _*)

    val peoplePersonOrganisationCrossRefRows = Seq(
      new PeoplePersonorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name === "Martin").get.id,
        organisations.find(_.name === "WMG").get.id,
        "Works_at", true,
        relationshipRecords.find(_.name === "Martin Works at WMG").get.id)

    val peoplePersonLocationCrossRefCrossRefRows = Seq(   
      new PeoplePersonLocationCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        people.find(_.name === "Xiao").get.id,
        locations.find(_.name === "Coventry").get.id,
        "Is_at", true,
        relationshipRecords.find(_.name === "Xiao Is_at Coventry").get.id)
    )

    PeoplePersonLocationCrossRef.forceInsertAll(peoplePersonLocationCrossRefRows: _*)

    linkEntityData(systemUOMs, systemTypes, systemProperties, propertyRecords, things, people, locatons,
      organisations, events, entities, dataFields)
  }

  def linkEntityData(systemUOMs: Seq[SystemUnitofmeasurementRow], systemTypes: Seq[SystemTypeRow],
                     systemProperties: Seq[SystemPropertyRow], propertyRecords: Seq[SystemPropertyrecordRow],
                     things: Seq[ThingsThingRow], people: Seq[PeoplePersonRow], locations: Seq[LocationsLocationRow],
                     organisations: Seq[OrganisationsOrganisationRow], events: Seq[EventsEventRow],
                     entities: Seq[EntityRow], dataFields: Seq[DataFieldRow])
                    (implicit session: Session) = {
    // Entity - Property linking

    val systemProperties = Seq(
      // FIXME: kitchenEleectricity has type 1, which is room dimensions?
      // FIXME: UoM is in meters
      new SystemPropertyRow(1, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", "Fibaro", 1, 1),
      // FIXME: wateruse has type 3, which is type utilities?
      // FIXME: wateruse UoM is kg
      new SystemPropertyRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse", "Fibaro", 3, 2),
      new SystemPropertyRow(3, LocalDateTime.now(), LocalDateTime.now(), "size", "Fibaro", systemTypes(0).id, 3),
      // FIXME: weight has type 2 which is _dayily_ activities?
      // FIXME: UoM 4 is broken
      new SystemPropertyRow(4, LocalDateTime.now(), LocalDateTime.now(), "weight", "Fibaro", 2, 4),
      // FIXME: elevation has type 4 which is personattributes?
      // FIXME: UoM 5 is broken
      new SystemPropertyRow(5, LocalDateTime.now(), LocalDateTime.now(), "elevation", "Fibaro", 4, 5))

    SystemProperty.forceInsertAll(systemProperties: _*)

    val propertyRecords = Seq(
      new SystemPropertyrecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity"),
      new SystemPropertyrecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      new SystemPropertyrecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "size"),
      new SystemPropertyrecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "weight"),
      new SystemPropertyrecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "elevation"))

    SystemPropertyRecord.forceInsertAll(propertyRecords: _*)


    // location Property/type Relationships 


    val locationsSystemPropertyDynamicCrossRefRows = Seq(
      // locationId: Int, systemPropertyId: Int, fieldId: Int, relationshipType
      // Location 2 -
      // systemProeprty 3 -
      // fieldId 2 -
      //
      new LocationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 2, 3, 2, "Parent Child", true, 1)
    )


    LocationsSystemPropertyDynamicCrossRefCrossRef.forceInsertAll(locationsSystemPropertyDynamicCrossRefRows: _*)

    val locationsSystemPropertyStaticCrossRefRows = Seq(
      new LocationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, 4, 3, "Parent Child", true, 5) // FIXME: too many arguments!
    )


    LocationsSystemPropertyStaticCrossRef.forceInsertAll(locationsSystemPropertyStaticCrossRefRows: _*)

    val locationsSystemTypeRows = Seq(
      new LocationsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 2, 1, "recordID", true) //FIXME: location 2 (bathroom) is linked to type "room dimensions" with relationship type "recordID" ???
    )

    LocationsSystemTypeCrossRef.forceInsertAll(locationsSystemTypeRows: _*)

    // things Property/type Relationships

    val thingsSystemPropertyStaticCrossRefRows = Seq(
      new ThingsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, 3, 2, 1, "Parent Child", true, 3)
    )


    ThingsSystemPropertyStaticCrossRef.forceInsertAll(locationsSystemTypeRows: _*)


    val thingsSystemPropertyDynamicCrossRefRows = Seq(
      new ThingsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
        things.find(_.name === "shower"),
        systemProperties.find(_.name === "wateruse"),
        2,  // FIXME: no data field to relate to water use
        "Parent Child", true,
        3)  // FIXME: property record "size"

    )


    ThingsSystemPropertyDynamicCrossRef.forceInsertAll(thingsSystemPropertyDynamicCrossRefRows: _*)

    val thingsSystemTypeCrossRefRows = Seq(
      new ThingsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), thingID, 2, "recordID", true)
    )


    ThingsThingSystemTypeCrossRef.forceInsertAll(thingsSystemTypeCrossRefRows: _*)

    // people Property/type Relationships

    val peopleSystemPropertyStaticCrossRefRows = Seq(
      new PeopleSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, 2, 1, 4, "Parent Child", true, 2)
    )


    PeopleSystemPropertyStaticCrossRef.forceInsertAll(peopleSystemPropertyStaticCrossRefRows: _*)

    val peopleSystemPropertyDynamicCrossRefRows = Seq(
      new PeopleSystempropertydynamiccrossrefRow()(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, 1, 3, "Parent Child", true, 2)
    )


    PeopleSystemPropertyDynamicCrossRef.forceInsertAll(peopleSystemPropertyDynamicCrossRefRows: _*)

    val peopleSystemTypeRows = Seq(
      new PeopleSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, "recordID", true)
    )

    PeopleSystemTypeCrossRef.forceInsertAll(peopleSystemTypeRows: _*)

    // events Property/type Relationships

    val eventsSystemPropertyStaticCrossRefRows = Seq(
      new EventsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 4, 3, 2, 1, "Parent Child", true, 2)
    )


    EventsSystemPropertyStaticCrossRef.forceInsertAll(eventsSystemPropertyStaticCrossRefRows: _*)

    val eventsSystemPropertyDynamicCrossRefRows = Seq(
      new EventsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 3, 2, 5, 2, "Parent Child", true, 3)
    )


    EventsSystemPropertyDynamicCrossRef.forceInsertAll(eventsSystemPropertyDynamicCrossRefRows: _*)

    val eventsSystemTypeRows = Seq(
      new EventsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 4, "recordID", true)
    )


    EventsSystemTypeRef.forceInsertAll(eventsSystemTypeRows: _*)

    // organisation Property/type Relationships

    val organisationsSystemPropertyStaticCrossRefRows = Seq(
      new OrganisationsSystempropertystaticcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, 3, 2, 1, "Parent Child", true, 4)
    )


    OrganisationsSystemPropertyStaticCrossRef.forceInsertAll(organisationsSystemPropertyStaticCrossRefRows: _*)

    val organisationsSystemPropertyDynamicCrossRefRows = Seq(
      new OrganisationsSystempropertydynamiccrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, 2, 3, "Parent Child", true, 1)
    )

    OrganisationsSystemPropertyDynamicCrossRef.forceInsertAll(organisationsSystemPropertyDynamicCrossRefRows: _*)

    val organisationsSystemTypeRows = Seq(
      new OrganisationsSystemtypecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "recordID", true)
    )

    OrganisationSystemType.forceInsertAll(organisationSystemTypeRows: _*)
  }

  def clearAllData(implicit session: Session) = {

    BundleTableslicecondition.delete
    BundleTableslice.delete
    BundleTable.delete
    BundleContext.delete
    BundlePropertySlice.delete
    BundlePropertySliceCondition.delete
    BundlePropertyRecordCrossRef.delete
    EntitySelection.delete
    Entity.delete

    SystemProperty.delete
    SystemType.delete
    SystemUnitOfMeasurement.delete
    SystemPropertyRecord.delete
    SystemRelationshipRecord.delete
    EventsSystemPropertyDynamicCrossref.delete
    EventsSystemPropertyStaticCrossRef.delete
    EventsSystemTypeCrossRef.delete
    EventsEventToEventCrossRef.delete
    EventsEventOrganisationCrossRef.delete
    EventsEventThingCrossRef.delete
    EventsEventLocationCrossRef.delete

    OrganisationsOrganisationToOrganisationCrossRef.delete
    OrganisationsSystemtypeCrossRef.delete
    OrganisationsSystemPropertyStaticCrossRef.delete
    OrganisationsSystemPropertyDynamicCrossRef.delete
    OrganisationsOrganisationThingCrossRef.delete
    OrganisationsOrganisationLocatioCrossRef.delete

    ThingsThingToTShingCrossRef.delete
    ThingsSystemTypeCrossRef.delete
    ThingsSystemPropertyDynamicCrossRef.delete
    ThingsSystemPropertyStaticCrossRef.delete
    ThingsThingPersonCrossRef.delete

    PeoplePersonToPersonCrossRef.delete
    PeopleSystemTypeCrossRef.delete
    PeopleSystemPropertyDynamicCrossRef.delete
    PeopleSystemPropertyStaticCrossRef.delete
    PeoplePersonOrganisationCrossRef.delete
    PeoplePersonLocationCrossRef.delete
    PeoplePersonOrganisationCrossRef.delete

    LocationsLocationToLocationCrossRef.delete
    LocationsSystemTypeCrossRef.delete
    LocationsSystemPropertydynamicCrossRef.delete
    LocationsSystemPropertyStaticCrossRef.delete
    LocationsLocationThingCrossRef.delete

    EventsEvent.delete
    ThingsThing.delete
    PeoplePerson.delete
    LocationsLocation.delete
    OrganisationsOrganisation.delete


    DataValue.delete
    DataRecord.delete
    DataField.delete
    DataTabletotablecrossref.delete
    DataTable.delete
  }
}
