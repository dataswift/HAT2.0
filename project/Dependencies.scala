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

import sbt._

object Dependencies {

  val resolvers = Seq(
    "Atlassian" at "https://maven.atlassian.com/public/",
    "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
  )

  object DsLib {
    private object Version {
      val DsAdjudicator         = "0.2.0"
      val DsBackend             = "2.5.0"
      val DsDexClient           = "3.3.1"
      val DsSilhouette          = "5.3.0"
      val DsSlickPostgresDriver = "0.1.2"
    }

    val Adjudicator              = "io.dataswift" %% "adjudicatorlib"                            % Version.DsAdjudicator
    val DexClient                = "org.hatdex"   %% "dex-client-scala"                          % Version.DsDexClient
    val IntegrationTestCommon    = "io.dataswift" %% "integration-test-common"                   % Version.DsBackend
    val PlayCommon               = "io.dataswift" %% "play-common"                               % Version.DsBackend
    val RedisCache               = "io.dataswift" %% "redis-cache"                               % Version.DsBackend
    val SilhouetteCryptoJca      = "com.mohiva"   %% "dataswift-play-silhouette-crypto-jca"      % Version.DsSilhouette
    val SilhouettePasswordBcrypt = "com.mohiva"   %% "dataswift-play-silhouette-password-bcrypt" % Version.DsSilhouette
    val SilhouettePersistence    = "com.mohiva"   %% "dataswift-play-silhouette-persistence"     % Version.DsSilhouette
    val SilhouetteTestkit        = "com.mohiva"   %% "dataswift-play-silhouette-testkit"         % Version.DsSilhouette
    val SlickPostgresDriver      = "org.hatdex"   %% "slick-postgres-driver"                     % Version.DsSlickPostgresDriver
  }

  private object Version {
    val AlpakkaAwsLambda     = "3.0.1"
    val CirceConfig          = "0.8.0"
    val PrettyTime           = "5.0.0.Final"
    val ScalaTestplusMockito = "3.2.9.0"
  }

  object LocalThirdParty {
    val AlpakkaAwsLambda     = "com.lightbend.akka"    %% "akka-stream-alpakka-awslambda" % Version.AlpakkaAwsLambda
    val CirceConfig          = "io.circe"              %% "circe-config"                  % Version.CirceConfig
    val PrettyTime           = "org.ocpsoft.prettytime" % "prettytime"                    % Version.PrettyTime
    val ScalaTestplusMockito = "org.scalatestplus"     %% "mockito-3-4"                   % Version.ScalaTestplusMockito
  }

}
