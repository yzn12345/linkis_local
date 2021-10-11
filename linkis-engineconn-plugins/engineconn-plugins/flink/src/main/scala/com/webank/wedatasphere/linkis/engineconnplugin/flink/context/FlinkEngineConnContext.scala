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
package com.webank.wedatasphere.linkis.engineconnplugin.flink.context

import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.config.Environment
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.context.ExecutionContext


class FlinkEngineConnContext(environmentContext: EnvironmentContext) {

  private var executionContext: ExecutionContext = _

  def getEnvironmentContext: EnvironmentContext = environmentContext

  def getExecutionContext: ExecutionContext = executionContext

  def setExecutionContext(executionContext: ExecutionContext): Unit = this.executionContext = executionContext

  def newExecutionContextBuilder(environment: Environment): ExecutionContext.Builder =
    ExecutionContext.builder(environmentContext.getDefaultEnv, environment, environmentContext.getDependencies,
      environmentContext.getFlinkConfig)

}
