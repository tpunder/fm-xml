FMPublic

name := "fm-xml"

description := "XML utilities"

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.11.11", "2.12.6")

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-language:implicitConversions",
  "-feature",
  "-Xlint",
  "-Ywarn-unused-import"
) ++ (if (scalaVersion.value.startsWith("2.12")) Seq(
  // Scala 2.12 specific compiler flags
  "-opt:l:inline",
  "-opt-inline-from:<sources>"
) else Nil)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
  "com.frugalmechanic" %% "fm-common" % "0.17.0",
  "com.frugalmechanic" %% "fm-lazyseq" % "0.9.0",
  "com.frugalmechanic" %% "scala-optparse" % "1.1.2",
  "com.fasterxml.woodstox" % "woodstox-core" % "5.0.2",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)
