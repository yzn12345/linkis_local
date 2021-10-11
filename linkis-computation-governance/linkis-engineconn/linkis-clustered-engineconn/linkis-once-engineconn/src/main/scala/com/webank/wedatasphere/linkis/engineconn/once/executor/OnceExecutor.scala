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

package com.webank.wedatasphere.linkis.engineconn.once.executor
import java.util

import com.webank.wedatasphere.linkis.bml.client.BmlClientFactory
import com.webank.wedatasphere.linkis.common.conf.Configuration
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.core.util.EngineConnUtils
import com.webank.wedatasphere.linkis.engineconn.executor.entity.{ExecutableExecutor, LabelExecutor, ResourceExecutor}
import com.webank.wedatasphere.linkis.engineconn.once.executor.exception.OnceEngineConnErrorException
import com.webank.wedatasphere.linkis.governance.common.protocol.task.RequestTask
import com.webank.wedatasphere.linkis.governance.common.utils.OnceExecutorContentUtils
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.{JobLabel, Label}
import com.webank.wedatasphere.linkis.scheduler.executer.{AsynReturnExecuteResponse, ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

import scala.collection.convert.wrapAsScala._
import scala.collection.mutable.ArrayBuffer

trait OnceExecutor extends ExecutableExecutor[ExecuteResponse] with LabelExecutor with Logging {

  private var executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = this.executorLabels = labels

  override final def execute(engineCreationContext: EngineCreationContext): ExecuteResponse = {
    val onceExecutorExecutionContext = createOnceExecutorExecutionContext(engineCreationContext)
    val arrayBuffer = new ArrayBuffer[Label[_]]
    executorLabels.foreach(l => arrayBuffer += l)
    onceExecutorExecutionContext.setLabels(arrayBuffer.toArray)
    initOnceExecutorExecutionContext(onceExecutorExecutionContext)
    execute(onceExecutorExecutionContext)
  }

  def execute(onceExecutorExecutionContext: OnceExecutorExecutionContext): ExecuteResponse

  protected def createOnceExecutorExecutionContext(engineCreationContext: EngineCreationContext): OnceExecutorExecutionContext = {
    val resource = engineCreationContext.getOptions.get(OnceExecutorContentUtils.ONCE_EXECUTOR_CONTENT_KEY)
    if(StringUtils.isEmpty(resource)) throw new OnceEngineConnErrorException(12560, OnceExecutorContentUtils.ONCE_EXECUTOR_CONTENT_KEY + " is not exist.")
    val bmlResource = OnceExecutorContentUtils.valueToResource(resource)
    val bmlClient = BmlClientFactory.createBmlClient(engineCreationContext.getUser)
    val contentStr = Utils.tryFinally {
      val inputStream = bmlClient.downloadResource(engineCreationContext.getUser, bmlResource.getResourceId, bmlResource.getVersion).inputStream
      Utils.tryFinally(IOUtils.toString(inputStream, Configuration.BDP_ENCODING.getValue))(IOUtils.closeQuietly(inputStream))
    } (bmlClient.close())
    val contentMap = EngineConnUtils.GSON.fromJson(contentStr, classOf[util.Map[String, Object]])
    val onceExecutorContent = OnceExecutorContentUtils.mapToContent(contentMap)
    new OnceExecutorExecutionContext(engineCreationContext, onceExecutorContent)
  }

  protected def initOnceExecutorExecutionContext(onceExecutorExecutionContext: OnceExecutorExecutionContext): Unit = {
    val properties = onceExecutorExecutionContext.getOnceExecutorContent.getRuntimeMap
    if (properties.containsKey(RequestTask.RESULT_SET_STORE_PATH)) {
      onceExecutorExecutionContext.setStorePath(properties.get(RequestTask.RESULT_SET_STORE_PATH).toString)
      info(s"ResultSet storePath: ${onceExecutorExecutionContext.getStorePath}.")
    }
    if(onceExecutorExecutionContext.getOnceExecutorContent.getExtraLabels != null) {
      val extraLabelsList = LabelBuilderFactoryContext.getLabelBuilderFactory
        .getLabels(onceExecutorExecutionContext.getOnceExecutorContent.getExtraLabels)
      val extraLabels = new ArrayBuffer[Label[_]]()
      extraLabelsList.foreach(executorLabels += _)
      onceExecutorExecutionContext.setLabels(onceExecutorExecutionContext.getLabels ++: extraLabels.toArray)
    }
    onceExecutorExecutionContext.getLabels.foreach {
      case jobLabel: JobLabel =>
        onceExecutorExecutionContext.setJobId(jobLabel.getJobId)
        info(s"JobId: ${onceExecutorExecutionContext.getJobId}.")
      case _ =>
    }
  }

  override def init(): Unit = tryReady()

  override def tryReady(): Boolean = true

}

trait ManageableOnceExecutor extends AccessibleExecutor with OnceExecutor with ResourceExecutor {

  private val notifyListeners = new ArrayBuffer[ExecuteResponse => Unit]
  private var response: ExecuteResponse = _

  override def tryReady(): Boolean = {
    transition(NodeStatus.Running)
    super.tryReady()
  }

  override def execute(onceExecutorExecutionContext: OnceExecutorExecutionContext): ExecuteResponse = {
    submit(onceExecutorExecutionContext)
    waitToRunning()
    transition(NodeStatus.Busy)
    new AsynReturnExecuteResponse {
      override def notify(rs: ExecuteResponse => Unit): Unit = notifyListeners += rs
    }
  }

  protected def submit(onceExecutorExecutionContext: OnceExecutorExecutionContext): Unit

  protected def waitToRunning(): Unit

  def waitForComplete(): Unit = this synchronized wait()

  def getResponse: ExecuteResponse = response

  protected def setResponse(response: ExecuteResponse): Unit = this.response = response

  override protected def onStatusChanged(fromStatus: NodeStatus, toStatus: NodeStatus): Unit = {
    if(NodeStatus.isCompleted(toStatus)) {
      if(response == null) toStatus match {
        case NodeStatus.Success => response = SuccessExecuteResponse()
        case _ => response = ErrorExecuteResponse("Unknown reason.", null)
      }
      Utils.tryFinally(notifyListeners.foreach(_(getResponse)))(this synchronized notifyAll)
    }
    super.onStatusChanged(fromStatus, toStatus)
  }

  override def tryShutdown(): Boolean = {
    this.ensureAvailable(transition(NodeStatus.ShuttingDown))
    close()
    true
  }

  def tryFailed(): Boolean = {
    this.whenStatus(NodeStatus.ShuttingDown, transition(NodeStatus.Failed))
    true
  }

  def trySucceed(): Boolean = {
    this.ensureAvailable(transition(NodeStatus.Success))
    true
  }

  override protected def callback(): Unit = {}

  override def supportCallBackLogs(): Boolean = true

}