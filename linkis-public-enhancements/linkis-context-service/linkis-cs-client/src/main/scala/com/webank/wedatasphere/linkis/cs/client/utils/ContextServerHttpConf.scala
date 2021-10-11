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

package com.webank.wedatasphere.linkis.cs.client.utils

import com.webank.wedatasphere.linkis.common.conf.Configuration

object ContextServerHttpConf {
  val gatewayInstance: String = Configuration.getGateWayURL()
  val urlPrefix: String = if (ContextClientConf.URL_PREFIX.getValue.endsWith("/")) {
    ContextClientConf.URL_PREFIX.getValue.substring(0, ContextClientConf.URL_PREFIX.getValue.length - 1)
  } else ContextClientConf.URL_PREFIX.getValue

  val createContextURL:String = urlPrefix + "/createContextID"

  val updateContextURL:String = urlPrefix + "/setValueByKey"

  val setKeyValueURL:String = urlPrefix + "/setValue"

  val resetKeyValueURL:String = urlPrefix + "/resetValue"

  val removeValueURL:String = urlPrefix + "/removeValue"

  val resetContextIdURL:String = urlPrefix + "/removeAllValue"

  val onBindKeyURL:String = urlPrefix + "/onBindKeyListener"

  val onBindIDURL:String = urlPrefix + "/onBindIDListener"

  val getContextIDURL:String = urlPrefix + "/getContextID"

  val heartBeatURL:String = urlPrefix + "/heartbeat"

  val searchURL:String = urlPrefix + "/searchContextValue"

  val getContextValueURL:String = urlPrefix + "/getContextValue"

  val createContextHistory:String = urlPrefix + "/createHistory"

  val removeContextHistory:String = urlPrefix + "/removeHistory"

  val getContextHistories:String = urlPrefix + "/getHistories"

  val getContextHistory:String = urlPrefix + "/getHistory"

  val searchContextHistory:String = urlPrefix + "/searchHistory"

  val removeAllValueByKeyPrefixAndContextTypeURL: String = urlPrefix + "/removeAllValueByKeyPrefixAndContextType"

  val removeAllValueByKeyPrefixURL: String = urlPrefix + "/removeAllValueByKeyPrefix"
}
