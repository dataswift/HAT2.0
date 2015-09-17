package dalapi

import dal.Tables._
import dalapi.models._
import dalapi.service.BundleService
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.http.HttpMethods._
import spray.http._
import spray.json._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._

import dal.SlickPostgresDriver.simple._

class BundleServiceSpec extends Specification with Specs2RouteTest with BeforeAfterAll with BundleService {
  def actorRefFactory = system

  import JsonProtocol._

  // Prepare the data to create test bundles on
  def beforeAll() = {
    val dataTableRows = Seq(
      new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", "Fibaro"),
      new DataTableRow(4, LocalDateTime.now(), LocalDateTime.now(), "event", "Facebook")
    )

    val dataFieldRows = Seq(
      new DataFieldRow(10, LocalDateTime.now(), LocalDateTime.now(), "timestamp", 3),
      new DataFieldRow(11, LocalDateTime.now(), LocalDateTime.now(), "value", 3),
      new DataFieldRow(12, LocalDateTime.now(), LocalDateTime.now(), "name", 4),
      new DataFieldRow(13, LocalDateTime.now(), LocalDateTime.now(), "location", 4),
      new DataFieldRow(14, LocalDateTime.now(), LocalDateTime.now(), "startTime", 4),
      new DataFieldRow(15, LocalDateTime.now(), LocalDateTime.now(), "endTime", 4)
    )

    db.withSession { implicit session =>
      DataTable.forceInsertAll(dataTableRows: _*)
      DataField.forceInsertAll(dataFieldRows: _*)
    }
  }

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      BundleTableslicecondition.delete
      BundleTableslice.delete
      BundleJoin.delete
      BundleContextless.delete
      BundleTable.delete

      DataValue.delete
      DataRecord.delete
      DataField.delete
      DataTabletotablecrossref.delete
      DataTable.delete
    }
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
      HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleTableKitchen)) ~>
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
  }

  "Contextless Bundle Service for Joins" should {
    "Create and combine required bundles without join conditions" in {

      val bundleWeekendEvents = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleWeekendEvents)) ~>
        createBundleTable ~> check {
        responseAs[ApiBundleTable]
      }
      bundleWeekendEvents.id must beSome

      val bundleTableKitchen = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleTableKitchen)) ~>
        createBundleTable ~> check {
        responseAs[ApiBundleTable]
      }
      bundleTableKitchen.id must beSome

      val bundle = JsonParser(BundleExamples.bundleContextlessNoJoin).convertTo[ApiBundleContextless]
      bundle.tables must have size (2)

      val completeBundle = bundle.copy(tables = Seq(
        bundle.tables(0).copy(
          bundleTable = bundle.tables(0).bundleTable.copy(id = bundleWeekendEvents.id)
        ),
        bundle.tables(1).copy(
          bundleTable = bundle.tables(1).bundleTable.copy(id = bundleTableKitchen.id)
        )
      ))

      val bundleJson: String = completeBundle.toJson.toString

      import JsonProtocol._
      HttpRequest(POST, "/contextless", entity = HttpEntity(MediaTypes.`application/json`, bundleJson)) ~>
        createBundleContextless ~> check {
        response.status should be equalTo Created
      }

    }

    "Create and combine required bundles with join conditions" in {

      val bundleWeekendEvents = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleWeekendEvents)) ~>
        createBundleTable ~> check {
        responseAs[ApiBundleTable]
      }
      bundleWeekendEvents.id must beSome

      val bundleTableKitchen = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, BundleExamples.bundleTableKitchen)) ~>
        createBundleTable ~> check {
        responseAs[ApiBundleTable]
      }
      bundleTableKitchen.id must beSome

      val bundle = JsonParser(BundleExamples.bundleContextlessJoin).convertTo[ApiBundleContextless]
      bundle.tables must have size (2)

      val completeBundle = bundle.copy(tables = Seq(
        bundle.tables(0).copy(
          bundleTable = bundle.tables(0).bundleTable.copy(id = bundleWeekendEvents.id)
        ),
        bundle.tables(1).copy(
          bundleTable = bundle.tables(1).bundleTable.copy(id = bundleTableKitchen.id)
        )
      ))

      val bundleJson: String = completeBundle.toJson.toString

      import JsonProtocol._
      val bundleId = HttpRequest(POST, "/contextless", entity = HttpEntity(MediaTypes.`application/json`, bundleJson)) ~>
        createBundleContextless ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain(""""operator": "equal"""")
        responseAs[String] must contain(""""name": "startTime"""")
        responseAs[ApiBundleContextless].id
      }

      bundleId must beSome

      HttpRequest(GET, s"/contextless/${bundleId.get}") ~>
        getBundleContextless ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain(""""operator": "equal"""")
        responseAs[String] must contain(""""name": "startTime"""")
      }
    }

    "Return correct error code for bundle that doesn't exist" in {
      HttpRequest(GET, "/contextless/0") ~>
        getBundleContextless ~> check {
        response.status should be equalTo NotFound
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
      |            "operator": "equal"
      |          },
      |          {
      |            "field": {
      |              "id": 14,
      |              "tableId": 4,
      |              "name": "startTime"
      |            },
      |            "value": "saturday",
      |            "operator": "equal"
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
      |            "operator": "equal"
      |          },
      |          {
      |            "field": {
      |              "id": 14,
      |              "tableId": 4,
      |              "name": "startTime"
      |            },
      |            "value": "sunday",
      |            "operator": "equal"
      |          }
      |        ]
      |      }
      |    ]
      |  }
    """.stripMargin

  val bundleContextlessNoJoin =
  """
    |   {
    |     "name": "Kitchen electricity on weekend parties",
    |     "tables": [
    |       {
    |         "name": "Weekend events at home Combination",
    |         "bundleTable": {
    |           "id": 0,
    |           "name": "Weekend events at home",
    |           "table": {
    |             "id": 4,
    |             "name": "event",
    |             "source": "Facebook"
    |           }
    |         }
    |       },
    |       {
    |         "name": "Electricity in the kitchen Combination",
    |         "bundleTable": {
    |           "id": 0,
    |           "name": "Electricity in the kitchen",
    |           "table": {
    |             "id": 3,
    |             "name": "kichenElectricity",
    |             "source": "fibaro"
    |           }
    |         }
    |       }
    |     ]
    |   }
  """.stripMargin

  val bundleContextlessJoin =
    """
      |   {
      |     "name": "Kitchen electricity on weekend parties",
      |     "tables": [
      |       {
      |         "name": "Weekend events at home",
      |         "bundleTable": {
      |           "id": 0,
      |           "name": "Weekend events at home",
      |           "table": {
      |             "id": 4,
      |             "name": "event",
      |             "source": "Facebook"
      |           }
      |         }
      |       },
      |       {
      |         "name": "Electricity in the kitchen",
      |         "bundleTable": {
      |           "id": 0,
      |           "name": "Electricity in the kitchen",
      |           "table": {
      |             "id": 3,
      |             "name": "kichenElectricity",
      |             "source": "fibaro"
      |           }
      |         },
      |         "bundleJoinField": {
      |           "id": 14,
      |           "tableId": 4,
      |           "name": "startTime"
      |         },
      |         "bundleTableField": {
      |           "id": 10,
      |           "tableId": 3,
      |           "name": "timestamp"
      |         },
      |         "operator": "equal"
      |       }
      |     ]
      |   }
    """.stripMargin
}
