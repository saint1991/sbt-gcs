
name := "sbt-gcs-example"

version := "0.2"

scalaVersion := "2.12.4"

mainClass in assembly := Some("com.github.saint1991.sbt.gcs.examples.Example")

gcsChunkSize := 8192

// upload a build artifact created by sbt-assembly with progress reporting.
mappings in gcsUpload := Seq(
  assembly.value -> "gs://example-bucket/artifact.jar"
)
gcsProgress in gcsUpload := true

// download 2 text files with progress reporting.
mappings in gcsDownload := Seq(
  (Path.userHome / "Downloads" / "downloaded.txt", "gs://example-bucket/to-download.txt"),
  (Path.userHome / "Downloads" / "downloaded2.txt","gs://example-bucket/to-download2.txt")
)
gcsProgress in gcsDownload := true

// delete 2 text files.
gcsUrls in gcsDelete := Seq(
  "gs://example-bucket/to_delete1.txt",
  "gs://example-bucket/to-delete2.txt"
)
