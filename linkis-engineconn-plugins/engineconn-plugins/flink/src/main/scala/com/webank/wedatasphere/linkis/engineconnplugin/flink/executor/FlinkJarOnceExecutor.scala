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

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engineconn.once.executor.OnceExecutorExecutionContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.deployment.YarnApplicationClusterDescriptorAdapter
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration._
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import org.apache.commons.lang.StringUtils

import scala.concurrent.duration.Duration


class FlinkJarOnceExecutor(override val id: Long,
                           override protected val flinkEngineConnContext: FlinkEngineConnContext)
  extends FlinkOnceExecutor[YarnApplicationClusterDescriptorAdapter] {


  override def doSubmit(onceExecutorExecutionContext: OnceExecutorExecutionContext,
                        options: Map[String, String]): Unit = {
    val args = FLINK_APPLICATION_ARGS.getValue(options)
    val programArguments = if(StringUtils.isNotEmpty(args)) args.split(" ") else Array.empty[String]
    val mainClass = FLINK_APPLICATION_MAIN_CLASS.getValue(options)
    info(s"Ready to submit flink application, mainClass: $mainClass, args: $args.")
    clusterDescriptor.deployCluster(programArguments, mainClass)
  }

  override protected def waitToRunning(): Unit = {
    Utils.waitUntil(() => clusterDescriptor.initJobId(), Duration.Inf)
    super.waitToRunning()
  }
}