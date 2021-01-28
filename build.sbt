import java.nio.file.{Files, StandardCopyOption}
import scala.sys.process._
import xsbt.Log

name := "lhlExp"
description := "罗华林的毕业实验"
version := "1.0"
fork in run := true
mainClass in (Compile, run) := Some("RunnableApp")
scalaVersion := "2.13.1"
javacOptions := Seq("-target", "1.8")

// https://mvnrepository.com/artifact/org.scalafx/scalafx
libraryDependencies += "org.scalafx" %% "scalafx" % "12.0.2-R18"
// https://mvnrepository.com/artifact/org.scalatest/scalatest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % Test
// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"
// https://mvnrepository.com/artifact/ch.qos.logback/logback-core
libraryDependencies += "ch.qos.logback" % "logback-core" % "1.2.3"
// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
// https://mvnrepository.com/artifact/com.typesafe.play/play-json
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.8.1"
// https://mvnrepository.com/artifact/commons-io/commons-io
libraryDependencies += "commons-io" % "commons-io" % "2.8.0"
// https://mvnrepository.com/artifact/com.typesafe/config
libraryDependencies += "com.typesafe" % "config" % "1.4.0"
libraryDependencies += "com.iheart" %% "ficus" % "1.5.0"


mainClass in assembly := Some("RunnableApp")
assemblyMergeStrategy in assembly := {
    case manifest if manifest.contains("MANIFEST.MF") =>
      MergeStrategy.discard
    case moduleInfo if moduleInfo.contains("module-info.class") =>
      MergeStrategy.discard
    case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
      MergeStrategy.concat
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
}
