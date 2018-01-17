
lazy val root = (project in file(".")).
  settings(
    name := "sbt-gcs",
    organization := "com.github.saint1991",
    organizationName := "saint1991",
    description := "GCS Plugin for sbt",
    sbtPlugin := true,
    scalaVersion := "2.12.4",
    crossSbtVersions := Seq("0.13.16", "1.1.0"),
    libraryDependencies ++= Seq(
      "com.google.cloud" % "google-cloud-storage" % "1.14.0",
      "org.scalatest" %% "scalatest" % "3.0.4" % Test
    ),
    mainClass := Some("com.github.saint1991.sbt.gcs.Main")
  ).
  settings(
    publishMavenStyle := false,
    startYear := Some(2018),
    licenses += ("Apache-2.0", new URL("http://www.apache.org/licenses/LICENSE-2.0")),
    bintrayRepository := "sbt-plugins",
    bintrayOrganization in bintray := None
  )

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", s"-Dplugin.version=${version.value}")
}
scriptedBufferLog := false
