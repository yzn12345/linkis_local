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

package com.webank.wedatasphere.linkis.server.socket.controller

import java.util

import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import com.webank.wedatasphere.linkis.server.socket.ServerSocket


class ServerEvent() extends Event {
  private var id: Int = _
  private var method: String = _
  private var data: util.Map[String, Object] = _
  private var user: String = _
  private var websocketTag: String = _
  def setId(id: Int) = this.id = id
  def getId = id
  def setUser(user: String) = this.user = user
  def setMethod(method: String) = this.method = method
  def getMethod = method
  def setData(data: util.Map[String, Object]) = this.data = data
  def getData = data
  def getUser = user
  def setWebsocketTag(websocketTag: String) = this.websocketTag = websocketTag
  def getWebsocketTag = websocketTag
}

class SocketServerEvent(private[controller] val socket: ServerSocket, val message: String) extends Event {
  val serverEvent: ServerEvent = SocketServerEvent.getServerEvent(message)
  socket.user.foreach(serverEvent.setUser)
  serverEvent.setId(socket.id)
}
object SocketServerEvent {
  def getServerEvent(message: String): ServerEvent = BDPJettyServerHelper.gson.fromJson(message, classOf[ServerEvent])
  def getMessageData(serverEvent: ServerEvent): String = BDPJettyServerHelper.gson.toJson(serverEvent.getData)
  def getMessageData(message: String): String = getMessageData(getServerEvent(message))
}