/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.datasourcemanager.common.domain;

import com.webank.wedatasphere.linkis.datasourcemanager.common.util.json.Json;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

/**
 * Store the data source information
 */
@JsonSerialize(include= JsonSerialize.Inclusion.NON_EMPTY)
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
public class DataSource {

    private Long id;

    /**
     * Data source name
     */
    @NotNull
    private String dataSourceName;

    /**
     * Data source description
     */
    @Size(min = 0, max = 200)
    private String dataSourceDesc;

    /**
     * ID of data source type
     */
    @NotNull
    private Long dataSourceTypeId;

    /**
     * Identify from creator
     */
    private String createIdentify;

    /**
     * System name from creator
     */
    @NotNull
    private String createSystem;
    /**
     * Connection parameters
     */
    private Map<String, Object> connectParams = new HashMap<>();
    /**
     * Parameter JSON string
     */
    @JsonIgnore
    private String parameter;

    /**
     * ID of data source environment
     */
    private Long dataSourceEnvId;

    /**
     * Create time
     */
    private Date createTime;

    /**
     * Modify time
     */
    private Date modifyTime;

    /**
     * Modify user
     */
    private String modifyUser;

    private String createUser;

    /**
     * Data source type entity
     */
    private DataSourceType dataSourceType;

    /**
     * Data source env
     */
    private DataSourceEnv dataSourceEnv;

    @JsonIgnore
    private List<DataSourceParamKeyDefinition> keyDefinitions = new ArrayList<>();

    public DataSource(){
        this.createTime = Calendar.getInstance().getTime();
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getDataSourceDesc() {
        return dataSourceDesc;
    }

    public void setDataSourceDesc(String dataSourceDesc) {
        this.dataSourceDesc = dataSourceDesc;
    }

    public Long getDataSourceTypeId() {
        return dataSourceTypeId;
    }

    public void setDataSourceTypeId(Long dataSourceTypeId) {
        this.dataSourceTypeId = dataSourceTypeId;
    }

    public String getCreateIdentify() {
        return createIdentify;
    }

    public void setCreateIdentify(String createIdentify) {
        this.createIdentify = createIdentify;
    }

    public String getCreateSystem() {
        return createSystem;
    }

    public void setCreateSystem(String createSystem) {
        this.createSystem = createSystem;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public Long getDataSourceEnvId() {
        return dataSourceEnvId;
    }

    public void setDataSourceEnvId(Long dataSourceEnvId) {
        this.dataSourceEnvId = dataSourceEnvId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Map<String, Object> getConnectParams() {
        if(connectParams.isEmpty() && StringUtils.isNotBlank(parameter)){
            connectParams.putAll(Objects.requireNonNull(Json.fromJson(parameter, Map.class)));
        }
        return connectParams;
    }

    public void setConnectParams(Map<String, Object> connectParams) {
        this.connectParams = connectParams;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getModifyUser() {
        return modifyUser;
    }

    public void setModifyUser(String modifyUser) {
        this.modifyUser = modifyUser;
    }

    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(DataSourceType dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public DataSourceEnv getDataSourceEnv() {
        return dataSourceEnv;
    }

    public void setDataSourceEnv(DataSourceEnv dataSourceEnv) {
        this.dataSourceEnv = dataSourceEnv;
    }

    public List<DataSourceParamKeyDefinition> getKeyDefinitions() {
        return keyDefinitions;
    }

    public void setKeyDefinitions(List<DataSourceParamKeyDefinition> keyDefinitions) {
        this.keyDefinitions = keyDefinitions;
    }
}
