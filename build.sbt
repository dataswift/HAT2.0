import Dependencies.Library
import com.typesafe.sbt.packager.docker._
import sbt.Keys._

val codeguruURI =
  "https://repo1.maven.org/maven2/software/amazon/codeguruprofiler/codeguru-profiler-java-agent-standalone/1.1.0/codeguru-profiler-java-agent-standalone-1.1.0.jar"

lazy val dockerSettings = Seq(
  Universal / javaOptions += "-Dpidfile.path=/dev/null",
  Docker / version := version.value,
  dockerCommands := Seq(
    Cmd(IO.read(file("Dockerfile"))),
    Cmd("CMD", s"./${packageName.value}"),
    Cmd("COPY", "--chown=daemon:daemon", "1/opt", "opt", "/opt/")
  )
)

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
          Library.HATDeX.dexClient,
          Library.HATDeX.codegen,
          Library.Utils.awsJavaS3Sdk,
          Library.Utils.awsJavaSesSdk,
          Library.Utils.awsJavaLambdaSdk,
          Library.Utils.prettyTime,
          Library.Utils.nbvcxz,
          Library.Utils.alpakkaAwsLambda,
          Library.Utils.playMemcached % Runtime,
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
  .settings(dockerSettings)
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
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
  )
)
