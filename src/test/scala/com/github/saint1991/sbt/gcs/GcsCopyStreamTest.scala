package com.github.saint1991.sbt.gcs

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

import org.scalatest.{Matchers, WordSpec}
import monix.execution.Scheduler.Implicits.global

object GcsCopyStreamTest {
  final val sampleText = "sbt-gcs is a simple sbt plugin to manipulate objects on Google Cloud Storage."
  final val chunkSize = 12
}

class GcsCopyStreamTest extends WordSpec with Matchers {

  import GcsCopyStreamTest._

  "GcsTasks#copyStream" should {
    "copy exactly the same bytes from the given InputStream to the OutputStream" in {
      val in = new ByteArrayInputStream(sampleText.getBytes("UTF-8"))
      val out = new ByteArrayOutputStream(chunkSize)

      Await.result(
        GcsTasks.copyStream(GcsTaskConfig(chunkSize = chunkSize))(in, out).foreach(_ => ()),
        1 minute
      )
      out.toString("UTF-8") should equal (sampleText)
    }
  }
}
