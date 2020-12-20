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

import scala.util.Try
import scala.util.control.Exception.allCatch

package object util {

  implicit val closableReleaser: Releaser[AutoCloseable] = new AutoCloseableReleaser()

  def using[Resource: Releaser, Return](resource: => Resource)(f: Resource => Try[Return])(implicit
    releaser: Releaser[Resource]
  ): Try[Return] =
    allCatch andFinally releaser.release(resource) apply f(resource)
}
