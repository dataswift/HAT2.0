// This is set as it is to access the private members of the filters to test them.
package com.github.stijndehaes.playprometheusfilters.filters

/* Test cases based on the archived project: https://github.com/stijndehaes/play-prometheus-filters */

import org.specs2.mock.Mockito
import play.api.test.{ FakeRequest, PlaySpecification }
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.api.routing.Router
import io.prometheus.client.CollectorRegistry
import play.api.libs.typedmap.TypedMap
import play.api.routing.HandlerDef
import play.api.mvc.{ AbstractController, ControllerComponents }
import javax.inject.Inject

class PrometheusFiltersSpec extends PlaySpecification with Mockito {
  implicit val system       = ActorSystem()
  implicit val executor     = system.dispatcher
  implicit val materializer = ActorMaterializer()

  sequential

  "LatencyFilter" should {
    "Measure the latency" in {
      val promFilter: LatencyFilter = new LatencyFilter(mock[CollectorRegistry])
      val fakeRequest               = FakeRequest()
      val action                    = new MockController(stubControllerComponents()).ok

      await(promFilter(action)(fakeRequest).run())

      val metrics = promFilter.requestLatency.collect()
      metrics must have size 1
      val samples = metrics.get(0).samples

      val countSample = samples.get(samples.size() - 2)
      countSample.value mustEqual 1.0
      countSample.labelValues must have size 0
    }
  }

  "StatusAndRouteLatencyFilter" should {
    "Measure the latency and status" in {
      val expectedLabelCount: Long = 5
      val expectedMethod           = "test"
      val expectedStatus: String   = "200"
      val expectedControllerName   = "promController"
      val expectedPath             = "/path"
      val expectedVerb             = "GET"
      val listOfMatches            = List(expectedMethod, expectedStatus, expectedControllerName, expectedPath, expectedVerb)

      val promFilter = new StatusAndRouteLatencyFilter(mock[CollectorRegistry])
      val fakeRequest = FakeRequest().withAttrs(
        TypedMap(
          Router.Attrs.HandlerDef -> HandlerDef(null,
                                                null,
                                                expectedControllerName,
                                                expectedMethod,
                                                null,
                                                expectedVerb,
                                                expectedPath,
                                                null,
                                                null
              )
        )
      )

      val action =
        new MockController(stubControllerComponents()).ok

      await(promFilter(action)(fakeRequest).run())

      val metrics = promFilter.requestLatency.collect()
      metrics must have size 1
      val samples     = metrics.get(0).samples
      val countSample = samples.get(samples.size() - 2)

      countSample.value mustEqual 1.0
      countSample.labelValues.size mustEqual expectedLabelCount
      countSample.labelValues mustEqual listOfMatches
    }
  }

  "StatusCounterFilter" should {
    "Count the requests with status" in {
      val promFilter  = new StatusCounterFilter(mock[CollectorRegistry])
      val fakeRequest = FakeRequest()
      val actionOK    = new MockController(stubControllerComponents()).ok
      val actionError = new MockController(stubControllerComponents()).error

      val bothRequests = for {
        ok <- promFilter(actionOK)(fakeRequest).run()
        ko <- promFilter(actionError)(fakeRequest).run()
      } yield (ok, ko)

      await(bothRequests)

      val metrics = promFilter.requestCounter.collect()
      metrics.size mustEqual 1
      val samplesOk    = metrics.get(0).samples.get(0)
      val samplesError = metrics.get(0).samples.get(1)

      samplesOk.value mustEqual 1.0
      samplesOk.labelValues.size mustEqual 1
      samplesOk.labelValues.get(0) mustEqual "200"

      samplesError.value mustEqual 1.0
      samplesError.labelValues.size mustEqual 1
      samplesError.labelValues.get(0) mustEqual "404"
    }
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
