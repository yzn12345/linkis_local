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

package com.webank.wedatasphere.linkis.engineconn.common.hook

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.conf.EngineConnConf
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import org.apache.commons.lang.StringUtils


trait EngineConnHook {

  def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit

  def beforeExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit

  def afterExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit

  def afterEngineServerStartFailed(engineCreationContext: EngineCreationContext, throwable: Throwable): Unit = {

  }

  def afterEngineServerStartSuccess(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {

  }

}

object EngineConnHook extends Logging {

  private val engineConnHooks = initEngineConnHooks

  private def initEngineConnHooks: Array[EngineConnHook] = {

    val hooks = EngineConnConf.ENGINE_CONN_HOOKS.getValue
    if (StringUtils.isNotBlank(hooks)) {
      val clazzArr = hooks.split(",")
      if (null != clazzArr && clazzArr.nonEmpty) {
        clazzArr.map { clazz =>
          Utils.tryAndWarn(Utils.getClassInstance[EngineConnHook](clazz))
        }.filter(_ != null)
      } else {
        Array.empty
      }
    } else {
      Array.empty
    }

  }

  def getEngineConnHooks: Array[EngineConnHook] = engineConnHooks
}