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

package com.webank.wedatasphere.linkis.rpc.sender

import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.rpc.{RPCReceiveRestful, Receiver}
import feign.codec.{Decoder, Encoder}
import feign.{Client, Contract}
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory
import org.springframework.cloud.netflix.ribbon.SpringClientFactory
import org.springframework.cloud.openfeign.FeignClientsConfiguration
import org.springframework.context.annotation.{Configuration, Import}


@Import(Array(classOf[FeignClientsConfiguration]))
@Autowired
@Configuration
@AutoConfigureBefore(Array(classOf[Receiver], classOf[RPCReceiveRestful]))
class SpringCloudFeignConfigurationCache(encoder: Encoder, decoder: Decoder,
                                         contract: Contract, client: Client) {

  @Autowired
  private var discoveryClient: DiscoveryClient = _
  @Autowired
  private var clientFactory: SpringClientFactory = _
  @Autowired(required = false)
  private var loadBalancedRetryFactory: LoadBalancedRetryFactory = _

  @PostConstruct
  def storeFeignConfiguration(): Unit = {
    SpringCloudFeignConfigurationCache.client = client
    SpringCloudFeignConfigurationCache.clientFactory = clientFactory
    SpringCloudFeignConfigurationCache.loadBalancedRetryFactory = loadBalancedRetryFactory
    SpringCloudFeignConfigurationCache.contract = contract
    SpringCloudFeignConfigurationCache.decoder = decoder
    SpringCloudFeignConfigurationCache.encoder = encoder
    SpringCloudFeignConfigurationCache.discoveryClient = discoveryClient
  }

}
private[linkis] object SpringCloudFeignConfigurationCache {
  private[SpringCloudFeignConfigurationCache] var encoder: Encoder = _
  private[SpringCloudFeignConfigurationCache] var decoder: Decoder = _
  private[SpringCloudFeignConfigurationCache] var contract: Contract = _
  private[SpringCloudFeignConfigurationCache] var client: Client = _
  private[SpringCloudFeignConfigurationCache] var clientFactory: SpringClientFactory = _
  private[SpringCloudFeignConfigurationCache] var loadBalancedRetryFactory: LoadBalancedRetryFactory = _
  private[SpringCloudFeignConfigurationCache] var discoveryClient: DiscoveryClient = _
  private val rpcTicketIdRequestInterceptor = new FeignClientRequestInterceptor

  private[rpc] def getEncoder = encoder
  private[rpc] def getDecoder = decoder
  private[rpc] def getContract = contract
  private[rpc] def getClient = {
    if(client == null) DataWorkCloudApplication.getApplicationContext.getBean(classOf[SpringCloudFeignConfigurationCache])
    client
  }
  private[rpc] def getClientFactory = clientFactory
  private[rpc] def getLoadBalancedRetryFactory = loadBalancedRetryFactory
  private[linkis] def getDiscoveryClient = {
    if(discoveryClient == null) DataWorkCloudApplication.getApplicationContext.getBean(classOf[SpringCloudFeignConfigurationCache])
    discoveryClient
  }
  private[rpc] def getRPCTicketIdRequestInterceptor = rpcTicketIdRequestInterceptor
}