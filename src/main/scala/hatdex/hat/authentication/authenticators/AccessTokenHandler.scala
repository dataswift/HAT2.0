package hatdex.hat.authentication.authenticators

import hatdex.hat.authentication.models.User

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import hatdex.hat.authentication._
import spray.routing.HttpService._
import spray.routing._

object AccessTokenHandler {

  case class AccessTokenAuthenticator(val keys: List[String] = defaultKeys,
                                      val authenticator: Map[String, String] => Future[Option[User]] = defaultAuthenticator) extends RestAuthenticator[User] {
    def apply(): Directive1[User] = authenticate(this)
  }

  val defaultKeys = List("access_token")
  val defaultAuthenticator = (params: Map[String, String]) => Future {
    val mayBeUser = params.get(defaultKeys(0))
    mayBeUser flatMap { token =>
      /*
       * get user form database , replace None with proper method once database service is ready.
       * getUserFromAccessToken(token)
       */
      None
    }
  }

}