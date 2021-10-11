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

package com.webank.wedatasphere.linkis.manager.am.label

import java.util

import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf._
import com.webank.wedatasphere.linkis.manager.label.entity.node.AliasServiceInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.entity.{EMNodeLabel, EngineNodeLabel, Label}
import com.webank.wedatasphere.linkis.manager.service.common.label.LabelFilter
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._


@Component
class AMLabelFilter extends LabelFilter {

  override def choseEngineLabel(labelList: util.List[Label[_]]): util.List[Label[_]] = {
    labelList.filter {
      case _: EngineNodeLabel => true
      // TODO: magic
      case label: AliasServiceInstanceLabel if label.getAlias.equals(ENGINE_CONN_SPRING_NAME.getValue) => true
      case _ => false
    }
  }

  override def choseEMLabel(labelList: util.List[Label[_]]): util.List[Label[_]] = {
    labelList.filter {
      case _: EMNodeLabel => true
      // TODO: magic
      case label: AliasServiceInstanceLabel if label.getAlias.equals(ENGINE_CONN_MANAGER_SPRING_NAME.getValue) => true
      case _ => false
    }
  }
}
