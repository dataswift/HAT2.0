package org.hatdex.hat.api.controllers.v2

import org.hatdex.hat.api.controllers.common._
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.resourceManagement.HatServer
import play.api.mvc.{ Action, AnyContent, BodyParser, Result }

import scala.concurrent.Future

trait MaybeWithToken {
  def extractToken: Option[String]
}

trait ContractAction {

  def doWithToken[A](
      parser: BodyParser[A],
      maybeNamespace: Option[String],
      permissions: RequiredNamespacePermissions
    )(contractAction: (A, HatUser, HatServer, Option[HatApiAuthEnvironment#A]) => Future[Result]): Action[A]

  def doWithToken(
      maybeNamespace: Option[String],
      permissions: RequiredNamespacePermissions
    )(contractAction: (HatUser, HatServer, Option[HatApiAuthEnvironment]) => Future[Result]): Action[AnyContent]

}
