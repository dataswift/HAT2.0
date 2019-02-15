package org.hatdex.hat.api.controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import org.hatdex.hat.api.json.ApplicationJsonProtocol
import org.hatdex.hat.api.models.Owner
import org.hatdex.hat.api.service.RemoteExecutionContext
import org.hatdex.hat.api.service.applications.ApplicationsService
import org.hatdex.hat.authentication.{ ContainsApplicationRole, HatApiAuthEnvironment, HatApiController, WithRole }
import play.api.{ Configuration, Logger }
import play.api.http.HttpEntity
import play.api.libs.ws.WSClient
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

class HattersRequestProxy @Inject() (
    components: ControllerComponents,
    silhouette: Silhouette[HatApiAuthEnvironment],
    wsClient: WSClient)(
    implicit
    val ec: RemoteExecutionContext,
    applicationsService: ApplicationsService,
    configuration: Configuration)
  extends HatApiController(components, silhouette) with ApplicationJsonProtocol {

  val logger = Logger(this.getClass)

  def proxyRequest(path: String, method: String = "GET"): Action[AnyContent] = SecuredAction(ContainsApplicationRole(Owner()) || WithRole(Owner())).async { implicit request =>
    val hattersUrl = s"${configuration.underlying.getString("hatters.scheme")}${configuration.underlying.getString("hatters.address")}"
    logger.info(s"Proxy $method request to $hattersUrl/$path with parameters: ${request.queryString}")

    // TODO: Generate validate Token
    // TODO Comment : hatters does not seem to require validate token. SECURITY HOLE!?
    val baseRequest = wsClient.url(s"$hattersUrl/$path")
      //.withHttpHeaders("x-auth-token" → token.accessToken)
      .addQueryStringParameters(request.queryString.map(p ⇒ (p._1, p._2.head)).toSeq: _*)
      .withMethod(method)

    request.body.asJson.fold(baseRequest)(b ⇒ baseRequest.withBody(b))
      .stream()
      .map(r ⇒ new Status(r.status).sendEntity(HttpEntity.Strict(r.bodyAsBytes, Some("application/json"))))
  }
}
