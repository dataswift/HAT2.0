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

import sbt.Keys._
import sbt._

////*******************************
//// Basic settings
////*******************************
object BasicSettings extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    object BuildEnv extends Enumeration {
      val Production, Stage, Test, Developement = Value
    }

    val buildEnv = settingKey[BuildEnv.Value]("the current build environment")

    def excludeSpecs2(module: ModuleID): ModuleID = {
      module.excludeAll(
        ExclusionRule(organization = "org.specs2"),
        ExclusionRule(organization = "org.seleniumhq.selenium"))
    }
  }
  import autoImport._

  override def projectSettings = Seq(
    organization := "org.hatdex",
    version := "2.5.2-SNAPSHOT",
    name := "HAT",
    resolvers ++= Dependencies.resolvers,
    scalaVersion := Dependencies.Versions.scalaVersion,
    crossScalaVersions := Dependencies.Versions.crossScala,
    scalacOptions ++= Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
//      "-Xfatal-warnings", // Fail the compilation if there are any warnings.
      "-Xlint", // Enable recommended additional warnings.
      "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Ywarn-dead-code", // Warn when dead code is identified.
      "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
      "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
      "-language:postfixOps", // Allow postfix operators
      "-Ywarn-numeric-widen" // Warn when numerics are widened.
    ),
    scalacOptions in Test ~= { (options: Seq[String]) =>
      options filterNot (_ == "-Ywarn-dead-code") // Allow dead code in tests (to support using mockito).
    },
    parallelExecution in Test := false,
    fork in Test := true,
    // Needed to avoid https://github.com/travis-ci/travis-ci/issues/3775 in forked tests
    // in Travis with `sudo: false`.
    // See https://github.com/sbt/sbt/issues/653
    // and https://github.com/travis-ci/travis-ci/issues/3775
    javaOptions += "-Xmx1G",
    buildEnv := {
      sys.props.get("env")
        .orElse(sys.env.get("BUILD_ENV"))
        .flatMap {
          case "prod" => Some(BuildEnv.Production)
          case "stage" => Some(BuildEnv.Stage)
          case "test" => Some(BuildEnv.Test)
          case "dev" => Some(BuildEnv.Developement)
          case unknown => None
        }
        .getOrElse(BuildEnv.Developement)
    },
    // give feed back
    onLoadMessage := {
      // depend on the old message as well
      val defaultMessage = onLoadMessage.value
      val env = buildEnv.value
      s"""|$defaultMessage
          |Running in build environment: $env""".stripMargin
    }
  ) ++ scalariformPrefs

  import com.typesafe.sbt.SbtScalariform._
  import scalariform.formatter.preferences._

  lazy val scalariformPrefs = Seq(
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(FormatXml, false)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(CompactControlReadability, true)
    .setPreference(DanglingCloseParenthesis, Prevent)
  )

}

////*******************************
//// ScalaDoc settings
////*******************************
//object Doc extends AutoPlugin {
//
//  import play.core.PlayVersion
//
//  override def projectSettings = Seq(
//    autoAPIMappings := true,
//    apiURL := Some(url(s"http://hub-of-all-things.github.io/doc/${version.value}/")),
//    apiMappings ++= {
//      implicit val cp = (fullClasspath in Compile).value
//      Map(
//        jarFor("com.typesafe.play", "play") -> url(s"http://www.playframework.com/documentation/${PlayVersion.current}/api/scala/"),
//        scalaInstance.value.libraryJar -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/")
//      )
//    }
//  )
//
//  /**
//   * Gets the JAR file for a package.
//   *
//   * @param organization The organization name.
//   * @param name The name of the package.
//   * @param cp The class path.
//   * @return The file which points to the JAR.
//   * @see http://stackoverflow.com/a/20919304/2153190
//   */
//  private def jarFor(organization: String, name: String)(implicit cp: Seq[Attributed[File]]): File = {
//    (for {
//      entry <- cp
//      module <- entry.get(moduleID.key)
//      if module.organization == organization
//      if module.name.startsWith(name)
//      jarFile = entry.data
//    } yield jarFile).head
//  }
//}

