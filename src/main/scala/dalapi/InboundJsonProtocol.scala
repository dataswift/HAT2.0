package dalapi

import com.wordnik.swagger.annotations.{ApiModelProperty, ApiModel}
import spray.json.{NullOptions, DefaultJsonProtocol}

import scala.annotation.meta.field

@ApiModel(description = "A Virtual Data Table")
case class ApiDataTable(
                         @(ApiModelProperty @field)(value = "unique identifier for the table")
                         id: Option[Int],   // FIXME: swagger UI does not handle Option field correctly - does not show them in results
                         @(ApiModelProperty @field )(value = "table name")
                         name: String,
                         @(ApiModelProperty @field)(value = "data source name")
                         source: String)

@ApiModel(description = "A Virtual Data Field")
case class ApiDataField(
                      @(ApiModelProperty @field)(value = "unique identifier for the field")
                      id: Option[Int],      // FIXME: swagger UI does not handle Option field correctly - does not show them in results
                      @(ApiModelProperty @field)(value = "table id within which to create the field")
                      tableId: Int,
                      @(ApiModelProperty @field)(value = "name of the field")
                      name: String)

@ApiModel(description = "Data Record, defines a group of data values")
case class ApiDataRecord (
                           @(ApiModelProperty @field)(value = "unique identifier for the value")
                           id: Option[Int],
                           @(ApiModelProperty @field)(value = "name of the record, if any")
                           name: String
                           )

@ApiModel(description = "Data Value")
case class ApiDataValue(
                         @(ApiModelProperty @field)(value = "unique identifier for the value")
                         id: Option[Int],
                         @(ApiModelProperty @field)(value = "the value")
                         value: String,
                         @(ApiModelProperty @field)(value = "field (column) id that holds the value; field is always associated witha  table")
                         fieldId: Int,
                         @(ApiModelProperty @field)(value = "record id the value is part of")
                         recordId: Int)

object InboundJsonProtocol extends DefaultJsonProtocol with NullOptions {
  implicit val virtualTableFormat = jsonFormat3(ApiDataTable)
  implicit val dataFieldformat = jsonFormat3(ApiDataField)
  implicit val apiDataRecord = jsonFormat2(ApiDataRecord)
  implicit val apiDataValueFormat = jsonFormat4(ApiDataValue)
}
