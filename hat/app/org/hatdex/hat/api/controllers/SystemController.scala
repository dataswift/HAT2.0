package org.hatdex.hat.api.controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.RemoteExecutionContext
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import play.api.{ Configuration, Logger }
import play.api.cache.AsyncCacheApi
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

import scala.concurrent.Future
import scala.util.{ Failure, Success }

class SystemController @Inject() (
    components: ControllerComponents,
    cache: AsyncCacheApi,
    configuration: Configuration,
    silhouette: Silhouette[HatApiAuthEnvironment])(implicit val ec: RemoteExecutionContext)
  extends HatApiController(components, silhouette) with HatJsonFormats {

  val hatSharedSecret: String = configuration.get[String]("resourceManagement.hatSharedSecret")

  private val logger = Logger(this.getClass)

  def destroyCache: Action[AnyContent] = UserAwareAction.async { implicit request =>
    request.headers.get("X-Auth-Token") match {
      case Some(authToken) if authToken == hatSharedSecret =>
        val response = Ok(Json.toJson(SuccessResponse("beforeDestroy DONE")))

        val hatAddress = request.dynamicEnvironment.domain
        val clearApps = cache.remove(s"apps:$hatAddress")
        val clearConfiguration = cache.remove(s"configuration:$hatAddress")
        val clearServer = cache.remove(s"server:$hatAddress")

        // We don't care if the cache is successfully cleared. We just go ahead and return successful.
        Future.sequence(Seq(clearApps, clearConfiguration, clearServer)).onComplete {
          case Success(_)         => logger.info(s"BEFORE DESTROY: $hatAddress DONE")
          case Failure(exception) => logger.error(s"BEFORE DESTROY: Could not clear cache of $hatAddress. Reason: ${exception.getMessage}")
        }
        Future.successful(response)

      case Some(_) => Future.successful(Forbidden(Json.toJson(ErrorMessage("Invalid token", s"Not a valid token provided"))))

      case None    => Future.successful(Forbidden(Json.toJson(ErrorMessage("Credentials missing", s"Credentials required"))))
    }
  }
}
