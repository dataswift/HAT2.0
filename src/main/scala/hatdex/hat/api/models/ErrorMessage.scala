package hatdex.hat.api.models

import spray.http.StatusCode

case class ErrorMessage(message: String, cause: String)

object ErrorMessage {
}

case class ApiError(statusCode: StatusCode, message: ErrorMessage) extends RuntimeException
