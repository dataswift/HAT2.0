package org.hatdex.hat.api.controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.RemoteExecutionContext
import org.hatdex.hat.authentication.{HatApiAuthEnvironment, HatApiController}
import org.hatdex.hat.resourceManagement.HatServer
import play.api.{Configuration, Logger}
import play.api.cache.AsyncCacheApi
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.Future

class SystemController @Inject() (
                                   components: ControllerComponents,
                                   cache: AsyncCacheApi,
                                   configuration: Configuration,
                                   silhouette: Silhouette[HatApiAuthEnvironment])(implicit val ec: RemoteExecutionContext)
  extends HatApiController(components, silhouette) with HatJsonFormats {

  val hatSharedSecret: String = configuration.get[String]("resourceManagement.hatSharedSecret")

  private val logger = Logger(this.getClass)

  def beforeDestroy()(implicit hat: HatServer): Action[AnyContent] = Action.async { implicit request =>
    request.headers.get("X-Auth-Token") match {
      case Some(authToken) if authToken == hatSharedSecret =>
        val clearApps = cache.remove(s"apps:${hat.domain}")
        val clearConfiguration = cache.remove(s"configuration:${hat.domain}")
        val clearServer = cache.remove(s"server:${hat.domain}")

        Future.sequence(Seq(clearApps, clearConfiguration, clearServer)).recover {
          case e =>
            logger.error(s"BEFORE DESTROY: Could not clear cache of ${hat.domain}. Reason: ${e.getMessage}")
        }

        // We don't care if the cacche is successfully cleared. We just go ahead and return successful.
        Future.successful(Ok(Json.toJson(SuccessResponse("beforeDestroy DONE"))))
      case Some(_) => Future.successful(Forbidden(Json.toJson(ErrorMessage("Invalid token", s"Not a valid token provided"))))
      case None    => Future.successful(Forbidden(Json.toJson(ErrorMessage("Credentials missing", s"Credentials required"))))
    }
  }

  /**
    * Not necessary to clear User from cache
    * Just need to clear app and configuration
    */
  private def clearCache(hat: HatServer): Unit = {
    val clearApps = cache.remove(s"apps:${hat.domain}")
    val clearConfiguration = cache.remove(s"configuration:${hat.domain}")
    val clearServer = cache.remove(s"server:${hat.domain}")

    Future.sequence(Seq(clearApps, clearConfiguration, clearServer))
  }
}
