package com.github.saint1991.sbt.gcs

import org.scalatest._

class GcsObjectUrlTest extends WordSpec with Matchers {

  "GcsObjectUrl instance" when {

    "URL is valid" should {
      "be constructed correctly" in {
        val url = GcsObjectUrl("gs://bucket/path/to/object.json")
        url.bucket should equal ("bucket")
        url.prefix should equal ("path/to/object.json")

        val url2 = GcsObjectUrl(url.bucket, url.prefix)
        url should equal (url2)
      }
    }

    "URL is invalid" should {
      "throw IllegalArgumentException" in {
        an [IllegalArgumentException] should be thrownBy GcsObjectUrl("http://bucket/path/to/object.json")
        an [IllegalArgumentException] should be thrownBy GcsObjectUrl("bucke+t_.", "path/to/object.json")
      }
    }

  }
}
