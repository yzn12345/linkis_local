/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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


package com.webank.wedatasphere.linkis.entrance.context;

import com.webank.wedatasphere.linkis.entrance.EntranceContext;
import com.webank.wedatasphere.linkis.entrance.EntranceParser;
import com.webank.wedatasphere.linkis.entrance.annotation.*;
import com.webank.wedatasphere.linkis.entrance.event.*;
import com.webank.wedatasphere.linkis.entrance.execute.EntranceExecutorManager;
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor;
import com.webank.wedatasphere.linkis.entrance.log.LogManager;
import com.webank.wedatasphere.linkis.entrance.persistence.PersistenceManager;
import com.webank.wedatasphere.linkis.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

@EntranceContextBeanAnnotation
public class DefaultEntranceContext extends EntranceContext {
    private static Logger logger = LoggerFactory.getLogger(DefaultEntranceContext.class);

    @EntranceParserBeanAnnotation.EntranceParserAutowiredAnnotation
    private EntranceParser entranceParser;

    @PersistenceManagerBeanAnnotation.PersistenceManagerAutowiredAnnotation
    private PersistenceManager persistenceManager;

    @LogManagerBeanAnnotation.LogManagerAutowiredAnnotation
    private LogManager logManager;

    @SchedulerBeanAnnotation.SchedulerAutowiredAnnotation
    private Scheduler scheduler;

    @EntranceInterceptorBeanAnnotation.EntranceInterceptorAutowiredAnnotation
    private EntranceInterceptor[] interceptors;

    @EntranceListenerBusBeanAnnotation.EntranceListenerBusAutowiredAnnotation
    private EntranceEventListenerBus<EntranceEventListener, EntranceEvent> listenerBus;

    @EntranceLogListenerBusBeanAnnotation.EntranceLogListenerBusAutowiredAnnotation
    private EntranceLogListenerBus<EntranceLogListener, EntranceLogEvent> logListenerBus;



    public DefaultEntranceContext(EntranceParser entranceParser, PersistenceManager persistenceManager, LogManager logManager,
                                  Scheduler scheduler, EntranceInterceptor[] interceptors, EntranceEventListenerBus<EntranceEventListener, EntranceEvent> listenerBus,
                                  EntranceLogListenerBus<EntranceLogListener, EntranceLogEvent> logListenerBus) {
        this.entranceParser = entranceParser;
        this.persistenceManager = persistenceManager;
        this.logManager = logManager;
        this.scheduler = scheduler;
        this.interceptors = interceptors;
        this.listenerBus = listenerBus;
        this.logListenerBus = logListenerBus;
    }

    public DefaultEntranceContext() {
    }

    @PostConstruct
    public void init() {
        entranceParser.setEntranceContext(this);
        logger.info("Finished init entranceParser from postConstruct end!");
        persistenceManager.setEntranceContext(this);
        logManager.setEntranceContext(this);
        logListenerBus.addListener(logManager);
       /* if(scheduler.getSchedulerContext().getOrCreateExecutorManager() instanceof EntranceExecutorManager) {
            listenerBus.addListener(((EntranceExecutorManager) scheduler.getSchedulerContext().getOrCreateExecutorManager()).getOrCreateEngineManager());
        }*/
    }

    @Override
    public Scheduler getOrCreateScheduler() {
        return scheduler;
    }

    @Override
    public EntranceParser getOrCreateEntranceParser() {
        return this.entranceParser;
    }

    @Override
    public EntranceInterceptor[] getOrCreateEntranceInterceptors() {
        return interceptors;
    }

    @Override
    public LogManager getOrCreateLogManager() {
        return logManager;
    }

    @Override
    public PersistenceManager getOrCreatePersistenceManager() {
        return persistenceManager;
    }

    @Override
    public EntranceEventListenerBus<EntranceEventListener, EntranceEvent> getOrCreateEventListenerBus() {
        return this.listenerBus;
    }

    @Override
    public EntranceLogListenerBus<EntranceLogListener, EntranceLogEvent> getOrCreateLogListenerBus() {
        return this.logListenerBus;
    }

}
