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

package com.webank.wedatasphere.linkis.manager.engineplugin.common.conf

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}


object EngineConnPluginConf {

  val JAVA_ENGINE_REQUEST_MEMORY = CommonVars[ByteType]("wds.linkis.engineconn.java.driver.memory", new ByteType("1g"))

  val JAVA_ENGINE_REQUEST_CORES = CommonVars[Int]("wds.linkis.engineconn.java.driver.cores", 2)

  val JAVA_ENGINE_REQUEST_INSTANCE = CommonVars[Int]("wds.linkis.engineconn.java.driver.instannce", 1)

  val ENGINECONN_TYPE_NAME = CommonVars[String]("wds.linkis.engineconn.type.name", "python")

  val ENGINECONN_MAIN_CLASS = CommonVars[String]("wds.linkis.engineconn.main.class", "com.webank.wedatasphere.linkis.engineconn.launch.EngineConnServer")

}
