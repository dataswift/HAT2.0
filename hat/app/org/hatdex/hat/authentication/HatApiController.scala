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

package org.hatdex.hat.authentication

import com.digitaltangible.playguard.{ RateLimitActionFilter, RateLimiter }
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.{ Environment, Silhouette }
import io.dataswift.models.hat.ErrorMessage
import io.dataswift.models.hat.applications.HatApplication
import io.dataswift.models.hat.json.HatJsonFormats
import org.hatdex.hat.api.service.applications.ApplicationsService
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.resourceManagement._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.{ Format, Json }
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class ServerSecuredRequest[T <: HatAuthEnvironment, A](
    val identity: HatUser,
    val server: HatServer,
    val wrapped: SecuredRequest[T, A])
    extends WrappedRequest[A](wrapped)

class ServerUserAwareRequest[T <: HatAuthEnvironment, A](
    val identity: HatUser,
    val server: HatServer,
    val wrapped: UserAwareRequest[T, A])
    extends WrappedRequest[A](wrapped)

abstract class HatController[T <: HatAuthEnvironment](
    components: ControllerComponents,
    silhouette: Silhouette[T])
    extends AbstractController(components)
    with I18nSupport {

  def env: Environment[T] = silhouette.env

  def SecuredAction: SecuredActionBuilder[T, AnyContent] =
    silhouette.securedAction(env)

  def UnsecuredAction: UnsecuredActionBuilder[T, AnyContent] =
    silhouette.unsecuredAction(env)

  def UserAwareAction(implicit ec: ExecutionContext): ActionBuilder[EnvironmentalServerUserAwareRequest, AnyContent] =
    silhouette.userAwareAction(env).andThen(UserAwareServerAction)

  type EnvironmentalSecuredRequest[A]         = SecuredRequest[T, A]
  type EnvironmentalUserAwareRequest[A]       = UserAwareRequest[T, A]
  type EnvironmentalServerSecuredRequest[A]   = ServerSecuredRequest[T, A]
  type EnvironmentalServerUserAwareRequest[A] = ServerUserAwareRequest[T, A]

  def SecuredServerAction[A](
      implicit ec: ExecutionContext): ActionRefiner[EnvironmentalSecuredRequest, EnvironmentalServerSecuredRequest] =
    new ActionRefiner[EnvironmentalSecuredRequest, EnvironmentalServerSecuredRequest] {
      def executionContext: ExecutionContext = ec

      override protected def refine[B](
          input: SecuredRequest[T, B]): Future[Either[Result, EnvironmentalServerSecuredRequest[B]]] =
        ???
    }

  def UserAwareServerAction[A](
      implicit ec: ExecutionContext)
      : ActionRefiner[EnvironmentalUserAwareRequest, EnvironmentalServerUserAwareRequest] =
    new ActionRefiner[EnvironmentalUserAwareRequest, EnvironmentalServerUserAwareRequest] {
      def executionContext: ExecutionContext = ec

      override protected def refine[B](
          input: UserAwareRequest[T, B]): Future[Either[Result, EnvironmentalServerUserAwareRequest[B]]] =
        ???
    }

  def securedRequest2ApplicationStatus(
      request: ServerSecuredRequest[T, _]
    )(implicit applicationsService: ApplicationsService): Future[Option[HatApplication]] =
    request.wrapped.authenticator.customClaims.flatMap { customClaims =>
      (customClaims \ "application").asOpt[String]
    } map { app =>
      applicationsService.applicationStatus(app)(
        request.dynamicEnvironment,
        request.identity,
        request
      )
    } getOrElse {
      Future.successful(None)
    }

  def userAwareRequest2ApplicationStatus[A](
      request: ServerUserAwareRequest[T, A]
    )(implicit applicationsService: ApplicationsService): Future[Option[HatApplication]] =
    (request.authenticator, request.identity) match {
      case (Some(authenticator), Some(identity)) =>
        authenticator.customClaims.flatMap { customClaims =>
          (customClaims \ "application").asOpt[String]
        } map { app =>
          applicationsService.applicationStatus(app)(
            request.dynamicEnvironment,
            identity,
            request
          )
        } getOrElse {
          Future.successful(None)
        }
      case _ =>
        Future.successful(None) // if no authenticator, certainly no application
    }
}

abstract class HatApiController(
    components: ControllerComponents,
    silhouette: Silhouette[HatApiAuthEnvironment])
    extends HatController[HatApiAuthEnvironment](components, silhouette)

/**
  * A Limiter for user logic.
  */
class UserLimiter @Inject() (
    implicit
    configuration: Configuration,
    ec: ExecutionContext) {

  /**
    * A Rate limiter Function for.
    *
    * @param rateLimiter The rate limiter implementation.
    * @param reject The function to apply on reject.
    * @param requestKeyExtractor The Request Parameter we want to filter from.
    * @tparam K the key by which to identify the user.
    */
  def createUserAware[T <: HatAuthEnvironment, R[_] <: UserAwareRequest[T, _], K](
      rateLimiter: RateLimiter
    )(reject: R[_] => Result,
      requestKeyExtractor: R[_] => K): RateLimitActionFilter[R] with ActionFunction[R, R] =
    new RateLimitActionFilter[R](rateLimiter)(reject, requestKeyExtractor) with ActionFunction[R, R]

  def createSecured[T <: HatAuthEnvironment, R[_] <: SecuredRequest[T, _], K](
      rateLimiter: RateLimiter
    )(reject: R[_] => Result,
      requestKeyExtractor: R[_] => K): RateLimitActionFilter[R] with ActionFunction[R, R] =
    new RateLimitActionFilter[R](rateLimiter)(reject, requestKeyExtractor) with ActionFunction[R, R]

  type Secured[B]   = SecuredRequest[HatApiAuthEnvironment, B]
  type UserAware[B] = ServerUserAwareRequest[HatApiAuthEnvironment, B]

  // allow 10 requests immediately and get a new token every 2 seconds
  private val rl = new RateLimiter(10, 1f / 2, "[Rate Limiter]")

  implicit private val errorMesageFormat: Format[ErrorMessage] =
    HatJsonFormats.errorMessage
  private def response(r: RequestHeader) =
    Results.BadRequest(
      Json.toJson(
        ErrorMessage(
          "Request rate exceeded",
          "Rate of requests from your IP exceeded",
          Some(Seq(s"Request to ${r.path}"))
        )
      )
    )

  def UserAwareRateLimit: RateLimitActionFilter[UserAware] with ActionFunction[UserAware, UserAware] =
    createUserAware[HatApiAuthEnvironment, UserAware, String](rl)(
      response,
      clientIp
    )

  def SecureRateLimit: RateLimitActionFilter[Secured] with ActionFunction[Secured, Secured] =
    createSecured[HatApiAuthEnvironment, Secured, String](rl)(
      response,
      clientIp
    )

  def clientIp(request: RequestHeader)(implicit conf: Configuration): String =
    (for {
      configuredHeader <- conf.get[Option[String]]("playguard.clientipheader")
      ip <- request.headers.get(configuredHeader)
    } yield ip) getOrElse {

      // Consider X-Forwarded-For as most accurate if it exists
      // Since it is easy to forge an X-Forwarded-For, only consider the last ip added by our proxy as the most accurate
      // https://en.wikipedia.org/wiki/X-Forwarded-For
      request.headers
        .get("X-Forwarded-For")
        .map(_.split(",").last.trim)
        .getOrElse {
          request.remoteAddress
        }

    }
}
