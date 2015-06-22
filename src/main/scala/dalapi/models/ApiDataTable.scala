package dalapi.models

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

@ApiModel(description = "A Virtual Data Table")
case class ApiDataTable(
                         @(ApiModelProperty @field)(value = "unique identifier for the table")
                         id: Option[Int],   // FIXME: swagger UI does not handle Option field correctly - does not show them in results
                         @(ApiModelProperty @field )(value = "table name")
                         name: String,
                         @(ApiModelProperty @field)(value = "data source name")
                         source: String)
