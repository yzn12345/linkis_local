/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineconn.computation.executor.execute

import java.io.File
import java.util

import com.webank.wedatasphere.linkis.common.io.resultset.{ResultSet, ResultSetWriter}
import com.webank.wedatasphere.linkis.common.io.{FsPath, MetaData, Record}
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.cs.client.utils.ContextServiceUtils
import com.webank.wedatasphere.linkis.cs.storage.CSTableResultSetWriter
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener.event.{TaskLogUpdateEvent, TaskProgressUpdateEvent, TaskResultCreateEvent, TaskResultSizeCreatedEvent}
import com.webank.wedatasphere.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import com.webank.wedatasphere.linkis.engineconn.executor.ExecutorExecutionContext
import com.webank.wedatasphere.linkis.engineconn.executor.entity.Executor
import com.webank.wedatasphere.linkis.engineconn.executor.listener.{EngineConnAsyncListenerBus, EngineConnSyncListenerBus, ExecutorListenerBusContext}
import com.webank.wedatasphere.linkis.governance.common.exception.engineconn.EngineConnExecutorErrorException
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.scheduler.executer.{AliasOutputExecuteResponse, OutputExecuteResponse}
import com.webank.wedatasphere.linkis.storage.resultset.table.TableResultSet
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import com.webank.wedatasphere.linkis.storage.{LineMetaData, LineRecord}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

class EngineExecutionContext(executor: ComputationExecutor, executorUser: String = Utils.getJvmUser) extends
    ExecutorExecutionContext with Logging {

  private val resultSetFactory = ResultSetFactory.getInstance

  private var defaultResultSetWriter: ResultSetWriter[_ <: MetaData, _ <: Record] = _

  private var resultSize = 0
  private var enableResultsetMetaWithTableName = false

  private val properties: java.util.Map[String, Object] = new util.HashMap[String, Object]()

  private var totalParagraph = 0
  private var currentParagraph = 0

  def getTotalParagraph: Int = totalParagraph

  def setTotalParagraph(totalParagraph: Int): Unit = this.totalParagraph = totalParagraph

  def getCurrentParagraph: Int = currentParagraph

  def setCurrentParagraph(currentParagraph: Int): Unit = this.currentParagraph = currentParagraph

  def pushProgress(progress: Float, progressInfo: Array[JobProgressInfo]): Unit = if (! executor.isInternalExecute){
    val listenerBus = getEngineSyncListenerBus
    getJobId.foreach(jId => {
      listenerBus.postToAll(TaskProgressUpdateEvent(jId, progress, progressInfo))
    })
  }

  def sendResultSet(resultSetWriter: ResultSetWriter[_ <: MetaData, _ <: Record]): Unit = {
    info("Start to send res to entrance")
    val fileName = new File(resultSetWriter.toFSPath.getPath).getName
    val index = if (fileName.indexOf(".") < 0) fileName.length else fileName.indexOf(".")
    val alias = if (fileName.startsWith("_")) fileName.substring(1, index) else fileName.substring(0, fileName.indexOf("_"))
    //resultSetWriter.flush()
    Utils.tryFinally(sendResultSet(resultSetWriter.toString(), alias)) {
      IOUtils.closeQuietly(resultSetWriter)
      resultSetWriters synchronized resultSetWriters -= resultSetWriter
    }
  }

  def sendResultSet(output: String): Unit = sendResultSet(output, "_" + aliasNum.getAndIncrement())

  def appendTextResultSet(output: String): Unit = {
    if (defaultResultSetWriter == null) aliasNum synchronized {
      if (defaultResultSetWriter == null) {
        defaultResultSetWriter = createDefaultResultSetWriter(ResultSetFactory.TEXT_TYPE)
        defaultResultSetWriter.addMetaData(new LineMetaData())
      }
    }
    defaultResultSetWriter.addRecord(new LineRecord(output))
  }

  private def sendResultSet(output: String, alias: String): Unit = {
    if (StringUtils.isEmpty(output)) return
    if (resultSetFactory.isResultSetPath(output) || resultSetFactory.isResultSet(output)) {
      val listenerBus = getEngineSyncListenerBus
      getJobId.foreach(jId => {
        //TODO peacewong executor.getEngineServerContext().getEngineAsyncListenerBus().post(ResultSetCreatedEvent(jId, output, alias))
        listenerBus.postToAll(TaskResultCreateEvent(jId, output, alias))
        resultSize += 1
      })
    } else throw new EngineConnExecutorErrorException(50050, "unknown resultSet: " + output)
  }

  def sendResultSet(outputExecuteResponse: OutputExecuteResponse): Unit = outputExecuteResponse match {
    case AliasOutputExecuteResponse(alias, output) => sendResultSet(output, alias)
    case output: OutputExecuteResponse => sendResultSet(output.getOutput, "_" + aliasNum.getAndIncrement())
  }

  def getProperties: java.util.Map[String, Object] = properties

  def addProperty(key: String, value: String): Unit = properties.put(key, value)

  override protected def getResultSetByType(resultSetType: String): ResultSet[_ <: MetaData, _ <: Record] =
    resultSetFactory.getResultSetByType(resultSetType)

  override protected def getDefaultResultSetByType: String = resultSetFactory.getResultSetType(0)

  def newResultSetWriter(resultSet: ResultSet[_ <: MetaData, _ <: Record],
                         resultSetPath: FsPath,
                         alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] = {
    //update by peaceWong 20200402
    resultSet match {
      case result: TableResultSet =>
        val contextIDStr = ContextServiceUtils.getContextIDStrByMap(getProperties)
        val nodeName = ContextServiceUtils.getNodeNameStrByMap(getProperties)
        if (StringUtils.isNotBlank(contextIDStr) && StringUtils.isNotBlank(nodeName)) {
          val csWriter = new CSTableResultSetWriter(result, ComputationExecutorConf.ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong, resultSetPath, contextIDStr, nodeName, alias)
          csWriter.setProxyUser(executorUser)
          csWriter
        } else {
          ResultSetWriter.getResultSetWriter(resultSet, ComputationExecutorConf.ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong, resultSetPath, executorUser)
        }
      case _ => ResultSetWriter.getResultSetWriter(resultSet, ComputationExecutorConf.ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong, resultSetPath, executorUser)
    }
    //update by peaceWong 20200402 end
  }

  def appendStdout(log: String): Unit = if (executor.isInternalExecute) {
    executor.info(log)
  } else {
    val listenerBus = getEngineSyncListenerBus
    // jobId.foreach(jId => listenerBus.post(TaskLogUpdateEvent(jId, log)))
    getJobId.foreach(jId => listenerBus.postToAll(TaskLogUpdateEvent(jId, log)))
  }

  override def close(): Unit = {
    resultSetWriters.toArray.foreach(sendResultSet)
    val listenerBus = getEngineSyncListenerBus
    getJobId.foreach(jId => {
      listenerBus.postToAll(TaskResultSizeCreatedEvent(jId, resultSize))
    })
    resultSetWriters.clear()
  }

  private def getEngineAsyncListenerBus: EngineConnAsyncListenerBus = {
    ExecutorListenerBusContext.getExecutorListenerBusContext.getEngineConnAsyncListenerBus
  }

  private def getEngineSyncListenerBus: EngineConnSyncListenerBus = {
    ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnSyncListenerBus
  }

  def getExecutor: Executor = executor

  def getEnableResultsetMetaWithTableName = enableResultsetMetaWithTableName

  def setEnableResultsetMetaWithTableName(withTableName: Boolean): Unit = this.enableResultsetMetaWithTableName = withTableName
}
