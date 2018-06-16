resolvers += Resolver.sonatypeRepo("public")

scalacOptions := Seq("-feature", "-deprecation")

scalaVersion := "2.12.6"

libraryDependencies += "net.bzzt" %% "scala-icalendar" % "0.0.1-SNAPSHOT"
libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "10.1.3"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.13"
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-json-streaming" % "0.19"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
