package hatdex.hat.api.endpoints

import java.util.UUID

import akka.event.LoggingAdapter
import hatdex.hat.api.endpoints.jsonExamples.UserExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.authentication.models.{AccessToken, User}
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import org.mindrot.jbcrypt.BCrypt
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._

class UsersSpec extends Specification with Specs2RouteTest with BeforeAfterAll with Users {
  def actorRefFactory = system
  val logger: LoggingAdapter = system.log
  import JsonProtocol._

  def beforeAll() = {
    val validUsers = Seq(
      UserUserRow(UUID.fromString("6096eba1-0e9e-4607-9dfd-072bfa106bf4"),
        LocalDateTime.now(), LocalDateTime.now(),
        "bob@example.com", Some(BCrypt.hashpw("pa55w0rd", BCrypt.gensalt())),
        "Test User", "owner", enabled=true),
      UserUserRow(UUID.fromString("17380a13-16c3-49f7-968b-30df0eefbe0f"),
        LocalDateTime.now(), LocalDateTime.now(),
        "alice@example.com", Some(BCrypt.hashpw("dr0w55ap", BCrypt.gensalt())),
        "Test Debit User", "dataDebit", enabled=true),
      UserUserRow(UUID.fromString("dd15948d-18ef-4062-a3c0-33f21b3cffd1"),
        LocalDateTime.now(), LocalDateTime.now(),
        "carol@example.com", Some(BCrypt.hashpw("p4ssWOrD", BCrypt.gensalt())),
        "Test Credit User", "dataCredit", enabled=false),
      UserUserRow(UUID.fromString("bae66f37-8421-4932-9755-ed7ffa865e0f"),
        LocalDateTime.now(), LocalDateTime.now(),
        "platform@platform.com", Some(BCrypt.hashpw("p4ssWOrD", BCrypt.gensalt())),
        "Platform User", "platform", enabled=true)
    )

    val validAccessTokens = Seq(
      UserAccessTokenRow("151aa44a-bc31-419c-aa9d-0c61b3fbc4e4", UUID.fromString("6096eba1-0e9e-4607-9dfd-072bfa106bf4")),
      UserAccessTokenRow("177991a1-6792-4889-a397-6185675dbe67", UUID.fromString("17380a13-16c3-49f7-968b-30df0eefbe0f")),
      UserAccessTokenRow("856fb539-9908-4c17-bd36-518d701fa267", UUID.fromString("dd15948d-18ef-4062-a3c0-33f21b3cffd1")),
      UserAccessTokenRow("34b7299d-16a0-4884-ad3f-7999d2cd8d3c", UUID.fromString("bae66f37-8421-4932-9755-ed7ffa865e0f"))
    )

    db.withSession { implicit session =>
      UserUser.forceInsertAll(validUsers: _*)
      UserAccessToken.forceInsertAll(validAccessTokens: _*)
    }
  }

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      val userIds = Seq(
        UUID.fromString("17380a13-16c3-49f7-968b-30df0eefbe0f"),
        UUID.fromString("dd15948d-18ef-4062-a3c0-33f21b3cffd1"),
        UUID.fromString("6096eba1-0e9e-4607-9dfd-072bfa106bf4"),
        UUID.fromString("bae66f37-8421-4932-9755-ed7ffa865e0f")
      )
      UserAccessToken.filter(_.userId inSet userIds).delete
      UserUser.filter(_.userId inSet userIds).delete
    }
//    db.close
  }

  sequential

  "User Service" should {
    "Let Platform user create new users" in {
      val platformCredentials = "?access_token=34b7299d-16a0-4884-ad3f-7999d2cd8d3c"
      val user = HttpRequest(POST, "/user" + platformCredentials, entity = HttpEntity(MediaTypes.`application/json`, UserExamples.userExample)) ~>
        sealRoute(createApiUserAccount) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("apiclient")
        responseAs[User]
      }
      //Cleanup right away
      db.withSession { implicit session =>
        UserAccessToken.filter(_.userId === user.userId).delete
        UserUser.filter(_.userId === user.userId).delete
      }
      user.role must be equalTo "dataDebit"
    }

    "Forbid Platform user from creating new owner" in {
      val platformCredentials = "?access_token=34b7299d-16a0-4884-ad3f-7999d2cd8d3c"
      HttpRequest(POST, "/user" + platformCredentials, entity = HttpEntity(MediaTypes.`application/json`, UserExamples.ownerUserExample)) ~>
        sealRoute(createApiUserAccount) ~> check {
        response.status should be equalTo Unauthorized
      }
    }

    "Forbid unprivileged user from creating new owner" in {
      val dataDebitCredentials = "?access_token=dd15948d-18ef-4062-a3c0-33f21b3cffd1"
      HttpRequest(POST, "/user" + dataDebitCredentials, entity = HttpEntity(MediaTypes.`application/json`, UserExamples.userExample)) ~>
        sealRoute(createApiUserAccount) ~> check {
        response.status should be equalTo Unauthorized
      }
    }

    "Provide access tokens for existing users" in {
      HttpRequest(GET, "/access_token?username=bob@example.com&password=pa55w0rd") ~> sealRoute(getAccessToken) ~> check {
        response.status should be equalTo OK
        responseAs[AccessToken].accessToken must be equalTo "151aa44a-bc31-419c-aa9d-0c61b3fbc4e4"
      }
    }

    "Reject disabled user's authentication" in {
      HttpRequest(GET, "/access_token?username=carol@example.com&password=p4ssWOrD") ~> sealRoute(getAccessToken) ~> check {
        response.status should be equalTo Unauthorized
      }
    }

    "Allow platform user to enable users" in {
      HttpRequest(PUT, "/user/dd15948d-18ef-4062-a3c0-33f21b3cffd1/enable?access_token=34b7299d-16a0-4884-ad3f-7999d2cd8d3c") ~>
        sealRoute(enableUserAccount) ~> check {
        response.status should be equalTo OK
      }

      HttpRequest(GET, "/access_token?username=carol@example.com&password=p4ssWOrD") ~> sealRoute(getAccessToken) ~> check {
        response.status should be equalTo OK
      }
    }

    "Let newly created user retrieve their access token" in {
      val platformCredentials = "?access_token=34b7299d-16a0-4884-ad3f-7999d2cd8d3c"
      val user = HttpRequest(POST, "/user" + platformCredentials, entity = HttpEntity(MediaTypes.`application/json`, UserExamples.userExample)) ~>
        sealRoute(createApiUserAccount) ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("apiclient")
        responseAs[User]
      }

      HttpRequest(GET, "/access_token?username=apiClient@platform.com&password=simplepass") ~> sealRoute(getAccessToken) ~> check {
        response.status should be equalTo OK
        responseAs[String] must contain("accessToken")
      }

      //Cleanup right away
      db.withSession { implicit session =>
        UserAccessToken.filter(_.userId === user.userId).delete
        UserUser.filter(_.userId === user.userId).delete
      }

      user.role must be equalTo "dataDebit"
    }
  }
}

