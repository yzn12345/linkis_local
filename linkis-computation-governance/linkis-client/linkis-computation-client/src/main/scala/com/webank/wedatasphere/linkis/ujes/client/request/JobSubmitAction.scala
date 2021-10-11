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

package com.webank.wedatasphere.linkis.ujes.client.request

import java.util

import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.request.POSTAction
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.ujes.client.exception.UJESClientBuilderException

class JobSubmitAction private() extends POSTAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("entrance", "submit")

  override def getRequestPayload: String = DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)
}

object JobSubmitAction {
  def builder(): Builder = new Builder

  class Builder private[JobSubmitAction]() {
    private var user: String = _

    private var executeUser: String = _

    private var executionContent: util.Map[String, Any] = _

    private var formatCode: Boolean = false

    private var labels: util.Map[String, Any] = _

    private var params: util.Map[String, Any] = _

    private var source: util.Map[String, Any] = _

    def addExecuteCode(executeCode: String): Builder = {
      if ( null == executionContent ) executionContent = new util.HashMap[String, Any]()
      executionContent.put("code", executeCode)
      this
    }

    def setRunTypeStr(runTypeStr: String): Builder = {
      if ( null == executionContent ) executionContent = new util.HashMap[String, Any]()
      executionContent.put("runType", runTypeStr)
      this
    }

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def addExecuteUser(executeUser: String): Builder = {
      this.executeUser = executeUser
      this
    }

    def enableFormatCode(): Builder = {
      this.formatCode = true
      this
    }

    def setExecutionContent(executionContent: util.Map[String, Any]): Builder = {
      this.executionContent = executionContent
      this
    }

    def setLabels(labels: util.Map[String, Any]): Builder = {
      this.labels = labels
      this
    }

    def setParams(params: util.Map[String, Any]): Builder = {
      this.synchronized(this.params = params)
      this
    }

    def setSource(source: util.Map[String, Any]): Builder = {
      this.synchronized(this.source = source)
      this
    }

    def setStartupParams(startupMap: util.Map[String, Any]): Builder = {
      if (this.params == null) this synchronized {
        if (this.params == null) this.params = new util.HashMap[String, Any]
      }
      TaskUtils.addStartupMap(this.params, startupMap)
      this
    }

    def setRuntimeParams(runtimeMap: util.Map[String, Any]): Builder = {
      if (this.params == null) this synchronized {
        if (this.params == null) this.params = new util.HashMap[String, Any]
      }
      TaskUtils.addRuntimeMap(this.params, runtimeMap)
      this
    }

    def setSpecialParams(specialMap: util.Map[String, Any]): Builder = {
      if (this.params == null) this synchronized {
        if (this.params == null) this.params = new util.HashMap[String, Any]
      }
      TaskUtils.addSpecialMap(this.params, specialMap)
      this
    }

    def setVariableMap(variableMap: util.Map[String, Any]): Builder = {
      if (this.params == null) this synchronized {
        if (this.params == null) this.params = new util.HashMap[String, Any]
      }
      TaskUtils.addVariableMap(this.params, variableMap)
      this
    }


    def build(): JobSubmitAction = {
      val submitAction = new JobSubmitAction
      submitAction.setUser(user)
      submitAction.addRequestPayload(TaskConstant.EXECUTE_USER, executeUser)
      if (formatCode) submitAction.addRequestPayload(TaskConstant.FORMATCODE, true)
      if (executionContent == null && params == null)
        throw new UJESClientBuilderException("executionContent is needed!")
      submitAction.addRequestPayload(TaskConstant.EXECUTION_CONTENT, executionContent)
      if (params == null) params = new util.HashMap[String, Any]()
      submitAction.addRequestPayload(TaskConstant.PARAMS, params)
      if (this.source == null) this.source = new util.HashMap[String, Any]()
      submitAction.addRequestPayload(TaskConstant.SOURCE, this.source)

      if (this.labels == null) this.labels = new util.HashMap[String, Any]()
      submitAction.addRequestPayload(TaskConstant.LABELS, this.labels)
      submitAction
    }
  }

}

