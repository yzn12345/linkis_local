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

package com.webank.wedatasphere.linkis.cs.server.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wedatasphere.linkis.cs.ContextSearch;
import com.webank.wedatasphere.linkis.cs.DefaultContextSearch;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.exception.ContextSearchFailedException;
import com.webank.wedatasphere.linkis.cs.persistence.ContextPersistenceManager;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextKeyValue;
import com.webank.wedatasphere.linkis.cs.persistence.persistence.ContextMapPersistence;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;
import com.webank.wedatasphere.linkis.cs.server.parser.KeywordParser;
import com.webank.wedatasphere.linkis.cs.server.service.ContextService;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ContextServiceImpl extends ContextService {

    //对接search

    private ContextSearch contextSearch = new DefaultContextSearch();

    @Autowired
    private ContextCacheService contextCacheService;

    @Autowired
    private ContextPersistenceManager persistenceManager;

    @Autowired
    private KeywordParser keywordParser;


    private ObjectMapper jackson = BDPJettyServerHelper.jacksonJson();


    private ContextMapPersistence getPersistence() throws CSErrorException {
        return persistenceManager.getContextMapPersistence();
    }

    @Override
    public String getName() {
        return ServiceType.CONTEXT.name();
    }

    @Override
    public ContextValue getContextValue(ContextID contextID, ContextKey contextKey) {
        //从缓存取即可,缓存中无会去数据库中拉取
        ContextKeyValue keyValue = contextCacheService.get(contextID, contextKey);
        if (keyValue == null) {
            return null;
        }
        logger.info(String.format("getContextValue,csId:%s,key:%s,csType:%s,csScope:%s",
                contextID.getContextId(), contextKey.getKey(), contextKey.getContextType(), contextKey.getContextScope()));
        return keyValue.getContextValue();
    }

    @Override
    public List<ContextKeyValue> searchContextValue(ContextID contextID, Map<Object, Object> conditionMap) throws ContextSearchFailedException {
        //return List<ContextKeyValue>
        logger.info(String.format("searchContextValue,csId:%s", contextID.getContextId()));
        return contextSearch.search(contextCacheService, contextID, conditionMap);
    }

/*    @Override
    public List<ContextKeyValue> searchContextValueByCondition(Condition condition) throws ContextSearchFailedException {
        //return List<ContextKeyValue>
        return contextSearch.search(contextCacheService, null, condition);
    }*/

    @Override
    public void setValueByKey(ContextID contextID, ContextKey contextKey, ContextValue contextValue) throws CSErrorException, ClassNotFoundException, JsonProcessingException {
        //1.获取value
        Object value = contextValue.getValue();
        //2.解析keywords,放入contextKey的keys中
        Set<String> keys = keywordParser.parse(value);
        keys.add(contextKey.getKey());
        contextValue.setKeywords(jackson.writeValueAsString(keys));
        //3.缓存中是否有这个key,没有创建,有就更新
        ContextKeyValue keyValue = contextCacheService.get(contextID, contextKey);
        if (keyValue == null) {
            //创建校验scope和tye
            if (contextKey.getContextScope() == null || contextKey.getContextType() == null) {
                throw new CSErrorException(97000, "try to create context ,type or scope cannot be empty");
            }
            //这里没有给出contextKeyvalue的具体实现,给个默认的persistence
            logger.warn(String.format("setValueByKey, keyValue is not exist, csId:%s,key:%s", contextID.getContextId(), contextKey.getKey()));
            keyValue = new PersistenceContextKeyValue();
            keyValue.setContextKey(contextKey);
            keyValue.setContextValue(contextValue);
            getPersistence().create(contextID, keyValue);
        } else {
            //update的话,如果scope和type 是空的,要用数据库中的值,因为update缓存要用到
            if (contextKey.getContextScope() == null) {
                contextKey.setContextScope(keyValue.getContextKey().getContextScope());
            }
            if (contextKey.getContextType() == null) {
                contextKey.setContextType(keyValue.getContextKey().getContextType());
            }
            keyValue.setContextKey(contextKey);
            keyValue.setContextValue(contextValue);
            getPersistence().update(contextID, keyValue);
        }
        //4.更新缓存
        contextCacheService.put(contextID, keyValue);
        logger.info(String.format("setValueByKey, csId:%s,key:%s,keywords:%s", contextID.getContextId(), contextKey.getKey(), contextValue.getKeywords()));
    }

    @Override
    public void setValue(ContextID contextID, ContextKeyValue contextKeyValue) throws CSErrorException, ClassNotFoundException, JsonProcessingException {
        //1.解析keywords
        Object value = contextKeyValue.getContextValue().getValue();
        Set<String> keys = keywordParser.parse(value);
        keys.add(contextKeyValue.getContextKey().getKey());
        contextKeyValue.getContextValue().setKeywords(jackson.writeValueAsString(keys));
        //2.数据库中是否有这个key,没有就创建,有就更新
        ContextKeyValue keyValue = contextCacheService.get(contextID, contextKeyValue.getContextKey());
        if (keyValue == null) {
            logger.warn("cache can not find contextId:{},key:{},now try to load from MySQL", contextID.getContextId(), contextKeyValue.getContextKey().getKey());
            keyValue = getPersistence().get(contextID, contextKeyValue.getContextKey());
            if (keyValue != null) {
                logger.warn("MySQL find the key,now reset the cache and get it");
                contextCacheService.rest(contextID, contextKeyValue.getContextKey());
                keyValue = contextCacheService.get(contextID, contextKeyValue.getContextKey());
            }
        }
        if (keyValue == null) {
            if (contextKeyValue.getContextKey().getContextScope() == null || contextKeyValue.getContextKey().getContextType() == null) {
                throw new CSErrorException(97000, "try to create context ,type or scope cannot be empty");
            }
            getPersistence().create(contextID, contextKeyValue);
        } else {
            //update的话,如果scope和type 是空的,要用数据库中的值,因为update缓存要用到
            if (contextKeyValue.getContextKey().getContextScope() == null) {
                contextKeyValue.getContextKey().setContextScope(keyValue.getContextKey().getContextScope());
            }
            if (contextKeyValue.getContextKey().getContextType() == null) {
                contextKeyValue.getContextKey().setContextType(keyValue.getContextKey().getContextType());
            }
            getPersistence().update(contextID, contextKeyValue);
        }
        //3.更新缓存
        contextCacheService.put(contextID, contextKeyValue);
        logger.info(String.format("setValue, csId:%s,key:%s", contextID.getContextId(), contextKeyValue.getContextKey().getKey()));
    }

    @Override
    public void resetValue(ContextID contextID, ContextKey contextKey) throws CSErrorException {
        //1.reset 数据库
        getPersistence().reset(contextID, contextKey);
        //2.reset 缓存
        contextCacheService.rest(contextID, contextKey);
        logger.info(String.format("resetValue, csId:%s,key:%s", contextID.getContextId(), contextKey.getKey()));
    }

    @Override
    public void removeValue(ContextID contextID, ContextKey contextKey) throws CSErrorException {
        ContextKeyValue contextKeyValue = contextCacheService.get(contextID, contextKey);
        if (contextKeyValue == null) {
            return;
        }
        //如果scope和type 不为空,则从原数据中进行赋值
        if (contextKey.getContextScope() == null) {
            contextKey.setContextScope(contextKeyValue.getContextKey().getContextScope());
        }
        if (contextKey.getContextType() == null) {
            contextKey.setContextType(contextKeyValue.getContextKey().getContextType());
        }
        //1.remove 数据库
        getPersistence().remove(contextID, contextKey);
        //2.remove 缓存
        contextCacheService.remove(contextID, contextKey);
        logger.info(String.format("removeValue, csId:%s,key:%s", contextID.getContextId(), contextKey.getKey()));
    }

    @Override
    public void removeAllValue(ContextID contextID) throws CSErrorException {
        //移除数据库
        getPersistence().removeAll(contextID);
        //移除缓存
        contextCacheService.removeAll(contextID);

        logger.info(String.format("removeAllValue, csId:%s", contextID.getContextId()));
    }

    @Override
    public void removeAllValueByKeyPrefixAndContextType(ContextID contextID, ContextType contextType, String keyPrefix) throws CSErrorException {
        contextCacheService.removeByKeyPrefix(contextID, keyPrefix, contextType);
        getPersistence().removeByKeyPrefix(contextID, contextType, keyPrefix);
        logger.info(String.format("removeAllValueByKeyPrefixAndContextType, csId:%s,csType:%s,keyPrefix:%s",
                contextID.getContextId(), contextType, keyPrefix));
    }

    @Override
    public void removeAllValueByKeyPrefix(ContextID contextID, String keyPrefix) throws CSErrorException {
        contextCacheService.removeByKeyPrefix(contextID, keyPrefix);
        getPersistence().removeByKeyPrefix(contextID, keyPrefix);
        logger.info(String.format("removeAllValueByKeyPrefix, csId:%s,keyPrefix:%s",
                contextID.getContextId(), keyPrefix));
    }


}
