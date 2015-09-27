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

      val dataTable = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchen)) ~> createTableApi ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("kitchen")
        responseAs[String] must contain("fibaro")
        responseAs[ApiDataTable]
      }

      dataTable.id must beSome

      val dataSubtable = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchenElectricity)) ~> createTableApi ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("kitchenElectricity")
        responseAs[String] must contain("fibaro")
        responseAs[ApiDataTable]
      }

      dataTable.id must beSome

      HttpRequest(POST, s"/table/${dataTable.id.get}/table/${dataSubtable.id.get}", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~> linkTableToTableApi ~> check {
        response.status should be equalTo OK
      }

      HttpRequest(GET, s"/table/${dataTable.id.get}") ~>
        getTableApi ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("kitchen")
        responseAs[String] must contain("subTables")
        responseAs[String] must contain("kitchenElectricity")
      }
    }

    "Reject incorrect table linking" in {
      HttpRequest(POST, s"/table/0/table/1", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~> linkTableToTableApi ~> check {
        response.status should be equalTo BadRequest
      }
    }

    "Allow table fields to be created" in {
      val dataTable = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchenElectricity)) ~> createTableApi ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("kitchenElectricity")
        responseAs[String] must contain("fibaro")
        responseAs[ApiDataTable]
      }

      dataTable.id must beSome

      val field = JsonParser(DataExamples.testField).convertTo[ApiDataField]
      val completeField = field.copy(tableId = dataTable.id.get)

      HttpRequest(POST, "/field", entity = HttpEntity(MediaTypes.`application/json`, completeField.toJson.toString)) ~> createFieldApi ~> check {
        response.status should be equalTo Created
      }

      HttpRequest(GET, s"/table/${dataTable.id.get}") ~>
        getTableApi ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("kitchen")
        responseAs[String] must contain("fields")
        responseAs[String] must contain("testField")
      }
    }

    "Reject fields to non-existing tables" in {
      HttpRequest(POST, "/field", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.testField)) ~> createFieldApi ~> check {
        response.status should be equalTo BadRequest
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

  val testField =
    """
      |{
      | "name": "testField",
      | "tableId": 0
      |}
    """.stripMargin

}