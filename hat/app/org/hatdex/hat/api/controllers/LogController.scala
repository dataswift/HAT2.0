package org.hatdex.hat.api.controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import org.hatdex.hat.api.json.ApplicationJsonProtocol
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.{ LogService, RemoteExecutionContext }
import org.hatdex.hat.authentication.{ ContainsApplicationRole, HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.utils.HatBodyParsers
import org.hatdex.hat.api.json.HatJsonFormats._
import org.hatdex.hat.api.service.applications.ApplicationsService
import play.api.libs.json.Json
import play.api.mvc.{ Action, ControllerComponents }

import scala.concurrent.Future

class LogController @Inject() (
    components: ControllerComponents,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment])(
    implicit
    val ec: RemoteExecutionContext,
    logService: LogService,
    applicationsService: ApplicationsService)
  extends HatApiController(components, silhouette) with ApplicationJsonProtocol {

  def log(): Action[LogRequest] = SecuredAction(ContainsApplicationRole(Owner(), ApplicationList()) || WithRole(Owner())).async(parsers.json[LogRequest]) { request =>
    val logRequest = request.body
    val hatAddress = request.dynamicEnvironment.domain
    val (applicationId, applicationVersion) = request.authenticator.customClaims.map { customClaims =>
      ((customClaims \ "application").asOpt[String], (customClaims \ "applicationVersion").asOpt[String])
    }.getOrElse((None, None))

    logService.logAction(hatAddress, logRequest.actionCode, logRequest.message, logRequest.logGroup, applicationId, applicationVersion)

    Future.successful(Ok(Json.toJson(SuccessResponse(s"${logRequest.actionCode} logged"))))
  }
}
