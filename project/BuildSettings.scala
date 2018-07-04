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
    version := "2.6.0-SNAPSHOT",
    name := "HAT",
    resolvers ++= Dependencies.resolvers,
    scalaVersion := Dependencies.Versions.scalaVersion,
    crossScalaVersions := Dependencies.Versions.crossScala,
    scalacOptions ++= Seq(
      "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
      "-encoding", "utf-8",                // Specify character encoding used by source files.
      "-explaintypes",                     // Explain type errors in more detail.
      "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
      "-language:higherKinds",             // Allow higher-kinded types
      "-language:implicitConversions",     // Allow definition of implicit functions called views
      "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
      "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
      "-Xfuture",                          // Turn on future language features.
      "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
      "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
      "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
      "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
      "-Xlint:option-implicit",            // Option.apply used implicit view.
      "-Xlint:package-object-classes",     // Class or object defined in package object.
      "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
      "-Xlint:unsound-match",              // Pattern match may not be typesafe.
      "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
      "-Ypartial-unification",             // Enable partial unification in type constructor inference
      "-Ywarn-dead-code",                  // Warn when dead code is identified.
      "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
      "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
      "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
      "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
      "-Ywarn-numeric-widen",              // Warn when numerics are widened.
      "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals",              // Warn if a local definition is unused.
      "-Ywarn-unused:params",              // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates",            // Warn if a private member is unused.
      "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
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

