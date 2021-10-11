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

package com.webank.wedatasphere.linkis.ecm.core.launch

import java.util

import scala.collection.JavaConversions._


trait DiscoveryMsgGenerator {

  def generate(engineConnManagerEnv: EngineConnManagerEnv): java.util.Map[String, String]

}

class EurekaDiscoveryMsgGenerator extends DiscoveryMsgGenerator {
  override def generate(engineConnManagerEnv: EngineConnManagerEnv): util.Map[String, String] =
    engineConnManagerEnv.properties.get("eureka.client.serviceUrl.defaultZone")
      .map(v => Map("eureka.client.serviceUrl.defaultZone" -> v)).getOrElse[Map[String,String]](Map.empty)
}