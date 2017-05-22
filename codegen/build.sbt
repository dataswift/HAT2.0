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

