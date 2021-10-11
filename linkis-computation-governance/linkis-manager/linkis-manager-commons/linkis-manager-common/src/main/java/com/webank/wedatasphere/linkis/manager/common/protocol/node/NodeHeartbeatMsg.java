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

package com.webank.wedatasphere.linkis.manager.common.protocol.node;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus;
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.NodeHealthyInfo;
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.NodeOverLoadInfo;
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource;
import com.webank.wedatasphere.linkis.protocol.AbstractRetryableProtocol;


public class NodeHeartbeatMsg extends AbstractRetryableProtocol implements HeartbeatProtocol {

    private NodeStatus status;

    private NodeHealthyInfo healthyInfo;

    private String heartBeatMsg;

    private NodeOverLoadInfo overLoadInfo;

    private ServiceInstance serviceInstance;

    private NodeResource nodeResource;

    public NodeStatus getStatus() {
        return status;
    }

    public NodeHeartbeatMsg setStatus(NodeStatus status) {
        this.status = status;
        return this;
    }

    public NodeHealthyInfo getHealthyInfo() {
        return healthyInfo;
    }

    public NodeHeartbeatMsg setHealthyInfo(NodeHealthyInfo healthyInfo) {
        this.healthyInfo = healthyInfo;
        return this;
    }

    public String getHeartBeatMsg() {
        return heartBeatMsg;
    }

    public NodeHeartbeatMsg setHeartBeatMsg(String heartBeatMsg) {
        this.heartBeatMsg = heartBeatMsg;
        return this;
    }

    public NodeOverLoadInfo getOverLoadInfo() {
        return overLoadInfo;
    }

    public NodeHeartbeatMsg setOverLoadInfo(NodeOverLoadInfo overLoadInfo) {
        this.overLoadInfo = overLoadInfo;
        return this;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public NodeHeartbeatMsg setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
        return this;
    }

    public NodeResource getNodeResource() {
        return nodeResource;
    }

    public NodeHeartbeatMsg setNodeResource(NodeResource nodeResource) {
        this.nodeResource = nodeResource;
        return this;
    }

    @Override
    public String toString() {
        return "NodeHeartbeatMsg{" +
                "status=" + status +
                ", serviceInstance=" + serviceInstance +
                '}';
    }
}
