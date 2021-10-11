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

import com.webank.wedatasphere.linkis.message.context.{MessageSchedulerContext, SpringMessageSchedulerContext}
import com.webank.wedatasphere.linkis.message.publisher.{AbstractMessagePublisher, DefaultMessagePublisher, MessagePublisher}
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean


class MessageRPCSpringConfiguration {

  @Bean
  def getPublisher: AbstractMessagePublisher = {
    new DefaultMessagePublisher()
  }

  @Bean
  @ConditionalOnMissingBean
  def getMessageSchedulerContext(messagePublisher: AbstractMessagePublisher): MessageSchedulerContext = {
    val context = new SpringMessageSchedulerContext
    messagePublisher.setContext(context)
    context.setPublisher(messagePublisher)
    context
  }

  @Bean
  def getReceiverChooser(messagePublisher: MessagePublisher): ReceiverChooser = {
    new MessageReceiverChooser(Option(new MessageReceiver(messagePublisher)))
  }

}
