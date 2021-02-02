package org.hatdex.hat.filters

//import java.util.concurrent.TimeUnit

import scala.concurrent.{ Await, ExecutionContextExecutor, Future }

import akka.actor.ActorSystem
import akka.stream.Materializer
//import akka.stream.{ ActorMaterializer, Materializer }
//import akka.util.Timeout
//import com.typesafe.config.ConfigFactory
import io.dataswift.test.common.BaseSpec
//import org.scalamock.scalatest.MockFactory
import play.api.Configuration
//import play.api.http.Status
//import play.api.libs.json.Json
//import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.api.routing.Router
import io.prometheus.client.CollectorRegistry
import com.github.stijndehaes.playprometheusfilters.filters.{
  LatencyFilter,
  StatusAndRouteLatencyFilter,
  StatusCounterFilter
}
import play.api.libs.typedmap.TypedMap
import play.api.routing.HandlerDef
//import com.github.stijndehaes.playprometheusfilters.controllers.PrometheusController

import play.api.mvc.{ AbstractController, ControllerComponents }
import javax.inject.Inject
//import com.github.stijndehaes.playprometheusfilters.metrics.LatencyRequestMetric

class PrometheusFiltersSpec extends BaseSpec {

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  implicit val system: ActorSystem          = ActorSystem()
  implicit val materializer: Materializer   = Materializer.matFromSystem(system)
  private val configuration                 = mock[Configuration]

  "LatencyFilter" should "Measure the latency" in {
    // I needed to have an independnt collector per test, otherwise they complain about having duplicate labels.
    val registryOne = new CollectorRegistry(true)
    val promFilter  = new LatencyFilter(registryOne, configuration)
    val fakeRequest = FakeRequest()
    val action      = new MockController(stubControllerComponents()).ok

    await(promFilter(action)(fakeRequest).run())

    val metrics = promFilter.metrics(0).metric.collect()
    metrics must have size 1
    val samples     = metrics.get(0).samples
    val countSample = samples.get(samples.size() - 2)
    countSample.value mustBe 1.0
    countSample.labelValues must have size 0
  }

  "StatusAndRouteLatencyFilter" should "Measure the latency and status" in {
    val expectedLabelCount: Long = 6
    val expectedMethod           = "test"
    val expectedStatus: String   = "200"
    val expectedControllerName   = "promController"
    val expectedPath             = "/path"
    val expectedVerb             = "GET"
    val expectedLatencyBucket    = "0.005"

    // I needed to have an independnt collector per test, otherwise they complain about having duplicate labels.
    val registryTwo = new CollectorRegistry(true)
    val promFilter  = new StatusAndRouteLatencyFilter(registryTwo, configuration)
    val fakeRequest = FakeRequest().withAttrs(
      TypedMap(
        Router.Attrs.HandlerDef -> HandlerDef(null, null, "promController", "test", null, "GET", "/path", null, null)
      )
    )

    val action =
      new MockController(stubControllerComponents()).ok

    await(promFilter(action)(fakeRequest).run())

    val metrics = promFilter.metrics(0).metric.collect()
    metrics must have size 1
    val samples = metrics.get(0).samples

    samples.get(0).value mustBe 1.0
    samples.get(0).labelValues must have size expectedLabelCount
    samples.get(0).labelValues.get(0) mustBe expectedMethod
    samples.get(0).labelValues.get(1) mustBe expectedStatus
    samples.get(0).labelValues.get(2) mustBe expectedControllerName
    samples.get(0).labelValues.get(3) mustBe expectedPath
    samples.get(0).labelValues.get(4) mustBe expectedVerb
    samples.get(0).labelValues.get(5) mustBe expectedLatencyBucket
  }

  "StatusCounterFilter" should "Count the requests with status" in {
    // I needed to have an independnt collector per test, otherwise they complain about having duplicate labels.
    val registryThree = new CollectorRegistry(true)
    val promFilter    = new StatusCounterFilter(registryThree, configuration)
    val fakeRequest   = FakeRequest()
    val action        = new MockController(stubControllerComponents()).ok

    await(promFilter(action)(fakeRequest).run())

    val metrics = promFilter.metrics(0).metric.collect()
    metrics must have size 1
    val samples = metrics.get(0).samples
    samples.get(0).value mustBe 1.0
    samples.get(0).labelValues must have size 1
    samples.get(0).labelValues.get(0) mustBe "200"
  }
}

class MockController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {
  def ok =
    Action {
      Ok("ok")
    }

  def error =
    Action {
      NotFound("error")
    }
}
