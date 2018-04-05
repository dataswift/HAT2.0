logLevel := Level.Warn

resolvers += Resolver.typesafeRepo("releases")

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.1")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.12")

// Code Quality
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.2")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.1")
// run "sbt dependencyUpdates" to check maven for updates or "sbt ";dependencyUpdates; reload plugins; dependencyUpdates" for sbt plugins
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4")


// web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.11")

// S3 based SBT resolver

resolvers += "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-snapshots.hubofallthings.com"

addSbtPlugin("org.hatdex" % "sbt-slick-postgres-generator" % "0.0.7-SNAPSHOT")
