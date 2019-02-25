package org.hatdex.hat.api.controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import org.hatdex.hat.api.json.ApplicationJsonProtocol
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.{ LogService, RemoteExecutionContext }
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.utils.HatBodyParsers
import org.hatdex.hat.api.json.HatJsonFormats._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{ Action, ControllerComponents }

class LogController @Inject() (
    components: ControllerComponents,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment])(
    implicit
    val ec: RemoteExecutionContext,
    logService: LogService)
  extends HatApiController(components, silhouette) with ApplicationJsonProtocol {

  protected val logger = Logger(this.getClass)

  def log(): Action[LogRequest] = SecuredAction.async(parsers.json[LogRequest]) { request =>
    val logRequest = request.body
    val hatAddress = request.dynamicEnvironment.domain
    val applicationData: Option[(String, String)] = request.authenticator.customClaims.flatMap { customClaims =>
      (customClaims \ "application").asOpt[String] match {
        case Some(applicationId) => Some((applicationId, (customClaims \ "applicationVersion").as[String]))
        case None                => None
      }
    }

    logService.logAction(hatAddress, logRequest.actionCode, logRequest.message, logRequest.logGroup, applicationData).map { _ =>
      Ok(Json.toJson(SuccessResponse(s"${logRequest.actionCode} logged")))
    }.recover {
      case _ => {
        logger.error(s"LogActionError::${logRequest.actionCode}")
        BadRequest(Json.toJson(ErrorMessage("LogActionError", s"LogActionError::${logRequest.actionCode} logging failed")))
      }
    }
  }
}
