import sbt.Keys.sbtPlugin

lazy val hat = Project(
  id = "hat",
  base = file("hat"))

val root = Project(
  id = "hat-project",
  base = file("."))
.settings(
  Defaults.coreDefaultSettings,
  publishLocal := {},
  publishM2 := {},
  publishArtifact := false)
.aggregate(hat)
