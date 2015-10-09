package hatdex.hat.api.json

import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json._

/**
 * Created by andrius on 10/10/15.
 */
trait DateTimeMarshalling {
  implicit object DateTimeFormat extends RootJsonFormat[LocalDateTime] {

    val formatter = ISODateTimeFormat.dateTimeNoMillis

    def write(obj: LocalDateTime): JsValue = {
      JsString(formatter.print(obj.toDateTime))
    }

    def read(json: JsValue): LocalDateTime = json match {
      case JsString(s) => try {
        formatter.parseLocalDateTime(s)
      }
      catch {
        case t: Throwable => error(s)
      }
      case _ =>
        error(json.toString())
    }

    def error(v: Any): LocalDateTime = {
      val example = formatter.print(0)
      deserializationError(f"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
    }
  }
}
