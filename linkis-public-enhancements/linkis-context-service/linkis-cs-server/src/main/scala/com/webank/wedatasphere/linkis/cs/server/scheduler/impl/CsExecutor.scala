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
package com.webank.wedatasphere.linkis.cs.server.scheduler.impl

import java.io.IOException

import com.webank.wedatasphere.linkis.scheduler.executer._



class CsExecutor extends Executor {
  private var id = 0L
  private var _state: ExecutorState.ExecutorState = _

  override def getId: Long = this.id

  override def execute(executeRequest: ExecuteRequest): ExecuteResponse = { //httpjob执行的地方
    try {
      executeRequest match {
        case request: CsExecuteRequest =>
          request.getConsumer.accept(request.get)
        case _ =>
      }
      new SuccessExecuteResponse
    } catch {
      case e: Exception =>
        ErrorExecuteResponse(e.getMessage, e)
    }
  }

  override def state: ExecutorState.ExecutorState = this._state

  override def getExecutorInfo = new ExecutorInfo(id, state)

  @throws[IOException]
  override def close(): Unit = {
  }
}
