package hatdex.hat.authentication

import java.util.UUID

import hatdex.hat.authentication.models.{AccessToken, User}
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object HatAuthTestHandler extends TestData {

  object AccessTokenHandler {
    val authenticator = authFunction _

    private def authFunction(params: Map[String, String]) = Future {
      //      println ("### Running test access_token authenticator")
      val mayBeToken = params.get("x-auth-token")
      mayBeToken flatMap { token =>
        validAccessTokens.find(_.accessToken equals token).map(_.userId) flatMap { userId =>
          validUsers.find(_.userId equals userId)
        }
      }
    }
  }

  object UserPassHandler {
    val authenticator = authFunction _

    private def authFunction(params: Map[String, String]) = Future {
      val emailOpt = params.get("username")
      val passwordOpt = params.get("password")
      //      println ("### Running test user_pass authenticator")
      val mayBeUser = for {
        email <- emailOpt
        password <- passwordOpt
        user <- validUsers.find(x =>
          // Only HAT Owner allowed to authenticate as a user
          (x.email equals email) && BCrypt.checkpw(password, x.pass.getOrElse("")) && (x.role equals "owner")
        )
      } yield user
      mayBeUser
    }
  }

}

trait TestData {
  // Following are the hard coded values
  val validUsers = Seq(
    User(UUID.fromString("afc5a06f-c351-4e7e-8755-765770c56bb6"), "bob@gmail.com", Some(BCrypt.hashpw("pa55w0rd", BCrypt.gensalt())), "Test User", "owner"),
    User(UUID.fromString("e1e8e1b7-30e2-4cd2-8407-e5321bf0e9ee"), "alice@gmail.com", Some(BCrypt.hashpw("dr0w55ap", BCrypt.gensalt())), "Test Debit User", "dataDebit"),
    User(UUID.fromString("08867a9c-4969-481f-8830-01d73ac45068"), "carol@gmail.com", Some(BCrypt.hashpw("p4ssWOrD", BCrypt.gensalt())), "Test Credit User", "dataCredit"),
    User(UUID.fromString("860774bc-99f9-4238-b661-c3492e95d800"), "platform@platform.com", Some(BCrypt.hashpw("p4ssWOrD", BCrypt.gensalt())), "Platform User", "platform")
  )

  val validAccessTokens = Seq(
    AccessToken("df4545665drgdfg", validUsers.find(_.email equals "bob@gmail.com").get.userId),
    AccessToken("df4545665drgdff", validUsers.find(_.email equals "alice@gmail.com").get.userId),
    AccessToken("df4545665drgdfh", validUsers.find(_.email equals "carol@gmail.com").get.userId),
    AccessToken("df4545665drgdfh", validUsers.find(_.email equals "platform@platform.com").get.userId)
  )
}