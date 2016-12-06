/* Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, September 2016
 */

import sbt.Keys._
import sbt._

object Build extends Build {

  lazy val hatClientPlay = Project(
    id = "hat-client-play",
    base = file("hat-client-scala-play")
  )

  lazy val marketsquareClientPlay = Project(
    id = "marketsquare-client-play",
    base = file("marketsquare-client-scala-play"),
    dependencies = Seq(hatClientPlay % "compile->compile;test->test")
  )

  lazy val codegen = Project(
    id = "codegen",
    base = file("codegen")
  )

  lazy val hat = Project(
    id = "hat",
    base = file("hat"),
    dependencies = Seq(
      codegen % "compile->compile;test->test",
      marketsquareClientPlay % "compile->compile;test->test",
      hatClientPlay % "compile->compile;test->test")
  )

  val root = Project(
    id = "hat-project",
    base = file("."),
    aggregate = Seq(
      hatClientPlay,
      codegen,
      marketsquareClientPlay,
      hat
    ),
    settings = Defaults.coreDefaultSettings ++
      Seq(
        publishLocal := {},
        publishM2 := {},
        publishArtifact := false
      )
  )
}