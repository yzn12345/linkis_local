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

package com.webank.wedatasphere.linkis.engineconn.executor.listener

import com.webank.wedatasphere.linkis.common.listener.ListenerEventBus
import com.webank.wedatasphere.linkis.engineconn.executor.conf.EngineConnExecutorConfiguration
import com.webank.wedatasphere.linkis.engineconn.executor.listener.event.EngineConnAsyncEvent

class EngineConnAsyncListenerBus(eventQueueCapacity: Int, name: String,
                                 listenerConsumerThreadSize: Int,
                                 listenerThreadMaxFreeTime: Long)
  extends ListenerEventBus[EngineConnAsyncListener, EngineConnAsyncEvent](eventQueueCapacity, name)(listenerConsumerThreadSize, listenerThreadMaxFreeTime) {

  /**
    * Post an event to the specified listener. `onPostEvent` is guaranteed to be called in the same
    * thread for all listeners.
    */
  override protected def doPostEvent(listener: EngineConnAsyncListener, event: EngineConnAsyncEvent): Unit = {
    listener.onEvent(event)
  }

}

object EngineConnAsyncListenerBus {

  def NAME: String = "EngineServerAsyncListenerBus"

  lazy val getInstance = new EngineConnAsyncListenerBus(
    EngineConnExecutorConfiguration.ENGINE_SERVER_LISTENER_ASYNC_QUEUE_CAPACITY.getValue,
    NAME,
    EngineConnExecutorConfiguration.ENGINE_SERVER_LISTENER_ASYNC_CONSUMER_THREAD_MAX.getValue,
    EngineConnExecutorConfiguration.ENGINE_SERVER_LISTENER_ASYNC_CONSUMER_THREAD_FREE_TIME_MAX.getValue.toLong)

}