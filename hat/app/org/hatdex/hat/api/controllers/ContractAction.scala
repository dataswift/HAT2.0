package org.hatdex.hat.api.controllers

import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.resourceManagement.HatServer
import play.api.mvc.{ Action, BodyParser, Result }

import scala.concurrent.Future

trait ContractAction {

  def doWithContract[A <: MaybeWithContractInfo](
      parser: BodyParser[A],
      maybeNamespace: Option[String],
      isWriteAction: Boolean
    )(contractAction: (A, HatUser, HatServer, Option[HatApiAuthEnvironment#A]) => Future[Result]): Action[A]

}
