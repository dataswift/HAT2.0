package dalapi

import dalapi.models._
import dalapi.service.DataService
import org.specs2.mutable.Specification
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.Specs2RouteTest
import spray.json._
import spray.httpx.SprayJsonSupport._

class DataServiceSpec extends Specification with Specs2RouteTest with DataService {
  def actorRefFactory = system

  import JsonProtocol._

  sequential

  "DataService" should {
    "Accept new tables created" in {

      val dataTable = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchen)) ~> createTable ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("kitchen")
        responseAs[String] must contain("fibaro")
        responseAs[ApiDataTable]
      }

      dataTable.id must beSome

      val dataSubtable = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchenElectricity)) ~> createTable ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("kitchenElectricity")
        responseAs[String] must contain("fibaro")
        responseAs[ApiDataTable]
      }

      dataTable.id must beSome

      HttpRequest(POST, s"/table/${dataTable.id.get}/table/${dataSubtable.id.get}", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~> linkTableToTable ~> check {
        response.status should be equalTo OK
      }

      HttpRequest(GET, s"/table/${dataTable.id.get}") ~>
        getTable ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("kitchen")
        responseAs[String] must contain("subTables")
        responseAs[String] must contain("kitchenElectricity")
      }
    }
  }
}

object DataExamples {
  val tableKitchen =
    """{
      | "name": "kitchen",
      | "source": "fibaro"
    }""".stripMargin

  val tableKitchenElectricity =
    """{
      "name": "kitchenElectricity",
      "source": "fibaro"
    }"""

  val relationshipParent =
    """{
      | "relationshipType": "parent child"
    }""".stripMargin

}