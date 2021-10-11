/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engineconnplugin.flink.executor

import java.io.Closeable
import java.util
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.{ByteTimeUtils, Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.{ComputationExecutor, EngineExecutionContext}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.deployment.{ClusterDescriptorAdapterFactory, YarnSessionClusterDescriptorAdapter}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ResultKind
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.{AbstractJobOperation, JobOperation, OperationFactory}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.parser.{SqlCommand, SqlCommandParser}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.{ExecutorInitException, SqlParseException}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.listener.RowsType.RowsType
import com.webank.wedatasphere.linkis.engineconnplugin.flink.listener.{FlinkStreamingResultSetListener, InteractiveFlinkStatusListener}
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory
import org.apache.calcite.rel.metadata.{JaninoRelMetadataProvider, RelMetadataQueryBase}
import org.apache.flink.api.common.JobStatus._
import org.apache.flink.table.planner.plan.metadata.FlinkDefaultRelMetadataProvider
import org.apache.flink.yarn.configuration.YarnConfigOptions
import org.apache.hadoop.yarn.util.ConverterUtils

import scala.collection.JavaConversions._


class FlinkSQLComputationExecutor(id: Long,
                                  override protected val flinkEngineConnContext: FlinkEngineConnContext) extends ComputationExecutor with FlinkExecutor {

  private var operation: JobOperation = _
  private var clusterDescriptor: YarnSessionClusterDescriptorAdapter = _

  override def init(): Unit = {
    ClusterDescriptorAdapterFactory.create(flinkEngineConnContext.getExecutionContext) match {
      case adapter: YarnSessionClusterDescriptorAdapter => clusterDescriptor = adapter
      case adapter if adapter != null => throw new ExecutorInitException(s"Not support ${adapter.getClass.getSimpleName} for FlinkSQLComputationExecutor.")
      case _ => throw new ExecutorInitException("Fatal error, ClusterDescriptorAdapter is null, please ask admin for help.")
    }
    info("Try to start a yarn-session application for interactive query.")
    clusterDescriptor.deployCluster()
    val applicationId = ConverterUtils.toString(clusterDescriptor.getClusterID)
    setApplicationId(applicationId)
    setApplicationURL(clusterDescriptor.getWebInterfaceUrl)
    flinkEngineConnContext.getEnvironmentContext.getFlinkConfig.setString(YarnConfigOptions.APPLICATION_ID, applicationId)
    info(s"Application is started, applicationId: $getApplicationId, applicationURL: $getApplicationURL.")
    super.init()
  }

  override def executeLine(engineExecutionContext: EngineExecutionContext, code: String): ExecuteResponse = {
    val callOpt = SqlCommandParser.getSqlCommandParser.parse(code.trim, true)
    val callSQL = if (!callOpt.isPresent) throw new SqlParseException("Unknown statement: " + code)
      else callOpt.get
    RelMetadataQueryBase.THREAD_PROVIDERS.set(JaninoRelMetadataProvider.of(FlinkDefaultRelMetadataProvider.INSTANCE))
    val operation = OperationFactory.getOperationFactory.createOperation(callSQL, flinkEngineConnContext)
    operation match {
      case jobOperation: JobOperation =>
        jobOperation.setClusterDescriptorAdapter(clusterDescriptor)
        this.operation = jobOperation
        jobOperation.addFlinkListener(new FlinkSQLStatusListener(jobOperation, engineExecutionContext))
        if(callSQL.command == SqlCommand.SELECT) {
          jobOperation.addFlinkListener(new FlinkSQLStreamingResultSetListener(jobOperation, engineExecutionContext))
          val properties: util.Map[String, String] = engineExecutionContext.getProperties.map {
            case (k, v: String) => (k, v)
            case (k, v) if v != null => (k, v.toString)
            case (k, _) => (k, null)
          }
          jobOperation.addFlinkListener(new DevFlinkSQLStreamingListener(jobOperation, properties))
        }
      case _ =>
    }
    val resultSet = operation.execute
    resultSet.getResultKind match {
      case ResultKind.SUCCESS =>
        new SuccessExecuteResponse
      case ResultKind.SUCCESS_WITH_CONTENT if !operation.isInstanceOf[JobOperation] =>
        FlinkExecutor.writeAndSendResultSet(resultSet, engineExecutionContext)
        new SuccessExecuteResponse
      case _ =>
        operation match {
          case jobOperation: AbstractJobOperation =>
            val jobId = jobOperation.transformToJobInfo(resultSet)
            setJobID(jobId.toHexString)
            setYarnMode("client")
            jobOperation.getFlinkStatusListeners.get(0) match {
              case listener: FlinkSQLStatusListener => listener.waitForCompleted()
                return listener.getResponse
              case _ =>
            }
          case jobOperation: JobOperation =>
            jobOperation.getFlinkListeners.find(_.isInstanceOf[FlinkSQLStatusListener]).foreach { case listener: FlinkSQLStatusListener =>
              listener.waitForCompleted()
              return listener.getResponse
            }
        }
        new SuccessExecuteResponse
    }
  }

  override def executeCompletely(engineExecutorContext: EngineExecutionContext, code: String, completedLine: String): ExecuteResponse = {
    val newCode = completedLine + code
    info("newCode is " + newCode)
    executeLine(engineExecutorContext, newCode)
  }

  //TODO wait for completed.
  override def progress(): Float = if(operation == null) 0 else operation.getJobStatus match {
    case jobState if jobState.isGloballyTerminalState => 1
    case RUNNING => 0.5f
    case _ => 0
  }

  override def getProgressInfo: Array[JobProgressInfo] = Array.empty

  override def getId: String = "FlinkComputationSQL_"+ id

  override def close(): Unit = {
    if(operation != null) {
      operation.cancelJob()
    }
    flinkEngineConnContext.getExecutionContext.createClusterDescriptor().close()
    flinkEngineConnContext.getExecutionContext.getClusterClientFactory.close()
    super.close()
  }
}

class FlinkSQLStatusListener(jobOperation: JobOperation, engineExecutionContext: EngineExecutionContext) extends InteractiveFlinkStatusListener {

  private var resp: ExecuteResponse = _
  private val startTime = System.currentTimeMillis

  override def onSuccess(rows: Int, rowsType: RowsType): Unit = {
    // Only success need to close.
    val executeCostTime = ByteTimeUtils.msDurationToString(System.currentTimeMillis - startTime)
    info(s"Time taken: $executeCostTime, $rowsType $rows row(s), wait resultSet to be stored.")
    Utils.tryCatch(jobOperation.getFlinkListeners.foreach {
      case listener: Closeable =>
        listener.close()
      case _ =>
    }){t =>
      onFailed("Failed to close Listeners", t, rowsType)
      return
    }
    resp = new SuccessExecuteResponse
    val totalCostTime = ByteTimeUtils.msDurationToString(System.currentTimeMillis - startTime)
    info(s"Time taken: $totalCostTime, $rowsType $rows row(s).")
    Utils.tryFinally(engineExecutionContext.appendStdout(s"Time taken: $totalCostTime, $rowsType $rows row(s)."))(synchronized(notify()))
  }

  override def tryFailed(message: String, t: Throwable): Unit = {
    error("Execute failed! Reason: " + message, t)
    resp = ErrorExecuteResponse(message, t)
    synchronized(notify())
  }

  def getResponse: ExecuteResponse = resp

  def waitForCompleted(maxWaitTime: Long): Unit = synchronized {
    if(maxWaitTime < 0) wait() else wait(maxWaitTime)
  }

  def waitForCompleted(): Unit = waitForCompleted(-1)
}

class FlinkSQLStreamingResultSetListener(jobOperation: JobOperation,
                                         engineExecutionContext: EngineExecutionContext)
  extends FlinkStreamingResultSetListener with Closeable with Logging {

  private val resultSetWriter = engineExecutionContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)

  override def onResultSetPulled(rows: Int): Unit = {
    info(s"$rows resultSets has pulled.")
    FlinkExecutor.writeResultSet(jobOperation.getJobResult.get(), resultSetWriter)
  }

  override def close(): Unit = engineExecutionContext.sendResultSet(resultSetWriter)
}

class DevFlinkSQLStreamingListener(jobOperation: JobOperation,
                                   maxWrittenLines: Int,
                                   maxWaitForResultTime: Long) extends FlinkStreamingResultSetListener with Logging {

  def this(jobOperation: JobOperation) =
    this(jobOperation,
      FlinkEnvConfiguration.FLINK_SQL_DEV_SELECT_MAX_LINES.getValue,
      FlinkEnvConfiguration.FLINK_SQL_DEV_RESULT_MAX_WAIT_TIME.getValue.toLong
    )

  def this(jobOperation: JobOperation, properties: util.Map[String, String]) =
    this(jobOperation,
      FlinkEnvConfiguration.FLINK_SQL_DEV_SELECT_MAX_LINES.getValue(properties),
      FlinkEnvConfiguration.FLINK_SQL_DEV_RESULT_MAX_WAIT_TIME.getValue(properties).toLong
    )

  private var lastPulledTime = System.currentTimeMillis
  private var writtenLines = 0

  override def onResultSetPulled(rows: Int): Unit = {
    info("begin to pull result set in DevFlinkSQLStreamingListener")
    lastPulledTime = System.currentTimeMillis
    writtenLines += rows
    if(writtenLines >= maxWrittenLines) {
      warn(s"The returned resultSet reached max lines $writtenLines, now kill the job automatic. Notice: only the dev environment will touch off the automatic kill.")
      stopJobOperation()
    }
  }

  private val future = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = if(System.currentTimeMillis - lastPulledTime > maxWaitForResultTime) {
      warn(s"Job killed since reached the max time ${ByteTimeUtils.msDurationToString(maxWaitForResultTime)} of waiting for resultSet. Notice: only the dev environment will touch off the automatic kill.")
      stopJobOperation()
    }
  }, maxWaitForResultTime, maxWaitForResultTime, TimeUnit.MILLISECONDS)

  def stopJobOperation(): Unit = {
    future.cancel(false)
    jobOperation.getFlinkListeners.foreach {
      case listener: InteractiveFlinkStatusListener => listener.markSuccess(writtenLines)
      case _ =>
    }
    jobOperation.cancelJob()
  }
}