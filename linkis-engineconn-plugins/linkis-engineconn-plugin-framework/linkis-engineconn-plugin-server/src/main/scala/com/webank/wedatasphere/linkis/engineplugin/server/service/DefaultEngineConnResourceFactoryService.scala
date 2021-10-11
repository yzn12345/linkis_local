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

package com.webank.wedatasphere.linkis.engineplugin.server.service

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineplugin.server.loader.EngineConnPluginsLoader
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.{EngineResourceFactory, EngineResourceRequest}
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineTypeLabel
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._


@Component
class DefaultEngineConnResourceFactoryService extends EngineConnResourceFactoryService with Logging {

  override def getResourceFactoryBy(engineType: EngineTypeLabel): EngineResourceFactory = {
    val engineConnPluginInstance = EngineConnPluginsLoader.getEngineConnPluginsLoader().getEngineConnPlugin(engineType)
    engineConnPluginInstance.plugin.getEngineResourceFactory
  }

  @Receiver
  override def createEngineResource(engineResourceRequest: EngineResourceRequest): NodeResource = {
    info(s"To invoke createEngineResource $engineResourceRequest")
    val engineTypeOption = engineResourceRequest.labels.find(_.isInstanceOf[EngineTypeLabel])

    if (engineTypeOption.isDefined) {
      val engineTypeLabel = engineTypeOption.get.asInstanceOf[EngineTypeLabel]
      getResourceFactoryBy(engineTypeLabel).createEngineResource(engineResourceRequest)
    } else {
      throw new EngineConnPluginErrorException(10001, "EngineTypeLabel are requested")
    }
  }

}
