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

import java.util
import java.util.concurrent.Future
import java.util.function.Supplier

import com.webank.wedatasphere.linkis.common.utils.{ByteTimeUtils, Utils, VariableUtils}
import com.webank.wedatasphere.linkis.engineconn.once.executor.OnceExecutorExecutionContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.deployment.YarnPerJobClusterDescriptorAdapter
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.FlinkInitFailedException
import com.webank.wedatasphere.linkis.governance.common.paser.{CodeParserFactory, CodeType}
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.scheduler.executer.ErrorExecuteResponse
import org.apache.commons.lang.StringUtils
import org.apache.flink.client.deployment.ClusterClientJobClientAdapter
import org.apache.flink.client.program.{ClusterClient, ClusterClientProvider}
import org.apache.flink.table.api.{ResultKind, TableResult}
import org.apache.hadoop.yarn.api.records.ApplicationId


class FlinkCodeOnceExecutor(override val id: Long,
                            override protected val flinkEngineConnContext: FlinkEngineConnContext)
  extends FlinkOnceExecutor[YarnPerJobClusterDescriptorAdapter] {

  private var codes: String = _
  private var future: Future[_] = _

  override def doSubmit(onceExecutorExecutionContext: OnceExecutorExecutionContext,
                        options: Map[String, String]): Unit = {
    codes = options(TaskConstant.CODE)
    options(TaskConstant.RUNTYPE) match {
      case "sql" =>
        if(StringUtils.isBlank(codes)) throw new FlinkInitFailedException(s"The sql code is empty.")
        info(s"Ready to submit flink application, sql is: $codes.")
        val variableMap = if(onceExecutorExecutionContext.getOnceExecutorContent.getVariableMap != null)
          onceExecutorExecutionContext.getOnceExecutorContent.getVariableMap.asInstanceOf[util.Map[String, Any]]
        else new util.HashMap[String, Any]
        codes = VariableUtils.replace(codes, variableMap)
        info(s"After variable replace, sql is: $codes.")
      case runType =>
        // Now, only support sql code.
        throw new FlinkInitFailedException(s"Not support runType $runType.")
    }
    future = Utils.defaultScheduler.submit(new Runnable {
      override def run(): Unit = {
        info("Try to execute codes.")
        Utils.tryCatch(CodeParserFactory.getCodeParser(CodeType.SQL).parse(codes).filter(StringUtils.isNotBlank).foreach(runCode)){ t =>
          error("Run code failed!", t)
          setResponse(ErrorExecuteResponse("Run code failed!", t))
          tryFailed()
        }
        info("All codes completed, now stop FlinkEngineConn.")
        closeDaemon()
        trySucceed()
        this synchronized notify()
        clusterDescriptor.close()
      }
    })
    this synchronized wait()
  }

  /**
    * Only support to execute sql in order, so it is problematic if more than one insert sql is submitted.
    */
  protected def runCode(code: String): Unit = {
    if(isClosed) return
    val trimmedCode = StringUtils.trim(code)
    info(s"$getId >> " + trimmedCode)
    val startTime = System.currentTimeMillis
    val tableResult = flinkEngineConnContext.getExecutionContext.wrapClassLoader(new Supplier[TableResult]{
      override def get(): TableResult = flinkEngineConnContext.getExecutionContext.getTableEnvironment.executeSql(trimmedCode)
    })
    if(tableResult.getJobClient.isPresent) {
      val jobClient = tableResult.getJobClient.get
      jobClient match {
        case adaptor: ClusterClientJobClientAdapter[ApplicationId] =>
          info(s"jobId is ${jobClient.getJobID.toHexString}")
          clusterDescriptor.deployCluster(jobClient.getJobID, FlinkCodeOnceExecutor.getClusterClient(adaptor))
      }
      this synchronized notify()
      tableResult.await()
    }
    tableResult.getResultKind match {
      case ResultKind.SUCCESS_WITH_CONTENT =>
        tableResult.print()
      case _ =>
    }
    info(s"Costs ${ByteTimeUtils.msDurationToString(System.currentTimeMillis - startTime)} to complete.")
  }

  override def close(): Unit = {
    future.cancel(true)
    super.close()
  }
}
object FlinkCodeOnceExecutor {

  private val clusterClientProviderField = classOf[ClusterClientJobClientAdapter[ApplicationId]].getDeclaredField("clusterClientProvider")
  clusterClientProviderField.setAccessible(true)

  def getClusterClient(adaptor: ClusterClientJobClientAdapter[ApplicationId]): ClusterClient[ApplicationId] = {
    clusterClientProviderField.get(adaptor) match {
      case provider: ClusterClientProvider[ApplicationId] => provider.getClusterClient
    }
  }

}