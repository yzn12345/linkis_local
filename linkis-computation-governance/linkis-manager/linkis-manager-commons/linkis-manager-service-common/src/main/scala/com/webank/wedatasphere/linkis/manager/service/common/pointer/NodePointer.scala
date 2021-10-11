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

package com.webank.wedatasphere.linkis.manager.service.common.pointer

import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.node.Node
import com.webank.wedatasphere.linkis.manager.common.protocol.node.NodeHeartbeatMsg
import com.webank.wedatasphere.linkis.manager.label.entity.Label


trait NodePointer {


  /**
    * 与该远程指针关联的node信息
    *
    * @return
    */
  def getNode(): Node

  /**
    * 向对应的Node发送请求获取节点状态
    *
    * @return
    */
  def getNodeStatus(): NodeStatus

  /**
    * 向对应的Node发送请求获取节点心跳信息
    *
    * @return
    */
  def getNodeHeartbeatMsg(): NodeHeartbeatMsg

  /**
    * 向对应的Node发送Kill 请求
    *
    * @return
    */
  def stopNode(): Unit

  /**
    * 向对应的Node发送Label更新 请求
    *
    * @return
    */
  def updateLabels(labels: Array[Label[_]]): Unit

  override def equals(obj: Any): Boolean = obj match {
    case nodeB: Node => getNode().getServiceInstance.equals(nodeB.getServiceInstance)
    case _ => false
  }

}
