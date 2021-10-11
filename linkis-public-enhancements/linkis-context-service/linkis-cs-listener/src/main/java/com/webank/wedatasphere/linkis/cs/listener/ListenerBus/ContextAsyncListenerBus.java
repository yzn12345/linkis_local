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

package com.webank.wedatasphere.linkis.cs.listener.ListenerBus;

import com.webank.wedatasphere.linkis.common.listener.Event;
import com.webank.wedatasphere.linkis.common.listener.ListenerEventBus;
import com.webank.wedatasphere.linkis.cs.listener.ContextAsyncEventListener;
import com.webank.wedatasphere.linkis.cs.listener.conf.ContextListenerConf;


public class ContextAsyncListenerBus<L extends ContextAsyncEventListener, E extends Event> extends ListenerEventBus<L, E> {


    private static final String NAME = "ContextAsyncListenerBus";

    public ContextAsyncListenerBus() {
        super(ContextListenerConf.WDS_CS_LISTENER_ASYN_QUEUE_CAPACITY, NAME, ContextListenerConf.WDS_CS_LISTENER_ASYN_CONSUMER_THREAD_MAX, ContextListenerConf.WDS_CS_LISTENER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX);
    }

    @Override
    public void doPostEvent(L listener, E event) {
        listener.onEvent(event);
    }


    private static ContextAsyncListenerBus contextAsyncListenerBus = null;

    public static ContextAsyncListenerBus getInstance() {
        if (contextAsyncListenerBus == null) {
            synchronized (ContextAsyncListenerBus.class) {
                if (contextAsyncListenerBus == null) {
                    contextAsyncListenerBus = new ContextAsyncListenerBus();
                    contextAsyncListenerBus.start();
                }
            }
        }
        return contextAsyncListenerBus;
    }
}
