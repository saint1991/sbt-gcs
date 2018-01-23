/*
 * Copyright 2018 saint1991
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.saint1991.sbt

import java.io.{InputStream, OutputStream}
import java.nio.ByteBuffer

import scala.language.reflectiveCalls
import scala.util.control.Exception._

import com.google.cloud.{ReadChannel, WriteChannel}
import com.google.cloud.storage.{Storage, StorageOptions}

package object gcs {

  type Credentials = com.google.auth.Credentials
  type Parallelism = Int
  type TaskIndex = Int
  type ChunkSize = Int
  type ReportProgress = Boolean

  /**
    * Utility for Load Pattern
    * @param resource closable resource
    * @param f operation using the resource
    * @tparam Return type of result of f
    * @tparam Resource type of closable Resource
    * @return
    */
  private [gcs] def using[
    Return,
    Resource <: { def close(): Unit }
  ](resource: Resource)(f: Resource => Return): Return = allCatch andFinally {
    if (resource != null) resource.close()
  } apply f(resource)


  object Converters {

    /**
      * Wrapper of ReadChannel to handle it as InputStream
      * @param in ReadChannel from GCS
      */
    class GcsInputStream(private val in: ReadChannel) extends InputStream {

      override def close(): Unit = if (in != null) in.close()

      override def read(): Int = {
        val buf = ByteBuffer.wrap(new Array[Byte](1))
        if (in.read(buf) == -1) -1
        else buf.get(0).toInt
      }

      override def read(b: Array[Byte], off: Int, len: Int): Int = {
        val buf = ByteBuffer.wrap(new Array[Byte](len))
        val readBytes = in.read(buf)
        buf.array().copyToArray(b, off, readBytes)
        readBytes
      }
    }

    /**
      * Wrapper of WriteChannel to handle it as OutputStream
      * @param out WriteChannel to GCS
      */
    class GcsOutputStream(private val out: WriteChannel) extends OutputStream {
      override def close(): Unit = if(out != null) out.close()
      override def write(b: Int): Unit = out.write(ByteBuffer.wrap(Array[Byte](b.toByte)))
      override def write(b: Array[Byte], off: Int, len: Int): Unit = out.write(ByteBuffer.wrap(b, off, len))
    }


    implicit class GcsReadChannel(val self: ReadChannel) extends AnyVal {
      def asInputStream = new GcsInputStream(self)
    }

    implicit class GcsWriteChannel(val self: WriteChannel) extends AnyVal {
      def asOutputStream = new GcsOutputStream(self)
    }
  }

  /**
    * Storage mixin according to credential.
    */
  trait StorageProvider {
    protected val credential: Option[Credentials]
    lazy val storage: Storage = getStorage(credential)
  }

  def getStorage(credential: Option[Credentials]): Storage = (credential match {
    case Some(c) => StorageOptions.newBuilder().setCredentials(c).build()
    case None => StorageOptions.getDefaultInstance
  }).getService

}
