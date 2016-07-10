package hatdex.hat.api.service

import java.util.UUID

import akka.actor.{ ActorSystem, ActorRefFactory, ActorContext }
import akka.event.{ Logging, LoggingAdapter }
import hatdex.hat.api.{ DatabaseInfo, TestDataCleanup }
import hatdex.hat.api.endpoints._
import hatdex.hat.api.endpoints.jsonExamples.{ BundleExamples, DataDebitExamples }
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.authenticators.AccessTokenHandler
import hatdex.hat.authentication.{ TestAuthCredentials, HatAuthTestHandler }
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import org.mindrot.jbcrypt.BCrypt
import org.specs2.mutable.Specification
import org.specs2.specification.{ BeforeAfterAll, Scope }
import spray.http.HttpRequest
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.json._
import spray.testkit.Specs2RouteTest
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport._

class StatsServiceSpec extends Specification with Specs2RouteTest with BeforeAfterAll with StatsService {
  override val logger: LoggingAdapter = Logging.getLogger(system, "tests")
  lazy val testLogger = logger
  override def actorRefFactory: ActorRefFactory = system

  // Prepare the data to create test bundles on
  def beforeAll() = {

  }

  // Clean up all data
  def afterAll() = {
    import hatdex.hat.dal.SlickPostgresDriver.simple._
    DatabaseInfo.db.withSession { implicit session =>
      TestDataCleanup.cleanupAll
      session.close()
    }
  }

  logger.info("Setting up Stats Service context")
  object DataDebitContext extends DataDebitContextualContext with DataDebitRequiredServices {
    def actorRefFactory = system
    val logger: LoggingAdapter = Logging.getLogger(system, "tests")
    override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
    import JsonProtocol._

    HatAuthTestHandler.validUsers.find(_.role == "owner") map { user =>
      UserUserRow(user.userId,
        LocalDateTime.now(), LocalDateTime.now(),
        user.email, user.pass,
        user.name, user.role, enabled = true)
    } map { userRow =>
      import hatdex.hat.dal.SlickPostgresDriver.api._
      db.run {
        (UserUser += userRow).asTry
      }
    }

    val contextlessBundle = BundleExamples.fullbundle

    val bundleData = JsonParser(contextlessBundle).convertTo[ApiBundleContextless]
    val dataDebitData = JsonParser(DataDebitExamples.dataDebitExample).convertTo[ApiDataDebit]

    val dataDebit = {
      HttpRequest(POST, "/dataDebit/propose")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, dataDebitData.copy(bundleContextless = Some(bundleData)).toJson.toString)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          val responseString = responseAs[String]
          responseString must contain("key")
          responseAs[ApiDataDebit]
        }
    }

    dataDebit.key must beSome

    HttpRequest(PUT, s"/dataDebit/${dataDebit.key.get}/enable")
      .withHeaders(ownerAuthHeader) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo OK
      }
  }

  class Context extends Scope {
    val property = DataDebitContext.property
    val populatedData = DataDebitContext.populatedData
    val populatedTable = DataDebitContext.dataTable
    val dataDebit = DataDebitContext.dataDebit
  }

  //  object StatsServiceSpecContext extends StatsServiceContext with DataDebitRequiredServices {
  //    def actorRefFactory: ActorRefFactory = system
  //    override val logger: LoggingAdapter = Logging.getLogger(system, "tests")
  //    logger.info("Stats Service context object created")
  //  }
  //
  //  class StatsServiceSpecContext extends Scope {
  //    val dataDebit = StatsServiceSpecContext.dataDebit
  //  }

  import JsonProtocol._

  sequential

  "Stats Service computations" should {
    val valuesString = hatdex.hat.api.endpoints.jsonExamples.DataDebitExamples.dataDebitContextlessValues
    val data = JsonParser(valuesString).convertTo[ApiDataDebitOut]

    "Correctly compute table value counts" in {
      data.bundleContextless must beSome
      val bundleContextless = data.bundleContextless.get
      val firstDataset = bundleContextless.dataGroups.head._2.head
      val stats = getTableValueCounts(firstDataset)
      // Must have extracted the right number of tables
      stats.keys.toSeq.length must be equalTo (1)
      stats.values.toSeq.head must be equalTo (12)
    }

    "Correctly compute field value counts" in {
      data.bundleContextless must beSome
      val bundleContextless = data.bundleContextless.get
      val firstDataset = bundleContextless.dataGroups.head._2.head
      val stats = getFieldValueCounts(firstDataset)
      stats map { stat =>
        stat._2 must be equalTo (3)
      }
      stats.keys.toSeq.length must be equalTo (4)
    }

    "Correctly compute overall data bundle stats" in {
      data.bundleContextless must beSome
      val bundleContextless = data.bundleContextless.get
      val (totalBundleRecords, tableValueStats, fieldValueStats) = getBundleStats(bundleContextless)
      logger.info(s"Table value stats: ${tableValueStats.mkString("\n")}")

      val electricityTableStats = tableValueStats.find(_._1.name == "kichenElectricity")
      electricityTableStats must beSome
      electricityTableStats.get._2 must be equalTo (6)

      val kitchenTableStats = tableValueStats.find(_._1.name == "kitchen")
      kitchenTableStats must beSome
      kitchenTableStats.get._2 must be equalTo (6)

      totalBundleRecords must be equalTo (6)
    }
  }

  "Data Stats reporting" should {
    val valuesString = hatdex.hat.api.endpoints.jsonExamples.DataDebitExamples.dataDebitContextlessValues
    val data = JsonParser(valuesString).convertTo[ApiDataDebitOut]
    "Correctly convert stats" in {
      data.bundleContextless must beSome
      val bundleContextless = data.bundleContextless.get
      val (totalBundleRecords, tableValueStats, fieldValueStats) = getBundleStats(bundleContextless)

      val stats = convertBundleStats(tableValueStats, fieldValueStats)
      stats.length must beEqualTo(3)
    }

    "Store data in a database" in new Context {
      HatAuthTestHandler.validUsers.find(_.role == "owner") map { user =>
        val ddOperationResult = recordDataDebitOperation(dataDebit, user, DataDebitOperations.Create(), "Test operation")

        eventually {
          ddOperationResult map { result =>
            logger.info(s"ddOperationResult: $result")
          }
          ddOperationResult must be isSuccess
        }
      } must beSome
    }
  }
}