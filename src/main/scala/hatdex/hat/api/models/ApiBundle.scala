package hatdex.hat.api.models

import hatdex.hat.dal.Tables._
import hatdex.hat.api.models.ComparisonOperators.ComparisonOperator
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

case class ApiBundleDataSourceField(name: String, description: String, fields: Option[List[ApiBundleDataSourceField]])
case class ApiBundleDataSourceDataset(name: String, description: String, fields: List[ApiBundleDataSourceField])
case class ApiBundleDataSourceStructure(source: String, datasets: List[ApiBundleDataSourceDataset])

case class ApiBundleContextless(
  id: Option[Int],
  dateCreated: Option[LocalDateTime],
  lastUpdated: Option[LocalDateTime],
  name: String,
  sources: Option[Seq[ApiBundleDataSourceStructure]])

object ApiBundleContextless {
  def fromBundleContextless(bundleContextless: BundleContextlessRow) : ApiBundleContextless = {
    new ApiBundleContextless(Some(bundleContextless.id),
      Some(bundleContextless.dateCreated), Some(bundleContextless.lastUpdated),
      bundleContextless.name, None)
  }

  def fromBundleContextlessSources(bundleContextless: BundleContextlessRow)(sources: Option[Seq[ApiBundleDataSourceStructure]]) : ApiBundleContextless = {
    ApiBundleContextless(Some(bundleContextless.id),
      Some(bundleContextless.dateCreated), Some(bundleContextless.lastUpdated),
      bundleContextless.name, sources)
  }
}

case class ApiBundleContextlessDatasetData(
  name: String,
  table: ApiDataTable, // Used to tag which table is bundled
  data: Option[Seq[ApiDataRecord]]) // Data is optional, only used on the outbound

case class ApiBundleContextlessData(
  id: Int,
  name: String,
  dataGroups: Map[String, Seq[ApiBundleContextlessDatasetData]])

object ApiBundleContextlessData {
  def fromDbModel(bundleContextless: BundleContextlessRow, dataGroups: Map[String, Seq[ApiBundleContextlessDatasetData]]): ApiBundleContextlessData = {
    ApiBundleContextlessData(bundleContextless.id, bundleContextless.name, dataGroups)
  }
}