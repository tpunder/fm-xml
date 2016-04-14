FMPublic

name := "fm-xml"

version := "0.3.0-SNAPSHOT"

description := "XML utilities"

scalaVersion := "2.11.8"

// Note: Use "++ 2.11.7" to select a specific version when building
crossScalaVersions := Seq("2.10.6", "2.11.8")

scalacOptions := Seq("-unchecked", "-deprecation", "-language:implicitConversions", "-feature", "-Xlint", "-optimise", "-Yinline-warnings")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
  "com.frugalmechanic" %% "fm-common" % "0.4.0-SNAPSHOT",
  "com.frugalmechanic" %% "fm-lazyseq" % "0.4.0-SNAPSHOT",
  "com.frugalmechanic" %% "scala-optparse" % "1.1.2",
  "org.codehaus.woodstox" % "woodstox-core-asl" % "4.4.1",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
