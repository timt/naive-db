import sbt._

import scala.Some
import scala.util.Try
import bintray.Keys._

name := "naive-db"

organization := "io.shaka"

version := Try(sys.env("LIB_VERSION")).getOrElse("1")

scalaVersion := "2.12.2"

val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % circeVersion % "provided",
  "io.circe" %% "circe-generic" % circeVersion % "provided",
  "io.circe" %% "circe-parser" % circeVersion % "provided",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

pgpPassphrase := Some(Try(sys.env("SECRET")).getOrElse("goaway").toCharArray)

pgpSecretRing := file("./publish/sonatype.asc")

bintrayPublishSettings

repository in bintray := "repo"

bintrayOrganization in bintray := None

publishMavenStyle := true

publishArtifact in Test := false

homepage := Some(url("https://github.com/timt/naive-db"))

licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

pomExtra :=
  <scm>
    <url>git@github.com:timt/naive-db.git</url>
    <connection>scm:git:git@github.com:timt/naive-db.git</connection>
  </scm>
    <developers>
      <developer>
        <id>timt</id>
      </developer>
    </developers>




