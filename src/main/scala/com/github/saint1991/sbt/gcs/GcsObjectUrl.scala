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

import scala.util.matching.Regex
import scala.util.matching.Regex.Groups


/**
  * Model class that represents object URL on Google Cloud Storage
  * @param bucket bucket name
  * @param prefix object prefix
  */
case class GcsObjectUrl(bucket: String, prefix: String) {
  import GcsObjectUrl._
  require(BucketNameRegex.pattern.matcher(bucket).matches(), s"Invalid bucket name: $bucket")

  lazy val url = s"gs://$bucket/$prefix"
}

object GcsObjectUrl {

  final val BucketNameRegex: Regex = "^[a-zA-Z0-9][a-zA-Z0-9\\._-]{1,253}[a-zA-Z0-9]$".r
  final val ObjectUrlRegex: Regex = "^gs://([a-zA-Z0-9][a-zA-Z0-9\\._-]{1,253}[a-zA-Z0-9])/(.+)$".r

  /**
    * Factory method that takes raw URL string
    * This accepts only gsutil based URL that begins with `gs://` otherwise throws IllegalArgumentException.
    * @param url a string representing object URL
    * @return
    */
  def apply(url: String): GcsObjectUrl = (apply(_: String, _: String)).tupled(
    ObjectUrlRegex.findFirstMatchIn(url) match {
      case Some(Groups(b, p)) => (b, p)
      case None => throw new IllegalArgumentException(s"Invalid format URI: $url")
    }
  )
}

