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

import java.util.UUID
import javax.inject.Inject

import akka.actor.ActorSystem
import com.digitaltangible.playguard.{ RateLimitActionFilter, RateLimiter, clientIp }
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.api.{ Environment, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import net.ceedubs.ficus.Ficus._
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models.{ ErrorMessage, User }
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.SlickPostgresDriver.api.Database
import org.hatdex.hat.resourceManagement._
import org.joda.time.DateTime
import play.api.{ Configuration, Logger, Play }
import play.api.http.{ DefaultHttpErrorHandler, HttpErrorHandler, LazyHttpErrorHandler, Status }
import play.api.i18n.I18nSupport
import play.api.libs.json.{ JsError, Json, Reads }
import play.api.mvc.BodyParsers.parse
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

abstract class HatController[T <: HatAuthEnvironment](
    silhouette: Silhouette[T],
    clock: Clock,
    hatServerProvider: HatServerProvider,
    configuration: Configuration) extends Controller with I18nSupport {

  def env: Environment[T] = silhouette.env
  def SecuredAction = silhouette.securedAction(env)
  def UnsecuredAction = silhouette.unsecuredAction(env)
  def UserAwareAction = silhouette.userAwareAction(env)

  implicit def securedRequest2User[A](implicit request: SecuredRequest[T, A]): HatUser = request.identity
  implicit def userAwareRequest2UserOpt[A](implicit request: UserAwareRequest[T, A]): Option[HatUser] = request.identity
  implicit def securedRequest2HatServer[A](implicit request: SecuredRequest[T, A]): HatServer = request.dynamicEnvironment
  implicit def userAwareRequest2HatServer[A](implicit request: UserAwareRequest[T, A]): HatServer = request.dynamicEnvironment
  implicit def securedRequest2Authenticator[A](implicit request: SecuredRequest[T, A]): T#A = request.authenticator
  implicit def hatServer2db(implicit hatServer: HatServer): Database = hatServer.db
  implicit def identity2ApiUser(implicit identity: T#I): User = ModelTranslation.fromInternalModel(identity)
}

abstract class HatApiController(
  silhouette: Silhouette[HatApiAuthEnvironment],
  clock: Clock,
  hatServerProvider: HatServerProvider,
  configuration: Configuration) extends HatController[HatApiAuthEnvironment](silhouette, clock, hatServerProvider, configuration)

abstract class HatFrontendController(
    silhouette: Silhouette[HatFrontendAuthEnvironment],
    clock: Clock,
    hatServerProvider: HatServerProvider,
    configuration: Configuration) extends HatController[HatFrontendAuthEnvironment](silhouette, clock, hatServerProvider, configuration) {

  def authenticatorWithRememberMe(authenticator: CookieAuthenticator, rememberMe: Boolean): CookieAuthenticator = {
    if (rememberMe) {
      val expirationTime: DateTime = clock.now + rememberMeParams._1
      authenticator.copy(
        expirationDateTime = expirationTime,
        idleTimeout = rememberMeParams._2,
        cookieMaxAge = rememberMeParams._3)
    }
    else {
      authenticator
    }
  }

  private lazy val rememberMeParams: (FiniteDuration, Option[FiniteDuration], Option[FiniteDuration]) = {
    val cfg = configuration.getConfig("silhouette.authenticator.rememberMe").get.underlying
    (
      cfg.as[FiniteDuration]("authenticatorExpiry"),
      cfg.getAs[FiniteDuration]("authenticatorIdleTimeout"),
      cfg.getAs[FiniteDuration]("cookieMaxAge"))
  }
}

/**
 * A Limiter for user logic.
 */
class UserLimiter @Inject() (implicit val actorSystem: ActorSystem, implicit val configuration: Configuration) {
  import scala.language.higherKinds

  /**
   * A Rate limiter Function for.
   *
   * @param rateLimiter The rate limiter implementation.
   * @param reject The function to apply on reject.
   * @param requestKeyExtractor The Request Parameter we want to filter from.
   * @param actorSystem The implicit Akka Actor system.
   * @tparam K the key by which to identify the user.
   */
  def createUserAware[T <: HatAuthEnvironment, R[_] <: UserAwareRequest[T, _], K](
    rateLimiter: RateLimiter)(reject: R[_] => Result, requestKeyExtractor: R[_] => K)(
    implicit
    actorSystem: ActorSystem): RateLimitActionFilter[R] with ActionFunction[R, R] = {
    new RateLimitActionFilter[R](rateLimiter)(reject, requestKeyExtractor) with ActionFunction[R, R]
  }

  def createSecured[T <: HatAuthEnvironment, R[_] <: SecuredRequest[T, _], K](
    rateLimiter: RateLimiter)(reject: R[_] => Result, requestKeyExtractor: R[_] => K)(
    implicit
    actorSystem: ActorSystem): RateLimitActionFilter[R] with ActionFunction[R, R] = {
    new RateLimitActionFilter[R](rateLimiter)(reject, requestKeyExtractor) with ActionFunction[R, R]
  }

  type Secured[B] = SecuredRequest[HatApiAuthEnvironment, B]
  type UserAware[B] = UserAwareRequest[HatApiAuthEnvironment, B]

  // allow 2 failures immediately and get a new token every 10 seconds
  private val rl = new RateLimiter(5, 1f / 5, "test failure rate limit")

  import HatJsonFormats.errorMessage
  private def response(r: RequestHeader) = Results.BadRequest(
    Json.toJson(ErrorMessage("Request rate exceeded", "Rate of requests from your IP exceeded")))

  def UserAwareRateLimit: RateLimitActionFilter[UserAware] with ActionFunction[UserAware, UserAware] =
    createUserAware[HatApiAuthEnvironment, UserAware, String](rl)(response, r => r.remoteAddress)

  def SecureRateLimit: RateLimitActionFilter[Secured] with ActionFunction[Secured, Secured] =
    createSecured[HatApiAuthEnvironment, Secured, String](rl)(response, r => r.remoteAddress)

}

///**
// * A Limiter for user logic.
// */
//object UserLimiter2 {
//
//  /**
//   * A Rate limiter Function for.
//   *
//   * @param rateLimiter The rate limiter implementation.
//   * @param reject The function to apply on reject.
//   * @param requestKeyExtractor The Request Parameter we want to filter from.
//   * @param actorSystem The implicit Akka Actor system.
//   * @tparam K the key by which to identify the user.
//   */
//  def apply[T <: HatAuthEnvironment, R[_] <: SecuredRequest[T, _], K](rateLimiter: RateLimiter)(
//    reject: R[_] => Result, requestKeyExtractor: R[_] => K
//  )(
//    implicit
//    actorSystem: ActorSystem
//  ): RateLimitActionFilter[R] with ActionFunction[R, R] = {
//    new RateLimitActionFilter[R](rateLimiter)(reject, requestKeyExtractor) with ActionFunction[R, R]
//  }
//
//  type Secured[B] = SecuredRequest[HatAuthEnvironment, B]
//
//  /**
//     * A default user filter implementation.
//     *
//     * @param ac The Akka Actor System implicitly provided.
//     */
//  def defaultUserFilter(implicit ac: ActorSystem): RateLimitActionFilter[Secured] with ActionFunction[Secured, Secured] = {
//    (UserLimiter.apply[HatAuthEnvironment, Secured, UUID](new RateLimiter(10, 1f / 10, "Default User Limiter"))
//      (_ => Results.TooManyRequests("You've been refreshing too much. Please try again in 10 seconds"), r => r.identity.userId))
//  }
//}