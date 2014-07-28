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
package org.copygrinder.unpure.persistance

import java.io.{File, FileWriter}

import com.softwaremill.macwire.MacwireMacros._
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class GitRepo(repoName: String) {

  protected val fileRepositoryBuilderWrapper = wire[FileRepositoryBuilderWrapper]

  def create(overwrite: Boolean = false): Unit = {

    if (overwrite) {
      FileUtils.deleteDirectory(new File(repoName))
    }

    val repository = buildRepository()
    repository.create()
    repository.close()
  }

  def createIfNonExistant(): Unit = {
    if (new File(repoName).exists() == false) {
      create(false)
    }
  }

  def add(fileName: String, content: String): Unit = {

    val file = new File(repoName + "/" + fileName)
    file.createNewFile()
    val out = new FileWriter(file)
    out.write(content)
    out.close()

    doGitAction((git: Git) => {
      git.add().addFilepattern(".").call()
    })
  }

  def commit(message: String): Unit = {
    doGitAction((git: Git) => {
      git.commit().setMessage(message).call()
    })
  }

  protected def doGitAction(func: (Git) => Unit): Unit = {
    val repository = buildRepository()
    val git = new Git(repository)
    func(git)
    repository.close()
  }

  protected def buildRepository(): Repository = {
    fileRepositoryBuilderWrapper.setGitDir(new File(repoName + "/.git")).setup().build()
  }

}

class FileRepositoryBuilderWrapper {

  val builder = new FileRepositoryBuilder()

  def setGitDir(file: File): FileRepositoryBuilderWrapper = {
    builder.setGitDir(file)
    this
  }

  def setup(): FileRepositoryBuilderWrapper = {
    builder.setup()
    this
  }

  def build(): Repository = {
    builder.build()
  }

}