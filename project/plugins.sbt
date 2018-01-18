addSbtPlugin("de.heikoseeberger" % "sbt-header" % "4.1.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
