package dalapi.models

import dal.Tables._
import dalapi.models.ComparisonOperators.ComparisonOperator
import org.joda.time.LocalDateTime
import scala.collection.immutable.Map

object ComparisonOperators {
  sealed trait ComparisonOperator
  case object equal extends ComparisonOperator
  case object notEqual extends ComparisonOperator
  case object greaterThan extends ComparisonOperator
  case object lessThan extends ComparisonOperator
  case object like extends ComparisonOperator
//  case object dateGreaterThan extends ComparisonOperator
//  case object dateLessThan extends ComparisonOperator
//  case object dateWeekdayGreaterThan extends ComparisonOperator
//  case object dateWeekdayLessThan extends ComparisonOperator
//  case object dateHourGreaterThan extends ComparisonOperator
//  case object dateHourLessThan extends ComparisonOperator

  def fromString(value: String): ComparisonOperator = {
    Vector(
      equal, notEqual, greaterThan, lessThan, like
//      dateGreaterThan, dateLessThan,
//      dateWeekdayGreaterThan, dateWeekdayLessThan,
//      dateHourGreaterThan, dateHourLessThan
    ).find(_.toString == value).get
  }

  val comparisonOperators: Set[ComparisonOperator] = Set(equal, notEqual, greaterThan, lessThan, like)
//    dateGreaterThan, dateLessThan, dateWeekdayGreaterThan, dateWeekdayLessThan, dateHourGreaterThan, dateHourLessThan)
}

case class ApiBundleTableCondition(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    field: ApiDataField,
    value: String,
    operator: ComparisonOperator)

object ApiBundleTableCondition {
  def fromBundleTableSliceCondition(condition: BundleTablesliceconditionRow)(field: ApiDataField) : ApiBundleTableCondition = {
    new ApiBundleTableCondition(Some(condition.id),
      Some(condition.dateCreated), Some(condition.lastUpdated),
      field, condition.value,
      ComparisonOperators.fromString(condition.operator))
  }
}

case class ApiBundleTableSlice(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    table: ApiDataTable,
    conditions: Seq[ApiBundleTableCondition])

object ApiBundleTableSlice {
  def fromBundleTableSlice(slice: BundleTablesliceRow)(table: ApiDataTable) : ApiBundleTableSlice = {
    new ApiBundleTableSlice(Some(slice.id),
      Some(slice.dateCreated), Some(slice.lastUpdated),
      table, Seq())
  }
}


case class ApiBundleTable(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    table: ApiDataTable,                      // Used to tag which table is bundled
    slices: Option[Seq[ApiBundleTableSlice]],
    data: Option[Iterable[ApiDataRecord]])     // Data is optional, only used on the outbound

object ApiBundleTable {
  def fromBundleTable(bundleTable: BundleTableRow)(table: ApiDataTable) : ApiBundleTable = {
    new ApiBundleTable(Some(bundleTable.id),
      Some(bundleTable.dateCreated), Some(bundleTable.lastUpdated),
      bundleTable.name, table, None, None)
  }
}

case class ApiBundleCombination(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    bundleTable: ApiBundleTable,
    bundleJoinField: Option[ApiDataField],
    bundleTableField: Option[ApiDataField],
    operator: Option[ComparisonOperator])

object ApiBundleCombination {
  def fromBundleJoin(bundleCombination: BundleJoinRow)
                    (bundleJoinField: Option[ApiDataField], bundleTableField: Option[ApiDataField], bundleTable: ApiBundleTable): ApiBundleCombination = {

    val operator = bundleCombination.operator.map(ComparisonOperators.fromString)

    new ApiBundleCombination(Some(bundleCombination.id),
      Some(bundleCombination.dateCreated), Some(bundleCombination.lastUpdated),
      bundleCombination.name, bundleTable,
      bundleJoinField, bundleTableField,
      operator)
  }
}

case class ApiBundleContextless(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    tables: Seq[ApiBundleCombination])

object ApiBundleContextless {
  def fromBundleContextless(bundleContextless: BundleContextlessRow) : ApiBundleContextless = {
    new ApiBundleContextless(Some(bundleContextless.id),
      Some(bundleContextless.dateCreated), Some(bundleContextless.lastUpdated),
      bundleContextless.name, Seq())
  }

  def fromBundleContextlessTables(bundleContextless: BundleContextlessRow)(tables: Seq[ApiBundleCombination]) : ApiBundleContextless = {
    new ApiBundleContextless(Some(bundleContextless.id),
      Some(bundleContextless.dateCreated), Some(bundleContextless.lastUpdated),
      bundleContextless.name, tables)
  }
}


case class ApiBundleContextlessData(
    id: Int,
    name: String,
    dataGroups: Iterable[Map[String, ApiBundleTable]])

object ApiBundleContextlessData {
  def fromDbModel(bundleContextless: BundleContextlessRow, dataGroups: Iterable[Map[String, ApiBundleTable]]): ApiBundleContextlessData = {
    new ApiBundleContextlessData(bundleContextless.id, bundleContextless.name, dataGroups)
  }
}