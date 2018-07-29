import sbt.Keys._
import sbt._

name := "aws-lambda-scala-handler"

organization := "org.hatdex"
version := "0.0.2-SNAPSHOT"
name := "AWS Lambda Scala Handler"
description := "Scala utility wrappers for integrating with AWS Lambda functions"
licenses += ("Mozilla Public License 2.0", url("https://www.mozilla.org/en-US/MPL/2.0"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/Hub-of-all-Things/aws-lambda-scala-handler"),
    "scm:git@github.com:Hub-of-all-Things/aws-lambda-scala-handler.git"
  )
)
homepage := Some(url("https://hubofallthings.com"))

developers := List(
  Developer(
    id    = "AndriusA",
    name  = "Andrius Aucinas",
    email = "andrius@smart-e.org",
    url   = url("http://smart-e.org")
  )
)

resolvers += Resolver.sonatypeRepo("public")
scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-events" % "1.3.0",
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
  "io.symphonia" % "lambda-logging" % "1.0.0",
  "com.typesafe.play" %% "play-json" % "2.6.8",
  "org.specs2" %% "specs2-core" % "4.0.0" % "provided",
  "org.specs2" %% "specs2-matcher-extra" % "4.0.0" % "provided",
  "org.specs2" %% "specs2-mock" % "4.0.0" % "provided"
)

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-language:postfixOps", // Allow postfix operators
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings")

publishTo := {
  val prefix = if (isSnapshot.value) "snapshots" else "releases"
  Some(s3resolver.value("HAT Library Artifacts " + prefix, s3("library-artifacts-" + prefix + ".hubofallthings.com")) withMavenPatterns)
}
