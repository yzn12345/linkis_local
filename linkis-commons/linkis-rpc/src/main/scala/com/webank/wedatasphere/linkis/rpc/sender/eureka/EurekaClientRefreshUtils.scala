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

package com.webank.wedatasphere.linkis.rpc.sender.eureka

import com.netflix.discovery.{EurekaClient, DiscoveryClient => NetflixDiscoveryClient}
import com.webank.wedatasphere.linkis.rpc.conf.RPCConfiguration
import com.webank.wedatasphere.linkis.server.utils.AopTargetUtils
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration


@Configuration
class EurekaClientRefreshUtils {

  @Autowired
  private var eurekaClient: EurekaClient = _
  private var eurekaClientLastRefreshTime = 0l

  val serviceRefreshInterval = RPCConfiguration.BDP_RPC_EUREKA_SERVICE_REFRESH_INTERVAL.getValue.toLong
  private[eureka] def refreshEurekaClient(): Unit = if(System.currentTimeMillis - eurekaClientLastRefreshTime > serviceRefreshInterval) synchronized {
    if (System.currentTimeMillis - eurekaClientLastRefreshTime < serviceRefreshInterval) return
    eurekaClientLastRefreshTime = System.currentTimeMillis
    eurekaClient match {
      case disClient: NetflixDiscoveryClient =>
        val refreshRegistry = classOf[NetflixDiscoveryClient].getDeclaredMethod("refreshRegistry")
        refreshRegistry.setAccessible(true)
        refreshRegistry.invoke(disClient)
        Thread.sleep(100)
      case _ =>
    }
  }

  @PostConstruct
  def storeEurekaClientRefreshUtils(): Unit = {
    eurekaClient = AopTargetUtils.getTarget(eurekaClient).asInstanceOf[EurekaClient]
    EurekaClientRefreshUtils.eurekaClientRefreshUtils = this
  }
}
object EurekaClientRefreshUtils {
  private var eurekaClientRefreshUtils: EurekaClientRefreshUtils = _
  def apply(): EurekaClientRefreshUtils = eurekaClientRefreshUtils
}