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

package com.webank.wedatasphere.linkis.resourcemanager.external.domain;

import com.google.gson.reflect.TypeToken;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;

import java.util.Map;

public class ExternalResourceProvider {

    Integer id;
    String resourceType;
    String name;
    String labels;
    String config;

    Map<String, Object> configMap;

    public String getName() {
        return name;
    }

    public String getLabels() {
        return labels;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public Map<String, Object> getConfigMap(){
        if(configMap == null){
            configMap = BDPJettyServerHelper.gson().fromJson(config, new TypeToken<Map<String, Object>>(){}.getType());
        }
        return configMap;
    }

    public String getResourceType(){
        return resourceType;
    }



}
