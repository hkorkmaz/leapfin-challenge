import sbt.Keys._

name := "leapfin"

version := "0.1"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.21"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion, //Akka Core

  //Tests
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

assemblyJarName in assembly := "finleap.jar"
target in assembly := new File("dist")
mainClass in assembly := Some("finleap.Main")