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

package com.webank.wedatasphere.linkis.manager.common.entity.metrics;


public class NodeOverLoadInfo {

    private Long maxMemory;

    private Long usedMemory;

    private Float systemCPUUsed;

    private Long systemLeftMemory;

    public Long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(Long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public Long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public Float getSystemCPUUsed() {
        return systemCPUUsed;
    }

    public void setSystemCPUUsed(Float systemCPUUsed) {
        this.systemCPUUsed = systemCPUUsed;
    }

    public Long getSystemLeftMemory() {
        return systemLeftMemory;
    }

    public void setSystemLeftMemory(Long systemLeftMemory) {
        this.systemLeftMemory = systemLeftMemory;
    }
}
