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

package com.webank.wedatasphere.linkis.cli.core.interactor.command.parser.transformer;

import com.webank.wedatasphere.linkis.cli.common.exception.LinkisClientRuntimeException;
import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorLevel;
import com.webank.wedatasphere.linkis.cli.core.exception.CommandException;
import com.webank.wedatasphere.linkis.cli.core.exception.error.CommonErrMsg;
import com.webank.wedatasphere.linkis.cli.core.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: Substitute a String key(e.g. spark.executor.cores) into
 * another String key accepted by Linkis-Client(e.g. wds.linkis.client.conf.spark.executor.cores)
 * according to mapperRules.
 */
public abstract class ParamKeyMapper {

    protected Map<String, String> mapperRules;

    public ParamKeyMapper() {
        mapperRules = new HashMap<>();
        initMapperRules();
    }

    public ParamKeyMapper(Map<String, String> mapperRules) {
        mapperRules = new HashMap<>();
        initMapperRules(mapperRules);
    }

    /**
     * Executor should overwrite init() method to set key to key mapping
     */

    public abstract void initMapperRules();


    public void initMapperRules(Map<String, String> mapperRules) {
        this.mapperRules = mapperRules;
    }

    /**
     * update keyMapping one by one.
     *
     * @param key
     * @param targetKey
     */
    public void updateMapping(String key, String targetKey) {
        if (this.mapperRules.containsKey(key)) {
            throw new CommandException("CMD0020", ErrorLevel.ERROR, CommonErrMsg.ParserParseErr, "ParamMapper should not map different keys into same key. Key is: " + targetKey);
        } else {
            this.mapperRules.put(key, targetKey);
        }
    }

    /**
     * update keyMapping according to kv-String.
     */
    private void updateMappingbyConfig(String kvString) {
        if (StringUtils.isNotBlank(kvString)) {
            Map<String, String> result = CommonUtils.parseKVStringToMap(kvString, ",");
            this.mapperRules.putAll(result);
        }
    }

    /**
     * Given a param map, replace all keys of this map.
     *
     * @param paramMap
     * @param <T>
     * @return
     */
    public <T> Map<String, T> getMappedMapping(Map<String, T> paramMap) throws LinkisClientRuntimeException {
        Map<String, T> resultMap = new HashMap<>();
        String targetKey;
        for (Map.Entry<String, T> entry : paramMap.entrySet()) {
            targetKey = getMappedKey(entry.getKey());
            if (resultMap.containsKey(targetKey)) {
                throw new CommandException("CMD0020", ErrorLevel.ERROR, CommonErrMsg.ParserParseErr, "ParamMapper should not map different keys into same key. Key is: " + targetKey);
            } else {
                resultMap.put(targetKey, entry.getValue());
            }
        }
        return resultMap;
    }

    /**
     * Get transformed key for executor given linkis-cli key.
     * If there exists none mapping for linkis-cli key. Then this method returns paramKey.
     *
     * @param paramKey
     * @return
     */
    public String getMappedKey(String paramKey) {
        if (this.mapperRules.containsKey(paramKey)) {
            return this.mapperRules.get(paramKey);
        } else {
            return paramKey;
        }
    }

}