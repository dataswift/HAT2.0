package hatdex.hat.authentication.authenticators

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import hatdex.hat.authentication._
import spray.routing.HttpService._
import spray.routing._

object UserPassHandler {

  case class UserPassAuthenticator(val keys: List[String] = defaultKeys,
                                   val authenticator: Map[String, String] => Future[Option[User]] = defaultAuthenticator)
    extends RestAuthenticator[User] {

    def apply(): Directive1[User] = authenticate(this)
  }

  val defaultKeys = List("username", "password")
  val defaultAuthenticator = authFunction _

  def authFunction(params: Map[String, String]): Future[Option[User]] = Future {
    val emailOpt = params.get(defaultKeys(0))
    val passwordOpt = params.get(defaultKeys(1))

    val mayBeUser = for {
      email <- emailOpt
      password <- passwordOpt
      user <- {
        //get user form database , replace None with proper method once database service is ready.
        //getUserByCredential(email, password)
        None
      }
    } yield user
    mayBeUser
  }
}