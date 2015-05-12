/*
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
package org.copygrinder.impure.concurrency

import org.copygrinder.pure.copybean.exception.FutureException

import scala.concurrent.{ExecutionContext, Future}

object FutureWrapper {
  def apply[T](future: Future[T])(implicit executor: ExecutionContext): Future[T] = {

    val outerThread = Thread.currentThread()

    def getStack(thread: Thread) = {
      thread.getStackTrace.map("\t" + _.toString).mkString("\n") + "\n\n"
    }

    val wrappedFuture = future.recover {
      case e: Exception => throw new FutureException(getStack(outerThread), e)
    }

    wrappedFuture
  }
}