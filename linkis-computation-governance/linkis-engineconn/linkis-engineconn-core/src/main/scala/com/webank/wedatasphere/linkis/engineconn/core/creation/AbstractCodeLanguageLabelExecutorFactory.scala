/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.engineconn.core.creation


import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineconn.executor.entity.LabelExecutor
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.CodeLanguageLabelExecutorFactory
import com.webank.wedatasphere.linkis.manager.engineplugin.common.exception.{EngineConnPluginErrorCode, EngineConnPluginErrorException}
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.CodeLanguageLabel
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType.RunType


trait AbstractCodeLanguageLabelExecutorFactory extends CodeLanguageLabelExecutorFactory {

  override def canCreate(labels: Array[Label[_]]): Boolean  = {
    val codeLanguageLabel = getDefaultCodeLanguageLabel
    if (null == codeLanguageLabel) {
      error("DefaultEngineRunTypeLabel must not be null!")
      throw new EngineConnPluginErrorException(EngineConnPluginErrorCode.INVALID_RUNTYPE, "DefaultEngineRunTypeLabel cannot be null.")
    }
    labels.exists {
      case label: CodeLanguageLabel =>
        info(s"Executor runType is ${codeLanguageLabel.getCodeType}, the task runType is ${label.getCodeType}.")
        getSupportRunTypes.exists(_.equalsIgnoreCase(label.getCodeType))
      case _ => false
    }
  }

  protected def getSupportRunTypes: Array[String] = Array(getRunType.toString)

  override def createExecutor(engineCreationContext: EngineCreationContext,
                              engineConn: EngineConn): LabelExecutor = {
    createExecutor(engineCreationContext, engineConn, null)
  }

  protected def newExecutor(id: Int,
                            engineCreationContext: EngineCreationContext,
                            engineConn: EngineConn,
                            labels: Array[Label[_]]): LabelExecutor

  override def createExecutor(engineCreationContext: EngineCreationContext,
                              engineConn: EngineConn,
                              labels: Array[Label[_]]): LabelExecutor = {
    val id = ExecutorManager.getInstance.generateExecutorId()
    val executor = newExecutor(id, engineCreationContext, engineConn, labels)
    if(labels != null){
      labels.foreach(executor.getExecutorLabels().add)
    }
    executor.getExecutorLabels().add(getDefaultCodeLanguageLabel)
    executor
  }

  protected def getRunType: RunType

  override def getDefaultCodeLanguageLabel: CodeLanguageLabel = {
    val runTypeLabel = new CodeLanguageLabel
    runTypeLabel.setCodeType(getRunType.toString)
    runTypeLabel
  }
}
