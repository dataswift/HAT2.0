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

package org.hatdex.hat.api.controllers

import java.io.StringReader
import java.util.UUID

import com.atlassian.jwt.core.keys.KeyUtils
import com.mohiva.play.silhouette.api.services.DynamicEnvironmentProviderService
import com.mohiva.play.silhouette.api.{ Environment, LoginInfo, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.test._
import org.hatdex.hat.authentication.HatFrontendAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SlickPostgresDriver.backend.Database
import org.hatdex.hat.phata.controllers.Phata
import org.hatdex.hat.resourceManagement.{ HatServer, HatServerProvider }
import org.specs2.specification.Scope
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{ Request, Result }
import play.api.test.{ FakeRequest, PlaySpecification }
import play.api.{ Application, Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PhataSpec extends PlaySpecification {

  val logger = Logger(this.getClass)

  "The `launcher` method" should {
    "return status 401 if authenticator but no identity was found" in new Context {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(LoginInfo("xing", "comedian@watchmen.com"))

      val controller = application.injector.instanceOf[Phata]
      val result: Future[Result] = controller.launcher().apply(request)

      status(result) must equalTo(UNAUTHORIZED)
    }

    "return OK if authenticator for matching identity" in new Context {
      val request = FakeRequest("GET", "http://hat.hubofallthings.net")
        .withAuthenticator(owner.loginInfo)

      logger.info(s"Authenticator ${request.headers}")

      val controller = application.injector.instanceOf[Phata]
      val result: Future[Result] = controller.launcher().apply(request)

      status(result) must equalTo(OK)
    }
  }

}

object FakeHatConfiguration {
  def config = Map(
    ("hat") -> Map(
      ("hat.hubofallthings.net") -> Map(
        "ownerEmail" -> "hat@hat.org",
        ("database") -> Map(
          "dataSourceClass" -> "org.postgresql.ds.PGSimpleDataSource",
          "properties" -> Map(
            "url" -> ("jdbc:h2:mem:play-test-" + scala.util.Random.nextInt.toString),
            "driver" -> "slick.driver.H2Driver$",
            "databaseName" -> "testhatdb1",
            "user" -> "testhatdb1",
            "password" -> "testing"
          )
        ),
        ("publicKey") -> """-----BEGIN PUBLIC KEY-----
                           |MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAznT9VIjovMEB/hoZ9j+j
                           |z9G+WWAsfj9IB7mAMQEICoLMWHC1ZnO4nrqTrRiQFKKrWekjhXFRp8jQZmGhv/sw
                           |h5EsIcbRUzNNPSBmiCM0NXHG8wwN8cFigHXLQB0p4ekOWOHpEXfIZkTN5VlpUq1o
                           |PdbgMXRW8NdU+Mwr7qQiUnHxaW0imFiahPs3n5Q3KLt2nWcxlvazeaRDphkEtFTk
                           |JCaFx9TPzd1NSYpBidSMC2cwhVM6utaNk3ZRktCs+y0iFezL606x28P6+VuDkb19
                           |6OxWEvSSxL3+1KQbKi3B9Hg/BNhigGsqQ35GzVPVygT7m90u9nNlxJ7KvfQDQc8t
                           |dQIDAQAB
                           |-----END PUBLIC KEY-----""".stripMargin,
        ("privateKey") -> """-----BEGIN RSA PRIVATE KEY-----
                            |MIIEowIBAAKCAQEAznT9VIjovMEB/hoZ9j+jz9G+WWAsfj9IB7mAMQEICoLMWHC1
                            |ZnO4nrqTrRiQFKKrWekjhXFRp8jQZmGhv/swh5EsIcbRUzNNPSBmiCM0NXHG8wwN
                            |8cFigHXLQB0p4ekOWOHpEXfIZkTN5VlpUq1oPdbgMXRW8NdU+Mwr7qQiUnHxaW0i
                            |mFiahPs3n5Q3KLt2nWcxlvazeaRDphkEtFTkJCaFx9TPzd1NSYpBidSMC2cwhVM6
                            |utaNk3ZRktCs+y0iFezL606x28P6+VuDkb196OxWEvSSxL3+1KQbKi3B9Hg/BNhi
                            |gGsqQ35GzVPVygT7m90u9nNlxJ7KvfQDQc8tdQIDAQABAoIBAF00Voub1UolbjNb
                            |cD4Jw/fVrjPmJaAHDIskNRmqaAlqvDrvAw3SD1ZlT7b04FLYjzfjdvxOyLjRATg/
                            |OkkT6vhA0yYafjSr8+I1JuSt0+uOxmzCE+eA0OnCg/QZVmedEbORpWkT5P46cKNq
                            |RpCjJWzJfWQGLBvFcqBxeCHfqnkCFESRDG+YTUTzQ3Z4tdvXh/Wn7ZNAsJFaM2+x
                            |krJF7bBas9MJ/A8fumtuickr6DpFB6/nQsKqou3wDsMPN9SeTgXAzvufnssK0bGx
                            |8Z0F7pQUsl7CF2VuSXH2rcmW59JOpqPeZQ1JfrJZRxZ839vY+0BUF+Ti3FVJBb95
                            |aXLqHF8CgYEA+vwJCI6y+W/Cfwu79ssoJB+038sJftqkpKcFCipTsvX26h8o5+Vd
                            |BSvo58cjbXSV6a7PevkQvlgpKPki9SZnE+LoEmq1KbmN6yV0kev4Kzmi7P9Lz1Z8
                            |XRkt5KWQSMn65ZhLRHeomM71TgzDye1QI6rIKp4oumZUrlj8xGPB7VMCgYEA0pUq
                            |DSprxCQajw5WiH9X2sswrzDuK/+YAPZFBcRkK2KS9KGkltqlU9EmfZSX794vqfZw
                            |WBzJMRvxy0tF9QYSFahGivk98dzUUfARx79lIrKDBRVeUuP5LQ762K7BhDanym5a
                            |4YvzRPsJGHUT6Kyn1nsoP/CXqr1fxbv/HaN7WRcCgYEAz+x+O1WklZptobyB4kmZ
                            |npuZx5C39ByEK1emiC5amrbD8F8SD1LnhgJDd8h05Beini5Q+opdwaLdrnD+8eL3
                            |n/Tp12AJZ2CuXrDv6nd3Z6/e9sHk9waqDqJub65tYq/Zp91L9ZO/26AQfrF6fc2Z
                            |B4NTQmM2UH24B5v3A2e1X7sCgYBXnFuMcrO3PNYX4n05+NESZCrzGEZe483XyJ3a
                            |0mRicHZ3dLDHWlwiTQfYg3PbBfOKoM8IuaEy309vpveKA2aOwB3pP9z3vUpQdLLR
                            |Cd4H24ELImLF1bcbefn/IGW+ngac/+CrqdAiSNb15+/Kg9qoL0EFqRFQpc0stRRk
                            |vllZLQKBgEuos9IFTnXvF5+NpwQJ54t4YQW/StgPL7sPVA86irXnuT3VwdVNg2VF
                            |AZa/LU3jAXt2iTziR0LTKueamj/V+YM4qVyc/LhUPvjKlsCjyLBb647p3C/ogYbj
                            |mO9kGhALaD5okBcI/VuAQiFvBXdK0ii/nVcBApXEu47PG4oYUgPI
                            |-----END RSA PRIVATE KEY-----""".stripMargin
      )
    )
  )
}

class FakeHatServerProvider(fakeHatServer: HatServer) extends HatServerProvider {
  def retrieve[B](request: Request[B]): Future[Option[HatServer]] = {
    Future.successful(Some(fakeHatServer))
  }
  def retrieve(hatAddress: String): Future[Option[HatServer]] = {
    Future.successful(Some(fakeHatServer))
  }
}

trait Context extends Scope {
  val hatAddress = "hat.hubofallthings.net"
  val hatUrl = s"http://$hatAddress"
  private val keyUtils = new KeyUtils()
  private val configuration = Configuration.from(FakeHatConfiguration.config)
  private val hatConfig = configuration.getConfig(s"hat.$hatAddress").get

  private def hatDatabase: Database = Database.forURL(hatConfig.getString("database.properties.url").get)

  implicit val hatServer: HatServer = HatServer(hatAddress, "hat", "user@hat.org",
    keyUtils.readRsaPrivateKeyFromPem(new StringReader(hatConfig.getString("privateKey").get)),
    keyUtils.readRsaPublicKeyFromPem(new StringReader(hatConfig.getString("publicKey").get)), hatDatabase)

  val owner = HatUser(UUID.randomUUID(), "hatuser", Some("pa55w0rd"), "hatuser", "owner", true)
  implicit val env: Environment[HatFrontendAuthEnvironment] = FakeEnvironment[HatFrontendAuthEnvironment](Seq(owner.loginInfo -> owner), hatServer)

  val application: Application = new GuiceApplicationBuilder()
    .configure(FakeHatConfiguration.config)
    .overrides(bind[HatServerProvider].toInstance(new FakeHatServerProvider(hatServer)))
    .overrides(bind[DynamicEnvironmentProviderService[HatServer]].toInstance(new FakeHatServerProvider(hatServer)))
    .overrides(bind[Environment[HatFrontendAuthEnvironment]].toInstance(env))
    .build
}