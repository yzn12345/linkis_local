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

package com.webank.wedatasphere.linkis.rpc

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.rpc.conf.RPCConfiguration
import com.webank.wedatasphere.linkis.rpc.sender.{LocalMessageSender, SpringMVCRPCSender}

trait SenderFactory {

  def createSender(serviceInstance: ServiceInstance): Sender

}

object SenderFactory {

  private val senderFactory: SenderFactory = new DefaultSenderFactory

  def getFactory: SenderFactory = {
    senderFactory
  }
}

class DefaultSenderFactory extends SenderFactory with Logging {


  override def createSender(serviceInstance: ServiceInstance): Sender = {
    if (RPCConfiguration.ENABLE_LOCAL_MESSAGE.getValue && RPCConfiguration.LOCAL_APP_LIST.contains(serviceInstance.getApplicationName)) {
      info(s"Start to create local message sender of $serviceInstance")
      new LocalMessageSender(serviceInstance)
    } else {
      new SpringMVCRPCSender(serviceInstance)
    }

  }

}
