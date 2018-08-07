name := "akka-http-client"

organization := "com.sinan"
version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.0"
lazy val akkaHttpVersion = "10.1.1"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.2",
  "com.typesafe.akka" %% "akka-actor"   % akkaVersion,
  "com.typesafe.akka" %% "akka-stream"  % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-http"    % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
