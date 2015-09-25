package dalapi

import dal.Tables._
import dalapi.service.BundleService
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.http.HttpMethods._
import spray.http._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._

import dal.SlickPostgresDriver.simple._

class ContexualBundleServiceSpec extends Specification with Specs2RouteTest with BeforeAfterAll with ContextBundleService {
  def actorRefFactory = system

  // Prepare the data to create test bundles on
  def beforeAll() = {

    // Data tables
    val dataTablesRows = Seq (
         new DataTableRow(1, LocalDateTime.now(), LocalDateTime.now(), "Facebook", "facebook"), 
         new DataTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "events", "facebook"),
         new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "me", "facebook"),
         new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "Fibaro", "Fibaro"),
         new DataTableRow(4, LocalDateTime.now(), LocalDateTime.now(), "Fitbit", "Fitbit"),
         new DataTableRow(5, LocalDateTime.now(), LocalDateTime.now(), "locations", "facebook")
         )

    db.withSession { implicit session =>
      DataTables.forceInsertAll(DataTablesRows: _*)
    }

    /*val findFacebookTableId = dataTables.filter(_.name === "Facebook").map(_.id).run.head
    val findEventsTableId = dataTables.filter(_.name === "events").map(_.id).run.head
    val findMeTableId = dataTables.filter(_.name === "me").map(_.id).run.head
    val findlocationsTableId = dataTables.filter(_.name === "locations").map(_.id).run.head
    val findFibaroTableId = dataTables.filter(_.name === "Fibaro").map(_.id).run.head
    val findFitbitTableId = dataTables.filter(_.name === "Fitbit").map(_.id).run.head
    */
     val dataTableToTableCrossRefRows = Seq(
          new DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", findFacebookTableId, findEventsTableId),
          new DataTabletotablecrossrefRow(2, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", findFacebookTableId, findlocationsTableId),
          new DataTabletotablecrossrefRow(3, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", findFacebookTableId, findMeTableId)
        )

     db.withSession { implicit session =>
      DataTabletotablecrossref.forceInsertAll(DataTabletotablecrossrefRows: _*)
    }


    val dataFieldRows = Seq(
          new DataFieldRow(1, LocalDateTime.now(), LocalDateTime.now(), "weight", findMeTableId),
          new DataFieldRow(2, LocalDateTime.now(), LocalDateTime.now(), "elevation", findlocationsTableId),
          new DataFieldRow(3, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", findEventsTableId),
          new DataFieldRow(4, LocalDateTime.now(), LocalDateTime.now(), "size",  findFibaroTableId),
          new DataFieldRow(5, LocalDateTime.now(), LocalDateTime.now(), "temperature",  findFibaroTableId)
        )

     db.withSession { implicit session =>
     DataField.forceInsertAll(DataFieldRows: _*)
    }

    val dataRecordRows = Seq(

        new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "FacebookMe"),
        new DataRecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "FacebookLocation"),
        new DataRecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "FibaroKitchen"),
        new DataRecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent"),
        new DataRecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "FibaroBathroom")
      )


    db.withSession { implicit session =>
     DataRecord.forceInsertAll(DataRecordRows: _*)
    }

    /*val findFacebookMeRecordId = dataTables.filter(_.name === "FacebookMe").map(_.id).run.head
    val findFacebookLocationRecordId = dataTables.filter(_.name === "FacebookLocation").map(_.id).run.head
    val findFibaroKitchenRecordId = dataTables.filter(_.name === "FibaroKitchen").map(_.id).run.head
    val findFacebookEventRecordId = dataTables.filter(_.name === "FacebookEvent").map(_.id).run.head
    val findFibaroBathroomRecordId = dataTables.filter(_.name === "FibaroBathroom").map(_.id).run.head

    val findWeightFieldId = dataTables.filter(_.name === "weight").map(_.id).run.head
    val findElevationFieldId = dataTables.filter(_.name === "elevation").map(_.id).run.head
    val findKichenElectricityFieldId = dataTables.filter(_.name === "kichenElectricity").map(_.id).run.head
    val findSizeFieldId = dataTables.filter(_.name === "size").map(_.id).run.head
    val findTemperatureFieldId = dataTables.filter(_.name === "temperature").map(_.id).run.head
    */
    val dataValueRows = Seq(
        new DataValueRow(1, LocalDateTime.now(), LocalDateTime.now(), "62", findWeightFieldId, findFacebookMeRecordId),
        new DataValueRow(2, LocalDateTime.now(), LocalDateTime.now(), "300", findElevationFieldId, findFacebookLocationRecordId),
        new DataValueRow(3, LocalDateTime.now(), LocalDateTime.now(), "20KwH", findKichenElectricityFieldId, findFibaroKitchenRecordId),
        new DataValueRow(4, LocalDateTime.now(), LocalDateTime.now(), "Having a Shower", findFacebookEventRecordId, findFibaroBathroomRecordId),
        new DataValueRow(5, LocalDateTime.now(), LocalDateTime.now(), "25", findTemperatureFieldId, findFacebookEventRecordId)
        )

    db.withSession { implicit session =>
     DataValue.forceInsertAll(DataValueRows: _*)
    }

    // contextualisation tools 
    val systemUnitOfMeasurementRows = Seq(
      new SystemUnitOfMeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "meters", "distance measurement", "m"),
      new SystemUnitOfMeasurementRow(2, LocalDateTime.now(), LocalDateTime.now(), "kilograms",, "weight measurement" "kg"),
      new SystemUnitOfMeasurementRow(3, LocalDateTime.now(), LocalDateTime.now(), "meters cubed", "3d spaceq" "m^3"),
      new SystemUnitOfMeasurementRow(4, LocalDateTime.now(), LocalDateTime.now(), "personattributes", "Fibaro"),
      new SystemUnitOfMeasurementRow(5, LocalDateTime.now(), LocalDateTime.now(), "locationattributes", "Fibaro")
    )

    db.withSession { implicit session =>
     SystemUnitOfMeasurement.forceInsertAll(SystemUnitOfMeasurementRows: _*)

    val systemTypeRows = Seq(
      new SystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "room dimensions", "Fibaro"),
      new SystemTypeRow(2, LocalDateTime.now(), LocalDateTime.now(), "dayily activities", "Fibaro"),
      new SystemTypeRow(3, LocalDateTime.now(), LocalDateTime.now(), "utilities", "Fibaro"),
      new SystemTypeRow(4, LocalDateTime.now(), LocalDateTime.now(), "personattributes", "Fibaro"),
      new SystemTypeRow(5, LocalDateTime.now(), LocalDateTime.now(), "locationattributes", "Fibaro")
    )

    db.withSession { implicit session =>
     SystemType.forceInsertAll(SystemTypeRows: _*)

    val systemPropertyRows = Seq(
      new SystemPropertyRow(1, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", "Fibaro", 1, 1),
      new SystemPropertyRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse", "Fibaro", 3, 2),
      new SystemPropertyRow(3, LocalDateTime.now(), LocalDateTime.now(), "size", "Fibaro", 1, 3),
      new SystemPropertyRow(4, LocalDateTime.now(), LocalDateTime.now(), "weight", "Fibaro", 2, 4),
      new SystemPropertyRow(5, LocalDateTime.now(), LocalDateTime.now(), "elevation", "Fibaro", 4, 5)
    )

    db.withSession { implicit session =>
     SystemProperty.forceInsertAll(SystemPropertyRows: _*)

    val systemPropertyRecordRows = Seq(
      new SystemPropertyRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity"),
      new SystemPropertyRecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      new SystemPropertyRecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "size"),
      new SystemPropertyRecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "weight"),
      new SystemPropertyRecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "elevation")
    )

    db.withSession { implicit session =>
     SystemPropertyRecord.forceInsertAll(SystemPropertyRecordRows: _*)

    // Entities
    val thingsThingRows = Seq(
      new ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "cupbord"),
      new ThingsThingRow(2, LocalDateTime.now(), LocalDateTime.now(), "car"),
      new ThingsThingRow(3, LocalDateTime.now(), LocalDateTime.now(), "shower"),
      new ThingsThingRow(4, LocalDateTime.now(), LocalDateTime.now(), "scales")     
    )

    db.withSession { implicit session =>
     ThingsThing.forceInsertAll(ThingsThingRows: _*)

    val peoplePersonRows = Seq(
      new PeoplePersonRow(1, LocalDateTime.now(), LocalDateTime.now(), "Martin"),
      new PeoplePersonRow(2, LocalDateTime.now(), LocalDateTime.now(), "Andrius"),
      new PeoplePersonRow(3, LocalDateTime.now(), LocalDateTime.now(), "Xiao")     
    )

     db.withSession { implicit session =>
     PeoplePerson.forceInsertAll(PeoplePersonRows: _*)

    val locationsLocationRows = Seq(
      new LocationsLocationRow(1, LocalDateTime.now(), LocalDateTime.now(), "kitchen"),
      new LocationsLocationRow(2, LocalDateTime.now(), LocalDateTime.now(), "bathroom"),
      new LocationsLocationRow(3, LocalDateTime.now(), LocalDateTime.now(), "WMG")    
    )

    db.withSession { implicit session =>
     LocationsLocation.forceInsertAll(LocationsLocationRows: _*)

    val organisationsOrganisationRows = Seq(
      new OrganisationsOrganisationRow(1, LocalDateTime.now(), LocalDateTime.now(), "seventrent"),
      new OrganisationsOrganisationRow(2, LocalDateTime.now(), LocalDateTime.now(), "WMG")      
    )

    db.withSession { implicit session =>
     OrganisationsOrganisation.forceInsertAll(OrganisationsOrganisationRows: _*)

    val eventsEventRows = Seq(
      new EventsEventRow(1, LocalDateTime.now(), LocalDateTime.now(), "having a shower"),
      new EventsEventRow(2, LocalDateTime.now(), LocalDateTime.now(), "driving"),
      new EventsEventRow(3, LocalDateTime.now(), LocalDateTime.now(), "going to work"),
      new EventsEventRow(4, LocalDateTime.now(), LocalDateTime.now(), "cooking")      
    )

    db.withSession { implicit session =>
     EventsEvent.forceInsertAll(EventsEventRows: _*)

    val entityRows = Seq(
      new EntityRow(1, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "thing", None, 1, None, None, None),
      new EntityRow(2, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "location", 1, None, None, None, None),
      new EntityRow(3, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "peron", None, None, None, None, 1),
      new EntityRow(4, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "organisation", None, None, None, 1, None),
      new EntityRow(5, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "event", None, None, 1, None, None),
      new EntityRow(6, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "thing", None, 2, None, None, None),
      new EntityRow(7, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "location", 2, None, None, None, None),
      new EntityRow(8, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "peron", None, None, None, None, 2),
      new EntityRow(9, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "organisation", None, None, None, 2, None),
      new EntityRow(10, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "event", None, None, 2, None, None),
      new EntityRow(11, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "event", None, None, 3, None, None),
      new EntityRow(12, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "thing", None, 3, None, None, None),
      new EntityRow(13, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "location", 3, None, None, None, None),
      new EntityRow(14, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "peron", None, None, None, None, 3),
      new EntityRow(15, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "event", None, None, 3, None, None),
      new EntityRow(16, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "event", None, None, 4, None, None),
      new EntityRow(16, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "thing", None, 4, None, None, None)
    )
    
    db.withSession { implicit session =>
     Entity.forceInsertAll(EntityRows: _*)

    // Relationship Record

    val systemRelationshipRecordRows = Seq(
      new SystemRelationshipRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "Driving to Work"),
      new SystemRelationshipRecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      new SystemRelationshipRecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "Colleagues"),
      new SystemRelationshipRecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "weight"),
      new SystemRelationshipRecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "elevation")
      new SystemRelationshipRecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "Driving to Work"),
      new SystemRelationshipRecordRow(7, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      new SystemRelationshipRecordRow(8, LocalDateTime.now(), LocalDateTime.now(), "size"),
      new SystemRelationshipRecordRow(9, LocalDateTime.now(), LocalDateTime.now(), "Car Ownership"),
      new SystemRelationshipRecordRow(10, LocalDateTime.now(), LocalDateTime.now(), "elevation")
      new SystemRelationshipRecordRow(11, LocalDateTime.now(), LocalDateTime.now(), "Driving to Work"),
      new SystemRelationshipRecordRow(12, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      new SystemRelationshipRecordRow(13, LocalDateTime.now(), LocalDateTime.now(), "size"),
      new SystemRelationshipRecordRow(14, LocalDateTime.now(), LocalDateTime.now(), "weight")
    )
      
    db.withSession { implicit session =>
     SystemRelationshipRecord.forceInsertAll(SystemRelationshipRecordRows: _*)

    ) 
    val eventsEventToEventCrossRefRows = Seq(
      new EventsEventToEventCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 3, 2, "Parent_Child", true, 1)
    )

    db.withSession { implicit session =>
     EventsEventToEventCrossRef.forceInsertAll(EventsEventToEventCrossRefRows: _*)

    val thingsThingToThingCrossRefRows = Seq(
      new ThingsThingToThingCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1,1, "Parent_Child", true, 2)
      )

    db.withSession { implicit session =>
     ThingsThingToThingCrossRefCrossRef.forceInsertAll(ThingsThingToThingCrossRefRows: _*)

    val peoplePersonToPersonCrossRefRows = Seq(
      new PeoplePersonToPersonCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "Colleague", true, 3)
      )

    db.withSession { implicit session =>
     PeoplePersonToPersonCrossRef.forceInsertAll(PeoplePersonToPersonCrossRefRows: _*)
   
    val organisationsOrganisationToOrganisationCrossRefRows = Seq(
      new OrganisationsOrganisationToOrganisationCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "Buys_From", true, 4)
      )

    db.withSession { implicit session =>
     OrganisationsOrganisationToOrganisationCrossRef.forceInsertAll(OrganisationsOrganisationToOrganisationCrossRefRows: _*)

    val locationsLocationToLocationCrossRefRows = Seq(
      new  LocationsLocationToLocationCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "Next_To", true, 4)
      )

    db.withSession { implicit session =>
     LocationsLocationToLocationCrossRef.forceInsertAll(LocationsLocationToLocationCrossRefRows: _*)

    // Event Relationships

    val eventsEventToThingCrossRefRows = Seq(
      new  EventsEventToThingCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, "Used_During", true, 5)
      )

    db.withSession { implicit session =>
     EventsEventToThingCrossRef.forceInsertAll(EventsEventToThingCrossRefRows: _*)

    val eventsEventToLocationCrossRefRows = Seq(
      new  EventsEventToLocationCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "Is_At", true, 6)
      )

    db.withSession { implicit session =>
     EventsEventToLocationCrossRef.forceInsertAll(EventsEventToLocationCrossRefRows: _*)

    val eventsEventToPersonCrossRefRows = Seq(
      new  EventsEventToPersonCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "Is_At", true, 7)
      )

    db.withSession { implicit session =>
     EventsEventToPersonCrossRef.forceInsertAll(EventsEventToPersonCrossRefRows: _*)

    val eventsEventToOrganisationCrossRefRows = Seq(
      new  EventsEventToOrganisationCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 3, 2, "Uses_Utility", true, 8)
      )
      
    db.withSession { implicit session =>
     EventsEventToOrganisationCrossRef.forceInsertAll(EventsEventToOrganisationCrossRefRows: _*)
    //  Thing Relationships

    val thingsThingToPersonCrossRefRows = Seq(
      new  ThingsThingToPersonCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, "Owns", true, 9)
      )

    db.withSession { implicit session =>
     ThingsThingToPersonCrossRef.forceInsertAll(ThingsThingToPersonCrossRefRows: _*)

    // Location Relationships

    val locationsLocationToThingCrossRefRows = Seq(
      new  LocationsLocationToThingCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 3, 2, "Is_At", true, 10)
      )

    db.withSession { implicit session =>
     LocationsLocationToThingCrossRef.forceInsertAll(LocationsLocationToThingCrossRefRows: _*)


    // Organisation Relationships

     val organisationOrganisationLocationCrossRefRows = Seq(
      new  OrganisationOrganisationLocationCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 2, 3, "Is_At", true, 11)
      )

    db.withSession { implicit session =>
      OrganisationOrganisationLocationCrossRef.forceInsertAll(OrganisationOrganisationLocationCrossRefRows: _*)


     val organisationOrganisationThingCrossRefRows = Seq(
      new  OrganisationOrganisationThingCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 2, 2, "Rents", true, 12)
      )

    db.withSession { implicit session =>
      OrganisationOrganisationThingCrossRef.forceInsertAll(OrganisationOrganisationThingCrossRefRows: _*)

    //People Relationships

    val peoplePersonOrganisationCrossRefRows = Seq(
      new  PeoplePersonOrganisationCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, "Works_at", true, 13)
      )

    db.withSession { implicit session =>
      PeoplePersonOrganisationCrossRefCrossRef.forceInsertAll(PeoplePersonOrganisationCrossRefRows: _*)


     val peoplePersonOrganisationCrossRefRows = Seq(
      new  PeoplePersonOrganisationCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 3, 1, "Is_at", true, 14)
      )

    db.withSession { implicit session =>
      PeoplePersonOrganisationCrossRef.forceInsertAll(PeoplePersonOrganisationCrossRefRefRows: _*)

    // location Property/type Relationships 

    val locationsSystemPropertyDynamicCrossRefRows = Seq(
      new LocationsSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 4, 2, 3, 2, "Parent Child", true, 1)
    )

    db.withSession { implicit session =>
     LocationsSystemPropertyDynamicCrossRefCrossRef.forceInsertAll(LocationsSystemPropertyDynamicCrossRefRows: _*)

    val locationsSystemPropertyStaticCrossRefRows = Seq(
      new LocationsSystemPropertyStaticCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, 4, 3, 4, "Parent Child", true, 5)
    )

    db.withSession { implicit session =>
     LocationsSystemPropertyStaticCrossRef.forceInsertAll(LocationsSystemPropertyStaticCrossRefRows: _*)

    val locationsSystemTypeRows = Seq(
      new LocationsSystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), 2, 1, "recordID", true)
    )
    
    db.withSession { implicit session =>
     LocationsSystemTypeCrossRef.forceInsertAll(LocationsSystemTypeRows: _*)

    // things Property/type Relationships

    val thingsSystemPropertyStaticCrossRefRows = Seq(
      new ThingsSystemPropertyStaticCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, 3, 2, 1, "Parent Child", true, 3)
    )

    db.withSession { implicit session =>
     ThingsSystemPropertyStaticCrossRef.forceInsertAll(LocationsSystemTypeRows: _*)


    val thingsSystemPropertyDynamicCrossRefRows = Seq(
      new ThingsSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 3, 2, 1, 2, "Parent Child", true, 3)

    )

    db.withSession { implicit session =>
     ThingsSystemPropertyDynamicCrossRef.forceInsertAll(ThingsSystemPropertyDynamicCrossRefRows: _*)
    
    val thingsSystemTypeCrossRefRows = Seq(
      new ThingsSystemTypeCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), thingID, 2, "recordID", true)
    )

    db.withSession { implicit session =>
     ThingsThingSystemTypeCrossRef.forceInsertAll(ThingsSystemTypeCrossRefRows: _*)

    // people Property/type Relationships

     val peopleSystemPropertyStaticCrossRefRows = Seq(
      new PeopleSystemPropertyStaticCrossRefRows(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, 2, 1, 4, "Parent Child", true, 2)
    )
    
    db.withSession { implicit session =>
     PeopleSystemPropertyStaticCrossRef.forceInsertAll(PeopleSystemPropertyStaticCrossRefRows: _*)

    val peopleSystemPropertyDynamicCrossRefRows = Seq(
      new PeopleSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, 1, 3, "Parent Child", true, 2)
    )

    db.withSession { implicit session =>
     PeopleSystemPropertyDynamicCrossRef.forceInsertAll(PeopleSystemPropertyDynamicCrossRefRows: _*)

    val peopleSystemTypeRows = Seq(
      new PeopleSystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, "recordID", true)
    )

    db.withSession { implicit session =>
     PeopleSystemTypeCrossRef.forceInsertAll(PeopleSystemTypeRows: _*)
    
    // events Property/type Relationships

    val eventsSystemPropertyStaticCrossRefRows = Seq(
      new  EventsSystemPropertyStaticCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 4, 3, 2, 1, "Parent Child", true, 2)
    )
    
    db.withSession { implicit session =>
     EventsSystemPropertyStaticCrossRef.forceInsertAll(EventsSystemPropertyStaticCrossRefRows: _*)

    val eventsSystemPropertyDynamicCrossRefRows = Seq(
      new EventsSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(),  3, 2, 5, 2, "Parent Child", true, 3)
    )

    db.withSession { implicit session =>
     EventsSystemPropertyDynamicCrossRef.forceInsertAll(EventsSystemPropertyDynamicCrossRefRows: _*)

    val eventsSystemTypeRows = Seq(
      new EventsSystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 4, "recordID", true)
    )

    db.withSession { implicit session =>
     EventsSystemTypeRef.forceInsertAll(EventsSystemTypeRows: _*)

    // organisation Property/type Relationships

    val organisationsSystemPropertyStaticCrossRefRows = Seq(
      new OrganisationsSystemPropertyStaticCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, 3, 2, 1, "Parent Child", true, 4)
    )

    db.withSession { implicit session =>
     OrganisationsSystemPropertyStaticCrossRef.forceInsertAll(OrganisationsSystemPropertyStaticCrossRefRows: _*)
    
    val organisationsSystemPropertyDynamicCrossRefRows = Seq(
      new OrganisationsSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, 2, 3, "Parent Child", true, 1)
    )

    db.withSession { implicit session =>
     OrganisationsSystemPropertyDynamicCrossRef.forceInsertAll(OrganisationsSystemPropertyDynamicCrossRefRows: _*)

    val organisationsSystemTypeRows = Seq(
    new OrganisationSystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "recordID", true)
    )

    db.withSession { implicit session =>
     OrganisationSystemType.forceInsertAll(OrganisationSystemTypeRows: _*)

    }
  }

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      BundleTableslicecondition.delete
      BundleTableslice.delete
      BundleTable.delete

      DataValue.delete
      DataRecord.delete
      DataField.delete
      DataTabletotablecrossref.delete
      DataTable.delete
      SystemProperty.delete
      SystemType.delete
      SystemUnitOfMeasurement.delete
      SystemPropertyRecord.delete
      SystemRelationshipRecord.delete
      EventsSystemPropertyStaticCrossRef.delete
      EventsSystemPropertyDynamicCrossRef.delete
      EventsSystemType.delete
      EventsEventToEventCrossRef.delete
      EventsEventToOrganisationCrossRef.delete
      EventsEventToThingCrossRef.delete
      EventsEventToLocationCrossRef.delete
      EventsEvent.delete
      OrganisationsOrganisation.delete
      OrganisationsOrganisationToOrganisationCrossRef.delete
      OrganisationsSystemType.delete
      OrganisationsSystemPropertyDynamicCrossRef.delete
      OrganisationsSystemPropertyStaticCrossRef.delete
      OrganisationOrganisationThingCrossRef.delete
      OrganisationOrganisationLocationCrossRef.delete
      ThingsThing.delete
      ThingsThingtoThingCrossRef.delete
      ThingsSystemTypeCrossRef.delete
      ThingsSystemPropertyDynamicCrossRef.delete
      ThingsSystemPropertyStaticCrossRef.delete
      ThingsThingToPersonCrossRef.delete
      PeoplePerson.delete
      PeoplePersontoPersonCrossRef.delete
      PeopleSystemTypeCrossRef.delete
      PeopleSystemPropertyDynamicCrossRef.delete
      PeopleSystemPropertyStaticCrossRef.delete
      PeoplePersonOrganisationCrossRef.delete
      PeoplePersonLocationCrossRef.delete
      PeoplePersonOrganisationCrossRef.delete
      LocationsLocation.delete
      LocationsToLocationsCrossRef.delete
      LocationsSystemTypeCrossRef.delete
      LocationsSystemPropertyDynamicCrossRef.delete
      LocationsSystemPropertyStaticCrossRef.delete
      LocationsLocationToThingCrossRef.delete
    }
  }

  sequential

  "BundleService" should {
    "Reject a bundle on table without specified id" in {
      HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleTableKitchenWrong)) ~>
        createBundleTable ~> check {
        response.status should be equalTo BadRequest
      }
    }

    "create a simple Bundle Table with no filters on data" in {
      HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleTableKitchen)) ~>
        createBundleTable ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("Electricity in the kitchen")
      }
    }

    "create a simple Bundle Table with multiple filters" in {
      HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleWeekendEvents)) ~>
        createBundleTable ~> check {
//        print(response.message)
        response.status should be equalTo Created
        responseAs[String] must contain("Weekend events at home")
      }
    }

  }
}

object BundleExamples {
  val bundleTableKitchenWrong =
    """
      |  {
      |    "name": "Electricity in the kitchen",
      |    "table": {
      |      "name": "kichenElectricity",
      |      "source": "fibaro"
      |    }
      |  }
    """.stripMargin

  val bundleTableKitchen =
    """
      |  {
      |    "name": "Electricity in the kitchen",
      |    "table": {
      |      "id": 3,
      |      "name": "kichenElectricity",
      |      "source": "fibaro"
      |    }
      |  }
    """.stripMargin

  val bundleWeekendEvents =
    """
      |  {
      |    "name": "Weekend events at home",
      |    "table": {
      |      "id": 4,
      |      "name": "event",
      |      "source": "Facebook"
      |    },
      |    "slices": [
      |      {
      |        "table": {
      |          "id": 4,
      |          "name": "event",
      |          "source": "Facebook"
      |        },
      |        "conditions": [
      |          {
      |            "field": {
      |              "id": 13,
      |              "tableId": 4,
      |              "name": "location"
      |            },
      |            "value": "home",
      |            "operator": "equals"
      |          },
      |          {
      |            "field": {
      |              "id": 14,
      |              "tableId": 4,
      |              "name": "startTime"
      |            },
      |            "value": "saturday",
      |            "operator": "equals"
      |          }
      |        ]
      |      },
      |      {
      |        "table": {
      |          "id": 4,
      |          "name": "event",
      |          "source": "Facebook"
      |        },
      |        "conditions": [
      |          {
      |            "field": {
      |              "id": 13,
      |              "tableId": 4,
      |              "name": "location"
      |            },
      |            "value": "home",
      |            "operator": "equals"
      |          },
      |          {
      |            "field": {
      |              "id": 14,
      |              "tableId": 4,
      |              "name": "startTime"
      |            },
      |            "value": "sunday",
      |            "operator": "equals"
      |          }
      |        ]
      |      }
      |    ]
      |  }
    """.stripMargin
}
