import Dependencies.Library
import com.typesafe.sbt.packager.docker._
import sbt.Keys._

val codeguruURI =
  "https://repo1.maven.org/maven2/software/amazon/codeguruprofiler/codeguru-profiler-java-agent-standalone/1.1.0/codeguru-profiler-java-agent-standalone-1.1.0.jar"

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
          Library.Play.playGuard,
          Library.Play.json,
          Library.Play.jsonJoda,
          Library.Play.test,
          Library.Play.Silhouette.passwordBcrypt,
          Library.Play.Silhouette.persistence,
          Library.Play.Silhouette.cryptoJca,
          Library.Play.Silhouette.silhouette,
          Library.Play.Jwt.atlassianJwtCore,
          Library.Play.Jwt.bouncyCastlePkix,
          Library.Backend.logPlay,
          Library.Backend.redisCache,
          Library.HATDeX.dexClient,
          Library.HATDeX.codegen,
          Library.Utils.awsJavaS3Sdk,
          Library.Utils.awsJavaSesSdk,
          Library.Utils.awsJavaLambdaSdk,
          Library.Utils.prettyTime,
          Library.Utils.nbvcxz,
          Library.Utils.alpakkaAwsLambda,
          Library.scalaGuice,
          Library.circeConfig,
          Library.ContractLibrary.adjudicator,
          Library.Utils.apacheCommonLang,
          Library.Prometheus.filters,
          Library.janino
        ),
    libraryDependencies := (buildEnv.value match {
          case BuildEnv.Developement | BuildEnv.Test =>
            libraryDependencies.value ++ Seq(
                  Library.Play.Silhouette.silhouetteTestkit,
                  Library.ScalaTest.scalaplaytestmock,
                  Library.Dataswift.integrationTestCommon,
                  Library.ScalaTest.mockitoCore
                )
          case BuildEnv.Stage | BuildEnv.Production =>
            libraryDependencies.value
        }),
    Test / parallelExecution := false,
    Assets / pipelineStages := Seq(digest),
    Assets / sourceDirectory := baseDirectory.value / "app" / "org" / "hatdex" / "hat" / "phata" / "assets",
    update / aggregate := false,
    Global / cancelable := false, // Workaround sbt/bug#4822 Unable to Ctrl-C out of 'run' in a Play app
    TwirlKeys.templateImports := Seq(),
    play.sbt.routes.RoutesKeys.routesImport := Seq.empty,
    routesGenerator := InjectedRoutesGenerator,
    Global / concurrentRestrictions += Tags.limit(Tags.Test, 1),
    coverageExcludedPackages := """.*\.controllers\..*Reverse.*;router.Routes.*;org.hatdex.hat.dal.Tables.*;org.hatdex.hat.phata.views.*;controllers.javascript\..*""",
    // Do not publish docs and source in compiled package
    Compile / doc / sources := Seq.empty,
    Compile / packageDoc / publishArtifact := false,
    Compile / packageSrc / publishArtifact := false
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(AshScriptPlugin)
  .settings(
    // Use the alternative "Ash" script for running compiled project form inside Alpine-derived container
    // as Bash is incompatible with Alpine
    Universal / javaOptions ++= Seq(),
    Docker / packageName := "hat",
    dockerEnvVars := Map("REDIS_CACHE_KEY_PREFIX" -> s"hat:${version.value}"),
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
    Universal / javaOptions ++= (buildEnv.value match {
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
    gentables / codegenPackageName := "org.hatdex.hat.dal",
    gentables / codegenBaseDir := (baseDirectory.value / "app").getCanonicalPath,
    gentables / codegenClassName := "Tables",
    gentables / codegenExcludedTables := Seq("databasechangelog", "databasechangeloglock"),
    gentables / codegenDatabase := "devdb",
    gentables / codegenConfig := "dev.conf",
    gentables / codegenEvolutions := "devhatMigrations"
  )
  .settings(
    // Omit Tables.scala for scalafix, since it is autogenerated
    Compile / scalafix / unmanagedSources := (Compile / unmanagedSources).value
          .filterNot(
            _.getAbsolutePath.contains(
              "dal/Tables.scala"
            )
          ),
    // Omit Tables.scala for scalafmt, since it is autogenerated
    Compile / scalafmt / unmanagedSources := (Compile / unmanagedSources).value
          .filterNot(
            _.getAbsolutePath.contains(
              "dal/Tables.scala"
            )
          )
  )

// Enable the semantic DB for scalafix
inThisBuild(
  List(
    scalaVersion := "2.13.5",
    scalafixScalaBinaryVersion := "2.13",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
  )
)
