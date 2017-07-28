import Dependencies._

libraryDependencies ++= Seq(
  filters,
  Library.Db.postgres,
  Library.Db.liquibase,
  Library.Slick.slickPgCore,
  Library.Slick.slickPg,
  Library.Slick.slickPgJoda,
  Library.Slick.slickPgJts,
  Library.Slick.slickCodegen,
  Library.Slick.slickHikari,
//  Library.Akka.slf4j,
  Library.Akka.httpCore,
//  Library.Akka.akkaStream,
//  Library.Akka.akkaActor,
//  Library.Akka.akkaTestkit,
  Library.Utils.jodaTime,
  Library.Utils.jodaConvert,
  Library.Utils.jts,
//  Library.Utils.slf4j,
//  Library.Utils.logbackCore,
  Library.Utils.logbackClassic,
  Library.Utils.pegdown,
  Library.Specs2.core,
  Library.Specs2.matcherExtra,
  Library.Specs2.mock,
  Library.Play.Silhouette.passwordBcrypt,
  Library.Play.Silhouette.persistence,
  Library.Play.Silhouette.cryptoJca,
  Library.Play.Silhouette.silhouette,
  Library.Play.Silhouette.silhouetteTestkit,
//  Library.Play.Jwt.nimbusDsJwt,
//  Library.Play.Jwt.atlassianJwtCore,
//  Library.Play.Jwt.atlassianJwtApi,
//  Library.Play.Jwt.bouncyCastle,
  Library.Play.Jwt.bouncyCastlePkix,
  Library.Play.ws,
  Library.Play.cache,
  Library.Play.test,
  Library.Play.typesafeConfigExtras,
  Library.Play.mailer,
  Library.Play.specs2,
  Library.Play.Specs2.matcherExtra,
  Library.Play.Specs2.mock,
  Library.Play.Utils.playBootstrap,
  Library.scalaGuice,
  Library.HATDeX.hatClient,
  Library.HATDeX.marketsquareClient,
  Library.Utils.awsJavaS3Sdk,
  Library.Utils.prettyTime,
  Library.Utils.nbvcxz
)

enablePlugins(PlayScala)

enablePlugins(JavaAppPackaging)

enablePlugins(SbtWeb, SbtSassify, SbtGzip, SbtDigest)

pipelineStages in Assets := Seq(digest)
sourceDirectory in Assets := baseDirectory.value / "app" / "org" / "hatdex" / "hat" / "phata" / "assets"

aggregate in update := false

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "exclude", "REMOTE")

routesGenerator := InjectedRoutesGenerator

// Do not publish docs and source in compiled package
publishArtifact in (Compile, packageDoc) := false
publishArtifact in (Compile, packageSrc) := false

// Use the alternative "Ash" script for running compiled project form inside Alpine-derived container
// as Bash is incompatible with Alpine
enablePlugins(AshScriptPlugin)
javaOptions in Universal ++= Seq("-Dhttp.port=8080")

import com.typesafe.sbt.packager.docker._
packageName in Docker := "hat"
maintainer in Docker := "andrius.aucinas@hatdex.org"
version in Docker := version.value
dockerExposedPorts := Seq(8080)
dockerBaseImage := "openjdk:8-jre-alpine"
dockerEntrypoint := Seq("bin/hat")

lazy val gentables = taskKey[Seq[File]]("Slick Code generation")

gentables := {
  val main = Project("root", file("."))
  val outputDir = (main.base.getAbsoluteFile / "hat/app").getPath
  streams.value.log.info("Output directory for codegen: " + outputDir.toString)
  val pkg = "org.hatdex.hat.dal"
  val jdbcDriver = "org.postgresql.Driver"
  val slickDriver = "slick.driver.PostgresDriver"
  streams.value.log.info("Dependency classpath: " + dependencyClasspath.toString)
  (runner in Compile).value.run("org.hatdex.hat.dal.CustomizedCodeGenerator", (dependencyClasspath in Compile).value.files, Array(outputDir, pkg), streams.value.log)
  val fname = outputDir + "/" + pkg.replace('.', '/') + "/Tables.scala"
  Seq(file(fname))
}
