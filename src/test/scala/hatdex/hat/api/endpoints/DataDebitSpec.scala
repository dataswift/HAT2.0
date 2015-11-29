package hatdex.hat.api.endpoints

import java.util.UUID

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.endpoints.jsonExamples.{BundleExamples, DataDebitExamples}
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
  }

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      TestDataCleanup.cleanupAll
      session.close()
    }
  }

  object Context {
    val propertySpec = new PropertySpec()
    val property = propertySpec.createWeightProperty
    val dataSpec = new DataSpec()
    dataSpec.createBasicTables
    val populatedData = dataSpec.populateDataReusable

    val personSpec = new PersonSpec()

    val newPerson = personSpec.createNewPerson
    newPerson.id must beSome

    val dataTable = populatedData match {
      case (dataTable, dataField, record) =>
        dataTable
    }
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
    val populatedTable = Context.dataTable
  }

  sequential

  "Data Debit Service" should {
    "Accept a contextless Data Debit proposal" in new Context {

      val contextlessBundle =
        s"""
          |{
          |    "name": "Kitchen electricity",
          |    "tables": [{
          |        "name": "Electricity in the kitchen",
          |        "bundleTable": {
          |            "name": "Electricity in the kitchen",
          |            "table": {
          |                "name": "kitchen",
          |                "source": "Fibaro",
          |                "id": ${populatedTable.id.get}
          |            }
          |        }
          |    }]
          |}
        """.stripMargin

      val bundleData = JsonParser(contextlessBundle).convertTo[ApiBundleContextless]
      val dataDebitData = JsonParser(DataDebitExamples.dataDebitExample).convertTo[ApiDataDebit]

      val dataDebit = {
        val dataDebit = HttpRequest(POST,
          "/dataDebit/propose" + appendParams(parameters),
          entity = HttpEntity(MediaTypes.`application/json`, dataDebitData.copy(bundleContextless = Some(bundleData)).toJson.toString)
        ) ~>
          sealRoute(routes) ~>
          check {
            logger.debug("Creating contextless DD response: " + response)
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

      val result = {
        HttpRequest(GET,
          s"/dataDebit/${dataDebit.key.get}/values" + appendParams(parameters)
        ) ~>
          sealRoute(routes) ~>
          check {
            logger.debug("Return data debit values response: " + response)
            response.status should be equalTo OK
            responseAs[ApiDataDebitOut]
          }
      }
      result.bundleContextless must beSome
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

