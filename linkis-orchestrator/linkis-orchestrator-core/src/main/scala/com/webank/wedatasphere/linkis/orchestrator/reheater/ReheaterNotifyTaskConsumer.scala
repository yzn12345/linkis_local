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

package com.webank.wedatasphere.linkis.orchestrator.reheater

import com.webank.wedatasphere.linkis.orchestrator.execution.impl.{DefaultTaskManager, NotifyTaskConsumer}
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.ExecTask

/**
  *
  */
abstract class ReheaterNotifyTaskConsumer extends NotifyTaskConsumer {

  val reheater: Reheater

  protected def reheatIt(execTask: ExecTask): Unit = {
    val key = getReheatableKey(execTask.getId)
    def compareAndSet(lastRunning: String): Unit = {
      val thisRunning = getExecution.taskManager.getCompletedTasks(execTask).map(_.task.getId).mkString(",")
      if(thisRunning != lastRunning) {
        reheater.reheat(execTask)
        execTask.getPhysicalContext.set(key, thisRunning)
      }
    }
    execTask.getPhysicalContext.get(key) match {
      case lastRunning: String =>
        compareAndSet(lastRunning)
      case _ if getExecution.taskManager.getCompletedTasks(execTask).nonEmpty =>
        compareAndSet(null)
      case _ =>
        //info(s"no need to deal ${execTask.getPhysicalContext.get(key) }")
    }

  }

  protected def getReheatableKey(id: String): String = ReheaterNotifyTaskConsumer.REHEAT_KEY_PREFIX + id

  override protected def beforeFetchLaunchTask(): Unit = getExecution.taskManager match {
    case taskManager: DefaultTaskManager =>
      taskManager.getRunnableExecutionTasks.foreach(t => reheatIt(t.getRootExecTask))
    case _ =>
  }

}

object ReheaterNotifyTaskConsumer{

  /**
   * Prefix of key in physical context for reheater
   */
  val REHEAT_KEY_PREFIX = "reheatable_"

}
