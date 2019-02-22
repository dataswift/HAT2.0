package org.hatdex.hat.api.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Json, Reads, Writes }

case class LogRequest(actionCode: String, message: Option[String], logGroup: Option[String])

object LogRequest {
  implicit val logRequestReads: Reads[LogRequest] = (
    (JsPath \ "actionCode").read[String] and
    (JsPath \ "message").readNullable[String] and
    (JsPath \ "logGroup").readNullable[String])(LogRequest.apply _)

  implicit val logRequestWrites: Writes[LogRequest] = Json.format[LogRequest]

}