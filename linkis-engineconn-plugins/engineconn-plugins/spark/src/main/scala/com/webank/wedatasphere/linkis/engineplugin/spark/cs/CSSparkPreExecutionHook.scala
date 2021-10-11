/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineplugin.spark.cs

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.cs.client.utils.ContextServiceUtils
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.engineplugin.spark.extension.SparkPreExecutionHook
import javax.annotation.PostConstruct
import org.springframework.stereotype.Component


@Component
class CSSparkPreExecutionHook extends SparkPreExecutionHook with Logging{

  @PostConstruct
  def  init(): Unit ={
    SparkPreExecutionHook.register(this)
  }

  private  val  csTableParser = new CSTableParser

  override def hookName: String = "CSSparkPreExecutionHook"

  override def callPreExecutionHook(engineExecutionContext: EngineExecutionContext, code: String): String = {

    var parsedCode = code
    val contextIDValueStr = ContextServiceUtils.getContextIDStrByMap(engineExecutionContext.getProperties)
    val nodeNameStr = ContextServiceUtils.getNodeNameStrByMap(engineExecutionContext.getProperties)
    info(s"Start to call CSSparkPreExecutionHook,contextID is $contextIDValueStr, nodeNameStr is $nodeNameStr")
    parsedCode = try {
      csTableParser.parse(engineExecutionContext, parsedCode, contextIDValueStr, nodeNameStr)
    } catch {
      case t: Throwable =>
        info("Failed to parser cs table", t)
        parsedCode
    }
    info(s"Finished to call CSSparkPreExecutionHook,contextID is $contextIDValueStr, nodeNameStr is $nodeNameStr")
    parsedCode
  }
}
