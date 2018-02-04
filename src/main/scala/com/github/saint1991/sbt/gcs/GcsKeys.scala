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

import sbt._

/**
  * SettingKeys and TaskKeys of GcsPlugin
  */
trait GcsKeys {

  /**
    * The task "gcsDelete" deletes a specified list of objects from Google Cloud Storage.
    * Depends on:
    *   - `gcsCredential`: Credentials object for Google Cloud Platform. If not provided, default credential is used
    *                      as stated [here](https://developers.google.com/identity/protocols/application-default-credentials)
    *   - `gcsUrls`:       a list of object URLs to delete from Google Cloud Storage.
    *   - `gcsOperationParallelism`: the parallelism of object deletion
    *   - `gcsOperationTimeout`: timeout duration of the whole gcsDelete task
    * Returns: a list of deleted object URLs. Note that those of inexistent objects are not included in the result.
    */
  val gcsDelete = TaskKey[Seq[String]]("gcs-delete", "Deletes objects from a bucket on Google Cloud Storage.")

  /**
    * The task "gcsUpload" uploads a list of files to specified URLs on Google Cloud Storage.
    * Depends on:
    *   - `mappings`: source file -> destination URL mappings.
    *   - `gcsCredential`: Credentials object for Google Cloud Platform. If not provided, default credential is used
    *                      as stated [here](https://cloud.google.com/video-intelligence/docs/common/auth)
    *   - `gcsOperationParallelism`: the parallelism of file uploading
    *   - `gcsOperationTimeout`: timeout duration of each uploading and the whole gcsUpload task
    *   - `gcsChunkSize`: the chunk size of data on uploading
    *   - `gcsProgress`: the flag whether showing a progress bar on uploading
    * Returns: a sequence of uploaded object URLs.
    */
  val gcsUpload = TaskKey[Seq[String]]("gcs-upload", "Uploads files to a bucket on Google Cloud Storage.")

  /**
    * The task "gcsDownload" downloads a list of objects on Google Cloud Storage to specified files.
    * Depends on:
    *   - `mappings`: source object URL -> destination file mappings.
    *   - `gcsCredential`: Credentials object for Google Cloud Platform. If not provided, default credential is used
    *                      as stated [here](https://cloud.google.com/video-intelligence/docs/common/auth)
    *   - `gcsOperationParallelism`: the parallelism of object downloading
    *   - `gcsOperationTimeout`: timeout duration of each downloading and the whole gcsDownload task
    *   - `gcsChunkSize`: the chunk size of data on downloading
    *   - `gcsProgress`: the flag whether showing a progress bar on downloading
    * Returns: a list of downloaded files.
    */
  val gcsDownload = TaskKey[Seq[File]]("gcs-download", "Downloads objects from Google Cloud Storage.")


  /**
    * Credential to authenticate Google Cloud Storage (optional).
    */
  val gcsCredential = SettingKey[Option[Credentials]]("gcs-credential", "Credential to authenticate Google Cloud Storage")

  /**
    * a list of object URLs on which a certain operation will be performed.
    */
  val gcsUrls = TaskKey[Seq[String]]("gcs-urls", "a list of object URLs on Google Cloud Storage")

  /**
    * the parallelism of operations (i.e. uploading, downloading, deleting).
    */
  val gcsOperationParallelism = SettingKey[Int]("gcs-task-parallelism", "the parallelism of operations")

  /**
    * the timeout for an operation against single object and the whole task.
    */
  val gcsOperationTimeout = SettingKey[FiniteDuration]("gcs-operation-timeout", "the timeout for each operation")

  /**
    * the chunk size for data transfer
    */
  val gcsChunkSize = SettingKey[Int]("gcs-chunk-size", "the chunk size for uploading/downloading.")

  /**
    * the flag whether showing a progress bar on uploading/downloading.
    * Set true to show it. (default false)
    */
  val gcsProgress = SettingKey[Boolean]("gcs-progress", "the flag whether showing progress bar on uploading/downloading")
}

object GcsKeys extends GcsKeys


object Defaults {
  final val DefaultCredential: Option[Credentials] = None
  final val DefaultParallelism = 8
  final val DefaultChunkSize = 8192
  final val DefaultTimeout = 10 minutes
  final val DefaultReportProgress = false
}
