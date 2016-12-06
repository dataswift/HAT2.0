import Dependencies._

libraryDependencies ++= Seq(
  Library.Db.postgres,
  Library.Db.liquibase,
  Library.Slick.slickPgCore,
  Library.Slick.slickPg,
  Library.Slick.slickPgJoda,
  Library.Slick.slickPgJts,
  Library.Slick.slickPgSprayJson,
  Library.Slick.slickCodegen,
  Library.Slick.slickHikari,
  Library.Akka.slf4j,
  Library.Akka.httpCore,
  Library.Akka.akkaStream,
  Library.Akka.akkaHttpSprayJson,
  Library.Akka.akkaActor,
  Library.Akka.akkaTestkit,
  Library.Utils.jodaTime,
  Library.Utils.jodaConvert,
  Library.Utils.jts,
  Library.Utils.slf4j,
  Library.Utils.logbackCore,
  Library.Utils.logbackClassic,
  Library.Utils.pegdown,
  Library.Specs2.core,
  Library.Specs2.matcherExtra,
  Library.Specs2.mock,
  Library.Spray.sprayCan,
  Library.Play.Silhouette.passwordBcrypt,
  Library.Play.Silhouette.persistence,
  Library.Play.Silhouette.cryptoJca,
  Library.Play.Silhouette.silhouette,
  Library.Spray.sprayRouting,
  Library.Spray.sprayTestkit,
  Library.Play.Jwt.nimbusDsJwt,
  Library.Play.Jwt.atlassianJwtCore,
  Library.Play.Jwt.atlassianJwtApi,
  Library.Play.Jwt.bouncyCastle,
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
  Library.jbcrypt
)

enablePlugins(PlayScala)

enablePlugins(JavaAppPackaging)

enablePlugins(SbtWeb, SbtSassify)

pipelineStages := Seq(uglify)
sourceDirectory in Assets := baseDirectory.value / "app" / "org" / "hatdex" / "hat" / "phata" / "assets"

//excludeFilter += (baseDirectory.value / "app" / "org" / "hatdex" / "hat" / "api" / "controllers" / "*.scala") filter ( _.g)

//unmanagedSourceDirectories in Compile ++= (baseDirectory.value / "app" / "org" / "hatdex" / "hat" / "api" / "actors" / "*.scala").get
//
//unmanagedSourceDirectories in Compile ++= (baseDirectory.value / "app" / "org" / "hatdex" / "hat" / "dal" / "*.scala").get
//
//unmanagedSourceDirectories in Compile ++= (baseDirectory.value / "app" / "org" / "hatdex" / "hat" / "api" / "service" / "UsersService*.scala").get
//
//unmanagedSourceDirectories in Compile ++= (baseDirectory.value / "app" / "org" / "hatdex" / "hat" / "api" / "DatabaseInfo*.scala").get

aggregate in update := false
//sourceDirectories in(Compile, TwirlKeys.compileTemplates) := (unmanagedSourceDirectories in Compile).value
testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "exclude", "REMOTE")


//  in unmanagedSources := new SimpleFileFilter(
//  (baseDirectory.value / "hat" / "app" / "org" / "hatdex" / "hat" / "api" / "endpoints" / "*.scala") +++
//  (baseDirectory.value / "hat" / "app" / "org" / "hatdex" / "hat" / "api" / "json" / "*.scala") +++
//  (baseDirectory.value / "hat" / "app" / "org" / "hatdex" / "hat" / "api" / "endpoints" / "Api*.scala") +++
//  (baseDirectory.value / "hat" / "app" / "org" / "hatdex" / "hat" / "api" / "endpoints" * "Boot*.scala") +++
//  (baseDirectory.value / "hat" / "app" / "org" / "hatdex" / "hat" / "api" / "endpoints" * "Cors*.scala"))

// scoverage.ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := "hatdex.hat.dal"

//publishArtifact in(Compile, packageDoc) := false

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
