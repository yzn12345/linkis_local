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

package com.webank.wedatasphere.linkis.entranceclient.conf

import com.webank.wedatasphere.linkis.common.utils.Logging
//import com.webank.wedatasphere.linkis.entrance.annotation.EntranceServerBeanAnnotation.EntranceServerAutowiredAnnotation
//import com.webank.wedatasphere.linkis.entrance.annotation._
//import com.webank.wedatasphere.linkis.entrance.conf.EntranceSpringConfiguration
//import com.webank.wedatasphere.linkis.entrance.execute._
//import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor
//import com.webank.wedatasphere.linkis.entrance.log.LogManager
//import com.webank.wedatasphere.linkis.entrance.persistence.PersistenceManager
//import com.webank.wedatasphere.linkis.entrance.{EntranceParser, EntranceServer}
//import com.webank.wedatasphere.linkis.entranceclient.annotation.ClientEngineSelectorBeanAnnotation.ClientEngineSelectorAutowiredAnnotation
//import com.webank.wedatasphere.linkis.entranceclient.annotation.ClientEntranceParserBeanAnnotation.ClientEntranceParserAutowiredAnnotation
//import com.webank.wedatasphere.linkis.entranceclient.annotation.ClientInterceptorsBeanAnnotation.ClientInterceptorsAutowiredAnnotation
//import com.webank.wedatasphere.linkis.entranceclient.annotation.DefaultEntranceClientBeanAnnotation
//import com.webank.wedatasphere.linkis.entranceclient.{EntranceClient, EntranceClientImpl, _}
//import com.webank.wedatasphere.linkis.rpc.{RPCMessageEvent, Receiver, ReceiverChooser}
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
//import org.springframework.boot.autoconfigure.{AutoConfigureAfter, AutoConfigureBefore}
//import org.springframework.context.annotation.{Conditional, Configuration}

/*@Configuration
@AutoConfigureBefore(Array(classOf[EntranceSpringConfiguration]))
@AutoConfigureAfter(Array(classOf[ClientSpringConfiguration]))
@Conditional(Array(classOf[SingleEntranceCondition]))*/
class ClientForEntranceSpringConfiguration extends Logging {

  warn(s"start a single-entrance application...")

  /*@DefaultEntranceClientBeanAnnotation
  def generateEntranceClient(@EntranceServerAutowiredAnnotation entranceServer: EntranceServer): EntranceClient = {
    val client = EntranceClientImpl(ClientConfiguration.CLIENT_DEFAULT_NAME)
    warn(s"ready to initial EntranceClient ${client.getEntranceClientName}...")
    client.init(entranceServer)
    client
  }*/
/*

  @ReceiverChooserBeanAnnotation
  def generateEntranceReceiverChooser(): ReceiverChooser = new ReceiverChooser{
    override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = None
  }

  @PersistenceManagerBeanAnnotation
  @ConditionalOnMissingBean(value = Array(classOf[context.ClientPersistenceManager]))
  def generatePersistenceManager(): PersistenceManager = new context.ClientPersistenceManager

  @EntranceParserBeanAnnotation
  def generateEntranceParser(@ClientEntranceParserAutowiredAnnotation clientEntranceParser: context.ClientEntranceParser): EntranceParser = clientEntranceParser

  @LogManagerBeanAnnotation
  @ConditionalOnMissingBean(value = Array(classOf[context.ClientLogManager]))
  def generateLogManager(): LogManager = new context.ClientLogManager


  @EntranceInterceptorBeanAnnotation
  def generateEntranceInterceptor(@ClientInterceptorsAutowiredAnnotation clientInterceptors: Array[EntranceInterceptor]): Array[EntranceInterceptor] = clientInterceptors
*/


//  @EngineSelectorBeanAnnotation
//  def generateEngineSelector(@ClientEngineSelectorAutowiredAnnotation clientEngineSelector: EngineSelector): EngineSelector = clientEngineSelector


}