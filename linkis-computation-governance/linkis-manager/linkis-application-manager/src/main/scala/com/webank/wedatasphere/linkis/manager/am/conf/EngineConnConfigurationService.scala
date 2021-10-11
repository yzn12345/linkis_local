/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.manager.am.conf

import java.util

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.server.JMap
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.{Bean, Configuration}

import scala.collection.JavaConversions._


trait EngineConnConfigurationService {

  def getConsoleConfiguration(label: util.List[Label[_]]): util.Map[String, String]

}

class DefaultEngineConnConfigurationService extends EngineConnConfigurationService with Logging {

  override def getConsoleConfiguration(label: util.List[Label[_]]): util.Map[String, String] = {
    val properties = new JMap[String, String]
    val userCreatorLabelOption = label.find(_.isInstanceOf[UserCreatorLabel])
    val engineTypeLabelOption = label.find(_.isInstanceOf[EngineTypeLabel])
    if (userCreatorLabelOption.isDefined) {
      val userCreatorLabel = userCreatorLabelOption.get.asInstanceOf[UserCreatorLabel]
      val globalConfig = Utils.tryAndWarn(ConfigurationMapCache.globalMapCache.getCacheMap(userCreatorLabel))
      if (null != globalConfig) {
        properties.putAll(globalConfig)
      }
      if (engineTypeLabelOption.isDefined) {
        val engineTypeLabel = engineTypeLabelOption.get.asInstanceOf[EngineTypeLabel]
        val engineConfig = Utils.tryAndWarn(ConfigurationMapCache.engineMapCache.getCacheMap((userCreatorLabel, engineTypeLabel)))
        if (null != engineConfig) {
          properties.putAll(engineConfig)
        }
      }
    }
    properties
  }

}


@Configuration
class ApplicationManagerSpringConfiguration{

  @ConditionalOnMissingBean
  @Bean
  def getDefaultEngineConnConfigurationService:EngineConnConfigurationService ={
    new DefaultEngineConnConfigurationService
  }

}