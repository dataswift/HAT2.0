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

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.api.{ Environment, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import net.ceedubs.ficus.Ficus._
import org.hatdex.hat.api.models.User
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.SlickPostgresDriver.api.Database
import org.hatdex.hat.resourceManagement._
import org.joda.time.DateTime
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.Controller

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