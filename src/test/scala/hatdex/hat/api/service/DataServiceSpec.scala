package hatdex.hat.api.service

import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.models._
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.testkit.Specs2RouteTest

class DataServiceSpec extends Specification with Specs2RouteTest with DataService with BeforeAfterAll {
  def actorRefFactory = system

  import JsonProtocol._

  def beforeAll() = {

  }

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      TestDataCleanup.cleanupAll
    }
    db.close
  }

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
        responseAs[String] must contain("tableTestField")
      }
    }

    "Reject fields to non-existing tables" in {
      HttpRequest(POST, "/field", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.testField)) ~> createFieldApi ~> check {
        response.status should be equalTo BadRequest
      }
    }

    "Accept and provide data in the right formats" in {
      // Create main table
      val dataTable = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchen)) ~> createTableApi ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataTable]
      }

      dataTable.id must beSome

      // Create sub-table
      val dataSubtable = HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchenElectricity)) ~> createTableApi ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataTable]
      }

      dataTable.id must beSome

      // Link table with subtable
      HttpRequest(POST, s"/table/${dataTable.id.get}/table/${dataSubtable.id.get}", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~> linkTableToTableApi ~> check {
        response.status should be equalTo OK
      }

      // Create fields
      val field = JsonParser(DataExamples.testField).convertTo[ApiDataField]
      val completeTableField = field.copy(tableId = dataTable.id.get)

      val dataField = HttpRequest(POST, "/field", entity = HttpEntity(MediaTypes.`application/json`, completeTableField.toJson.toString)) ~>
        createFieldApi ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataField]
      }
      dataField.id must beSome

      val completeSubTableField1 = field.copy(tableId = dataSubtable.id.get, name = "subtableTestField1")
      val dataSubfield1 = HttpRequest(POST, "/field", entity = HttpEntity(MediaTypes.`application/json`, completeSubTableField1.toJson.toString)) ~>
        createFieldApi ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataField]
      }
      dataSubfield1.id must beSome

      val completeSubTableField2 = field.copy(tableId = dataSubtable.id.get, name = "subtableTestField2")
      val dataSubfield2 = HttpRequest(POST, "/field", entity = HttpEntity(MediaTypes.`application/json`, completeSubTableField2.toJson.toString)) ~>
        createFieldApi ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataField]
      }
      dataSubfield2.id must beSome

      // Create Data Record
      val record = HttpRequest(POST, "/record", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.testRecord)) ~>
        createRecordApi ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataRecord]
      }
      record.id must beSome

      // Batch-fill it with data
      val dataValues = Seq(
        new ApiDataValue(None, None, None, "testValue1", dataField.id.get, record.id.get),
        new ApiDataValue(None, None, None, "testValue2", dataSubfield1.id.get, record.id.get),
        new ApiDataValue(None, None, None, "testValue3", dataSubfield2.id.get, record.id.get)
      )

      HttpRequest(POST, "/value/list", entity = HttpEntity(MediaTypes.`application/json`, dataValues.toJson.toString)) ~>
        storeValueListApi ~> check {
        response.status should be equalTo Created
      }

      // Create another record
      val record2 = HttpRequest(POST, "/record", entity = HttpEntity(MediaTypes.`application/json`, DataExamples.testRecord2)) ~>
        createRecordApi ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataRecord]
      }
      record2.id must beSome

      // Fill it with data one-by-one
      val dataValues2 = Seq(
        new ApiDataValue(None, None, None, "testValue2-1", dataField.id.get, record2.id.get),
        new ApiDataValue(None, None, None, "testValue2-2", dataSubfield1.id.get, record2.id.get),
        new ApiDataValue(None, None, None, "testValue2-3", dataSubfield2.id.get, record2.id.get)
      )

      dataValues2 map { dataValue =>
        HttpRequest(POST, "/value", entity = HttpEntity(MediaTypes.`application/json`, dataValue.toJson.toString)) ~>
          createValueApi ~> check {
          response.status should be equalTo Created
        }
      }

      // Make sure that the right data elements are contained in the different kinds of responses
      HttpRequest(GET, s"/record/${record.id.get}/values") ~> getRecordValuesApi ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("testValue1")
        responseAs[String] must contain("testValue2")
        responseAs[String] must contain("testValue3")
        responseAs[String] must not contain("testValue2-1")
        responseAs[String] must contain("testRecord 1")
      }

      HttpRequest(GET, s"/field/${dataField.id.get}/values") ~> getFieldValuesApi ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("testValue1")
        responseAs[String] must contain("testValue2-1")
        responseAs[String] must not contain("testValue3")
      }

      HttpRequest(GET, s"/table/${dataTable.id.get}/values") ~> getTableValuesApi ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("testValue1")
        responseAs[String] must contain("testValue2")
        responseAs[String] must contain("testValue3")
        responseAs[String] must contain("testValue2-1")
        responseAs[String] must contain("testValue2-2")
        responseAs[String] must contain("testValue2-3")
        responseAs[String] must contain("testRecord 1")
        responseAs[String] must contain("testRecord 2")
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
      | "name": "tableTestField",
      | "tableId": 0
      |}
    """.stripMargin

  val testRecord =
  """{
    | "name": "testRecord 1"
  }""".stripMargin

  val testRecord2 =
    """{
      | "name": "testRecord 2"
  }""".stripMargin
}