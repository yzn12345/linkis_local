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

package com.webank.wedatasphere.linkis.ecm.server;

import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import com.webank.wedatasphere.linkis.LinkisBaseServerApp;
import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.ecm.core.listener.ECMAsyncListenerBus;
import com.webank.wedatasphere.linkis.ecm.core.listener.ECMSyncListenerBus;
import com.webank.wedatasphere.linkis.ecm.server.context.ECMContext;
import com.webank.wedatasphere.linkis.ecm.server.listener.ECMClosedEvent;
import com.webank.wedatasphere.linkis.ecm.server.listener.ECMReadyEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import static com.webank.wedatasphere.linkis.ecm.server.conf.ECMConfiguration.ECM_ASYNC_BUS_WAITTOEMPTY_TIME;


public class LinkisECMApplication extends DataWorkCloudApplication {

    private static ECMContext ecmContext;

    private volatile static boolean ready;

    private static ServiceInstance ecmServiceInstance;

    private static String[] parmas;

    public static void main(String[] args) throws ReflectiveOperationException {
        parmas = args;
        LinkisBaseServerApp.main(args);
    }

    public static ECMContext getContext() {
        return ecmContext;
    }

    public static void setContext(ECMContext context) {
        ecmContext = context;
    }

    public static ServiceInstance getECMServiceInstance() {
        return ecmServiceInstance;
    }

    public static void setECMServiceInstance(ServiceInstance serviceInstance) {
        ecmServiceInstance = serviceInstance;
    }

    public static boolean isReady() {
        return ready;
    }

    public static void setReady(boolean applicationReady) {
        ready = applicationReady;
    }

    public static String[] getParmas() {
        return parmas;
    }
}


@Configuration
class ECMApplicationListener {

    private final Log logger = LogFactory.getLog(this.getClass());

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        ServiceInstance serviceInstance = DataWorkCloudApplication.getServiceInstance();
        LinkisECMApplication.setECMServiceInstance(serviceInstance);
        ECMContext context = event.getApplicationContext().getBean(ECMContext.class);
        LinkisECMApplication.setContext(context);
        ECMAsyncListenerBus emAsyncListenerBus = context.getECMAsyncListenerBus();
        ECMSyncListenerBus emSyncListenerBus = context.getECMSyncListenerBus();
        emAsyncListenerBus.start();
        ECMReadyEvent ecmReadyEvent = new ECMReadyEvent(LinkisECMApplication.getParmas());
        emAsyncListenerBus.postToAll(ecmReadyEvent);
        emSyncListenerBus.postToAll(ecmReadyEvent);
        LinkisECMApplication.setReady(true);
        logger.info(String.format("ECM:%s is ready", serviceInstance));
    }

    @EventListener
    public void onApplicationClosed(ContextClosedEvent contextClosedEvent) {
        ServiceInstance serviceInstance = DataWorkCloudApplication.getServiceInstance();
        LinkisECMApplication.setReady(false);
        ECMClosedEvent ecmClosedEvent = new ECMClosedEvent();
        LinkisECMApplication.getContext().getECMSyncListenerBus().postToAll(ecmClosedEvent);
        ECMAsyncListenerBus ecmAsyncListenerBus = LinkisECMApplication.getContext().getECMAsyncListenerBus();
        ecmAsyncListenerBus.postToAll(ecmClosedEvent);
        logger.info(String.format("wait ECM:%s asyncBus empty", serviceInstance));
        try {
            ecmAsyncListenerBus.waitUntilEmpty(ECM_ASYNC_BUS_WAITTOEMPTY_TIME());
        } catch (Throwable e) {
            logger.error("wait ECM asyncBus empty failed", e);
        }
        logger.info("ECM asyncBus is empty");
        ecmAsyncListenerBus.stop();
        logger.info("ECM is closed");
    }

}

