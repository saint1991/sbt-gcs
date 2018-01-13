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

package com.github.saint1991.sbt.gcs

import java.io.{BufferedInputStream, BufferedOutputStream, FileInputStream, FileOutputStream}
import java.nio.ByteBuffer
import java.nio.file.Files

import scala.concurrent._
import ExecutionContext.Implicits.global

import sbt._
import com.google.cloud.storage.{BlobId, BlobInfo, Storage}


object Gcs {

  private final val BufferSize = 4096

  def upload(storage: Storage)(src: File, dest: GcsObjectUrl): Future[(File, GcsObjectUrl)] = Future {

    val blobInfo = BlobInfo
      .newBuilder(dest.bucket, dest.prefix)
      .setContentType(Files.probeContentType(src.toPath) match {
        case null => "application/octet-stream"
        case str => str
      })
      .build()

    using(new BufferedInputStream(new FileInputStream(src))) { fstream =>
      using(storage.writer(blobInfo)) { blob =>
        var len = 0
        val buf = new Array[Byte](BufferSize)
        while ({ len = fstream.read(buf); len > 0 }) blob.write(ByteBuffer.wrap(buf, 0, len))
      }
    }

    (src, dest)
  }


  def download(storage: Storage)(src: GcsObjectUrl, dest: File): Future[(GcsObjectUrl, File)] = Future {

    using(storage.reader(src.bucket, src.prefix)) { blob =>
      using(new BufferedOutputStream(new FileOutputStream(dest))) { fstream =>
        var len = 0
        val buf = ByteBuffer.wrap(new Array[Byte](BufferSize))
        while ({ len = blob.read(buf); len > 0 }) {
          fstream.write(buf.array(), buf.arrayOffset(), len)
          buf.clear()
        }
        fstream.flush()
      }
    }

    (src, dest)
  }


  def delete(storage: Storage)(target: GcsObjectUrl): Future[Option[GcsObjectUrl]] = Future {
    if (storage.delete(BlobId.of(target.bucket, target.prefix))) Some(target)
    else None
  }

}
