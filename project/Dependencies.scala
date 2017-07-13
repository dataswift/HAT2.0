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
    val crossScala = Seq("2.11.8")
    val scalaVersion = crossScala.head
  }

  val resolvers = Seq(
    "Atlassian Releases" at "https://maven.atlassian.com/public/",
    "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
    "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-snapshots.hubofallthings.com",
    "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
  )

  object Library {

    object Play {
      val version = play.core.PlayVersion.current
      val ws = "com.typesafe.play" %% "play-ws" % version
      val cache = "com.typesafe.play" %% "play-cache" % version
      val test = "com.typesafe.play" %% "play-test" % version
      val specs2 = "com.typesafe.play" %% "play-specs2" % version
      val typesafeConfigExtras = "com.iheart" %% "ficus" % "1.4.1"
      val mailer = "com.typesafe.play" %% "play-mailer" % "5.0.0"

      object Specs2 {
        private val version = "3.6.6"
        val matcherExtra = "org.specs2" %% "specs2-matcher-extra" % version
        val mock = "org.specs2" %% "specs2-mock" % version
      }
      object Jwt {
        private val bouncyCastleVersion = "1.57"
        val bouncyCastle = "org.bouncycastle" % "bcprov-jdk15on" % bouncyCastleVersion
        val bouncyCastlePkix = "org.bouncycastle" % "bcpkix-jdk15on" % bouncyCastleVersion
        val atlassianJwtVersion = "1.6.1"
        val atlassianJwtCore = "com.atlassian.jwt" % "jwt-core" % atlassianJwtVersion
        val atlassianJwtApi = "com.atlassian.jwt" % "jwt-api" % atlassianJwtVersion
        val nimbusDsJwt = "com.nimbusds" % "nimbus-jose-jwt" % "3.6"
      }
      object Db {
        val jdbc = "com.typesafe.play" %% "play-jdbc" % version
        val postgres = Library.Db.postgres
      }

      object Utils {
        val playBootstrap = "com.adrianhurt" %% "play-bootstrap" % "1.1-P25-B3" exclude("org.webjars", "jquery")
        val commonsValidator = "commons-validator" % "commons-validator" % "1.5.0"
        val htmlCompressor = "com.mohiva" %% "play-html-compressor" % "0.6.3"
      }

      object Silhouette {
        val version = "4.1.0-SNAPSHOT"
        val passwordBcrypt = "com.mohiva" %% "play-silhouette-password-bcrypt" % version
        val persistence = "com.mohiva" %% "play-silhouette-persistence" % version
        val cryptoJca = "com.mohiva" %% "play-silhouette-crypto-jca" % version
        val silhouette = "com.mohiva" %% "play-silhouette" % version
        val silhouetteTestkit = "com.mohiva" %% "play-silhouette-testkit" % version % "test"
      }
    }

    object Specs2 {
      private val version = "3.6.6"
      val core = "org.specs2" %% "specs2-core" % version
      val matcherExtra = "org.specs2" %% "specs2-matcher-extra" % version
      val mock = "org.specs2" %% "specs2-mock" % version
    }

    object Utils {
      val pegdown = "org.pegdown" % "pegdown" % "1.6.0"
      val jodaTime = "joda-time" % "joda-time" % "2.9.9"
      val jodaConvert = "org.joda" % "joda-convert" % "1.8"
      val jts = "com.vividsolutions" % "jts" % "1.13"
      val slf4j = "org.slf4j" % "slf4j-api" % "1.7.18"
      val logbackV = "1.1.2"
      val logbackCore = "ch.qos.logback" % "logback-core" % logbackV
      val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackV
      val awsJavaSdk = "com.amazonaws" % "aws-java-sdk" % "1.10.64"
      val awsJavaS3Sdk = "com.amazonaws" % "aws-java-sdk-s3" % "1.11.123"
      val prettyTime = "org.ocpsoft.prettytime" % "prettytime" % "3.2.7.Final"
      val nbvcxz = "me.gosimple" % "nbvcxz" % "1.3.4"
    }

    object Db {
      val liquibase = "org.liquibase" % "liquibase-maven-plugin" % "3.5.3"
      val postgres = "org.postgresql" % "postgresql" % "9.4-1206-jdbc4"
      val hikariCP = "com.zaxxer" % "HikariCP" % "2.5.0"
    }

    object Slick {
      private val slickVersion = "3.1.1"
      val slick = "com.typesafe.slick" %% "slick" % slickVersion
      val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
      val slickCodegen = "com.typesafe.slick" %% "slick-codegen" % slickVersion
      val slick_pgV = "0.14.6"
      val slickPgCore = "com.github.tminglei" % "slick-pg_core_2.11" % slick_pgV
      val slickPg = "com.github.tminglei" %% "slick-pg" % slick_pgV
      val slickPgJoda = "com.github.tminglei" %% "slick-pg_joda-time" % slick_pgV
      val slickPgJts = "com.github.tminglei" %% "slick-pg_jts" % slick_pgV
      val slickPgSprayJson = "com.github.tminglei" % "slick-pg_spray-json_2.11" % slick_pgV
      val slickPgPlayJson = "com.github.tminglei" % "slick-pg_play-json_2.11" % slick_pgV
    }

    object Akka {
      private val version = "2.4.19"
      val slf4j = "com.typesafe.akka" %% "akka-slf4j" % version
      val httpCore = "com.typesafe.akka" % "akka-http-core_2.11" % "10.0.8"
      val akkaStream = "com.typesafe.akka" %% "akka-stream" % version
      val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json-experimental" % version
      val akkaActor = "com.typesafe.akka" %% "akka-actor" % version
      val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % version
    }

//    object Spray {
//      private val version = "1.3.3"
//      val sprayCan = "io.spray" %% "spray-can" % version
//      val sprayRouting = "io.spray" %% "spray-routing-shapeless2" % version
//      val sprayTestkit = "io.spray" %% "spray-testkit" % version % "test"
//    }

    object HATDeX {
      private val version = "2.3.0-SNAPSHOT"
      val hatClient = "org.hatdex" %% "hat-client-scala-play" % "2.4.0-SNAPSHOT"
      val marketsquareClient = "org.hatdex" %% "marketsquare-client-scala-play" % version
    }

    val jwtCore = Play.Jwt.atlassianJwtCore
    val jwtApi = Play.Jwt.atlassianJwtApi
    val akkaTestkit = Akka.akkaTestkit
    val scalaGuice = "net.codingwell" %% "scala-guice" % "4.0.1"
    val jbcrypt = "org.mindrot" % "jbcrypt" % "0.3m"
  }
}
