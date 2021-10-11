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

package com.webank.wedatasphere.linkis.gateway.parser

import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.gateway.http.{GatewayContext, GatewayRoute}
import com.webank.wedatasphere.linkis.rpc.conf.RPCConfiguration
import com.webank.wedatasphere.linkis.rpc.interceptor.ServiceInstanceUtils
import com.webank.wedatasphere.linkis.server.Message
import com.webank.wedatasphere.linkis.server.conf.ServerConfiguration
import org.apache.commons.lang.StringUtils

trait GatewayParser {

  def shouldContainRequestBody(gatewayContext: GatewayContext): Boolean

  def parse(gatewayContext: GatewayContext): Unit

}
abstract class AbstractGatewayParser extends GatewayParser with Logging {

  protected def sendResponseWhenNotMatchVersion(gatewayContext: GatewayContext, version: String): Boolean = if(version != ServerConfiguration.BDP_SERVER_VERSION) {
    warn(s"Version not match. The gateway(${ServerConfiguration.BDP_SERVER_VERSION}) not support requestUri ${gatewayContext.getRequest.getRequestURI} from remoteAddress ${gatewayContext.getRequest.getRemoteAddress.getAddress.getHostAddress}.")
    sendErrorResponse(s"The gateway${ServerConfiguration.BDP_SERVER_VERSION} not support version $version.", gatewayContext)
    true
  } else false

  protected def sendErrorResponse(errorMsg: String, gatewayContext: GatewayContext): Unit = sendMessageResponse(Message.error(errorMsg), gatewayContext)

  protected def sendMessageResponse(dataMsg: Message, gatewayContext: GatewayContext): Unit = {
    gatewayContext.setGatewayRoute(new GatewayRoute)
    gatewayContext.getGatewayRoute.setRequestURI(gatewayContext.getRequest.getRequestURI)
    dataMsg << gatewayContext.getRequest.getRequestURI
    if(dataMsg.getStatus != 0) warn(dataMsg.getMessage)
    if(gatewayContext.isWebSocketRequest) gatewayContext.getResponse.writeWebSocket(dataMsg)
    else gatewayContext.getResponse.write(dataMsg)
    gatewayContext.getResponse.sendResponse()
  }

  /**
    * Return to the gateway list information(返回gateway列表信息)
    */
  protected def responseHeartbeat(gatewayContext: GatewayContext): Unit = {
    val gatewayServiceInstances = ServiceInstanceUtils.getRPCServerLoader.getServiceInstances(DataWorkCloudApplication.getApplicationName)
    val msg = Message.ok("Gateway heartbeat ok!").data("gatewayList", gatewayServiceInstances.map(_.getInstance)).data("isHealthy", true)
    sendMessageResponse(msg, gatewayContext)
  }
}
object AbstractGatewayParser {
  val GATEWAY_HEART_BEAT_URL = Array("gateway", "heartbeat")
}
class DefaultGatewayParser(gatewayParsers: Array[GatewayParser]) extends AbstractGatewayParser {

  private val COMMON_REGEX = "/api/rest_[a-zA-Z]+/(v\\d+)/([^/]+)/.+".r
  private val CLIENT_HEARTBEAT_REGEX = s"/api/rest_[a-zA-Z]+/(v\\d+)/${AbstractGatewayParser.GATEWAY_HEART_BEAT_URL.mkString("/")}".r

  override def shouldContainRequestBody(gatewayContext: GatewayContext): Boolean = gatewayContext.getRequest.getMethod.toUpperCase != "GET" &&
    (gatewayContext.getRequest.getRequestURI match {
    case uri if uri.startsWith(ServerConfiguration.BDP_SERVER_USER_URI.getValue) => true
    case _ => gatewayParsers.exists(_.shouldContainRequestBody(gatewayContext))
  })

  override def parse(gatewayContext: GatewayContext): Unit = {
    val path = gatewayContext.getRequest.getRequestURI
    if(gatewayContext.getGatewayRoute == null) {
      gatewayContext.setGatewayRoute(new GatewayRoute)
      gatewayContext.getGatewayRoute.setRequestURI(path)
    }
    gatewayParsers.foreach(_.parse(gatewayContext))
    if(gatewayContext.getGatewayRoute.getServiceInstance == null) path match {
      case CLIENT_HEARTBEAT_REGEX(version) =>
        if(sendResponseWhenNotMatchVersion(gatewayContext, version)) return
        info(gatewayContext.getRequest.getRemoteAddress + " try to heartbeat.")
        responseHeartbeat(gatewayContext)
      case COMMON_REGEX(version, serviceId) =>
        if(sendResponseWhenNotMatchVersion(gatewayContext, version)) return
        val applicationName = if(RPCConfiguration.ENABLE_PUBLIC_SERVICE.getValue && RPCConfiguration.PUBLIC_SERVICE_LIST.contains(serviceId))
          RPCConfiguration.PUBLIC_SERVICE_APPLICATION_NAME.getValue else serviceId
        gatewayContext.getGatewayRoute.setServiceInstance(ServiceInstance(applicationName, null))
      case p if p.startsWith("/dws/") =>
        //TODO add version support
        val params = gatewayContext.getGatewayRoute.getParams
        params.put("proxyId", "dws")
        val secondaryProxyId = StringUtils.substringBetween(p, "/dws/", "/")
        if(StringUtils.isNotBlank(secondaryProxyId)){
          params.put("proxyId", "dws/" + secondaryProxyId)
        }
        gatewayContext.getGatewayRoute.setParams(params)
      case _ =>
        sendErrorResponse(s"Cannot find a service to deal $path, ignore it.", gatewayContext)
    }
  }
}