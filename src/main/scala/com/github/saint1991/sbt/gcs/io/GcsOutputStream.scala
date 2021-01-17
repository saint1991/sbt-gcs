package com.github.saint1991.sbt.gcs.io

import com.google.cloud.WriteChannel

import java.io.OutputStream
import java.nio.ByteBuffer

/**
 * Wrapper of WriteChannel to handle it as OutputStream.
 * @param out WriteChannel of Google Cloud Storage
 */
class GcsOutputStream(private val out: WriteChannel) extends OutputStream {
  override def close(): Unit = if (out != null) out.close()

  override def write(b: Int): Unit = out.write(
    ByteBuffer.wrap(Array[Byte](b.toByte))
  )

  override def write(b: Array[Byte], off: Int, len: Int): Unit = out.write(
    ByteBuffer.wrap(b, off, len)
  )
}
