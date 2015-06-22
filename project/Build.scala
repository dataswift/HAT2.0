import sbt.Keys._
import sbt._
//import demo.CustomizedCodeGenerator


/**
 * This is a slightly more advanced sbt setup using two projects.
 * The first one, "codegen" a customized version of Slick's
 * code-generator. The second one "main" depends on "codegen", which means
 * it is compiled after "codegen". "main" uses the customized
 * code-generator from project "codegen" as a sourceGenerator, which is run
 * to generate Slick code, before the code in project "main" is compiled.
 */
object myBuild extends Build {
  /** main project containing main source code depending on slick and codegen project */

  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  val specs2V = "3.3"

  lazy val mainProject = Project(
    id="main",
    base=file("."),
    settings = sharedSettings ++ Seq(
      libraryDependencies ++= List(
        "io.spray"            %%  "spray-can"     % sprayV,
        "io.spray"            %%  "spray-routing-shapeless2" % sprayV,
        "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
        "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
        "io.spray"      %%  "spray-testkit" % sprayV  % "test",
        "org.specs2" % "specs2-core_2.11" % "3.3",
        "org.specs2" % "specs2_2.11" % "3.3",
        "com.gettyimages" %% "spray-swagger" % "0.5.1",
        "io.spray" %%  "spray-json" % "1.3.2"
      ),
      slick <<= slickCodeGenTask // register manual sbt command
      // sourceGenerators in Compile <+= slickCodeGenTask, // register automatic code generation on every compile, remove for only manual use
//      cleanFiles <+= baseDirectory { base => base / "src/main/scala/dal/" }
//      watchSources <++= baseDirectory map { path => ((path / "src/main/scala/dalapi/") ** "*.scala").get }
    )
  ).dependsOn( codegenProject )

  lazy val codegenProject = Project(
    id="codegen",
    base=file("codegen"),
    settings = sharedSettings ++ Seq(
      libraryDependencies ++= List(
        "com.typesafe.slick" %% "slick-codegen" % "3.0.0"
      )
    )
  )

  val sharedSettings = Project.defaultSettings ++ Seq(
    scalaVersion := "2.11.6",
    libraryDependencies ++= List(
      "com.typesafe.slick" %% "slick" % "3.0.0",
      "com.github.tminglei" % "slick-pg_core_2.11" % "0.9.0",
      "com.github.tminglei" %% "slick-pg" % "0.9.0",
      "com.github.tminglei" %% "slick-pg_joda-time" % "0.6.5.3",
      "com.github.tminglei" %% "slick-pg_jts" % "0.6.5.3",
      "joda-time" % "joda-time" % "2.7",
      "org.joda" % "joda-convert" % "1.7",
      "com.vividsolutions" % "jts" % "1.13",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe" % "config" % "1.3.0",
      "com.zaxxer" % "HikariCP" % "2.3.8"
    ),
    resolvers ++= List("Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases")
  )

  // code generation task
  lazy val slick = TaskKey[Seq[File]]("gen-tables")

  lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
    val main = Project("main", file("."))
    val outputDir = (main.base.getAbsoluteFile / "src/main/scala").getPath
    s.log.info("Output directory for codegen: " + outputDir.toString)
    val pkg = "dal"
    toError(r.run("autodal.CustomizedCodeGenerator", cp.files, Array(outputDir, pkg), s.log))
    val fname = outputDir + "/" + pkg + "/Tables.scala"
    Seq(file(fname))
  }

}
