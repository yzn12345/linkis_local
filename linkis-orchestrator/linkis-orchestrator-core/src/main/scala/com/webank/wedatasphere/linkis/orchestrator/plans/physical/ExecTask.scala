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

package com.webank.wedatasphere.linkis.orchestrator.plans.physical

import com.webank.wedatasphere.linkis.orchestrator.execution.TaskResponse
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{Job, Stage}
import com.webank.wedatasphere.linkis.orchestrator.plans.logical.TaskDesc

/**
  *
  */
trait ExecTask extends PhysicalOrchestration[ExecTask] {

  def canExecute: Boolean

  def execute(): TaskResponse

  def getTaskDesc: TaskDesc

  def isLocalMode: Boolean

  override def hashCode(): Int = getId.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case execTask: ExecTask => execTask.getId.equals(getId)
    case _ => false
  }

  def getIDInfo(): String = {
    val desc = getTaskDesc
    val jobID = desc.getOrigin.getASTOrchestration match {
      case job: Job =>
        job.getId
      case stage: Stage =>
        stage.getJob.getId
      case _ => ""
    }
    jobID + "_" + getId
  }

}