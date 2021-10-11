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

package com.webank.wedatasphere.linkis.cs.common.serialize.impl.context;

import com.webank.wedatasphere.linkis.cs.common.entity.source.CommonContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.serialize.AbstractSerializer;
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class ContextKeyValueSerializer extends AbstractSerializer<CommonContextKeyValue> {

    private static final Logger logger = LoggerFactory.getLogger(ContextKeyValueSerializer.class);

    @Override
    public String getJsonValue(CommonContextKeyValue contextKeyValue) throws CSErrorException {
        try {
            Map<String, String> map = new HashMap<>();
            ContextKey contextKey = contextKeyValue.getContextKey();
            ContextValue contextValue = contextKeyValue.getContextValue();
            map.put("key", ContextSerializationHelper.getInstance().serialize(contextKey));
            map.put("value", ContextSerializationHelper.getInstance().serialize(contextValue));
            return CSCommonUtils.gson.toJson(map);
        } catch (Exception e) {
            logger.error("Failed to serialize contextKeyValue: ", e);
            throw new CSErrorException(97000, "Failed to serialize contextKeyValue");
        }
    }

    @Override
    public CommonContextKeyValue fromJson(String json) throws CSErrorException {
        try {
            Map<String, String> jsonObj = CSCommonUtils.gson.fromJson(json, new HashMap<String, String>().getClass());
            String key = jsonObj.get("key");
            String value = jsonObj.get("value");
            Object contextKey = ContextSerializationHelper.getInstance().deserialize(key);
            Object contextValue = ContextSerializationHelper.getInstance().deserialize(value);
            CommonContextKeyValue contextKeyValue = new CommonContextKeyValue();
            contextKeyValue.setContextKey((ContextKey) contextKey);
            contextKeyValue.setContextValue((ContextValue) contextValue);
            return contextKeyValue;
        } catch (Exception e) {
            logger.error("Failed to deserialize contextKeyValue: ", e);
            throw new CSErrorException(97000, "Failed to serialize contextKeyValue");
        }
    }

    @Override
    public String getType() {
        return "commonContextKeyValue";
    }

    @Override
    public boolean accepts(Object obj) {
        if (null != obj && obj instanceof CommonContextKeyValue){
            return true;
        }
        return false;
    }
}
