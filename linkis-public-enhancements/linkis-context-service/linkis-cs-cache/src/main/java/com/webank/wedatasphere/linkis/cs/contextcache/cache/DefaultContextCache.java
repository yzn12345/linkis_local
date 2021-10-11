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

package com.webank.wedatasphere.linkis.cs.contextcache.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.webank.wedatasphere.linkis.common.listener.Event;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.csid.ContextIDValue;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.csid.ContextIDValueGenerator;
import com.webank.wedatasphere.linkis.cs.contextcache.metric.ContextCacheMetric;
import com.webank.wedatasphere.linkis.cs.contextcache.metric.ContextIDMetric;
import com.webank.wedatasphere.linkis.cs.contextcache.metric.DefaultContextCacheMetric;
import com.webank.wedatasphere.linkis.cs.listener.CSIDListener;
import com.webank.wedatasphere.linkis.cs.listener.ListenerBus.ContextAsyncListenerBus;
import com.webank.wedatasphere.linkis.cs.listener.event.ContextIDEvent;
import com.webank.wedatasphere.linkis.cs.listener.event.impl.DefaultContextIDEvent;
import com.webank.wedatasphere.linkis.cs.listener.manager.imp.DefaultContextListenerManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.webank.wedatasphere.linkis.cs.listener.event.enumeration.OperateType.*;

@Component
public class DefaultContextCache implements ContextCache , CSIDListener {

    private static final Logger logger = LoggerFactory.getLogger(DefaultContextCache.class);

    private ContextAsyncListenerBus listenerBus = DefaultContextListenerManager.getInstance().getContextAsyncListenerBus();


    @Autowired
    private RemovalListener<String, ContextIDValue> contextIDRemoveListener;

    @Autowired
    private ContextIDValueGenerator contextIDValueGenerator ;

    private Cache<String, ContextIDValue> cache =  null;

    private ContextCacheMetric contextCacheMetric = new DefaultContextCacheMetric();

    @PostConstruct
    private void init(){
        listenerBus.addListener(this);
        this.cache = CacheBuilder.newBuilder().maximumSize(3000)
                .removalListener(contextIDRemoveListener)
                .recordStats().build();
    }

    @Override
    public ContextIDValue getContextIDValue(ContextID contextID) throws CSErrorException {
        if(null == contextID || StringUtils.isBlank(contextID.getContextId())) {
            return null;
        }
        try {
            ContextIDValue contextIDValue = cache.getIfPresent(contextID.getContextId());
            if (contextIDValue == null){
                contextIDValue = contextIDValueGenerator.createContextIDValue(contextID);
                put(contextIDValue);
                DefaultContextIDEvent defaultContextIDEvent = new DefaultContextIDEvent();
                defaultContextIDEvent.setContextID(contextID);
                defaultContextIDEvent.setOperateType(ADD);
                listenerBus.post(defaultContextIDEvent);
            }
            DefaultContextIDEvent defaultContextIDEvent = new DefaultContextIDEvent();
            defaultContextIDEvent.setContextID(contextID);
            defaultContextIDEvent.setOperateType(ACCESS);
            listenerBus.post(defaultContextIDEvent);
            return contextIDValue;
        } catch (Exception e){
            String errorMsg = String.format("Failed to get contextIDValue of ContextID(%s)", contextID.getContextId());
            logger.error(errorMsg);
            throw new CSErrorException(97001, errorMsg, e);
        }
    }

    @Override
    public void remove(ContextID contextID) {
        if ( null != contextID && StringUtils.isNotBlank(contextID.getContextId())){
            logger.info("From cache to remove contextID:{}", contextID.getContextId());
            cache.invalidate(contextID.getContextId());
        }
    }

    @Override
    public void put(ContextIDValue contextIDValue) throws CSErrorException {

        if(contextIDValue != null && StringUtils.isNotBlank(contextIDValue.getContextID())){
            cache.put(contextIDValue.getContextID(), contextIDValue);
        }
    }

    @Override
    public Map<String, ContextIDValue> getAllPresent(List<ContextID> contextIDList) {
        List<String> contextIDKeys = contextIDList.stream().map(contextID -> {
            if(StringUtils.isBlank(contextID.getContextId())) {
                return  null;
            }
            return contextID.getContextId();
        }).filter(StringUtils :: isNotBlank).collect(Collectors.toList());
        return cache.getAllPresent(contextIDKeys);
    }

    @Override
    public void refreshAll() throws CSErrorException {
        //TODO
    }

    @Override
    public void putAll(List<ContextIDValue> contextIDValueList) throws CSErrorException {
        for (ContextIDValue contextIDValue : contextIDValueList){
            put(contextIDValue);
        }
    }

    @Override
    public ContextIDValue loadContextIDValue(ContextID contextID) {
        return null;
    }

    @Override
    public ContextCacheMetric getContextCacheMetric() {
        return this.contextCacheMetric;
    }

    @Override
    public void onCSIDAccess(ContextIDEvent contextIDEvent) {
        ContextID contextID = contextIDEvent.getContextID();
        try {
            ContextIDValue contextIDValue = getContextIDValue(contextID);
            ContextIDMetric contextIDMetric = contextIDValue.getContextIDMetric();

            contextIDMetric.setLastAccessTime(System.currentTimeMillis());
            contextIDMetric.addCount();
            getContextCacheMetric().addCount();
        } catch (CSErrorException e) {
            logger.error("Failed to deal CSIDAccess event csid is {}", contextID.getContextId());
        }
    }

    @Override
    public void onCSIDADD(ContextIDEvent contextIDEvent) {
        logger.info("deal contextID ADD event of {}", contextIDEvent.getContextID());
        getContextCacheMetric().addCount();
        getContextCacheMetric().setCachedCount(getContextCacheMetric().getCachedCount() + 1);
        logger.info("Now, The cachedCount is (%d)", getContextCacheMetric().getCachedCount());
    }

    @Override
    public void onCSIDRemoved(ContextIDEvent contextIDEvent) {
        logger.info("deal contextID remove event of {}", contextIDEvent.getContextID());
        getContextCacheMetric().setCachedCount(getContextCacheMetric().getCachedCount() - 1);
        logger.info("Now, The cachedCount is (%d)", getContextCacheMetric().getCachedCount());
    }

    @Override
    public void onEventError( Event event,  Throwable t) {
        logger.error("Failed to deal event", t);
    }

    @Override
    public void onEvent(Event event) {
        DefaultContextIDEvent defaultContextIDEvent = null;
        if (event != null && event instanceof DefaultContextIDEvent){
            defaultContextIDEvent = (DefaultContextIDEvent) event;
        }
        if (null == defaultContextIDEvent) {
            return;
        }
        if (ADD.equals(defaultContextIDEvent.getOperateType())){
           onCSIDADD(defaultContextIDEvent);
        } else if (DELETE.equals(defaultContextIDEvent.getOperateType())) {
            onCSIDRemoved(defaultContextIDEvent);
        } else {
            onCSIDAccess(defaultContextIDEvent);
        }
    }
}
