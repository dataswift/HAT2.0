package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.endpoints.jsonExamples.BundleExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.authentication.authenticators.{ AccessTokenHandler, UserPassHandler }
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.json._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._

class BundlesSpec extends Specification with Specs2RouteTest with BeforeAfterAll with Bundles {
  def actorRefFactory = system
  val logger: LoggingAdapter = system.log

  val ownerAuthToken = HatAuthTestHandler.validUsers.find(_.role == "owner").map(_.userId).flatMap { ownerId =>
    HatAuthTestHandler.validAccessTokens.find(_.userId == ownerId).map(_.accessToken)
  } getOrElse ("")
  val ownerAuthHeader = RawHeader("X-Auth-Token", ownerAuthToken)

  import JsonProtocol._

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()
  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  // Prepare the data to create test bundles on
  def beforeAll() = {
    val dataTableRows = Seq(
      new DataTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "kitchen", "Fibaro"),
      new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", "Fibaro"),
      new DataTableRow(4, LocalDateTime.now(), LocalDateTime.now(), "event", "Facebook"))

    val dataTableCrossrefs = Seq(
      new DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "contains", 2, 3))

    val dataFieldRows = Seq(
      new DataFieldRow(10, LocalDateTime.now(), LocalDateTime.now(), "timestamp", 3),
      new DataFieldRow(11, LocalDateTime.now(), LocalDateTime.now(), "value", 3),
      new DataFieldRow(12, LocalDateTime.now(), LocalDateTime.now(), "name", 4),
      new DataFieldRow(13, LocalDateTime.now(), LocalDateTime.now(), "location", 4),
      new DataFieldRow(14, LocalDateTime.now(), LocalDateTime.now(), "startTime", 4),
      new DataFieldRow(15, LocalDateTime.now(), LocalDateTime.now(), "endTime", 4))

    val dataRecordRows = Seq(
      new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 1"),
      new DataRecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 2"),
      new DataRecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "kitchen record 3"),
      new DataRecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "event record 1"),
      new DataRecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "event record 2"),
      new DataRecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "event record 3"))

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
      new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), "event endTime 3", 15, 6))

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
      session.close()
    }
  }

  sequential

  "Contextless Bundle Service" should {
    "Create and combine required bundles" in {
      val bundleJson: String = BundleExamples.fullbundle
      val cBundle = HttpRequest(POST, "/bundles/contextless")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, bundleJson)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            val responseString = responseAs[String]
            response.status should be equalTo Created
          }
          responseAs[ApiBundleContextless]
        }

      logger.info(s"Looking up bundle id ${cBundle.id}")

      HttpRequest(GET, s"/bundles/contextless/${cBundle.id.get}")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            val responseString = responseAs[String]
//            logger.info(s"Bundle data response ${responseString}")
            response.status should be equalTo OK
          }
        }

      HttpRequest(GET, s"/bundles/contextless/${cBundle.id.get}/values")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            val responseString = responseAs[String]

//            logger.info(s"Bundle data response ${responseString}")

            response.status should be equalTo OK

            responseString must contain("dataGroups")
            responseString must contain("event record 1")
            responseString must contain("event record 2")
            responseString must contain("event record 3")

            responseString must contain("kitchen record 1")
            responseString must contain("kitchen record 2")
            responseString must contain("kitchen record 3")

            responseString must contain("kitchen value 1")
            responseString must contain("event name 1")
            responseString must contain("event location 1")
          }
        }
    }

    "Correctly exclude fields not part of a bundle from results" in {
      val bundleJson: String = BundleExamples.fieldSelectionsBundle
      val cBundle = HttpRequest(POST, "/bundles/contextless")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, bundleJson)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            val responseString = responseAs[String]
            response.status should be equalTo Created
          }
          responseAs[ApiBundleContextless]
        }

      HttpRequest(GET, s"/bundles/contextless/${cBundle.id.get}/values")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            val responseString = responseAs[String]

            response.status should be equalTo OK

            responseString must contain("dataGroups")
            responseString must contain("event record 1")
            responseString must contain("event record 2")
            responseString must contain("event record 3")

            responseString must contain("kitchen record 1")
            responseString must contain("kitchen record 2")
            responseString must contain("kitchen record 3")

            responseString must not contain("kitchen time 1")
            responseString must contain("kitchen value 1")
            responseString must contain("event name 1")
            responseString must not contain("event location 1")
            responseString must contain("event name 2")
            responseString must not contain("event location 2")
            responseString must not contain("event startTime 2")
            responseString must not contain("event endTime 2")
          }
        }
    }

    "Do not allow source datasets with no tables/fields" in {
      val bundleJson: String = BundleExamples.fieldlessDataset
      HttpRequest(POST, "/bundles/contextless")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, bundleJson)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo BadRequest
          }
        }
    }

    "Return correct error code for bundle that doesn't exist" in {
      HttpRequest(GET, "/bundles/contextless/0")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo NotFound
        }
    }

  }
}

