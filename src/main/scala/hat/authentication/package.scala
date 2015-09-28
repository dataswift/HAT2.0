package hat

import spray.routing.RequestContext
import spray.routing.Rejection
import scala.concurrent.Future

package authentication {

  case class User(userId: String, email: String, pass: String, address: String, role: String)

  case class Consumer(appKey: String, appSecret: String, description: String)

  case class AccessToken(accessToken: String, userId: String)

}

package object authentication {
  type ParamExtractor = RequestContext => Map[String, String]
  //type Authentication[T] = Either[Rejection, T]
  //type ContextAuthenticator[T] = RequestContext => Future[Authentication[T]]
}