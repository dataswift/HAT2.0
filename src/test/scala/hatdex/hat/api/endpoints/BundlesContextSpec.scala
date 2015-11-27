package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.api.endpoints.jsonExamples.{BundleContextExamples, BundleExamples}
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.json._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._
import scala.util.{Failure, Success}

class BundlesContextSpec extends Specification with Specs2RouteTest with BeforeAfterAll with BundlesContext {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  val ownerAuth = "username=bob@gmail.com&password=pa55w0rd"

  val parameters = Map("username" -> "bob@gmail.com", "password" -> "pa55w0rd")

  def appendParams(parameters: Map[String, String]): String = {
    parameters.foldLeft[String]("?") { case (params, (key, value)) =>
      s"$params$key=$value&"
    }
  }

  import JsonProtocol._

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  def peopleService = new Person {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  def thingsService = new Thing {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  def organisationsService = new Organisation {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  def locationsService = new Location {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  def eventsService = new Event {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  // Prepare the data to create test bundles on
  def beforeAll() = {

  }

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      TestDataCleanup.cleanupAll
    }
  }

  sequential

  "Contextual Bundle Service for Tables" should {
    "Accept valid empty bundles" in {
      val bundle = HttpRequest(POST,
        "/bundles/context" + appendParams(parameters),
        entity = HttpEntity(MediaTypes.`application/json`, BundleContextExamples.emptyBundle)
      ) ~>
        routes ~>
        check {
          response.status should be equalTo Created
          val resp = responseAs[String]
          resp must contain ("emptyBundleTest1")
          resp must contain ("emptyBundleTest2")
          resp must contain ("emptyBundleTest3")
          responseAs[ApiBundleContext]
        }

      bundle.id must beSome

      HttpRequest(GET,
        s"/bundles/context/${bundle.id.get}" + appendParams(parameters)
      ) ~>
        routes ~>
        check {
          response.status should be equalTo OK
          val resp = responseAs[String]
          resp must contain ("emptyBundleTest1")
          resp must contain ("emptyBundleTest2")
          resp must contain ("emptyBundleTest3")
          responseAs[ApiBundleContext].id must beSome
        }

    }

    "Accept bundles with entity selectors" in {
      val bundle = HttpRequest(POST,
        "/bundles/context" + appendParams(parameters),
        entity = HttpEntity(MediaTypes.`application/json`, BundleContextExamples.entityBundleSunrise)
      ) ~>
        routes ~>
        check {
          response.status should be equalTo Created
          val resp = responseAs[String]
          resp must contain ("emptyBundleTest2-1")
          resp must contain ("sunrise")
          responseAs[ApiBundleContext]
        }

      bundle.id must beSome

      HttpRequest(GET,
        s"/bundles/context/${bundle.id.get}" + appendParams(parameters)
      ) ~>
        routes ~>
        check {
          response.status should be equalTo OK
          val resp = responseAs[String]
          resp must contain ("emptyBundleTest2-1")
          resp must contain ("sunrise")
          responseAs[ApiBundleContext].id must beSome
        }

      val bundle2 = HttpRequest(POST,
        "/bundles/context" + appendParams(parameters),
        entity = HttpEntity(MediaTypes.`application/json`, BundleContextExamples.entityBundleKind)
      ) ~>
        routes ~>
        check {
          response.status should be equalTo Created
          val resp = responseAs[String]
          resp must contain ("emptyBundleTest3-1")
          resp must contain ("person")
          responseAs[ApiBundleContext]
        }

      bundle2.id must beSome

      HttpRequest(GET,
        s"/bundles/context/${bundle2.id.get}" + appendParams(parameters)
      ) ~>
        routes ~>
        check {
          response.status should be equalTo OK
          val resp = responseAs[String]
          resp must contain ("emptyBundleTest3-1")
          resp must contain ("person")
          responseAs[ApiBundleContext].id must beSome
        }

      val bundle3 = HttpRequest(POST,
        "/bundles/context" + appendParams(parameters),
        entity = HttpEntity(MediaTypes.`application/json`, BundleContextExamples.entitiesBundleKindName)
      ) ~>
        routes ~>
        check {
          response.status should be equalTo Created
          val resp = responseAs[String]
          resp must contain ("emptyBundleTest4-1")
          resp must contain ("person")
          resp must contain ("sunrise")
          responseAs[ApiBundleContext]
        }

      bundle3.id must beSome

      HttpRequest(GET,
        s"/bundles/context/${bundle3.id.get}" + appendParams(parameters)
      ) ~>
        routes ~>
        check {
          response.status should be equalTo OK
          val resp = responseAs[String]
          resp must contain ("emptyBundleTest4-1")
          resp must contain ("person")
          resp must contain ("sunrise")
          responseAs[ApiBundleContext].id must beSome
        }
    }

    "Accept bundles with entity and property selectors" in {
      val bundle = HttpRequest(POST,
        "/bundles/context" + appendParams(parameters),
        entity = HttpEntity(MediaTypes.`application/json`, BundleContextExamples.entityBundleProperties)
      ) ~>
        routes ~>
        check {
          response.status should be equalTo Created
          val resp = responseAs[String]
          resp must contain ("emptyBundleTest5-1")
          resp must contain ("person")
          resp must contain ("dynamic")
          resp must contain ("BodyWeight")
          resp must contain ("QuantitativeValue")
          resp must contain ("kilograms")
          responseAs[ApiBundleContext]
        }

      bundle.id must beSome

      HttpRequest(GET,
        s"/bundles/context/${bundle.id.get}" + appendParams(parameters)
      ) ~>
        routes ~>
        check {
          response.status should be equalTo OK
          val resp = responseAs[String]
          resp must contain ("emptyBundleTest5-1")
          resp must contain ("person")
          resp must contain ("dynamic")
          resp must contain ("BodyWeight")
          resp must contain ("QuantitativeValue")
          resp must contain ("kilograms")
          responseAs[ApiBundleContext].id must beSome
        }
    }
  }
}


