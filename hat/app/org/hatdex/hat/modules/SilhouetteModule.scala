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

package org.hatdex.hat.modules

import com.google.inject.name.Named
import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.{ SecuredErrorHandler, UnsecuredErrorHandler }
import com.mohiva.play.silhouette.api.crypto.{ CookieSigner, Crypter, _ }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.crypto.{ JcaCookieSigner, JcaCookieSignerSettings, JcaCrypter, JcaCrypterSettings }
import com.mohiva.play.silhouette.impl.authenticators.{ JWTRS256Authenticator, _ }
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader._
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.authentication._
import org.hatdex.hat.phata.models.MailTokenUser
import org.hatdex.hat.phata.service.{ MailTokenService, MailTokenUserService }
import org.hatdex.hat.resourceManagement.{ HatServer, HatServerProvider, HatServerProviderImpl }
import org.hatdex.hat.utils.{ ErrorHandler, HatMailer, HatMailerImpl }
import play.api.Configuration
import play.api.http.HttpErrorHandler
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WSClient

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure() {
    bind[HatServerProvider].to[HatServerProviderImpl]
    bind[DynamicEnvironmentProviderService[HatServer]].to[HatServerProvider]

    bind[Silhouette[HatApiAuthEnvironment]].to[SilhouetteProvider[HatApiAuthEnvironment]]
    bind[Silhouette[HatFrontendAuthEnvironment]].to[SilhouetteProvider[HatFrontendAuthEnvironment]]
    bind[MailTokenService[MailTokenUser]].to[MailTokenUserService]
    bind[HatMailer].to[HatMailerImpl]

    bind[HttpErrorHandler].to[ErrorHandler]
    bind[SecuredErrorHandler].to[ErrorHandler]
    bind[UnsecuredErrorHandler].to[ErrorHandler]
    bind[CacheLayer].to[PlayCacheLayer]

    bind[AuthUserService].to[AuthUserServiceImpl]
    bind[DelegableAuthInfoDAO[PasswordInfo, HatServer]].to[PasswordInfoService]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher(logRounds = 14))
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))

    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
  }

  /**
   * Provides the HTTP layer implementation.
   *
   * @param client Play's WS client.
   * @return The HTTP layer implementation.
   */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
   * Provides the Silhouette environment.
   *
   * @param userService The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus The event bus instance.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
    userService: AuthUserService,
    authenticatorService: AuthenticatorService[JWTRS256Authenticator, HatServer],
    dynamicEnvironmentProviderService: DynamicEnvironmentProviderService[HatServer],
    eventBus: EventBus): Environment[HatApiAuthEnvironment] = {

    Environment[HatApiAuthEnvironment](
      userService,
      authenticatorService,
      Seq(),
      dynamicEnvironmentProviderService,
      eventBus)
  }

  @Provides
  def provideCookieAuthEnvironment(
    userService: AuthUserService,
    authenticatorService: AuthenticatorService[CookieAuthenticator, HatServer],
    dynamicEnvironmentProviderService: DynamicEnvironmentProviderService[HatServer],
    eventBus: EventBus): Environment[HatFrontendAuthEnvironment] = {

    Environment[HatFrontendAuthEnvironment](
      userService,
      authenticatorService,
      Seq(),
      dynamicEnvironmentProviderService,
      eventBus)
  }

  /**
   * Provides the auth info repository.
   *
   * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
   * @return The auth info repository instance.
   */
  @Provides
  def provideAuthInfoRepository(passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo, HatServer]): AuthInfoRepository[HatServer] = {
    new DelegableAuthInfoRepository[HatServer](passwordInfoDAO)
  }

  /**
   * Provides the cookie signer for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The cookie signer for the authenticator.
   */
  @Provides @Named("authenticator-cookie-signer")
  def provideAuthenticatorCookieSigner(configuration: Configuration): CookieSigner = {
    val config = configuration.underlying.as[JcaCookieSignerSettings]("silhouette.authenticator.cookie.signer")
    new JcaCookieSigner(config)
  }

  /**
   * Provides the crypter for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The crypter for the authenticator.
   */
  @Provides @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  /**
   * Provides the authenticator service.
   *
   * @param cookieSigner The cookie signer implementation.
   * @param crypter The crypter implementation.
   * @param fingerprintGenerator The fingerprint generator implementation.
   * @param idGenerator The ID generator implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
    //    @Named("authenticator-cookie-signer") cookieSigner: CookieSigner,
    //    @Named("authenticator-crypter") crypter: Crypter,
    fingerprintGenerator: FingerprintGenerator,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock): AuthenticatorService[JWTRS256Authenticator, HatServer] = {

    val config = configuration.underlying.as[JWTRS256AuthenticatorSettings]("silhouette.authenticator")
    val encoder = new Base64AuthenticatorEncoder()

    new JWTRS256AuthenticatorService[HatServer](config, None, encoder, idGenerator, clock)
  }

  @Provides
  def provideCookieAuthenticatorService(
    @Named("authenticator-cookie-signer") cookieSigner: CookieSigner,
    @Named("authenticator-crypter") crypter: Crypter,
    fingerprintGenerator: FingerprintGenerator,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock): AuthenticatorService[CookieAuthenticator, HatServer] = {

    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val encoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService[HatServer](config, None, cookieSigner, encoder, fingerprintGenerator, idGenerator, clock)
  }

  /**
   * Provides the password hasher registry.
   *
   * @param passwordHasher The default password hasher implementation.
   * @return The password hasher registry.
   */
  @Provides
  def providePasswordHasherRegistry(passwordHasher: PasswordHasher): PasswordHasherRegistry = {
    new PasswordHasherRegistry(passwordHasher)
  }

  /**
   * Provides the credentials provider.
   *
   * @param authInfoRepository The auth info repository implementation.
   * @param passwordHasherRegistry The password hasher registry.
   * @return The credentials provider.
   */
  @Provides
  def provideCredentialsProvider(
    authInfoRepository: AuthInfoRepository[HatServer],
    passwordHasherRegistry: PasswordHasherRegistry): CredentialsProvider[HatServer] = {
    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }
}