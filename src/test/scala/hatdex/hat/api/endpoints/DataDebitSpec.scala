package hatdex.hat.api.endpoints

import java.util.UUID

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.endpoints.jsonExamples.DataDebitExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import org.mindrot.jbcrypt.BCrypt
import org.specs2.mutable.Specification
import org.specs2.specification.{BeforeAfterAll, Scope}
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.json._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._

class DataDebitSpec extends Specification with Specs2RouteTest with BeforeAfterAll with DataDebit {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  import JsonProtocol._

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  val apiUser: User = User(UUID.randomUUID(), "alice@gmail.com", Some(BCrypt.hashpw("dr0w55ap", BCrypt.gensalt())), "Test User", "dataDebit")
  val parameters = Map("username" -> "bob@gmail.com", "password" -> "pa55w0rd")

  def appendParams(parameters: Map[String, String]): String = {
    parameters.foldLeft[String]("?") { case (params, (key, value)) =>
      s"$params$key=$value&"
    }
  }

  trait LoggingHttpService {
    def actorRefFactory = system

    val logger = system.log
  }

  val bundlesService = new Bundles with LoggingHttpService
  val bundleContextService = new BundlesContext with LoggingHttpService {
    val eventsService = new Event with LoggingHttpService
    val locationsService = new Location with LoggingHttpService
    val peopleService = new Person with LoggingHttpService
    val thingsService = new Thing with LoggingHttpService
    val organisationsService = new Organisation with LoggingHttpService
  }

  // Prepare the data to create test bundles on
  def beforeAll() = {
//    val dataTables = Seq(
//      new DataTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "dd_kitchen", "dd_Fibaro"),
//      new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "dd_kichenElectricity", "dd_Fibaro"),
//      new DataTableRow(4, LocalDateTime.now(), LocalDateTime.now(), "dd_event", "dd_Facebook")
//    )
//
//    val dataTableCrossrefs = Seq(
//      new DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "contains", 2, 3)
//    )
//
//    val dataFields = Seq(
//      new DataFieldRow(10, LocalDateTime.now(), LocalDateTime.now(), "timestamp", 3),
//      new DataFieldRow(11, LocalDateTime.now(), LocalDateTime.now(), "value", 3),
//      new DataFieldRow(12, LocalDateTime.now(), LocalDateTime.now(), "name", 4),
//      new DataFieldRow(13, LocalDateTime.now(), LocalDateTime.now(), "location", 4),
//      new DataFieldRow(14, LocalDateTime.now(), LocalDateTime.now(), "startTime", 4),
//      new DataFieldRow(15, LocalDateTime.now(), LocalDateTime.now(), "endTime", 4)
//    )
//
//    val dataRecords = Seq(
//      new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 1"),
//      new DataRecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 2"),
//      new DataRecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 3"),
//      new DataRecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "event record 1"),
//      new DataRecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "event record 2"),
//      new DataRecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "event record 3")
//    )
//
//    val dataValues = Seq(
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen time 1",
//        dataFields.find(_.name equals "timestamp").get.id,
//        dataRecords.find(_.name equals "kitchen record 1").get.id
//      ),
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen value 1",
//        dataFields.find(_.name equals "value").get.id,
//        dataRecords.find(_.name equals "kitchen record 1").get.id
//      ),
//
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen time 2",
//        dataFields.find(_.name equals "timestamp").get.id,
//        dataRecords.find(_.name equals "kitchen record 2").get.id
//      ),
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen value 2",
//        dataFields.find(_.name equals "value").get.id,
//        dataRecords.find(_.name equals "kitchen record 2").get.id
//      ),
//
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen time 3",
//        dataFields.find(_.name equals "timestamp").get.id,
//        dataRecords.find(_.name equals "kitchen record 3").get.id
//      ),
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen value 3",
//        dataFields.find(_.name equals "value").get.id,
//        dataRecords.find(_.name equals "kitchen record 3").get.id
//      ),
//
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event name 1",
//        dataFields.find(_.name equals "name").get.id,
//        dataRecords.find(_.name equals "event record 1").get.id
//      ),
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event location 1",
//        dataFields.find(_.name equals "location").get.id,
//        dataRecords.find(_.name equals "event record 1").get.id
//      ),
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event startTime 1",
//        dataFields.find(_.name equals "startTime").get.id,
//        dataRecords.find(_.name equals "event record 1").get.id
//      ),
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event endTime 1",
//        dataFields.find(_.name equals "endTime").get.id,
//        dataRecords.find(_.name equals "event record 1").get.id
//      ),
//
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event name 2",
//        dataFields.find(_.name equals "name").get.id, dataRecords.find(_.name equals "event record 2").get.id),
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event location 2",
//        dataFields.find(_.name equals "location").get.id, dataRecords.find(_.name equals "event record 2").get.id),
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event startTime 2",
//        dataFields.find(_.name equals "startTime").get.id, dataRecords.find(_.name equals "event record 2").get.id),
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event endTime 2",
//        dataFields.find(_.name equals "endTime").get.id, dataRecords.find(_.name equals "event record 2").get.id),
//
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event name 3",
//        dataFields.find(_.name equals "name").get.id, dataRecords.find(_.name equals "event record 3").get.id),
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event location 3",
//        dataFields.find(_.name equals "location").get.id, dataRecords.find(_.name equals "event record 3").get.id),
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event startTime 3",
//        dataFields.find(_.name equals "startTime").get.id, dataRecords.find(_.name equals "event record 3").get.id),
//      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event endTime 3",
//        dataFields.find(_.name equals "endTime").get.id, dataRecords.find(_.name equals "event record 3").get.id)
//    )
//
//    //Bundle Tables
//    val bundleTables = Seq(
//      BundleContextlessTableRow(1, LocalDateTime.now(), LocalDateTime.now(), "Weekend events at home", dataTables.find(_.name equals "dd_event").get.id),
//      BundleContextlessTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "Electricity in the kitchen", dataTables.find(_.name equals "dd_kichenElectricity").get.id)
//    )
//
//    // Bundle slices
//    val slices = Seq(
//      BundleContextlessTableSliceRow(1, LocalDateTime.now(), LocalDateTime.now(),
//        bundleTables.find(_.name equals "Weekend events at home").get.id,
//        dataTables.find(_.name equals "dd_event").get.id),
//      BundleContextlessTableSliceRow(2, LocalDateTime.now(), LocalDateTime.now(),
//        bundleTables.find(_.name equals "Weekend events at home").get.id,
//        dataTables.find(_.name equals "dd_event").get.id)
//    )
//
//    // Bundle Slice conditions
//    val sliceConditions = Seq(
//      BundleContextlessTableSliceConditionRow(1, LocalDateTime.now(), LocalDateTime.now(),
//        dataFields.find(_.name equals "location").get.id,
//        slices(0).id,
//        "equal", "event location 1"),
//      BundleContextlessTableSliceConditionRow(2, LocalDateTime.now(), LocalDateTime.now(),
//        dataFields.find(_.name equals "location").get.id,
//        slices(1).id,
//        "equal", "event location 2"),
//      BundleContextlessTableSliceConditionRow(3, LocalDateTime.now(), LocalDateTime.now(),
//        dataFields.find(_.name equals "startTime").get.id,
//        slices(1).id,
//        "equal", "event startTime 2")
//    )
//
//    // Contextless bundles
//    val bundles = Seq(
//      BundleContextlessRow(3, "Kitchen electricity on weekend parties", LocalDateTime.now(), LocalDateTime.now())
//    )
//
//    // Bundle joins (combinations of bundle tables)
//    val bundleJoins = Seq(
//      BundleContextlessJoinRow(1, LocalDateTime.now(), LocalDateTime.now(), "Weekend events at home",
//        bundleTables.find(_.name equals "Weekend events at home").get.id,
//        bundles.find(_.name equals "Kitchen electricity on weekend parties").get.id,
//        None, None, Some("equal")),
//      BundleContextlessJoinRow(2, LocalDateTime.now(), LocalDateTime.now(), "Electricity in the kitchen",
//        bundleTables.find(_.name equals "Electricity in the kitchen").get.id,
//        bundles.find(_.name equals "Kitchen electricity on weekend parties").get.id,
//        None, None, Some("equal"))
//    )
//
//    db.withSession { implicit session =>
//      TestDataCleanup.cleanupAll
//
//      DataTable.forceInsertAll(dataTables: _*)
//      DataTabletotablecrossref.forceInsertAll(dataTableCrossrefs: _*)
//      DataField.forceInsertAll(dataFields: _*)
//      DataRecord.forceInsertAll(dataRecords: _*)
//      // Don't _foce_ insert all data values -- IDs don't particularly matter to us
//      DataValue.insertAll(dataValues: _*)
//
//      BundleContextlessTable.forceInsertAll(bundleTables: _*)
//      BundleContextlessTableSlice.forceInsertAll(slices: _*)
//      BundleContextlessTableSliceCondition.forceInsertAll(sliceConditions: _*)
//      BundleContextless.forceInsertAll(bundles: _*)
//      BundleContextlessJoin.forceInsertAll(bundleJoins: _*)
//
//      logger.debug("Contextual bundle:" + bundlesService.getBundleContextlessById(3).toJson.toString)
//      session.close()
//    }
  }

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      TestDataCleanup.cleanupAll
    }
//    db.close
  }

  sequential

  "Data Debit Service" should {
//    "Accept a contextless Data Debit proposal" in {
//
//      val dataDebit = {
//        val dataDebit = HttpRequest(POST,
//          "/dataDebit/propose" + appendParams(parameters),
//          entity = HttpEntity(MediaTypes.`application/json`, DataDebitExamples.dataDebitExample)
//        ) ~>
//          sealRoute(routes) ~>
//          check {
//            response.status should be equalTo Created
//            val responseString = responseAs[String]
//            responseString must contain("key")
//            responseAs[ApiDataDebit]
//          }
//
//        HttpRequest(GET, s"/${dataDebit.key.get}/values" + appendParams(parameters)) ~> sealRoute(retrieveDataDebitValuesApi) ~> check {
//          response.status should be equalTo Forbidden
//        }
//
//        dataDebit
//      }
//
//      dataDebit.key must beSome
//
//      val t = {
//        HttpRequest(PUT,
//          s"/dataDebit/${dataDebit.key.get}/enable" + appendParams(parameters)
//        ) ~>
//          sealRoute(routes) ~>
//          check {
//            response.status should be equalTo OK
//          }
//      }
//
//      val result = {
//        HttpRequest(GET,
//          s"/dataDebit/${dataDebit.key.get}/values" + appendParams(parameters)
//        ) ~>
//          sealRoute(routes) ~>
//          check {
//            response.status should be equalTo OK
//            responseAs[ApiDataDebitOut]
//          }
//      }
//      result.bundleContextless must beSome
//    }

    object Context {
      val propertySpec = new PropertySpec()
      val property = propertySpec.createWeightProperty
      val dataSpec = new DataSpec()
      dataSpec.createBasicTables
      val populatedData = dataSpec.populateDataReusable

      val personSpec = new PersonSpec()

      val newPerson = personSpec.createNewPerson
      newPerson.id must beSome

      val dataField = populatedData match {
        case (dataTable, dataField, record) =>
          dataField
      }
      val dynamicPropertyLink = ApiPropertyRelationshipDynamic(
        None, property, None, None, "test property", dataField)

      val propertyLinkId = HttpRequest(
        POST, s"/person/${newPerson.id.get}/property/dynamic/${property.id.get}" + appendParams(parameters),
        entity = HttpEntity(MediaTypes.`application/json`, dynamicPropertyLink.toJson.toString)
      ) ~>
        sealRoute(personSpec.routes) ~>
        check {
          eventually {
            response.status should be equalTo Created
          }
          responseAs[ApiGenericId]
        }

      val personValues = HttpRequest(GET, s"/person/${newPerson.id.get}/values" + appendParams(parameters)) ~>
        sealRoute(personSpec.routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[String] must contain("testValue1")
            responseAs[String] must contain("testValue2-1")
            responseAs[String] must not contain ("testValue3")
          }
        }
    }

    class Context extends Scope {
      val property = Context.property
      val populatedData = Context.populatedData
    }

    "Accept a contextual Data Debit proposal" in new Context {

      val dataDebit = {
        val dataDebit = HttpRequest(POST,
          "/dataDebit/propose" + appendParams(parameters),
          entity = HttpEntity(MediaTypes.`application/json`, DataDebitExamples.dataDebitContextual)
        ) ~>
          sealRoute(routes) ~>
          check {
            response.status should be equalTo Created
            val responseString = responseAs[String]
            responseString must contain("key")
            responseAs[ApiDataDebit]
          }

        HttpRequest(GET, s"/${dataDebit.key.get}/values" + appendParams(parameters)) ~> sealRoute(retrieveDataDebitValuesApi) ~> check {
          response.status should be equalTo Forbidden
        }

        dataDebit
      }

      dataDebit.key must beSome

      val t = {
        HttpRequest(PUT,
          s"/dataDebit/${dataDebit.key.get}/enable" + appendParams(parameters)
        ) ~>
          sealRoute(routes) ~>
          check {
            response.status should be equalTo OK
          }
      }


      HttpRequest(GET,
        s"/dataDebit/${dataDebit.key.get}/values" + appendParams(parameters)
      ) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo OK
          val resp = responseAs[String]
          resp must contain("HATperson")
          resp must contain("testValue1")
          resp must contain("testValue2-1")
          responseAs[Seq[ApiEntity]]
        }
    }
  }
}

