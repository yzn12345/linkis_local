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

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.protocol.BroadcastProtocol
import com.webank.wedatasphere.linkis.rpc.conf.RPCConfiguration.{BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX, BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX, BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY}
import com.webank.wedatasphere.linkis.rpc.exception.DWCURIException
import com.webank.wedatasphere.linkis.rpc.transform.{RPCConsumer, RPCProduct}
import com.webank.wedatasphere.linkis.server.{Message, catchIt}
import javax.annotation.PostConstruct
import javax.ws.rs.core.MediaType
import javax.ws.rs.{Consumes, POST, Path, Produces}
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.duration.Duration
import scala.runtime.BoxedUnit


@Component
@Path("/rpc")
@Produces(Array(MediaType.APPLICATION_JSON))
@Consumes(Array(MediaType.APPLICATION_JSON))
private[rpc] class RPCReceiveRestful extends RPCReceiveRemote with Logging {

  @Autowired(required = false)
  private var receiverChoosers: Array[ReceiverChooser] = Array.empty
  @Autowired(required = false)
  private var receiverSenderBuilders: Array[ReceiverSenderBuilder] = Array.empty
  @Autowired(required = false)
  private var broadcastListeners: Array[BroadcastListener] = Array.empty
  private var rpcReceiverListenerBus: AsynRPCMessageBus = _

  private def getFirst[K, T](buildArray: Array[K], buildObj: K => Option[T]): Option[T] = {
    var obj: Option[T] = None
    for(builder <- buildArray if obj.isEmpty) obj = buildObj(builder)
    obj
  }

  private implicit def getReceiver(event: RPCMessageEvent): Option[Receiver] = getFirst[ReceiverChooser, Receiver](receiverChoosers, _.chooseReceiver(event))

  private implicit def getSender(event: RPCMessageEvent): Sender = getFirst[ReceiverSenderBuilder, Sender](receiverSenderBuilders, _.build(event)).get

  def registerReceiverChooser(receiverChooser: ReceiverChooser): Unit = {
    info("register a new ReceiverChooser " + receiverChooser)
    receiverChoosers = receiverChooser +: receiverChoosers
  }
  def registerBroadcastListener(broadcastListener: BroadcastListener): Unit = {
    broadcastListeners = broadcastListener +: broadcastListeners
    addBroadcastListener(broadcastListener)
  }

  @PostConstruct
  def initListenerBus(): Unit =  {
    if(!receiverChoosers.exists(_.isInstanceOf[CommonReceiverChooser]))
      receiverChoosers = receiverChoosers :+ new CommonReceiverChooser
    info("init all receiverChoosers in spring beans, list => " + receiverChoosers.toList)
    if(!receiverSenderBuilders.exists(_.isInstanceOf[CommonReceiverSenderBuilder]))
      receiverSenderBuilders = receiverSenderBuilders :+ new CommonReceiverSenderBuilder
    receiverSenderBuilders = receiverSenderBuilders.sortBy(_.order)
    info("init all receiverSenderBuilders in spring beans, list => " + receiverSenderBuilders.toList)
    val queueSize = BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY.acquireNew
    val threadSize = BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX.acquireNew
    rpcReceiverListenerBus = new AsynRPCMessageBus(queueSize,
      "RPC-Receiver-Asyn-Thread")(threadSize,
      BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX.getValue.toLong)
    info(s"init RPCReceiverListenerBus with queueSize $queueSize and consumeThreadSize $threadSize.")
    rpcReceiverListenerBus.addListener(new RPCMessageEventListener {
      override def onEvent(event: RPCMessageEvent): Unit = event.message match {
        case _: BroadcastProtocol =>
        case _ =>
          event.fold(warn(s"cannot find a receiver to deal $event."))(_.receive(event.message, event))
      }
      override def onMessageEventError(event: RPCMessageEvent, t: Throwable): Unit =
        warn(s"deal RPC message failed! Message: " + event.message, t)
    })
    broadcastListeners.foreach(addBroadcastListener)
    rpcReceiverListenerBus.start()
  }

  private def addBroadcastListener(broadcastListener: BroadcastListener): Unit = if(rpcReceiverListenerBus != null) {
    info("add a new RPCBroadcastListener => " + broadcastListener.getClass)
    rpcReceiverListenerBus.addListener(new RPCMessageEventListener {
      val listenerName = broadcastListener.getClass.getSimpleName
      override def onEvent(event: RPCMessageEvent): Unit = event.message match {
        case broadcastProtocol: BroadcastProtocol => broadcastListener.onBroadcastEvent(broadcastProtocol, event)
        case _ =>
      }
      override def onMessageEventError(event: RPCMessageEvent, t: Throwable): Unit =
        warn(s"$listenerName consume broadcast message failed! Message: " + event.message, t)
    })
  }

  private implicit def toMessage(obj: Any): Message = obj match {
    case Unit | () =>
      RPCProduct.getRPCProduct.ok()
    case _: BoxedUnit => RPCProduct.getRPCProduct.ok()
    case _ =>
      RPCProduct.getRPCProduct.toMessage(obj)
  }

  @Path("receive")
  @POST
  override def receive(message: Message): Message = catchIt {
    val obj = RPCConsumer.getRPCConsumer.toObject(message)
    val event = RPCMessageEvent(obj, BaseRPCSender.getInstanceInfo(message.getData))
    rpcReceiverListenerBus.post(event)
  }

  private def receiveAndReply(message: Message, opEvent: (Receiver, Any, Sender) => Message): Message = catchIt {
    val obj = RPCConsumer.getRPCConsumer.toObject(message)
    val serviceInstance = BaseRPCSender.getInstanceInfo(message.getData)
    val event = RPCMessageEvent(obj, serviceInstance)
    event.map(opEvent(_, obj, event)).getOrElse(RPCProduct.getRPCProduct.notFound())
  }

  @Path("receiveAndReply")
  @POST
  override def receiveAndReply(message: Message): Message = receiveAndReply(message, _.receiveAndReply(_, _))

  @Path("replyInMills")
  @POST
  override def receiveAndReplyInMills(message: Message): Message = catchIt {
    val duration = message.getData.get("duration")
    if(duration == null || StringUtils.isEmpty(duration.toString)) throw new DWCURIException(10002, "The timeout period is not set!(超时时间未设置！)")
    val timeout = Duration(duration.toString.toLong, TimeUnit.MILLISECONDS)
    receiveAndReply(message, _.receiveAndReply(_, timeout, _))
  }
}