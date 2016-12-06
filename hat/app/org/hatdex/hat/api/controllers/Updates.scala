package org.hatdex.hat.api.controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.util.{ Clock, Credentials, PasswordHasherRegistry }
import com.mohiva.play.silhouette.api.{ LoginEvent, Silhouette }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models.{ AccessToken, ErrorMessage, SuccessResponse, User }
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.authentication.models.{ HatUser, _ }
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.resourceManagement._
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{ JsObject, Json }
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.Future

class Updates @Inject() (
    val messagesApi: MessagesApi,
    configuration: Configuration,
    silhouette: Silhouette[HatApiAuthEnvironment],
    hatServerProvider: HatServerProvider,
    clock: Clock,
    hatDatabaseProvider: HatDatabaseProvider) extends HatApiController(silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  val logger = Logger("org.hatdex.hat.api.controllers.Users")

  def update(): Action[AnyContent] = SecuredAction.async { implicit request =>
    hatDatabaseProvider.update(request.dynamicEnvironment.db) map {
      case _ =>
        Ok(Json.toJson(SuccessResponse("Database updated")))
    }
  }

}
