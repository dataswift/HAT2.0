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
      val mayBeToken = params.get("access_token")
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
    User(UUID.randomUUID(), "bob@gmail.com", Some(BCrypt.hashpw("pa55w0rd", BCrypt.gensalt())), "Test User", "owner"),
    User(UUID.randomUUID(), "alice@gmail.com", Some(BCrypt.hashpw("dr0w55ap", BCrypt.gensalt())), "Test Debit User", "dataDebit"),
    User(UUID.randomUUID(), "carol@gmail.com", Some(BCrypt.hashpw("p4ssWOrD", BCrypt.gensalt())), "Test Credit User", "dataCredit"),
    User(UUID.randomUUID(), "platform@platform.com", Some(BCrypt.hashpw("p4ssWOrD", BCrypt.gensalt())), "Platform User", "platform")
  )

  val validAccessTokens = Seq(
    AccessToken("df4545665drgdfg", validUsers.find(_.email equals "bob@gmail.com").get.userId),
    AccessToken("df4545665drgdff", validUsers.find(_.email equals "alice@gmail.com").get.userId),
    AccessToken("df4545665drgdfh", validUsers.find(_.email equals "carol@gmail.com").get.userId),
    AccessToken("df4545665drgdfh", validUsers.find(_.email equals "platform@platform.com").get.userId)
  )
}