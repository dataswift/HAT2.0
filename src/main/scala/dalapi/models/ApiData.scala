package dalapi.models

import org.joda.time.LocalDateTime

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

case class ApiDataField(
                         id: Option[Int],
                         dateCreated: Option[LocalDateTime],
                         lastUpdated: Option[LocalDateTime],
                         tableId: Int,
                         name: String)

case class ApiDataRecord (
                           id: Option[Int],
                           name: String
                           )

case class ApiDataTable(
                         id: Option[Int],
                         dateCreated: Option[LocalDateTime],
                         lastUpdated: Option[LocalDateTime],
                         name: String,
                         source: String,
                         fields: Option[Seq[ApiDataField]],
                         subTables: Seq[ApiDataTable]
                         )


case class ApiDataValue(
                         id: Option[Int],
                         value: String,
                         fieldId: Int,
                         recordId: Int)