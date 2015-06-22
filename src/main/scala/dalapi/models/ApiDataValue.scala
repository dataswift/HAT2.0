package dalapi.models

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

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