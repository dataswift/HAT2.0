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
 * 11 / 2017
 */

package org.hatdex.hat.she.controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.util.Clock
import org.hatdex.hat.api.json.RichDataJsonFormats
import org.hatdex.hat.api.models._
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.resourceManagement._
import org.hatdex.hat.she.models.{ FunctionConfiguration, FunctionConfigurationJsonProtocol }
import org.hatdex.hat.she.service.{ FunctionExecutionDispatcher, FunctionService }
import org.hatdex.hat.utils.HatBodyParsers
import play.api.libs.json._
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

class FunctionManager @Inject() (
    components: ControllerComponents,
    configuration: Configuration,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    clock: Clock,
    hatServerProvider: HatServerProvider,
    functionService: FunctionService,
    functionExecutionDispatcher: FunctionExecutionDispatcher)(
    implicit
    val ec: ExecutionContext)
  extends HatApiController(components, silhouette, clock, hatServerProvider, configuration)
  with RichDataJsonFormats
  with FunctionConfigurationJsonProtocol {

  private val logger = Logger(this.getClass)

  def functionList(): Action[AnyContent] = SecuredAction(WithRole(Owner(), Platform())).async { implicit request =>
    logger.debug("Listing functions")
    functionService.all(active = false).map { functions =>
      Ok(Json.toJson(functions))
    }
  }

  def functionGet(function: String): Action[AnyContent] = SecuredAction(WithRole(Owner(), Platform())).async { implicit request =>
    logger.debug(s"Get function $function")
    functionService.get(name = function).map { maybeFunction =>
      maybeFunction.map { function =>
        Ok(Json.toJson(function))
      } getOrElse {
        NotFound(Json.toJson(ErrorMessage("Function Not Found", s"Function $function not found")))
      }
    }
  }

  def functionEnable(function: String): Action[AnyContent] = SecuredAction(WithRole(Owner())).async { implicit request =>
    functionSetEnabled(function, enabled = true)
  }

  def functionDisable(function: String): Action[AnyContent] = SecuredAction(WithRole(Owner())).async { implicit request =>
    functionSetEnabled(function, enabled = false)
  }

  protected def functionSetEnabled(function: String, enabled: Boolean)(implicit request: SecuredRequest[HatApiAuthEnvironment, AnyContent]): Future[Result] = {
    logger.debug(s"Enable function $function = $enabled")
    functionService.get(function).flatMap { maybeFunction =>
      maybeFunction.map { function =>
        functionService.save(function.copy(enabled = enabled))
          .map(f => Ok(Json.toJson(f)))
      } getOrElse {
        Future.successful(NotFound(Json.toJson(ErrorMessage("Function Not Found", s"Function $function not found"))))
      }
    }
  }

  def functionTrigger(function: String): Action[AnyContent] = SecuredAction(WithRole(Owner(), Platform())).async { implicit request =>
    logger.debug(s"Trigger function $function")
    functionService.get(function).flatMap { maybeFunction =>
      maybeFunction.map {
        case c: FunctionConfiguration if c.available && c.enabled =>
          functionExecutionDispatcher.trigger(request.dynamicEnvironment.domain, c)(60.seconds, ec)
            .map(_ => Ok(Json.toJson(SuccessResponse("Function Executed"))))
            .recover {
              case e =>
                logger.error(s"Function $function execution errored with ${e.getMessage}", e)
                InternalServerError(Json.toJson(ErrorMessage("Function Execution Failed", s"Function $function execution errored with ${e.getMessage}")))
            }
        case FunctionConfiguration(_, _, _, false, _, _, _) =>
          Future.successful(BadRequest(Json.toJson(ErrorMessage("Function Not Available", s"Function $function not available for execution"))))
        case FunctionConfiguration(_, _, _, true, false, _, _) =>
          Future.successful(BadRequest(Json.toJson(ErrorMessage("Function Not Enabled", s"Function $function not enabled for execution"))))
      } getOrElse {
        Future.successful(NotFound(Json.toJson(ErrorMessage("Function Not Found", s"Function $function not found"))))
      }
    }
  }
}
