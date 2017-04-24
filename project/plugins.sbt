logLevel := Level.Warn

resolvers += Resolver.typesafeRepo("releases")

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.4")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.1.0")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.5.1")

addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.1.1")

addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.5")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")


// web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "1.0.3")
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.6")

// S3 based SBT resolver
resolvers += "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com"

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.15.0")
