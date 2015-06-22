package dalapi.models

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

@ApiModel(description = "Data Record, defines a group of data values")
case class ApiDataRecord (
                           @(ApiModelProperty @field)(value = "unique identifier for the value")
                           id: Option[Int],
                           @(ApiModelProperty @field)(value = "name of the record, if any")
                           name: String
                           )
