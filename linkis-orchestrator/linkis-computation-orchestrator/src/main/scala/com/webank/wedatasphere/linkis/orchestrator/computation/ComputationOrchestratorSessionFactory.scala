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
 *
 */

package com.webank.wedatasphere.linkis.orchestrator.computation

import com.webank.wedatasphere.linkis.common.utils.ClassUtils
import com.webank.wedatasphere.linkis.orchestrator.computation.conf.ComputationOrchestratorConf
import com.webank.wedatasphere.linkis.orchestrator.core.OrchestratorSessionBuilder
import com.webank.wedatasphere.linkis.orchestrator.{Orchestrator, OrchestratorSession}
import org.apache.commons.lang.StringUtils

/**
  *
  *
  */
trait ComputationOrchestratorSessionFactory {

  def getOrCreateSession(id: String): OrchestratorSession

  def getOrchestrator(): Orchestrator

  def createSessionBuilder(id: String): OrchestratorSessionBuilder

  def getOrCreateSession(orchestratorSessionBuilder: OrchestratorSessionBuilder): OrchestratorSession
}

object ComputationOrchestratorSessionFactory {

  private var sessionFactory: ComputationOrchestratorSessionFactory = _

  def getOrCreateExecutionFactory(): ComputationOrchestratorSessionFactory = {

    if (sessionFactory == null) synchronized {
      if (sessionFactory == null) {
        sessionFactory = if (StringUtils.isNotBlank(ComputationOrchestratorConf.COMPUTATION_SESSION_FACTORY_CLASS.getValue))
          ClassUtils.getClassInstance(ComputationOrchestratorConf.COMPUTATION_SESSION_FACTORY_CLASS.getValue)
        else {
          new ComputationOrchestratorSessionFactoryImpl
        }
      }
    }
    sessionFactory
  }
}