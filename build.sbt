ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

lazy val root = (project in file("."))
  .settings(
    name := "ScalaCatsEffectsProjects"
  )

libraryDependencies ++= Seq(

  // CATS
  "org.typelevel" %% "cats-core" % "2.7.0",
  "org.typelevel" %% "cats-kernel" % "2.7.0",

  // CATS-EFFECTS
  "org.typelevel" %% "cats-effect" % "3.3.12",
  //  "co.fs2" %% "fs2-core" % "3.2.8",

  // LOGGING
  "org.typelevel" %% "log4cats-core"    % "2.4.0",  // Only if you want to Support Any Backend
  "org.typelevel" %% "log4cats-slf4j"   % "2.4.0",  // Direct Slf4j Support - Recommended


)
ThisBuild / scalacOptions ++= Seq(
  "-Ykind-projector"
)