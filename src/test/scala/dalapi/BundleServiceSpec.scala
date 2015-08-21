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

class BundleServiceSpec extends Specification with Specs2RouteTest with BeforeAfterAll with BundleService {
  def actorRefFactory = system

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
