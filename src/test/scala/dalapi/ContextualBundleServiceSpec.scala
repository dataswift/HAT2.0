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

class ContextualBundleServiceSpec extends Specification with Specs2RouteTest with BeforeAfterAll with ContexBundleService {
  def actorRefFactory = system

  // Prepare the data to create test bundles on
  def beforeAll() = {

    val dataTablesRows = Seq (

         new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "events", "facebook")
         new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "me", "facebook")
         new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "cover", "facebook")
         new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "owner", "facebook")
         new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "place", "facebook")
         new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), "location", "facebook")
         )

     val dataTableToTableCrossRefRows = Seq(
          new DataTabletotablecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", eventsId, coverId),
          new DataTabletotablecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", eventsId, ownerId),
          new DataTabletotablecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", eventsId, placeId),
          new DataTabletotablecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", placeId, locationId)
        )

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

    val dataRecordRows = Seq(

      new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent1")
      )

    val systemUnitOfMeasurementRows = Seq(
      new SystemUnitOfMeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "meters", "distance measurement", "m"),
      new SystemUnitOfMeasurementRow(2, LocalDateTime.now(), LocalDateTime.now(), "kilograms",, "weight measurement" "kg"),
      new SystemUnitOfMeasurementRow(3, LocalDateTime.now(), LocalDateTime.now(), "meters cubed", "3d spaceq" "m^3"),
      new SystemUnitOfMeasurementRow(4, LocalDateTime.now(), LocalDateTime.now(), "personattributes", "Fibaro"),
      new SystemUnitOfMeasurementRow(5, LocalDateTime.now(), LocalDateTime.now(), "locationattributes", "Fibaro")
    )

    val systemTypeRows = Seq(
      new SystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "room dimensions", "Fibaro"),
      new SystemTypeRow(2, LocalDateTime.now(), LocalDateTime.now(), "dayily activities", "Fibaro"),
      new SystemTypeRow(3, LocalDateTime.now(), LocalDateTime.now(), "utilities", "Fibaro"),
      new SystemTypeRow(4, LocalDateTime.now(), LocalDateTime.now(), "personattributes", "Fibaro"),
      new SystemTypeRow(5, LocalDateTime.now(), LocalDateTime.now(), "locationattributes", "Fibaro")
    )
    val systemPropertyRows = Seq(
      new SystemPropertyRow(1, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", "Fibaro", typeID, unitofmeasurementID),
      new SystemPropertyRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse", "Fibaro", typeID, unitofmeasurementID),
      new SystemPropertyRow(3, LocalDateTime.now(), LocalDateTime.now(), "size", "Fibaro", typeID, unitofmeasurementID),
      new SystemPropertyRow(4, LocalDateTime.now(), LocalDateTime.now(), "weight", "Fibaro", typeID, unitofmeasurementID)
      new SystemPropertyRow(5, LocalDateTime.now(), LocalDateTime.now(), "elevation", "Fibaro", typeID, unitofmeasurementID)
    )

    val thingsThingRows = Seq(
      new ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "cupbord")     
    )

    val peoplePersonRows = Seq(
      new ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "Martin")     
    )

    val locationsLocationRows = Seq(
      new ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "kitchen")     
    )

    val organisationsOrganisationRows = Seq(
      new ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "seventrent")     
    )

        val eventsEventRows = Seq(
      new ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "having a shower")     
    )

    val entityRows = Seq(
      new EntityRow(1, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "thing", None, thingID, None, None, None),
      new EntityRow(2, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "location", locationID, None, None, None, None),
      new EntityRow(3, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "peron", None, None, None, None, personID),
      new EntityRow(4, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "organisation", None, None, None, organisationID, None),
      new EntityRow(5, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "event", None, None, eventID, None, None),
    )

    val eventsEventToEventCrossRefRows = Seq(
      new EventsEventToEventCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), eventoneID, eventtwoID, "bleh", true, relationshiprecordID)
      )

    val thingsThingToThingCrossRefRows = Seq(
      new ThingsThingToThingCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), thingoneID,thingtwoID, "bleh", true, relationshiprecordID)
      )

    val peoplePersonToPersonCrossRefRows = Seq(
      new PeoplePersonToPersonCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), persononeID, persontwoID, "bleh", true, relationshiprecordID)
      )

    val organisationsOrganisationToOrganisationCrossRefRows = Seq(
      new OrganisationsOrganisationToOrganisationCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), organisationoneID, organisationtwoID, "bleh", true, relationshiprecordID)
      )

    val locationsLocationtoLocationCrossRefRows = Seq(
      new  LocationsLocationtoLocationCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), locationoneID, locationtwoID, "bleh", true, relationshiprecordID)
      )

    val locationsSystemPropertyDynamicCrossRefRows = Seq(
      new LocationsSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), locationID, systempropertyID, fieldID, thingID, "Parent Child", true, propertyrecordID)
    )

    val locationsSystemPropertyStaticCrossRefRows = Seq(
      new LocationsSystemPropertyStaticCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), locationID, systempropertyID, recordID, fieldID, thingID, "Parent Child", true, propertyrecordID)
    )

    val thingsSystemPropertyStaticCrossRefRows = Seq(
      new ThingsSystemPropertyStaticCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), thingID, systempropertyID, recordID, fieldID, thingID, "Parent Child", true, propertyrecordID)
    )

    val thingsSystemPropertyDynamicCrossRefRows = Seq(
      new ThingsSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), thingID, systempropertyID, fieldID, thingID, "Parent Child", true, propertyrecordID)

    )

     val peopleSystemPropertyStaticCrossRefRows = Seq(
      new PeopleSystemPropertyStaticCrossRefRows(1, LocalDateTime.now(), LocalDateTime.now(), peopleID, systempropertyID, recordID, fieldID, thingID, "Parent Child", true, propertyrecordID)
    )
    

    val peopleSystemPropertyDynamicCrossRefRows = Seq(
      new PeopleSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), peopleID, systempropertyID, fieldID, thingID, "Parent Child", true, propertyrecordID)
    )

     val eventsSystemPropertyStaticCrossRefRows = Seq(
      new  EventsSystemPropertyStaticCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), eventID, systempropertyID, recordID, fieldID, thingID, "Parent Child", true, propertyrecordID)
    )
    

    val eventsSystemPropertyDynamicCrossRefRows = Seq(
      new EventsSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(),  eventID, systempropertyID, fieldID, thingID, "Parent Child", true, propertyrecordID)
    )

     val organisationsSystemPropertyStaticCrossRefRows = Seq(
      new OrganisationsSystemPropertyStaticCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), organisationID, systempropertyID, recordID, fieldID, thingID, "Parent Child", true, propertyrecordID)
    )
    

    val organisationsSystemPropertyDynamicCrossRefRows = Seq(
      new OrganisationsSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), organisationID, systempropertyID, fieldID, thingID, "Parent Child", true, propertyrecordID)
    )

    db.withSession { implicit session =>
      SystemProperty.forceInsertAll(systemPropertyRows: _*)
      Entity.forceInsertAll(entityRows: _*)
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
