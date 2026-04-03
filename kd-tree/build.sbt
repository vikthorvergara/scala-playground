val scala3Version = "3.8.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-playground",
    version := "0.1.0",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.1.0" % Test
  )
