import java.nio.file.{Files, StandardCopyOption}
import scala.sys.process._
import xsbt.Log

name := "lhlExp"
description := "罗华林的毕业实验"
version := "1.1"
fork in run := true
mainClass in (Compile, run) := Some("com.mazhangjing.lhl.RunnableApp")
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


mainClass in assembly := Some("com.mazhangjing.lhl.RunnableApp")
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

lazy val winpkg = taskKey[Unit]("jar 打包 exe")
winpkg := {
  //确保 launchPath 包含 launch4j 安装程序
  //确保 target/scala-2.13 生成的 jar 文件名称和 package.xml 文件内对应，确保 package.xml 中 output 文件夹存在且具有 JRE
  val launchPath = "C:\\Program Files (x86)\\Launch4j"
  val pkg_config = "C:\\Users\\Corkine\\Desktop\\lhlExp\\target\\scala-2.13\\package.xml"
  val output = "C:\\Users\\Corkine\\Desktop\\lhlExperiment_output"
  println(s"cmd /c C: && cd $launchPath && .\\launch4j.jar $pkg_config")
  val ans = s"cmd /c C: && cd $launchPath && .\\launch4j.jar $pkg_config && explorer $output".!!
  println(ans)
}


//Use assembly jar for Universal executable
//For Windows Package, Use launch4j Wrapper, launch4j.jar -> load config -> wrap it.
//For MacOS Package, Use jar2app: jar2app lhlExp-assembly-1.0.jar -n "Psy4J App" -r /Library/Java/JavaVirtualMachines/jdk1.8.0_261.jdk main.app
//Note: The work dir is User's home, data and config should set there.

/*
enablePlugins(WindowsPlugin)
// general package information (can be scoped to Windows)
maintainer := "Corkine Ma <corkine@outlook.com>"
packageSummary := "LHLExperiment"
packageDescription := """LHLExperiment v0.0.1"""

// wix build information
wixProductId := "48c5089b-c8d9-4f7c-ad72-e7ad7963cce2"
wixProductUpgradeId := "673cc705-76a9-47ac-94a7-ce5c1c8f8873"*/

//For Mac Package, Use
/*
enablePlugins(JDKPackagerPlugin)
lazy val iconGlob = sys.props("os.name").toLowerCase match {
  case os if os.contains("mac") => "*.icns"
  case os if os.contains("win") => "*.ico"
  case _ => "*.png"
}
jdkPackagerJVMArgs := Seq("-Xmx1g")
maintainer := "CorkineMa"
packageSummary := "LHLExperiment Powered by Psy4J"
packageDescription := "LHLExperiment Powered by Psy4J"
jdkPackagerProperties := Map("app.name" -> name.value, "app.version" -> version.value)
jdkPackagerAppArgs := Seq(maintainer.value, packageSummary.value, packageDescription.value)
jdkPackagerType := "image"
(antPackagerTasks in JDKPackager) := (antPackagerTasks in JDKPackager).value orElse {
  for {
    f <- Some(file("/Library/Java/JavaVirtualMachines/jdk1.8.0_261.jdk/Contents/Home/lib/ant-javafx.jar")) if f.exists()
  } yield f
}*/

