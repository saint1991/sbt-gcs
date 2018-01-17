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

import scala.language.reflectiveCalls
import scala.util.control.Exception._

package object gcs {

  def using[
    Return,
    Resource <: { def close(): Unit }
  ](resource: Resource)(f: Resource => Return): Return = allCatch andFinally {
    if (resource != null) resource.close()
  } apply f(resource)

}
