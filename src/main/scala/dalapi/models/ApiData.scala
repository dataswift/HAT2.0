package dalapi.models

import org.joda.time.LocalDateTime

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

case class ApiDataField(
                         id: Option[Int],
                         dateCreated: Option[LocalDateTime],
                         lastUpdated: Option[LocalDateTime],
                         tableId: Int,
                         name: String,
                         values: Option[Seq[ApiDataValue]]
                         )

case class ApiDataFieldValues(
                             field: ApiDataField,
                             data: Seq[ApiDataValue]
                               )

case class ApiDataRecord(
                          id: Option[Int],
                          dateCreated: Option[LocalDateTime],
                          lastUpdated: Option[LocalDateTime],
                          name: String,
                          tables: Option[Seq[ApiDataTable]]
                          )

case class ApiDataTable(
                         id: Option[Int],
                         dateCreated: Option[LocalDateTime],
                         lastUpdated: Option[LocalDateTime],
                         name: String,
                         source: String,
                         fields: Option[Seq[ApiDataField]],
                         subTables: Option[Seq[ApiDataTable]]
                         )


case class ApiDataValue(
                         id: Option[Int],
                         dateCreated: Option[LocalDateTime],
                         lastUpdated: Option[LocalDateTime],
                         value: String,
                         fieldId: Int,
                         recordId: Int
                         )