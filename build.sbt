import Dependencies.Library
import Dependencies.Versions

import sbt.Keys._
import com.typesafe.sbt.packager.docker._

val codeguruURI =
  "https://repo1.maven.org/maven2/software/amazon/codeguruprofiler/codeguru-profiler-java-agent-standalone/1.1.0/codeguru-profiler-java-agent-standalone-1.1.0.jar"

// the application
lazy val hat = project
  .in(file("hat"))
  .enablePlugins(PlayScala)
  .enablePlugins(SbtWeb, SbtSassify, SbtGzip, SbtDigest)
  .enablePlugins(BasicSettings)
  .settings(
    dependencyOverrides := Library.overrides,
    libraryDependencies ++= Seq(
          Library.Play.ws,
          filters,
          ehcache,
          Library.Play.cache,
          Library.Play.test,
          Library.Play.playGuard,
          Library.Play.json,
          Library.Play.jsonJoda,
          Library.Backend.logPlay,
          Library.Backend.dexPlay,
          Library.Backend.hatPlay,
          Library.Backend.dexModels,
          Library.Backend.hatModels,
          Library.Play.Silhouette.passwordBcrypt,
          Library.Play.Silhouette.persistence,
          Library.Play.Silhouette.cryptoJca,
          Library.Play.Silhouette.silhouette,
          Library.Play.Jwt.atlassianJwtCore,
          Library.Play.Jwt.bouncyCastlePkix,
          Library.HATDeX.hatClient,
          Library.HATDeX.dexClient,
          Library.HATDeX.codegen,
          Library.Utils.pegdown,
          Library.Utils.awsJavaS3Sdk,
          Library.Utils.awsJavaSesSdk,
          Library.Utils.prettyTime,
          Library.Utils.nbvcxz,
          Library.Utils.playMemcached,
          Library.Utils.elasticacheClusterClient,
          Library.Utils.alpakkaAwsLambda,
          Library.scalaGuice,
          Library.circeConfig,
          Library.ContractLibrary.adjudicator,
          Library.Utils.apacheCommonLang,
          Library.Prometheus.filters
        ),
    libraryDependencies := (buildEnv.value match {
          case BuildEnv.Developement | BuildEnv.Test =>
            libraryDependencies.value ++ Seq(
                  Library.Play.Silhouette.silhouetteTestkit,
                  Library.ScalaTest.scalaplaytest,
                  Library.ScalaTest.scalaplaytestmock,
                  Library.Dataswift.integrationTestCommon
                )
          case BuildEnv.Stage | BuildEnv.Production =>
            libraryDependencies.value.map(excludeSpecs2)
        }),
    libraryDependencies += "org.codehaus.janino" % "janino"       % "3.1.2",
    libraryDependencies += "org.mockito"         % "mockito-core" % "3.3.3" % Test,
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
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(AshScriptPlugin)
  .settings(
    // Use the alternative "Ash" script for running compiled project form inside Alpine-derived container
    // as Bash is incompatible with Alpine
    javaOptions in Universal ++= Seq(),
    packageName in Docker := "hat",
    version in Docker := version.value,
    // add a flag to not run in prod
    // modify the binary to include "-javaagent:codeguru-profiler-java-agent-standalone-1.1.0.jar="profilingGroupName:HatInDev,heapSummaryEnabled:true"
    dockerCommands := (buildEnv.value match {
          case BuildEnv.Developement | BuildEnv.Test =>
            Seq(
              Cmd("FROM", "adoptopenjdk/openjdk11:jre-11.0.10_9-alpine"),
              Cmd("WORKDIR", "/opt/docker/bin"),
              Cmd("CMD", s"./${packageName.value}"),
              Cmd("EXPOSE", "9000"),
              Cmd("RUN", s"apk add --no-cache wget; wget --no-check-certificate ${codeguruURI}"),
              Cmd("USER", "daemon"),
              Cmd("COPY", "--chown=daemon:daemon", "1/opt", "opt", "/opt/")
            )
          case BuildEnv.Stage | BuildEnv.Production =>
            Seq(
              Cmd("FROM", "adoptopenjdk/openjdk11:jre-11.0.10_9-alpine"),
              Cmd("WORKDIR", "/opt/docker/bin"),
              Cmd("CMD", s"./${packageName.value}"),
              Cmd("EXPOSE", "9000"),
              Cmd("USER", "daemon"),
              Cmd("COPY", "--chown=daemon:daemon", "1/opt", "opt", "/opt/")
            )
        }),
    javaOptions in Universal ++= (buildEnv.value match {
          case BuildEnv.Developement | BuildEnv.Test =>
            Seq(
              "-javaagent:/opt/docker/bin/codeguru-profiler-java-agent-standalone-1.1.0.jar=\"profilingGroupName:HatInDev,heapSummaryEnabled:true\""
            )
          case BuildEnv.Stage | BuildEnv.Production =>
            Seq(
            )
        })
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
    .settings(

      // Omit Tables.scala for scalafix, since it is autogenerated
      unmanagedSources.in(Compile, scalafix) := unmanagedSources
        .in(Compile)
        .value
        .filterNot(
          _.getAbsolutePath.contains(
            "dal/Tables.scala"
          )
        ),

        // Omit Tables.scala for scalafmt, since it is autogenerated
        unmanagedSources.in(Compile, scalafmt) := unmanagedSources
  .in(Compile)
  .value
  .filterNot(
    _.getAbsolutePath.contains(
      "dal/Tables.scala"
    )
  )
    )

// Enable the semantic DB for scalafix
inThisBuild(
  List(
    scalaVersion := Versions.scalaVersion,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.4.4"
  )
)


