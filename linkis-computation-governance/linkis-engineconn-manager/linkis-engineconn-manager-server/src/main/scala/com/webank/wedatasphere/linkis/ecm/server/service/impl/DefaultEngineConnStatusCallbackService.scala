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

package com.webank.wedatasphere.linkis.ecm.server.service.impl

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.ecm.server.LinkisECMApplication
import com.webank.wedatasphere.linkis.ecm.server.conf.ECMConfiguration.MANAGER_SPRING_NAME
import com.webank.wedatasphere.linkis.ecm.server.listener.EngineConnStatusChangeEvent
import com.webank.wedatasphere.linkis.ecm.server.service.EngineConnStatusCallbackService
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus.{Failed, Running}
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineConnStatusCallback, EngineConnStatusCallbackToAM}
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.rpc.Sender
import org.springframework.stereotype.Service


@Service
class DefaultEngineConnStatusCallbackService extends EngineConnStatusCallbackService with Logging {

  @Receiver
  override def dealEngineConnStatusCallback(protocol: EngineConnStatusCallback): Unit = {
    info(s"Start to deal EngineConnStatusCallback $protocol")

    if (NodeStatus.isAvailable(protocol.status)) {

      LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(EngineConnStatusChangeEvent(protocol.ticketId, Running))
    } else {

      Sender.getSender(MANAGER_SPRING_NAME).send(EngineConnStatusCallbackToAM(protocol.serviceInstance,
        protocol.status, protocol.initErrorMsg))
      LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(EngineConnStatusChangeEvent(protocol.ticketId, Failed))
    }

    info(s"Finished to deal EngineConnStatusCallback $protocol")
  }
}
