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

package com.webank.wedatasphere.linkis.manager.common.protocol.engine;

import com.webank.wedatasphere.linkis.protocol.message.RequestMethod;

import java.util.Map;

public class EngineCreateRequest implements EngineRequest, RequestMethod {

    private Map<String, String> properties;

    private Map<String, Object> labels;

    private long timeOut;

    private String user;

    private String createService;

    private String description;

    private boolean ignoreTimeout = false;

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Object> labels) {
        this.labels = labels;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    @Override
    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCreateService() {
        return createService;
    }

    public void setCreateService(String createService) {
        this.createService = createService;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIgnoreTimeout() {
        return ignoreTimeout;
    }

    public void setIgnoreTimeout(boolean ignoreTimeout) {
        this.ignoreTimeout = ignoreTimeout;
    }

    @Override
    public String method() {
        return "/engine/create";
    }

    @Override
    public String toString() {
        return "EngineCreateRequest{" +
                "labels=" + labels +
                ", timeOut=" + timeOut +
                ", user='" + user + '\'' +
                ", createService='" + createService + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
