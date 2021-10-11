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

package com.webank.wedatasphere.linkis.engineconn.core

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.conf.EngineConnConf
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.manager.engineplugin.common.EngineConnPlugin

object EngineConnObject extends Logging {

  private val engineConnPlugin: EngineConnPlugin = Utils.tryCatch {
    Utils.getClassInstance[EngineConnPlugin](EngineConnConf.ENGINE_CONN_PLUGIN_CLAZZ.getValue)
  } { t =>
    error("Failed to create engineConnPlugin: " + EngineConnConf.ENGINE_CONN_PLUGIN_CLAZZ.getValue, t)
    System.exit(1)
    null
  }
  private var engineCreationContext: EngineCreationContext = _
  private var  ready = false

  def isReady: Boolean = this.ready

  def setReady(): Unit = this.ready = true

  def getEngineConnPlugin: EngineConnPlugin = engineConnPlugin

  def getEngineCreationContext: EngineCreationContext = this.engineCreationContext

  def setEngineCreationContext(engineCreationContext: EngineCreationContext): Unit = this.engineCreationContext = engineCreationContext


}
