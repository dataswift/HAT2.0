package org.hatdex.hat.api.controllers.v2

import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.resourceManagement.HatServer
import play.api.mvc.{ Action, BodyParser, Result }
import org.hatdex.hat.api.controllers.common._

import scala.concurrent.Future

trait MaybeWithToken {
  def extractToken: Option[String]
}

trait ContractAction {

  def doWithToken[A]( //<: MaybeWithToken](
      parser: Option[BodyParser[A]],
      maybeNamespace: Option[String],
      isWriteAction: Boolean
    )(contractAction: (A, HatUser, HatServer, Option[HatApiAuthEnvironment#A]) => Future[Result]): Action[A]

}
