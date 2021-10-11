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

package com.webank.wedatasphere.linkis.rpc

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.listener.{Event, EventListener, ListenerEventBus}
import com.webank.wedatasphere.linkis.rpc.errorcode.RPCErrorConstants
import com.webank.wedatasphere.linkis.rpc.exception.{DWCRPCRetryException, RPCInitFailedException}


class AsynRPCMessageBus(capacity: Int, busName: String)
                       (consumerThreadSize: Int, threadMaxFreeTime: Long) extends
  ListenerEventBus[RPCMessageEventListener, RPCMessageEvent](capacity, busName)(consumerThreadSize, threadMaxFreeTime) {

  /**
    * Post an event to the specified listener. `onPostEvent` is guaranteed to be called in the same
    * thread for all listeners.
    */
  override protected def doPostEvent(listener: RPCMessageEventListener, event: RPCMessageEvent): Unit = listener.onEvent(event)

  override protected val dropEvent: DropEvent = new DropEvent {
    override def onDropEvent(event: RPCMessageEvent): Unit = throw new DWCRPCRetryException("Asyn RPC Consumer Queue is full, please retry after some times.")
    override def onBusStopped(event: RPCMessageEvent): Unit = throw new RPCInitFailedException(RPCErrorConstants.RPC_INIT_ERROR, "Asyn RPC Consumer Thread has stopped!")
  }
}

trait RPCMessageEventListener extends EventListener {
  def onEvent(event: RPCMessageEvent): Unit

  def onMessageEventError(event: RPCMessageEvent, t: Throwable): Unit

  override def onEventError(event: Event, t: Throwable): Unit = event match {
    case rpcMessage: RPCMessageEvent => onMessageEventError(rpcMessage, t)
    case _ =>
  }
}

case class RPCMessageEvent(message: Any, serviceInstance: ServiceInstance) extends Event