package hatdex.hat.api.service

import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.service.jsonExamples.BundleExamples
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.models._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.testkit.Specs2RouteTest

class BundleServiceSpec extends Specification with Specs2RouteTest with BeforeAfterAll with BundleService {
  def actorRefFactory = system

  val dataService = new DataService {
    def actorRefFactory = system
  }

  import JsonProtocol._

  // Prepare the data to create test bundles on
  def beforeAll() = {
    val dataTableRows = Seq(
      new DataTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "kitchen", "Fibaro"),
      new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", "Fibaro"),
      new DataTableRow(4, LocalDateTime.now(), LocalDateTime.now(), "event", "Facebook")
    )

    val dataTableCrossrefs = Seq(
      new DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "contains", 2, 3)
    )

    val dataFieldRows = Seq(
      new DataFieldRow(10, LocalDateTime.now(), LocalDateTime.now(), "timestamp", 3),
      new DataFieldRow(11, LocalDateTime.now(), LocalDateTime.now(), "value", 3),
      new DataFieldRow(12, LocalDateTime.now(), LocalDateTime.now(), "name", 4),
      new DataFieldRow(13, LocalDateTime.now(), LocalDateTime.now(), "location", 4),
      new DataFieldRow(14, LocalDateTime.now(), LocalDateTime.now(), "startTime", 4),
      new DataFieldRow(15, LocalDateTime.now(), LocalDateTime.now(), "endTime", 4)
    )

    val dataRecordRows = Seq(
      new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 1"),
      new DataRecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 2"),
      new DataRecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 3"),
      new DataRecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "event record 1"),
      new DataRecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "event record 2"),
      new DataRecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "event record 3")
    )

    val dataValues = Seq(
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen time 1", 10, 1),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen value 1", 11, 1),

      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen time 2", 10, 2),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen value 2", 11, 2),

      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen time 3", 10, 3),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen value 3", 11, 3),

      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event name 1", 12, 4),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event location 1", 13, 4),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event startTime 1", 14, 4),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event endTime 1", 15, 4),

      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event name 2", 12, 5),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event location 2", 13, 5),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event startTime 2", 14, 5),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event endTime 2", 15, 5),

      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event name 3", 12, 6),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event location 3", 13, 6),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event startTime 3", 14, 6),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event endTime 3", 15, 6)
    )

    db.withSession { implicit session =>
      DataTable.forceInsertAll(dataTableRows: _*)
      DataTabletotablecrossref.forceInsertAll(dataTableCrossrefs: _*)
      DataField.forceInsertAll(dataFieldRows: _*)
      DataRecord.forceInsertAll(dataRecordRows: _*)
      // Don't _foce_ insert all data values -- IDs don't particularly matter to us
      DataValue.insertAll(dataValues: _*)
    }
  }

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      TestDataCleanup.cleanupAll
    }
    db.close
  }

  sequential

  "Contextless Bundle Service for Tables" should {
    "Reject a bundle on table without specified id" in {
      HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleTableKitchenWrong)) ~>
        createBundleTable ~> check {
        response.status should be equalTo BadRequest
      }
    }

    "create a simple Bundle Table with no filters on data" in {
      HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleTableKitchenElectricity)) ~>
        createBundleTable ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("Electricity in the kitchen")
      }
    }

    "create a simple Bundle Table with multiple filters" in {
      HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleWeekendEvents)) ~>
        createBundleTable ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("Weekend events at home")
      }
    }

    "Create and retrieve a bundle by ID" in {
      val bundleId = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleWeekendEvents)) ~>
        createBundleTable ~> check {
        responseAs[ApiBundleTable].id
      }

      bundleId must beSome

      val url = s"/table/${bundleId.get}"
      HttpRequest(GET, url) ~>
        getBundleTable ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("Weekend events at home")
      }
    }
    
    "Bundle without filters should contain all data of linked table only" in {
      val bundleTableKitchen = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleTableKitchen)) ~>
        createBundleTable ~> check {
        responseAs[ApiBundleTable]
      }
      bundleTableKitchen.id must beSome

      HttpRequest(GET, s"/table/${bundleTableKitchen.id.get}/values") ~>
        getBundleTableValuesApi ~> check {
        response.status should be equalTo OK
        val responseString = responseAs[String]
        responseString must contain("kitchen")
        responseString must contain("kitchen record 1")
        responseString must contain("kitchen record 2")
        responseString must not contain("event record 1")

        responseString must contain("kitchen time 1")
        responseString must contain("kitchen value 1")
        responseString must contain("kitchen value 2")
        responseString must contain("kitchen value 3")
        responseString must not contain("event name 1")
      }
    }

    "create a Bundle Table with multiple filters and correctly retrieve data" in {
      val bundleTable = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleWeekendEvents)) ~>
        createBundleTable ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("Weekend events at home")
        responseAs[ApiBundleTable]
      }

      bundleTable.id must beSome

      HttpRequest(GET, s"/table/${bundleTable.id.get}/values") ~>
        getBundleTableValuesApi ~> check {
        response.status should be equalTo OK

        val responseString = responseAs[String]

        responseString must contain("event")
        responseString must contain("event record 1")
        responseString must contain("event record 2")
        responseString must not contain("event record 3")
        responseString must not contain("kitchen record 1")
      }
    }
  }

  "Contextless Bundle Service for Joins" should {
    "Create and combine required bundles without join conditions" in {

      val bundleWeekendEvents = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleWeekendEvents)) ~>
        createBundleTable ~> check {
        responseAs[ApiBundleTable]
      }
      bundleWeekendEvents.id must beSome

      val bundleTableKitchen = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleTableKitchenElectricity)) ~>
        createBundleTable ~> check {
        responseAs[ApiBundleTable]
      }
      bundleTableKitchen.id must beSome

      val bundle = JsonParser(BundleExamples.bundleContextlessNoJoin).convertTo[ApiBundleContextless]
      bundle.tables must beSome
      bundle.tables.get must have size (2)

      val completeBundle = bundle.copy(tables = Some(Seq(
        bundle.tables.get(0).copy(
          bundleTable = bundle.tables.get(0).bundleTable.copy(id = bundleWeekendEvents.id)
        ),
        bundle.tables.get(1).copy(
          bundleTable = bundle.tables.get(1).bundleTable.copy(id = bundleTableKitchen.id)
        )
      )))

      val bundleJson: String = completeBundle.toJson.toString
      val cBundle = HttpRequest(POST, "/", entity = HttpEntity(MediaTypes.`application/json`, bundleJson)) ~>
        createBundleContextless ~> check {
        response.status should be equalTo Created
        responseAs[ApiBundleContextless]
      }

      HttpRequest(GET, s"/${cBundle.id.get}/values") ~>
        getBundleContextlessValuesApi ~> check {
        response.status should be equalTo OK

        val responseString = responseAs[String]

        responseString must contain("dataGroups")
        responseString must contain("event record 1")
        responseString must contain("event record 2")
        responseString must not contain("event record 3")

        responseString must contain("kitchen record 1")
        responseString must contain("kitchen record 2")
        responseString must contain("kitchen record 3")

        responseString must contain("kitchen value 1")
        responseString must contain("event name 1")
        responseString must contain("event location 1")
      }

    }

    "Create and combine required bundles with join conditions" in {

      val bundleWeekendEvents = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleWeekendEvents)) ~>
        createBundleTable ~> check {
        responseAs[ApiBundleTable]
      }
      bundleWeekendEvents.id must beSome

      val bundleTableKitchen = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleTableKitchenElectricity)) ~>
        createBundleTable ~> check {
        responseAs[ApiBundleTable]
      }
      bundleTableKitchen.id must beSome

      val bundle = JsonParser(BundleExamples.bundleContextlessJoin).convertTo[ApiBundleContextless]
      bundle.tables must beSome
      bundle.tables.get must have size (2)

      val completeBundle = bundle.copy(tables = Some(Seq(
        bundle.tables.get(0).copy(
          bundleTable = bundle.tables.get(0).bundleTable.copy(id = bundleWeekendEvents.id)
        ),
        bundle.tables.get(1).copy(
          bundleTable = bundle.tables.get(1).bundleTable.copy(id = bundleTableKitchen.id)
        )
      )))

      val bundleJson: String = completeBundle.toJson.toString

      import JsonProtocol._
      val bundleId = HttpRequest(POST, "/", entity = HttpEntity(MediaTypes.`application/json`, bundleJson)) ~>
        createBundleContextless ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain(""""operator": "equal"""")
        responseAs[String] must contain(""""name": "startTime"""")
        responseAs[ApiBundleContextless].id
      }

      bundleId must beSome

      HttpRequest(GET, s"/${bundleId.get}") ~>
        getBundleContextless ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain(""""operator": "equal"""")
        responseAs[String] must contain(""""name": "startTime"""")
      }
    }

    "Return correct error code for bundle that doesn't exist" in {
      HttpRequest(GET, "/0") ~>
        getBundleContextless ~> check {
        response.status should be equalTo NotFound
      }
    }


  }
}


