import NativePackagerKeys._
import com.typesafe.sbt.SbtNativePackager._
import sbt.Keys._

enablePlugins(JavaAppPackaging)

name := """The HAT"""
organization := """org.hatex"""
version := "2.0-SNAPSHOT"

scalaVersion := "2.11.6"

parallelExecution in Test := false

publishArtifact in Test := false

scalacOptions in (Compile, doc) ++= Seq("-unchecked", /*"-deprecation", */ "-implicits", "-skip-packages", "samples")

logLevel := Level.Info

val akkaV = "2.3.9"
val sprayV = "1.3.3"
val specs2V = "3.3"
val slf4jV = "1.7.10"
val logbackV = "1.1.2"

lazy val commonSettings = Seq(
  scalaVersion := "2.11.6",
  libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % "3.0.0",
    "com.github.tminglei" % "slick-pg_core_2.11" % "0.9.0",
    "com.github.tminglei" %% "slick-pg" % "0.9.0",
    "com.github.tminglei" %% "slick-pg_joda-time" % "0.6.5.3",
    "com.github.tminglei" %% "slick-pg_jts" % "0.6.5.3",
    "joda-time" % "joda-time" % "2.7",
    "org.joda" % "joda-convert" % "1.7",
    "com.vividsolutions" % "jts" % "1.13",
    "org.slf4j" % "slf4j-api" % slf4jV,
    "ch.qos.logback" % "logback-core" % logbackV,
    "ch.qos.logback" % "logback-classic" % logbackV,
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe" % "config" % "1.3.0",
    "com.zaxxer" % "HikariCP" % "2.3.8"
  )
)

// code generation task
lazy val gentables = taskKey[Seq[File]]("Slick Code generation")

lazy val codegen = (project in file("codegen")).
  settings(commonSettings: _*).
  settings(
    name := "codegen",
    libraryDependencies ++= List(
      "com.typesafe.slick" %% "slick-codegen" % "3.0.0"
    ),
    gentables := {
      val main = Project("root", file("."))
      val outputDir = (main.base.getAbsoluteFile / "src/main/scala").getPath
      streams.value.log.info("Output directory for codegen: " + outputDir.toString)
      val pkg = "hatdex.hat.dal"
      streams.value.log.info("Dependency classpath: " + dependencyClasspath.toString)
      (runner in Compile).value.run("hatdex.hat.dal.CustomizedCodeGenerator", (dependencyClasspath in Compile).value.files, Array(outputDir, pkg), streams.value.log)
      val fname = outputDir + "/" + pkg.replace('.', '/') + "/Tables.scala"
      Seq(file(fname))
    },
    cleanFiles <+= baseDirectory { base => base / "../src/main/scala/hatdex/hat/dal/" }
  )

lazy val core = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "root",
    libraryDependencies ++= List(
      "io.spray"            %%  "spray-can"     % sprayV,
      "io.spray"            %%  "spray-routing-shapeless2" % sprayV,
      "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
      "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
      "io.spray"      %%  "spray-testkit" % sprayV  % "test",
      "org.specs2" % "specs2-core_2.11" % specs2V  % "test",
      "org.specs2" % "specs2_2.11" % specs2V % "test",
      "io.spray" %%  "spray-json" % "1.3.2",
      "org.mindrot" % "jbcrypt" % "0.3m"
    ),
    gentables := {
      val main = Project("root", file("."))
      val outputDir = (main.base.getAbsoluteFile / "src/main/scala").getPath
      streams.value.log.info("Output directory for codegen: " + outputDir.toString)
      val pkg = "hatdex.hat.dal"
      (runner in Compile).value.run("hatdex.hat.dal.CustomizedCodeGenerator", (dependencyClasspath in Compile).value.files, Array(outputDir, pkg), streams.value.log)
      val fname = outputDir + "/" + pkg.replace('.', '/') + "/Tables.scala"
      Seq(file(fname))
    }
  ).
  dependsOn("codegen").
  settings (
    aggregate in update := false
  )

resolvers ++= Seq(
  "scalaz.bintray" at "http://dl.bintray.com/scalaz/releases",
  "scoverage-bintray" at "https://dl.bintray.com/sksamuel/sbt-plugins/"
)
