/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.orchestrator.code.plans.logical

import com.webank.wedatasphere.linkis.orchestrator.plans.logical.{AbstractTask, Origin, Task}
import com.webank.wedatasphere.linkis.orchestrator.utils.OrchestratorIDCreator

class CacheTask(private var  parents: Array[Task],
                private var children: Array[Task]) extends AbstractTask {

  private var realTask: CodeLogicalUnitTask = _
  private var id: String = _

  override def getOrigin: Origin = getTaskDesc.getOrigin

  override def getParents: Array[Task] = parents

  override def getChildren: Array[Task] = children

  override def withNewChildren(children: Array[Task]): Unit = modifyFamilyNodes(parents, children)

  override def withNewParents(parents: Array[Task]): Unit = modifyFamilyNodes(parents, children)

  private def modifyFamilyNodes(parents: Array[Task], children: Array[Task]): Unit = {
    this.parents = parents
    this.children = children
  }

  override protected def newNode(): Task = {
    val cacheTask = new CacheTask(null, null)
    cacheTask.setTaskDesc(getTaskDesc)
    cacheTask.setRealTask(this.realTask)
    cacheTask
  }

  def setRealTask(realTask: CodeLogicalUnitTask) = {
    this.realTask = realTask
  }

  def getRealTask() = realTask

  override def getId: String = {
    if (null == id) synchronized {
      if (null == id) {
        id = OrchestratorIDCreator.getLogicalTaskIDCreator.nextID("cache")
      }
    }
    id
  }

  override def theSame(other: Task): Boolean = if (super.equals(other)) true else if (other == null) false else other match {
    case jobTask: CacheTask => jobTask.getParents.sameElements(parents) && jobTask.getChildren.sameElements(children) && jobTask.getTaskDesc == getTaskDesc
    case _ => false
  }

}
