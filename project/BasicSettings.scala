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
  }
  import autoImport._

  override def projectSettings =
    Seq(
      organization := "org.hatdex",
      version := "2.8.0-SNAPSHOT",
      name := "HAT",
      resolvers ++= Dependencies.resolvers,
      scalacOptions ++= Seq(
            "-deprecation", // Emit warning and location for usages of deprecated APIs.
            "-encoding",
            "utf-8", // Specify character encoding used by source files.
            "-explaintypes", // Explain type errors in more detail.
            "-feature", // Emit warning and location for usages of features that should be imported explicitly.
            "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
            "-language:experimental.macros", // Allow macro definition (besides implementation and application)
            "-language:higherKinds", // Allow higher-kinded types
            "-language:implicitConversions", // Allow definition of implicit functions called views
            "-unchecked", // Enable additional warnings where generated code depends on assumptions.
            "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
            //"-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
            "-Xlint",
            "-Ywarn-dead-code", // Warn when dead code is identified.
            "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
            "-Ywarn-numeric-widen", // Warn when numerics are widened.
            "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
            "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
            "-Ywarn-unused:locals", // Warn if a local definition is unused.
            "-Ywarn-unused:params", // Warn if a value parameter is unused.
            "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
            "-Ywarn-unused:privates", // Warn if a private member is unused.
            "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
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
        sys.props
          .get("env")
          .orElse(sys.env.get("BUILD_ENV"))
          .flatMap {
            case "prod"  => Some(BuildEnv.Production)
            case "stage" => Some(BuildEnv.Stage)
            case "test"  => Some(BuildEnv.Test)
            case "dev"   => Some(BuildEnv.Developement)
            case unknown => None
          }
          .getOrElse(BuildEnv.Developement)
      },
      // give feed back
      onLoadMessage := {
        // depend on the old message as well
        val defaultMessage = onLoadMessage.value
        val env            = buildEnv.value
        s"""|$defaultMessage
          |Running in build environment: $env""".stripMargin
      }
    )
}
