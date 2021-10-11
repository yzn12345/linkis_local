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
package com.webank.wedatasphere.linkis.ujes.jdbc

import java.util
import java.util.Properties

import com.webank.wedatasphere.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfigBuilder
import com.webank.wedatasphere.linkis.ujes.client.UJESClient
import com.webank.wedatasphere.linkis.ujes.jdbc.UJESSQLDriverMain._
import org.apache.commons.lang.StringUtils


object UJESClientFactory {

  private val ujesClients = new util.HashMap[String, UJESClient]

  def getUJESClient(props: Properties): UJESClient = {
    val host = props.getProperty(HOST)
    val port = props.getProperty(PORT)
    val serverUrl = if(StringUtils.isNotBlank(port)) s"http://$host:$port" else "http://" + host
    if(ujesClients.containsKey(serverUrl)) ujesClients.get(serverUrl)
    else serverUrl.intern synchronized {
      if(ujesClients.containsKey(serverUrl)) return ujesClients.get(serverUrl)
      val ujesClient = createUJESClient(serverUrl, props)
      ujesClients.put(serverUrl, ujesClient)
      ujesClient
    }
  }

  private def createUJESClient(serverUrl: String, props: Properties): UJESClient = {
    val clientConfigBuilder = DWSClientConfigBuilder.newBuilder()
    clientConfigBuilder.addServerUrl(serverUrl)
    clientConfigBuilder.setAuthTokenKey(props.getProperty(USER))
    clientConfigBuilder.setAuthTokenValue(props.getProperty(PASSWORD))
    clientConfigBuilder.setAuthenticationStrategy(new StaticAuthenticationStrategy())
    clientConfigBuilder.readTimeout(100000)
    clientConfigBuilder.maxConnectionSize(20)
    clientConfigBuilder.readTimeout(10000)
    val params = props.getProperty(PARAMS)
    var versioned = false
    if(StringUtils.isNotBlank(params)) {
      var enableDiscovery = false
      params.split(PARAM_SPLIT).foreach { kv =>
        kv.split(KV_SPLIT) match {
          case Array(VERSION, v) =>
            clientConfigBuilder.setDWSVersion(v)
            versioned = true
          case Array(MAX_CONNECTION_SIZE, v) =>
            clientConfigBuilder.maxConnectionSize(v.toInt)
          case Array(READ_TIMEOUT, v) =>
            clientConfigBuilder.readTimeout(v.toLong)
          case Array(ENABLE_DISCOVERY, v) =>
            clientConfigBuilder.discoveryEnabled(v.toBoolean)
            enableDiscovery = true
          case Array(ENABLE_LOADBALANCER, v) if enableDiscovery =>
            clientConfigBuilder.loadbalancerEnabled(v.toBoolean)
          case _ =>
        }
      }
    }
    if(!versioned) clientConfigBuilder.setDWSVersion("v" + DEFAULT_VERSION)
    UJESClient(clientConfigBuilder.build())
  }

}
