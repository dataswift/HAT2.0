import Dependencies._

libraryDependencies ++= Seq(
  filters,
  Library.Akka.httpCore,
  Library.Utils.jodaTime,
  Library.Utils.jodaConvert,
  Library.Utils.jts,
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
  Library.Play.Jwt.atlassianJwtCore,
  Library.Play.Jwt.atlassianJwtApi,
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
  Library.Play.Utils.playGuard,
  Library.scalaGuice,
  Library.HATDeX.hatClient,
  Library.HATDeX.dexClient,
  Library.HATDeX.codegen,
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

enablePlugins(SlickCodeGeneratorPlugin)

codegenPackageName in gentables := "org.hatdex.hat.dal"
codegenOutputDir in gentables := (baseDirectory.value / "app").getPath
codegenClassName in gentables := "Tables"
codegenExcludedTables in gentables := Seq("databasechangelog", "databasechangeloglock")
codegenDatabase in gentables := "devdb"

import scalariform.formatter.preferences._
scalariformPreferences := scalariformPreferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(DoubleIndentConstructorArguments, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(CompactControlReadability, true)
  .setPreference(DanglingCloseParenthesis, Prevent)
