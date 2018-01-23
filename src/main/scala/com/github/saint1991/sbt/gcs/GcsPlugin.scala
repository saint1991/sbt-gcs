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

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

import sbt._
import sbt.Keys._
import sbt.util.Logger
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global

/**
  * GcsPlugin is a sbt plugin that can perform basic operations on objects on Google Cloud Storage.
  */
object GcsPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport extends GcsKeys
  import autoImport._

  private def addSignalHandler(f: (Int) => Unit) = {
    val handler = (code: Int) => () => {
      f(code)
      sys.exit(code)
    }
    Signals.register(handler(15), "TERM")
  }

  private def gcsInitTask[Item, Return](
    thisTask: TaskKey[Seq[Return]],
    itemsKey: TaskKey[Seq[Item]]
  )(op: (Option[Credentials], Seq[Item], Parallelism, FiniteDuration, ChunkSize, ReportProgress, Logger) => Seq[Return]) = Def.task {
    val credential = (gcsCredential in thisTask).value
    val items = (itemsKey in thisTask).value
    val parallelism = (gcsOperationParallelism in thisTask).value
    val timeout = (gcsOperationTimeout in thisTask).value
    val chunkSize = (gcsChunkSize in thisTask).value
    val reportProgress = (gcsProgress in thisTask).value
    val logger = streams.value.log
    op(credential, items, parallelism, timeout, chunkSize, reportProgress, logger)
  }

  private val gcsSettings = Seq(

    gcsDelete := gcsInitTask[String, String](gcsDelete, gcsUrls) {
      (credential, urls, parallelism, timeout, _, _, logger) =>

      val taskFactory = GcsTaskFactory.delete.withCredential(credential)
      val tasks = Observable.fromIterable(urls.map(GcsObjectUrl.apply).map { url =>
        taskFactory.create(url)
      }).mapAsync(parallelism)(identity) map { url =>
        url match {
          case Left(u) => logger.info(s"Object did not exist: ${u.url}.")
          case Right(u) => logger.info(s"Deleted ${u.url}.")
        }
        url
      } collect { case Right(url) => url.url }

      val cancelable = tasks.toListL.runAsync
      addSignalHandler { _ => cancelable.cancel() }
      Await.result(cancelable, timeout)
    }.value,

    gcsUpload := gcsInitTask[(File, String), String](gcsUpload, mappings) {
      (credential, mps, parallelism, timeout, chunkSize, reportProgress, logger) =>

        val mappings = mps.map { case (src, dest) => src -> GcsObjectUrl(dest) }.zipWithIndex
        val taskFactory = GcsTaskFactory.upload.withCredential(credential).withChunkSize(chunkSize)

        // create progress bar. this will be evaluated only if reportProgress is true.
        lazy val progressBar: LoggingProgressBar = {
          val pb = new LoggingProgressBar(mappings.map { case (f, _) => f._1.name -> f._1.length }, logger)
          pb.initRender()
          pb
        }

        val tasks = Observable.fromIterable(mappings.map { case ((src, dest), taskIndex) => (
          if (reportProgress) taskFactory.withObservers(Seq(new ProgressObserver(taskIndex, progressBar)))
          else taskFactory
        ).create(src, dest)}).mapAsync(parallelism)(identity).map(_._2.url)

        val cancelable = tasks.toListL.runAsync
        addSignalHandler { _ => cancelable.cancel() }
        Await.result(cancelable, timeout)

    }.value,


    gcsDownload := gcsInitTask[(File, String), File](gcsDownload, mappings) {
      (credential, mps, parallelism, timeout, chunkSize, reportProgress, logger) =>

        val mappings = mps.map { case (src, dest) => GcsObjectUrl(dest) -> src }.zipWithIndex
        val taskFactory = GcsTaskFactory.download.withCredential(credential).withChunkSize(chunkSize)

        // create progress bar. this will be evaluated only if reportProgress is true.
        lazy val progressBar = {
          val storage = getStorage(credential)
          val pb = new LoggingProgressBar(mappings.map { case ((src, _), _) =>
            val blob = storage.get(src.bucket, src.prefix)
            src.url -> blob.getSize.toLong
          }, logger)
          pb.initRender()
          pb
        }

        val tasks = Observable.fromIterable(mappings.map { case ((src, dest), taskIndex) => (
          if (reportProgress) taskFactory.withObservers(Seq(new ProgressObserver(taskIndex, progressBar)))
          else taskFactory
        ).create(src, dest)}).mapAsync(parallelism)(identity).map(_._2)

        val cancelable = tasks.toListL.runAsync
        addSignalHandler { _ => cancelable.cancel() }
        Await.result(cancelable, timeout)

    }.value,

    gcsCredential := None,
    mappings in gcsUpload := Seq.empty,
    mappings in gcsDownload := Seq.empty,
    gcsUrls in gcsDelete := Seq.empty,
    gcsOperationParallelism := 8,
    gcsOperationTimeout := 5.minutes,
    gcsChunkSize := 8192,
    gcsProgress := false
  )

  override def projectSettings: Seq[Def.Setting[_]] = super.projectSettings ++ gcsSettings
}
