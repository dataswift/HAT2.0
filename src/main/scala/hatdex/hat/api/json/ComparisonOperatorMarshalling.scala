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

import hatdex.hat.api.models.ComparisonOperators
import spray.json._

/**
 * Created by andrius on 10/10/15.
 */
trait ComparisonOperatorMarshalling {
  // The scala way of doing "Enums"
  import ComparisonOperators._

  // serialising/deserialising between json and sealed case class
  implicit object ComparisonOperatorsFormat extends JsonFormat[ComparisonOperator] {
    def write(obj: ComparisonOperator) = JsString(obj.toString)

    def read(json: JsValue): ComparisonOperator = {
      json match {
        case JsString(txt) =>
          try {
            ComparisonOperators.fromString(txt)
          } catch {
            case t: Throwable => error(txt)
          }
        case _ =>
          error(json)
      }
    }

    def error(v: Any): ComparisonOperator = {
      val availableOperators = comparisonOperators.toString()
      deserializationError(f"'$v' is not a valid operator, must be one of $availableOperators")
    }
  }
}
