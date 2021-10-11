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

package com.webank.wedatasphere.linkis.orchestrator.plans.ast

import com.webank.wedatasphere.linkis.orchestrator.utils.OrchestratorIDCreator

/**
  *
  */
trait Job extends ASTOrchestration[Job] {

  private var visited: Boolean = false

  private var id: String = _

  override def isVisited: Boolean = visited

  override def setVisited(): Unit = this.visited = true

  def getAllStages: Array[Stage]

  def getRootStages: Array[Stage] = getAllStages.filter(null != _.getParents)

  def getStage(stageId: String): Option[Stage] = getAllStages.find(_.getId == stageId)

  def copyWithNewStages(stages: Array[Stage]): Job

  override def getId: String =  {
    if (null == id) synchronized {
      if (null == id) {
        id = OrchestratorIDCreator.getAstJobIDCreator.nextID("astJob")
      }
    }
    id
  }

}
