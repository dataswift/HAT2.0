package hatdex.hat.api.endpoints

import java.util.UUID

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.authentication.HatAuthTestHandler
import hatdex.hat.api.endpoints.jsonExamples.DataDebitExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import org.mindrot.jbcrypt.BCrypt
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.testkit.Specs2RouteTest

class DataDebitSpec extends Specification with Specs2RouteTest with BeforeAfterAll with DataDebit {
  def actorRefFactory = system
  val logger: LoggingAdapter = system.log

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  val apiUser:User = User(UUID.randomUUID(), "alice@gmail.com", Some(BCrypt.hashpw("dr0w55ap", BCrypt.gensalt())), "Test User", "dataDebit")
  val ownerUser: User = User(UUID.randomUUID, "bob@gmail.com", Some(BCrypt.hashpw("pa55w0rd", BCrypt.gensalt())), "Test User", "owner")

  import JsonProtocol._

  val ownerAuth = "username=bob@gmail.com&password=pa55w0rd"

  // Prepare the data to create test bundles on
  def beforeAll() = {
    val dataTables = Seq(
      new DataTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "kitchen", "Fibaro"),
      new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", "Fibaro"),
      new DataTableRow(4, LocalDateTime.now(), LocalDateTime.now(), "event", "Facebook")
    )

    val dataTableCrossrefs = Seq(
      new DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "contains", 2, 3)
    )

    val dataFields = Seq(
      new DataFieldRow(10, LocalDateTime.now(), LocalDateTime.now(), "timestamp", 3),
      new DataFieldRow(11, LocalDateTime.now(), LocalDateTime.now(), "value", 3),
      new DataFieldRow(12, LocalDateTime.now(), LocalDateTime.now(), "name", 4),
      new DataFieldRow(13, LocalDateTime.now(), LocalDateTime.now(), "location", 4),
      new DataFieldRow(14, LocalDateTime.now(), LocalDateTime.now(), "startTime", 4),
      new DataFieldRow(15, LocalDateTime.now(), LocalDateTime.now(), "endTime", 4)
    )

    val dataRecords = Seq(
      new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 1"),
      new DataRecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 2"),
      new DataRecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 3"),
      new DataRecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "event record 1"),
      new DataRecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "event record 2"),
      new DataRecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "event record 3")
    )

    val dataValues = Seq(
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen time 1", 
        dataFields.find(_.name equals "timestamp").get.id, 
        dataRecords.find(_.name equals "kitchen record 1").get.id
      ),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen value 1", 
        dataFields.find(_.name equals "value").get.id, 
        dataRecords.find(_.name equals "kitchen record 1").get.id
      ),

      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen time 2", 
        dataFields.find(_.name equals "timestamp").get.id, 
        dataRecords.find(_.name equals "kitchen record 2").get.id
      ),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen value 2", 
        dataFields.find(_.name equals "value").get.id, 
        dataRecords.find(_.name equals "kitchen record 2").get.id
      ),

      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen time 3", 
        dataFields.find(_.name equals "timestamp").get.id, 
        dataRecords.find(_.name equals "kitchen record 3").get.id
      ),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "kitchen value 3", 
        dataFields.find(_.name equals "value").get.id, 
        dataRecords.find(_.name equals "kitchen record 3").get.id
      ),

      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event name 1", 
        dataFields.find(_.name equals "name").get.id, 
        dataRecords.find(_.name equals "event record 1").get.id
      ),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event location 1", 
        dataFields.find(_.name equals "location").get.id, 
        dataRecords.find(_.name equals "event record 1").get.id
      ),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event startTime 1", 
        dataFields.find(_.name equals "startTime").get.id, 
        dataRecords.find(_.name equals "event record 1").get.id
      ),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event endTime 1", 
        dataFields.find(_.name equals "endTime").get.id, 
        dataRecords.find(_.name equals "event record 1").get.id
      ),

      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event name 2", 
        dataFields.find(_.name equals "name").get.id, dataRecords.find(_.name equals "event record 2").get.id),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event location 2",
        dataFields.find(_.name equals "location").get.id, dataRecords.find(_.name equals "event record 2").get.id),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event startTime 2",
        dataFields.find(_.name equals "startTime").get.id, dataRecords.find(_.name equals "event record 2").get.id),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event endTime 2",
        dataFields.find(_.name equals "endTime").get.id, dataRecords.find(_.name equals "event record 2").get.id),

      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event name 3",
        dataFields.find(_.name equals "name").get.id, dataRecords.find(_.name equals "event record 3").get.id),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event location 3",
        dataFields.find(_.name equals "location").get.id, dataRecords.find(_.name equals "event record 3").get.id),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event startTime 3",
        dataFields.find(_.name equals "startTime").get.id, dataRecords.find(_.name equals "event record 3").get.id),
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event endTime 3",
        dataFields.find(_.name equals "endTime").get.id, dataRecords.find(_.name equals "event record 3").get.id)
    )

    //Bundle Tables
    val bundleTables = Seq(
      BundleContextlessTableRow(1, LocalDateTime.now(), LocalDateTime.now(), "Weekend events at home", dataTables.find(_.name equals "event").get.id),
      BundleContextlessTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "Electricity in the kitchen", dataTables.find(_.name equals "kichenElectricity").get.id)
    )

    // Bundle slices
    val slices = Seq(
      BundleContextlessTableSliceRow(1, LocalDateTime.now(), LocalDateTime.now(),
        bundleTables.find(_.name equals "Weekend events at home").get.id,
        dataTables.find(_.name equals "event").get.id),
      BundleContextlessTableSliceRow(2, LocalDateTime.now(), LocalDateTime.now(),
        bundleTables.find(_.name equals "Weekend events at home").get.id,
        dataTables.find(_.name equals "event").get.id)
    )

    // Bundle Slice conditions
    val sliceConditions = Seq(
      BundleContextlessTableSliceConditionRow(1, LocalDateTime.now(), LocalDateTime.now(),
        dataFields.find(_.name equals "location").get.id,
        slices(0).id,
        "equal", "event location 1"),
      BundleContextlessTableSliceConditionRow(2, LocalDateTime.now(), LocalDateTime.now(),
        dataFields.find(_.name equals "location").get.id,
        slices(1).id,
        "equal", "event location 2"),
      BundleContextlessTableSliceConditionRow(3, LocalDateTime.now(), LocalDateTime.now(),
        dataFields.find(_.name equals "startTime").get.id,
        slices(1).id,
        "equal", "event startTime 2")
    )

    // Contextless bundles
    val bundles = Seq(
      BundleContextlessRow(3, "Kitchen electricity on weekend parties", LocalDateTime.now(), LocalDateTime.now())
    )

    // Bundle joins (combinations of bundle tables)
    val bundleJoins = Seq(
      BundleContextlessJoinRow(1, LocalDateTime.now(), LocalDateTime.now(), "Weekend events at home",
        bundleTables.find(_.name equals "Weekend events at home").get.id,
        bundles.find(_.name equals "Kitchen electricity on weekend parties").get.id,
        None, None, Some("equal")),
      BundleContextlessJoinRow(2, LocalDateTime.now(), LocalDateTime.now(), "Electricity in the kitchen",
        bundleTables.find(_.name equals "Electricity in the kitchen").get.id,
        bundles.find(_.name equals "Kitchen electricity on weekend parties").get.id,
        None, None, Some("equal"))
    )

    db.withSession { implicit session =>
      TestDataCleanup.cleanupAll

      DataTable.forceInsertAll(dataTables: _*)
      DataTabletotablecrossref.forceInsertAll(dataTableCrossrefs: _*)
      DataField.forceInsertAll(dataFields: _*)
      DataRecord.forceInsertAll(dataRecords: _*)
      // Don't _foce_ insert all data values -- IDs don't particularly matter to us
      DataValue.insertAll(dataValues: _*)

      BundleContextlessTable.forceInsertAll(bundleTables: _*)
      BundleContextlessTableSlice.forceInsertAll(slices: _*)
      BundleContextlessTableSliceCondition.forceInsertAll(sliceConditions: _*)
      BundleContextless.forceInsertAll(bundles: _*)
      BundleContextlessJoin.forceInsertAll(bundleJoins: _*)
    }
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
    "Accept a Data Debit proposal" in {

      val dataDebit = {
        implicit val user:User = apiUser
        val dataDebit = HttpRequest(POST, "/propose?"+ownerAuth, entity = HttpEntity(MediaTypes.`application/json`, DataDebitExamples.dataDebitExample)) ~>
          proposeDataDebitApi ~> check {
          val responseString = responseAs[String]
          responseString must contain("key")
          responseAs[ApiDataDebit]
        }

        HttpRequest(GET, s"/${dataDebit.key.get}/values?"+ownerAuth) ~> sealRoute(retrieveDataDebitValuesApi) ~> check {
          response.status should be equalTo Forbidden
        }

        dataDebit
      }

      dataDebit.key must beSome

      val t = {
        implicit val user:User = ownerUser
        HttpRequest(PUT, s"/${dataDebit.key.get}/enable?"+ownerAuth) ~> sealRoute(enableDataDebitApi) ~> check {
          response.status should be equalTo OK
        }
      }
      
      val result = {
        implicit val user:User = apiUser
        HttpRequest(GET, s"/${dataDebit.key.get}/values?"+ownerAuth) ~> sealRoute(retrieveDataDebitValuesApi) ~> check {
          response.status should be equalTo OK
          responseAs[ApiDataDebitOut]
        }
      }
      result.bundleContextless must beSome
    }
  }
}

