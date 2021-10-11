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

package com.webank.wedatasphere.linkis.orchestrator

import java.io.Closeable

import com.webank.wedatasphere.linkis.common.utils.{ClassUtils, Logging}
import com.webank.wedatasphere.linkis.orchestrator.conf.OrchestratorConfiguration.ORCHESTRATOR_BUILDER_CLASS
import com.webank.wedatasphere.linkis.orchestrator.core.OrchestratorSessionBuilder
import com.webank.wedatasphere.linkis.orchestrator.core.impl.OrchestratorImpl
import org.apache.commons.lang.StringUtils

/**
  *
  */
trait Orchestrator extends Closeable {

  def getName: String

  def version: String

  def initialize(): Unit

  def getOrchestratorContext: OrchestratorContext

  def createOrchestratorSessionBuilder(): OrchestratorSessionBuilder

  def getActiveOrchestratorSession: OrchestratorSession

  def setActiveOrchestratorSession(orchestratorSession: OrchestratorSession): Unit

  def getDefaultOrchestratorSession: OrchestratorSession

  def setDefaultOrchestratorSession(orchestratorSession: OrchestratorSession): Unit

}

object Orchestrator extends Logging {

  private var orchestrator: Orchestrator = _

  def getOrchestrator: Orchestrator = {
    if(orchestrator == null) synchronized {
      if(orchestrator == null) {
        val orchestratorBuilder = if(StringUtils.isNotBlank(ORCHESTRATOR_BUILDER_CLASS.getValue))
          ClassUtils.getClassInstance(ORCHESTRATOR_BUILDER_CLASS.getValue)
          else () => new OrchestratorImpl
        info("Use " + orchestratorBuilder.getClass.getName + " to instance a new orchestrator.")
        orchestrator = orchestratorBuilder()
      }
    }
    orchestrator
  }

  type OrchestratorBuilder = () => Orchestrator

}
