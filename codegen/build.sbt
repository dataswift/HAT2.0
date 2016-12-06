import Dependencies._

libraryDependencies ++= Seq(
  Library.Db.postgres,
  Library.Db.hikariCP,
  Library.Slick.slickPgCore,
  Library.Slick.slickPg,
  Library.Slick.slickPgJoda,
  Library.Slick.slickPgJts,
  Library.Slick.slickPgPlayJson,
  Library.Slick.slickCodegen,
  Library.Akka.slf4j,
  Library.Akka.httpCore,
  Library.Akka.akkaStream,
  Library.Akka.akkaHttpSprayJson,
  Library.Utils.jodaTime,
  Library.Utils.jodaConvert,
  Library.Utils.jts,
  Library.Utils.slf4j,
  Library.Utils.logbackCore,
  Library.Utils.logbackClassic,

  Library.Slick.slickPgCore,
  Library.Slick.slickPg,
  Library.Slick.slickPgJoda,
  Library.Slick.slickPgJts,
  Library.Slick.slickPgSprayJson,
  Library.Slick.slickCodegen,
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
  Library.Specs2.mock
)

lazy val gentables = taskKey[Seq[File]]("Slick Code generation")

gentables := {
  val main = Project("root", file("."))
  val outputDir = (main.base.getAbsoluteFile / "hat/app/org").getPath
  streams.value.log.info("Output directory for codegen: " + outputDir.toString)
  val pkg = "org.hatdex.hat.dal"
  streams.value.log.info("Dependency classpath: " + dependencyClasspath.toString)
  (runner in Compile).value.run("org.hatdex.hat.dal.CustomizedCodeGenerator", (dependencyClasspath in Compile).value.files, Array(outputDir, pkg), streams.value.log)
  val fname = outputDir + "/" + pkg.replace('.', '/') + "/Tables.scala"
  Seq(file(fname))
}
