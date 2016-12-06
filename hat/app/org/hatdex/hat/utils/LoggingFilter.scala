package org.hatdex.hat.utils

import javax.inject.{ Inject, Named }

import play.api.mvc.{ Filter, RequestHeader, Result }
import play.api.Logger
import play.api.routing.Router.Tags

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import javax.inject.Inject

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.Materializer
import org.hatdex.hat.resourceManagement.actors.HatServerProviderActor
import play.api.http.DefaultHttpFilters
import play.filters.gzip.GzipFilter
import scala.concurrent.duration._

class Filters @Inject() (
  gzip: GzipFilter,
  log: LoggingFilter
) extends DefaultHttpFilters(gzip, log)

class LoggingFilter @Inject() (@Named("hatServerProviderActor") serverProviderActor: ActorRef, implicit val mat: Materializer) extends Filter {
  val logger = Logger("http")

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

    val startTime = System.currentTimeMillis
    implicit val timeout: akka.util.Timeout = 200 milliseconds

    for {
      result <- nextFilter(requestHeader)
      activeHats <- serverProviderActor.ask(HatServerProviderActor.GetHatServersActive()).mapTo[HatServerProviderActor.HatServersActive]
    } yield {
      val endTime = System.currentTimeMillis
      val requestTime = endTime - startTime

      logger.info(s"[${requestHeader.method}:${requestHeader.host}${requestHeader.uri}] [${result.header.status}] [TIME ${requestTime}ms] [HATs ${activeHats.active}]")

      result.withHeaders("Request-Time" -> requestTime.toString)
    }
  }
}