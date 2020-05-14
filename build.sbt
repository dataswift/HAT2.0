import Dependencies.Library
import sbt.Keys._
import com.typesafe.sbt.packager.docker._

// the application
lazy val hat = project
  .in(file("hat"))
  .enablePlugins(PlayScala)
  .enablePlugins(SbtWeb, SbtSassify, SbtGzip, SbtDigest)
  .enablePlugins(BasicSettings)
  .settings(
    libraryDependencies ++= Seq(
      Library.Play.ws,
      filters,
      ehcache,
      Library.Play.cache,
      Library.Play.test,
      Library.Play.mailer,
      Library.Play.mailerGuice,
      Library.Play.playGuard,
      Library.Play.json,
      Library.Play.jsonJoda,
      Library.Play.Silhouette.passwordBcrypt,
      Library.Play.Silhouette.persistence,
      Library.Play.Silhouette.cryptoJca,
      Library.Play.Silhouette.silhouette,
      Library.Play.Jwt.atlassianJwtCore,
      Library.Play.Jwt.bouncyCastlePkix,
      Library.Specs2.core,
      Library.Specs2.matcherExtra,
      Library.Specs2.mock,
      Library.HATDeX.hatClient,
      Library.HATDeX.dexClient,
      Library.HATDeX.codegen,
      Library.Utils.pegdown,
      Library.Utils.awsJavaS3Sdk,
      Library.Utils.prettyTime,
      Library.Utils.nbvcxz,
      Library.Utils.playMemcached,
      Library.Utils.elasticacheClusterClient,
      Library.Utils.alpakkaAwsLambda,
      Library.scalaGuice
    ),
    libraryDependencies := (buildEnv.value match {
      case BuildEnv.Developement | BuildEnv.Test =>
        libraryDependencies.value ++ Seq(
          Library.Play.specs2,
          Library.Specs2.core,
          Library.Specs2.matcherExtra,
          Library.Specs2.mock,
          Library.Play.Silhouette.silhouetteTestkit,
        )
      case BuildEnv.Stage | BuildEnv.Production =>
        libraryDependencies.value.map(excludeSpecs2)
    }),
    libraryDependencies += "org.codehaus.janino" % "janino" % "3.1.2",
    libraryDependencies += "org.mockito" % "mockito-core" % "3.3.3" % Test,
    pipelineStages in Assets := Seq(digest),
    sourceDirectory in Assets := baseDirectory.value / "app" / "org" / "hatdex" / "hat" / "phata" / "assets",
    aggregate in update := false,
    cancelable in Global := false, // Workaround sbt/bug#4822 Unable to Ctrl-C out of 'run' in a Play app
    testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "exclude", "REMOTE"),
    TwirlKeys.templateImports := Seq(),
    play.sbt.routes.RoutesKeys.routesImport := Seq.empty,
    routesGenerator := InjectedRoutesGenerator,
    concurrentRestrictions in Global += Tags.limit(Tags.Test, 1),
    coverageExcludedPackages := """.*\.controllers\..*Reverse.*;router.Routes.*;org.hatdex.hat.dal.Tables.*;org.hatdex.hat.phata.views.*;controllers.javascript\..*""",
    // Do not publish docs and source in compiled package
    sources in (Compile,doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(AshScriptPlugin)
  .settings(
    
    // Use the alternative "Ash" script for running compiled project form inside Alpine-derived container
    // as Bash is incompatible with Alpine
    javaOptions in Universal ++= Seq(),
    packageName in Docker := "hat",
    maintainer in Docker := "andrius.aucinas@hatdex.org",
    version in Docker := version.value,
    dockerExposedPorts := Seq(9000),
    dockerBaseImage := "openjdk:8-jre-alpine",
    dockerEntrypoint := Seq("bin/hat")
  )
  .enablePlugins(SlickCodeGeneratorPlugin)
  .settings(
    codegenPackageName in gentables := "org.hatdex.hat.dal",
    codegenBaseDir in gentables := (baseDirectory.value / "app").getCanonicalPath,
    codegenClassName in gentables := "Tables",
    codegenExcludedTables in gentables := Seq("databasechangelog", "databasechangeloglock"),
    codegenDatabase in gentables := "devdb",
    codegenConfig in gentables := "dev.conf",
    codegenEvolutions in gentables := "devhatMigrations"
  )

