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

import scala.concurrent.duration._
import scala.language.postfixOps

import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.Observer
import sbt.File


sealed case class GcsTaskConfig private [gcs](
  credential: Option[Credentials] = None,
  chunkSize: ChunkSize = 8192,
  timeout: Duration = 10 minutes,
  observers: Seq[Observer[Array[Byte]]] = Seq.empty
) extends StorageProvider


sealed trait GcsOperation
sealed trait GcsTransfer extends GcsOperation

object GcsTaskFactory {
  def delete = new GcsDeleteFactory()
  def upload = new GcsUploadFactory()
  def download = new GcsDownloadFactory()
}

class GcsTaskFactory[T <: GcsOperation] protected {

  protected var config: GcsTaskConfig = GcsTaskConfig()

  def withCredential(credential: Option[Credentials]): this.type = {
    config = config.copy(credential = credential)
    this
  }
  def withCredential(credential: Credentials): this.type = withCredential(Some(credential))
  def withChunkSize(chunkSize: ChunkSize)(implicit ev: T =:= GcsTransfer): this.type = {
    config = config.copy(chunkSize = chunkSize)
    this
  }
  def withObservers(observers: Seq[Observer[Array[Byte]]])(implicit ev: T =:= GcsTransfer): this.type = {
    config = config.copy(observers = observers)
    this
  }
  def withTimeout(timeout: Duration)(implicit ev: T =:= GcsTransfer): this.type = {
    config = config.copy(timeout = timeout)
    this
  }
}

class GcsDeleteFactory private [gcs] extends GcsTaskFactory[GcsOperation] {
  def create(url: GcsObjectUrl)(implicit scheduler: Scheduler): Task[Either[GcsObjectUrl, GcsObjectUrl]] = GcsTasks.delete(config)(url)
}
class GcsUploadFactory private [gcs]  extends GcsTaskFactory[GcsTransfer] {
  def create(src: File, dest: GcsObjectUrl)(implicit scheduler: Scheduler): Task[(sbt.File, GcsObjectUrl)] = GcsTasks.upload(config)(src, dest)
}
class GcsDownloadFactory private [gcs] extends GcsTaskFactory[GcsTransfer] {
  def create(src: GcsObjectUrl, dest: File)(implicit scheduler: Scheduler): Task[(GcsObjectUrl, sbt.File)] = GcsTasks.download(config)(src, dest)
}
