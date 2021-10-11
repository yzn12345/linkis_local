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

package com.webank.wedatasphere.linkis.cs.common.serialize.impl.history.metadata;

import com.google.gson.reflect.TypeToken;
import com.webank.wedatasphere.linkis.cs.common.entity.history.metadata.CSTableLineageHistory;
import com.webank.wedatasphere.linkis.cs.common.entity.metadata.CSTable;
import com.webank.wedatasphere.linkis.cs.common.entity.metadata.Table;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.serialize.AbstractSerializer;
import com.webank.wedatasphere.linkis.cs.common.serialize.impl.history.CommonHistorySerializer;
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils;

import java.util.List;
import java.util.Map;


public class CSTableLineageSerializer extends AbstractSerializer<CSTableLineageHistory> implements CommonHistorySerializer {


    @Override
    public CSTableLineageHistory fromJson(String json) throws CSErrorException {
        Map<String, String> map = getMapValue(json);
        CSTableLineageHistory history = get(map, new CSTableLineageHistory());
        history.setSourceTables(CSCommonUtils.gson.fromJson(map.get("sourceTables"),
                new TypeToken<List<CSTable>>() {
                }.getType()));
        history.setTable(CSCommonUtils.gson.fromJson(map.get("targetTable"), CSTable.class));
        return history;
    }

    @Override
    public String getJsonValue(CSTableLineageHistory tableLineageMetadataContextHistory) throws CSErrorException {
        Table targetTable = tableLineageMetadataContextHistory.getTable();
        List<Table> sourceTables = tableLineageMetadataContextHistory.getSourceTables();
        String targetTableStr = CSCommonUtils.gson.toJson(targetTable);
        String sourceTablesStr = CSCommonUtils.gson.toJson(sourceTables);
        Map<String, String> mapValue = getMapValue(tableLineageMetadataContextHistory);
        mapValue.put("targetTable", targetTableStr);
        mapValue.put("sourceTables", sourceTablesStr);
        return CSCommonUtils.gson.toJson(mapValue);
    }

    @Override
    public String getType() {
        return "CSTableLineageMetadataContextHistory";
    }

    @Override
    public boolean accepts(Object obj) {
        return null != obj && obj.getClass().getName().equals(CSTableLineageHistory.class.getName());
    }
}
