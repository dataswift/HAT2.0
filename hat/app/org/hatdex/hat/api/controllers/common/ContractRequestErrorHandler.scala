package org.hatdex.hat.api.controllers.common

import scala.concurrent.Future
import play.api.mvc.Result

trait ContractRequestErrorHandler {
  def handleError(failure: RequestValidationFailure): Future[Result]
}
