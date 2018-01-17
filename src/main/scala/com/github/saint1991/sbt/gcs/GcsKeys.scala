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

import sbt._

trait GcsKeys {

  type Credentials = com.google.auth.Credentials

  // tasks
  val gcsUpload = TaskKey[Seq[String]]("gcs-upload", "Uploads files to a bucket on Google Cloud Storage.")
  val gcsDownload = TaskKey[Seq[File]]("gcs-download", "Downloads objects from Google Cloud Storage.")
  val gcsDelete = TaskKey[Seq[String]]("gcs-delete", "Deletes files from a bucket on Google Cloud Storage.")

  // settings
  val gcsCredential = SettingKey[Option[Credentials]]("credential", "Credential to authenticate Google Cloud Storage")
  val gcsUrls = TaskKey[Seq[String]]("gcs-urls", "URLs of objects on Google Cloud Storage")
}

object GcsKeys extends GcsKeys
