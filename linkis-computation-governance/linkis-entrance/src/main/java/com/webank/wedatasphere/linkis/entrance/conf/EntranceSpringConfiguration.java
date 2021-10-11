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

package com.webank.wedatasphere.linkis.entrance.conf;

import com.webank.wedatasphere.linkis.entrance.EntranceParser;
import com.webank.wedatasphere.linkis.entrance.annotation.*;
import com.webank.wedatasphere.linkis.entrance.event.*;
import com.webank.wedatasphere.linkis.entrance.execute.impl.EntranceExecutorManagerImpl;
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor;
import com.webank.wedatasphere.linkis.entrance.interceptor.OnceJobInterceptor;
import com.webank.wedatasphere.linkis.entrance.interceptor.impl.*;
import com.webank.wedatasphere.linkis.entrance.log.*;
import com.webank.wedatasphere.linkis.entrance.parser.CommonEntranceParser;
import com.webank.wedatasphere.linkis.entrance.persistence.*;
import com.webank.wedatasphere.linkis.entrance.scheduler.EntranceGroupFactory;
import com.webank.wedatasphere.linkis.entrance.scheduler.EntranceSchedulerContext;
import com.webank.wedatasphere.linkis.orchestrator.ecm.EngineConnManagerBuilder;
import com.webank.wedatasphere.linkis.orchestrator.ecm.EngineConnManagerBuilder$;
import com.webank.wedatasphere.linkis.orchestrator.ecm.entity.Policy;
import com.webank.wedatasphere.linkis.scheduler.Scheduler;
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorManager;
import com.webank.wedatasphere.linkis.scheduler.queue.ConsumerManager;
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelConsumerManager;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

import static com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration.ENTRANCE_SCHEDULER_MAX_PARALLELISM_USERS;

/**
 * Description:This configuration class is used to generate some singleton classes in the entity module.(该配置类用于生成entrance模块中的一些单例类)
 */
@Configuration
//@AutoConfigureBefore({EntranceServer.class, EntranceExecutionService.class})
public class EntranceSpringConfiguration {

    private Logger logger = LoggerFactory.getLogger(getClass());
    {
        logger.info("load the ujes-entrance spring configuration.");
    }


    @PersistenceEngineBeanAnnotation
    @ConditionalOnMissingBean(name = {PersistenceEngineBeanAnnotation.BEAN_NAME})
    public PersistenceEngine generatePersistenceEngine(){
        return new QueryPersistenceEngine();
    }

    @ResultSetEngineBeanAnnotation
    @ConditionalOnMissingBean(name = {ResultSetEngineBeanAnnotation.BEAN_NAME})
    public ResultSetEngine generateResultSetEngine(){
        return new EntranceResultSetEngine();
    }

    @PersistenceManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {PersistenceManagerBeanAnnotation.BEAN_NAME})
    public PersistenceManager generatePersistenceManager(@PersistenceEngineBeanAnnotation.PersistenceEngineAutowiredAnnotation PersistenceEngine persistenceEngine,
                                                         @ResultSetEngineBeanAnnotation.ResultSetEngineAutowiredAnnotation ResultSetEngine resultSetEngine){
        logger.info("init PersistenceManager.");
        QueryPersistenceManager persistenceManager = new QueryPersistenceManager();
        persistenceManager.setPersistenceEngine(persistenceEngine);
        persistenceManager.setResultSetEngine(resultSetEngine);
        return persistenceManager;
    }

    @EntranceParserBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceParserBeanAnnotation.BEAN_NAME})
    public EntranceParser generateEntranceParser(@PersistenceManagerBeanAnnotation.PersistenceManagerAutowiredAnnotation PersistenceManager persistenceManager){
        return new CommonEntranceParser(persistenceManager);
    }

    @EntranceListenerBusBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceListenerBusBeanAnnotation.BEAN_NAME})
    public EntranceEventListenerBus<EntranceEventListener, EntranceEvent> generateEntranceEventListenerBus() {
        EntranceEventListenerBus<EntranceEventListener, EntranceEvent> entranceEventListenerBus = new EntranceEventListenerBus<EntranceEventListener, EntranceEvent>();
        entranceEventListenerBus.start();
        return entranceEventListenerBus;
    }

    @EntranceLogListenerBusBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceLogListenerBusBeanAnnotation.BEAN_NAME})
    public EntranceLogListenerBus<EntranceLogListener, EntranceLogEvent> generateEntranceLogListenerBus() {
        EntranceLogListenerBus<EntranceLogListener, EntranceLogEvent> entranceLogListenerBus = new EntranceLogListenerBus<EntranceLogListener, EntranceLogEvent>();
        entranceLogListenerBus.start();
        return entranceLogListenerBus;
    }

    /**
     * Update by peaceWong add CSEntranceInterceptor
     *
     * @return
     */
    @EntranceInterceptorBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceInterceptorBeanAnnotation.BEAN_NAME})
    public EntranceInterceptor[] generateEntranceInterceptors() {
        return new EntranceInterceptor[]{
                new OnceJobInterceptor(),
                new CSEntranceInterceptor(),
                new ShellDangerousGrammerInterceptor(),
                new PythonCodeCheckInterceptor(),
                new DBInfoCompleteInterceptor(),
                new SparkCodeCheckInterceptor(),
                new SQLCodeCheckInterceptor(),
                new LabelCheckInterceptor(),
                new VarSubstitutionInterceptor(),
                new LogPathCreateInterceptor(),
                new StorePathEntranceInterceptor(),
                new ScalaCodeInterceptor(),
                new SQLLimitEntranceInterceptor(),
                new CommentInterceptor()
                };
    }

    @ErrorCodeListenerBeanAnnotation
    @ConditionalOnMissingBean(name = {ErrorCodeListenerBeanAnnotation.BEAN_NAME})
    public ErrorCodeListener generateErrorCodeListener(@PersistenceManagerBeanAnnotation.PersistenceManagerAutowiredAnnotation PersistenceManager persistenceManager,
                                                       @EntranceParserBeanAnnotation.EntranceParserAutowiredAnnotation EntranceParser entranceParser) {
        PersistenceErrorCodeListener errorCodeListener = new PersistenceErrorCodeListener();
        errorCodeListener.setEntranceParser(entranceParser);
        errorCodeListener.setPersistenceManager(persistenceManager);
        return errorCodeListener;
    }

    @ErrorCodeManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {ErrorCodeManagerBeanAnnotation.BEAN_NAME})
    public ErrorCodeManager generateErrorCodeManager() {
        try {
            Class.forName("com.webank.wedatasphere.linkis.errorcode.client.handler.LinkisErrorCodeHandler");
        } catch (final Exception e) {
            logger.error("failed to init linkis error code handler", e);
        }
        return FlexibleErrorCodeManager$.MODULE$;
    }

    @LogManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {LogManagerBeanAnnotation.BEAN_NAME})
    public LogManager generateLogManager(@ErrorCodeListenerBeanAnnotation.ErrorCodeListenerAutowiredAnnotation ErrorCodeListener errorCodeListener,
                                         @ErrorCodeManagerBeanAnnotation.ErrorCodeManagerAutowiredAnnotation ErrorCodeManager errorCodeManager){
        CacheLogManager logManager = new CacheLogManager();
        logManager.setErrorCodeListener(errorCodeListener);
        logManager.setErrorCodeManager(errorCodeManager);
        return logManager;
    }


    @GroupFactoryBeanAnnotation
    @ConditionalOnMissingBean(name = {GroupFactoryBeanAnnotation.BEAN_NAME})
    public GroupFactory generateGroupFactory(){
        return new EntranceGroupFactory();
    }

    @ConsumerManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {ConsumerManagerBeanAnnotation.BEAN_NAME})
    public ConsumerManager generateConsumerManager(){
        return new ParallelConsumerManager(ENTRANCE_SCHEDULER_MAX_PARALLELISM_USERS().getValue(), "EntranceJobScheduler");
    }

    @SchedulerContextBeanAnnotation
    @ConditionalOnMissingBean(name = {SchedulerContextBeanAnnotation.BEAN_NAME})
    public SchedulerContext generateSchedulerContext(@GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation GroupFactory groupFactory,
                                                     @EntranceExecutorManagerBeanAnnotation.EntranceExecutorManagerAutowiredAnnotation ExecutorManager executorManager,
                                                     @ConsumerManagerBeanAnnotation.ConsumerManagerAutowiredAnnotation ConsumerManager consumerManager) {
        return new EntranceSchedulerContext(groupFactory, consumerManager, executorManager);
    }

   /* @EngineRequesterBeanAnnotation
    @ConditionalOnMissingBean(name = {EngineRequesterBeanAnnotation.BEAN_NAME})
    public EngineRequester generateEngineRequester(){
        return new EngineRequesterImpl();
    }
*/
  /*  @EngineSelectorBeanAnnotation
    @ConditionalOnMissingBean(name = {EngineSelectorBeanAnnotation.BEAN_NAME})
    public EngineSelector generateEngineSelector(@EntranceListenerBusBeanAnnotation.EntranceListenerBusAutowiredAnnotation
                                                             EntranceEventListenerBus<EntranceEventListener, EntranceEvent> entranceEventListenerBus) {
        SingleEngineSelector singleEngineSelector = new SingleEngineSelector();
        singleEngineSelector.setEntranceEventListenerBus(entranceEventListenerBus);
        return singleEngineSelector;
    }
*/
    /*@EngineBuilderBeanAnnotation
    @ConditionalOnMissingBean(name = {EngineBuilderBeanAnnotation.BEAN_NAME})
    public EngineBuilder generateEngineBuilder(@GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation GroupFactory groupFactory) {
        return new AbstractEngineBuilder(groupFactory) {
            @Override
            public EntranceEngine createEngine(long id) {
                return new SingleEntranceEngine(id);
            }
        };
    }*/

   /* @EngineManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {EngineManagerBeanAnnotation.BEAN_NAME})
    public EngineManager generateEngineManager() {
        return new EngineManagerImpl();
    }*/

    @EntranceExecutorManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceExecutorManagerBeanAnnotation.BEAN_NAME})
    public ExecutorManager generateExecutorManager(@GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation GroupFactory groupFactory) {
        EngineConnManagerBuilder engineConnManagerBuilder = EngineConnManagerBuilder$.MODULE$.builder();
        engineConnManagerBuilder.setPolicy(Policy.Process);
        return new EntranceExecutorManagerImpl(groupFactory, engineConnManagerBuilder.build());
    }

    @SchedulerBeanAnnotation
    @ConditionalOnMissingBean(name = {SchedulerBeanAnnotation.BEAN_NAME})
    public Scheduler generateScheduler(@SchedulerContextBeanAnnotation.SchedulerContextAutowiredAnnotation SchedulerContext schedulerContext) {
        Scheduler scheduler = new ParallelScheduler(schedulerContext);
        scheduler.init();
        scheduler.start();
        return scheduler;
    }




  /*  @NewEngineBroadcastListenerBeanAnnotation
    @ConditionalOnMissingBean(name = {NewEngineBroadcastListenerBeanAnnotation.BEAN_NAME})
    public NewEngineBroadcastListener generateNewEngineBroadcastListener(@EntranceExecutorManagerBeanAnnotation.EntranceExecutorManagerAutowiredAnnotation
                                                                                 EntranceExecutorManager entranceExecutorManager) {
        NewEngineBroadcastListener newEngineBroadcastListener = new NewEngineBroadcastListener();
        newEngineBroadcastListener.setEntranceExecutorManager(entranceExecutorManager);
        return newEngineBroadcastListener;
    }

    @ResponseEngineStatusChangedBroadcastListenerBeanAnnotation
    @ConditionalOnMissingBean(name = {ResponseEngineStatusChangedBroadcastListenerBeanAnnotation.BEAN_NAME})
    public ResponseEngineStatusChangedBroadcastListener generateResponseEngineStatusChangedBroadcastListener(@EntranceExecutorManagerBeanAnnotation.EntranceExecutorManagerAutowiredAnnotation
                                                                                 EntranceExecutorManager entranceExecutorManager) {
        ResponseEngineStatusChangedBroadcastListener broadcastListener = new ResponseEngineStatusChangedBroadcastListener();
        broadcastListener.setEntranceExecutorManager(entranceExecutorManager);
        return broadcastListener;
    }*/
}
