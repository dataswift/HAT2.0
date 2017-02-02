package org.hatdex.hat.api.controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.Clock
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models.SuccessResponse
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.resourceManagement._
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{ Configuration, Logger }

class Updates @Inject() (
    val messagesApi: MessagesApi,
    configuration: Configuration,
    silhouette: Silhouette[HatApiAuthEnvironment],
    hatServerProvider: HatServerProvider,
    clock: Clock,
    hatDatabaseProvider: HatDatabaseProvider) extends HatApiController(silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  val logger = Logger("org.hatdex.hat.api.controllers.Users")

  configuration.getStringSeq("databaseServers.serverUrls") map { testlist =>
    logger.warn(s"Got testlist")
    testlist.foreach { item =>
      logger.warn(s"Item $item")
    }
  }

  def update(): Action[AnyContent] = SecuredAction.async { implicit request =>
    hatDatabaseProvider.update(request.dynamicEnvironment.db) map {
      case _ =>
        Ok(Json.toJson(SuccessResponse("Database updated")))
    }
  }

}
