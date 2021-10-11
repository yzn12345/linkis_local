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

package com.webank.wedatasphere.linkis.resourcemanager.restful.vo;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.linkis.manager.common.entity.resource.Resource;
import com.webank.wedatasphere.linkis.manager.common.entity.resource.ResourceType;

import java.util.List;

public class ApplicationListVo {

    Resource maxResource = Resource.initResource(ResourceType.LoadInstance);
    Resource usedResource = Resource.initResource(ResourceType.LoadInstance);
    Resource lockedResource = Resource.initResource(ResourceType.LoadInstance);
    List<EngineInstanceVo> engineInstances = Lists.newArrayList();

    public Resource getMaxResource() {
        return maxResource;
    }

    public void setMaxResource(Resource maxResource) {
        this.maxResource = maxResource;
    }

    public Resource getUsedResource() {
        return usedResource;
    }

    public void setUsedResource(Resource usedResource) {
        this.usedResource = usedResource;
    }

    public Resource getLockedResource() {
        return lockedResource;
    }

    public void setLockedResource(Resource lockedResource) {
        this.lockedResource = lockedResource;
    }

    public List<EngineInstanceVo> getEngineInstances() {
        return engineInstances;
    }

    public void setEngineInstances(List<EngineInstanceVo> engineInstances) {
        this.engineInstances = engineInstances;
    }
}
