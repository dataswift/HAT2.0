package org.hatdex.hat.api.controllers.common

import scala.concurrent.Future
import play.api.mvc.Result
import play.api.mvc.Results.BadRequest

class ContractRequestErrorHandlerImpl extends ContractRequestErrorHandler {

  def handleError(failure: RequestValidationFailure): Future[Result] =
    failure match {
      case HatNotFound(hatName)               => Future.successful(BadRequest(s"HatName not found: $hatName"))
      case MissingHatName(hatName)            => Future.successful(BadRequest(s"Missing HatName: $hatName"))
      case InaccessibleNamespace(namespace)   => Future.successful(BadRequest(s"Namespace Inaccessible: $namespace"))
      case InvalidShortLivedToken(contractId) => Future.successful(BadRequest(s"Invalid Token: $contractId"))
      case GeneralError                       => Future.successful(BadRequest("Unknown Error"))
    }
}
