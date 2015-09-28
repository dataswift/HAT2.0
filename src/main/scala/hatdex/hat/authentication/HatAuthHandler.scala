package hatdex.hat.authentication

import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Here we need to moke authenticate function of AuthHandlers
 */

object HatAuthHandler extends TestData {

  object AccessTokenHandler {
    val authenticator = authFunction _
    private def authFunction(params: Map[String, String]) = Future {
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

      val mayBeUser = for {
        email <- emailOpt
        password <- passwordOpt
        user <- validUsers.find(x =>
          // Only HAT Owner allowed to authenticate as a user
          (x.email equals email) && BCrypt.checkpw(password, x.pass) && (x.role equals "owner")
        )
      } yield user
      mayBeUser
    }
  }
}

trait TestData {
  // Following are the hard coded values
  val validUsers = Seq(
    User("u-100", "bob@gmail.com", BCrypt.hashpw("pa55w0rd", BCrypt.gensalt()), "Any Address....", "owner"),
    User("u-101", "alice@gmail.com", BCrypt.hashpw("dr0w55ap", BCrypt.gensalt()), "Any Address....", "dataDebit"),
    User("u-102", "carol@gmail.com", BCrypt.hashpw("p4ssWOrD", BCrypt.gensalt()), "Any Address....", "dataCredit")
  )

  val validAccessTokens = Seq(
    AccessToken("df4545665drgdfg", "u-100"),
    AccessToken("df4545665drgdff", "u-101"),
    AccessToken("df4545665drgdfh", "u-102")
  )
}