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

package com.github.saint1991.sbt.gcs.observer

import com.github.saint1991.sbt.gcs.{ ProgressBar, TaskIndex }
import monix.execution.Ack
import monix.execution.Ack.Continue
import monix.reactive.Observer

import scala.concurrent.Future

/**
 * Observer of InputStream that indicates the progress of a certain task identified by *taskIndex*.
 *
 * @param taskIndex index of corresponding task in *maxValues* passed to ProgressBar
 * @param progressBar ProgressBar instance
 */
class ProgressObserver(taskIndex: TaskIndex, progressBar: ProgressBar) extends Observer[Array[Byte]] {

  private var accSize: Long = 0L

  override def onError(ex: Throwable): Unit = throw ex
  override def onComplete(): Unit = {
    progressBar.setProgress(taskIndex, progressBar.maxValues(taskIndex)._2)
    progressBar.render()
  }

  override def onNext(elem: Array[Byte]): Future[Ack] = {
    accSize += elem.length
    progressBar.setProgress(taskIndex, accSize)
    progressBar.render()
    Continue
  }
}
