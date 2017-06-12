import sbt._

name := "db"

organization := "io.shaka"

scalaVersion := "2.12.0"

val circeVersion = "0.6.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
 ) ++ Seq(
  "io.circe" %% "circe-core" % "provided",
  "io.circe" %% "circe-generic" % "provided",
  "io.circe" %% "circe-parser"  % "provided"
).map(_ % circeVersion)



