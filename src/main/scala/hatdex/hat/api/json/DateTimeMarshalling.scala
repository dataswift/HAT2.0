/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package hatdex.hat.api.json

import org.joda.time.LocalDateTime
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormatterBuilder, DateTimeParser}
import spray.json._

/**
 * Created by andrius on 10/10/15.
 */
trait DateTimeMarshalling {
  implicit object DateTimeFormat extends RootJsonFormat[LocalDateTime] {

    ISODateTimeFormat.dateTime()
//    val formatter = new DateTimeFormatterBuilder()
//      .appendOptional(ISODateTimeFormat.dateTime.getParser)
//      .appendOptional(ISODateTimeFormat.localDateOptionalTimeParser().getParser)
//      .appendOptional(ISODateTimeFormat.dateTimeNoMillis().getParser)
//      .toFormatter

    import collection.JavaConverters._
    val parsers = Array(
      ISODateTimeFormat.dateTime.getParser,
      ISODateTimeFormat.localDateOptionalTimeParser().getParser,
      ISODateTimeFormat.dateTimeNoMillis().getParser
    )

    val formatter = new DateTimeFormatterBuilder().append( ISODateTimeFormat.dateTimeNoMillis().getPrinter, parsers).toFormatter();

//    val printFormatter = ISODateTimeFormat.dateTimeNoMillis()

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
