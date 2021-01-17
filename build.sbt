val googleCloudVersion = "1.113.9"
val monixVersion = "3.3.0"
val scalaTestVersion = "3.2.3"

lazy val root = (project in file("."))
  .enablePlugins(GitVersioning, SbtPlugin)
  .settings(
    name := "sbt-gcs",
    organization := "com.github.saint1991",
    organizationName := "saint1991",
    description := "GCS Plugin for sbt",
    sbtPlugin := true,
    scalaVersion := "2.12.13",
    sbtVersion := "1.4.6",
    libraryDependencies ++= Seq(
      "com.google.cloud" % "google-cloud-storage" % googleCloudVersion,
      "io.monix" %% "monix-reactive" % monixVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    ),
    scriptedBufferLog := false,
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++ Seq(s"-Dplugin.version=${version.value}")
    }
  )
  .settings(
    publishMavenStyle := false,
    startYear := Some(2018),
    licenses += ("Apache-2.0", new URL("http://www.apache.org/licenses/LICENSE-2.0")),
    bintrayRepository := "sbt-plugins",
  )
