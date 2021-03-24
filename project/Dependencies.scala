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

  private object Version {
    val Play: String          = play.core.PlayVersion.current
    val PlayJson              = "2.9.2"
    val Silhouette            = "7.0.0"
    val AtlassianJwt          = "3.2.0"
    val AwsSdk                = "1.11.979"
    val AlpakkaAwsLambda      = "2.0.2"
    val CommonsLang3          = "3.11"
    val BouncyCastle          = "1.68"
    val PlayPrometheusFilters = "0.6.1"
    val PlayGuard             = "2.5.0"
    val PrettyTime            = "5.0.0.Final"
    val Nbvcxz                = "1.5.0"

    val Adjudicator = "0.1.0-SNAPSHOT"
    val DsBackend   = "2.1.1"
    val DsTestTools = "0.2.3"
  }

  val resolvers = Seq(
    Resolver.jcenterRepo,
    "Atlassian Releases" at "https://maven.atlassian.com/public/",
    Resolver.bintrayRepo("scalaz", "releases"),
    "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-snapshots.hubofallthings.com",
    "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
  )

  object Library {

    object Play {
      val ws       = "com.typesafe.play" %% "play-ws"        % Version.Play
      val cache    = "com.typesafe.play" %% "play-cache"     % Version.Play
      val test     = "com.typesafe.play" %% "play-test"      % Version.Play % Test
      val jdbc     = "com.typesafe.play" %% "play-jdbc"      % Version.Play
      val json     = "com.typesafe.play" %% "play-json"      % Version.PlayJson
      val jsonJoda = "com.typesafe.play" %% "play-json-joda" % Version.PlayJson

      val htmlCompressor = "com.mohiva"          %% "play-html-compressor" % "0.6.3"
      val playGuard      = "com.digitaltangible" %% "play-guard"           % Version.PlayGuard

      object Jwt {
        val bouncyCastle     = "org.bouncycastle"  % "bcprov-jdk15on" % Version.BouncyCastle
        val bouncyCastlePkix = "org.bouncycastle"  % "bcpkix-jdk15on" % Version.BouncyCastle
        val atlassianJwtCore = "com.atlassian.jwt" % "jwt-core"       % Version.AtlassianJwt
      }

      object Silhouette {
        val passwordBcrypt    = "com.mohiva" %% "play-silhouette-password-bcrypt" % Version.Silhouette
        val persistence       = "com.mohiva" %% "play-silhouette-persistence"     % Version.Silhouette
        val cryptoJca         = "com.mohiva" %% "play-silhouette-crypto-jca"      % Version.Silhouette
        val silhouette        = "com.mohiva" %% "play-silhouette"                 % Version.Silhouette
        val silhouetteTestkit = "com.mohiva" %% "play-silhouette-testkit"         % Version.Silhouette % Test
      }
    }

    object Utils {
//      val pegdown       = "org.pegdown"            % "pegdown"          % "1.6.0"
      val awsJavaS3Sdk  = "com.amazonaws"          % "aws-java-sdk-s3"  % Version.AwsSdk
      val awsJavaSesSdk = "com.amazonaws"          % "aws-java-sdk-ses" % Version.AwsSdk
      val prettyTime    = "org.ocpsoft.prettytime" % "prettytime"       % Version.PrettyTime
      val nbvcxz        = "me.gosimple"            % "nbvcxz"           % Version.Nbvcxz

      // FIXME ? exclude ("net.spy", "spymemcached")
//      val playMemcached = "com.github.mumoshu" %% "play2-memcached-play28" % "0.11.0"

      val alpakkaAwsLambda = "com.lightbend.akka" %% "akka-stream-alpakka-awslambda" % Version.AlpakkaAwsLambda
      val apacheCommonLang = "org.apache.commons"  % "commons-lang3"                 % Version.CommonsLang3
    }

    object Backend {
      val logPlay = "io.dataswift" %% "log-play" % Version.DsBackend
      val hatPlay = "io.dataswift" %% "hat-play" % Version.DsBackend
      val dexPlay = "io.dataswift" %% "dex-play" % Version.DsBackend
    }

    object HATDeX {
      val hatClient = "org.hatdex" %% "hat-client-scala-play" % "3.1.2"
      val dexClient = "org.hatdex" %% "dex-client-scala"      % "3.1.2"
      val codegen   = "org.hatdex" %% "slick-postgres-driver" % "0.1.2"
    }

    val scalaGuice  = "net.codingwell"     %% "scala-guice"  % "4.2.11"
    val circeConfig = "io.circe"           %% "circe-config" % "0.8.0"
    val janino      = "org.codehaus.janino" % "janino"       % "3.1.3"

    object ContractLibrary {
      val adjudicator = "io.dataswift" %% "adjudicatorlib" % Version.Adjudicator
    }

    object Prometheus {
      val filters = "io.github.jyllands-posten" %% "play-prometheus-filters" % Version.PlayPrometheusFilters
    }

    object ScalaTest {
//      val scalaplaytest     = "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"   % Test
      val scalaplaytestmock = "org.scalatestplus" %% "mockito-3-4"  % "3.2.6.0" % Test
      val mockitoCore       = "org.mockito"        % "mockito-core" % "3.4.6"   % Test
    }

    object Dataswift {
      val testCommon            = "io.dataswift" %% "test-common"             % Version.DsTestTools % Test
      val integrationTestCommon = "io.dataswift" %% "integration-test-common" % Version.DsTestTools % Test
    }

    val overrides = Seq(
//      "com.typesafe.play" %% "play"                  % Play.version,
//      "com.typesafe.play" %% "play-server"           % Play.version,
//      "com.typesafe.play" %% "play-ahc-ws"           % Play.version,
//      "com.typesafe.play" %% "play-akka-http-server" % Play.version,
//      "com.typesafe.play" %% "filters-helpers"       % Play.version,
//      Library.Play.cache,
//      Library.Play.ws,
//      Library.Play.json,
//      Library.Play.jsonJoda
    )
  }
}
