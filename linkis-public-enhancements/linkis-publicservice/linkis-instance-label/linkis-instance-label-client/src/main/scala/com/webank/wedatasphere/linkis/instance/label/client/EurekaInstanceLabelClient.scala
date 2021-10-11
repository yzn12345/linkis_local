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

package com.webank.wedatasphere.linkis.instance.label.client

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.protocol.label.{InsLabelAttachRequest, InsLabelRemoveRequest}
import com.webank.wedatasphere.linkis.rpc.Sender
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.serviceregistry.Registration
import org.springframework.context.event.{ContextClosedEvent, EventListener}
import org.springframework.stereotype.Component


@Component
class EurekaInstanceLabelClient extends Logging {


  @Autowired
  private var registration: Registration = _


  @PostConstruct
  def init(): Unit = {
    info("EurekaInstanceLabelClient init")
    val metadata = registration.getMetadata
    if (null != metadata) {
      info(s"Start to register label for instance $metadata")
      val insLabelAttachRequest = new InsLabelAttachRequest
      insLabelAttachRequest.setLabels(metadata.asInstanceOf[java.util.Map[String, Object]])
      insLabelAttachRequest.setServiceInstance(Sender.getThisServiceInstance)
      InstanceLabelClient.getInstance.attachLabelsToInstance(insLabelAttachRequest)
    }
  }

  @EventListener(classes = Array(classOf[ContextClosedEvent]))
  def shutdown(contextClosedEvent: ContextClosedEvent): Unit = {
    info("To remove labels for instance")
    val insLabelRemoveRequest = new InsLabelRemoveRequest
    insLabelRemoveRequest.setServiceInstance(Sender.getThisServiceInstance)
    InstanceLabelClient.getInstance.removeLabelsFromInstance(insLabelRemoveRequest)
    info("success to send clear label rpc request")
  }


}
