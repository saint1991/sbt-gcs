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

import scala.concurrent.duration.FiniteDuration

import sbt._

trait GcsKeys {

  /**
    * The task "gcsUpload" uploads a list of files to specified URLs on Google Cloud Storage.
    * Depends on:
    *   - `gcsCredential`: Credentials object for Google Cloud Platform. If not provided, default credential is used
    *                      as stated [here](https://cloud.google.com/video-intelligence/docs/common/auth)
    *   - `mappings`: source file -> destination URL mappings.
    *
    * Returns: a sequence of uploaded object URLs.
    */
  val gcsUpload = TaskKey[Seq[String]]("gcs-upload", "Uploads files to a bucket on Google Cloud Storage.")

  /**
    * The task "gcsDownload" downloads a lust of objects on Google Cloud Storage to specified files.
    * Depends on:
    *   - `gcsCredential`: Credentials object for Google Cloud Platform. If not provided, default credential is used
    *                      as stated [here](https://cloud.google.com/video-intelligence/docs/common/auth)
    *   - `mappings`: destination file <- source URL mappings.
    *
    * Returns: a sequence of downloaded files.
    */
  val gcsDownload = TaskKey[Seq[File]]("gcs-download", "Downloads objects from Google Cloud Storage.")

  /**
    * The task "gcsDelete" deletes a specified list of objects from Google Cloud Storage.
    * Depends on:
    *   - `gcsCredential`: Credentials object for Google Cloud Platform. If not provided, default credential is used
    *                      as stated [here](https://developers.google.com/identity/protocols/application-default-credentials)
    *   - `gcsUrls`:       a list of object URLs to delete from Google Cloud Storage.
    *
    * Returns: a sequence of deleted object URLs. Note that those of inexistent objects are not included in the result.
    */
  val gcsDelete = TaskKey[Seq[String]]("gcs-delete", "Deletes files from a bucket on Google Cloud Storage.")


  /**
    * Credential to authenticate Google Cloud Storage (optional).
    */
  val gcsCredential = SettingKey[Option[Credentials]]("gcs-credential", "Credential to authenticate Google Cloud Storage")

  /**
    * a list of object URLs on which a certain operation should be performed.
    */
  val gcsUrls = TaskKey[Seq[String]]("gcs-urls", "URLs of objects on Google Cloud Storage")

  /**
    * Maximum parallelism of executing operations (i.e. uploading, downloading, deleting).
    */
  val gcsOperationParallelism = SettingKey[Int]("gcs-task-parallelism", "Maximum parallelism of operations.")

  /**
    * Timeout for each operation against single object.
    */
  val gcsOperationTimeout = SettingKey[FiniteDuration]("gcs-operation-timeout", "Timeout for each operation.")

  /**
    * Chunk size for uploading/downloading
    */
  val gcsChunkSize = SettingKey[Int]("gcs-chunk-size", "chunk size for uploading/downloading.")

  /**
    * Set true to show progress bar on uploading/downloading
    */
  val gcsProgress = SettingKey[Boolean]("gcs-progress", "flg whether shows progress bar on uploading/downloading")
}

object GcsKeys extends GcsKeys
