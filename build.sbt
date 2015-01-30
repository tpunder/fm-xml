FMPublic

name := "fm-xml"

version := "0.1.0"

description := "XML utilities"

scalaVersion := "2.11.5"

// Note: Use "++ 2.11.5" to select a specific version when building
crossScalaVersions := Seq("2.10.4", "2.11.5")

scalacOptions := Seq("-unchecked", "-deprecation", "-language:implicitConversions", "-feature", "-Xlint", "-optimise", "-Yinline-warnings")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

libraryDependencies ++= Seq(
  "com.frugalmechanic" %% "fm-common" % "0.2.0",
  "com.frugalmechanic" %% "fm-lazyseq" % "0.2.0",
  "com.frugalmechanic" %% "scala-optparse" % "1.1.1",
  "org.codehaus.woodstox" % "woodstox-core-asl" % "4.4.0",
  "org.scalatest" %% "scalatest" % "2.1.3" % "test"
)
