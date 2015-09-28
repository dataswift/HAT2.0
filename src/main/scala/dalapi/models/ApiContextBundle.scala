package dalapi.models

import dal.Tables._
import dalapi.models.ComparisonOperators.ComparisonOperator
import org.joda.time.LocalDateTime


object ComparisonOperators {
  sealed trait ComparisonOperator
  case object equal extends ComparisonOperator
  case object notEqual extends ComparisonOperator
  case object greaterThan extends ComparisonOperator
  case object lessThan extends ComparisonOperator
  case object like extends ComparisonOperator
  case object dateGreaterThan extends ComparisonOperator
  case object dateLessThan extends ComparisonOperator
  case object dateWeekdayGreaterThan extends ComparisonOperator
  case object dateWeekdayLessThan extends ComparisonOperator
  case object dateHourGreaterThan extends ComparisonOperator
  case object dateHourLessThan extends ComparisonOperator

  def fromString(value: String): ComparisonOperator = {
    Vector(equal, notEqual, greaterThan, lessThan, like,
      dateGreaterThan, dateLessThan,
      dateWeekdayGreaterThan, dateWeekdayLessThan,
      dateHourGreaterThan, dateHourLessThan).find(_.toString == value).get
  }

  val comparisonOperators: Set[ComparisonOperator] = Set(equal, notEqual, greaterThan, lessThan, like, dateGreaterThan,
    dateLessThan, dateWeekdayGreaterThan, dateWeekdayLessThan, dateHourGreaterThan, dateHourLessThan)
}


case class ApiBundlePropertySliceCondition(
    id: Option[Int],
    table: ApiBundlePropertySlice,
    operator: ComparisonOperator,
    value: String)

object ApiBundlePropertySliceCondition {
  def fromBundlePropertySliceCondition(condition: BundlePropertySliceConditionRow)(field: ApiDataField) : ApiBundlePropertySliceCondition = {
    new ApiBundlePropertySliceCondition(Some(condition.id),
      table, condition.operator, condition.value,
      ComparisonOperators.fromString(condition.operator))
  }
}

case class ApiBundlePropertySlice(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    table: ApiDataTable,
    conditions: Seq[ApiBundleTableCondition])

object ApiBundlePropertySlice {
  def fromBundleTableSlice(slice: BundleTablesliceRow)(table: ApiDataTable) : ApiBundleTableSlice = {
    new ApiBundleTableSlice(Some(slice.id),
      Some(slice.dateCreated), Some(slice.lastUpdated),
      table, Seq())
  }
}

case class ApiEntitySelection(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    entityname: String,
    table: ApiEntity,
    entitykind: String)

object ApiEntitySelection {
  def fromEntitySelection(entitySelection: EntitySelectionRow)(table: ApiEntity) : ApiEntitySelection = {
    new ApiEntitySelection(Some(entitySelection.id),
      Some(entitySelection.dateCreated), Some(entitySelection.lastUpdated),
      entitySelection.name, table, entitykind)
  }
}


case class ApiBundleContext(
    selftable: ApiBundleContext
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    table: ApiEntitySelection,
    slices: Option[Seq[ApiBundleTableSlice]])

object ApiBundleContext {
  def fromBundleContext(bundleContext: BundleContextRow)(selftable: ApiBundleContext)(table: ApiEntitySelection) : ApiBundleTable = {
    new ApiBundleTable(selftable, Some(bundleContext.id),
      Some(bundleContext.dateCreated), Some(bundleContext.lastUpdated),
      bundleContext.name, table, None)
  }
}