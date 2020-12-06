addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.6.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
