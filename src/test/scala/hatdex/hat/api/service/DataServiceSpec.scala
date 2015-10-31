package hatdex.hat.api.service

import akka.event.{LoggingAdapter, Logging}
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.authentication.HatAuthTestHandler
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.jsonExamples.DataExamples
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.testkit.Specs2RouteTest

import scala.concurrent.duration._

class DataServiceSpec extends Specification with Specs2RouteTest with DataService with BeforeAfterAll {
  def actorRefFactory = system
  val logger: LoggingAdapter = system.log

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  implicit val routeTestTimeout = RouteTestTimeout(5.second)

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
  
  val ownerAuthParams = "?username=bob@gmail.com&password=pa55w0rd"

  "DataService" should {
    "Accept new tables created" in {
      val dataTable = HttpRequest(POST, "/table"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchen)) ~>
        sealRoute(createTableApi) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("kitchen")
        responseAs[String] must contain("fibaro")
        responseAs[ApiDataTable]
      }

      dataTable.id must beSome

      val dataSubtable = HttpRequest(POST, "/table"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchenElectricity)) ~>
        sealRoute(createTableApi) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("kitchenElectricity")
        responseAs[String] must contain("fibaro")
        responseAs[ApiDataTable]
      }

      dataTable.id must beSome

      HttpRequest(POST, s"/table/${dataTable.id.get}/table/${dataSubtable.id.get}"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkTableToTableApi) ~> check {
        response.status should be equalTo OK
      }

      HttpRequest(GET, s"/table/${dataTable.id.get}"+ownerAuthParams) ~>
        sealRoute(getTableApi) ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("kitchen")
        responseAs[String] must contain("subTables")
        responseAs[String] must contain("kitchenElectricity")
      }
    }

    "Accept new nested tables" in {
      val dataTable = HttpRequest(POST, "/table"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.nestedTableKitchen)) ~>
        sealRoute(createTableApi) ~> check {
        response.status should be equalTo Created
        val responseString = responseAs[String]
        responseString must contain("kitchen")
        responseString must contain("fibaro")
        responseString must contain("kitchenElectricity")
        responseString must contain("tableTestField4")
        responseAs[ApiDataTable]
      }
      dataTable.id must beSome

      HttpRequest(GET, s"/table/${dataTable.id.get}"+ownerAuthParams) ~>
        sealRoute(getTableApi) ~> check {
        val responseString = responseAs[String]
        responseString must contain("kitchen")
        responseString must contain("fibaro")
        responseString must contain("kitchenElectricity")
        responseString must contain("tableTestField4")
      }
    }

    "Reject incorrect table linking" in {
      HttpRequest(POST, s"/table/0/table/1"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkTableToTableApi) ~> check {
        response.status should be equalTo BadRequest
      }
    }

    "Allow table fields to be created" in {
      val dataTable = HttpRequest(POST, "/table"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchenElectricity)) ~>
        sealRoute(createTableApi) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("kitchenElectricity")
        responseAs[String] must contain("fibaro")
        responseAs[ApiDataTable]
      }

      dataTable.id must beSome

      val field = JsonParser(DataExamples.testField).convertTo[ApiDataField]
      val completeField = field.copy(tableId = dataTable.id)

      HttpRequest(POST, "/field"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, completeField.toJson.toString)) ~>
        sealRoute(createFieldApi) ~> check {
        response.status should be equalTo Created
      }

      HttpRequest(GET, s"/table/${dataTable.id.get}"+ownerAuthParams) ~>
        sealRoute(getTableApi) ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("kitchen")
        responseAs[String] must contain("fields")
        responseAs[String] must contain("tableTestField")
      }
    }

    "Reject fields to non-existing tables" in {
      HttpRequest(POST, "/field"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.testField)) ~>
        sealRoute(createFieldApi) ~> check {
        response.status should be equalTo BadRequest
      }
    }

    "Accept and provide data in the right formats" in {
      // Create main table
      val dataTable = HttpRequest(POST, "/table"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchen)) ~>
        sealRoute(createTableApi) ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataTable]
      }

      dataTable.id must beSome

      // Create sub-table
      val dataSubtable = HttpRequest(POST, "/table"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchenElectricity)) ~>
        sealRoute(createTableApi) ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataTable]
      }

      dataTable.id must beSome

      // Link table with subtable
      HttpRequest(POST, s"/table/${dataTable.id.get}/table/${dataSubtable.id.get}"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(linkTableToTableApi) ~> check {
        response.status should be equalTo OK
      }

      // Create fields
      val field = JsonParser(DataExamples.testField).convertTo[ApiDataField]
      val completeTableField = field.copy(tableId = dataTable.id)

      val dataField = HttpRequest(POST, "/field"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, completeTableField.toJson.toString)) ~>
        sealRoute(createFieldApi) ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataField]
      }
      dataField.id must beSome

      val completeSubTableField1 = field.copy(tableId = dataSubtable.id, name = "subtableTestField1")
      val dataSubfield1 = HttpRequest(POST, "/field"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, completeSubTableField1.toJson.toString)) ~>
        sealRoute(createFieldApi) ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataField]
      }
      dataSubfield1.id must beSome

      val completeSubTableField2 = field.copy(tableId = dataSubtable.id, name = "subtableTestField2")
      val dataSubfield2 = HttpRequest(POST, "/field"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, completeSubTableField2.toJson.toString)) ~>
        sealRoute(createFieldApi) ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataField]
      }
      dataSubfield2.id must beSome

      // Create Data Record
      val record = HttpRequest(POST, "/record"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.testRecord)) ~>
        sealRoute(createRecordApi) ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataRecord]
      }
      record.id must beSome

      // Batch-fill it with data
      val dataValues = Seq(
        new ApiDataValue(None, None, None, "testValue1", Some(dataField), None),
        new ApiDataValue(None, None, None, "testValue2", Some(dataSubfield1), None),
        new ApiDataValue(None, None, None, "testValue3", Some(dataSubfield2), None)
      )

      HttpRequest(POST, s"/record/${record.id.get}/values"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, dataValues.toJson.toString)) ~>
        sealRoute(storeValueListApi) ~> check {
        response.status should be equalTo Created
      }

      // Create another record
      val record2 = HttpRequest(POST, "/record"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, DataExamples.testRecord2)) ~>
        sealRoute(createRecordApi) ~> check {
        response.status should be equalTo Created
        responseAs[ApiDataRecord]
      }
      record2.id must beSome

      // Fill it with data one-by-one
      val dataValues2 = Seq(
        new ApiDataValue(None, None, None, "testValue2-1", Some(dataField), Some(record2)),
        new ApiDataValue(None, None, None, "testValue2-2", Some(dataSubfield1), Some(record2)),
        new ApiDataValue(None, None, None, "testValue2-3", Some(dataSubfield2), Some(record2))
      )

      dataValues2 map { dataValue =>
        HttpRequest(POST, "/value"+ownerAuthParams, entity = HttpEntity(MediaTypes.`application/json`, dataValue.toJson.toString)) ~>
          sealRoute(createValueApi) ~> check {
          response.status should be equalTo Created
        }
      }

      // Make sure that the right data elements are contained in the different kinds of responses
      HttpRequest(GET, s"/record/${record.id.get}/values"+ownerAuthParams) ~>
        sealRoute(getRecordValuesApi) ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("testValue1")
        responseAs[String] must contain("testValue2")
        responseAs[String] must contain("testValue3")
        responseAs[String] must not contain ("testValue2-1")
        responseAs[String] must contain("testRecord 1")
      }

      HttpRequest(GET, s"/field/${dataField.id.get}/values"+ownerAuthParams) ~>
        sealRoute(getFieldValuesApi) ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("testValue1")
        responseAs[String] must contain("testValue2-1")
        responseAs[String] must not contain ("testValue3")
      }

      HttpRequest(GET, s"/table/${dataTable.id.get}/values"+ownerAuthParams) ~>
        sealRoute(getTableValuesApi) ~> check {
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

