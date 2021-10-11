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

package com.webank.wedatasphere.linkis.datasourcemanager.core.vo;


import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataSourceVo extends PageViewVo{

    private String dataSourceName;

    private Long dataSourceTypeId;

    private List<String> createIdentifyList = new ArrayList<>();

    private String createSystem;

    public DataSourceVo(){

    }

    public DataSourceVo(String dataSourceName, Long dataSourceTypeId,
                        String createIdentifies, String createSystem){
        this.dataSourceName = dataSourceName;
        this.dataSourceTypeId = dataSourceTypeId;
        if(StringUtils.isNotBlank(createIdentifies)){
            createIdentifyList.addAll(Arrays.asList(createIdentifies.split(",")));
        }
        this.createSystem = createSystem;
    }
    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public Long getDataSourceTypeId() {
        return dataSourceTypeId;
    }

    public void setDataSourceTypeId(Long dataSourceTypeId) {
        this.dataSourceTypeId = dataSourceTypeId;
    }


    public String getCreateSystem() {
        return createSystem;
    }

    public void setCreateSystem(String createSystem) {
        this.createSystem = createSystem;
    }

    public List<String> getCreateIdentifyList() {
        return createIdentifyList;
    }

    public void setCreateIdentifyList(List<String> createIdentifyList) {
        this.createIdentifyList = createIdentifyList;
    }
}
