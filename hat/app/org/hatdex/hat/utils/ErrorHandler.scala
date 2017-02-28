/*
 * Copyright (C) 2017 HAT Data Exchange Ltd
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>
 * 2 / 2017
 */

package org.hatdex.hat.utils

import java.sql.SQLTransientConnectionException
import javax.inject._

import com.mohiva.play.silhouette.api.actions.{ SecuredErrorHandler, UnsecuredErrorHandler }
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.resourceManagement.HatServerDiscoveryException
import play.api._
import play.api.http.{ ContentTypes, DefaultHttpErrorHandler }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router

import scala.concurrent.Future

class ErrorHandler @Inject() (
  env: Environment,
  config: Configuration,
  sourceMapper: OptionalSourceMapper,
  router: Provider[Router],
  hatMailer: HatMailer,
  val messagesApi: MessagesApi) extends DefaultHttpErrorHandler(env, config, sourceMapper, router)
    with SecuredErrorHandler with UnsecuredErrorHandler with I18nSupport with ContentTypes with RequestExtractors with Rendering {

  // 401 - Unauthorized
  override def onNotAuthenticated(implicit request: RequestHeader): Future[Result] = {
    Logger.warn("[Silhouette] Not authenticated")
    Future.successful {
      render {
        case Accepts.Json() => Unauthorized(Json.obj("error" -> "Not Authenticated", "message" -> s"Not Authenticated"))
        case _              => Redirect(org.hatdex.hat.phata.controllers.routes.Phata.rumpelIndex())
      }
    }
  }

  // 403 - Forbidden
  override def onNotAuthorized(implicit request: RequestHeader): Future[Result] = {
    Logger.warn("[Silhouette] Not authorized")
    Future.successful {
      render {
        case Accepts.Json() => Forbidden(Json.obj("error" -> "Forbidden", "message" -> s"Access Denied"))
        case _              => Redirect(org.hatdex.hat.phata.controllers.routes.Phata.rumpelIndex())
      }
    }
  }

  // 404 - page not found error
  override def onNotFound(request: RequestHeader, message: String): Future[Result] = Future.successful {
    implicit val _request = request
    implicit val noUser: Option[HatUser] = None
    render {
      case Accepts.Json() =>
        NotFound(Json.obj(
          "error" -> "Handler Not Found",
          "message" -> s"Request handler at ${request.method}:${request.path} does not exist"))
      case _ =>
        NotFound(env.mode match {
          case Mode.Prod => views.html.defaultpages.notFound(request.method, request.uri)
          case _         => views.html.defaultpages.devNotFound(request.method, request.uri, Some(router.get))
        })
    }
  }

  // 500 - internal server error
  override def onProdServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    implicit val _request = request
    implicit val noUser: Option[HatUser] = None
    hatMailer.serverErrorNotify(request, exception)
    Future.successful {
      render {
        case Accepts.Json() =>
          val message = exception match {
            case e: SQLTransientConnectionException =>
              "HAT unavailable"
            case e =>
              s"A server error occurred, please report this error code to our admins: ${e.getMessage}"
          }
          InternalServerError(Json.obj(
            "error" -> "Internal Server error",
            "message" -> message))
        case _ =>
          exception match {
            case e: HatServerDiscoveryException =>
              NotFound(org.hatdex.hat.phata.views.html.hatNotFound())
            case e =>
              InternalServerError(s"A server error occurred, please report this error code to our admins: ${e.getMessage}")
          }
      }
    }
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    implicit val _request = request
    implicit val noUser: Option[HatUser] = None

    Future.successful {
      render {
        case Accepts.Json() =>
          val message = exception match {
            case e: SQLTransientConnectionException =>
              "HAT unavailable"
            case e =>
              s"A server error occurred, please report this error code to our admins: ${e.getMessage}"
          }
          InternalServerError(Json.obj(
            "error" -> "Internal Server error",
            "message" -> message))
        case _ =>
          exception match {
            case e: HatServerDiscoveryException =>
              NotFound(org.hatdex.hat.phata.views.html.hatNotFound())
            case e =>
              InternalServerError(s"A server error occurred, please report this error code to our admins: ${e.getMessage}")
          }
      }
    }
  }

  override def onBadRequest(request: RequestHeader, message: String): Future[Result] = {
    implicit val _request = request
    implicit val noUser: Option[HatUser] = None
    Future.successful {
      render {
        case Accepts.Json() => BadRequest(Json.obj("error" -> "Bad Request", "message" -> message))
        case _              => InternalServerError(views.html.defaultpages.badRequest(request.method, request.uri, message))
      }
    }
  }
}