package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.endpoints.jsonExamples.TypeExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.{ApiSystemType, ApiSystemUnitofmeasurement, ErrorMessage}
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{HttpEntity, HttpRequest, MediaTypes}
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._

class TypeSpec extends Specification with Specs2RouteTest with Type with BeforeAfterAll {
  def actorRefFactory = system

  val logger: LoggingAdapter = system.log

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  def beforeAll() = {

  }

  import JsonProtocol._

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      TestDataCleanup.cleanupAll
      session.close()
    }
  }

  def createPostalAddressType = HttpRequest(POST, "/type/type" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.postalAddress)) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        logger.debug("Type create response: " + response.toString)
        response.status should be equalTo Created
        responseAs[String] must contain("PostalAddress")
        responseAs[ApiSystemType].id must beSome
      }
      responseAs[ApiSystemType]
    }

  def createDateType = HttpRequest(POST, "/type/type" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.date)) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("Date")
        responseAs[ApiSystemType].id must beSome
      }
      responseAs[ApiSystemType]
    }

  def createPlaceType = HttpRequest(POST, "/type/type" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.place)) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("Place")
        responseAs[ApiSystemType].id must beSome
      }
      responseAs[ApiSystemType]
    }

  def createQuantitativeValueType = HttpRequest(POST, "/type/type" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.quantitativeValue)) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("QuantitativeValue")
        responseAs[ApiSystemType].id must beSome
      }
      responseAs[ApiSystemType]
    }

  def createMetersUom = HttpRequest(POST, "/type/unitofmeasurement" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.uomMeters)) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        logger.debug("UOM create response: " + response.toString)
        response.status should be equalTo Created
        responseAs[String] must contain("meters")
        responseAs[ApiSystemUnitofmeasurement].id must beSome
      }
      responseAs[ApiSystemUnitofmeasurement]
    }

  def createWeightUom = HttpRequest(POST, "/type/unitofmeasurement" + ownerAuthParams,
    entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.uomWeight)) ~>
    sealRoute(routes) ~>
    check {
      eventually {
        response.status should be equalTo Created
        responseAs[String] must contain("kilograms")
        responseAs[ApiSystemUnitofmeasurement].id must beSome
      }
      responseAs[ApiSystemUnitofmeasurement]
    }

  sequential

  val ownerAuthParams = "?username=bob@gmail.com&password=pa55w0rd"

  "Types Service" should {
    "Accept new types created" in {
      val postalAddressType = createPostalAddressType
      val dateType = createDateType
      val placeType = createPlaceType

      HttpRequest(POST, s"/type/${placeType.id.get}/type/${postalAddressType.id.get}" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.addressOfPlace)) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo Created
          }
        }
    }

    "Allow type lookup" in {
      HttpRequest(
        GET, "/type/type" + ownerAuthParams + "&name=PostalAddress") ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo OK
          val types = responseAs[List[ApiSystemType]]
          types must have size (1)
          responseAs[String] must contain("PostalAddress")
        }

      HttpRequest(
        GET, "/type/type" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo OK
          val types = responseAs[List[ApiSystemType]]
          types must not be empty
          responseAs[String] must contain("PostalAddress")
          responseAs[String] must contain("Date")
          responseAs[String] must contain("Place")
        }
    }

    "Disallow duplicte types" in {
      HttpRequest(POST, "/type/type" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.postalAddress)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[String] must contain("PostalAddress")
          responseAs[ErrorMessage].message must contain("Error")
        }
    }

    "Rejecet bad linking of types" in {
      HttpRequest(POST, s"/type/1/type/0" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.addressOfPlace)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].message must contain("Error linking Types")
        }
    }

    "Accept new Units of Measurement" in {
      val uom = createMetersUom
      uom.id must beSome
      val kilograms = createWeightUom
      kilograms.id must beSome
    }

    "Reject duplicate Units of Measurement" in {
      HttpRequest(POST, "/type/unitofmeasurement" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, TypeExamples.uomMeters)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].message must contain("Error")
        }
    }

    "Allow Unit of Measurement lookup" in {
      HttpRequest(
        GET, "/type/unitofmeasurement" + ownerAuthParams + "&name=meters") ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo OK
          val uoms = responseAs[List[ApiSystemUnitofmeasurement]]
          uoms must not be empty
          responseAs[String] must contain("meters")
        }

      HttpRequest(
        GET, "/type/unitofmeasurement" + ownerAuthParams) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo OK
          val uoms = responseAs[List[ApiSystemUnitofmeasurement]]
          uoms must not be empty
          responseAs[String] must contain("meters")
          responseAs[String] must contain("kilograms")
        }

      HttpRequest(
        GET, "/type/unitofmeasurement" + ownerAuthParams + "&name=notExistingName") ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo OK
          val uoms = responseAs[List[ApiSystemUnitofmeasurement]]
          uoms must be empty
        }
    }

  }
}
