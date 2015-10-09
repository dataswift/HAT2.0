package hatdex.hat

import spray.routing.RequestContext
import spray.routing.Rejection
import scala.concurrent.Future

package object authentication {
  type ParamExtractor = RequestContext => Map[String, String]
  //type Authentication[T] = Either[Rejection, T]
  //type ContextAuthenticator[T] = RequestContext => Future[Authentication[T]]
}