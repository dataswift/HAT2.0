package org.hatdex.hat.filters

import scala.concurrent.{ Await, ExecutionContextExecutor, Future }
import akka.actor.ActorSystem
import io.dataswift.test.common.BaseSpec
import play.api.Configuration
import play.api.test.Helpers._
import play.api.test._
import play.api.routing.Router
import io.prometheus.client.CollectorRegistry
import com.github.stijndehaes.playprometheusfilters.filters.{
  LatencyFilter,
  StatusAndRouteLatencyFilter,
  StatusCounterFilter
}
import org.hatdex.hat.api.service.applications.ApplicationsServiceContext
import play.api.libs.typedmap.TypedMap
import play.api.routing.HandlerDef
import org.specs2.mock.Mockito
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents }
import play.api.cache.AsyncCacheApi

import javax.inject.Inject

class PrometheusFiltersSpec extends PlaySpecification with Mockito with ApplicationsServiceContext {
  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  val configuration                         = mock[Configuration]

  sequential

  "LatencyFilter" should {
    "Measure the latency" in {
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
      countSample.value mustEqual 1.0
      countSample.labelValues must have size 0
    }
  }

//  "StatusAndRouteLatencyFilter" should {
//    "Measure the latency and status" in {
//      val expectedLabelCount: Long = 6
//      val expectedMethod           = "test"
//      val expectedStatus: String   = "200"
//      val expectedControllerName   = "promController"
//      val expectedPath             = "/path"
//      val expectedVerb             = "GET"
//      val expectedLatencyBucket    = "0.005"
//
//      // I needed to have an independnt collector per test, otherwise they complain about having duplicate labels.
//      val registryTwo = new CollectorRegistry(true)
//      val promFilter  = new StatusAndRouteLatencyFilter(registryTwo, configuration)
//      val fakeRequest = FakeRequest().withAttrs(
//        TypedMap(
//          Router.Attrs.HandlerDef -> HandlerDef(null, null, "promController", "test", null, "GET", "/path", null, null)
//        )
//      )
//
//      val action =
//        new MockController(stubControllerComponents()).ok
//
//      await(promFilter(action)(fakeRequest).run())
//
//      val metrics = promFilter.metrics(0).metric.collect()
//      metrics must have size 1
//      val samples = metrics.get(0).samples
//
//      samples.get(0).value mustEqual 1.0
//      samples.get(0).labelValues.toArray.length mustEqual expectedLabelCount
//      samples.get(0).labelValues.get(0) mustEqual expectedMethod
//      samples.get(0).labelValues.get(1) mustEqual expectedStatus
//      samples.get(0).labelValues.get(2) mustEqual expectedControllerName
//      samples.get(0).labelValues.get(3) mustEqual expectedPath
//      samples.get(0).labelValues.get(4) mustEqual expectedVerb
//      samples.get(0).labelValues.get(5) mustEqual expectedLatencyBucket
//    }
//  }
//
//  "StatusCounterFilter" should {
//    "Count the requests with status" in {
//      // I needed to have an independnt collector per test, otherwise they complain about having duplicate labels.
//      val registryThree = new CollectorRegistry(true)
//      val promFilter    = new StatusCounterFilter(registryThree, configuration)
//      val fakeRequest   = FakeRequest()
//      val action        = new MockController(stubControllerComponents()).ok
//
//      await(promFilter(action)(fakeRequest).run())
//
//      val metrics = promFilter.metrics(0).metric.collect()
//      metrics must have size 1
//      val samples = metrics.get(0).samples
//      samples.get(0).value mustEqual 1.0
//      samples.get(0).labelValues must have size 1
//      samples.get(0).labelValues.get(0) mustEqual "200"
//    }
//  }
}

class MockController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {
  def ok: Action[AnyContent] =
    Action {
      Ok("ok")
    }

  def error: Action[AnyContent] =
    Action {
      NotFound("error")
    }
}
