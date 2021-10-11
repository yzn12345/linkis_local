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

package com.webank.wedatasphere.linkis.orchestrator.ecm.service

import java.io.Closeable
import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.governance.common.protocol.task.RequestTask
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse


trait EngineConnExecutor extends Closeable {


  def getServiceInstance: ServiceInstance

  def getLabels(): Array[Label[_]]

  def setLabels(labels: Array[Label[_]])

  def isAvailable: Boolean

  def useEngineConn: Boolean

  def unUseEngineConn: Unit

  def getRunningTaskCount: Int

  def execute(requestTask: RequestTask): ExecuteResponse

  def killTask(execId: String): Boolean

  def killAll(): Boolean

  def pause(execId: String): Boolean

  def pauseAll(): Boolean

  def resume(execId: String): Boolean

  def resumeAll(): Boolean

  def status(execId: String): ExecutionNodeStatus

  def canEqual(other: Any): Boolean = other.isInstanceOf[EngineConnExecutor]

  def getLastUpdateTime(): Long

  def updateLastUpdateTime(): Unit

  override def equals(other: Any): Boolean = other match {
    case that: EngineConnExecutor =>
      (that canEqual this) &&
        getServiceInstance == that.getServiceInstance
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(getServiceInstance)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString: String = {
    "" + getServiceInstance
  }

}

abstract class AbstractEngineConnExecutor extends EngineConnExecutor with Logging {

  private var labels: Array[Label[_]] = _

  protected var available: Boolean = true

  private var lastUpdateTime: Long = System.currentTimeMillis()

  private val runningTask: util.Map[String, RequestTask] = new util.HashMap[String, RequestTask]()


  override def getLastUpdateTime(): Long = lastUpdateTime

  override def updateLastUpdateTime(): Unit = lastUpdateTime = System.currentTimeMillis()

  override def getLabels(): Array[Label[_]] = labels

  override def setLabels(labels: Array[Label[_]]): Unit = this.labels = labels

  override def isAvailable: Boolean = this.available

  override def getRunningTaskCount: Int = this.runningTask.size()

  protected def getRunningTasks: util.Map[String, RequestTask] = {
    this.runningTask
  }

}