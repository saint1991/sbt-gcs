package com.github.saint1991.sbt.gcs.io

import com.google.cloud.ReadChannel

import java.io.{Closeable, InputStream}
import java.nio.ByteBuffer

/**
 * Wrapper of ReadChannel to handle it as InputStream.
 * @param in ReadChannel of Google Cloud Storage
 */
class GcsInputStream(private val in: ReadChannel) extends InputStream with Closeable {

  override def close(): Unit = if (in != null) in.close()

  override def read(): Int = {
    val buf = ByteBuffer.allocate(1)
    if (in.read(buf) == -1) -1
    else buf.get(0).toInt
  }

  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    val buf = ByteBuffer.wrap(b, off, len)
    val readBytes = in.read(buf)
    readBytes
  }
}
