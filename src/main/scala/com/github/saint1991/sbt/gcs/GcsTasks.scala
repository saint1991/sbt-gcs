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

import java.io._
import java.nio.file.Files

import scala.concurrent.{ Await, Future }
import scala.language.postfixOps

import sbt._
import com.google.cloud.storage.{ BlobId, BlobInfo }
import monix.eval.Task
import monix.execution.{ Ack, Scheduler }
import monix.execution.Ack.Continue
import monix.reactive._
import monix.reactive.observables.ConnectableObservable

/**
 * Observer that simply copies bytes to OutputStream.
 * @param out OutputStream to write bytes to
 */
private[gcs] class StreamCopyObserver(out: OutputStream) extends Observer[Array[Byte]] {
  override def onComplete(): Unit = ()
  override def onError(ex: Throwable): Unit = throw ex
  override def onNext(elem: Array[Byte]): Future[Ack] = {
    out.write(elem)
    Continue
  }
}

/**
 * Logic of operations against Google Cloud Storage
 */
object GcsTasks {
  import Converters._

  /**
   * Delete *target* object from Google Cloud Storage.
   * @param config GcsTaskConfig to get Storage object according to configured credential
   * @param target URL of object to delete
   * @return URL of Left if the object does not exist otherwise returns that of Right as the task execution
   */
  def delete(config: GcsTaskConfig)(target: GcsObjectUrl): Task[Either[GcsObjectUrl, GcsObjectUrl]] = Task {
    if (config.storage.delete(BlobId.of(target.bucket, target.prefix))) Right(target)
    else Left(target)
  }.memoizeOnSuccess

  /**
   * Factory of stream copy task with configured observers.
   * @param config Configuration of a copy task such as chunk size and observers
   * @param in InputStream to read bytes from
   * @param out OutputStream to write bytes to
   * @param scheduler Monix scheduler
   * @return
   */
  private[gcs] def copyStream(
    config: GcsTaskConfig
  )(in: InputStream, out: OutputStream)(implicit scheduler: Scheduler): ConnectableObservable[Array[Byte]] = {
    val blueprint = (new StreamCopyObserver(out) +: config.observers).foldLeft(
      Observable.fromInputStream(in, config.chunkSize).multicast(Pipe(MulticastStrategy.publish[Array[Byte]]))
    ) { (acc, o) =>
      acc.subscribe(o)
      acc
    }
    blueprint.connect()
    blueprint
  }

  /**
   * Upload a *src* file to a *dest* URL on Google Cloud Storage.
   * @param config Configuration of a upload task
   * @param src a file to upload
   * @param dest a destination URL to upload *src* to
   * @param scheduler Monix scheduler
   * @return the tuple of uploaded (src, dest) pair as the result of task execution
   */
  def upload(config: GcsTaskConfig)(src: File, dest: GcsObjectUrl)(implicit scheduler: Scheduler): Task[(File, GcsObjectUrl)] = {

    val blobInfo = BlobInfo
      .newBuilder(dest.bucket, dest.prefix)
      .setContentType(Files.probeContentType(src.toPath) match {
        case null => "application/octet-stream"
        case str => str
      })
      .build()

    Task {
      val storage = getStorage(config.credential)
      storage.create(blobInfo, Files.readAllBytes(src.toPath))
      (src, dest)
    }.memoizeOnSuccess
  }


  /**
   * Download *src* object from Google Cloud Storage as the *dest* file.
   * @param src URL of object to download
   * @param dest destination file
   * @param scheduler Monix scheduler
   * @return the tuple of downloaded (src, dest) pair as the result of task execution
   */
  def download(config: GcsTaskConfig)(src: GcsObjectUrl, dest: File)(implicit scheduler: Scheduler): Task[(GcsObjectUrl, File)] = Task {
    using(config.storage.reader(src.bucket, src.prefix).asInputStream) { in =>
      using(new BufferedOutputStream(new FileOutputStream(dest))) { out =>
        Await.ready(copyStream(config)(in, out).foreach(_ => ()), config.timeout)
      }
    }
    (src, dest)
  }.memoizeOnSuccess

}
