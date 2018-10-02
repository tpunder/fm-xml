FMPublic

name := "fm-xml"

description := "XML utilities"

scalaVersion := "2.12.7"

crossScalaVersions := Seq("2.11.11", "2.12.7")

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
  "com.frugalmechanic" %% "fm-common" % "0.26.0",
  "com.frugalmechanic" %% "fm-lazyseq" % "0.10.0",
  "com.frugalmechanic" %% "scala-optparse" % "1.1.2",
  "com.fasterxml.woodstox" % "woodstox-core" % "5.1.0",
  "com.sun.xml.bind" % "jaxb-core" % "2.3.0.1", // JAXB - Needed for Java 9+ since it is no longer automatically available
  "com.sun.xml.bind" % "jaxb-impl" % "2.3.0.1", // JAXB - Needed for Java 9+ since it is no longer automatically available
  "javax.xml.bind" % "jaxb-api" % "2.3.0", // JAXB - Needed for Java 9+ since it is no longer automatically available
  "javax.activation" % "javax.activation-api" % "1.2.0", // JAXB - Needed for Java 9+ since it is no longer automatically available
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
