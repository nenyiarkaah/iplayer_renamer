name := "iplayer_renamer"

version := "1.0"

scalaVersion := "2.12.1"


libraryDependencies += "org" % "jaudiotagger" % "2.0.3"
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.3"
resolvers += "OSS Sonatype" at "https://repo1.maven.org/maven2/"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
libraryDependencies += "commons-io" % "commons-io" % "2.5"