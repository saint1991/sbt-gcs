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

import com.github.saint1991.sbt.gcs.io.{GcsInputStream, GcsOutputStream}

import scala.language.reflectiveCalls
import com.google.cloud.{ReadChannel, WriteChannel}
import com.google.cloud.storage.{Storage, StorageOptions}

package object gcs {

  type Credentials = com.google.auth.Credentials
  type Parallelism = Int
  type TaskIndex = Int
  type ChunkSize = Int
  type ReportProgress = Boolean


  object Converters {

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
