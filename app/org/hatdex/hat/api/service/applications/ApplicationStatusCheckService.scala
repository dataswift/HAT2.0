package org.hatdex.hat.api.service.applications

import io.dataswift.models.hat.applications.ApplicationStatus
import org.hatdex.hat.api.service.RemoteExecutionContext
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class ApplicationStatusCheckService @Inject() (
    wsClient: WSClient
  )(implicit val rec: RemoteExecutionContext) {

  def status(
      statusCheck: ApplicationStatus.Status,
      token: String): Future[Boolean] =
    statusCheck match {
      case _: ApplicationStatus.Internal => Future.successful(true)
      case s: ApplicationStatus.External => status(s, token)
    }

  protected def status(
      statusCheck: ApplicationStatus.External,
      token: String): Future[Boolean] =
    wsClient
      .url(statusCheck.statusUrl)
      .withHttpHeaders("x-auth-token" -> token)
      .withRequestTimeout(5000.millis)
      .get()
      .map(_.status == statusCheck.expectedStatus)
}
