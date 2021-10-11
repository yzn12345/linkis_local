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

package com.webank.wedatasphere.linkis.ecm.server.context

import com.webank.wedatasphere.linkis.ecm.core.listener.{ECMAsyncListenerBus, ECMSyncListenerBus}
import com.webank.wedatasphere.linkis.ecm.core.metrics.ECMMetrics
import com.webank.wedatasphere.linkis.ecm.server.conf.ECMConfiguration._
import com.webank.wedatasphere.linkis.ecm.server.metrics.DefaultECMMetrics
import org.springframework.stereotype.Component


@Component
class DefaultECMContext extends ECMContext {

  private val emSyncListenerBus = new ECMSyncListenerBus

  private val emAsyncListenerBus = new ECMAsyncListenerBus(ECM_ASYNC_BUS_CAPACITY, ECM_ASYNC_BUS_NAME)(ECM_ASYNC_BUS_CONSUMER_SIZE, ECM_ASYNC_BUS_THREAD_MAX_FREE_TIME)

  private val metrics = new DefaultECMMetrics


  override def getECMAsyncListenerBus: ECMAsyncListenerBus = emAsyncListenerBus

  override def getECMSyncListenerBus: ECMSyncListenerBus = emSyncListenerBus

  override def getECMMetrics: ECMMetrics = metrics
}
