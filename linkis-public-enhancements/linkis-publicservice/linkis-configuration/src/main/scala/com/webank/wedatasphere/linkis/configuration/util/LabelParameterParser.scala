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

package com.webank.wedatasphere.linkis.configuration.util

import java.util

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.configuration.exception.ConfigurationException
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineType, UserCreatorLabel}

import scala.collection.JavaConverters._

object LabelParameterParser {
  private val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  def getDefaultEngineVersion(engineType: String): String = {
    var returnType: CommonVars[String] = null
    EngineType.values.foreach(DefinedEngineType => {
      if (DefinedEngineType.toString.equals(engineType.toLowerCase)) {
        returnType = engineType.toLowerCase match {
          case "spark" => GovernanceCommonConf.SPARK_ENGINE_VERSION
          case "hive" => GovernanceCommonConf.HIVE_ENGINE_VERSION
          case "python" => GovernanceCommonConf.PYTHON_ENGINE_VERSION
          case _ => throw new ConfigurationException(s"Configuration does not support engine type:${engineType}(配置暂不支持${engineType}引擎类型)")
        }
      }
    })
    if (returnType == null) {
      throw new ConfigurationException(s"The corresponding engine type is not matched:${engineType}(没有匹配到对应的引擎类型:${engineType})")
    }

    returnType.getValue
  }


  def changeUserToDefault(labelList: java.util.List[Label[_]]): java.util.List[Label[_]] = {
    val newList = new util.LinkedList[Label[_]]()
    if (labelList != null) {
      labelList.asScala.foreach(label => {
        if (label.isInstanceOf[UserCreatorLabel]) {
          val newLabel = labelBuilderFactory.createLabel(classOf[UserCreatorLabel])
          newLabel.setUser("*")
          newLabel.setCreator("*")
          newList.addLast(newLabel)
        } else {
          newList.addLast(label)
        }
      })
    }
    newList
  }
}
