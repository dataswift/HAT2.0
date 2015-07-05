package dalapi.models

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

case class ApiDataField(
                         id: Option[Int],
                         tableId: Int,
                         name: String)

case class ApiDataRecord (
                           id: Option[Int],
                           name: String
                           )

case class ApiDataTable(
                         id: Option[Int],
                         name: String,
                         source: String)


case class ApiDataValue(
                         id: Option[Int],
                         value: String,
                         fieldId: Int,
                         recordId: Int)