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

import monix.eval.Task

import sbt._
import com.google.cloud.storage.{BlobId, BlobInfo, Storage}

/**
  * Basic operations against Google Cloud Storage
  */
object GcsTasks {

  protected final val BufferSize = 4096

  /**
    * Upload *src* file to the *dest* URL on Google Cloud Storage.
    * This method is executed asynchronously.
    * @param storage Storage object
    * @param src a file to upload
    * @param dest a destination URL to which upload a file
    * @return the tuple of completed upload pair (src, dest) as a Future
    */
  def upload(storage: Storage, src: File, dest: GcsObjectUrl): Task[(File, GcsObjectUrl)] = Task {

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

  }.memoizeOnSuccess

  /**
    * Download *src* object from Google Cloud Storage as the *src* file.
    * This method is executed asynchronously.
    * @param storage Storage object
    * @param src the URL of an object to download
    * @param dest a destination file
    * @return the tuple of completed download pair (src, dest) as a Future
    */
  def download(storage: Storage, src: GcsObjectUrl, dest: File): Task[(GcsObjectUrl, File)] = Task {

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

  }.memoizeOnSuccess


  /**
    * Delete *target* object from Google Cloud Storage.
    * @param storage Storage object
    * @param target the URL of an object to delete
    * @return Right of URL that is completed deleting or Left of URL if the object does not exist.
    */
  def delete(storage: Storage, target: GcsObjectUrl): Task[Either[GcsObjectUrl, GcsObjectUrl]] = Task {

    if (storage.delete(BlobId.of(target.bucket, target.prefix))) Right(target)
    else Left(target)

  }.memoizeOnSuccess

}
