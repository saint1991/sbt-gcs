# sbt-gcs

sbt-gcs is a simple sbt plugin that can manipulate objects on Google Cloud Storage.


## Install

As a prerequisite, sbt 0.13.16 or newer is required.  
In order to add the sbt-gcs plugin to your build, add the following to `project/plugins.sbt`.


```project/plugins.sbt
addSbtPlugin("com.github.saint1991" % "sbt-gcs" % "0.1.0-SNAPSHOT")
```

This plugin automatically add some tasks to manipulate Google Cloud Storage.

## Usage

