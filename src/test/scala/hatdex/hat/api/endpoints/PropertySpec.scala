package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.authentication.HatAuthTestHandler
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.ApiProperty
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.{HttpEntity, HttpRequest, MediaTypes}
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.testkit.Specs2RouteTest

class PropertySpec extends Specification with Specs2RouteTest with Property with BeforeAfterAll {
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

  sequential

  val ownerAuthParams = "?username=bob@gmail.com&password=pa55w0rd"

  def createWeightProperty = {
    val typeSpec = new TypeSpec
    val quantitativeType = typeSpec.createQuantitativeValueType
    val weightUom = typeSpec.createWeightUom

    val weightProperty = ApiProperty(None, None, None, "BodyWeight",
      Some("Person body weight"), quantitativeType, weightUom)

    HttpRequest(POST, "" + ownerAuthParams,
      entity = HttpEntity(MediaTypes.`application/json`, weightProperty.toJson.toString)) ~>
      sealRoute(createProperty) ~>
      check {
        response.status should be equalTo Created
        responseAs[String] must contain("BodyWeight")
        responseAs[String] must contain("QuantitativeValue")
        responseAs[String] must contain("kilograms")
        responseAs[ApiProperty]
      }
  }

  sequential

  "Property Service" should {
    "Accept new properties created" in {
      val weightProperty = createWeightProperty
      weightProperty.id must beSome

      HttpRequest(GET, s"/${weightProperty.id.get}" + ownerAuthParams) ~>
        sealRoute(getPropertyApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val asString = responseAs[String]
            asString must contain(s"${weightProperty.id.get}")
            asString must contain(s"${weightProperty.name}")
          }
        }
    }

    "List properties" in {
      HttpRequest(GET, s"" + ownerAuthParams) ~>
        sealRoute(getPropertiesApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val asList = responseAs[List[ApiProperty]]
            asList must not be empty
            val asString = responseAs[String]
            asString must contain("BodyWeight")
          }
        }

      HttpRequest(GET, s"" + ownerAuthParams + "&name=BodyWeight") ~>
        sealRoute(getPropertiesApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val asList = responseAs[List[ApiProperty]]
            asList must not be empty
            val asString = responseAs[String]
            asString must contain("BodyWeight")
          }
        }

      HttpRequest(GET, s"" + ownerAuthParams + "&name=RandomProperty") ~>
        sealRoute(getPropertiesApi) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val asList = responseAs[List[ApiProperty]]
            asList must have size (0)
          }
        }
    }
  }
}
