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

package com.webank.wedatasphere.linkis.engineconn.computation.executor.entity
import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.manager.label.entity.Label


class CommonEngineConnTask(val taskId: String, var taskSupportRetry: Boolean = false) extends EngineConnTask {

  private var code: String = _
  private var prop: util.Map[String, Object] = _
  private var status: ExecutionNodeStatus = ExecutionNodeStatus.Inited
  private var labels: Array[Label[_]] = new Array[Label[_]](0)
  private var serviceInstance: ServiceInstance = _

  override def getTaskId: String = taskId

  override def isTaskSupportRetry: Boolean = taskSupportRetry

  override def getCode: String = code

  override def setCode(code: String): Unit = this.code = code

  override def getProperties: util.Map[String, Object] = prop

  override def setProperties(properties: util.Map[String, Object]): Unit = this.prop = properties

  override def data(key: String, value: Object): Unit = this.prop.put(key, value)

  override def getStatus: ExecutionNodeStatus = status

  override def setStatus(status: ExecutionNodeStatus): Unit = this.status = status

  override def getLables: Array[Label[_]] = labels

  override def setLabels(labels: Array[Label[_]]): Unit = this.labels = labels

  override def getCallbackServiceInstance(): ServiceInstance = serviceInstance

  override def setCallbackServiceInstance(serviceInstance: ServiceInstance): Unit = this.serviceInstance = serviceInstance
}
