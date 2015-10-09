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
