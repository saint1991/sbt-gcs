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
import com.google.cloud.storage.{Storage, StorageOptions}
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global

/**
  * GcsPlugin is a sbt plugin that can perform basic operations on objects on Google Cloud Storage.
  */
object GcsPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport extends GcsKeys
  import autoImport._

  private def getStorage(credentials: Option[Credentials]): Storage = (credentials match {
    case Some(c) => StorageOptions.newBuilder().setCredentials(c).build()
    case None => StorageOptions.getDefaultInstance
  }).getService

  private def addSignalHandler(f: (Int) => Unit) = {
    val handler = (code: Int) => () => {
      f(code)
      sys.exit(code)
    }
    Signals.register(handler(15), "TERM")
  }

  private def gcsInitTask[Item, Return](
    thisTask: TaskKey[Seq[Return]],
    itemsKey: TaskKey[Seq[Item]])
  (
    op: (Storage, Seq[Item], Parallelism, FiniteDuration, Logger) => Seq[Return]
  ) = Def.task {
    val logger = streams.value.log
    val credential = (gcsCredential in thisTask).value
    val items = (itemsKey in thisTask).value
    val parallelism = (gcsOperationParallelism in thisTask).value
    val timeout = (gcsOperationTimeout in thisTask).value
    val storage = getStorage(credential)
    op(storage, items, parallelism, timeout, logger)
  }

  private val gcsSettings = Seq(

    gcsUpload := gcsInitTask[(File, String), String](gcsUpload, mappings) { (storage, mappings, parallelism, timeout, logger) =>
       val cancelable = Observable.fromIterable(mappings map {
         case (s, d) => (s, GcsObjectUrl(d))
       }).mapAsync(parallelism) { case (src, dest) =>
         GcsTasks.upload(storage, src, dest)
       }.map { case (file, url) =>
         logger.info(s"Uploaded ${file.getAbsolutePath} to ${url.url}")
         url.url
       }.toListL.runAsync
       addSignalHandler { _ => cancelable.cancel() }
       Await.result(cancelable, timeout)
    }.value,

    gcsDownload := gcsInitTask[(File, String), File](gcsDownload, mappings) { (storage, mappings, parallelism, timeout, logger) =>
      val cancelable = Observable.fromIterable(mappings map {
        case (d, s) => (GcsObjectUrl(s), d)
      }).mapAsync(parallelism) { case (src, dest) =>
        GcsTasks.download(storage, src, dest)
      }.map { case (url, file) =>
        logger.info(s"Downloaded ${url.url} to ${file.getAbsolutePath}")
        file
      }.toListL.runAsync
      addSignalHandler { _ => cancelable.cancel() }
      Await.result(cancelable, timeout)
    }.value,

    gcsDelete := gcsInitTask[String, String](gcsDelete, gcsUrls) { (storage, urls, parallelism, timeout, logger) =>
      val cancelable = Observable.fromIterable(urls.map(GcsObjectUrl.apply)).mapAsync(parallelism) { target =>
        GcsTasks.delete(storage, target)
      }.map {
        case Left(u) =>
          logger.info(s"${u.url} not found.")
          None
        case Right(u) =>
          logger.info(s"Deleted ${u.url}.")
          Some(u)
      }.collect { case Some(u) => u.url }.toListL.runAsync
      addSignalHandler { _ => cancelable.cancel() }
      Await.result(cancelable, timeout)
    }.value,

    gcsCredential := None,
    gcsOperationParallelism := 8,
    gcsOperationTimeout := 5.minutes,
    mappings in gcsUpload := Seq.empty,
    mappings in gcsDownload := Seq.empty,
    gcsUrls in gcsDelete := Seq.empty
  )

  override def projectSettings: Seq[Def.Setting[_]] = super.projectSettings ++ gcsSettings
}
