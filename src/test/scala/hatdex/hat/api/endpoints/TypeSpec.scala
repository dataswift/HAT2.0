package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.authentication.HatAuthTestHandler
import hatdex.hat.api.endpoints.jsonExamples.TypeExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.{ApiSystemType, ApiSystemUnitofmeasurement, ErrorMessage}
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{HttpEntity, HttpRequest, MediaTypes}
import spray.httpx.SprayJsonSupport._
import spray.testkit.Specs2RouteTest

class TypeSpec extends Specification with Specs2RouteTest with Type with BeforeAfterAll {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  import JsonProtocol._

  def beforeAll() = {

  }

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      TestDataCleanup.cleanupAll
      session.close()
    }
  }

  def createPostalAddressType = HttpRequest(POST, "/type" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.postalAddress)) ~>
    sealRoute(createType) ~>
    check {
      logger.debug("Create Type response: " + response.toString)
      response.status should be equalTo Created
      responseAs[String] must contain("PostalAddress")
      responseAs[ApiSystemType].id must beSome
      responseAs[ApiSystemType]
    }

  def createDateType = HttpRequest(POST, "/type" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.date)) ~>
    sealRoute(createType) ~>
    check {
      response.status should be equalTo Created
      responseAs[String] must contain("Date")
      responseAs[ApiSystemType].id must beSome
      responseAs[ApiSystemType]
    }

  def createPlaceType = HttpRequest(POST, "/type" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.place)) ~>
    sealRoute(createType) ~>
    check {
      response.status should be equalTo Created
      responseAs[String] must contain("Place")
      responseAs[ApiSystemType].id must beSome
      responseAs[ApiSystemType]
    }

  def createQuantitativeValueType = HttpRequest(POST, "/type" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.quantitativeValue)) ~>
    sealRoute(createType) ~>
    check {
      logger.debug("Create quantitative value type response: " + response.toString)
      response.status should be equalTo Created
      responseAs[String] must contain("QuantitativeValue")
      responseAs[ApiSystemType].id must beSome
      responseAs[ApiSystemType]
    }

  def createMetersUom = HttpRequest(POST, "/unitofmeasurement" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.uomMeters)) ~>
    sealRoute(createUnitOfMeasurement) ~>
    check {
      response.status should be equalTo Created
      responseAs[String] must contain("meters")
      responseAs[ApiSystemUnitofmeasurement].id must beSome
      responseAs[ApiSystemUnitofmeasurement]
    }

  def createWeightUom = HttpRequest(POST, "/unitofmeasurement" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.uomWeight)) ~>
    sealRoute(createUnitOfMeasurement) ~>
    check {
      response.status should be equalTo Created
      responseAs[String] must contain("kilograms")
      responseAs[ApiSystemUnitofmeasurement].id must beSome
      responseAs[ApiSystemUnitofmeasurement]
    }

  sequential

  val ownerAuthParams = "?username=bob@gmail.com&password=pa55w0rd"

  "Types Service" should {
    "Accept new types created" in {
      val postalAddressType = createPostalAddressType
      val dateType = createDateType
      val placeType = createPlaceType

      HttpRequest(POST, s"/${placeType.id.get}/type/${postalAddressType.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.addressOfPlace)) ~>
        sealRoute(linkTypeToType) ~>
        check {
          response.status should be equalTo Created
        }
    }

    "Disallow duplicte types" in {
      HttpRequest(POST, "/type" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.postalAddress)) ~>
        sealRoute(createType) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[String] must contain("PostalAddress")
          responseAs[ErrorMessage].message must contain("Error")
        }
    }

    "Rejecet bad linking of types" in {
      HttpRequest(POST, s"/1/type/0" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.addressOfPlace)) ~>
        sealRoute(linkTypeToType) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].message must contain("Error linking Types")
        }
    }

    "Accept new Units of Measurement" in {
      val uom = createMetersUom
      uom.id must beSome
    }

    "Reject duplicate Units of Measurement" in {
      HttpRequest(POST, "/unitofmeasurement" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.uomMeters)) ~>
        sealRoute(createUnitOfMeasurement) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].message must contain("Error")
        }
    }
  }
}
