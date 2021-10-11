/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.governance.common.protocol.task.{RequestTask, RequestTaskExecute}
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteRequest, JobExecuteRequest}

import scala.collection.JavaConversions


trait ExecuteRequestInterceptor {

  def apply(requestTask: RequestTask, executeRequest: ExecuteRequest): RequestTask

}
object LabelExecuteRequestInterceptor extends ExecuteRequestInterceptor {
  override def apply(requestTask: RequestTask, executeRequest: ExecuteRequest): RequestTask = executeRequest match {
    case labelExecuteRequest: LabelExecuteRequest =>
      val rq = if (requestTask == null) {
        val requestTask = new RequestTaskExecute
        requestTask.setCode(executeRequest.code)
        requestTask
      } else requestTask
      rq.setLabels(labelExecuteRequest.labels)
      rq
    case _ => requestTask
  }
}
object JobExecuteRequestInterceptor extends ExecuteRequestInterceptor {
  val PROPERTY_JOB_ID = "jobId"
  override def apply(requestTask: RequestTask,
                     executeRequest: ExecuteRequest): RequestTask = executeRequest match {

    case job: JobExecuteRequest =>
      if (requestTask == null) {
        val requestTask = new RequestTaskExecute
        requestTask.setCode(executeRequest.code)
        requestTask.data(PROPERTY_JOB_ID, job.jobId)
        requestTask
      } else {
        requestTask.data(PROPERTY_JOB_ID, job.jobId)
        requestTask
      }
    case _ => requestTask
  }
}
object ReconnectExecuteRequestInterceptor extends ExecuteRequestInterceptor {
  val PROPERTY_EXEC_ID = "execId"
  override def apply(requestTask: RequestTask,
                     executeRequest: ExecuteRequest): RequestTask = executeRequest match {
    case reconnect: ReconnectExecuteRequest =>
      requestTask.data(PROPERTY_EXEC_ID, reconnect.execId)
      requestTask
    case _ => requestTask
  }
}
object StorePathExecuteRequestInterceptor extends ExecuteRequestInterceptor {
  override def apply(requestTask: RequestTask, executeRequest: ExecuteRequest): RequestTask = executeRequest match {
    case storePath: StorePathExecuteRequest =>
      requestTask.data(RequestTask.RESULT_SET_STORE_PATH, storePath.storePath)
      requestTask
    case _ => requestTask
  }
}

object RuntimePropertiesExecuteRequestInterceptor extends ExecuteRequestInterceptor {
  override def apply(requestTask: RequestTask, executeRequest: ExecuteRequest): RequestTask = executeRequest match {
    case runtime: RuntimePropertiesExecuteRequest =>
      JavaConversions.mapAsScalaMap(runtime.properties).foreach { case (k, v) => requestTask.data(k, v)}
      requestTask
    case _ => requestTask
  }
}
