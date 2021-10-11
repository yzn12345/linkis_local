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

package com.webank.wedatasphere.linkis.orchestrator.execution

import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.orchestrator.core.{FailedOrchestrationResponse, _}

/**
  *
  */
trait TaskResponse extends OrchestrationResponse

trait AsyncTaskResponse extends TaskResponse {
  def notifyMe(listener: AsyncTaskResponse.NotifyListener): Unit
  def waitForCompleted(): TaskResponse
}
object AsyncTaskResponse {
  type NotifyListener = TaskResponse => Unit
}

trait FunctionalTaskResponse extends TaskResponse {
  def getName: String = this.getClass.getSimpleName.replace("TaskResponse", "")
}

trait CompletedTaskResponse extends TaskResponse with CompletedOrchestrationResponse

trait RetryTaskResponse extends FunctionalTaskResponse {
  val interval: Long
  val maxReties: Int
}

trait TriggerTaskResponse extends RetryTaskResponse {
  def canTrig(event: Event): Boolean
}

trait SucceedTaskResponse extends CompletedTaskResponse with SucceedOrchestrationResponse

trait ResultSetTaskResponse extends SucceedTaskResponse with ResultSetOrchestrationResponse

trait ArrayResultSetTaskResponse extends SucceedTaskResponse with ArrayResultSetOrchestrationResponse

trait FailedTaskResponse extends CompletedTaskResponse with FailedOrchestrationResponse {
  def getCause: Throwable
}