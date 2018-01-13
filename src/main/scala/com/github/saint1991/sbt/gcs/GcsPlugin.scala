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
import ExecutionContext.Implicits.global

import sbt._
import sbt.Keys._
import sbt.util.Logger

import com.google.cloud.storage.{Storage, StorageOptions}


object GcsPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport extends GcsKeys
  import autoImport._

  private def getClient(credentials: Option[Credentials]): Storage = (credentials match {
    case Some(c) => StorageOptions.newBuilder().setCredentials(c).build()
    case None => StorageOptions.getDefaultInstance
  }).getService

  private def gcsInitTask[Item, Return](
    thisTask: TaskKey[Seq[Return]],
    itemsKey: TaskKey[Seq[Item]],
    op: (Storage, Seq[Item], Logger) => Seq[Return]
  ) = Def.task {
    val logger = streams.value.log
    val credential = (gcsCredential in thisTask).value
    val items = (itemsKey in thisTask).value
    val storage = getClient(credential)
    op(storage, items, logger)
  }

  private val gcsSettings = Seq(

    gcsUpload := gcsInitTask[(File, String), String](gcsUpload, mappings, { (storage, uploadMappings, logger) =>
      Await.result(Future.sequence(uploadMappings map { case (s, d) => (s, GcsObjectUrl(d)) } map { case (src, dest) =>
        Gcs.upload(storage)(src, dest).map { case (cmpSrc, cmpDest) =>
          logger.info(s"Uploaded ${cmpSrc.getAbsoluteFile} to ${cmpDest.url}")
          cmpDest.url
        }
      }), 30 seconds)
    }).value,

    gcsDownload := gcsInitTask[(File, String), File](gcsDownload, mappings, { (storage, downloadMappings, logger) =>
      Await.result(Future.sequence(downloadMappings map { case (d, s) => (GcsObjectUrl(s), d) } map { case (src, dest) =>
        Gcs.download(storage)(src, dest).map { case (cmpSrc, cmpDest) =>
          logger.info(s"Downloaded ${cmpDest.getAbsoluteFile} from ${cmpSrc.url}")
          cmpDest
        }
      }), 30 seconds)
    }).value,

    gcsDelete := gcsInitTask[String, String](gcsDelete, gcsUrls, { (storage, deleteUrls, logger) =>
      Await.result(Future.sequence(deleteUrls.map(GcsObjectUrl.apply).map { target =>
        Gcs.delete(storage)(target).map {
          case None =>
            logger.info(s"Object ${target.url} does not exist")
            None
          case Some(cmpUrl) =>
            logger.info(s"Deleted ${cmpUrl.url}")
            Some(cmpUrl.url)
        }
      }), 30 seconds).flatten
    }).value,

    gcsCredential := None,
    mappings in gcsUpload := Seq.empty,
    mappings in gcsDownload := Seq.empty,
    gcsUrls in gcsDelete := Seq.empty
  )

  override def projectSettings: Seq[Def.Setting[_]] = super.projectSettings ++ gcsSettings
}
