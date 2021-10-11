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

package com.webank.wedatasphere.linkis.manager.am.service.engine


import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.governance.common.entity.NodeExistStatus
import com.webank.wedatasphere.linkis.governance.common.protocol.engineconn.{RequestEngineStatusBatch, ResponseEngineStatusBatch}
import com.webank.wedatasphere.linkis.governance.common.utils.GovernanceConstant
import com.webank.wedatasphere.linkis.manager.am.manager.{EMNodeManager, EngineNodeManager}
import com.webank.wedatasphere.linkis.manager.am.vo.AMEngineNodeVo
import com.webank.wedatasphere.linkis.manager.common.entity.node.{EMNode, EngineNode}
import com.webank.wedatasphere.linkis.manager.label.service.NodeLabelService
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.resourcemanager.service.ResourceManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._
import com.webank.wedatasphere.linkis.manager.label.builder.factory.{LabelBuilderFactoryContext, StdLabelBuilderFactory}
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineTypeLabel

import scala.collection.JavaConverters._


@Service
class DefaultEngineInfoService extends AbstractEngineService with EngineInfoService with Logging {

  @Autowired
  private var engineNodeManager: EngineNodeManager = _

  @Autowired
  private var emNodeManager: EMNodeManager = _

  @Autowired
  private var resourceManager: ResourceManager = _

  @Autowired
  private var labelService: NodeLabelService = _

  private val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  /**
   * 通过user获取EngineNode 的基本信息，含metric,resourceInfo
   *
   * @param user
   * @return
   */
  override def listUserEngines(user: String): java.util.List[EngineNode] = {
    //1.获取node 和metric信息
    val nodes = engineNodeManager.listEngines(user)
    val resourceInfo = resourceManager.getResourceInfo(nodes.map(_.getServiceInstance).toArray).resourceInfo
    val resourceInfoMap = resourceInfo.map(r => (r.getServiceInstance.toString, r)).toMap
    nodes.map { node =>
      resourceInfoMap.get(node.getServiceInstance.toString).map(_.getNodeResource).foreach(node.setNodeResource)
      node.setLabels(labelService.getNodeLabels(node.getServiceInstance))
      node
    }
    nodes
  }

  /**
   * 通过em（主要是instance信息） 获取engine的基本信息，含metric
   *
   * @param em
   * @return
   */
  override def listEMEngines(em: EMNode): java.util.List[EngineNode] = {
    val nodes = emNodeManager.listEngines(em)
    val resourceInfo = resourceManager.getResourceInfo(nodes.map(_.getServiceInstance).toArray).resourceInfo
    val resourceInfoMap = resourceInfo.map(r => (r.getServiceInstance.toString, r)).toMap
    nodes.map { node =>
      resourceInfoMap.get(node.getServiceInstance.toString).map(_.getNodeResource).foreach(node.setNodeResource)
      node.setLabels(labelService.getNodeLabels(node.getServiceInstance))
      node
    }
    nodes
  }

  @Receiver
  override def dealBatchGetEngineStatus(request: RequestEngineStatusBatch): ResponseEngineStatusBatch = {
    if (request.engineList.size() > GovernanceConstant.REQUEST_ENGINE_STATUS_BATCH_LIMIT) {
      return ResponseEngineStatusBatch(null, s"Engines size ${request.engineList.size()} in request cannot excceed the batch limit of ${GovernanceConstant.REQUEST_ENGINE_STATUS_BATCH_LIMIT}")
    }
    val map = new util.HashMap[ServiceInstance, NodeExistStatus]
    request.engineList.asScala.foreach(e => {
      var engineNode: EngineNode = null
      Utils.tryCatch {
        engineNode = engineNodeManager.getEngineNode(e)
        if (null == engineNode) {
          map.put(e, NodeExistStatus.UnExist)
        } else {
          map.put(e, NodeExistStatus.Exist)
        }
      } {
        case t : Throwable =>
          error(s"Get engineNode of ${e.toString} error. ", t)
          map.put(e, NodeExistStatus.Unknown)
      }
    })
    ResponseEngineStatusBatch(map, null)
  }

  override def modifyEngineLabel(instance: ServiceInstance, labelKeyValue: java.util.Map[String,String]): Unit = {
    labelKeyValue.asScala.foreach(keyValue => {
      val label = labelBuilderFactory.createLabel(keyValue._1, keyValue._2)
      labelService.updateLabelToNode(instance, label)
      info(s"instance:${instance} success to update label, labelKey:${keyValue._1} labelValue:${keyValue._2}")
    })
  }

}
