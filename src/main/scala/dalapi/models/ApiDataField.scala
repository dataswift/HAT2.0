package dalapi.models

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

@ApiModel(description = "A Virtual Data Field")
case class ApiDataField(
                      @(ApiModelProperty @field)(value = "unique identifier for the field")
                      id: Option[Int],      // FIXME: swagger UI does not handle Option field correctly - does not show them in results
                      @(ApiModelProperty @field)(value = "table id within which to create the field")
                      tableId: Int,
                      @(ApiModelProperty @field)(value = "name of the field")
                      name: String)
