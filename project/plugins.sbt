resolvers += "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"

addSbtPlugin("com.typesafe.sbt"  % "sbt-native-packager" % "1.8.1")
addSbtPlugin("com.typesafe.play" % "sbt-plugin"          % "2.8.7")

// Code Quality
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.7")

// web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-web"     % "1.4.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-digest"  % "1.1.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip"    % "1.0.2")
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.5.1")

addSbtPlugin("org.hatdex" % "sbt-slick-postgres-generator" % "0.1.2")

// run "sbt dependencyUpdates" to check maven for updates or "sbt ";dependencyUpdates; reload plugins; dependencyUpdates" for sbt plugins
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.2")

// ScalaFMT, ScalaFIX and Tools Common
addSbtPlugin("org.scalameta" % "sbt-scalafmt"          % "2.4.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix"          % "0.9.26")
addSbtPlugin("io.dataswift"  % "sbt-scalatools-common" % "0.2.4")

addDependencyTreePlugin
