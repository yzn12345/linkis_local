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

package com.webank.wedatasphere.linkis.gateway.ujes.route.contextservice

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextIDParser
import com.webank.wedatasphere.linkis.cs.common.utils.CSHighAvailableUtils
import com.webank.wedatasphere.linkis.instance.label.service.InsLabelServiceAdapter
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.protocol.label.LabelInsQueryRequest
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

import java.util
import javax.annotation.Resource


/**
 * Description: If id is correct format of ContextHAId, will parse it to get the instance and backup instances.
 */
@Component
class ContextIdParserImpl extends ContextIDParser with Logging {

  @Resource
  private var insLabelService: InsLabelServiceAdapter = _

  override def parse(contextId: String): util.List[String] = {

    if (CSHighAvailableUtils.checkHAIDBasicFormat(contextId)) {
      val instances = new util.ArrayList[String](2)
      val haContextID = CSHighAvailableUtils.decodeHAID(contextId)
      val mainInstance = getInstanceByAlias(haContextID.getInstance())
      if (null != mainInstance) {
        instances.add(mainInstance.getInstance)
      } else {
        error(s"parse HAID instance invalid. haIDKey : " + contextId)
      }
      val backupInstance = getInstanceByAlias(haContextID.getInstance())
      if (null != backupInstance) {
        instances.add(backupInstance.getInstance)
      } else {
        error("parse HAID backupInstance invalid. haIDKey : " + contextId)
      }
      instances
    } else {
      new util.ArrayList[String](0)
    }
  }

  private def isNumberic(s:String):Boolean = {
    s.toCharArray foreach {
      c => if (c < 48 || c >57) return false
    }
    true
  }

  // todo same as that in RouteLabelInstanceAliasConverter
  private def getInstanceByAlias(alias: String): ServiceInstance = {
    if (StringUtils.isNotBlank(alias)) {
      Utils.tryAndError {
        val request = new LabelInsQueryRequest()
        val labelMap = new util.HashMap[String, Any]()
        labelMap.put(LabelKeyConstant.ROUTE_KEY, alias)
        request.setLabels(labelMap.asInstanceOf[util.HashMap[String, Object]])
        var serviceInstance: ServiceInstance = null
        val labels = new util.ArrayList[Label[_]]()
        labels.add(LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel[Label[_]](LabelKeyConstant.ROUTE_KEY, alias))
        val insList = insLabelService.searchInstancesByLabels(labels)
        if (null != insList && !insList.isEmpty) {
          serviceInstance = insList.get(0)
        }
        serviceInstance
      }
    } else {
      null
    }
  }

}
