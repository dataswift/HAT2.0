resolvers += "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
addSbtPlugin("com.typesafe.sbt" % "sbt-digest"                   % "1.1.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip"                     % "1.0.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-web"                      % "1.4.4")
addSbtPlugin("io.dataswift"     % "sbt-scalatools-common"        % "0.5.0")
addSbtPlugin("org.hatdex"       % "sbt-slick-postgres-generator" % "0.1.2")
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify"                  % "1.5.1")
addSbtPlugin("org.scoverage"    % "sbt-coveralls"                % "1.2.7")
addSbtPlugin("org.scoverage"    % "sbt-scoverage"                % "1.8.2")
