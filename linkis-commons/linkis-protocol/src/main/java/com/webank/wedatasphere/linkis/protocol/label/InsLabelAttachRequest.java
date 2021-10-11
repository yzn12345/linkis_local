/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.protocol.label;

import com.webank.wedatasphere.linkis.common.ServiceInstance;

import java.util.HashMap;
import java.util.Map;


public class InsLabelAttachRequest implements LabelRequest {
    /**
     * Service instance
     */
    private ServiceInstance serviceInstance;

    /**
     * Labels stored as map structure
     */
    private Map<String, Object> labels = new HashMap<>();


    public InsLabelAttachRequest(){

    }

    public InsLabelAttachRequest(ServiceInstance serviceInstance, Map<String, Object> labels){
        this.serviceInstance = serviceInstance;
        this.labels = labels;
    }
    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public Map<String, Object> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Object> labels) {
        this.labels = labels;
    }
}
