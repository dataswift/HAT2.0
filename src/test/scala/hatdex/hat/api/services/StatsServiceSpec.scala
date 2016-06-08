package hatdex.hat.api.services

import akka.event.{Logging, LoggingAdapter}
import hatdex.hat.api.endpoints.Bundles
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.{ApiDataDebitOut, ApiBundleContextlessData}
import hatdex.hat.api.service.StatsService
import org.specs2.mutable.Specification
import spray.json._
import spray.testkit.Specs2RouteTest

class StatsServiceSpec extends Specification with Specs2RouteTest with StatsService {
  val logger: LoggingAdapter = Logging.getLogger(system, "tests")

  import JsonProtocol._

  "Stats Service computations" should {
    val valuesString = hatdex.hat.api.endpoints.jsonExamples.DataDebitExamples.dataDebitContextlessValues
    val data = JsonParser(valuesString).convertTo[ApiDataDebitOut]

    "Corrently compute data debit bundle record count" in {
      data.bundleContextless must beSome
      val bundleContextless = data.bundleContextless.get
      val firstBundleTable = bundleContextless.dataGroups.head.values.head
      getBundleTableRecordCount(firstBundleTable)._2 must be equalTo(5)
    }

    "Correctly compute table value counts" in {
      data.bundleContextless must beSome
      val bundleContextless = data.bundleContextless.get
      val firstBundleTable = bundleContextless.dataGroups.head.values.head
      val stats = getTableValueCounts(firstBundleTable)

      // Must have extracted the right number of tables
      stats.keys.toSeq.length must be equalTo(2)

      val electricityTableStats = stats.find(_._1.name == "kitchenElectricity")
      electricityTableStats must beSome
      electricityTableStats.get._2 must be equalTo(10)

      val kitchenTableStats = stats.find(_._1.name == "kitchen")
      kitchenTableStats must beSome
      kitchenTableStats.get._2 must be equalTo(15)
    }

    "Correctly compute field value counts" in {
      data.bundleContextless must beSome
      val bundleContextless = data.bundleContextless.get
      val firstBundleTable = bundleContextless.dataGroups.head.values.head
      val stats = getFieldValueCounts(firstBundleTable)
      stats map { stat =>
        stat._2 must be equalTo(5)
      }
      stats.keys.toSeq.length must be equalTo(3)
    }

    "Correctly compute overall data bundle stats" in {
      data.bundleContextless must beSome
      val bundleContextless = data.bundleContextless.get
      val (totalBundleRecords, bundleTableStats, tableValueStats, fieldValueStats) = getBundleStats(bundleContextless)

      totalBundleRecords must be equalTo(5)
    }
  }

  "Data Stats reporting" should {
    val valuesString = hatdex.hat.api.endpoints.jsonExamples.DataDebitExamples.dataDebitContextlessValues
    val data = JsonParser(valuesString).convertTo[ApiDataDebitOut]
    "Correctly convert stats" in {
      data.bundleContextless must beSome
      val bundleContextless = data.bundleContextless.get
      val (totalBundleRecords, bundleTableStats, tableValueStats, fieldValueStats) = getBundleStats(bundleContextless)

      val stats = convertBundleStats(tableValueStats, fieldValueStats)
      stats.length must beEqualTo(2)
    }
  }
}
