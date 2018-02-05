val pluginVersion = sys.props
  .get("plugin.version")
  .getOrElse(sys.error("Sys prop plugin.version must be defined!"))

addSbtPlugin("com.github.saint1991" % "sbt-gcs" % pluginVersion)
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")
