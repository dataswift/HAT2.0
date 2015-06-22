package dalapi

import com.wordnik.swagger.annotations.{ApiModelProperty, ApiModel}
import dalapi.models.{ApiDataTable, ApiDataRecord, ApiDataField, ApiDataValue}
import spray.json.{NullOptions, DefaultJsonProtocol}
import scala.reflect.runtime.universe._
import scala.annotation.meta.field
import dalapi.models._

object InboundJsonProtocol extends DefaultJsonProtocol with NullOptions {
  implicit val virtualTableFormat = jsonFormat3(ApiDataTable)
  implicit val dataFieldformat = jsonFormat3(ApiDataField)
  implicit val apiDataRecord = jsonFormat2(ApiDataRecord)
  implicit val apiDataValueFormat = jsonFormat4(ApiDataValue)
}
