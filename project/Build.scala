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