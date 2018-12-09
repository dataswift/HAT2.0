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
    val crossScala = Seq("2.12.6")
    val scalaVersion = crossScala.head
  }

  val resolvers = Seq(
    Resolver.jcenterRepo,
    "Atlassian Releases" at "https://maven.atlassian.com/public/",
    Resolver.bintrayRepo("scalaz", "releases"),
    Resolver.sonatypeRepo("snapshots"),
    "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-snapshots.hubofallthings.com",
    "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com")

  object Library {

    object Play {
      val version = play.core.PlayVersion.current
      val ws = "com.typesafe.play" %% "play-ws" % version
      val cache = "com.typesafe.play" %% "play-cache" % version
      val test = "com.typesafe.play" %% "play-test" % version
      val specs2 = "com.typesafe.play" %% "play-specs2" % version
      val jdbc = "com.typesafe.play" %% "play-jdbc" % version
      val json = "com.typesafe.play" %% "play-json" % "2.6.9"
      val jsonJoda = "com.typesafe.play" %% "play-json-joda" % "2.6.9"
      val mailer = "com.typesafe.play" %% "play-mailer" % "6.0.1"
      val mailerGuice = "com.typesafe.play" %% "play-mailer-guice" % "6.0.1"

      val htmlCompressor = "com.mohiva" %% "play-html-compressor" % "0.6.3"
      val playGuard = "com.digitaltangible" %% "play-guard" % "2.2.0"

      object Jwt {
        private val bouncyCastleVersion = "1.60"
        val bouncyCastle = "org.bouncycastle" % "bcprov-jdk15on" % bouncyCastleVersion
        val bouncyCastlePkix = "org.bouncycastle" % "bcpkix-jdk15on" % bouncyCastleVersion
        val atlassianJwtVersion = "2.0.2"
        val atlassianJwtCore = "com.atlassian.jwt" % "jwt-core" % atlassianJwtVersion
      }

      object Silhouette {
        val version = "5.1.4"
        val passwordBcrypt = "com.mohiva" %% "play-silhouette-password-bcrypt" % version
        val persistence = "com.mohiva" %% "play-silhouette-persistence" % version
        val cryptoJca = "com.mohiva" %% "play-silhouette-crypto-jca" % version
        val silhouette = "com.mohiva" %% "play-silhouette" % version
        val silhouetteTestkit = "com.mohiva" %% "play-silhouette-testkit" % version % "test"
      }
    }

    object Specs2 {
      private val version = "3.9.5"
      val core = "org.specs2" %% "specs2-core" % version
      val matcherExtra = "org.specs2" %% "specs2-matcher-extra" % version
      val mock = "org.specs2" %% "specs2-mock" % version
    }

    object Utils {
      val pegdown = "org.pegdown" % "pegdown" % "1.6.0"
      val awsJavaSdk = "com.amazonaws" % "aws-java-sdk" % "1.11.386"
      val awsJavaS3Sdk = "com.amazonaws" % "aws-java-sdk-s3" % "1.11.386"
      val prettyTime = "org.ocpsoft.prettytime" % "prettytime" % "4.0.2.Final"
      val nbvcxz = "me.gosimple" % "nbvcxz" % "1.4.2"
      val elasticacheClusterClient = "com.amazonaws" % "elasticache-java-cluster-client" % "1.1.1"
      val playMemcached = "com.github.mumoshu" %% "play2-memcached-play26" % "0.9.2" exclude("net.spy", "spymemcached")
      val alpakkaAwsLambda = "com.lightbend.akka" %% "akka-stream-alpakka-awslambda" % "0.20"
    }

    object HATDeX {
      private val version = "2.6.4-SNAPSHOT"
      val hatClient = "org.hatdex" %% "hat-client-scala-play" % version
      val dexClient = "org.hatdex" %% "dex-client-scala-play" % version
      val codegen = "org.hatdex" %% "slick-postgres-driver" % "0.0.9"
    }

    val scalaGuice = "net.codingwell" %% "scala-guice" % "4.2.1"
  }
}
