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

  object Versions {
    val crossScala   = Seq("2.12.11")
    val scalaVersion = crossScala.head
    val adjudicator  = "0.1.0-SNAPSHOT"
  }

  val resolvers = Seq(
    Resolver.jcenterRepo,
    "Atlassian Releases" at "https://maven.atlassian.com/public/",
    Resolver.bintrayRepo("scalaz", "releases"),
    Resolver.sonatypeRepo("snapshots"),
    "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-snapshots.hubofallthings.com",
    "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
  )

  object Library {

    object Version {
      val ScalaTest           = "3.2.3"
      val TestContainersScala = "0.38.8"
      val DsTestTools         = "0.2.3"
    }

    object Play {
      val version  = play.core.PlayVersion.current
      val ws       = "com.typesafe.play" %% "play-ws"        % version
      val cache    = "com.typesafe.play" %% "play-cache"     % version
      val test     = "com.typesafe.play" %% "play-test"      % version
      val jdbc     = "com.typesafe.play" %% "play-jdbc"      % version
      val json     = "com.typesafe.play" %% "play-json"      % "2.6.14"
      val jsonJoda = "com.typesafe.play" %% "play-json-joda" % "2.6.14"

      val htmlCompressor = "com.mohiva"          %% "play-html-compressor" % "0.6.3"
      val playGuard      = "com.digitaltangible" %% "play-guard"           % "2.2.0"

      object Jwt {
        private val bouncyCastleVersion = "1.60"
        val bouncyCastle                = "org.bouncycastle"  % "bcprov-jdk15on" % bouncyCastleVersion
        val bouncyCastlePkix            = "org.bouncycastle"  % "bcpkix-jdk15on" % bouncyCastleVersion
        val atlassianJwtVersion         = "2.0.5"
        val atlassianJwtCore            = "com.atlassian.jwt" % "jwt-core"       % atlassianJwtVersion
      }

      object Silhouette {
        val version           = "5.1.4"
        val passwordBcrypt    = "com.mohiva" %% "play-silhouette-password-bcrypt" % version
        val persistence       = "com.mohiva" %% "play-silhouette-persistence"     % version
        val cryptoJca         = "com.mohiva" %% "play-silhouette-crypto-jca"      % version
        val silhouette        = "com.mohiva" %% "play-silhouette"                 % version
        val silhouetteTestkit = "com.mohiva" %% "play-silhouette-testkit"         % version % Test
      }
    }

    object Utils {
      private val awsSdkVersion    = "1.11.989"
      val pegdown                  = "org.pegdown"            % "pegdown"                         % "1.6.0"
      val awsJavaS3Sdk             = "com.amazonaws"          % "aws-java-sdk-s3"                 % awsSdkVersion
      val awsJavaSesSdk            = "com.amazonaws"          % "aws-java-sdk-ses"                % awsSdkVersion
      val prettyTime               = "org.ocpsoft.prettytime" % "prettytime"                      % "4.0.4.Final"
      val nbvcxz                   = "me.gosimple"            % "nbvcxz"                          % "1.4.3"
      val elasticacheClusterClient = "com.amazonaws"          % "elasticache-java-cluster-client" % "1.1.2"
      val playMemcached            = "com.github.mumoshu"    %% "play2-memcached-play26"          % "0.9.3" exclude ("net.spy", "spymemcached")
      val alpakkaAwsLambda         = "com.lightbend.akka"    %% "akka-stream-alpakka-awslambda"   % "0.20"
      val apacheCommonLang         = "org.apache.commons"     % "commons-lang3"                   % "3.10"
    }

    object Backend {
      private val version = "2.1.3_play2.6.25"
      val logPlay         = "io.dataswift" %% "log-play" % version
      val hatPlay         = "io.dataswift" %% "hat-play" % version
      val hatModels       = "io.dataswift" %% "hat"      % version
      val dexPlay         = "io.dataswift" %% "dex-play" % version
      val dexModels       = "io.dataswift" %% "dex"      % version
    }

    object HATDeX {
      val hatClient = "org.hatdex" %% "hat-client-scala-play" % "3.1.2" intransitive ()
      val dexClient = "org.hatdex" %% "dex-client-scala"      % "3.1.2" intransitive ()
      val codegen   = "org.hatdex" %% "slick-postgres-driver" % "0.1.2"
    }

    val scalaGuice  = "net.codingwell" %% "scala-guice"  % "4.2.6"
    val circeConfig = "io.circe"       %% "circe-config" % "0.8.0"

    object ContractLibrary {
      val adjudicator = "io.dataswift" %% "adjudicatorlib" % Versions.adjudicator
    }

    object Prometheus {
      val filters = "com.github.stijndehaes" %% "play-prometheus-filters" % "0.4.0"
    }

    object ScalaTest {
      val scalaplaytest     = "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2"   % Test
      val scalaplaytestmock = "org.scalatestplus"      %% "mockito-3-4"        % "3.2.2.0" % Test
    }

    object Dataswift {
      val testCommon            = "io.dataswift" %% "test-common"             % Version.DsTestTools % Test
      val integrationTestCommon = "io.dataswift" %% "integration-test-common" % Version.DsTestTools % Test
    }
    val overrides = Seq(
      "com.typesafe.play" %% "play"                  % Play.version,
      "com.typesafe.play" %% "play-server"           % Play.version,
      "com.typesafe.play" %% "play-ahc-ws"           % Play.version,
      "com.typesafe.play" %% "play-akka-http-server" % Play.version,
      "com.typesafe.play" %% "filters-helpers"       % Play.version,
      Library.Play.cache,
      Library.Play.ws,
      Library.Play.json,
      Library.Play.jsonJoda
    )
  }
}
