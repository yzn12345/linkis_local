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

package com.webank.wedatasphere.linkis.storage.io.client

import java.lang.reflect.UndeclaredThrowableException
import java.util

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.storage.io.orchestrator.IOFileOrchestratorFactory
import com.webank.wedatasphere.linkis.storage.io.utils.IOClientUtils
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineType
import com.webank.wedatasphere.linkis.manager.label.entity.entrance.BindEngineLabel
import com.webank.wedatasphere.linkis.manager.label.utils.{EngineTypeLabelCreator, LabelUtil}
import com.webank.wedatasphere.linkis.orchestrator.ecm.conf.ECMPluginConf
import com.webank.wedatasphere.linkis.orchestrator.execution.{ArrayResultSetTaskResponse, FailedTaskResponse, SucceedTaskResponse}
import com.webank.wedatasphere.linkis.rpc.exception.NoInstanceExistsException
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import com.webank.wedatasphere.linkis.storage.domain.MethodEntity
import com.webank.wedatasphere.linkis.storage.exception.{FSNotInitException, StorageErrorCode, StorageErrorException}
import com.webank.wedatasphere.linkis.storage.io.conf.IOFileClientConf
import com.webank.wedatasphere.linkis.storage.io.orchestrator.IOFileOrchestratorFactory
import com.webank.wedatasphere.linkis.storage.io.utils.IOClientUtils
import org.springframework.stereotype.Component


@Component
class DefaultIOClient extends IOClient with Logging {

  private  val loadBalanceLabel = IOClientUtils.getDefaultLoadBalanceLabel

  private  val extraLabels: Array[Label[_]] = Utils.tryCatch(IOClientUtils.getExtraLabels()){
    case throwable: Throwable =>
      error("Failed to create extraLabels, No extra labels will be used", throwable)
      Array.empty[Label[_]]
  }

  override def execute(user: String, methodEntity: MethodEntity, bindEngineLabel: BindEngineLabel): String = {
    val params = new util.HashMap[String, Any]()
    if (null != bindEngineLabel) {
      IOClientUtils.addLabelToParams(bindEngineLabel, params)
    }
    executeResult(user, methodEntity, params)
  }

  def executeResult(user: String, methodEntity: MethodEntity, params: java.util.Map[String, Any], retryLimit: Int = 0): String = {
    val engineTypeLabel = EngineTypeLabelCreator.createEngineTypeLabel(EngineType.mapFsTypeToEngineType(methodEntity.fsType))
    IOClientUtils.addLabelToParams(loadBalanceLabel, params)
    IOClientUtils.addLabelToParams(engineTypeLabel, params)
    extraLabels.foreach(label => IOClientUtils.addLabelToParams(label, params))
    val startTime = System.currentTimeMillis()
    val jobReq = IOClientUtils.buildJobReq(user, methodEntity, params)
    if (null == jobReq) {
      throw new StorageErrorException(IOFileClientConf.IO_EXECUTE_FAILED_CODE, s"Job with id ${jobReq.getId} failed to execute method fs for user : ${user}, code : $methodEntity， Because jobReq is null")
    }
    val bindEngineLabel = LabelUtil.getBindEngineLabel(jobReq.getLabels)
    if (null == bindEngineLabel) {
      throw new StorageErrorException(IOFileClientConf.IO_EXECUTE_FAILED_CODE, s"Job with id ${jobReq.getId} failed to execute method fs for user : ${user}, code : $methodEntity， Because bindEngineLabel is null")
    }

    val orchestration = IOFileOrchestratorFactory.getOrchestratorSession().orchestrate(jobReq)
    val orchestrationTime = System.currentTimeMillis()
    var response = orchestration.execute()
    var initCount = 0
    while (!response.isInstanceOf[SucceedTaskResponse] && initCount < retryLimit) {
      initCount += 1
      info(s"JobId ${jobReq.getId} execute method ${methodEntity} failed, to retry $initCount")
      val reTryOrchestration = IOFileOrchestratorFactory.getOrchestratorSession().orchestrate(jobReq)
      response = reTryOrchestration.execute()
    }
    val result: String = response match {
      case succeedResponse: SucceedTaskResponse =>
        succeedResponse match {
          case arrayResultSetPathResp: ArrayResultSetTaskResponse =>
            val firstResultSet = arrayResultSetPathResp.getResultSets.headOption.orNull
            if (null != firstResultSet) {
              // tod check
              firstResultSet.result
            } else {
              info(s"JobId ${jobReq.getId} execute method ${methodEntity} with null result.")
              IOClientUtils.SUCCESS
            }
          case _ =>
            info(s"JobId ${jobReq.getId} execute method ${methodEntity} with null result.")
            IOClientUtils.SUCCESS
        }
      case failedResponse: FailedTaskResponse =>
        val msg = s"IO_FILE job: ${jobReq.getId} failed to execute code : ${methodEntity}, reason : ${failedResponse.getErrorMsg}."
        info(msg)
        if (failedResponse.getErrorMsg.contains(StorageErrorCode.FS_NOT_INIT.getMessage) || failedResponse.getErrorMsg.contains(ECMPluginConf.ECM_MARK_CACHE_ERROR_CODE.toString)) {
              throw new FSNotInitException()
        }
        throw new StorageErrorException(IOFileClientConf.IO_EXECUTE_FAILED_CODE, msg)
      case o =>
        val msg = s"IO_FILE job : ${jobReq.getId} failed to execute code : ${methodEntity}, return a unknown response : ${BDPJettyServerHelper.gson.toJson(o)}"
        warn(msg)
        throw new StorageErrorException(IOFileClientConf.IO_EXECUTE_UNKNOWN_REASON_CODE, msg)
    }
    val executeTime = System.currentTimeMillis()
    info(s"${jobReq.getId} execute method ${methodEntity.methodName}, orchestratorTime(${orchestrationTime - startTime}ms) execute time(${executeTime - orchestrationTime}ms)")
    result
  }

  override def executeWithRetry(user: String, methodEntity: MethodEntity, bindEngineLabel: BindEngineLabel, reTryLimit: Int = defaultRetry): String = {
    val params = new util.HashMap[String, Any]()
    if (null != bindEngineLabel) {
      IOClientUtils.addLabelToParams(bindEngineLabel, params)
    }
    val result = executeResult(user, methodEntity, params, reTryLimit)
    result
  }

}
