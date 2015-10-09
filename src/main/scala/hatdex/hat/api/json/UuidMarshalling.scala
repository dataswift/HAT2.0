package hatdex.hat.api.json

import java.util.UUID

import spray.json._

/**
 * Created by andrius on 10/10/15.
 */
trait UuidMarshalling {
  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(x: UUID) = JsString(x toString ())
    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }
}
