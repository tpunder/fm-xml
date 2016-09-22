FMPublic

name := "fm-xml"

version := "0.4.0-SNAPSHOT"

description := "XML utilities"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-language:implicitConversions", "-feature", "-Xlint", "-optimise", "-Yinline-warnings")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
  "com.frugalmechanic" %% "fm-common" % "0.7.0-SNAPSHOT",
  "com.frugalmechanic" %% "fm-lazyseq" % "0.5.0-SNAPSHOT",
  "com.frugalmechanic" %% "scala-optparse" % "1.1.2",
  "com.fasterxml.woodstox" % "woodstox-core" % "5.0.2",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
