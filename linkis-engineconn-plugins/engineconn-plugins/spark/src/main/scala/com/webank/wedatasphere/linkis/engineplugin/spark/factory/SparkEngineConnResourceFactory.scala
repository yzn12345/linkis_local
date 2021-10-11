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

package com.webank.wedatasphere.linkis.engineplugin.spark.factory

import java.util

import com.webank.wedatasphere.linkis.common.utils.{ByteTimeUtils, Logging}
import com.webank.wedatasphere.linkis.engineplugin.spark.config.SparkResourceConfiguration._
import com.webank.wedatasphere.linkis.manager.common.entity.resource.{DriverAndYarnResource, LoadInstanceResource, Resource, YarnResource}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.AbstractEngineResourceFactory
import org.apache.commons.lang.StringUtils


class SparkEngineConnResourceFactory extends AbstractEngineResourceFactory with Logging {

  override protected def getRequestResource(properties: util.Map[String, String]): Resource = {
    val executorNum = LINKIS_SPARK_EXECUTOR_INSTANCES.getValue(properties)
    val executorMemory = LINKIS_SPARK_EXECUTOR_MEMORY.getValue(properties)
    val executorMemoryWithUnit = if (StringUtils.isNumeric(executorMemory)) {
      executorMemory + "g"
    } else {
      executorMemory
    }
    val driverMemory = LINKIS_SPARK_DRIVER_MEMORY.getValue(properties)
    val driverMemoryWithUnit = if (StringUtils.isNumeric(driverMemory)) {
      driverMemory + "g"
    } else {
      driverMemory
    }
    val driverCores = LINKIS_SPARK_DRIVER_CORES.getValue(properties)
    new DriverAndYarnResource(
      new LoadInstanceResource(ByteTimeUtils.byteStringAsBytes(driverMemoryWithUnit), driverCores, 1),
      new YarnResource(ByteTimeUtils.byteStringAsBytes(executorMemoryWithUnit) * executorNum,
        LINKIS_SPARK_EXECUTOR_CORES.getValue(properties) * executorNum, 0, LINKIS_QUEUE_NAME.getValue(properties))
    )
  }
}
