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

package com.webank.wedatasphere.linkis.metadatamanager.service;

import com.webank.wedatasphere.linkis.metadatamanager.common.Json;
import com.webank.wedatasphere.linkis.metadatamanager.common.domain.MetaColumnInfo;
import com.webank.wedatasphere.linkis.metadatamanager.common.service.AbstractMetaService;
import com.webank.wedatasphere.linkis.metadatamanager.common.service.MetadataConnection;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ElasticMetaService extends AbstractMetaService<ElasticConnection> {
    @Override
    public MetadataConnection<ElasticConnection> getConnection(String operator, Map<String, Object> params)
    throws Exception{
        String[] endPoints = new String[]{};
        Object urls = params.get(ElasticParamsMapper.PARAM_ES_URLS.getValue());
        if(!(urls instanceof List)){
            List<String> urlList = Json.fromJson(String.valueOf(urls), List.class, String.class);
            assert urlList != null;
            endPoints = urlList.toArray(endPoints);
        }else{
            endPoints = ((List<String>)urls).toArray(endPoints);
        }
        ElasticConnection conn = new ElasticConnection(endPoints, String.valueOf(params.getOrDefault(ElasticParamsMapper.PARAM_ES_USERNAME.getValue(), "")),
                String.valueOf(params.getOrDefault(ElasticParamsMapper.PARAM_ES_PASSWORD.getValue(), "")));
        return new MetadataConnection<>(conn, false);
    }

    @Override
    public List<String> queryDatabases(ElasticConnection connection) {
        //Get indices
        try{
            return connection.getAllIndices();
        }catch (Exception e){
            throw new RuntimeException("Fail to get ElasticSearch indices(获取索引列表失败)", e);
        }
    }

    @Override
    public List<String> queryTables(ElasticConnection connection, String database) {
        //Get types
        try{
            return connection.getTypes(database);
        }catch (Exception e){
            throw new RuntimeException("Fail to get ElasticSearch types(获取索引类型失败)", e);
        }
    }

    @Override
    public List<MetaColumnInfo> queryColumns(ElasticConnection connection, String database, String table) {
        try {
            Map<Object, Object> props = connection.getProps(database, table);
            return props.entrySet().stream().map(entry -> {
                MetaColumnInfo info = new MetaColumnInfo();
                info.setName(String.valueOf(entry.getKey()));
                Object value = entry.getValue();
                if(value instanceof Map){
                    info.setType(String.valueOf(((Map)value)
                            .getOrDefault(ElasticConnection.DEFAULT_TYPE_NAME, "")));
                }
                return info;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Fail to get ElasticSearch columns(获取索引字段失败)", e);
        }
    }
}
